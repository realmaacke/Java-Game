package builder.base;

import builder.objects.GameObject;
import org.joml.Matrix4f;
import org.lwjgl.assimp.*;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.assimp.Assimp.*;

public class GLTFLoader {
    public static AIScene load(String path) {
        AIScene scene = aiImportFile(
                path,
                aiProcess_Triangulate |
                aiProcess_JoinIdenticalVertices |
                aiProcess_FlipUVs
        );

        if (scene == null || scene.mRootNode() == null) {
            throw new RuntimeException("Failed to load file: " + aiGetErrorString());
        }
        return scene;
    }


    public static List<Mesh> loadMeshes(String path) {

        AIScene scene = aiImportFile(path,
                aiProcess_Triangulate |
                        aiProcess_JoinIdenticalVertices);

        if (scene == null)
            throw new RuntimeException("Failed to load: " + aiGetErrorString());

        List<Mesh> result = new ArrayList<>();

        int meshCount = scene.mNumMeshes();

        for(int i=0;i<meshCount;i++) {

            AIMesh aiMesh = AIMesh.create(scene.mMeshes().get(i));

            float[] vertices = new float[aiMesh.mNumVertices()*6];

            AIVector3D.Buffer verts = aiMesh.mVertices();
            AIVector3D.Buffer norms = aiMesh.mNormals();

            for(int v=0; v < aiMesh.mNumVertices(); v++) {
                AIVector3D pos = verts.get(v);
                AIVector3D nrm = norms != null ? norms.get(v) : null;

                int base = v * 6;

                vertices[base] = pos.x();
                vertices[base + 1] = pos.y();
                vertices[base + 2] = pos.z();

                if(nrm != null) {
                    vertices[base + 3] = nrm.x();
                    vertices[base + 4] = nrm.y();
                    vertices[base + 5] = nrm.z();
                } else {
                    vertices[base + 3] = 0f;
                    vertices[base + 4] = 1f;
                    vertices[base + 5] = 0f;
                }
            }

            int indexTotal = aiMesh.mNumFaces()*3;
            int[] indices = new int[indexTotal];

            AIFace.Buffer faces = aiMesh.mFaces();

            int idx=0;

            for(int f=0; f<aiMesh.mNumFaces(); f++) {
                AIFace face = faces.get(f);

                indices[idx++] = face.mIndices().get(0);
                indices[idx++] = face.mIndices().get(1);
                indices[idx++] = face.mIndices().get(2);
            }

            Mesh mesh = new Mesh(vertices, indices);

            int materialIndex = aiMesh.mMaterialIndex();

            if (materialIndex >= 0) {
                AIMaterial mat = AIMaterial.create(scene.mMaterials().get(materialIndex));
                AIColor4D color = AIColor4D.create();

                int resultColor = aiGetMaterialColor(
                        mat,
                        AI_MATKEY_COLOR_DIFFUSE,
                        aiTextureType_NONE,
                        0,
                        color
                );

                if (resultColor == 0) {
                    mesh.color.set(color.r(), color.g(), color.b());
                }
            }

            result.add(mesh);
        }

        aiReleaseImport(scene);

        return result;
    }

    public static void buildSceneObjects(
            AIScene scene,
            AINode node,
            List<Mesh> meshes,
            List<GameObject> outObjects
    ) {
        String nodeName = node.mName().dataString();


        int meshCount = node.mNumMeshes();

        if (meshCount > 0) {
            GameObject obj = new GameObject();

            for (int i = 0; i < meshCount; i++) {

                int meshIndex = node.mMeshes().get(i);

                Mesh mesh = meshes.get(meshIndex);

                obj.meshes.add(mesh);
            }

            AIMatrix4x4 m = node.mTransformation();

            Matrix4f transform = new Matrix4f(
                    m.a1(), m.b1(), m.c1(), m.d1(),
                    m.a2(), m.b2(), m.c2(), m.d2(),
                    m.a3(), m.b3(), m.c3(), m.d3(),
                    m.a4(), m.b4(), m.c4(), m.d4()
            );

            transform.getTranslation(obj.transform.position);
            transform.getScale(obj.transform.scale);

            outObjects.add(obj);
        }

        int childCount = node.mNumChildren();

        for (int i = 0; i < childCount; i++) {
            buildSceneObjects(
                    scene,
                    AINode.create(node.mChildren().get(i)),
                    meshes,
                    outObjects
            );
        }
    }


    public static class ReturnData {
            public List<Mesh> mesh;
            public List<GameObject> object;

            public ReturnData(List<Mesh> meshes, List<GameObject> objects) {
                this.mesh = meshes;
                this.object = objects;
            }
    }
    public static ReturnData loadScene(String path) {
        AIScene scene = load(path);

        List<Mesh> meshes = loadMeshes(path);

        List<GameObject> objects = new ArrayList<>();

        buildSceneObjects(scene, scene.mRootNode(), meshes, objects);

        aiReleaseImport(scene);

        return new ReturnData(meshes, objects);
    }
}