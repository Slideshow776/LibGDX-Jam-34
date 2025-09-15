package no.sandramoen.libgdx34.screens.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.Array;

import no.sandramoen.libgdx34.actors.Background;
import no.sandramoen.libgdx34.actors.CellGUI;
import no.sandramoen.libgdx34.actors.Overlay;
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
    public void update(float _delta) {
    }


    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Keys.ESCAPE)
            Gdx.app.exit();
        else if (keycode == Keys.F1)
            BaseGame.setActiveScreen(new LevelScreen());
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
                if (cell.is_player_here)
                    cellGUI.is_player = false;
                cellGUI.setPlayerHere(cell.is_player_here);
                cellGUI.setGoalHere(cell.is_goal_here);

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

                if (is_new_letters)
                    gui.setLetter(cell.letter);
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
