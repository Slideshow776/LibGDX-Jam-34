package no.sandramoen.libgdx34.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import no.sandramoen.libgdx34.utils.BaseActor;
import no.sandramoen.libgdx34.utils.BaseGame;


public class Player extends BaseActor {

    public boolean is_dead = false;
    private float movementSpeed = 6f;
    private float movementAcceleration = movementSpeed * 4f;


    public Player(float x, float y, Stage s) {
        super(x, y, s);
        loadImage("cross");
        //setDebug(true);

        // body
        setSize(1, 1);
        centerAtPosition(x, y);
        setOrigin(Align.center);
        setBoundaryPolygon(8, 0.5f);

        setWorldBounds(BaseGame.WORLD_WIDTH + 0.5f, BaseGame.WORLD_HEIGHT);

        // movement
        setAcceleration(movementAcceleration);
        setMaxSpeed(movementSpeed);
        setDeceleration(movementAcceleration);
    }


    @Override
    public void act(float delta) {
        super.act(delta);

        if (is_dead)
            return;

        // poll keyboard
        if (Gdx.input.isKeyPressed(Keys.W) || Gdx.input.isKeyPressed(Keys.UP))
            accelerateAtAngle(90f);
        if (Gdx.input.isKeyPressed(Keys.A) || Gdx.input.isKeyPressed(Keys.LEFT))
            accelerateAtAngle(180f);
        if (Gdx.input.isKeyPressed(Keys.S) || Gdx.input.isKeyPressed(Keys.DOWN))
            accelerateAtAngle(270f);
        if (Gdx.input.isKeyPressed(Keys.D) || Gdx.input.isKeyPressed(Keys.RIGHT))
            accelerateAtAngle(0f);

        applyPhysics(delta);
        boundToWorld();
    }


    public boolean isMoving() {
        return getSpeed() > 0.1f; // small threshold to avoid tiny jitter counts as moving
    }


    public void kill() {
        setColor(Color.BLACK);
        is_dead = true;
    }

}
