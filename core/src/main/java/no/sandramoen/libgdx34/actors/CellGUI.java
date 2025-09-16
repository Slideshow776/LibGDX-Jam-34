package no.sandramoen.libgdx34.actors;

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

    private boolean is_declared_goal = false;
    private SequenceAction wobbleAction; // reference to the current wobble


    public CellGUI(float x, float y, Stage stage, String letter) {
        super(x, y, stage);
        setLetter(letter);
    }


    public void setLetter(String letter) {
        if (letter == null) return;

        loadImage("alphabet/" + letter.toLowerCase());
        setSize(CELL_SIZE, CELL_SIZE);
        setOrigin(Align.center);

        float scaleAmount = 1.05f;
        float scaleDuration = 0.25f;
        addAction(Actions.sequence(
            Actions.scaleTo(scaleAmount, scaleAmount, scaleDuration / 2),
            Actions.scaleTo(1f, 1f, scaleDuration / 2)
        ));
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
                clearActions();
                clearWobble(); // remove current wobble only
                setColor(1, 1, 0, 1); // yellow for player
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
                setColor(1, 1, 1, 1); // white
                clearWobble(); // remove current wobble only
                wobbleAction = createDefaultWobble();
                addAction(wobbleAction);
                addAction(Actions.scaleTo(1f, 1f, 1.0f, Interpolation.swingOut));
            }
        }
    }


    public void setGoalHere(boolean isGoal) {
        if (isGoal) {

            if (!is_declared_goal) {
                setColor(0, 1, 0, 1); // green for goal
                clearWobble();
                is_declared_goal = true;
                addAction(
                    Actions.parallel(
                        Actions.scaleBy(5f, 5f, 0.5f, Interpolation.elasticOut),
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
