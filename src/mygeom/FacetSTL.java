package mygeom;

import java.util.Vector;

public class FacetSTL {
	private Point3D normal;
	private Vector<Point3D> vertices;
	//private Vector<Line3D> edges;
	private int N;
	
	public FacetSTL(){
		vertices = new Vector<Point3D>();
		//edges = new Vector<Line3D>();
		N = 0;
	}
	
	//create a new point (x, y, z)
	//add to the vertices vector
	public void addVertex(double x, double y, double z){
		vertices.add(new Point3D(x, y, z));
		N++;
	}
	
	public int getN(){
		return N;
	}
}
