package mainpack;

import org.ejml.alg.dense.decomposition.DecompositionFactory;
import org.ejml.alg.dense.decomposition.EigenDecomposition;
import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import mygeom.VO3D;

public class Test {
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
	
	public static MatrixChains getOctahedralVertexChain() {
		MatrixHolder yRm = new YRotation(-Math.PI/3);
		MatrixHolder yRp = new YRotation(Math.PI/3);
		MatrixHolder T = new XZTranslation(Math.PI/3);
		
		MatrixChain chain1 = new MatrixChain (
				new MatrixHolder[] {
				new DihedralAngle(0),
				yRm, new DihedralAngle(1),
				T, yRp,	new DihedralAngle(5),
				T, yRp,	new DihedralAngle(9),
				yRm, new DihedralAngle(8),
				yRm, new DihedralAngle(10),
				T, yRp, new DihedralAngle(6),
				T, yRp, new DihedralAngle(2),
				yRm
		});
		MatrixChain chain2 = new MatrixChain (
				new MatrixHolder[] {
						new DihedralAngle(2),
						yRm, new DihedralAngle(5),
						yRm, new DihedralAngle(7),
						yRm, new DihedralAngle(6),
						yRm
				});
		MatrixChain chain3 = new MatrixChain (
				new MatrixHolder[] {
						new DihedralAngle(0),
						yRm, new DihedralAngle(1),
						yRm, new DihedralAngle(2),
						yRm, new DihedralAngle(3),
						yRm
				});
		MatrixChain chain4 = new MatrixChain (
				new MatrixHolder[] {
						new DihedralAngle(1),
						yRm, new DihedralAngle(7),
						yRm, new DihedralAngle(8),
						yRm, new DihedralAngle(4),
						yRm
				});
		MatrixChain chain5 = new MatrixChain (
				new MatrixHolder[] {
						new DihedralAngle(11),
						yRm, new DihedralAngle(10),
						yRm, new DihedralAngle(9),
						yRm, new DihedralAngle(8),
						yRm
				});
		MatrixChains togo2 = new MatrixChains(new MatrixChain[] {chain1, chain2, chain3, chain4, chain5});
		return togo2;
	}
	
	public static void main(String[] args) {
		//MatrixChains chain = getTetrahedralVertexChain();
		//double angle1 = Math.acos(1.0/3.0);
		//double angle1 = Math.PI/4;
		//double [] angles = {0.8, 2.0, 1.0};
		double ao = Math.PI - Math.acos(1.0 / 3.0) + 0.1;
		double[] angles = {ao, ao, ao, ao,  ao, ao, ao, ao, ao, ao, ao, ao};
		MatrixChains chain = getOctahedralVertexChain();
		
		System.out.println(f(chain, angles));
		System.out.println();
		
		double dby1 = dfby(chain, angles, 0);
		System.out.println(dby1);
		
		System.out.println(numDiff(chain, angles, 0, 1e-6));
		System.out.println();
		
		double dby11 = df2by(chain, angles, 0, 0);
		System.out.println(dby11);
		
		System.out.println(numDiff(chain, angles, 0, 0, 1e-5));
		for (int i = 0; i < 500; ++ i) {
			boolean printStep = ((i + 1)%100) == 0;
			angles = nstep(chain, angles, printStep);
		}

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

			System.out.println(f(chain, newAng));
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
		DenseMatrix64F mul = vector(l);

		boolean solved = CommonOps.solve(J, grad, mul);
		 
		if (!solved) {
			throw new RuntimeException("Failed to solve update equation");
		}

		if (printStep) {
			EigenDecomposition<DenseMatrix64F> decomp = DecompositionFactory.eigSymm(l, false);
			boolean posed = decomp.decompose(J);
			if (!posed) {
				throw new RuntimeException("Failed to decompose matrix");
			}
			double[] eigs = eigs(decomp);
			System.out.println("Computed eigenvalues " + printVec(eigs));
		}

		DenseMatrix64F angv = vector(l);
		angv.data = angles;

		DenseMatrix64F newAng = vector(l);
		CommonOps.sub(angv, mul, newAng);

		return newAng.data;
	};
	
	// temporary matrix multiplication function until we get a proper matrix library
	public static double[] matrix_mult(double[][] a, double[] p) {
		double[] result = new double[3];
		result[0] = a[0][0]*p[0] + a[0][1]*p[1] + a[0][2]*p[2];
		result[1] = a[1][0]*p[0] + a[1][1]*p[1] + a[1][2]*p[2];
		result[2] = a[2][0]*p[0] + a[2][1]*p[1] + a[2][2]*p[2];
		
		return result;
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
				int di = ((DihedralAngle)hi).edgeindex;
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
				matrix = VO3D.getMatrix_rotateZ(angle);
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
