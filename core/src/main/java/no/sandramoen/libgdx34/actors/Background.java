package no.sandramoen.libgdx34.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;

import no.sandramoen.libgdx34.utils.BaseActor;
import no.sandramoen.libgdx34.utils.BaseGame;

public class Background extends BaseActor {

    public Background(float x, float y, Stage stage) {
        super(x, y, stage);

        loadImage("gui/map");
        setSize(BaseGame.WORLD_WIDTH, BaseGame.WORLD_HEIGHT);
        setPosition(0, 0);

        /*setSize(BaseGame.WORLD_WIDTH + 2, BaseGame.WORLD_HEIGHT + 2);
        setPosition(-1, -1);*/

        float colour = 0.8f;
        setColor(new Color(colour, colour, colour, 1.0f));
    }
}
