package no.sandramoen.libgdx34.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;

import no.sandramoen.libgdx34.utils.AssetLoader;
import no.sandramoen.libgdx34.utils.BaseGame;

public class WaterZone {

    private Polygon bounds;
    private int numSides;
    private float centerX, centerY;

    private float[] baseRadii;     // Base radius for each vertex
    private float[] angleOffsets;  // Phase offset per vertex for morphing
    private float morphAmplitude = 5f;  // Radius oscillation amount
    private float morphSpeed = 1.0f;       // Oscillation speed
    private float radiusX, radiusY;

    private float speedX = 20f; // movement speed in pixels/second along X
    private float speedY = 10f; // movement speed in pixels/second along Y

    public boolean shrinking = false;
    private float shrinkSpeed = MathUtils.random(8f, 12f); // pixels per second

    private static final float MIN_RADIUS = 5f;  // minimal radius before removal
    public boolean isActive = true;               // if false, don't draw or update

    public static float inner_line_thickness = 5f;
    public static float outer_line_thickness = 10f;


    public WaterZone(float centerX, float centerY, float speedX, float speedY, float radiusX, float radiusY) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.speedX = speedX;
        this.speedY = speedY;

        this.radiusX = radiusX;
        this.radiusY = radiusY;

        numSides = MathUtils.random(12, 24);

        baseRadii = new float[numSides];
        angleOffsets = new float[numSides];

        for (int i = 0; i < numSides; i++) {
            baseRadii[i] = 1f; // normalized radius
            angleOffsets[i] = MathUtils.random(0f, MathUtils.PI2);
        }

        updateVertices(0f);
    }


    public void update(float elapsedTime, float deltaTime) {
        if (!isActive) return;  // skip if inactive

        if (shrinking) {
            radiusX -= shrinkSpeed * deltaTime;
            radiusY -= shrinkSpeed * deltaTime;

            if (radiusX < MIN_RADIUS || radiusY < MIN_RADIUS) {
                radiusX = 0;
                radiusY = 0;
                isActive = false;  // deactivate the zone
                AssetLoader.ahSound.play(BaseGame.soundVolume, MathUtils.random(0.9f, 1.3f), 0f);
                return;
            }
        }

        updateVertices(elapsedTime);
    }



    private void updateVertices(float elapsedTime) {
        if (!isActive) return;

        float[] vertices = new float[numSides * 2];
        for (int i = 0; i < numSides; i++) {
            float morphFactor = 1f + (morphAmplitude / radiusX) * (float)Math.sin(morphSpeed * elapsedTime + angleOffsets[i]);
            float angle = (float)(i * 2 * Math.PI / numSides);

            vertices[2 * i] = centerX + radiusX * baseRadii[i] * morphFactor * (float)Math.cos(angle);
            vertices[2 * i + 1] = centerY + radiusY * baseRadii[i] * morphFactor * (float)Math.sin(angle);
        }
        bounds = new Polygon(vertices);
    }


    public void draw(ShapeRenderer shape_renderer) {
        if (!isActive) return;

        float[] vertices = bounds.getTransformedVertices();

        // --- First pass: draw inside scratch lines ---
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shape_renderer.begin(ShapeRenderer.ShapeType.Filled); // use Filled for rectLine()
        shape_renderer.setColor(new Color(0f, 0.1f, 0.4f, 0.3f));

        float spacing = 10f; // distance between scratch lines
        float angle = 45f;   // angle in degrees

        // Get bounding box of polygon
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
        for (int i = 0; i < vertices.length; i += 2) {
            float x = vertices[i];
            float y = vertices[i + 1];
            if (x < minX) minX = x;
            if (y < minY) minY = y;
            if (x > maxX) maxX = x;
            if (y > maxY) maxY = y;
        }

        float rad = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);
        float length = (float) Math.sqrt((maxX - minX)*(maxX - minX) + (maxY - minY)*(maxY - minY));
        float centerX = (minX + maxX) / 2f;
        float centerY = (minY + maxY) / 2f;

        for (float offset = -length; offset <= length; offset += spacing) {
            float startX = minX;
            float startY = minY + offset;
            float endX = maxX;
            float endY = minY + offset;

            // Rotate start and end points around center
            float rotatedStartX = cos * (startX - centerX) - sin * (startY - centerY) + centerX;
            float rotatedStartY = sin * (startX - centerX) + cos * (startY - centerY) + centerY;
            float rotatedEndX = cos * (endX - centerX) - sin * (endY - centerY) + centerX;
            float rotatedEndY = sin * (endX - centerX) + cos * (endY - centerY) + centerY;

            int segments = 20;
            float prevX = rotatedStartX;
            float prevY = rotatedStartY;
            boolean prevInside = bounds.contains(prevX, prevY);

            for (int i = 1; i <= segments; i++) {
                float t = i / (float) segments;
                float currX = rotatedStartX + t * (rotatedEndX - rotatedStartX);
                float currY = rotatedStartY + t * (rotatedEndY - rotatedStartY);
                boolean currInside = bounds.contains(currX, currY);

                if (prevInside && currInside) {
                    shape_renderer.rectLine(prevX, prevY, currX, currY, inner_line_thickness);  // use your preferred thickness
                }
                prevX = currX;
                prevY = currY;
                prevInside = currInside;
            }
        }
        shape_renderer.end();

        // --- Second pass: draw polygon border lines and spokes ---
        shape_renderer.begin(ShapeRenderer.ShapeType.Filled);
        shape_renderer.setColor(new Color(0f, 0.5f, 1f, 0.4f)); // bright blue

        // Draw border using rectLine
        for (int i = 0; i < vertices.length; i += 2) {
            float x1 = vertices[i];
            float y1 = vertices[i + 1];
            float x2 = vertices[(i + 2) % vertices.length];
            float y2 = vertices[(i + 3) % vertices.length];
            shape_renderer.rectLine(x1, y1, x2, y2, outer_line_thickness);  // outer border thickness
        }

        shape_renderer.end();
    }



    public boolean overlaps(Polygon actorPolygon, Camera camera) {
        float[] worldVertices = actorPolygon.getTransformedVertices();
        float[] screenVertices = new float[worldVertices.length];
        for (int i = 0; i < worldVertices.length; i += 2) {
            float wx = worldVertices[i];
            float wy = worldVertices[i + 1];
            Vector3 screenCoords = camera.project(new Vector3(wx, wy, 0));
            screenVertices[i] = screenCoords.x;
            screenVertices[i + 1] = screenCoords.y;
        }
        Polygon screenPolygon = new Polygon(screenVertices);
        return Intersector.overlapConvexPolygons(this.bounds, screenPolygon);
    }


    public Polygon getBounds() {
        return bounds;
    }
}
