package builder;

import builder.enums.PlayerState;
import builder.objects.GameObject;
import org.joml.Vector3f;

public class Player extends GameObject {
    // Movement Speed
    public float forward_speed = 0.1f;
    public float backward_speed = 0.05f;
    // Mount Speed
    public float mount_forward_speed = 0.5f;
    public float mount_backward_speed = 0.25f;

    public boolean mounted = false;

    // Mouse click movement
    public Vector3f moveTarget = new Vector3f();
    public boolean hasMoveTarget = false;
    public float turnSpeed = 0.5f;

    // Player state
    public PlayerState state = PlayerState.IDLE;

    // Check if mounted
    public float returnSpeed (boolean backward) {
        if (mounted) {
            return backward ? mount_backward_speed : mount_forward_speed;
        }
        return backward ? backward_speed : forward_speed;
    }

    // Collission

    public World world;
    public Player(World import_world) {
        blocking = true;
        world = import_world;
    }

    // Update player movement
    public void updateMovement() {

        // Get up pos
        if(world.terrain != null){
            float h = world.terrain.getHeight(
                    transform.position.x,
                    transform.position.z
            );

            if(h > -9990f) {
                transform.position.y = h + 0.5f;
            }
        }


        Vector3f toTarget = new Vector3f(moveTarget).sub(transform.position);
        Vector3f current = transform.position;

        toTarget.y = 0f;

        if (!hasMoveTarget) {
            state = PlayerState.IDLE;
            return;
        }

        float dist = toTarget.length();

        if (dist < 0.15f) {
            hasMoveTarget = false;
            state = PlayerState.IDLE;
            return;
        }

        state = PlayerState.MOVING;

        toTarget.normalize();

        float speed = forward_speed;

        Vector3f nextPos = new Vector3f(transform.position);

        nextPos.x += toTarget.x * speed;
        nextPos.z += toTarget.z * speed;

        boolean blocked = world.isBlocked(this, nextPos);

        if (!blocked) {
            transform.position.set(nextPos);
        } else {
            Vector3f  tryAxisX = new Vector3f(current);
            Vector3f tryAxisY = new Vector3f(current);

            if (!tryAxis(tryAxisX, toTarget, speed, true)) {
                tryAxis(tryAxisY, toTarget, speed, false);
            }
        }
    }

    public boolean tryAxis(Vector3f dirr, Vector3f target, float speed, boolean moveX) {
        if (moveX) {
            dirr.x += target.x * speed;
        } else {
            dirr.z += target.z * speed;
        }

        if (!world.isBlocked(this, dirr)) {
            transform.position.set(dirr);
            return true;
        }
        return false;
    }

    public void updateFacing(Vector3f worldPos) {
        if (state == PlayerState.STUNNED) {
            return;
        }

        Vector3f dir = new Vector3f(worldPos).sub(transform.position);
        dir.y = 0;

        if (dir.lengthSquared() < 0.0001f)
            return;

        dir.normalize();

        float targetYaw = (float)Math.atan2(dir.x, dir.z);
        float currentYaw = transform.rotation.y;

        float diff = targetYaw - currentYaw;

        while (diff > Math.PI) diff -= Math.PI * 2f;
        while (diff < -Math.PI) diff += Math.PI * 2f;

        transform.rotation.y += diff * 0.15f * turnSpeed;
    }
}
