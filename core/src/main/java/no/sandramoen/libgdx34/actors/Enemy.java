package no.sandramoen.libgdx34.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import no.sandramoen.libgdx34.utils.BaseActor;
import no.sandramoen.libgdx34.utils.BaseGame;

public class Enemy extends BaseActor {


    public Enemy(Stage stage, Array<Array<CellGUI>> cell_guis, float speed) {
        super(0f, 0f, stage);
        loadImage("enemy/enemy");
        setSize(1.5f, 1.5f);
        setOrigin(Align.center);

        // visuals
        setColor(Color.RED);
        float rotation_direction = 1f;
        if (MathUtils.randomBoolean())
            rotation_direction = -1f;
        addAction(Actions.forever(Actions.rotateBy(rotation_direction * 10f, 0.1f)));

        spawn_at_edge(cell_guis, speed);

        // collision
        setBoundaryPolygon(8, 0.5f);
        setDebug(false);
    }

    private void spawn_at_edge(Array<Array<CellGUI>> cell_guis, float speed) {
        if (MathUtils.randomBoolean()) {
            Array<CellGUI> randomRow = cell_guis.random();
            CellGUI randomCell = randomRow.random();

            float rand = MathUtils.random();
            if (rand >= 0.5) { // left to right
                setPosition(-1, randomCell.getY());
                addAction(Actions.moveTo(BaseGame.WORLD_WIDTH + 1, getY(), speed));
            } else { // right to left
                setPosition(BaseGame.WORLD_WIDTH + 1, randomCell.getY());
                addAction(Actions.moveTo(-1, getY(), speed));
            }
        } else {
            // Decide randomly whether to move bottom-to-top or top-to-bottom
            boolean bottomToTop = MathUtils.randomBoolean();

            // bottom-to-top or top-to-bottom along a straight line off screen (random diagonal direction)
            Array<CellGUI> widest_row = cell_guis.get(2);
            int random_index = MathUtils.random(0, widest_row.size - 1);
            CellGUI randomCell = widest_row.get(random_index);

            float x_offset = 3.6f;
            boolean moveRight = MathUtils.randomBoolean(); // random left-right direction
            float startX = randomCell.getX() + (moveRight ? -x_offset * CellGUI.CELL_SIZE : x_offset * CellGUI.CELL_SIZE);
            float startY, finalY;

            if (bottomToTop) {
                // starting position slightly below the screen
                startY = -1;
                finalY = BaseGame.WORLD_HEIGHT+1;
            } else {
                // starting position slightly above the screen
                startY = BaseGame.WORLD_HEIGHT;
                finalY = -1;
            }

            setPosition(startX, startY);

            // compute vector from start to target cell
            float dx = randomCell.getX() - startX;
            float dy = randomCell.getY() - startY;

            // scale the vector to move past the screen edge
            float scale = (finalY - startY) / dy;
            float finalX = startX + dx * scale;

            // move along the straight line off screen
            addAction(Actions.sequence(
                Actions.moveTo(finalX, finalY, speed),
                Actions.removeActor()
            ));
        }
    }
}
