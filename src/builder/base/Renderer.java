package builder.base;

import builder.Terrain;
import builder.objects.Collider2D;
import builder.objects.FixedCamera;
import builder.objects.GameObject;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL30.*;

public class Renderer {
    private Shader shader;
    private FixedCamera camera;

    private Shader debugShader;

    public Matrix4f currentProjection;
    public void init() {

        glEnable(GL_DEPTH_TEST);

        camera = new FixedCamera();

        shader = new Shader("shaders/base.vert", "shaders/base.frag", true);

        debugShader = new Shader("shaders/debug.vert", "shaders/debug.frag", true);
    }

    public FixedCamera getCamera() {
        return camera;
    }

    public void updateProjection() {

        currentProjection = new Matrix4f()
                .perspective(
                        (float)Math.toRadians(70f),
                        (float)WindowContext.width / (float)WindowContext.height,
                        0.1f,
                        100f
                );
    }

    public void beginFrame() {
        shader.use();

        Vector3f camPos = camera.getPosition();
        glUniform3f(shader.getUniform("viewPos"),
                camPos.x, camPos.y, camPos.z);

        glUniform3f(shader.getUniform("lightDir"),
                -0.4f, -1.0f, -0.3f);

        glUniform3f(shader.getUniform("lightColor"),
                1.0f, 0.92f, 0.75f);

        glUniform3f(shader.getUniform("skyColor"),
                0.6f, 0.6f, 1.0f);

        glUniform3f(shader.getUniform("groundColor"),
                0.18f, 0.16f, 0.14f);
    }

    public void draw(GameObject obj, boolean selected) {

        if (obj.meshes == null || obj.meshes.isEmpty())
            return;

        Vector3f camPos = camera.getPosition();

        Matrix4f view = camera.getViewMatrix();
        Matrix4f model = obj.transform.getModelMatrix();

        Matrix4f mvp = new Matrix4f();
        currentProjection.mul(view, mvp).mul(model);

        int mvpLoc = shader.getUniform("mvp");
        int modelLoc = shader.getUniform("model");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);

            mvp.get(fb);
            glUniformMatrix4fv(mvpLoc, false, fb);

            model.get(fb.clear());
            glUniformMatrix4fv(modelLoc, false, fb);
        }

        int colorLoc = shader.getUniform("color");

        for (Mesh mesh : obj.meshes) {

            if (selected) {
                glUniform3f(colorLoc, 1f, 1f, 0f);
            } else {
                glUniform3f(colorLoc, mesh.color.x, mesh.color.y, mesh.color.z);
            }
            mesh.draw();
        }
    }

    public Vector3f getMouseGroundIntersection() {

        float ndcX = (2.0f * (float)WindowContext.mouseX) / WindowContext.width - 1.0f;
        float ndcY = 1.0f - (2.0f * (float)WindowContext.mouseY) / WindowContext.height;

        Matrix4f view = camera.getViewMatrix();

        Matrix4f invVP = new Matrix4f(currentProjection)
                .mul(view)
                .invert();

        // Near point
        Vector4f nearPoint = new Vector4f(ndcX, ndcY, -1f, 1f).mul(invVP);
        nearPoint.div(nearPoint.w);

        // Far point
        Vector4f farPoint = new Vector4f(ndcX, ndcY, 1f, 1f).mul(invVP);
        farPoint.div(farPoint.w);

        Vector3f rayOrigin = new Vector3f(nearPoint.x, nearPoint.y, nearPoint.z);
        Vector3f rayDir = new Vector3f(
                farPoint.x - nearPoint.x,
                farPoint.y - nearPoint.y,
                farPoint.z - nearPoint.z
        ).normalize();

        // Ground plane y = 0
        float t = -rayOrigin.y / rayDir.y;

        if (t < 0) return null;

        return new Vector3f(rayOrigin).fma(t, rayDir);
    }

    public class Ray {
        public Vector3f origin = new Vector3f();
        public Vector3f dir = new Vector3f();
    }

    public Ray getMouseRay() {

        float ndcX = (2f * (float)WindowContext.mouseX) / WindowContext.width - 1f;
        float ndcY = 1f - (2f * (float)WindowContext.mouseY) / WindowContext.height;

        Matrix4f view = camera.getViewMatrix();

        Matrix4f invVP = new Matrix4f(currentProjection)
                .mul(view)
                .invert();

        Vector4f near = new Vector4f(ndcX, ndcY, -1f, 1f).mul(invVP);
        near.div(near.w);

        Vector4f far = new Vector4f(ndcX, ndcY, 1f, 1f).mul(invVP);
        far.div(far.w);

        Ray ray = new Ray();

        ray.origin.set(near.x, near.y, near.z);
        ray.dir.set(
                far.x - near.x,
                far.y - near.y,
                far.z - near.z
        ).normalize();

        return ray;
    }

    private void drawAABB(GameObject obj, Collider2D col, Matrix4f mvp) {

        Vector3f min = new Vector3f(col.min).mul(obj.transform.scale);
        Vector3f max = new Vector3f(col.max).mul(obj.transform.scale);

        float x0 = min.x;
        float y0 = min.y;
        float z0 = min.z;

        float x1 = max.x;
        float y1 = max.y;
        float z1 = max.z;

        float[] lines = {
                // bottom
                x0,y0,z0,  x1,y0,z0,
                x1,y0,z0,  x1,y0,z1,
                x1,y0,z1,  x0,y0,z1,
                x0,y0,z1,  x0,y0,z0,

                // top
                x0,y1,z0,  x1,y1,z0,
                x1,y1,z0,  x1,y1,z1,
                x1,y1,z1,  x0,y1,z1,
                x0,y1,z1,  x0,y1,z0,

                // vertical
                x0,y0,z0,  x0,y1,z0,
                x1,y0,z0,  x1,y1,z0,
                x1,y0,z1,  x1,y1,z1,
                x0,y0,z1,  x0,y1,z1,
        };

        int vao = glGenVertexArrays();
        int vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, lines, GL_STATIC_DRAW);

        glVertexAttribPointer(0,3,GL_FLOAT,false,0,0);
        glEnableVertexAttribArray(0);

        try (MemoryStack stack = MemoryStack.stackPush()) {

            FloatBuffer fb = stack.mallocFloat(16);
            mvp.get(fb);

            glUniformMatrix4fv(
                    debugShader.getUniform("mvp"),
                    false,
                    fb
            );
        }

        glDrawArrays(GL_LINES,0,lines.length/3);

        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }

    public void drawColliders(GameObject obj) {

        if(obj.colliders == null || obj.colliders.isEmpty())
            return;

        debugShader.use();

        Matrix4f view = camera.getViewMatrix();
        Matrix4f model = obj.transform.getModelMatrix();

        Matrix4f mvp = new Matrix4f();
        currentProjection.mul(view, mvp).mul(model);

        glUniform3f(debugShader.getUniform("color"),
                0f,1f,0f); // green outlines

        glDisable(GL_DEPTH_TEST); // X-ray mode

        for(Collider2D col : obj.colliders)
            drawAABB(obj, col,mvp);

        glEnable(GL_DEPTH_TEST);
    }



}
