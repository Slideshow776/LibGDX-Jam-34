package no.sandramoen.libgdx34.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;

import no.sandramoen.libgdx34.utils.BaseActor;
import no.sandramoen.libgdx34.utils.BaseGame;

public class Background extends BaseActor {

    public Background(Stage stage) {
        super(0f, 0f, stage);

        loadImage("whitePixel");

        setSize(BaseGame.WORLD_WIDTH + 2, BaseGame.WORLD_HEIGHT + 2);
        setPosition(-1, -1);

        //setColor(new Color(0x264167FF));
        setColor(new Color(0x789accFF));
    }
}
