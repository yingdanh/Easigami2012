package mygeom;

import mygeom.HalfSpace;

import java.util.Vector;

import mainpack.AddressBook;

public class FacetSTL {
	private Point3D normal;
	private Vector<Point3D> vertices;
	private Vector<Line3D> edges;
	private int N;
	private byte[] color;
	private String ptype;
	private HalfSpace hs;
	private boolean isProcessed;

	public FacetSTL() {
		vertices = new Vector<Point3D>();
		edges = new Vector<Line3D>();
		N = 0;
		// color = new byte[3];
	}

	public FacetSTL(Point3D p1, Point3D p2, Point3D p3) {
		vertices = new Vector<Point3D>();
		edges = new Vector<Line3D>();
		N = 0;
		addVertex(p1);
		addVertex(p2);
		addVertex(p3);
		computeHalfSpace(p1, p2, p3);
		isProcessed = false;
	}

	private void computeHalfSpace(Point3D p1, Point3D p2, Point3D p3) {
		hs = new HalfSpace(p1, p2, p3);
		// centre = tri[0].add(tri[1]).add(tri[2]).scale(1.0/3.0);
	}
	
	public HalfSpace getHalfSpace(){
		return hs;
	}

	public boolean inside(Point3D x) {
		return hs.inside(x);
	}
	
	public void setProcessed(boolean tf){
		this.isProcessed = tf;
	}
	
	public boolean isProcessed(){
		return this.isProcessed;
	}

	// create a new point (x, y, z)
	// add to the vertices vector
	public void addVertex(double x, double y, double z) {
		vertices.add(new Point3D(x, y, z));
		N++;
	}

	public void addVertex(Point3D p) {
		vertices.add(new Point3D(p.getX(), p.getY(), p.getZ()));
		N++;
	}

	// set normal vector of a surface
	public void setNormal(double x, double y, double z) {
		normal = new Point3D(x, y, z);
	}

	public Point3D getNormal() {
		return normal;
	}

	// get the count of vertices/sides of a polygon
	public int getN() {
		return N;
	}

	public void assignColor(byte[] cs) {
		color = cs;
	}

	public byte[] getColor() {
		return color;
	}

	public void setVertexAt(int index, Point3D v) {
		Point3D vertex = vertices.get(index);
		// System.out.println("original: " + vertex);
		// System.out.println("new: " + v);
		vertex.setX(v.getX());
		vertex.setY(v.getY());
		vertex.setZ(v.getZ());
	}

	public Vector<Point3D> getVertices() {
		return vertices;
	}

	public Vector<Line3D> getEdges() {
		Line3D edge;

		for (int i = 0; i < vertices.size() - 1; i++) {
			edge = new Line3D(vertices.get(i), vertices.get(i + 1));
			edges.add(edge);
		}

		return edges;
	}

	// get a point by index
	public Point3D getVertexAt(int index) {
		return vertices.get(index);
	}

	public Point3D getCentroid() {
		Point3D centroid = null;

		// System.out.println("check vertices: " + this);
		if (getN() == 3) {
			// System.out.println("v0 " + getVertexAt(0));
			// System.out.println("v1 " + getVertexAt(1));
			// System.out.println("v2 " + getVertexAt(2));

			double d1 = VO3D.distance(getVertexAt(0), getVertexAt(1));
			double d2 = VO3D.distance(getVertexAt(1), getVertexAt(2));
			double d3 = VO3D.distance(getVertexAt(2), getVertexAt(0));
			// System.out.println(d1 + " " + d2 + " " + d3);
			if (Math.abs(d1 - d2) < 1E-10 && Math.abs(d2 - d3) < 1E-10
					&& Math.abs(d3 - d1) < 1E-10) {
				// System.out.println("getCentroid: " +
				// AddressBook.Triangle_Equ_Str);
				this.ptype = AddressBook.Triangle_Equ_Str;
			} else {
				// System.out.println("getCentroid" +
				// AddressBook.Triangle_Iso_Str);
				// this.ptype = AddressBook.Triangle_Iso_Str;
			}

			centroid = getInteraction(
					getVertexAt(0),
					VO3D.middlePoint(new Line3D(getVertexAt(1), getVertexAt(2))),
					getVertexAt(1), VO3D.middlePoint(new Line3D(getVertexAt(2),
							getVertexAt(0))));
			/*
			 * if(centroid == null) centroid = getInteraction(getVertexAt(0),
			 * VO3D.middlePoint(new Line3D(getVertexAt(1), getVertexAt(2))),
			 * getVertexAt(2), VO3D.middlePoint(new Line3D(getVertexAt(0),
			 * getVertexAt(1)))); if(centroid == null) centroid =
			 * getInteraction(getVertexAt(2), VO3D.middlePoint(new
			 * Line3D(getVertexAt(0), getVertexAt(1))), getVertexAt(0),
			 * VO3D.middlePoint(new Line3D(getVertexAt(1), getVertexAt(2))));
			 */
			// System.out.println("Centroid: " + centroid);
		} else if (getN() == 4) {
			centroid = VO3D.middlePoint(new Line3D(getVertexAt(0),
					getVertexAt(2)));
		} else if (getN() == 5) {
			centroid = getInteraction(
					getVertexAt(0),
					VO3D.middlePoint(new Line3D(getVertexAt(2), getVertexAt(3))),
					getVertexAt(1), VO3D.middlePoint(new Line3D(getVertexAt(3),
							getVertexAt(4))));
			if (centroid == null)
				centroid = getInteraction(getVertexAt(1),
						VO3D.middlePoint(new Line3D(getVertexAt(3),
								getVertexAt(4))), getVertexAt(2),
						VO3D.middlePoint(new Line3D(getVertexAt(4),
								getVertexAt(0))));
		} else if (getN() == 6) {
			centroid = VO3D.middlePoint(new Line3D(getVertexAt(0),
					getVertexAt(3)));
		}
		return centroid;
	}

	// Line 1: p0, p1
	// Line 2: p2, p3
	public Point3D getInteraction(Point3D p0, Point3D p1, Point3D p2, Point3D p3) {
		// x = x1 + a*t, y = y1 + b*t, and z = z1 + c*t;
		// x = x2 + d*s, y = y2 + e*s, and z = z2 + f*s
		double s = 0, t = 0;
		// Point3D AB = VO3D.diff_vector(p1, p0);
		// Point3D CD = VO3D.diff_vector(p3, p2);
		// System.out.println("p0 " + p0);
		// System.out.println("p1 " + p1);
		// System.out.println("p2 " + p2);
		// System.out.println("p3 " + p3);
		// System.out.println("AB " + AB);
		// System.out.println("CD " + CD);

		/*
		 * if(AB.getX()==0){ System.out.println("here!"); return null; }
		 * 
		 * if(AB.getX()*CD.getY()-AB.getY()*CD.getX() == 0){ return null; }
		 */
		// if((p0.getX()-p1.getX())*(p3.getY()-p2.getY()) -
		// (p3.getX()-p2.getX())*(p0.getY()-p1.getY()) == 0)
		// return null;

		// System.out.println("upper: " +
		// ((p0.getY()-p2.getY())*AB.getX()+(p2.getX()-p0.getX())*AB.getY()));
		// System.out.println("lower: " +
		// (AB.getX()*CD.getY()-AB.getY()*CD.getX()));
		// double s =
		// ((p0.getY()-p2.getY())*AB.getX()+(p2.getX()-p0.getX())*AB.getY())/(AB.getX()*CD.getY()-AB.getY()*CD.getX());
		// double t = (p2.getX()-p0.getX()+CD.getX()*s)/AB.getX();
		if (ptype.equals(AddressBook.Triangle_Equ_Str)) {
			s = 0.3333333333333333;
			t = 0.3333333333333333;
		} else {
			s = ((p3.getY() - p1.getY()) * (p0.getX() - p1.getX()) - (p3.getX() - p1
					.getX()) * (p0.getY() - p1.getY()))
					/ ((p0.getX() - p1.getX()) * (p3.getY() - p2.getY()) - (p3
							.getX() - p2.getX()) * (p0.getY() - p1.getY()));
			t = ((p3.getX() - p1.getX()) - (p3.getX() - p2.getX()) * s)
					/ (p0.getX() - p1.getX());
		}
		// System.out.println("s = " + s);
		// System.out.println("t = " + t);
		double x = (p0.getX() - p1.getX()) * t + p1.getX();
		double y = (p0.getY() - p1.getY()) * t + p1.getY();
		double z = (p0.getZ() - p1.getZ()) * t + p1.getZ();

		return new Point3D(x, y, z);
	}

	public String toString() {
		String str = "";
		for (int i = 0; i < vertices.size(); i++) {
			str += getVertexAt(i).toString();
		}
		return str;
	}
}
