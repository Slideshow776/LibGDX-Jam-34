package no.sandramoen.libgdx34.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.textra.TextraLabel;

import no.sandramoen.libgdx34.utils.AssetLoader;
import no.sandramoen.libgdx34.utils.BaseActor;

public class CellGUI extends BaseActor {
    public static final float CELL_SIZE = 2f;

    private TextraLabel label;
    private Label label2;

    public CellGUI(float x, float y, Stage stage, String letter) {
        super(x, y, stage);

        loadImage("alphabet/" + letter.toLowerCase());
        //setDebug(true);

        // body
        setSize(CELL_SIZE, CELL_SIZE);
        centerAtPosition(x, y);
        setOrigin(Align.center);

        /*label = new TextraLabel(letter, AssetLoader.getLabelStyle("Play-Bold20white"));
        label.setAlignment(Align.center);
        addActor(label);*/
    }


    public void setLetter(String letter) {
        label.setText(letter);
    }


    public void setPlayerHere(boolean isPlayer) {
        if (isPlayer)
            setColor(1, 1, 0, 1); // yellow for player
        else
            setColor(1, 1, 1, 1); // white
    }


    public void setGoalHere(boolean isGoal) {
        if (isGoal)
            setColor(0, 1, 0, 1); // green for goal
    }
}
