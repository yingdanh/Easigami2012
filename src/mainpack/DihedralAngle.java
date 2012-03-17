package mainpack;

public class DihedralAngle extends MatrixHolder {
	public DihedralAngle(int edgeindex) {
		this.edgeindex = edgeindex;
	}
	public int edgeindex;
	public double angle;
	public double sign;
}
