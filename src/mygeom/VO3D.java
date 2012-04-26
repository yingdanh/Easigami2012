package mygeom;

public class VO3D extends Point3D {

	public VO3D() {
	}

	/* vector addition */
	public static Point3D add_vector(Point3D p1, Point3D p2) {
		return new Point3D(p1.getX() + p2.getX(), p1.getY() + p2.getY(),
				p1.getZ() + p2.getZ());
	}

	/* vector difference */
	public static Point3D diff_vector(Point3D p1, Point3D p2) {
		return new Point3D(p1.getX() - p2.getX(), p1.getY() - p2.getY(),
				p1.getZ() - p2.getZ());
	}

	/* vector scalar multiplication */
	public static Point3D scalar_vector(double s, Point3D p1) {
		return new Point3D(s * p1.getX(), s * p1.getY(), s * p1.getZ());
	}

	/* cross product */
	public static Point3D cross(Point3D p1, Point3D p2) {
		return new Point3D(p1.getY() * p2.getZ() - p1.getZ() * p2.getY(),
				p1.getZ() * p2.getX() - p1.getX() * p2.getZ(), p1.getX()
						* p2.getY() - p1.getY() * p2.getX());
	}

	/* dot product */
	public static double dot(Point3D a, Point3D b) {
		return a.getX() * b.getX() + a.getY() * b.getY() + a.getZ() * b.getZ();
	}

	/* get unit vector */
	public static Point3D unit(Point3D p) {
		double len = Math.sqrt(Math.pow(p.getX(), 2) + Math.pow(p.getY(), 2)
				+ Math.pow(p.getZ(), 2));
		if (len != 0)
			return scalar_vector(1 / len, p);
		return p;
	}

	/* the length of a 3D vector */
	public static double length(Point3D p) {
		return Math.sqrt(Math.pow(p.getX(), 2) + Math.pow(p.getY(), 2)
				+ Math.pow(p.getZ(), 2));
	}

	public static double distance(Point3D p1, Point3D p2) {
		double dx = p1.getX() - p2.getX();
		double dy = p1.getY() - p2.getY();
		double dz = p1.getZ() - p2.getZ();

		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	public static Point3D getNormal(Point3D p0, Point3D p1, Point3D p2) {
		return cross(diff_vector(p1, p0), diff_vector(p2, p0));
	}

	// get the middle point of a line
	public static Point3D middlePoint(Line3D l) {
		Point3D p1 = l.getP1();
		Point3D p2 = l.getP2();
		return new Point3D((p1.getX() + p2.getX()) / 2,
				(p1.getY() + p2.getY()) / 2, (p1.getZ() + p2.getZ()) / 2);
	}

	/**
	 * calculate angle by dot product
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double getAngleDeg(Point3D a, Point3D b) {
		double cosine = dot(a, b) / (length(a) * length(b));
		return Math.toDegrees(Math.acos(cosine));
	}

	public static double getAngleRad(Point3D a, Point3D b) {
		double cosine = dot(a, b) / (length(a) * length(b));
		return Math.acos(cosine);
	}

	public static double[][] formLocalCoordinates2D(Point3D axis) {
		return getBasis(axis);
		// flatMatrix44(mat, basis);
	}

	public static double[][] GetBasisTranspose(Point3D axis) {
		double[][] basis = getBasis(axis);
		return matrix_transpose(basis);
	}

	/*
	 * generate a new coordinate system, which will be contained in mat[] axis
	 * angdeg will be the angle is between the new X-Z plane and the original
	 * X-Z plane
	 */
	public static double[][] formLocalCoordinates3D(Point3D axis, double angrad) {
		double[][] basis = getBasis(axis);
		printMatrix44(basis);
		// Ruvw(T) * rotation-z * Ruvw
		double[][] ZRuvw = matrixMultiply(matrix_transpose(basis),
				getMatrix_rotateZ(angrad));

		return ZRuvw;
	}

	/*
	 * convert 16x1 array to 4x4 matrix
	 */
	public static double[][] squareMatrix44(double[] mat) {
		if (mat.length != 16)
			throw new RuntimeException("ERROR: the length of the matrix.");
		double[][] matrix = new double[4][4];
		for (int i = 0; i < 16; i++) {
			matrix[i % 4][i / 4] = mat[i];
		}

		return matrix;
	}

	/*
	 * change a 4x4 matrix to 1D array
	 */
	public static void flatMatrix44(double[] mat, double m44[][]) {
		// fill out the 1D array mat
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				mat[i * 4 + j] = m44[j][i];
			}
		}
	}

	/*
	 * get an identity 4x4 array
	 */
	public static double[][] getIdentityMatrix44() {
		double[][] identity = new double[4][4];
		for (int i = 0; i < 4; i++) {
			identity[i][i] = 1;
		}
		return identity;
	}

	public static double[] getIdentityMatrix() {
		double[] identity = { 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1 };
		return identity;
	}

	public static double[][] getZeroMatrix44() {
		double[][] togo = new double[4][4];
		return togo;
	}

	public static double[][] point_by_angle(Point3D axis, double angle) {
		double c = Math.cos(angle), s = Math.sin(angle), c1 = 1.0 - c;
		double ux = axis.getX(), uy = axis.getY(), uz = axis.getZ();
		double[][] matrix = {
	    	 {c + ux * ux * c1,       ux * uy * c1 - uz * s,  ux * uz * c1 + uy * s, 0},
		     {uy * ux * c1 + uz * s,  c + uy * uy * c1,       uy * uz * c1 - ux * s, 0},
		     {uz * ux * c1 - uy * s,  uz * uy * c1 + ux * s,  c + uz * uz * c1,      0},
		     {0, 0, 0, 1}
		};
		return matrix;  
	}   

	/*
	 * Constructing a basis (4x4) from a single vector <Fundamentals of Computer
	 * Graphics> - Peter Shirley, page 29
	 */
	private static double[][] getBasis(Point3D axis) {
		// System.out.println("calculate basis: " + axis);
		Point3D w = unit(axis); // unit axis
		// rotate unit axis w count-clockwise with 90 degree, and then get the
		// unit vector
		Point3D u = unit(matrix41_mult(getMatrix_rotateY(Math.toRadians(90)), w));
		Point3D v = cross(w, u);

		double[][] basis = { { u.getX(), u.getY(), u.getZ(), 0 },
				{ v.getX(), v.getY(), v.getZ(), 0 },
				{ w.getX(), w.getY(), w.getZ(), 0 }, { 0, 0, 0, 1 } };
		// System.out.println("u: " + u.getX() + " " + u.getY() + " " +
		// u.getZ());
		// System.out.println("v: " + v.getX() + " " + v.getY() + " " +
		// v.getZ());
		// System.out.println("w: " + w.getX() + " " + w.getY() + " " +
		// w.getZ());
		return basis;
	}

	/*
	 * 4x4 and 1x3 matrix multiplication make use of the homogeneous matrices
	 */
	public static Point3D matrix41_mult(double[][] a, Point3D p) {
		Point3D result = new Point3D();
		result.setX(a[0][0] * p.getX() + a[0][1] * p.getY() + a[0][2]
				* p.getZ() + a[0][3]);
		result.setY(a[1][0] * p.getX() + a[1][1] * p.getY() + a[1][2]
				* p.getZ() + a[1][3]);
		result.setZ(a[2][0] * p.getX() + a[2][1] * p.getY() + a[2][2]
				* p.getZ() + a[2][3]);

		return result;
	}

	/*
	 * 4x4 matrix subtraction
	 */
	public static double[][] matrixSubtract(double[][] a, double[][] b) {
		double[][] c = new double[4][4];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				c[i][j] = a[i][j] - b[i][j];
			}
		}
		return c;
	}

	public static double sumProduct(double[][] a, double[][] b) {
		double sum = 0.0;
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				sum += a[i][j] * b[i][j];
			}
		}
		return sum;
	}

	/*
	 * 4x4 matrix multiplication
	 */
	public static double[][] matrixMultiply(double[][] a, double[][] b) {
		double[][] c = new double[4][4];

		double temp = 0;

		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				temp = 0;
				for (int n = 0; n < 4; n++) {
					temp += a[i][n] * b[n][j];
				}
				c[i][j] = temp;
			}
		}

		return c;
	}

	// 16x1 matrix
	public static double[] matrixMultiply(double[] a, double[] b) {
		double[] c = new double[16];

		c[0] = a[0] * b[0] + a[4] * b[1] + a[8] * b[2] + a[12] * b[3];
		c[1] = a[1] * b[0] + a[5] * b[1] + a[9] * b[2] + a[13] * b[3];
		c[2] = a[2] * b[0] + a[6] * b[1] + a[10] * b[2] + a[14] * b[3];
		c[3] = a[3] * b[0] + a[7] * b[1] + a[11] * b[2] + a[15] * b[3];
		c[4] = a[0] * b[4] + a[4] * b[5] + a[8] * b[6] + a[12] * b[7];
		c[5] = a[1] * b[4] + a[5] * b[5] + a[9] * b[6] + a[13] * b[7];
		c[6] = a[2] * b[4] + a[6] * b[5] + a[10] * b[6] + a[14] * b[7];
		c[7] = a[3] * b[4] + a[7] * b[5] + a[11] * b[6] + a[15] * b[7];
		c[8] = a[0] * b[8] + a[4] * b[9] + a[8] * b[10] + a[12] * b[11];
		c[9] = a[1] * b[8] + a[5] * b[9] + a[9] * b[10] + a[13] * b[11];
		c[10] = a[2] * b[8] + a[6] * b[9] + a[10] * b[10] + a[14] * b[11];
		c[11] = a[3] * b[8] + a[7] * b[9] + a[11] * b[10] + a[15] * b[11];
		c[12] = a[0] * b[12] + a[4] * b[13] + a[8] * b[14] + a[12] * b[15];
		c[13] = a[1] * b[12] + a[5] * b[13] + a[9] * b[14] + a[13] * b[15];
		c[14] = a[2] * b[12] + a[6] * b[13] + a[10] * b[14] + a[14] * b[15];
		c[15] = a[3] * b[12] + a[7] * b[13] + a[11] * b[14] + a[15] * b[15];
		/*
		 * for (int i = 0; i < 4; i++) { for (int j = 0; j < 16; j += 4) {
		 * System.out.print(c[i + j] + " "); } System.out.println(); }
		 */
		return c;
	}

	/*
	 * transpose 4x4 matrix
	 */
	public static double[][] matrix_transpose(double[][] r) {
		double[][] t = new double[4][4];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++)
				t[i][j] = r[j][i];
		}
		return t;
	}

	public static double[][] getMatrix_rotateX(double angle) {
		double rotate_x[][] = { { 1, 0, 0, 0 },
				{ 0, Math.cos(angle), -Math.sin(angle), 0 },
				{ 0, Math.sin(angle), Math.cos(angle), 0 }, { 0, 0, 0, 1 } };

		return rotate_x;
	}

	public static double[][] getMatrix_rotateY(double angle) {
		double rotate_y[][] = { { Math.cos(angle), 0, Math.sin(angle), 0 },
				{ 0, 1, 0, 0 }, { -Math.sin(angle), 0, Math.cos(angle), 0 },
				{ 0, 0, 0, 1 } };

		return rotate_y;
	}

	public static double[][] getMatrix_rotateZ(double angle) {
		double rotate_z[][] = { { Math.cos(angle), -Math.sin(angle), 0, 0 },
				{ Math.sin(angle), Math.cos(angle), 0, 0 }, { 0, 0, 1, 0 },
				{ 0, 0, 0, 1 } };

		return rotate_z;
	}

	public static double[][] getMatrix_translation(double xt, double yt,
			double zt) {
		double translate[][] = { { 1, 0, 0, xt }, { 0, 1, 0, yt },
				{ 0, 0, 1, zt }, { 0, 0, 0, 1 } };

		return translate;
	}

	public static double[][] getMatrix_scale(double xs, double ys, double zs) {
		double[][] scale = { { xs, 0, 0, 0 }, { 0, ys, 0, 0 }, { 0, 0, zs, 0 },
				{ 0, 0, 0, 1 } };

		return scale;
	}

	public static Point3D calPoint(double transformation[], Point3D p) {
		return matrix41_mult(squareMatrix44(transformation), p);
	}

	/*
	 * m0*m'=m m' = m*INV(m0)
	 */
	public static double[] calTransformationMatrix(double m0[], double m[]) {
		double[] invOut = new double[16];
		gluInvertMatrix(m0, invOut);
		return matrixMultiply(m, invOut);
	}

	/*
	 * public static double[][] invertMatrix(double[][] a) { double[] flat = new
	 * double[16]; flatMatrix44(flat, a); double[] flatinv = new double[16];
	 * gluInvertMatrix(flat, flatinv); return squareMatrix44(flatinv); }
	 */

	/*
	 * m, invOut - 1D array with size 16
	 */
	public static boolean gluInvertMatrix(double m[], double invOut[]) {
		double[] inv = new double[16];
		double det;
		int i;

		inv[0] = m[5] * m[10] * m[15] - m[5] * m[11] * m[14] - m[9] * m[6]
				* m[15] + m[9] * m[7] * m[14] + m[13] * m[6] * m[11] - m[13]
				* m[7] * m[10];
		inv[4] = -m[4] * m[10] * m[15] + m[4] * m[11] * m[14] + m[8] * m[6]
				* m[15] - m[8] * m[7] * m[14] - m[12] * m[6] * m[11] + m[12]
				* m[7] * m[10];
		inv[8] = m[4] * m[9] * m[15] - m[4] * m[11] * m[13] - m[8] * m[5]
				* m[15] + m[8] * m[7] * m[13] + m[12] * m[5] * m[11] - m[12]
				* m[7] * m[9];
		inv[12] = -m[4] * m[9] * m[14] + m[4] * m[10] * m[13] + m[8] * m[5]
				* m[14] - m[8] * m[6] * m[13] - m[12] * m[5] * m[10] + m[12]
				* m[6] * m[9];
		inv[1] = -m[1] * m[10] * m[15] + m[1] * m[11] * m[14] + m[9] * m[2]
				* m[15] - m[9] * m[3] * m[14] - m[13] * m[2] * m[11] + m[13]
				* m[3] * m[10];
		inv[5] = m[0] * m[10] * m[15] - m[0] * m[11] * m[14] - m[8] * m[2]
				* m[15] + m[8] * m[3] * m[14] + m[12] * m[2] * m[11] - m[12]
				* m[3] * m[10];
		inv[9] = -m[0] * m[9] * m[15] + m[0] * m[11] * m[13] + m[8] * m[1]
				* m[15] - m[8] * m[3] * m[13] - m[12] * m[1] * m[11] + m[12]
				* m[3] * m[9];
		inv[13] = m[0] * m[9] * m[14] - m[0] * m[10] * m[13] - m[8] * m[1]
				* m[14] + m[8] * m[2] * m[13] + m[12] * m[1] * m[10] - m[12]
				* m[2] * m[9];
		inv[2] = m[1] * m[6] * m[15] - m[1] * m[7] * m[14] - m[5] * m[2]
				* m[15] + m[5] * m[3] * m[14] + m[13] * m[2] * m[7] - m[13]
				* m[3] * m[6];
		inv[6] = -m[0] * m[6] * m[15] + m[0] * m[7] * m[14] + m[4] * m[2]
				* m[15] - m[4] * m[3] * m[14] - m[12] * m[2] * m[7] + m[12]
				* m[3] * m[6];
		inv[10] = m[0] * m[5] * m[15] - m[0] * m[7] * m[13] - m[4] * m[1]
				* m[15] + m[4] * m[3] * m[13] + m[12] * m[1] * m[7] - m[12]
				* m[3] * m[5];
		inv[14] = -m[0] * m[5] * m[14] + m[0] * m[6] * m[13] + m[4] * m[1]
				* m[14] - m[4] * m[2] * m[13] - m[12] * m[1] * m[6] + m[12]
				* m[2] * m[5];
		inv[3] = -m[1] * m[6] * m[11] + m[1] * m[7] * m[10] + m[5] * m[2]
				* m[11] - m[5] * m[3] * m[10] - m[9] * m[2] * m[7] + m[9]
				* m[3] * m[6];
		inv[7] = m[0] * m[6] * m[11] - m[0] * m[7] * m[10] - m[4] * m[2]
				* m[11] + m[4] * m[3] * m[10] + m[8] * m[2] * m[7] - m[8]
				* m[3] * m[6];
		inv[11] = -m[0] * m[5] * m[11] + m[0] * m[7] * m[9] + m[4] * m[1]
				* m[11] - m[4] * m[3] * m[9] - m[8] * m[1] * m[7] + m[8] * m[3]
				* m[5];
		inv[15] = m[0] * m[5] * m[10] - m[0] * m[6] * m[9] - m[4] * m[1]
				* m[10] + m[4] * m[2] * m[9] + m[8] * m[1] * m[6] - m[8] * m[2]
				* m[5];

		det = m[0] * inv[0] + m[1] * inv[4] + m[2] * inv[8] + m[3] * inv[12];
		if (det == 0)
			return false;

		det = 1.0 / det;

		for (i = 0; i < 16; i++)
			invOut[i] = inv[i] * det;

		return true;
	}

	/*
	 * calculate determinant | m[0][0] m[0][1] m[0][2] m[0][3] | | m[1][0]
	 * m[1][1] m[1][2] m[1][3] | | m[2][0] m[2][1] m[2][2] m[2][3] | | m[3][0]
	 * m[3][1] m[3][2] m[3][3] |
	 */
	public static double detMatrix(double[][] m) {
		double A = m[0][3] * m[1][2] * m[2][1] * m[3][0] - m[0][2] * m[1][3]
				* m[2][1] * m[3][0] - m[0][3] * m[1][1] * m[2][2] * m[3][0]
				+ m[0][1] * m[1][3] * m[2][2] * m[3][0] + m[0][2] * m[1][1]
				* m[2][3] * m[3][0] - m[0][1] * m[1][2] * m[2][3] * m[3][0]
				- m[0][3] * m[1][2] * m[2][0] * m[3][1] + m[0][2] * m[1][3]
				* m[2][0] * m[3][1] + m[0][3] * m[1][0] * m[2][2] * m[3][1]
				- m[0][0] * m[1][3] * m[2][2] * m[3][1] - m[0][2] * m[1][0]
				* m[2][3] * m[3][1] + m[0][0] * m[1][2] * m[2][3] * m[3][1]
				+ m[0][3] * m[1][1] * m[2][0] * m[3][2] - m[0][1] * m[1][3]
				* m[2][0] * m[3][2] - m[0][3] * m[1][0] * m[2][1] * m[3][2]
				+ m[0][0] * m[1][3] * m[2][1] * m[3][2] + m[0][1] * m[1][0]
				* m[2][3] * m[3][2] - m[0][0] * m[1][1] * m[2][3] * m[3][2]
				- m[0][2] * m[1][1] * m[2][0] * m[3][3] + m[0][1] * m[1][2]
				* m[2][0] * m[3][3] + m[0][2] * m[1][0] * m[2][1] * m[3][3]
				- m[0][0] * m[1][2] * m[2][1] * m[3][3] - m[0][1] * m[1][0]
				* m[2][2] * m[3][3] + m[0][0] * m[1][1] * m[2][2] * m[3][3];

		return A;
	}

	// http://wiki.answers.com/Q/Determinant_of_matrix_in_java
	public double determinant(double[][] mat) {
		double result = 0;

		if (mat.length == 1) {
			result = mat[0][0];
			return result;
		}

		if (mat.length == 2) {
			result = mat[0][0] * mat[1][1] - mat[0][1] * mat[1][0];
			return result;
		}

		for (int i = 0; i < mat[0].length; i++) {
			double temp[][] = new double[mat.length - 1][mat[0].length - 1];

			for (int j = 1; j < mat.length; j++) {
				System.arraycopy(mat[j], 0, temp[j - 1], 0, i);
				System.arraycopy(mat[j], i + 1, temp[j - 1], i, mat[0].length
						- i - 1);
			}

			result += mat[0][i] * Math.pow(-1, i) * determinant(temp);
		}

		return result;

	}

	/**
	 * 
	 * @return unit z axis
	 */
	/*
	 * public static Point3D getAxisZ2D() { return new Point3D(0, 0, 1); }
	 */

	/**
	 * print out a 2D matrix
	 * 
	 * @param a
	 */
	public static void printMatrix44(double[][] a) {
		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[i].length; j++) {
				System.out.format("%10.3f", a[i][j]);
			}
			System.out.println();
		}
	}

	/**
	 * print out a 1D matrix
	 * 
	 * @param a
	 */
	public static void printMatrix16(double[] a) {
		/*
		 * for (int i = 0; i < a.length; i++) { System.out.print(a[i] + " "); }
		 * System.out.println();
		 */
		printMatrix44(squareMatrix44(a));
	}

	// make sure the previous polygon is at the right of the
	// vector axis
	/*
	 * public static Point3D adjustAxis(Point3D p1, Point3D p2, Point3D right,
	 * boolean isLeft) { Point3D axis = VO3D.diff_vector(p2, p1);
	 * System.out.println("tentative axis: " + axis + ", " + "opp: " + right);
	 * if (isLeft) { System.out.println("the right is drawn!"); // the right
	 * polygon is drawn, so check if opp is at the right // if it isn't, change
	 * the direction System.out.println(VO3D.getSide(axis, right)); if
	 * (VO3D.getSide(axis, right).equals("LEFT")) {// make it as left axis =
	 * VO3D.diff_vector(p1, p2); } } else {
	 * System.out.println("the left is drawn!"); // the left polygon is drawn,
	 * so check if opp is at the left // if it isn't, change the direction
	 * System.out.println(VO3D.getSide(axis, right)); if (VO3D.getSide(axis,
	 * right).equals("RIGHT")) {// make is as right axis = VO3D.diff_vector(p1,
	 * p2); } }
	 * 
	 * // System.out.println("axis: " + axis); return axis; }
	 */

	public static boolean adjustAxis(Point3D right, boolean isLeft) {
		Point3D z = new Point3D(0, 0, -10);
		if (isLeft) {
			// System.out.println("the right is drawn! Need to draw left now.");
			// System.out.println(VO3D.getSide(z, right));
			if (VO3D.getSide(z, right).equals("LEFT")) {// on the left actually
				return true; // need adjustment
			}
		} else {
			// System.out.println("the left is drawn! Need to draw right now.");
			// System.out.println(VO3D.getSide(z, right));
			if (VO3D.getSide(z, right).equals("RIGHT")) {// on the right
				// actually
				return true; // need adjustment
			}
		}

		return false;
	}

	// compute to which side of a line
	// the point p is at
	// it is in fact a 2D function
	// y value should be always 0
	private static String getSide(Point3D pp2, Point3D p0) {
		/*
		 * double x0 = p0.getX(); double z0 = p0.getZ(); double x = p.getX();
		 * double z = p.getZ();
		 * 
		 * double side = z0 * (x - x0) - x0 * (z - z0);
		 */

		double x0 = p0.getX();
		double y0 = p0.getZ();
		double x2 = pp2.getX();
		double y2 = pp2.getZ();
		double x1 = 0;
		double y1 = 0;
		double m = (x2 - x1) * (y2 - y1); // a rectangular
		double p1 = (x0 - x1) * (y0 - y1); // a second rectangular
		double p2 = (x2 - x0) * (y2 - y0); // and another
		double p3 = (x2 - x0) * (y0 - y1) * 2; // now two rectangulars

		if (m > p1 + p2 + p3) {
			return "RIGHT";
		} else if (m < p1 + p2 + p3) {
			return "LEFT";
		} else {
			return "ON";
		}
	}

}
