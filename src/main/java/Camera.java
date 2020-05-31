import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL30;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Camera {
    private Matrix4f mvp, projection, view;
    private float width, height;
    private float zoom = 1.0f;
    private float baseSpeed = 0.0125f;
    private float currentSpeed = 0.0125f;

    public Camera(float width, float height) {
        this.width = width;
        this.height = height;

        projection = new Matrix4f().ortho(-(width / 2.0f), width / 2.0f, -(height / 2.0f), height / 2.0f, -1.0f,1.0f);
        view = new Matrix4f();

        recalculateMVP();
    }

    private void recalculateMVP() {
        mvp = projection.mul(view);
    }

    public void takeScreenShot(String filePath, int windowWidth, int windowHeight) {
        GL30.glReadBuffer(GL30.GL_FRONT);
        int bpp = 4;
        ByteBuffer screenshotBuffer = BufferUtils.createByteBuffer(windowWidth * windowHeight * bpp);
        GL30.glReadPixels(0, 0, windowWidth, windowHeight, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, screenshotBuffer);
        File file = new File(filePath);
        String format = "JPG";
        BufferedImage screenshot = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);

        for(int x = 0; x < windowWidth; x++)
        {
            for(int y = 0; y < windowHeight; y++)
            {
                int i = (x + (windowWidth * y)) * bpp;
                int r = screenshotBuffer.get(i) & 0xFF;
                int g = screenshotBuffer.get(i + 1) & 0xFF;
                int b = screenshotBuffer.get(i + 2) & 0xFF;
                screenshot.setRGB(x, windowHeight - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }

        try {
            ImageIO.write(screenshot, format, file);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void zoom(float zoomAmount) {
        projection.scaleLocal(zoomAmount, zoomAmount, 0f);
        zoom = zoom * zoomAmount;
    }

    public void move(Vector3f distance) {
        view = new Matrix4f().translate(distance).invert();
        recalculateMVP();
        updateSpeed();
    }

    private void updateSpeed() {
        currentSpeed = baseSpeed / zoom;
    }

    public float getSpeed() {
        return this.currentSpeed;
    }

    public Matrix4f getMVP() {
        return this.mvp;
    }
}
