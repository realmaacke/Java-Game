package builder.base;

public class PrimitiveMeshes {

    public static Mesh createCube() {

        float[] vertices = {
                -0.5f,-0.5f, 0.5f, 0, 0, 1,
                0.5f,-0.5f, 0.5f, 0, 0, 1,
                0.5f, 0.5f, 0.5f, 0, 0, 1,
                -0.5f, 0.5f, 0.5f, 0, 0, 1,

                -0.5f,-0.5f,-0.5f, 0, 0, 1,
                0.5f,-0.5f,-0.5f, 0, 0, 1,
                0.5f, 0.5f,-0.5f, 0, 0, 1,
                -0.5f, 0.5f,-0.5f, 0, 0, 1,
        };

        int[] indices = {
                0,1,2,2,3,0,
                1,5,6,6,2,1,
                5,4,7,7,6,5,
                4,0,3,3,7,4,
                3,2,6,6,7,3,
                4,5,1,1,0,4
        };

        return new Mesh(vertices, indices);
    }
}
