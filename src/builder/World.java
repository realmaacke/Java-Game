package builder;

import builder.base.Renderer;
import builder.objects.Collider2D;
import builder.objects.GameObject;
import org.joml.Vector3f;

import org.joml.Intersectionf;
import org.joml.Vector2f;


import java.util.ArrayList;
import java.util.List;

public class World {
    public List<GameObject> objects = new ArrayList<>();
    public Player player;
    public Terrain terrain;

    public GameObject selected;

    public void update() {
    }

    public GameObject pickRay(Renderer.Ray ray) {

        GameObject closest = null;
        float closestT = Float.MAX_VALUE;

        Vector2f result = new Vector2f();

        for(GameObject obj : objects) {

            if(!obj.selectable) continue;
            if (obj.colliders.isEmpty()) continue;

            for (Collider2D col : obj.colliders) {
                Vector3f worldMin = new Vector3f(col.min)
                        .mul(obj.transform.scale)
                        .add(obj.transform.position);

                Vector3f worldMax = new Vector3f(col.max)
                        .mul(obj.transform.scale)
                        .add(obj.transform.position);

                boolean hit = Intersectionf.intersectRayAab(
                        ray.origin,
                        ray.dir,
                        worldMin,
                        worldMax,
                        result
                );

                if (!hit) continue;

                float t = result.x;

                if (t > 0 && t < closestT) {
                    closestT = t;
                    closest = obj;
                }
            }
        }
        return closest;
    }


    public GameObject pick (Vector3f worldPos) {
        GameObject closest = null;
        float closestDist = Float.MAX_VALUE;

        for (GameObject obj : objects) {
            if (!obj.selectable) continue;

            float dx = obj.transform.position.x - worldPos.x;
            float dz = obj.transform.position.z - worldPos.z;

            float distSqrd = dx*dx + dz*dz;

            float radius = obj.selectionRadius;

            if (distSqrd < radius * radius && distSqrd < closestDist) {
                closest = obj;
                closestDist = distSqrd;
            }
        }
        return closest;
    }

    public boolean isBlocked(GameObject mover, Vector3f nextPos) {
        for (GameObject obj : objects) {
            if (obj == mover) continue;
            if (!obj.blocking) continue;

            float minDist = mover.colliderRadius + obj.colliderRadius;

            float dx = nextPos.x - obj.transform.position.x;
            float dz = nextPos.z - obj.transform.position.z;

            float distSq = dx*dx + dz*dz;

            if (distSq < minDist * minDist) {
                return true;
            }
        }
        return false;
    }
}
