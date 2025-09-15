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
                game_board.movePlayerIfMatch(typed);
                updateGUI();
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

        float startX = BaseGame.WORLD_WIDTH / 2f - 5f;    // center horizontally
        float startY = BaseGame.WORLD_HEIGHT - 2f - 1;   // start near top

        float margin = 0.5f; // space between cells in world units

        for (int r = 0; r < game_board.rows.size; r++) {
            Array<Cell> row = game_board.rows.get(r);
            Array<CellGUI> guiRow = new Array<>();

            // Create a temporary CellGUI just to get its width/height
            float tempWidth = CellGUI.CELL_SIZE;  // default width, same as setSize(1,1)
            float tempHeight = CellGUI.CELL_SIZE; // default height

            // Horizontal offset to center shorter rows
            float rowOffset = (maxRowLength - row.size) * (tempWidth + margin) / 2f;

            for (int c = 0; c < row.size; c++) {
                Cell cell = row.get(c);

                // Add margin between cells
                float x = startX + (c * (tempWidth + margin)) + rowOffset;
                float y = startY - r * ((tempHeight * margin) + 3*margin); // stagger vertically for hex

                CellGUI cellGUI = new CellGUI(x, y, mainStage, cell.letter);
                cellGUI.setPlayerHere(cell.is_player_here);
                cellGUI.setGoalHere(cell.is_goal_here);

                guiRow.add(cellGUI);
            }

            cell_guis.add(guiRow);
        }
    }


    private void updateGUI() {
        for (int r = 0; r < game_board.rows.size; r++) {
            Array<Cell> row = game_board.rows.get(r);
            Array<CellGUI> guiRow = cell_guis.get(r);

            for (int c = 0; c < row.size; c++) {
                Cell cell = row.get(c);
                CellGUI gui = guiRow.get(c);

                // Reset color
                gui.setColor(1, 1, 1, 1);

                // Highlight player or goal
                if (cell.is_player_here) {
                    gui.setPlayerHere(true);
                } else if (cell.is_goal_here) {
                    gui.setGoalHere(true);
                }

                // Optionally, swap the image if letter changed
                // gui.loadImage("alphabet/" + cell.letter.toLowerCase());
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
