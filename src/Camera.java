import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private Matrix4f mvp, projection, view;
    private Vector3f position = new Vector3f(0.0f, 0.0f, 0.0f);
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

    public void zoom(float zoomAmount) {
        Vector3f oldPosition = new Vector3f(position.x, position.y, position.z);
        move(new Vector3f(-position.x, -position.y, -position.z));
        projection.scaleXY(zoomAmount, zoomAmount);
        move(oldPosition);
        zoom = zoom * zoomAmount;
        System.out.println(zoom);
        System.out.println(currentSpeed);
    }

    public float getZoomAmount() {
        return zoom;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
        recalculateMVP();
    }

    public void move(Vector3f distance) {
        view = new Matrix4f().translate(distance).invert();
        position.add(distance);
        recalculateMVP();
        updateSpeed();
    }

    private void updateSpeed() {
        currentSpeed = baseSpeed / zoom;
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public void setSpeed(float speed) {
        this.currentSpeed = speed;
    }

    public float getSpeed() {
        return this.currentSpeed;
    }

    public float getBaseSpeed() {
        return this.baseSpeed;
    }

    public Matrix4f getMVP() {
        return this.mvp;
    }
}
