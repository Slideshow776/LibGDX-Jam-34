package no.sandramoen.libgdx34.screens.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;

import no.sandramoen.libgdx34.actors.Background;
import no.sandramoen.libgdx34.actors.Overlay;
import no.sandramoen.libgdx34.utils.BaseActor;
import no.sandramoen.libgdx34.utils.BaseGame;
import no.sandramoen.libgdx34.utils.BaseScreen;

public class LevelScreen extends BaseScreen {

    private Background map_background;
    private BaseActor overlay;


    @Override
    public void initialize() {
        // background
        map_background = new Background(mainStage);
        //map_background.getColor().a = 0.0f;

        initialize_gui();
        //GameUtils.playLoopingMusic(AssetLoader.levelMusic);

        overlay = new Overlay(mainStage);
    }


    @Override
    public void update(float _delta) {
    }


    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Keys.ESCAPE || keycode == Keys.Q)
            Gdx.app.exit();
        else if (keycode == Keys.R)
            BaseGame.setActiveScreen(new LevelScreen());
        return super.keyDown(keycode);
    }


    private void initialize_gui() {
        //scoreLabel = new TextraLabel("0", AssetLoader.getLabelStyle("Play-Bold59white"));
        //scoreLabel.setAlignment(Align.center);


        uiTable.defaults()
            .padTop(Gdx.graphics.getHeight() * .02f)
        ;

        /*uiTable.add(scoreLabel).center()
            .height(scoreLabel.getPrefHeight() * 1.5f)
            .padTop(-Gdx.graphics.getHeight() * 0.005f)
            .row()
        ;*/
    }
}
