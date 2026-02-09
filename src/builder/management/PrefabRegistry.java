package builder.management;

import builder.Game;
import builder.base.GLTFLoader;
import builder.base.Mesh;
import builder.enums.GameObjectType;
import builder.objects.GameObject;
import builder.objects.ObjectDetails;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PrefabRegistry {
    private final Gson json = new Gson();

    public static class PrefabAxis {
        public float x;
        public float y;
        public float z;
    }

    public static class Prefab extends ObjectDetails {
        public String name;
        public GameObjectType type;
        public String mesh;
        public boolean selectable;
        public boolean blocking;
        public PrefabAxis scale;

        public GameObject convertToGameObject(Vector3f worldPosition) {
            GameObject object = new GameObject();
            object.transform.scale.set(scale.x, scale.y, scale.z);
            object.transform.position.set(worldPosition);
            object.name = name;
            object.type = type;
            System.out.println(mesh);
            object.meshes = GLTFLoader.loadScene(("prefabs/" + name + "/" + mesh)).mesh;
            object.selectable = selectable;
            object.blocking = blocking;

            object.AddMeshCollider();
            return object;
        }
    }

    public ArrayList<GameObject> convertAllToGameObjects() {
        return new ArrayList<GameObject>();
    }

    public Prefab loadPrefab(String name) {
        Prefab pre = null;
        try {
            String rawJson = Files.readString(Path.of(
                    "prefabs/" +  name + "/" +name + ".json"
            ));
            JsonObject root = JsonParser.parseString(rawJson).getAsJsonObject();

            var element = root.getAsJsonObject();
            pre = json.fromJson(element, Prefab.class);
        } catch (IOException exc) {
            exc.printStackTrace();
            System.out.println("Could not load prefab: " + name);
        }

        return pre;
    }
}
