package delaunay;



import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;


public class Triangulation extends AbstractSet<Triangle> {

    private Triangle mostRecent = null;      
    private Graph<Triangle> triGraph;        
    public Triangulation (Triangle triangle) {
        triGraph = new Graph<Triangle>();
        triGraph.add(triangle);
        mostRecent = triangle;
    }


    @Override
    public Iterator<Triangle> iterator () {
        return triGraph.nodeSet().iterator();
    }

    @Override
    public int size () {
        return triGraph.nodeSet().size();
    }

    @Override
    public String toString () {
        return "Triangulation with " + size() + " triangles";
    }

 
    public boolean contains (Object triangle) {
        return triGraph.nodeSet().contains(triangle);
    }

   
    public Triangle neighborOpposite (Pnt site, Triangle triangle) {

        for (Triangle neighbor: triGraph.neighbors(triangle)) {
            if (!neighbor.contains(site)) return neighbor;
        }
        return null;
    }

 
    public Set<Triangle> neighbors(Triangle triangle) {
        return triGraph.neighbors(triangle);
    }

   
    public List<Triangle> surroundingTriangles (Pnt site, Triangle triangle) {
     
        List<Triangle> list = new ArrayList<Triangle>();
        Triangle start = triangle;
        Pnt guide = triangle.getVertexButNot(site);        
        while (true) {
            list.add(triangle);
            Triangle previous = triangle;
            triangle = this.neighborOpposite(guide, triangle); 
            guide = previous.getVertexButNot(site, guide);     
            if (triangle == start) break;
        }
        return list;
    }

    
    public Triangle locate (Pnt point) {
        Triangle triangle = mostRecent;
        if (!this.contains(triangle)) triangle = null;
        Set<Triangle> visited = new HashSet<Triangle>();
        while (triangle != null) {

            visited.add(triangle);
            Pnt corner = point.isOutside(triangle.toArray(new Pnt[0]));
            if (corner == null) return triangle;
            triangle = this.neighborOpposite(corner, triangle);
        }
        for (Triangle tri: this) {
            if (point.isOutside(tri.toArray(new Pnt[0])) == null) return tri;
        }
        return null;
    }

    /**
     * 建立新的节点在DT
     */
    public void delaunayPlace (Pnt site) {

        Triangle triangle = locate(site);
   
        if (triangle.contains(site)) return;//如果triangle包含这个点，返回空
        Set<Triangle> cavity = getCavity(site, triangle);
        mostRecent = update(site, cavity);
    }

    
    private Set<Triangle> getCavity (Pnt site, Triangle triangle) {
        Set<Triangle> encroached = new HashSet<Triangle>();
        Queue<Triangle> toBeChecked = new LinkedList<Triangle>();
        Set<Triangle> marked = new HashSet<Triangle>();
        toBeChecked.add(triangle);
        marked.add(triangle);
        while (!toBeChecked.isEmpty()) {
            triangle = toBeChecked.remove();
            if (site.vsCircumcircle(triangle.toArray(new Pnt[0])) == 1)
                continue; 
            encroached.add(triangle);
            for (Triangle neighbor: triGraph.neighbors(triangle)){
                if (marked.contains(neighbor)) continue;
                marked.add(neighbor);
                toBeChecked.add(neighbor);
            }
        }
        return encroached;
    }

    private Triangle update (Pnt site, Set<Triangle> cavity) {
        Set<Set<Pnt>> boundary = new HashSet<Set<Pnt>>();
        Set<Triangle> theTriangles = new HashSet<Triangle>();
        for (Triangle triangle: cavity) {
            theTriangles.addAll(neighbors(triangle));
            for (Pnt vertex: triangle) {
                Set<Pnt> facet = triangle.facetOpposite(vertex);
                if (boundary.contains(facet)) boundary.remove(facet);
                else boundary.add(facet);
            }
        }
        theTriangles.removeAll(cavity);        

        for (Triangle triangle: cavity) triGraph.remove(triangle);
        Set<Triangle> newTriangles = new HashSet<Triangle>();
        for (Set<Pnt> vertices: boundary) {
            vertices.add(site);
            Triangle tri = new Triangle(vertices);
            triGraph.add(tri);
            newTriangles.add(tri);
        }

        theTriangles.addAll(newTriangles);    
        for (Triangle triangle: newTriangles)
            for (Triangle other: theTriangles)
                if (triangle.isNeighbor(other))
                    triGraph.add(triangle, other);

        return newTriangles.iterator().next();
    }


}