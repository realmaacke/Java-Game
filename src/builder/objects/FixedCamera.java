package builder.objects;

import builder.Player;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class FixedCamera {

    public Player target;

    // Corepunk-like camera offset
    private Vector3f offset = new Vector3f(0f, 12f, 10f);

    private Vector3f currentPosition = new Vector3f();


    public Matrix4f getViewMatrix() {

        Vector3f playerPos = target.transform.position;

        currentPosition.set(playerPos).add(offset);

        return new Matrix4f().lookAt(
                currentPosition,
                playerPos,
                new Vector3f(0,1,0)
        );
    }

    public Vector3f getPosition() {
        return new Vector3f(currentPosition);
    }



    // Corepunk movement directions (static!)
    public Vector3f getForward() {
        return new Vector3f(0,0,-1);
    }

    public Vector3f getRight() {
        return new Vector3f(1,0,0);
    }
}
