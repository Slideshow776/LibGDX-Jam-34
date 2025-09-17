package no.sandramoen.libgdx34.screens.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.github.tommyettinger.textra.TextraLabel;

import no.sandramoen.libgdx34.actors.Background;
import no.sandramoen.libgdx34.actors.CellGUI;
import no.sandramoen.libgdx34.actors.Enemy;
import no.sandramoen.libgdx34.actors.Overlay;
import no.sandramoen.libgdx34.utils.AssetLoader;
import no.sandramoen.libgdx34.utils.BaseActor;
import no.sandramoen.libgdx34.utils.BaseGame;
import no.sandramoen.libgdx34.utils.BaseScreen;
import no.sandramoen.libgdx34.utils.Cell;
import no.sandramoen.libgdx34.utils.GameBoard;

public class LevelScreen extends BaseScreen {

    private Background map_background;
    private BaseActor overlay;
    private GameBoard game_board;
    private Array<Array<CellGUI>> cell_guis;

    private float time = 0f;
    private final float RESTART_DELAY_DURATION = 1.5f;
    private boolean is_game_over = false;
    private boolean is_game_started = false;

    private int key_count = 0;
    private final int NUM_KEYS_TO_GET = 7;
    private boolean has_current_key = false;

    private float enemy_spawn_counter = 0f;
    private float enemy_spawn_decrement = 1.0f;
    private float enemy_speed = 10.0f;
    private final float ENEMY_SPAWN_MIN_FREQUENCY = 1.75f;
    private float enemy_spawn_frequency = ENEMY_SPAWN_MIN_FREQUENCY;
    private final float MAX_ENEMY_SPEED = 5f;

    private Array<Image> key_images;


    @Override
    public void initialize() {
        // background
        map_background = new Background(mainStage);
        //map_background.getColor().a = 0.0f;

        initialize_gui();
        //GameUtils.playLoopingMusic(AssetLoader.levelMusic);

        overlay = new Overlay(mainStage);

        game_board = new GameBoard();
        cell_guis = new Array<>();
        renderGameBoard();
    }


    @Override
    public void update(float delta) {
        time += delta;

        if (is_game_over || !is_game_started)
            return;

        handle_collision_detection();
        handle_enemy_spawning(delta);
    }


    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Keys.ESCAPE)
            Gdx.app.exit();
        else if ((keycode == Keys.F1 || is_game_over) && time > 1.0f)
            restart();
        else if (keycode == Keys.SLASH){
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
                        AssetLoader.game_start_sound.play(BaseGame.soundVolume);
                    is_game_started = true;

                    if (game_board.checkIfKey()) {
                        has_current_key = true;
                        AssetLoader.key_sound.play(BaseGame.soundVolume, MathUtils.random(0.8f, 1.2f), 0f);
                    }

                    boolean is_new_letters = false;
                    if (game_board.checkPlayerReachedGoalAndShuffle() && has_current_key) {
                        key_count++;
                        is_new_letters = true;
                        game_board.placeRandomKey();
                        has_current_key = false;
                        AssetLoader.new_letters_sound.play(BaseGame.soundVolume * 0.25f);
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
            Enemy enemy = new Enemy(mainStage, cell_guis);
            enemy.speed = enemy_speed;
            enemy_speed *= 0.9f;
            if (enemy_speed < MAX_ENEMY_SPEED)
                enemy_speed = MAX_ENEMY_SPEED;

            enemy_spawn_counter = 0f;
            enemy_spawn_frequency -= enemy_spawn_decrement;
            if (enemy_spawn_frequency < ENEMY_SPAWN_MIN_FREQUENCY)
                enemy_spawn_frequency = ENEMY_SPAWN_MIN_FREQUENCY;
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


    private void set_game_over() {
        is_game_over = true;
        is_game_started = false;
        has_current_key = false;
        time = 0f;
        key_count = 0;

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


    private void restart() {
        BaseGame.setActiveScreen(new LevelScreen());
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
                if (cell.is_player_here) {
                    cellGUI.is_player = false; // HACK: needs to be false to work, actually means true
                    cellGUI.setPlayerHere(true);
                } else if (cell.is_goal_here) {
                    cellGUI.setGoalHere(true);
                } else if(cell.is_key_here) {
                    cellGUI.setKeyHere(true);
                } else {
                    cellGUI.setPlayerHere(false);
                }


                guiRow.add(cellGUI);
            }

            cell_guis.add(guiRow);
        }
    }


    private void updateGUI(boolean is_new_letters) {
        for (int r = 0; r < game_board.rows.size; r++) {
            Array<Cell> row = game_board.rows.get(r);
            Array<CellGUI> guiRow = cell_guis.get(r);

            for (int c = 0; c < row.size; c++) {
                Cell cell = row.get(c);
                CellGUI gui = guiRow.get(c);

                // Highlight player or goal
                if (cell.is_player_here) {
                    gui.setPlayerHere(true);
                } else if (cell.is_goal_here) {
                    gui.setGoalHere(true);
                } else if (cell.is_key_here) {
                    gui.setKeyHere(true);
                } else {
                    gui.setPlayerHere(false);
                    gui.setGoalHere(false);
                    gui.setKeyHere(false);
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
        /*TextraLabel scoreLabel = new TextraLabel("banana phone", AssetLoader.getLabelStyle("Play-Bold59white"));
        scoreLabel.setAlignment(Align.center);*/

        key_images = new Array<>();
        for (int i = 0; i < 7; i++) {
            Image image = new Image(AssetLoader.textureAtlas.findRegion("key"));
            image.setColor(new Color(0f, 0f, 0f, 0.2f));
            key_images.add(image);
        }

        uiTable.defaults()
            .padTop(Gdx.graphics.getHeight() * .02f)
        ;

        for (Image key_image : key_images)
            uiTable.add(key_image)
                .width(Gdx.graphics.getWidth() * 0.075f)
                .height(Gdx.graphics.getHeight() * 0.075f)
                .expandY()
                .top()
            ;

        /*uiTable.add(scoreLabel).center()
            .height(scoreLabel.getPrefHeight() * 1.5f)
            .padTop(-Gdx.graphics.getHeight() * 0.005f)
            .row()
        ;*/

        //uiTable.setDebug(true);
    }
}
