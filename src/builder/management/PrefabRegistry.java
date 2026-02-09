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

public class PrefabRegistry {
    private Gson json;

    public static class PrefabAxis {
        public float x;
        public float y;
        public float z;
    }

    public static class Prefab extends ObjectDetails {
        public String name;
        public String type;
        public String mesh;
        public boolean selectable;
        public boolean blocking;
        public PrefabAxis scale;
        public PrefabAxis position;

        public GameObject convertToGameObject(Vector3f position) {
            GameObject obj = new GameObject();
            obj.name = name;
            obj.type = GameObjectType.fromString(type);
            obj.meshes = GLTFLoader.loadMeshes("prefabs/" + obj.name + "/" + mesh);
            obj.selectable = selectable;
            obj.blocking = blocking;
            obj.transform.scale.set(
                    scale.x,
                    scale.y,
                    scale.z
            );

            obj.transform.position = position;

            for (Mesh mesh : obj.meshes) {
                obj.colliders.add(mesh.buildCollider2D());
            }
            return obj;
        }
    }

    public PrefabRegistry() {
        json = new Gson();
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
