package no.sandramoen.libgdx34.actors;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;

import no.sandramoen.libgdx34.utils.BaseActor;

public class WaterPickup extends BaseActor {
    public WaterPickup(Stage stage) {
        super(0f, 0f, stage);
        loadImage("water_bottle");
        setSize(0.5f, 0.8f);
        setOrigin(Align.center);
        setBoundaryPolygon(8, 0.8f);
        // setDebug(true);

        float duration = 0.2f;
        addAction(Actions.forever(Actions.sequence(
            Actions.rotateTo(-5f, duration),
            Actions.rotateTo(5f, 2*duration)
        )));
    }

    public void consume() {
        remove();
    }
}
