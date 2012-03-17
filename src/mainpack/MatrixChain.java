package mainpack;

import java.util.ArrayList;
import java.util.List;

import mygeom.VO3D;

public class MatrixChain {
	public MatrixChain(int size) {
		count = 0;
		matrices = new MatrixHolder[size];
	}
	
	public MatrixChain(MatrixHolder[] matrices) {
		count = 0;
		this.matrices = matrices;
	}
	
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
	
	public void append(double[][] m){
		MatrixHolder mh = new MatrixHolder();
		mh.matrix = m;
		matrices[count] = mh;
		count++;
	}
	
	public void append(int edgeIndex, double sign, double angle, double[][] m){
		DihedralAngle da = new DihedralAngle(edgeIndex);
		da.sign = sign;
		da.matrix = m;
		da.angle = angle;
		matrices[count] = da;
		count++;
	}
	
	public void printChain(){
		for(int i=0; i<matrices.length; i++){
			System.out.println(i);
			VO3D.printMatrix44(matrices[i].matrix);
			System.out.println();
		}
	}
	
	public MatrixHolder[] matrices;
	public int[] angles;
	private int count;
}
