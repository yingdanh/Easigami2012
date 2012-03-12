package mainpack;

import mygeom.VO3D;

public class YRotation extends MatrixHolder {
	public YRotation(double angle) {
		matrix = VO3D.getMatrix_rotateY(angle);
	}
}
