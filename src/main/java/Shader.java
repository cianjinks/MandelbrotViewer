import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_FALSE;

public class Shader {
    public int programID;
    public int shaderObj;
    public Shader() {
        programID = GL20.glCreateProgram();
    }

    public void setupShader(String filePath, int shaderType) {

        String shader = null;
        if(shaderType == GL20.GL_VERTEX_SHADER) {
            shader = parseShaderFromFile(filePath);
        }
        else if(shaderType == GL20.GL_FRAGMENT_SHADER) {
            shader = parseShaderFromFile(filePath);
        }
        int shaderID = createShader(shader, shaderType);
        bindShader(shaderID);
    }

    private int createShader(String shader, int shaderType) {
        shaderObj = GL20.glCreateShader(shaderType);
        GL20.glShaderSource(shaderObj, shader);
        GL20.glCompileShader(shaderObj);

        //ERROR HANDLING
        int result = GL20.glGetShaderi(shaderObj, GL20.GL_COMPILE_STATUS);
        if (result == GL_FALSE) {
            int length;
            length = GL20.glGetShaderi(shaderObj, GL20.GL_INFO_LOG_LENGTH);
            char message[];
            message = GL20.glGetShaderInfoLog(shaderObj, length).toCharArray();
            System.out.println("Failed to compile " + (shaderType == GL20.GL_VERTEX_SHADER ? "vertex" : "fragment") + " shader!");
            System.out.println(message);
            GL20.glDeleteShader(shaderObj);
        }

        GL20.glAttachShader(programID, shaderObj);
        GL20.glLinkProgram(programID);
        GL20.glValidateProgram(programID);

        return programID;
    }

    public int getProgramID() {
        return programID;
    }

    public void bindShader(int programID) {
        GL30.glUseProgram(programID);
    }

    public void unBindShader() {
        GL30.glUseProgram(0);
    }

    public void deleteShader() {
        GL30.glDeleteShader(shaderObj);
    }

    public void bindShaderAttribute(int index, String name) {
        GL20.glBindAttribLocation(programID, index, name);
    }

    public void setUniMat4f(String name, Matrix4f matrix) {

        // TODO: Add location cache
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(4 * 4);
        matrix.get(matrixBuffer);
        int uniformLocation = GL30.glGetUniformLocation(programID, name);
        if(uniformLocation != -1) {
            GL20.glUniformMatrix4fv(uniformLocation, false, matrixBuffer);
        }
        else {
            System.out.println("The name " + name + " does not correspond to an active uniform variable in the current shader.");
        }
    }

    public void setUniVec1f(String name, float[] vector) {
        int uniformLocation = GL30.glGetUniformLocation(programID, name);
        if(uniformLocation != -1) {
            GL30.glUniform1fv(uniformLocation, vector);
        }
        else {
            System.out.println("The name " + name + " does not correspond to an active uniform variable in the current shader.");
        }
    }

    private static String parseShaderFromFile(String filePath) {
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
