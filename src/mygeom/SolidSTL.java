package mygeom;

import java.util.Vector;

class SolidSTL {
	private String solidName;
	private Vector<FacetSTL> faces;
	
	public SolidSTL(String name){
		solidName = name;
		faces = new Vector<FacetSTL>();
	}
	
	public void addFace(FacetSTL f){
		faces.add(f);
	}
	
	public Vector<FacetSTL> getFaces(){
		return faces;
	}
	
	//inner class
	private class FacetSTL{
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
}
