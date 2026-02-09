package builder;

import builder.base.Mesh;
import org.joml.Vector3f;

import java.util.List;

public class Terrain {
    public List<Mesh> meshes;

    public Terrain(List<Mesh> meshes) {
        this.meshes = meshes;
    }

    public float getHeight(float x, float z) {

        float closestY = -9999f;

        Vector3f v0 = new Vector3f();
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();

        for (Mesh mesh : meshes) {
            float[] verts = mesh.cpuVertices;
            int[] inds = mesh.cpuIndices;

            for(int i=0;i<inds.length;i+=3){

                int i0 = inds[i]*3;
                int i1 = inds[i+1]*3;
                int i2 = inds[i+2]*3;

                v0.set(verts[i0],verts[i0+1],verts[i0+2]);
                v1.set(verts[i1],verts[i1+1],verts[i1+2]);
                v2.set(verts[i2],verts[i2+1],verts[i2+2]);

                Float hit = intersectRayTriangle(x,z,v0,v1,v2);

                if(hit != null){
                    closestY = hit;
                    break;
                }
            }
        }
        return closestY;
    }
    private Float intersectRayTriangle(
            float px, float pz,
            Vector3f a, Vector3f b, Vector3f c){

        Vector3f ab = new Vector3f(b).sub(a);
        Vector3f ac = new Vector3f(c).sub(a);
        Vector3f n  = ab.cross(ac, new Vector3f());

        if(Math.abs(n.y) < 0.0001f) return null;

        float d = -n.dot(a);

        float y = -(n.x*px + n.z*pz + d)/n.y;

        if(pointInTriangle(px,pz,a,b,c))
            return y;

        return null;
    }

    private boolean pointInTriangle(
            float px,float pz,
            Vector3f a,Vector3f b,Vector3f c){

        float d1 = sign(px,pz,a,b);
        float d2 = sign(px,pz,b,c);
        float d3 = sign(px,pz,c,a);

        boolean hasNeg = (d1<0)||(d2<0)||(d3<0);
        boolean hasPos = (d1>0)||(d2>0)||(d3>0);

        return !(hasNeg && hasPos);
    }

    private float sign(float px,float pz,Vector3f v1,Vector3f v2){
        return (px-v2.x)*(v1.z-v2.z)-(v1.x-v2.x)*(pz-v2.z);
    }
}
