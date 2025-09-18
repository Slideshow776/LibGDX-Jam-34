package no.sandramoen.libgdx34.screens.shell;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.textra.TextraLabel;
import com.github.tommyettinger.textra.TypingLabel;

import no.sandramoen.libgdx34.actors.Background;
import no.sandramoen.libgdx34.actors.Overlay;
import no.sandramoen.libgdx34.gui.MadeByLabel;
import no.sandramoen.libgdx34.screens.gameplay.LevelScreen;
import no.sandramoen.libgdx34.utils.AssetLoader;
import no.sandramoen.libgdx34.utils.BaseActor;
import no.sandramoen.libgdx34.utils.BaseGame;
import no.sandramoen.libgdx34.utils.BaseScreen;
import no.sandramoen.libgdx34.utils.GameUtils;


public class MenuScreen extends BaseScreen {

    private Background background;
    private BaseActor overlay;
    public enum Difficulty {EASY, MEDIUM, HARD}

    @Override
    public void initialize() {
        overlay = new Overlay(mainStage);
        background = new Background(mainStage);
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);

        // ui resources
        Image easy_image = new Image(AssetLoader.textureAtlas.findRegion("clock"));
        TypingLabel easy_label = new TypingLabel("{RAINBOW}E{ENDRAINBOW}ASY", AssetLoader.getLabelStyle("Play-Bold59white"));
        easy_label.setAlignment(Align.left);

        Image medium_image = new Image(AssetLoader.textureAtlas.findRegion("clock"));
        TypingLabel medium_label = new TypingLabel("{RAINBOW}M{ENDRAINBOW}EDIUM", AssetLoader.getLabelStyle("Play-Bold59white"));
        medium_label.setAlignment(Align.right);

        Image hard_image = new Image(AssetLoader.textureAtlas.findRegion("clock"));
        TypingLabel hard_label = new TypingLabel("{RAINBOW}H{ENDRAINBOW}ARD", AssetLoader.getLabelStyle("Play-Bold59white"));
        hard_label.setAlignment(Align.left);

        // ui setup
        uiTable.defaults()
            .padTop(Gdx.graphics.getHeight() * .02f)
        ;

        uiTable.add(easy_image)
            .width(Gdx.graphics.getWidth() * 0.04f)
            .height(Gdx.graphics.getHeight() * 0.04f)
            .padRight(Gdx.graphics.getWidth() * 0.01f)
        ;
        uiTable.add(easy_label)
            .left()
            .row()

        ;uiTable.add(medium_image)
            .width(Gdx.graphics.getWidth() * 0.04f)
            .height(Gdx.graphics.getHeight() * 0.04f)
            .padRight(Gdx.graphics.getWidth() * 0.01f)
        ;
        uiTable.add(medium_label)
            .left()
            .row()

        ;uiTable.add(hard_image)
            .width(Gdx.graphics.getWidth() * 0.04f)
            .height(Gdx.graphics.getHeight() * 0.04f)
            .padRight(Gdx.graphics.getWidth() * 0.01f)
        ;
        uiTable.add(hard_label)
            .left()
            .row()
        ;

        //uiTable.setDebug(true);
    }


    @Override
    public void update(float delta) {}


    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Keys.ESCAPE) {
            Gdx.app.exit();
        } else if (keycode == Keys.E)
            start(Difficulty.EASY);
        else if (keycode == Keys.M)
            start(Difficulty.MEDIUM);
        else if (keycode == Keys.H)
            start(Difficulty.HARD);
        return super.keyDown(keycode);
    }

    private void start(Difficulty difficulty) {
        BaseGame.setActiveScreen(new LevelScreen());
    }
}
