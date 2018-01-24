package delaunay;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

class Triangle extends ArraySet<Pnt> {

    private int idNumber;                   
    private Pnt circumcenter = null;        
    private Pnt barycenter=null; 
    private double area;
    private static int idGenerator = 0;     
    public static boolean moreInfo = false; 

    public Triangle (Pnt... vertices) {
        this(Arrays.asList(vertices));
    }

   
    public Triangle (Collection<? extends Pnt> collection) {
        super(collection);
        idNumber = idGenerator++;
       
    }

    @Override
    public String toString () {
        if (!moreInfo) return "Triangle" + idNumber;
        return "Triangle" + idNumber + super.toString();
    }

    public Pnt getVertexButNot (Pnt... badVertices) {
        Collection<Pnt> bad = Arrays.asList(badVertices);
        for (Pnt v: this) if (!bad.contains(v)) return v;
        throw new NoSuchElementException("");
    }

  
    public boolean isNeighbor (Triangle triangle) {
        int count = 0;
        for (Pnt vertex: this)
            if (!triangle.contains(vertex)) count++;
        return count == 1;
    }

  
    public ArraySet<Pnt> facetOpposite (Pnt vertex) {
        ArraySet<Pnt> facet = new ArraySet<Pnt>(this);
        if (!facet.remove(vertex))
            throw new IllegalArgumentException("");
        return facet;
    }

    
    public Pnt getCircumcenter () {
        if (circumcenter == null)
            circumcenter = Pnt.circumcenter(this.toArray(new Pnt[0]));
        return circumcenter;
    }
    public Pnt getFocus()
    {
		barycenter=Pnt.Focus(this.toArray(new Pnt[0]));
		return barycenter;
    	
    }
    public double getarea(){
    	area=Pnt.area(this.toArray(new Pnt[0]));
    	return area;
    }

    @Override
    public boolean add (Pnt vertex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Pnt> iterator () {
        return new Iterator<Pnt>() {
            private Iterator<Pnt> it = Triangle.super.iterator();
            public boolean hasNext() {return it.hasNext();}
            public Pnt next() {return it.next();}
            public void remove() {throw new UnsupportedOperationException();}
        };
    }
//    @Override
//    public int hashCode () {
//        return (int)(idNumber^(idNumber>>>32));
//    }
    @Override
    public boolean equals (Object o) {
        return (this == o);
    }

}