package mygeom;

public class HalfSpace {

	/* final */Point3D normal; // normal to boundary plane
	/* final */double d; // eqn of half space is normal.x - d > 0

	/**
	 * Create a half space
	 */
	public HalfSpace(Point3D a, Point3D b, Point3D c) {
		normal = VO3D.unit(VO3D.cross(VO3D.diff_vector(b, a),
				VO3D.diff_vector(c, a)));
		d = VO3D.dot(normal, a);
	}

	/**
	 * Create a half space parallel to z axis
	 */
	/*
	 * public HalfSpace(Point3D a,Point3D b){ normal =
	 * b.subtract(a).cross(Point3d.k).normalize(); d = normal.dot(a); }
	 */

	public boolean inside(Point3D x) {
		return VO3D.dot(normal, x) > d;
	}

	/**
	 * z coordinate of intersection of a vertical line through p and boundary
	 * plane
	 */
	/*
	 * public double zint(Point3d p){ return (d - normal.x()*p.x() -
	 * normal.y()*p.y()) / normal.z(); }
	 */

}
