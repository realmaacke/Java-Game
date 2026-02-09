package builder.management;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import builder.Terrain;
import builder.base.Config;
import builder.base.GLTFLoader;
import builder.base.Mesh;
import builder.enums.GameObjectType;
import builder.objects.GameObject;
import builder.objects.ObjectDetails;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.joml.Vector3f;

public class MapManager {
    private Gson json;
    private PrefabRegistry prefabRegistry;

    public MapManager() {
        json = new Gson();
        prefabRegistry = new PrefabRegistry();
    }

    public static class Map {
        public String file;
    }

    public static class ObjectPosition {
        public float x;
        public float y;
        public float z;
    }
    public static class ZoneObjects extends ObjectDetails {
        public String prefab;
        public ObjectPosition position;
    }

    public static class Zone {
        public String terrain;
        public ArrayList<ZoneObjects> objects;
    }

    public static class ReturnMap {
        public ArrayList<GameObject> elements;
        public Terrain terrain;
        public ReturnMap(ArrayList<GameObject> elements, Terrain terrain) {
            this.elements = elements;
            this.terrain = terrain;
        }
    }

    public ReturnMap LoadMap(String mapName) {
        Map map;
        Zone zone;
        ArrayList<GameObject> mapObjects = new ArrayList<>();
        try {
            map = LoadClassFromJsonObj(Map.class, Config.maps, "starter");
            zone = LoadWholeJson(Zone.class, map.file);

            List<GameObject> terrainObjects = GLTFLoader.loadScene(zone.terrain);
            mapObjects.addAll(terrainObjects);

            List<Mesh> terrainMeshes = new ArrayList<>();

            for (GameObject obj : terrainObjects) {
                terrainMeshes.addAll(obj.meshes);
            }

            Terrain terrain = new Terrain(terrainMeshes);

            for (ZoneObjects obj : zone.objects) {
                var prefab = prefabRegistry.loadPrefab(obj.prefab);
                mapObjects.add(prefab.convertToGameObject(
                    new Vector3f(
                            obj.position.x,
                            obj.position.y,
                            obj.position.z
                    )
                ));
            }

            return new ReturnMap(mapObjects, terrain);
        } catch (Exception e) {
            System.out.println("Could not load Map: " + mapName);
            throw new RuntimeException(e);
        }
    }

    public <T> T LoadClassFromJsonObj(Class<T> type, String path, String name) {
        try {
            JsonObject root = JsonParser
                    .parseString(Files.readString(Path.of(path)))
                    .getAsJsonObject();

            return json.fromJson(root.getAsJsonObject(name), type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T LoadWholeJson(Class<T> type, String path) {
        try {
            String raw = Files.readString(Path.of(path));
            return json.fromJson(raw, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
