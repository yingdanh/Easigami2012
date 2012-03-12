package mainpack;

import mygeom.VO3D;

public class XZTranslation extends MatrixHolder {
	public XZTranslation(double zangle) {
		matrix = VO3D.getMatrix_translation(-Math.sin(zangle), 0, Math.cos(zangle));
	}
}
