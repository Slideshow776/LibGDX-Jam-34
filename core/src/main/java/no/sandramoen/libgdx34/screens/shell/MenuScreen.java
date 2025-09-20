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
import com.github.tommyettinger.textra.Styles;
import com.github.tommyettinger.textra.TextraLabel;
import com.github.tommyettinger.textra.TypingLabel;

import com.github.tommyettinger.textra.effects.RainbowEffect;
import no.sandramoen.libgdx34.actors.Background;
import no.sandramoen.libgdx34.actors.Enemy;
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

    @Override
    public void initialize() {
        background = new Background(mainStage);
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);

        Styles.LabelStyle playBold = AssetLoader.getLabelStyle("Play-Bold59white");
        // ui resources
        Image easy_image = new Image(AssetLoader.textureAtlas.findRegion("clock"));
        TypingLabel easy_label = new TypingLabel("{RAINBOW}E{ENDRAINBOW}ASY", playBold);
        easy_label.setAlignment(Align.left);

        Image medium_image = new Image(AssetLoader.textureAtlas.findRegion("clock"));
        TypingLabel medium_label = new TypingLabel("\u200B\u200B{RAINBOW}M{ENDRAINBOW}EDIUM", playBold);
        medium_label.setAlignment(Align.right);

        Image hard_image = new Image(AssetLoader.textureAtlas.findRegion("clock"));
        TypingLabel hard_label = new TypingLabel("\u200B\u200B\u200B\u200B{RAINBOW}H{ENDRAINBOW}ARD", playBold);
        hard_label.setAlignment(Align.left);

        Enemy easy_enemy = new Enemy(mainStage, null, 50.0f);
        easy_enemy.setPosition(5.1f, 8.1f);
        easy_enemy.setScale(0.6f);

        Enemy medium_enemy = new Enemy(mainStage, null, 10.0f);
        medium_enemy.setPosition(5.1f, 7.0f);
        medium_enemy.setScale(0.6f);

        Enemy hard_enemy = new Enemy(mainStage, null, 1.0f);
        hard_enemy.setPosition(5.1f, 6.0f);
        hard_enemy.setScale(0.6f);

        overlay = new Overlay(mainStage);

        // ui setup
        uiTable.defaults()
            .padTop(Gdx.graphics.getHeight() * .02f)
        ;

        /*uiTable.add(easy_image)
            .width(Gdx.graphics.getWidth() * 0.04f)
            .height(Gdx.graphics.getHeight() * 0.04f)
            .padRight(Gdx.graphics.getWidth() * 0.01f)
        ;*/
        uiTable.add(easy_label)
            .left()
            .row()
        ;

        /*;uiTable.add(medium_image)
            .width(Gdx.graphics.getWidth() * 0.04f)
            .height(Gdx.graphics.getHeight() * 0.04f)
            .padRight(Gdx.graphics.getWidth() * 0.01f)
        ;*/
        uiTable.add(medium_label)
            .left()
            .row()
        ;

        /*;uiTable.add(hard_image)
            .width(Gdx.graphics.getWidth() * 0.04f)
            .height(Gdx.graphics.getHeight() * 0.04f)
            .padRight(Gdx.graphics.getWidth() * 0.01f)
        ;*/
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
            start(BaseGame.Difficulty.EASY);
        else if (keycode == Keys.M)
            start(BaseGame.Difficulty.MEDIUM);
        else if (keycode == Keys.H)
            start(BaseGame.Difficulty.HARD);
        return super.keyDown(keycode);
    }

    private void start(BaseGame.Difficulty difficulty) {
        BaseGame.current_difficulty = difficulty;

        overlay.addAction(Actions.sequence(
            Actions.alpha(1.0f, Overlay.DURATION),
            Actions.run(() -> BaseGame.setActiveScreen(new LevelScreen()))
        ));
    }
}
