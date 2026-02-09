package builder.objects;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transform {
    public Vector3f position = new Vector3f();
    public Vector3f rotation = new Vector3f();
    public Vector3f scale = new Vector3f(1f,1f,1f);
    public GameObject owner;

    public Matrix4f getModelMatrix() {

        Matrix4f local = new Matrix4f()
                .translate(position)
                .rotateX(rotation.x)
                .rotateY(rotation.y)
                .rotateZ(rotation.z)
                .scale(scale);

        if (owner != null && owner.parent != null) {
            return new Matrix4f(owner.parent.transform.getModelMatrix().mul(local));
        }

        return local;
    }
}
