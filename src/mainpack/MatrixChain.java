package mainpack;

import java.util.ArrayList;
import java.util.List;

public class MatrixChain {
	public MatrixChain(int size) {
		matrices = new MatrixHolder[size];
	}
	public MatrixChain(MatrixHolder[] matrices) {
		this.matrices = matrices;
	}
	public MatrixHolder[] matrices;
	public int[] angles;
	public void indexAngles() {
		List<Integer> ints = new ArrayList<Integer>();
		for (int i = 0; i < matrices.length; ++ i) {
			if (matrices[i] instanceof DihedralAngle) {
				ints.add(i);
			}
		}
		angles = new int[ints.size()];
		for (int i = 0; i < angles.length; ++ i) {
			angles[i] = ints.get(i);
		}
	}
}
