package no.sandramoen.libgdx34.actors;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;

import no.sandramoen.libgdx34.actors.particles.EffectEnemyMovement;
import no.sandramoen.libgdx34.utils.AssetLoader;
import no.sandramoen.libgdx34.utils.BaseActor;
import no.sandramoen.libgdx34.utils.BaseGame;


public class Enemy extends BaseActor {

    private static final float MIN_MOVEMENT_SPEED = 2f;
    private static final float MAX_MOVEMENT_SPEED = 8f;

    private float movementSpeed = 10f;
    private float movementAcceleration = movementSpeed * 10f;
    private float angle = MathUtils.random(0f, 360f);
    public Music purrSound = AssetLoader.catPurrMusic;

    private EffectEnemyMovement effect;
    public boolean is_dead = false;


    public Enemy(Stage s) {
        super(0f, 0f, s);

        loadImage("cat_normal");

        // body
        float width = MathUtils.random(0.75f, 1.25f);
        float height = 2 * width;
        setSize(width, height);
        //setDebug(true);
        setOrigin(Align.center);
        setBoundaryPolygon(8, 0.5f);

        effect = new EffectEnemyMovement();
        effect.setScale(0.005f);
        effect.setPosition((getWidth() / 2) - 0.2f, getHeight() * 0.5f);
        effect.start();
        addActor(effect);

        // spawn
        reset();

        float duration = 2.0f * 1 / movementSpeed;
        //System.out.println("movement speed: " + movementSpeed + ", duration: " + duration);
        addAction(Actions.forever(Actions.sequence(
            Actions.delay(duration),
            Actions.run(this::flip),
            Actions.delay(duration),
            Actions.run(this::flip)
        )));

        purrSound.isLooping();
        purrSound.play();
        purrSound.setVolume(0f);
    }


    @Override
    public void act(float delta) {
        if (pause) return;
        super.act(delta);

        accelerateAtAngle(angle);
        applyPhysics(delta);

        setRotation(getMotionAngle() - 90f);

        if (
            Math.abs(getX()) > BaseGame.WORLD_WIDTH * 1.2f ||
            Math.abs(getY()) > BaseGame.WORLD_HEIGHT * 1.2f
        ) {
            reset();
        }
    }


    private void reset() {
        effect.stop();
        is_dead = false;
        setPositionAtEdge();

        // Randomize movement speed
        movementSpeed = MathUtils.random(MIN_MOVEMENT_SPEED, MAX_MOVEMENT_SPEED);
        movementAcceleration = movementSpeed * 10f;


        float width = MathUtils.random(0.75f, 1.25f);
        float height = 2 * width;
        setSize(width, height);
        setBoundaryPolygon(8, 0.5f);

        addAction(Actions.sequence(
            Actions.delay(0.75f), // hack that eliminates particle effect misfire
            Actions.run(() -> {
                setMaxSpeed(movementSpeed);
                setAcceleration(movementAcceleration);
                setDeceleration(movementAcceleration);
                effect.start();
            })
        ));

        setMaxSpeed(0f);
        setAcceleration(0f);
        setDeceleration(0f);

    }


    private void setPositionAtEdge() {
        int side = MathUtils.random(0, 3);
        float x;
        float y;
        float start_offset = 2;

        if (side == 0) { // left
            x = -start_offset;
            y = MathUtils.random(-start_offset, BaseGame.WORLD_HEIGHT + start_offset);
            angle = MathUtils.random(-45f, 45f);
        } else if (side == 1) { // right
            x = BaseGame.WORLD_WIDTH + start_offset;
            y = MathUtils.random(-start_offset, BaseGame.WORLD_HEIGHT + start_offset);
            angle = MathUtils.random(135f, 225f);
        } else if (side == 2) { // bottom
            x = MathUtils.random(-start_offset, BaseGame.WORLD_WIDTH + start_offset);
            y = -start_offset;
            angle = MathUtils.random(45f, 135f);
        } else { // top
            x = MathUtils.random(-start_offset, BaseGame.WORLD_WIDTH + start_offset);
            y = BaseGame.WORLD_HEIGHT + start_offset;
            angle = MathUtils.random(225f, 315f);
        }
        setPosition(x, y);
    }


}
