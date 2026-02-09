package builder.objects;

import builder.base.Mesh;
import builder.enums.GameObjectType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class GameObject extends  ObjectDetails {

    public Transform transform = new Transform();

    public GameObject() {
        transform.owner = this;
    }

    // Selection
    public boolean selectable = false;
    public float selectionRadius = 0.5f;

    // Collision
    public boolean blocking = false;
    public float colliderRadius = 0.5f;

    // Rendering
    public List<Mesh> meshes = new ArrayList<>();
    public List<Collider2D> colliders = new ArrayList<>();

    // Object related
    public String name;
    public GameObject parent;
    public GameObjectType type;

    public void AddMeshCollider() {
        for (Mesh mesh : meshes) {
            colliders.add(mesh.buildCollider2D());
        }
    }
}
