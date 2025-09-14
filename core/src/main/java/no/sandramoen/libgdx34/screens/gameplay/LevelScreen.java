package no.sandramoen.libgdx34.screens.gameplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.github.tommyettinger.textra.TextraLabel;
import com.github.tommyettinger.textra.TypingLabel;

import no.sandramoen.libgdx34.actors.Background;
import no.sandramoen.libgdx34.actors.Enemy;
import no.sandramoen.libgdx34.actors.MapLine;
import no.sandramoen.libgdx34.actors.Player;
import no.sandramoen.libgdx34.actors.RadiationZone;
import no.sandramoen.libgdx34.actors.WaterPickup;
import no.sandramoen.libgdx34.actors.WaterZone;
import no.sandramoen.libgdx34.actors.particles.EffectWaterSplash;
import no.sandramoen.libgdx34.gui.BaseProgressBar;
import no.sandramoen.libgdx34.utils.AssetLoader;
import no.sandramoen.libgdx34.utils.BaseActor;
import no.sandramoen.libgdx34.utils.BaseGame;
import no.sandramoen.libgdx34.utils.BaseScreen;
import no.sandramoen.libgdx34.utils.GameUtils;

public class LevelScreen extends BaseScreen {
    public static TextraLabel scoreLabel;
    public static TypingLabel messageLabel;
    public BaseProgressBar water_bar;
    public BaseProgressBar radiation_bar;
    private Background map_background;

    private Player player;
    private Array<Enemy> enemies;
    private Array<MapLine> map_lines;
    private Array<WaterPickup> waterPickups;
    private Array<RadiationZone> radiation_zones;
    private Array<WaterZone> water_zones;

    private float game_time = 0f;
    private float lastEnemySpawnTime = 0f;
    private float enemySpawnInterval = 12f;
    private final float MIN_SPAWN_INTERVAL = 0.5f;

    private float map_line_spawn_interval = 0.075f;
    private float last_map_line_spawn_time = 0f;

    private float water_spawn_interval = 5.0f;
    private float last_water_spawn_time = 0f;
    private float water_consumption_rate = 0.41f;
    private float radiation_consumption_rate = 1.8f;

    private float scoreUpdateTimer = 0f;
    private final float SCORE_UPDATE_INTERVAL = 2.5f; // update every 1 second, change `s` here
    private int score = 0; // your current score variable

    private boolean is_game_over = false;
    private boolean is_pass_time = true;

    private float radiationSpawnInterval = 12f;
    private float lastRadiationSpawnTime = MathUtils.random(0f, radiationSpawnInterval);
    private float radiation_accumulation_rate = 0.5f; // lower for more difficult

    private float waterZoneSpawnInterval = 4f;
    private float lastWaterZoneSpawnTime = 0f;

    private float targetAmbientVolume = 0f;
    private final float AMBIENT_FADE_SPEED = 2.0f; // adjust speed of fade here

    private final float MAX_WATER_ZONES = 5;

    private float windAngle = MathUtils.random(360f);
    private float windSpeed = 1.0f; // adjust as needed
    private float windChangeTimer = 0f;
    private final float WIND_CHANGE_INTERVAL = 3f; // seconds

    private BaseActor overlay;


    @Override
    public void initialize() {
        // background
        map_background = new Background(0f, 0f, mainStage);
        //map_background.getColor().a = 0.0f;

        // characters
        player = new Player(BaseGame.WORLD_WIDTH / 2, BaseGame.WORLD_HEIGHT / 2, mainStage);
        player.setColor(Color.WHITE);

        enemies = new Array<Enemy>();
        for(int i = 0; i < 5; i++)
            enemies.add(new Enemy(mainStage));

        map_lines = new Array<MapLine>();
        waterPickups = new Array<WaterPickup>();
        radiation_zones = new Array<RadiationZone>();

        water_zones = new Array<WaterZone>();

        initialize_gui();
        GameUtils.playLoopingMusic(AssetLoader.levelMusic);
        GameUtils.playLoopingMusic(AssetLoader.ambientMusic);

        GameUtils.playLoopingMusic(AssetLoader.drinkingMusic);
        AssetLoader.drinkingMusic.setVolume(0f);

        GameUtils.playLoopingMusic(AssetLoader.radiationMusic);
        AssetLoader.radiationMusic.setVolume(0f);

        // shape-renderer line thickness
        float inner_line_thickness = 4f;
        RadiationZone.inner_line_thickness = inner_line_thickness;
        WaterZone.inner_line_thickness = inner_line_thickness;
        float outer_line_thickness = 8f;
        RadiationZone.outer_line_thickness = outer_line_thickness;
        WaterZone.outer_line_thickness = outer_line_thickness;

        overlay = new BaseActor(0f, 0f, mainStage);
        overlay.loadImage("whitePixel");
        overlay.setSize(BaseGame.WORLD_WIDTH + 2, BaseGame.WORLD_HEIGHT + 2);
        overlay.setPosition(-1, -1);

        float overlay_colour = 0.0f;
        overlay.setColor(new Color(overlay_colour, overlay_colour, overlay_colour, 1.0f));

        overlay.addAction(Actions.alpha(0.6f, 0f));
        overlay.addAction(Actions.alpha(0.0f, 0.5f));
    }


    @Override
    public void update(float delta) {
        AssetLoader.drinkingMusic.setVolume(0f);

        AssetLoader.radiationMusic.setVolume(0f);
        if (
            (player.isMoving() && !player.is_dead)
            || (is_pass_time && !is_game_over)
        ) {
            game_time += delta;
            boolean is_drinking = false;
            boolean is_radiation = false;
            targetAmbientVolume = BaseGame.musicVolume;

            for (RadiationZone radiation_zone : radiation_zones) {
                radiation_zone.update(game_time, delta);
            }

            for (WaterZone water_zone : water_zones) {
                water_zone.update(game_time, delta);
            }

            // update enemies
            for (Enemy enemy : enemies) {
                enemy.pause = false;

                // collision detection
                if (player.overlaps(enemy))
                    set_game_over("killed by catworm!");
            }

            for (RadiationZone radiation_zone : radiation_zones) {
                for (Enemy enemy : enemies) {
                    if (radiation_zone.overlaps(enemy.getBoundaryPolygon(), mainStage.getCamera())) {
                        enemy.setSize(
                            enemy.getWidth() + 0.005f,
                            enemy.getHeight() + 0.005f
                        );
                        enemy.setBoundaryPolygon(8, 0.5f);
                        if (!enemy.purrSound.isPlaying()) {
                            enemy.purrSound.setVolume(BaseGame.soundVolume);
                        }
                    } else {
                        if (enemy.purrSound.isPlaying()) {
                            enemy.purrSound.setVolume(0f);
                        }
                    }
                }

                if (radiation_zone.overlaps(player.getBoundaryPolygon(), mainStage.getCamera())) {
                    is_radiation = true;
                    if (!radiation_bar.progress.hasActions()) {
                        radiation_bar.incrementPercentage(1, radiation_accumulation_rate);
                    }
                    if (radiation_bar.level >= 100f)
                        set_game_over("killed by radiation!");
                }
            }

            for (WaterZone water_zone : water_zones) {
                // Using the iterator allows us to call iterator.remove(), which is safe to call even when
                // removing multiple enemies in one frame.
                for (Array.ArrayIterator<Enemy> iterator = enemies.iterator(); iterator.hasNext(); ) {
                    Enemy enemy = iterator.next();
                    if (
                        water_zone.isActive &&
                            water_zone.overlaps(enemy.getBoundaryPolygon(), mainStage.getCamera())
                    ) {
                        AssetLoader.cat_meow_sounds.random().play(BaseGame.soundVolume, MathUtils.random(0.8f, 1.2f), 0f);
                        iterator.remove();
                        AssetLoader.splashSound.play(BaseGame.soundVolume * 0.5f, MathUtils.random(0.5f, 0.8f), 0f);

                        if (!enemy.is_dead) {
                            EffectWaterSplash effect = new EffectWaterSplash();
                            effect.setScale(0.005f);
                            effect.start();
                            effect.addAction(Actions.sequence(
                                Actions.delay(0.75f),
                                Actions.removeActor()
                            ));
                            effect.setPosition(enemy.getX() + enemy.getWidth() / 2, enemy.getY() + enemy.getHeight() / 2);
                            mainStage.addActor(effect);
                        }

                        enemy.is_dead = true;
                        enemies.removeValue(enemy, true);
                        enemy.remove();
                    }
                }

                if (
                    water_zone.isActive &&
                    water_zone.overlaps(player.getBoundaryPolygon(), mainStage.getCamera())
                ) {
                    water_zone.shrinking = true;
                    is_drinking = true;
                    if (!water_bar.progress.hasActions())
                        water_bar.incrementPercentage(1, 0.5f); // TODO
                } else {
                    water_zone.shrinking = false;
                }
            }

            // consume water
            if (!water_bar.progress.hasActions()) {
                water_bar.decrementPercentage(1, water_consumption_rate);
            }
            if (water_bar.level <= 10) {
                water_bar.activate_warning();
            } else {
                water_bar.deactivate_warning();
            }

            if (is_drinking && !is_game_over) {
                AssetLoader.drinkingMusic.setVolume(BaseGame.soundVolume);
            } else {
            }

            if (is_radiation && !is_game_over) {
                AssetLoader.radiationMusic.setVolume(BaseGame.soundVolume * 0.4f);
            } else {
            }

            // consume radiation
            if (!radiation_bar.progress.hasActions()) {
                radiation_bar.decrementPercentage(1, radiation_consumption_rate);
            }
            if (radiation_bar.level >= 90) {
                radiation_bar.activate_warning();
            } else {
                radiation_bar.deactivate_warning();
            }

            lastWaterZoneSpawnTime += delta;
            if (lastWaterZoneSpawnTime >= waterZoneSpawnInterval) {
                spawnWaterZone();
                lastWaterZoneSpawnTime = 0f;
            }

            for (int i = water_zones.size - 1; i >= 0; i--) {
                WaterZone waterZone = water_zones.get(i);
                waterZone.update(game_time, delta);
                if (!waterZone.isActive) {
                    water_zones.removeIndex(i);
                }
            }


            if (water_bar.level <= 0) {
                set_game_over("died of thirst!");
            }

            handle_map_lines(delta);
            //handle_water(delta);
            handle_score(delta);
            increment_difficulty(delta);

            /*// Wind gradually changes direction
            windChangeTimer += delta;
            if (windChangeTimer >= WIND_CHANGE_INTERVAL) {
                windAngle += MathUtils.random(-20f, 20f); // small random shift
                windAngle = (windAngle + 360f) % 360f;
                windSpeed = MathUtils.random(1f, 2f); // vary strength
                windChangeTimer = 0f;
                System.out.println("wind changing direction" + windSpeed);
            }

            // Wind effect
            float windVelocityX = windSpeed * MathUtils.cosDeg(windAngle);
            float windVelocityY = windSpeed * MathUtils.sinDeg(windAngle);

            player.moveBy(windVelocityX * delta, windVelocityY * delta);*/

        } else {
            for (Enemy enemy : enemies)
                enemy.pause = true;

            for (MapLine map_line : map_lines)
                map_line.pause = true;

            targetAmbientVolume = 0f;
        }


        // Smoothly interpolate current volume towards target volume
        float currentVolume = AssetLoader.ambientMusic.getVolume();
        if (Math.abs(currentVolume - targetAmbientVolume) > 0.01f) {
            float newVolume = MathUtils.lerp(currentVolume, targetAmbientVolume, AMBIENT_FADE_SPEED * delta);
            AssetLoader.ambientMusic.setVolume(newVolume);
        } else {
            // Snap to target if very close to avoid floating point precision issues
            AssetLoader.ambientMusic.setVolume(targetAmbientVolume);
        }


        if (Gdx.input.isKeyPressed(Keys.SPACE)) {
            is_pass_time = true;
        } else {
            is_pass_time = false;
        }
    }


    @Override
    public void render(float delta) {
        super.render(delta);
        for (RadiationZone radiation_zone : radiation_zones)
            radiation_zone.draw(shape_renderer);
        for (WaterZone water_zone : water_zones)
            water_zone.draw(shape_renderer);

        uiStage.act(delta);
        uiStage.getViewport().apply();
        uiStage.draw();
    }


    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Keys.ESCAPE || keycode == Keys.Q)
            Gdx.app.exit();
        else if (keycode == Keys.R)
            BaseGame.setActiveScreen(new LevelScreen());
        return super.keyDown(keycode);
    }


    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) { // 0 for left, 1 for right
        return super.touchDown(screenX, screenY, pointer, button);
    }


    private void spawnWaterZone() {
        if (water_zones.size > MAX_WATER_ZONES) return;

        float radiusX = MathUtils.random(40f, 80f);
        float radiusY = radiusX + MathUtils.random(-10f, 10f);
        float speedX = 0f;
        float speedY = 0f;

        // Spawn somewhere inside the game world boundaries
        float x = MathUtils.random(radiusX, Gdx.graphics.getWidth() - radiusX);
        float y = MathUtils.random(radiusY, Gdx.graphics.getHeight() - radiusY);

        WaterZone waterZone = new WaterZone(x, y, speedX, speedY, radiusX, radiusY);
        water_zones.add(waterZone);
    }


    private void handle_map_lines(float delta) {
        if (!player.isMoving())
            return;

        last_map_line_spawn_time += delta;
        if (last_map_line_spawn_time >= map_line_spawn_interval) {
            MapLine map_line = new MapLine(player.getX() + player.getWidth() / 2, player.getY() + player.getHeight() / 2, mainStage);
            map_line.setRotation(player.getMotionAngle());
            map_line.setZIndex(map_background.getZIndex() + 1);
            map_lines.add(map_line);
            last_map_line_spawn_time = 0f;
        }
        for (MapLine map_line : map_lines)
            map_line.pause = false;
    }


    private void handle_water(float delta) {
        // pick up water
        for (WaterPickup water_pickup : waterPickups) {
            if (player.overlaps(water_pickup)) {
                float roll = MathUtils.random();
                if (roll <= 0.1f) {
                    water_bar.incrementPercentage(MathUtils.random(40, 60), 1.25f);
                } else if (roll <= 0.5f) {
                    water_bar.incrementPercentage(MathUtils.random(20, 30), 1.25f);
                } else {
                    water_bar.incrementPercentage(MathUtils.random(5, 10), 1.25f);
                }
                water_pickup.consume();
                waterPickups.removeValue(water_pickup, false);
            }
        }

        // consume water
        if (!water_bar.progress.hasActions()) {
            water_bar.decrementPercentage(1, water_consumption_rate);
        }

        // create water
        last_water_spawn_time += delta;
        if (last_water_spawn_time >= water_spawn_interval) {
            WaterPickup water_pickup = new WaterPickup(mainStage);
            water_pickup.centerAtPosition(
                MathUtils.random(1f, BaseGame.WORLD_WIDTH - 1),
                MathUtils.random(1f, BaseGame.WORLD_HEIGHT - 1)
            );
            waterPickups.add(water_pickup);
            last_water_spawn_time = 0f;
        }

        // dying from thirst
    }


    private void handle_score(float delta) {
        if (is_game_over)
            return;

        scoreUpdateTimer += delta;
        if (scoreUpdateTimer >= SCORE_UPDATE_INTERVAL) {
            scoreUpdateTimer -= SCORE_UPDATE_INTERVAL; // reset timer but keep overflow

            // Update your score logic here (example just increments)
            score += 1;

            // Update the score label text
            scoreLabel.setText(String.valueOf(score));
        }
    }


    private void increment_difficulty(float _delta) {
        float spawnInterval = Math.max(enemySpawnInterval - game_time * 0.11f, MIN_SPAWN_INTERVAL);
        if (game_time - lastEnemySpawnTime >= spawnInterval) {
            //System.out.println("added a new enemy, count: " + enemies.size + ", spawn interval: " + spawnInterval);
            lastEnemySpawnTime = game_time;
            enemies.add(new Enemy(mainStage));

            water_consumption_rate -= 0.009f;
            water_spawn_interval -= 0.05f;
        }

        lastRadiationSpawnTime += _delta;
        if (lastRadiationSpawnTime >= radiationSpawnInterval) {
            spawnRadiationZoneAtBorder();
            lastRadiationSpawnTime = 0f;
            if (radiation_consumption_rate >= 0f)
                radiation_accumulation_rate -= 0.01f;
        }
    }


    private void spawnRadiationZoneAtBorder() {
        float radiusX = MathUtils.random(40f, 300f);
        float radiusY = radiusX + MathUtils.random(-25f, 25f);
        float maxRadius = Math.max(radiusX, radiusY);

        int edge = MathUtils.random(3); // 0=left, 1=top, 2=right, 3=bottom
        float baseSpeed = 100f; // adjust this as you want max speed for smallest cloud

        // speed inversely proportional to size: bigger = slower
        float speed = baseSpeed * (40f / maxRadius);  // 40f is minimum radius, keep scaling reasonable

        float x = 0, y = 0;
        float speedX = 0f, speedY = 0f;
        float offset = maxRadius;

        switch (edge) {
            case 0: // left edge
                x = -offset;
                y = MathUtils.random(0, Gdx.graphics.getHeight());
                speedX = speed;  // move right
                speedY = 0f;
                break;
            case 1: // top edge
                x = MathUtils.random(0, Gdx.graphics.getWidth());
                y = Gdx.graphics.getHeight() + offset;
                speedX = 0f;
                speedY = -speed; // move down
                break;
            case 2: // right edge
                x = Gdx.graphics.getWidth() + offset;
                y = MathUtils.random(0, Gdx.graphics.getHeight());
                speedX = -speed; // move left
                speedY = 0f;
                break;
            case 3: // bottom edge
                x = MathUtils.random(0, Gdx.graphics.getWidth());
                y = -offset;
                speedX = 0f;
                speedY = speed; // move up
                break;
        }

        RadiationZone radiation_zone = new RadiationZone(x, y, speedX, speedY, radiusX, radiusY);
        radiation_zones.add(radiation_zone);
    }


    private void set_game_over(String reason) {
        is_game_over = true;
        player.kill();
        messageLabel.setText("{CROWD}press '{RAINBOW}R{ENDRAINBOW}' to restart\n{ENDCROWD}{SICK}{COLOR=#c30010}" + reason);
        messageLabel.getColor().a = 1.0f;
        create_skull(player.getX(), player.getY());

        overlay.addAction(Actions.alpha(0.6f, 0.5f));
        overlay.setZIndex(9001);
    }


    private void create_skull(float x, float y) {
        BaseActor skull = new BaseActor(x, y, mainStage);
        skull.loadImage("skull");
        skull.setSize(0.75f, 0.75f);
        skull.centerAtPosition(x + player.getWidth() / 2, y + player.getHeight() / 2);
    }


    private void initialize_gui() {
        Image calendar = new Image(new Texture("images/included/gui/calendar.png"));
        float desiredWidth = 60f; // or whatever fits your layout
        float aspectRatio = calendar.getHeight() / calendar.getWidth();
        calendar.setSize(70f, 60f);

        scoreLabel = new TextraLabel("0", AssetLoader.getLabelStyle("Play-Bold59white"));
        scoreLabel.setAlignment(Align.center);

        messageLabel = new TypingLabel("{CROWD}press '{RAINBOW}R{ENDRAINBOW}' to restart", AssetLoader.getLabelStyle("Play-Bold59white"));
        messageLabel.getColor().a = 0.0f;
        messageLabel.setAlignment(Align.center);

        water_bar = new BaseProgressBar(Gdx.graphics.getWidth() * -.41f, Gdx.graphics.getHeight() * 0.5f, uiStage);
        water_bar.rotateBy(90f);
        water_bar.setProgress(75);
        water_bar.set_color(Color.BLUE);
        water_bar.setProgressBarColor(Color.CYAN);
        water_bar.setOpacity(0.5f);
        water_bar.progress.setOpacity(0.5f);
        uiStage.addActor(water_bar);

        radiation_bar = new BaseProgressBar(Gdx.graphics.getWidth() * .51f, Gdx.graphics.getHeight() * 0.5f, uiStage);
        radiation_bar.rotateBy(90f);
        radiation_bar.setProgress(50);
        radiation_bar.set_color(Color.OLIVE);
        radiation_bar.setProgressBarColor(Color.GREEN);
        radiation_bar.setOpacity(0.5f);
        radiation_bar.progress.setOpacity(0.5f);
        uiStage.addActor(radiation_bar);

        uiTable.defaults()
            .padTop(Gdx.graphics.getHeight() * .02f)
        ;

        uiTable.add(calendar)
            .width(calendar.getWidth())
            .height(calendar.getHeight())
            .padBottom(Gdx.graphics.getHeight() * -.015f)
            .row();

        uiTable.add(scoreLabel).center()
            .height(scoreLabel.getPrefHeight() * 1.5f)
            .padTop(-Gdx.graphics.getHeight() * 0.005f)
            .row()
        ;

        uiTable.add(messageLabel)
            .expandY()
            .padBottom(Gdx.graphics.getHeight() * .1f)
            .row();

        /*uiTable.add(water_bar)
            .expand()
            .left()
            .row();*/

        //uiTable.setDebug(true);
    }
}
