package builder.base;

import builder.objects.Collider2D;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL30.*;

public class Mesh {

    public int vao;
    public int vbo;
    public int ebo;
    public int indexCount;

    public float[] cpuVertices;
    public int[] cpuIndices;

    public Vector3f color = new Vector3f(1f, 1f, 1f);

    public Mesh(float[] vertices, int[] indices) {
        this.cpuVertices = vertices;
        this.cpuIndices = indices;

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        int stride = 6 * Float.BYTES;

        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        indexCount = indices.length;
    }

    public Collider2D buildCollider2D() {
        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

        for (int i = 0; i < cpuVertices.length; i += 6) {
            float x = cpuVertices[i];
            float y = cpuVertices[i + 1];
            float z = cpuVertices[i + 2];

            if (x < min.x) min.x = x;
            if (y < min.y) min.y = y;
            if (z < min.z) min.z = z;

            if (x > max.x) max.x = x;
            if (y > max.y) max.y = y;
            if (z > max.z) max.z = z;
        }

        Collider2D collider = new Collider2D();
        collider.min.set(min);
        collider.max.set(max);
        return collider;
    }

    public void draw() {
        glBindVertexArray(vao);
        glDrawElements(GL_TRIANGLES,indexCount,GL_UNSIGNED_INT,0);
    }
}
