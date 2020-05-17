import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_FALSE;

public class Shader {

    private static int program;

    public Shader() {
        program = GL30.glCreateProgram();
    }

    public void addShader(String filePath, int type) {
        int shaderObj = GL30.glCreateShader(type);
        String source = parseShaderFromFile(filePath);
        GL30.glShaderSource(shaderObj, source);
        GL30.glCompileShader(shaderObj);

        //ERROR HANDLING
        int result = GL20.glGetShaderi(shaderObj, GL20.GL_COMPILE_STATUS);
        if (result == GL_FALSE) {
            int length;
            length = GL20.glGetShaderi(shaderObj, GL20.GL_INFO_LOG_LENGTH);
            char[] message;
            message = GL20.glGetShaderInfoLog(shaderObj, length).toCharArray();
            System.out.println("Failed to compile " + (type == GL20.GL_VERTEX_SHADER ? "vertex" : "fragment") + " shader!");
            System.out.println(message);
            GL20.glDeleteShader(shaderObj);
        }

        GL30.glAttachShader(program, shaderObj);
    }

    public void bindProgram() {
        GL30.glUseProgram(program);
    }

    public void validateProgram() {
        GL30.glLinkProgram(program);
        GL30.glValidateProgram(program);
    }

    public void unBindProgram() {
        GL30.glUseProgram(0);
    }

    public void setUniMat4f(String name, Matrix4f matrix) {

        // TODO: Add location cache
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(4 * 4);
        matrix.get(matrixBuffer);
        int uniformLocation = GL30.glGetUniformLocation(program, name);
        if(uniformLocation != -1) {
            GL20.glUniformMatrix4fv(uniformLocation, false, matrixBuffer);
        }
        else {
            System.out.println("The name " + name + " does not correspond to an active uniform variable in the current shader.");
        }
    }

    public void setUniVec1f(String name, float[] vector) {
        int uniformLocation = GL30.glGetUniformLocation(program, name);
        if(uniformLocation != -1) {
            GL30.glUniform1fv(uniformLocation, vector);
        }
        else {
            System.out.println("The name " + name + " does not correspond to an active uniform variable in the current shader.");
        }
    }

    public void bindShaderAttribute(int index, String name) {
        GL20.glBindAttribLocation(program, index, name);
    }

    public static String parseShaderFromFile(String filePath) {
        StringBuilder data = new StringBuilder();
        String line = "";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(Shader.class.getResourceAsStream(filePath)));
            while( (line = reader.readLine()) != null )
            {
                data.append(line);
                data.append('\n');
            }
        }
        catch(Exception e)
        {
            throw new IllegalArgumentException("Unable to load shader from file path: " + filePath, e);
        }

        return data.toString();
    }
}
