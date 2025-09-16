package no.sandramoen.libgdx34.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.Align;

import no.sandramoen.libgdx34.utils.BaseActor;

public class CellGUI extends BaseActor {
    public static final float CELL_SIZE = 1.4f;

    public boolean is_player = true;
    public enum Font {BOWLBY, METAL_MANIA, ALEGREYA}

    private boolean is_declared_goal = false;
    private SequenceAction wobbleAction; // reference to the current wobble
    private String letter;


    public CellGUI(float x, float y, Stage stage, String letter) {
        super(x, y, stage);
        setLetter(letter, Font.ALEGREYA);
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


    public void showError() {
        float amount = 0.2f;
        float duration = 0.05f;
        addAction(Actions.sequence(
            Actions.moveBy(-amount, 0f, duration),
            Actions.moveBy(2 * amount, 0f, 2 * duration),
            Actions.moveBy(-amount, 0f, duration)
        ));
    }


    public void setPlayerHere(boolean isPlayer) {
        if (isPlayer) {
            if (!is_player) {
                is_player = true;
                setFont(Font.METAL_MANIA);
                clearActions();
                clearWobble();
                setColor(Color.FOREST);
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
                setColor(Color.BLACK);
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
                setFont(Font.BOWLBY);
                setColor(Color.GOLDENROD);
                clearWobble();
                is_declared_goal = true;
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
