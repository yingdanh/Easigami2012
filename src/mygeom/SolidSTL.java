package mygeom;

import java.util.Stack;
import java.util.Vector;

public class SolidSTL {
	private String solidName;
	private Vector<FacetSTL> faces;
	private Vector<FacetSTL> chFaces; // convex hull

	public SolidSTL(String name) {
		solidName = name;
		faces = new Vector<FacetSTL>();
	}

	public void addFace(FacetSTL f) {
		faces.add(f);
	}

	public Vector<FacetSTL> getFaces() {
		return faces;
	}

	public FacetSTL getFaceAt(int index) {
		return faces.get(index);
	}

	// get convex hull using incremental algorithm
	// for this solid
	public void incrementalCovexhull() {
		chFaces = new Vector<FacetSTL>();
		// used to find boundary of hole
		Stack<Line3D> edgeStack = new Stack<Line3D>();
		Vector<Point3D> verts = new Vector<Point3D>();
		boolean inside = true;
		//int frameNo = 1;
		FacetSTL f;
		Vector<Point3D> vs;

		// when there is no face existed
		if (faces.size() == 0)
			return;

		// put all the vertices together
		boolean found = false;
		for (int i = 0; i < faces.size(); i++) {
			vs = faces.get(i).getVertices();
			for(int j=0; j<vs.size(); j++){//for each new point
				found = false;
				for(int k=0; k<verts.size(); k++){//check existing points
					if((vs.get(j)).compareTo(verts.get(k))==0)
						found = true;
				}
				if(!found)
					verts.add(vs.get(j));
			}
		}
		System.out.println("there are " + verts.size() + " points.");

		// add first triangles
		chFaces.add(new FacetSTL(verts.get(0), verts.get(1), verts.get(2)));
		System.out.println("check hs: " + chFaces.get(0).getHalfSpace());
		//frameNo++; // frameNo==2
		chFaces.add(new FacetSTL(verts.get(0), verts.get(2), verts.get(1)));
		//frameNo++; // frameNo==3

		// now the main loop -- add vertices one at a time
		// for (int i = 3; i < pts.length; i++) {
		for (int i = 3; i < verts.size(); i++) {
			System.out.println("i = " + i);
			// delete faces that this vertex can see
			inside = true; // are we inside the hull?

			//loop through all the existing faces of the convex hull
			for (int j = 0; j < chFaces.size(); j++) {
				System.out.println("j = " + j);
				f = chFaces.get(j);
				System.out.println(f.getVertexAt(0) + ", " + f.getVertexAt(1) + ", " + f.getVertexAt(2));
				if (!f.isProcessed() && f.inside(verts.get(i))) {
					System.out.println("out!");
					inside = false;
					f.setProcessed(true);
					// update boundary of hole
					edgeStack.push(new Line3D(f.getVertexAt(0), f
							.getVertexAt(1)));
					edgeStack.push(new Line3D(f.getVertexAt(1), f
							.getVertexAt(2)));
					edgeStack.push(new Line3D(f.getVertexAt(2), f
							.getVertexAt(0)));
					chFaces.remove(j--);
				}
			}
			if (inside)continue;
			// frameNo++;

			// mend the hole use the next vertex
			while (!edgeStack.isEmpty()) {
				System.out.println("while");
				Line3D e = edgeStack.pop();
				System.out.println(e.getP1() + ", " + e.getP2() + ", " + verts.get(i));
				chFaces.add(new FacetSTL(e.getP1(), e.getP2(), verts.get(i)));
			}
			System.out.println("Convex Hull faces: " + chFaces.size());
			//frameNo++;
		}
		
		System.out.println("Convex Hull is Finalized!");
	}
	
	public Vector<FacetSTL> getConvexHull(){
		return chFaces;
	}

	public void translate(double xt, double yt, double zt) {
		// System.out.println("in SolidSTL::translate");
		FacetSTL face;
		Vector<Point3D> vertices;
		Point3D vertex;

		double[][] T = VO3D.getMatrix_translation(xt, yt, zt);
		for (int i = 0; i < faces.size(); i++) {
			face = faces.get(i);
			vertices = face.getVertices();
			for (int j = 0; j < vertices.size(); j++) {
				vertex = vertices.get(j);
				face.setVertexAt(j, VO3D.matrix41_mult(T, vertex));
			}
		}
	}

	public void scale(double xs, double ys, double zs) {
		FacetSTL face;
		Vector<Point3D> vertices;
		Point3D vertex;

		double[][] S = VO3D.getMatrix_scale(xs, ys, zs);
		for (int i = 0; i < faces.size(); i++) {
			face = faces.get(i);
			vertices = face.getVertices();
			for (int j = 0; j < vertices.size(); j++) {
				vertex = vertices.get(j);
				face.setVertexAt(j, VO3D.matrix41_mult(S, vertex));
			}
		}
	}

	public void rotate(double xr, double yr, double zr) {
		FacetSTL face;
		Vector<Point3D> vertices;
		Point3D vertex;

		double[][] rotationX = VO3D.getMatrix_rotateX(xr);
		double[][] rotationY = VO3D.getMatrix_rotateY(yr);
		double[][] rotationZ = VO3D.getMatrix_rotateZ(zr);

		Point3D afterx, aftery, afterz;

		for (int i = 0; i < faces.size(); i++) {
			face = faces.get(i);
			vertices = face.getVertices();
			for (int j = 0; j < vertices.size(); j++) {
				vertex = vertices.get(j);
				afterx = VO3D.matrix41_mult(rotationX, vertex);
				aftery = VO3D.matrix41_mult(rotationY, afterx);
				afterz = VO3D.matrix41_mult(rotationZ, aftery);
				face.setVertexAt(j, afterz);
			}
		}
	}
}
