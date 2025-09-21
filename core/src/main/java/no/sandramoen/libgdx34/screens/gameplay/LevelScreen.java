package no.sandramoen.libgdx34.screens.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.github.tommyettinger.textra.TextraLabel;

import no.sandramoen.libgdx34.actors.Background;
import no.sandramoen.libgdx34.actors.CellGUI;
import no.sandramoen.libgdx34.actors.Enemy;
import no.sandramoen.libgdx34.actors.Overlay;
import no.sandramoen.libgdx34.screens.shell.MenuScreen;
import no.sandramoen.libgdx34.utils.AssetLoader;
import no.sandramoen.libgdx34.utils.BaseActor;
import no.sandramoen.libgdx34.utils.BaseGame;
import no.sandramoen.libgdx34.utils.BaseScreen;
import no.sandramoen.libgdx34.utils.Cell;
import no.sandramoen.libgdx34.utils.GameBoard;
import no.sandramoen.libgdx34.utils.GameUtils;

public class LevelScreen extends BaseScreen {

    private Background background;
    private BaseActor overlay;
    private GameBoard game_board;
    private Array<Array<CellGUI>> cell_guis;

    private float time = 0f;
    private float game_time = 0f;
    private final float RESTART_DELAY_DURATION = 1.0f;
    private boolean is_game_over = false;
    private boolean is_game_started = false;

    private int num_collected_keys = 0;
    private int num_keys_to_get = 0;
    private boolean has_current_key = false;

    private float enemy_spawn_counter = 0f;
    private float enemy_spawn_decrement = 1.0f;
    private float enemy_speed = 0f;
    private float enemy_spawn_min_frequency = 1.0f;
    private float enemy_spawn_frequency = enemy_spawn_min_frequency;
    private final float MAX_ENEMY_SPEED = 5f;

    private Array<Image> key_images;
    private TextraLabel score_label;
    private TextraLabel high_score_label;


    public LevelScreen() {
        if (BaseGame.current_difficulty == BaseGame.Difficulty.EASY) {
            enemy_speed = 20.0f;
            enemy_spawn_decrement = 0.25f;
            enemy_spawn_min_frequency = 1.5f;
            enemy_spawn_frequency = enemy_spawn_min_frequency * 5;
        } else if (BaseGame.current_difficulty == BaseGame.Difficulty.MEDIUM) {
            enemy_speed = 10.0f;
            enemy_spawn_decrement = 0.2f;
            enemy_spawn_min_frequency = 1.5f;
            enemy_spawn_frequency = enemy_spawn_min_frequency * 3;
        } else if (BaseGame.current_difficulty == BaseGame.Difficulty.HARD) {
            enemy_speed = 5.0f;
            enemy_spawn_decrement = 0.25f;
            enemy_spawn_min_frequency = 1.0f;
            enemy_spawn_frequency = enemy_spawn_min_frequency * 2;
        }
    }


    @Override
    public void initialize() {
        background = new Background(mainStage);

        if (BaseGame.current_difficulty == BaseGame.Difficulty.EASY) {
            num_keys_to_get = 5;
        } else if (BaseGame.current_difficulty == BaseGame.Difficulty.MEDIUM) {
            num_keys_to_get = 10;
        } else if (BaseGame.current_difficulty == BaseGame.Difficulty.HARD) {
            num_keys_to_get = 20;
        }

        initialize_gui();
        //GameUtils.playLoopingMusic(AssetLoader.levelMusic);

        if (BaseGame.high_score <= 0) {
            BaseGame.high_score = Integer.MAX_VALUE;
        }
        high_score_label.setText(BaseGame.high_score == Integer.MAX_VALUE ? "--" : BaseGame.high_score + "s");

        game_board = new GameBoard();
        cell_guis = new Array<>();
        renderGameBoard();

        overlay = new Overlay(mainStage);
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);
    }


    @Override
    public void update(float delta) {
        time += delta;

        if (is_game_over || !is_game_started)
            return;

        game_time += delta;

        updateScoreLabel();

        handle_collision_detection();
        handle_enemy_spawning(delta);
    }


    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Keys.ESCAPE && time > RESTART_DELAY_DURATION) {
            overlay.addAction(Actions.sequence(
                Actions.alpha(1.0f, Overlay.DURATION),
                Actions.run(() -> BaseGame.setActiveScreen(new MenuScreen()))
            ));
        } else if (is_game_over && time > RESTART_DELAY_DURATION) {
            GameBoard.SEED = game_board.random.nextLong();
            restart();
        } else if (!is_game_over) {
            char typed = keycodeToChar(keycode);
            if (typed != 0) {
                if (!game_board.movePlayerIfMatch(typed, has_current_key)) { // wrong letter was typed
                    for (int r = 0; r < game_board.rows.size; r++) {
                        Array<Cell> row = game_board.rows.get(r);
                        Array<CellGUI> guiRow = cell_guis.get(r);

                        for (int c = 0; c < row.size; c++) {
                            Cell cell = row.get(c);
                            if (cell.letter.equalsIgnoreCase(String.valueOf(typed)))
                                guiRow.get(c).showError();
                        }
                    }
                } else { // correct letter was typed
                    if (!is_game_started)
                        AssetLoader.game_start_sound.play(BaseGame.soundVolume * 0.25f);
                    is_game_started = true;

                    if (game_board.checkIfKey()) {
                        has_current_key = true;
                        AssetLoader.key_sound.play(BaseGame.soundVolume, MathUtils.random(0.8f, 1.2f), 0f);
                        num_collected_keys++;
                        updateKeyImages();
                    }

                    //AssetLoader.locked_sound.play(BaseGame.soundVolume, MathUtils.random(0.8f, 1.2f), 0f);
                    boolean is_new_letters = false;
                    if (game_board.checkPlayerReachedGoalAndShuffle() && has_current_key) {
                        is_new_letters = true;
                        game_board.placeRandomKey();
                        has_current_key = false;
                        AssetLoader.door_open_sound.play(BaseGame.soundVolume * 0.4f, MathUtils.random(0.9f, 1.4f), 0f);
                        if (num_collected_keys >= key_images.size)
                            set_win();
                    }
                    updateGUI(is_new_letters);
                }
            }
        }
        return super.keyDown(keycode);
    }


    private char keycodeToChar(int keycode) {
        if (keycode >= Keys.A && keycode <= Keys.Z) {
            return (char) ('A' + (keycode - Keys.A));
        }
        return 0;
    }


    private void handle_enemy_spawning(float delta) {
        enemy_spawn_counter += delta;
        if (enemy_spawn_counter >= enemy_spawn_frequency) {
            new Enemy(mainStage, cell_guis, enemy_speed);
            enemy_speed *= 0.9f;
            if (enemy_speed < MAX_ENEMY_SPEED)
                enemy_speed = MAX_ENEMY_SPEED;

            enemy_spawn_counter = 0f;
            enemy_spawn_frequency -= enemy_spawn_decrement;
            if (enemy_spawn_frequency < enemy_spawn_min_frequency)
                enemy_spawn_frequency = enemy_spawn_min_frequency;
            enemy_spawn_decrement *= 1.05f;

            //System.out.println("freq: " + enemy_spawn_frequency + ", speed: " + enemy_speed);
        }
    }


    private void handle_collision_detection() {
        for (Array<CellGUI> row : cell_guis) {
            for (CellGUI cell_gui : row) {
                cell_gui.markDangerous(false);
            }
        }

        for (int i = 0; i < mainStage.getActors().size; i++) {
            Actor actor = mainStage.getActors().get(i);
            if (actor instanceof Enemy) {
                Enemy enemy = (Enemy) actor;

                for (int r = 0; r < cell_guis.size; r++) {
                    Array<CellGUI> row = cell_guis.get(r);
                    for (int c = 0; c < row.size; c++) {
                        CellGUI cell_gui = row.get(c);

                        if (enemy.overlaps(cell_gui)) {
                            //cell_gui.markDangerous(true);
                            if (cell_gui.is_player) {
                                set_game_over();
                                cell_gui.addAction(Actions.parallel(
                                    Actions.scaleTo(20f, 20f, 0.25f),
                                    Actions.fadeOut(0.25f)
                                ));
                            }
                        }
                    }
                }
            }
        }

    }


    private void updateScoreLabel() {
        int seconds = (int) game_time;
        score_label.setText(seconds + "s");
    }


    private void updateHighScoreIfNeeded() {
        int currentScore = (int) game_time;
        if (currentScore < BaseGame.high_score) {
            BaseGame.high_score = currentScore;
            high_score_label.setText(BaseGame.high_score + "s");
        }
    }


    private void set_game_over() {
        is_game_over = true;
        time = 0f;
        AssetLoader.game_over_sound.play(BaseGame.soundVolume);

        // Fade all cells out (but keep their actions like wobble)
        for (Array<CellGUI> row : cell_guis) {
            for (CellGUI cell_gui : row) {
                cell_gui.setVisible(true);
                cell_gui.addAction(Actions.sequence(
                    //Actions.delay(0.25f),
                    Actions.fadeIn(0.25f),
                    Actions.scaleTo(1f, 1f, 0f),
                Actions.fadeOut(0.5f)
                ));
            }
        }

        int row1Index = 1;
        int row2Index = 2;

        String textRow1 = "GAME";
        String textRow2 = "OVER!";

        // Fade in row 1 text
        if (row1Index < cell_guis.size) {
            Array<CellGUI> row1 = cell_guis.get(row1Index);
            int startIndex = (row1.size - textRow1.length()) / 2;

            for (int i = 0; i < textRow1.length() && startIndex + i < row1.size; i++) {
                CellGUI cell_gui = row1.get(startIndex + i);
                int finalI = i;
                cell_gui.addAction(Actions.sequence(
                    Actions.delay(1f),
                    Actions.run(() -> cell_gui.setLetter(String.valueOf(textRow1.charAt(finalI)), CellGUI.Font.METAL_MANIA)),
                    Actions.delay(i * 0.25f),  // staggered effect
                    Actions.alpha(1f, 0.3f)  // fade in smoothly
                ));
            }
        }

        // Fade in row 2 text
        if (row2Index < cell_guis.size) {
            Array<CellGUI> row2 = cell_guis.get(row2Index);
            int startIndex = (row2.size - textRow2.length()) / 2;

            for (int i = 0; i < textRow2.length() && startIndex + i < row2.size; i++) {
                CellGUI cell_gui = row2.get(startIndex + i);
                int finalI = i;
                cell_gui.addAction(Actions.sequence(
                    Actions.delay(1f),
                    Actions.run(() -> cell_gui.setLetter(String.valueOf(textRow2.charAt(finalI)), CellGUI.Font.METAL_MANIA)),
                    Actions.delay((i * 0.1f) + 0.4f), // row 2 comes after row 1
                    Actions.alpha(1f, 0.3f)
                ));
            }
        }
    }


    private void set_win() {
        is_game_over = true;
        time = 0f;
        updateHighScoreIfNeeded();
        AssetLoader.game_over_sound.play(BaseGame.soundVolume);

        // Fade all cells out (but keep their actions like wobble)
        for (Array<CellGUI> row : cell_guis) {
            for (CellGUI cell_gui : row) {
                cell_gui.setVisible(true);
                cell_gui.addAction(Actions.sequence(
                    //Actions.delay(0.25f),
                    Actions.fadeIn(0.25f),
                    Actions.scaleTo(1f, 1f, 0f),
                    Actions.fadeOut(0.5f)
                ));
            }
        }

        int row1Index = 1;
        int row2Index = 2;

        String textRow1 = "YOU";
        String textRow2 = "WON!";

        // Fade in row 1 text
        if (row1Index < cell_guis.size) {
            Array<CellGUI> row1 = cell_guis.get(row1Index);
            int startIndex = (row1.size - textRow1.length()) / 2;

            for (int i = 0; i < textRow1.length() && startIndex + i < row1.size; i++) {
                CellGUI cell_gui = row1.get(startIndex + i);
                int finalI = i;
                cell_gui.addAction(Actions.sequence(
                    Actions.delay(1f),
                    Actions.run(() -> cell_gui.setLetter(String.valueOf(textRow1.charAt(finalI)), CellGUI.Font.METAL_MANIA)),
                    Actions.delay(i * 0.25f),  // staggered effect
                    Actions.alpha(1f, 0.3f)  // fade in smoothly
                ));
            }
        }

        // Fade in row 2 text
        if (row2Index < cell_guis.size) {
            Array<CellGUI> row2 = cell_guis.get(row2Index);
            int startIndex = (row2.size - textRow2.length()) / 2;

            for (int i = 0; i < textRow2.length() && startIndex + i < row2.size; i++) {
                CellGUI cell_gui = row2.get(startIndex + i);
                int finalI = i;
                cell_gui.addAction(Actions.sequence(
                    Actions.delay(1f),
                    Actions.run(() -> cell_gui.setLetter(String.valueOf(textRow2.charAt(finalI)), CellGUI.Font.METAL_MANIA)),
                    Actions.delay((i * 0.1f) + 0.4f), // row 2 comes after row 1
                    Actions.alpha(1f, 0.3f)
                ));
            }
        }
    }


    private void restart() {
        is_game_started = false;
        has_current_key = false;
        game_time = 0f;
        num_collected_keys = 0;
        updateKeyImages();

        overlay.addAction(Actions.sequence(
            Actions.alpha(1.0f, Overlay.DURATION),
            Actions.run(() -> BaseGame.setActiveScreen(new LevelScreen()))
        ));
    }


    private void renderGameBoard() {
        int maxRowLength = 0;
        for (int r = 0; r < game_board.rows.size; r++)
            if (game_board.rows.get(r).size > maxRowLength)
                maxRowLength = game_board.rows.get(r).size;

        cell_guis.clear();

        float startX = BaseGame.WORLD_WIDTH / 2f - 6f;    // center horizontally
        float startY = BaseGame.WORLD_HEIGHT - 2f - 2.25f - 1.25f;   // start near top

        float margin_x = 1.25f;
        float margin_y = 0.5f;

        boolean placedPlayer = false, placedGoal = false;

        for (int r = 0; r < game_board.rows.size; r++) {
            Array<Cell> row = game_board.rows.get(r);
            Array<CellGUI> guiRow = new Array<>();

            // Create a temporary CellGUI just to get its width/height
            float tempWidth = CellGUI.CELL_SIZE;  // default width, same as setSize(1,1)
            float tempHeight = CellGUI.CELL_SIZE; // default height

            // Horizontal offset to center shorter rows
            float rowOffset = (maxRowLength - row.size) * (tempWidth + margin_x) / 2f;

            for (int c = 0; c < row.size; c++) {
                Cell cell = row.get(c);

                // Add margin between cells
                float x = startX + (c * (tempWidth + margin_x)) + rowOffset;
                float y = startY - r * ((tempHeight * margin_y) + 3 * margin_y); // stagger vertically for hex

                CellGUI cellGUI = new CellGUI(x, y, mainStage, cell.letter);
                if (cell.is_player_here && !placedPlayer) {
                    cellGUI.is_player = false; // HACK: needs to be false to work, actually means true
                    cellGUI.setPlayerHere(true);
                    placedPlayer = true;
                } else if (cell.is_goal_here && !placedGoal) {
                    cellGUI.setGoalHere(true);
                    placedGoal = true;
                } else if(cell.is_key_here) {
                    cellGUI.setKeyHere(true, key_images.get(num_collected_keys).getColor());
                } else {
                    cellGUI.setPlayerHere(false);
                    cellGUI.setGoalHere(false);
                }


                guiRow.add(cellGUI);
            }

            cell_guis.add(guiRow);
        }
    }


    private void updateKeyImages() {
        for (int i = 0; i < key_images.size; i++) {
            if (i < num_collected_keys) {
                key_images.get(i).getColor().a = 1.0f;
            } else {
                key_images.get(i).getColor().a = 0.15f;
            }
        }
    }


    private void updateGUI(boolean is_new_letters) {
        boolean placedPlayer = false, placedGoal = false;
        for (int r = 0; r < game_board.rows.size; r++) {
            Array<Cell> row = game_board.rows.get(r);
            Array<CellGUI> guiRow = cell_guis.get(r);

            for (int c = 0; c < row.size; c++) {
                Cell cell = row.get(c);
                CellGUI gui = guiRow.get(c);

                // Highlight player or goal
                if (cell.is_player_here && !placedPlayer) {
                    gui.setPlayerHere(true);
                    gui.setGoalHere(false);
                    placedPlayer = true;
                } else if (cell.is_goal_here && !placedGoal) {
                    gui.setPlayerHere(false);
                    gui.setGoalHere(true);
                    placedGoal = true;
                } else if (cell.is_key_here && num_collected_keys < key_images.size) {
                    gui.setPlayerHere(false);
                    gui.setGoalHere(false);
                    gui.setKeyHere(true, key_images.get(num_collected_keys).getColor());
                } else {
                    gui.setPlayerHere(false);
                    gui.setGoalHere(false);
                    gui.setKeyHere(false, Color.WHITE);
                }

                if (is_new_letters) {
                    if (cell.is_goal_here)
                        gui.setLetter(cell.letter, CellGUI.Font.BOWLBY);
                    else if(cell.is_player_here)
                        gui.setLetter(cell.letter, CellGUI.Font.METAL_MANIA);
                    else
                        gui.setLetter(cell.letter, CellGUI.Font.ALEGREYA);
                }
            }
        }
    }


    private void initialize_gui() {
        // resources setup
        float label_scale = 1.0f;
        Image score_image = new Image(AssetLoader.textureAtlas.findRegion("clock"));
        score_label = new TextraLabel("0s", AssetLoader.getLabelStyle("Alegreya40white"));
        score_label.getFont().scale(label_scale);
        score_label.setColor(Color.BLACK);
        score_label.setAlignment(Align.center);

        Image high_score_image = new Image(AssetLoader.textureAtlas.findRegion("crown"));
        high_score_label = new TextraLabel(BaseGame.high_score + "s", AssetLoader.getLabelStyle("Alegreya40white"));
        high_score_label.getFont().scale(label_scale);
        high_score_label.setColor(Color.BLACK);
        high_score_label.setAlignment(Align.center);


        key_images = new Array<>();
        for (int i = 0; i < num_keys_to_get; i++) {
            Image image = new Image(AssetLoader.textureAtlas.findRegion("key"));
            image.setColor(GameUtils.randomLightColor());
            key_images.add(image);
        }

        updateKeyImages();

        // ui setup
        uiTable.defaults()
            .padTop(Gdx.graphics.getHeight() * .02f)
        ;

        Table temp = new Table();

        // score
        temp.add(score_image)
            .width(Gdx.graphics.getWidth() * 0.05f)
            .height(Gdx.graphics.getHeight() * 0.05f)
            .padRight(Gdx.graphics.getWidth() * 0.01f)
            .padTop(Gdx.graphics.getHeight() * 0.01f)
        ;

        temp.add(score_label)
            .center()
            .padRight(Gdx.graphics.getWidth() * 0.02f)
        ;

        Table temp2 = new Table();
        // keys
        for (int i = 0; i < key_images.size; i++) {
            if (i > 0 && i % 10 == 0)
                temp2.row();

            temp2.add(key_images.get(i))
                .width(Gdx.graphics.getWidth() * 0.075f)
                .height(Gdx.graphics.getHeight() * 0.075f)
                .center()
            ;
        }
        temp.add(temp2);

        // high score
        temp.add(high_score_image)
            .width(Gdx.graphics.getWidth() * 0.05f)
            .height(Gdx.graphics.getHeight() * 0.05f)
            .padRight(Gdx.graphics.getWidth() * 0.01f)
            .padLeft(Gdx.graphics.getWidth() * 0.02f)
            .padTop(Gdx.graphics.getHeight() * 0.01f)
        ;

        temp.add(high_score_label)
            .center()
            .height(high_score_label.getPrefHeight() * 1.5f)
        ;

        uiTable.add(temp)
            .expandY()
            .top()
        ;

        //uiTable.setDebug(true);
    }
}
