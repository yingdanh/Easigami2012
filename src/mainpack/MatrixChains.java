package mainpack;

public class MatrixChains {
	public MatrixChain[] chains;
	double tolerance = 1e-7;
	double[] initialPoint;
	double regularise = 0.0;
	
	public MatrixChains(MatrixChain[] chains) {
		this.chains = chains;
	}
	
	public MatrixChains(int size){
		this.chains = new MatrixChain[size];
	}
	
	public void setValueAt(int index, MatrixChain mc){
		chains[index] = mc;
	}
	
	public int getLength(){
		return chains.length;
	}
}
