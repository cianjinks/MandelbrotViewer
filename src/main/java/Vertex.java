import org.lwjgl.opengl.GL30;

public class Vertex {
    public static int type = GL30.GL_FLOAT;
    public static int positionElementCount = 4;
    private float[] xyzw = new float[positionElementCount];

    public static int elementSize = Float.BYTES;
    public static int positionSize = Float.BYTES * positionElementCount;
    public static int positionByteOffset = 0;

    // Setters
    public void setXYZW(float x, float y, float z, float w) {
        this.xyzw = new float[] {x, y, z, w};
    }
    public void setXYZ(float x, float y, float z) {
        this.xyzw = new float[] {x, y, z, 1f};
    }

    // Getters
    public float[] getXYZW() {
        return new float[] {this.xyzw[0], this.xyzw[1], this.xyzw[2], this.xyzw[3]};
    }
}
