package mainpack;

import org.ejml.alg.dense.decomposition.DecompositionFactory;
import org.ejml.alg.dense.decomposition.EigenDecomposition;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import mygeom.VO3D;

public class Newton {	
	public static MatrixChains getTetrahedralVertexChain() {
		MatrixChain togo = new MatrixChain(6);
		togo.matrices[0] = new DihedralAngle(0);
		togo.matrices[2] = new DihedralAngle(1);
		togo.matrices[4] = new DihedralAngle(2);
		togo.matrices[1] = togo.matrices[3] = togo.matrices[5] = new YRotation(-Math.PI/3);
		togo.indexAngles();
		MatrixChains togo2 = new MatrixChains(new MatrixChain[] {togo});
		return togo2;
	}
	
	public static String printVec(double[] vec) {
		String togo = "";
		for (int i = 0; i < vec.length - 1; ++ i) {
			togo += (vec[i] + ", "); 
		}
		togo += (vec[vec.length - 1]);
		return togo;
	}
	
	public static double[] nstep(MatrixChains chain, double[] angles, boolean printStep) {
		double[] newAng = NiuDunLaFuSunStep(chain, angles, printStep);
		if (printStep) {
			System.out.println("Stepped to " + printVec(newAng));

			System.out.println("f: " + f(chain, newAng));
			System.out.println();
		}
		return newAng;
	}
	
	public static DenseMatrix64F vector(int size) {
		return new DenseMatrix64F(size, 1);
	}
	
	public static double[] eigs(EigenDecomposition<DenseMatrix64F> decomp) {
		double[] togo = new double[decomp.getNumberOfEigenvalues()];
		for (int i = 0; i < togo.length; ++ i) {
			togo[i] = decomp.getEigenvalue(i).real;
		}
		return togo;
	}
	
	public static double[] NiuDunLaFuSunStep(MatrixChains chain, double[] angles, boolean printStep) {
		// see: http://code.google.com/p/efficient-java-matrix-library/wiki/SolvingLinearSystems
		int l = angles.length;
		DenseMatrix64F J = get2d(chain, angles);
		
		DenseMatrix64F grad = get1d(chain, angles);
		DenseMatrix64F propose = vector(l);

		boolean solved = CommonOps.solve(J, grad, propose);
		 
		if (!solved) {
			throw new RuntimeException("Failed to solve update equation");
		}
		CommonOps.scale(-1, propose);

		if (printStep) {
			EigenDecomposition<DenseMatrix64F> decomp = DecompositionFactory.eigSymm(l, false);
			boolean posed = decomp.decompose(J);
			if (!posed) {
				throw new RuntimeException("Failed to decompose matrix");
			}
			double[] eigs = eigs(decomp);
			System.out.println("Computed eigenvalues " + printVec(eigs));
		}

		DenseMatrix64F angv = vec(angles);

		DenseMatrix64F newAng = vector(l);
		CommonOps.add(angv, propose, newAng);
		int i = 0;
		for (i = 0; i < 10; ++ i) {
		    boolean verify = verifyProposal(chain, angles, newAng.data, grad, propose);
		    if (verify) {
		    	break;
		    }
		    else {
		    	CommonOps.scale(-0.5, propose);
		    	System.out.println("Halved proposal length to " + Math.sqrt(dot(propose.data, propose.data)));
				CommonOps.add(angv, propose, newAng);
		    }
		}
		if (i == 10) {
			throw new RuntimeException("Failed to find acceptable step after 10 tries");
		}
		return newAng.data;
	};
	
	public static DenseMatrix64F vec(double[] vec) {
		DenseMatrix64F togo = vector(vec.length);
		togo.data = vec;
		return togo;
	}
	
	public static double dot(double[] vec1, double[] vec2) {
		double togo = 0.0;
		for (int i = 0; i < vec1.length; ++ i) {
			togo += vec1[i] * vec2[i];
		}
		return togo;
	}
	
	private static boolean verifyProposal(MatrixChains chain, double[] angles,
			double[] newAng, DenseMatrix64F grad, DenseMatrix64F propose) {
		for (int i = 0; i < newAng.length; ++ i) {
			if (newAng[i] <= 0 || newAng[i] >= 2 * Math.PI) {
				System.out.println("Rejected out of range angle " + newAng[i] + " at index i");
				return false;
			}
		}
		double descent = dot(grad.data, propose.data);
		if (descent > 0) {
			//throw new RuntimeException("Non-reducing gradient direction with slope " + descent);
		}
		double oldF = f(chain, angles);
		double newF = f(chain, newAng);
		if (newF > oldF + 1e-4 * descent) {
			System.out.println("Rejected failure of descent step to " + newF);
			return false;
		}
		DenseMatrix64F newGrad = get1d(chain, newAng);
		// These would both be negative numbers reflecting correct gradient direction
		double newDescent = dot(newGrad.data, propose.data);
	//	if (newDescent < 0.9 * descent) {
	//		System.out.println("Rejected failure of gradient reduce step from " + 
	//	         descent + " to " + newDescent);
	//		return false;
	//	}
		
		return true;
	}

	
	// Produce 1st derivative of objective function at specified angle setting
	public static DenseMatrix64F get1d(MatrixChains chain, double[] angles) {
		int l = angles.length;
		DenseMatrix64F togo = vector(l);
		for (int i = 0; i < l; ++ i) {
			togo.data[i] = dfby(chain, angles, i);
		}
		return togo;
	}
	
	// Produce 2nd derivative of objective function at specified angle setting
	public static DenseMatrix64F get2d(MatrixChains chain, double[] angles) {
		int dim = angles.length;
		DenseMatrix64F togo = new DenseMatrix64F(dim, dim);
		for (int i = 0; i < dim; ++ i) {
			for (int j = 0; j < dim; ++ j) {
				togo.set(i, j, df2by(chain, angles, i, j));
			}
		}
		return togo;
	}
	// A utility function for numerical evaluation of derivatives - produce version of angles vector perturbed by
	// supplied epsilon at the supplied index
	public static double[] perturbAngles(double[] angles, int index, double epsilon) {
		double[] newAngles = new double[angles.length];
		for (int i = 0; i < angles.length; ++ i) {
	        newAngles[i] = angles[i] + (i == index? epsilon : 0);
		}
		return newAngles;
	}
	// numerically compute second derivative of f with respect to 2 different indices for comparison purposes
	public static double numDiff(MatrixChains chain, double[] angles, int index1, int index2, double epsilon) {
		double[] newAngles1f = perturbAngles(angles, index1, epsilon);
		double[] newAngles1b = perturbAngles(angles, index1, -epsilon);
		
		double f1f = numDiff(chain, newAngles1f, index2, epsilon);
		double f1b = numDiff(chain, newAngles1b, index2, -epsilon);

		double f2 = (f1f - f1b)/ (2 * epsilon);
		return f2;
	} 
	
	public static double numDiff(MatrixChains chain, double[] angles, int index, double epsilon) {	
		double[] newAngles1 = perturbAngles(angles, index, -epsilon);
		double[] newAngles2 = perturbAngles(angles, index, epsilon);
		double f1 = f(chain, newAngles1);
		double f2 = f(chain, newAngles2);
		return (f2 - f1) / (2 * epsilon);
	}
	
	// construct the transformation matrix chain, possibly up to 2nd derivative order - set indices
	// to -1 for unused derivative slots
	public static double[][][] getMatrices(MatrixChain chain, double[] angles, int smashindex1, int smashindex2) {
		int l = chain.matrices.length;
		double[][][] mat = new double[l][][];
		for (int i = 0; i < l; ++i) {
			MatrixHolder hi = chain.matrices[i];
			double[][] matrix = null;
			if (hi instanceof DihedralAngle) {
				DihedralAngle da = (DihedralAngle) hi;
				int di = da.edgeindex;
				boolean smashoff = false;
				double angle;
				if (di == smashindex1 && di == smashindex2) {
					angle = -angles[di];
					smashoff = true;
				} else if (di == smashindex1 || di == smashindex2) {
					angle = Math.PI / 2.0 - angles[di];
					smashoff = true;
				} else {
					angle = Math.PI - angles[di];
				}
				matrix = VO3D.getMatrix_rotateZ(da.sign * angle);
				if (smashoff) {
					matrix[2][2] = matrix[3][3] = 0.0;
				}
				++ di;
			}
			else {
				matrix = hi.matrix;
			}
			mat[i] = matrix;
		}
		return mat;
	}
	
	public static double[][] F(MatrixChain chain, double[] angles){
        double[][][] mat = getMatrices(chain, angles, -1, -1);
		return multiplyMatrices(mat);
	}
	
	// computes F-I for an angle set
	public static double[][] Fremainder(MatrixChain chain, double[] angles) {
		double [][] result = F(chain, angles);
		double [][] remainder = VO3D.matrixSubtract(result, VO3D.getIdentityMatrix44());
		return remainder;
	}
	
	public static double[][] multiplyMatrices(double [][][] mat){
		double [][] product = VO3D.getIdentityMatrix44();
		for(int i=0; i<mat.length; i++){
			product = VO3D.matrixMultiply(product, mat[i]);
		}
		
		return product;
	}
	
	public static double[][] getProduct(MatrixChain chain, double[] angles, int index1, int index2) {
		double[][][] mats = getMatrices(chain, angles, index1, index2);
		double[][] mat = multiplyMatrices(mats);
		return mat;
	}
	
	public static double dfby1(MatrixChain chain, double[] angles, int index) {
		double[][] remainder = Fremainder(chain, angles);
		double[][] dF = getProduct(chain, angles, index, -1);
		return 2* VO3D.sumProduct(remainder, dF);
	}
	
	public static double dfby(MatrixChains chains, double[] angles, int index) {
		double v = 0;
		for (int i = 0; i < chains.chains.length; ++ i) {
			v += dfby1(chains.chains[i], angles, index);
		}
		return v;
	}
	
	public static double df2by1(MatrixChain chain, double[] angles, int index1, int index2) {
		double[][] remainder = Fremainder(chain, angles);
		double[][] dF1 = getProduct(chain, angles, index1, -1);
		double[][] dF2 = getProduct(chain, angles, index2, -1);
		double[][] d2F = getProduct(chain, angles, index1, index2);
		
		return 2* (VO3D.sumProduct(d2F, remainder) + VO3D.sumProduct(dF1, dF2));
	}
	
	public static double df2by(MatrixChains chains, double[] angles, int index1, int index2) {
		double v = 0;
		for (int i = 0; i < chains.chains.length; ++ i) {
			v += df2by1(chains.chains[i], angles, index1, index2);
		}
		return v;
	}
	
	public static double f1(MatrixChain chain, double[] angles) {
		double[][] remainder = Fremainder(chain, angles);
		return VO3D.sumProduct(remainder, remainder);
	}
	
	public static double f(MatrixChains chains, double[] angles) {
		double v = 0;
		for (int i = 0; i < chains.chains.length; ++ i) {
			v += f1(chains.chains[i], angles);
		}
		return v;
	}
}
