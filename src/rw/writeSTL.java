package rw;

import mainpack.AddressBook;
import mainpack.Polygon;
import mygeom.Point3D;
import mygeom.Polygon3D;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class writeSTL {
	private String filename = "";
	private BufferedWriter out;
	private Vector<Polygon> ps;

	public writeSTL(String fn, Vector<Polygon> polygonVector)
			throws IOException {
		filename = fn;
		File f = new File(filename);
		if (!f.exists()) {
			// Create file
			FileWriter fstream = new FileWriter(f);
			out = new BufferedWriter(fstream);
		}

		ps = polygonVector;
	}

	public void generateSTL() {
		write2file("solid " + "something.tri\n");
		for (int i = 0; i < ps.size(); i++) {
			writeFace(ps.get(i));
		}
		write2file("endsolid\n");
	}

	public void writeFace(Polygon p) {
		Polygon3D p3d = p.getPolygon3D();
		Point3D normal = p3d.getAbsNormal();
		Point3D vert;

		for (int i = 0; i < p3d.getN() - 2; i++) {// for each triangle
			write2file("facet normal " + normal.getX() + " " + normal.getY()
					+ " " + normal.getZ() + "\n");
			write2file("outer loop\n");

			vert = p3d.getAbsVertexAt(0);
			write2file("vertex " + vert.getX() + " " + vert.getY() + " "
					+ vert.getZ() + "\n");
			vert = p3d.getAbsVertexAt(i + 1);
			write2file("vertex " + vert.getX() + " " + vert.getY() + " "
					+ vert.getZ() + "\n");
			vert = p3d.getAbsVertexAt(i + 2);
			write2file("vertex " + vert.getX() + " " + vert.getY() + " "
					+ vert.getZ() + "\n");

			write2file("endloop\n");
			write2file("endfacet\n");
		}
	}

	public void write2file(String str) {
		try {
			out.write(str);
		} catch (Exception e) {// Catch exception if any
			System.err.println("Write Error: " + e.getMessage());
		}
	}

	// Close the output stream
	public void close() {
		System.out.println("Close the output file.");
		try {
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Close Error: " + e.getMessage());
		}
	}
}
