package builder;

import builder.base.*;
import builder.management.MapManager;
import builder.management.PrefabRegistry;
import builder.objects.GameObject;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.glfw.GLFW.*;


public class Game {
    private Renderer renderer;

    private Player player;
    private GameObject cursorIndicator;

    private World world;

    private MapManager mapManager;
    private PrefabRegistry prefabRegistry;

    private void loadDependencies() {
        prefabRegistry = new PrefabRegistry();
        mapManager = new MapManager();
    }

    public void init() {
        renderer = new Renderer();
        renderer.init();
        this.loadDependencies();

        world = new World();
        player = new Player(world);

        Mesh cube = PrimitiveMeshes.createCube();
        player.meshes.add(cube);

        player.transform.position.y = 0.5f;
        world.player = player;
        world.objects.add(player);
        renderer.getCamera().target = player;

        cursorIndicator = new GameObject();
        cursorIndicator.meshes.add(cube);
        cursorIndicator.transform.scale.set(0.3f, 0.02f, 0.3f); // flat marker
        world.objects.add(cursorIndicator);

        GameObject sceneRoot = new GameObject();
        sceneRoot.name = "SceneRoot";
        world.objects.add(sceneRoot);

        mapManager = new MapManager();

        var builtMap = mapManager.LoadMap("starter");
        world.objects.addAll(builtMap.elements);
        world.terrain = builtMap.terrain;

        for (int i = 0; i < 5; i++) {

            GameObject obj = new GameObject();
            Mesh obj_mesh = PrimitiveMeshes.createCube();

            obj.selectable = true;
            obj.blocking = true;

            obj.transform.position.set(i * 2f - 4f, 0.5f, -3f);

            obj.meshes.add(obj_mesh);

            obj.AddMeshCollider();
            world.objects.add(obj);
        }

    }
    public void update() {
        renderer.updateProjection();

        boolean lmb = glfwGetMouseButton(WindowContext.window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;
        boolean rmb = glfwGetMouseButton(WindowContext.window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS;

        Renderer.Ray ray = renderer.getMouseRay();
        Vector3f groundHit = renderer.getMouseGroundIntersection();

        if (lmb) {
            GameObject picked = world.pickRay(ray);
            world.selected = picked;

            if (picked != null) {
//                System.out.println(picked.toString());
            }
        }

        if (rmb && groundHit != null) {
            player.moveTarget.set(groundHit);
            player.hasMoveTarget = true;

            player.updateFacing(groundHit);

            cursorIndicator.transform.position.set(groundHit);
            cursorIndicator.transform.position.y = 0.02f;
        }

        player.updateMovement();
        world.update();
    }
    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderer.beginFrame();

        for (GameObject obj : world.objects) {
            renderer.draw(obj, obj == world.selected);
        }

        for (GameObject obj : world.objects) {
            renderer.drawColliders(obj);
        }
    }
}
