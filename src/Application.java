import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Application {

    // The window handle
    private long window;
    private Camera camera;
    private final int PIXEL_WIDTH = 960;
    private final int PIXEL_HEIGHT = 960;
    private final int WIDTH = 4;
    private final int HEIGHT = 4;
    private final String TITLE = "Mandelbrot Viewer";

    private int screenshotCount = 1;
    private boolean moveRight = false;
    private boolean moveLeft = false;
    private boolean moveUp = false;
    private boolean moveDown = false;
    private boolean zoomIn = false;
    private boolean zoomOut = false;

    public void run() {
        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(PIXEL_WIDTH, PIXEL_HEIGHT, TITLE, NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE ) {
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            }

            // Camera Control
            // Screenshot:
            if (key == GLFW_KEY_K && action == GLFW_PRESS) {
                camera.takeScreenShot("res/screenshots/screenshot" + screenshotCount + ".jpg", PIXEL_WIDTH, PIXEL_HEIGHT);
                screenshotCount++;
            }

            // Right:
            if (key == GLFW_KEY_D && action == GLFW_PRESS) {
                moveRight = true;
            }
            if (key == GLFW_KEY_D && action == GLFW_RELEASE) {
                moveRight = false;
            }

            // Left:
            if (key == GLFW_KEY_A && action == GLFW_PRESS) {
                moveLeft = true;
            }
            if (key == GLFW_KEY_A && action == GLFW_RELEASE) {
                moveLeft = false;
            }

            // Up:
            if (key == GLFW_KEY_W && action == GLFW_PRESS) {
                moveUp = true;
            }
            if (key == GLFW_KEY_W && action == GLFW_RELEASE) {
                moveUp = false;
            }

            // Down:
            if (key == GLFW_KEY_S && action == GLFW_PRESS) {
                moveDown = true;
            }
            if (key == GLFW_KEY_S && action == GLFW_RELEASE) {
                moveDown = false;
            }

            // Zoom in:
            if (key == GLFW_KEY_Z && action == GLFW_PRESS) {
                zoomIn = true;
            }
            if (key == GLFW_KEY_Z && action == GLFW_RELEASE) {
                zoomIn = false;
            }

            // Zoom out:
            if (key == GLFW_KEY_X && action == GLFW_PRESS) {
                zoomOut = true;
            }
            if (key == GLFW_KEY_X && action == GLFW_RELEASE) {
                zoomOut = false;
            }
        });
        glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
            float zoomAmount = 1.0f;
            zoomAmount += yoffset * 0.25f;
            zoomAmount = Math.max(zoomAmount, 0.25f);
            camera.zoom(zoomAmount);
        });

        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        // Enables openGL debug messages
        GLUtil.setupDebugMessageCallback();

        // Background Colour
        glClearColor(0.6f, 0.0f, 46f / 255f, 1.0f);

        // Vertices
        Vertex v0 = new Vertex(); v0.setXYZ(-2f, 2f, 0f);
        Vertex v1 = new Vertex(); v1.setXYZ(-2f, -2f, 0f);
        Vertex v2 = new Vertex(); v2.setXYZ( 2f,-2f, 0f);
        Vertex v3 = new Vertex(); v3.setXYZ( 2f, 2f, 0f);
        Vertex[] vertices = new Vertex[] {v0, v1, v2, v3};

        // VBO (Vertex Buffer Object)
        FloatBuffer vboBuffer = BufferUtils.createFloatBuffer(vertices.length * Vertex.positionElementCount);
        for(int vertex = 0; vertex < vertices.length; vertex++) {
            vboBuffer.put(vertices[vertex].getXYZW());
        }
        vboBuffer.flip();

        // IBO (Index Buffer Object)
        int[] indices = {
                0, 1, 2,
                2, 3, 0
        };
        IntBuffer iboBuffer = BufferUtils.createIntBuffer(indices.length);
        iboBuffer.put(indices);
        iboBuffer.flip();

        // VAO (Vertex Array Object)
        int vaoID = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoID);

        // Buffer Bind and Data TODO: Change GL_STATIC_DRAW
        int vboID = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboID);
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vboBuffer, GL30.GL_STATIC_DRAW);
        GL30.glVertexAttribPointer(0, Vertex.positionElementCount, Vertex.type, false, Vertex.positionSize, 0);
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);

        GL30.glBindVertexArray(0);

        int iboID = GL30.glGenBuffers();
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, iboID);
        GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, iboBuffer, GL30.GL_STATIC_DRAW);
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, 0);

        // Shaders
        Shader shader = new Shader();
        shader.setupShader("res/shaders/vert.shader", GL20.GL_VERTEX_SHADER);
        shader.setupShader("res/shaders/frag.shader", GL20.GL_FRAGMENT_SHADER);
        shader.bindShaderAttribute(0, "in_Position");

        // Camera (Both axis from -2 to 2)
        camera = new Camera(WIDTH, HEIGHT);
        float zoomAmount = 1.05f;
        // Loop
        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            // Camera Movement
            if(moveRight) {
                camera.move(new Vector3f(camera.getSpeed(), 0.0f, 0.0f));
            }
            if(moveLeft) {
                camera.move(new Vector3f(-camera.getSpeed(), 0.0f, 0.0f));
            }
            if(moveUp) {
                camera.move(new Vector3f(0.0f, camera.getSpeed(), 0.0f));
            }
            if(moveDown) {
                camera.move(new Vector3f(0.0f, -camera.getSpeed(), 0.0f));
            }
            if(zoomIn) {
                camera.zoom(zoomAmount);
            }
            if(zoomOut) {
                camera.zoom(1.0f / zoomAmount);
            }

            // Bind Shader
            shader.bindShader(shader.getProgramID());
            shader.setUniMat4f("u_MVP", camera.getMVP());

            // Bind VAO
            GL30.glBindVertexArray(vaoID);
            GL30.glEnableVertexAttribArray(0);
            GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, iboID);

            // Draw the vertices
            GL30.glDrawElements(GL30.GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);

            // Unbind VAO
            GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, 0);
            GL30.glDisableVertexAttribArray(0);
            GL30.glBindVertexArray(0);

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
        shader.deleteShader();
    }

    public static void main(String[] args) {
        new Application().run();
    }

}