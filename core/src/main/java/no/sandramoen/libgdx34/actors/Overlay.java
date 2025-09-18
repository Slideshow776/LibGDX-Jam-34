package no.sandramoen.libgdx34.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import no.sandramoen.libgdx34.utils.BaseActor;
import no.sandramoen.libgdx34.utils.BaseGame;

public class Overlay extends BaseActor {

    public static final float DURATION = 0.25f;

    public Overlay(Stage stage) {
        super(0f, 0f, stage);

        loadImage("whitePixel");
        setSize(BaseGame.WORLD_WIDTH + 2, BaseGame.WORLD_HEIGHT + 2);
        setPosition(-1, -1);

        setColor(Color.BLACK);
        getColor().a = 1.0f;
        addAction(Actions.alpha(0.0f, DURATION));
    }
}
