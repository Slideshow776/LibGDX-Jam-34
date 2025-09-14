package no.sandramoen.libgdx34.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import no.sandramoen.libgdx34.utils.BaseActor;

public class MapLine extends BaseActor {

    public MapLine(float x, float y, Stage stage) {
        super(x, y, stage);
        loadImage("whitePixel");
        setColor(Color.BLACK);
        setOpacity(0.5f);
        setSize(.25f, 0.125f);

        addAction(Actions.sequence(
            Actions.delay(2f),
            Actions.fadeOut(1f),
            Actions.removeActor()
        ));
    }
}
