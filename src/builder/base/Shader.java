package builder.base;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static org.lwjgl.opengl.GL20.*;

public class Shader {

    private int programId;

    private HashMap<String,Integer> uniformCache = new HashMap<>();

    public int getProgramId() {
        return programId;
    }

    // ===== Constructor: load from files =====
    public Shader(String vertexPath, String fragmentPath, boolean fromFile) {

        String vertexSource;
        String fragmentSource;

        try {
            vertexSource   = Files.readString(Path.of(vertexPath));
            fragmentSource = Files.readString(Path.of(fragmentPath));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load shader files", e);
        }

        compile(vertexSource, fragmentSource);
    }

    // ===== Constructor: raw source strings =====
    public Shader(String vertexSrc, String fragmentSrc) {
        compile(vertexSrc, fragmentSrc);
    }

    // ===== REAL compile logic (shared) =====
    private void compile(String vertexSrc, String fragmentSrc) {

        int vertexId = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexId, vertexSrc);
        glCompileShader(vertexId);

        if (glGetShaderi(vertexId, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException(glGetShaderInfoLog(vertexId));
        }

        int fragmentId = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentId, fragmentSrc);
        glCompileShader(fragmentId);

        if (glGetShaderi(fragmentId, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException(glGetShaderInfoLog(fragmentId));
        }

        programId = glCreateProgram();
        glAttachShader(programId, vertexId);
        glAttachShader(programId, fragmentId);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException(glGetProgramInfoLog(programId));
        }

        glDeleteShader(vertexId);
        glDeleteShader(fragmentId);
    }

    public int getUniform(String name) {

        if(uniformCache.containsKey(name))
            return uniformCache.get(name);

        int loc = glGetUniformLocation(programId, name);
        uniformCache.put(name, loc);
        return loc;
    }

    public void use() {
        glUseProgram(programId);
    }
}
