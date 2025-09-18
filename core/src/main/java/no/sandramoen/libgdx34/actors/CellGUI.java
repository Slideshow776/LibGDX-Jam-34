package no.sandramoen.libgdx34.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;

import no.sandramoen.libgdx34.utils.AssetLoader;
import no.sandramoen.libgdx34.utils.BaseActor;
import no.sandramoen.libgdx34.utils.BaseGame;

public class CellGUI extends BaseActor {
    public static final float CELL_SIZE = 1.4f;

    public boolean is_player = true;
    public boolean is_dangerous = true;
    public boolean is_key = false;
    public enum Font {BOWLBY, METAL_MANIA, ALEGREYA}

    public boolean is_declared_goal = false;
    private SequenceAction wobbleAction; // reference to the current wobble
    private String letter;
    private Color key_colour = Color.WHITE;
    private Color player_colour = Color.GREEN;
    private Color goal_colour = Color.YELLOW;
    private Color default_colour = Color.BLACK;

    private TextureRegion backgroundRegion;


    public CellGUI(float x, float y, Stage stage, String letter) {
        super(x, y, stage);
        setLetter(letter, Font.ALEGREYA);

        setBoundaryPolygon(8, 0.5f);
        setDebug(false);

        addAction(Actions.sequence(
            Actions.scaleTo(0f, 0f, 0f),
            Actions.scaleTo(1f, 1f, MathUtils.random(0.1f, 1.0f))
        ));
    }


    @Override
    public void act(float delta) {
        super.act(delta);
        if (is_declared_goal)
            backgroundRegion = AssetLoader.textureAtlas.findRegion("door");
        else if(is_key)
            backgroundRegion = AssetLoader.textureAtlas.findRegion("key");
        else
            backgroundRegion = AssetLoader.textureAtlas.findRegion("emptyPixel");
    }


    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Draw background first
        if (backgroundRegion != null) {
            batch.setColor(key_colour);
            batch.draw(
                backgroundRegion,
                getX(), getY(),
                getOriginX(), getOriginY(),
                getWidth(), getHeight(),
                1.2f * getScaleX(), 1.2f * getScaleY(),
                getRotation()
            );
        }

        // Then let BaseActor draw the letter + children
        super.draw(batch, parentAlpha);
    }


    public void setLetter(String letter, Font font) {
        if (letter == null) return;

        this.letter = letter;
        setFont(font);

        float scaleAmount = 1.05f;
        float scaleDuration = 0.25f;
        addAction(Actions.sequence(
            Actions.scaleTo(scaleAmount, scaleAmount, scaleDuration / 2),
            Actions.scaleTo(1f, 1f, scaleDuration / 2)
        ));
    }


    public void markDangerous(boolean is_dangerous) {
        this.is_dangerous = is_dangerous;
        //setVisible(!is_dangerous);
    }


    public void showError() {
        float amount = 0.2f;
        float duration = 0.05f;
        addAction(Actions.sequence(
            Actions.moveBy(-amount, 0f, duration),
            Actions.moveBy(2 * amount, 0f, 2 * duration),
            Actions.moveBy(-amount, 0f, duration)
        ));
        AssetLoader.error_sound.play(BaseGame.soundVolume, MathUtils.random(0.8f, 1.2f), 0f);
    }


    public void setPlayerHere(boolean isPlayer) {
        if (isPlayer) {
            if (!is_player) {
                is_key = false;
                is_player = true;
                is_declared_goal = false;
                AssetLoader.move_sound.play(BaseGame.soundVolume, MathUtils.random(0.8f, 1.2f), 0f);
                setFont(Font.METAL_MANIA);
                clearActions();
                clearWobble();
                setColor(player_colour);
                wobbleAction = createPlayerWobble();
                addAction(wobbleAction);

                if (getScaleX() != 1.75f) {
                    float amount = 1.75f;
                    float duration = 0.5f;
                    addAction(Actions.scaleTo(0f, 0f, 0f));
                    addAction(Actions.scaleTo(amount, amount, duration, Interpolation.elasticOut));
                }
            }
        } else {
            if (is_player) {
                is_player = false;
                setFont(Font.ALEGREYA);
                setColor(default_colour);
                clearWobble();
                wobbleAction = createDefaultWobble();
                addAction(wobbleAction);
                addAction(Actions.scaleTo(1f, 1f, 1.0f, Interpolation.swingOut));
            }
        }
    }


    public void setGoalHere(boolean isGoal) {
        if (isGoal) {
            if (!is_declared_goal) {
                is_key = false;
                is_player = false;
                is_declared_goal = true;
                setFont(Font.BOWLBY);
                setColor(goal_colour);
                clearWobble();
                addAction(
                    Actions.parallel(
                        Actions.scaleBy(2.5f, 2.5f, 0.5f, Interpolation.elasticOut),
                        Actions.sequence(
                            Actions.rotateBy(360f, 0.5f),
                            Actions.rotateTo(0f, 0.5f)
                        )
                    )
                );
            }
        } else {
            is_declared_goal = false;
        }
    }


    public void setKeyHere(boolean isKey, Color key_colour) {
        if (isKey) {
            is_key = true;
            is_player = false;
            this.key_colour = new Color(key_colour.r, key_colour.g, key_colour.b, 1.0f);
        } else {
            is_key = false;
        }
    }


    private void setFont(Font font) {
        if (font == Font.METAL_MANIA)
            loadImage("fonts/metal mania/" + letter.toLowerCase());
        else if (font == Font.ALEGREYA)
            loadImage("fonts/alegreya/" + letter.toLowerCase());
        else if (font == Font.BOWLBY)
            loadImage("fonts/bowlby/" + letter.toLowerCase());

        setSize(CELL_SIZE, CELL_SIZE);
        setOrigin(Align.center);
    }


    private void clearWobble() {
        if (wobbleAction != null) {
            removeAction(wobbleAction);
            wobbleAction = null;
        }
    }


    private SequenceAction createDefaultWobble() {
        float delay = MathUtils.random(0f, 2f);
        return Actions.sequence(
            Actions.delay(delay),
            Actions.forever(Actions.sequence(
                Actions.rotateTo(10f, 1f, Interpolation.pow2Out),
                Actions.rotateTo(-10f, 2f, Interpolation.pow2),
                Actions.rotateTo(0f, 1f, Interpolation.pow2In)
            ))
        );
    }


    private SequenceAction createPlayerWobble() {
        return Actions.sequence(
            Actions.delay(0.1f),
            Actions.forever(Actions.sequence(
                Actions.rotateTo(20f, 0.5f, Interpolation.exp10Out),
                Actions.rotateTo(-20f, 1f, Interpolation.exp10),
                Actions.rotateTo(0f, 0.5f, Interpolation.exp10In)
            ))
        );
    }
}
