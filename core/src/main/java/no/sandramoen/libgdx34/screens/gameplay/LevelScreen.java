package no.sandramoen.libgdx34.screens.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Array;

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

    private int win_count = 0;
    private float time = 0f;
    private boolean is_game_over = false;

    private float enemy_spawn_counter = 0f;
    private float enemy_spawn_frequency = 7f;
    private float enemy_spawn_decrement = 1.0f;
    private float enemy_speed = 10.0f;
    private final float ENEMY_SPAWN_MIN_FREQUENCY = 1.75f;
    private final float MAX_ENEMY_SPEED = 5f;



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

        if (is_game_over)
            return;

        handle_collision_detection();
        handle_enemy_spawning(delta);
    }


    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Keys.ESCAPE)
            Gdx.app.exit();
        else if (
            (keycode == Keys.F1 || is_game_over) &&
                time > 1.0f
        )
            restart();
        else {
            char typed = keycodeToChar(keycode);
            if (typed != 0) {
                if (!game_board.movePlayerIfMatch(typed)) {
                    // Find the CellGUI with the matching letter
                    for (int r = 0; r < game_board.rows.size; r++) {
                        Array<Cell> row = game_board.rows.get(r);
                        Array<CellGUI> guiRow = cell_guis.get(r);

                        for (int c = 0; c < row.size; c++) {
                            Cell cell = row.get(c);
                            if (cell.letter.equalsIgnoreCase(String.valueOf(typed)))
                                guiRow.get(c).showError();
                        }
                    }
                }
                boolean is_new_letters = false;
                if (game_board.checkPlayerReachedGoalAndShuffle()) {
                    win_count++;
                    is_new_letters = true;
                    AssetLoader.new_letters_sound.play(BaseGame.soundVolume * 0.25f);
                }
                updateGUI(is_new_letters);
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
                            }
                        }
                    }
                }
            }
        }

    }


    private void set_game_over() {
        is_game_over = true;
        time = 0f;

        // First hide all cells
        for (Array<CellGUI> row : cell_guis) {
            for (CellGUI cell_gui : row) {
                cell_gui.setVisible(false);
            }
        }

        // Row indices to display text (ensure your board has at least 3 rows)
        int row1Index = 1;
        int row2Index = 2;

        String textRow1 = "GAME";
        String textRow2 = "OVER!";

        // Show "GAME" on row 1, centered
        if (row1Index < cell_guis.size) {
            Array<CellGUI> row1 = cell_guis.get(row1Index);

            int startIndex = (row1.size - textRow1.length()) / 2;
            for (int i = 0; i < textRow1.length() && startIndex + i < row1.size; i++) {
                CellGUI cell_gui = row1.get(startIndex + i);
                cell_gui.setVisible(true);
                cell_gui.setLetter(String.valueOf(textRow1.charAt(i)), CellGUI.Font.BOWLBY);
            }
        }

        // Show "OVER!" on row 2, centered
        if (row2Index < cell_guis.size) {
            Array<CellGUI> row2 = cell_guis.get(row2Index);

            int startIndex = (row2.size - textRow2.length()) / 2;
            for (int i = 0; i < textRow2.length() && startIndex + i < row2.size; i++) {
                CellGUI cell_gui = row2.get(startIndex + i);
                cell_gui.setVisible(true);
                cell_gui.setLetter(String.valueOf(textRow2.charAt(i)), CellGUI.Font.METAL_MANIA);
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
        float startY = BaseGame.WORLD_HEIGHT - 2f - 2.25f;   // start near top

        float margin_x = 1.25f;
        float margin_y = 0.5f;

        boolean hasAssignedPlayer = false, hasAssignedGoal = false;

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
                    cellGUI.is_player = false;
                    cellGUI.setPlayerHere(true);
                    cellGUI.setGoalHere(false);
                } else {
                    cellGUI.setPlayerHere(false);
                    cellGUI.setGoalHere(cell.is_goal_here);
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
                } else {
                    gui.setPlayerHere(false);
                    gui.setGoalHere(false);
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
        //scoreLabel = new TextraLabel("0", AssetLoader.getLabelStyle("Play-Bold59white"));
        //scoreLabel.setAlignment(Align.center);


        uiTable.defaults()
            .padTop(Gdx.graphics.getHeight() * .02f)
        ;

        /*uiTable.add(scoreLabel).center()
            .height(scoreLabel.getPrefHeight() * 1.5f)
            .padTop(-Gdx.graphics.getHeight() * 0.005f)
            .row()
        ;*/
    }
}
