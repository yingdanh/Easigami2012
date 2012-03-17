package mainpack;

public class MatrixChains {
	public MatrixChain[] chains;
	
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
