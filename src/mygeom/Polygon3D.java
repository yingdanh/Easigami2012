package mygeom;

import mygeom.Point3D;

public class Polygon3D {
	public static final double len = 10.0; // 10CM

	protected int N;
	protected Point3D[] vertices3D;
	protected Line3D[] edges;
	protected Point3D[] absVerts3D;
	protected Point3D absNormal;
	private boolean isDebug = false;

	protected final float CMAX = 255.0f;
	protected float[] front_color3f;
	protected float[] back_color3f;

	public Polygon3D(int n) {
		this.N = n;
		vertices3D = new Point3D[N];
		edges = new Line3D[n];
		absVerts3D = new Point3D[N];

		// color
		front_color3f = new float[3];
		back_color3f = new float[3];
	}

	// Getters
	// get the # of vertices of a polygon
	public int getN() {
		return N;
	}

	public Point3D[] getVertices() {
		return vertices3D;
	}

	public Point3D getVertexAt(int index) {
		return vertices3D[index];
	}

	public Line3D[] getEdges() {
		return edges;
	}

	public Line3D getEdgeAt(int index) {
		return edges[index];
	}
	
	public Point3D[] getAbsVerts(){
		return absVerts3D;
	}
	
	public Point3D getAbsVertexAt(int index){
		return absVerts3D[index];
	}

	public float[] getFrontColor() {
		return front_color3f;
	}

	public float[] getBackColor() {
		return back_color3f;
	}

	/*
	 * public Point3D getRefPoint2D(){ return refp2d; }
	 */

	// Setters
	public void setVertexAt(int index, Point3D p) {
		vertices3D[index] = p;
	}

	public void setEdgeAt(int index, Point3D p1, Point3D p2) {
		edges[index] = new Line3D(p1, p2);
	}
	
	public void setAbsVertexAt(int index, Point3D p){
		absVerts3D[index] = p;
	}

	/*
	 * return a vertex which is not on the connected edge
	 */
	public Point3D setOppIsosceles(int e_index, boolean isLeft) {
		if (isDebug)
			System.out.println("Polygon3D::setOppIsosceles");
		Point3D[] ps = new Point3D[3];
		double hhypo = len * Math.sqrt(2) / 2;
		double hlen = len / 2;

		if (isLeft) {
			switch (e_index) {
			case 0:
				ps[0] = new Point3D(0, 0, hlen);
				ps[1] = new Point3D(-len, 0, hlen);
				ps[2] = new Point3D(0, 0, -hlen);
				break;
			case 1:
				ps[0] = new Point3D(0, 0, -hlen);
				ps[1] = new Point3D(0, 0, hlen);
				ps[2] = new Point3D(-len, 0, -hlen);
				break;
			case 2:
				ps[0] = new Point3D(-hhypo, 0, 0);
				ps[1] = new Point3D(0, 0, -hhypo);
				ps[2] = new Point3D(0, 0, hhypo);
				break;
			default:
				System.out.println("Edge index error!");
				break;
			}
		} else {// isRight
			switch (e_index) {
			case 0:
				ps[0] = new Point3D(0, 0, -hlen);
				ps[1] = new Point3D(len, 0, -hlen);
				ps[2] = new Point3D(0, 0, hlen);
				break;
			case 1:
				ps[0] = new Point3D(0, 0, hlen);
				ps[1] = new Point3D(0, 0, -hlen);
				ps[2] = new Point3D(len, 0, hlen);
				break;
			case 2:
				ps[0] = new Point3D(hhypo, 0, 0);
				ps[1] = new Point3D(0, 0, hhypo);
				ps[2] = new Point3D(0, 0, -hhypo);
				break;
			default:
				System.out.println("Edge index error!");
				break;
			}
		}

		return ps[(e_index + 1) % this.getN()];
	}

	public void setIsosceles(int e_index, boolean isLeft) {
		if (isDebug)
			System.out.println("Polygon3D::setIsosceles");
		Point3D[] ps = new Point3D[3];
		double hhypo = len * Math.sqrt(2) / 2;
		double hlen = len / 2;

		if (isLeft) {
			switch (e_index) {
			case 0:
				ps[0] = new Point3D(0, 0, hlen);
				ps[1] = new Point3D(-len, 0, hlen);
				ps[2] = new Point3D(0, 0, -hlen);
				break;
			case 1:
				ps[0] = new Point3D(0, 0, -hlen);
				ps[1] = new Point3D(0, 0, hlen);
				ps[2] = new Point3D(-len, 0, -hlen);
				break;
			case 2:
				ps[0] = new Point3D(-hhypo, 0, 0);
				ps[1] = new Point3D(0, 0, -hhypo);
				ps[2] = new Point3D(0, 0, hhypo);
				break;
			default:
				System.out.println("Edge index error!");
				break;
			}
		} else {// isRight
			switch (e_index) {
			case 0:
				ps[0] = new Point3D(0, 0, -hlen);
				ps[1] = new Point3D(len, 0, -hlen);
				ps[2] = new Point3D(0, 0, hlen);
				break;
			case 1:
				ps[0] = new Point3D(0, 0, hlen);
				ps[1] = new Point3D(0, 0, -hlen);
				ps[2] = new Point3D(len, 0, hlen);
				break;
			case 2:
				ps[0] = new Point3D(hhypo, 0, 0);
				ps[1] = new Point3D(0, 0, hhypo);
				ps[2] = new Point3D(0, 0, -hhypo);
				break;
			default:
				System.out.println("Edge index error!");
				break;
			}
		}

		setVertexAt(0, ps[0]);
		setVertexAt(1, ps[1]);
		setVertexAt(2, ps[2]);
		setEdgeAt(0, ps[2], ps[0]);
		setEdgeAt(1, ps[0], ps[1]);
		setEdgeAt(2, ps[1], ps[2]);
	}

	public Point3D setOppEuilateral(int e_index, boolean isLeft) {
		if (isDebug)
			System.out.println("Polygon3D::setOppEuilateral");
		Point3D[] ps = new Point3D[3];
		double hlen = len / 2;
		double h = Math.sqrt(3) * len / 2.0;

		if (isLeft) {
			switch (e_index) {
			case 0:
				ps[0] = new Point3D(0, 0, hlen);
				ps[1] = new Point3D(-h, 0, 0);
				ps[2] = new Point3D(0, 0, -hlen);
				break;
			case 1:
				ps[0] = new Point3D(0, 0, -hlen);
				ps[1] = new Point3D(0, 0, hlen);
				ps[2] = new Point3D(-h, 0, 0);
				break;
			case 2:
				ps[0] = new Point3D(-h, 0, 0);
				ps[1] = new Point3D(0, 0, -hlen);
				ps[2] = new Point3D(0, 0, hlen);
				break;
			default:
				System.out.println("Edge index error!");
				break;
			}
		} else {
			switch (e_index) {
			case 0:
				ps[0] = new Point3D(0, 0, -hlen);
				ps[1] = new Point3D(h, 0, 0);
				ps[2] = new Point3D(0, 0, hlen);
				break;
			case 1:
				ps[0] = new Point3D(0, 0, hlen);
				ps[1] = new Point3D(0, 0, -hlen);
				ps[2] = new Point3D(h, 0, 0);
				break;
			case 2:
				ps[0] = new Point3D(h, 0, 0);
				ps[1] = new Point3D(0, 0, hlen);
				ps[2] = new Point3D(0, 0, -hlen);
				break;
			default:
				System.out.println("Edge index error!");
				break;
			}
		}
		
		return ps[(e_index + 1) % this.getN()];
	}

	public void setEuilateral(int e_index, boolean isLeft) {
		if (isDebug)
			System.out.println("Polygon3D::setEuilateral");
		Point3D[] ps = new Point3D[3];
		double hlen = len / 2;
		double h = Math.sqrt(3) * len / 2.0;

		if (isLeft) {
			switch (e_index) {
			case 0:
				ps[0] = new Point3D(0, 0, hlen);
				ps[1] = new Point3D(-h, 0, 0);
				ps[2] = new Point3D(0, 0, -hlen);
				break;
			case 1:
				ps[0] = new Point3D(0, 0, -hlen);
				ps[1] = new Point3D(0, 0, hlen);
				ps[2] = new Point3D(-h, 0, 0);
				break;
			case 2:
				ps[0] = new Point3D(-h, 0, 0);
				ps[1] = new Point3D(0, 0, -hlen);
				ps[2] = new Point3D(0, 0, hlen);
				break;
			default:
				System.out.println("Edge index error!");
				break;
			}
		} else {
			switch (e_index) {
			case 0:
				ps[0] = new Point3D(0, 0, -hlen);
				ps[1] = new Point3D(h, 0, 0);
				ps[2] = new Point3D(0, 0, hlen);
				break;
			case 1:
				ps[0] = new Point3D(0, 0, hlen);
				ps[1] = new Point3D(0, 0, -hlen);
				ps[2] = new Point3D(h, 0, 0);
				break;
			case 2:
				ps[0] = new Point3D(h, 0, 0);
				ps[1] = new Point3D(0, 0, hlen);
				ps[2] = new Point3D(0, 0, -hlen);
				break;
			default:
				System.out.println("Edge index error!");
				break;
			}
		}

		setVertexAt(0, ps[0]);
		setVertexAt(1, ps[1]);
		setVertexAt(2, ps[2]);
		setEdgeAt(0, ps[2], ps[0]);
		setEdgeAt(1, ps[0], ps[1]);
		setEdgeAt(2, ps[1], ps[2]);
	}

	public Point3D setOppSquare(int e_index, boolean isLeft) {
		if (isDebug)
			System.out.println("Polygon3D::setSquare");

		Point3D[] ps = new Point3D[4];
		double hlen = len / 2;

		if (isLeft) {
			switch (e_index) {
			case 0:		
				ps[0] = new Point3D(0, 0, hlen);
				ps[1] = new Point3D(-len, 0, hlen);
				ps[2] = new Point3D(-len, 0, -hlen);
				ps[3] = new Point3D(0, 0, -hlen);
				break;
			case 1:
				ps[0] = new Point3D(0, 0, -hlen);
				ps[1] = new Point3D(0, 0, hlen);
				ps[2] = new Point3D(-len, 0, hlen);
				ps[3] = new Point3D(-len, 0, -hlen);
				break;
			case 2:
				ps[0] = new Point3D(-len, 0, -hlen);
				ps[1] = new Point3D(0, 0, -hlen);
				ps[2] = new Point3D(0, 0, hlen);
				ps[3] = new Point3D(-len, 0, hlen);
				break;
			case 3:
				ps[0] = new Point3D(-len, 0, hlen);
				ps[1] = new Point3D(-len, 0, -hlen);
				ps[2] = new Point3D(0, 0, -hlen);
				ps[3] = new Point3D(0, 0, hlen);
				break;
			default:
				System.out.println("Edge index error!");
				break;
			}
		} else {
			switch (e_index) {
			case 0:
				ps[0] = new Point3D(0, 0, -hlen);
				ps[1] = new Point3D(len, 0, -hlen);
				ps[2] = new Point3D(len, 0, hlen);
				ps[3] = new Point3D(0, 0, hlen);
				break;
			case 1:
				ps[0] = new Point3D(0, 0, hlen);
				ps[1] = new Point3D(0, 0, -hlen);
				ps[2] = new Point3D(len, 0, -hlen);
				ps[3] = new Point3D(len, 0, hlen);
				break;
			case 2:
				ps[0] = new Point3D(len, 0, hlen);
				ps[1] = new Point3D(0, 0, hlen);
				ps[2] = new Point3D(0, 0, -hlen);
				ps[3] = new Point3D(len, 0, -hlen);
				break;
			case 3:
				ps[0] = new Point3D(len, 0, -hlen);
				ps[1] = new Point3D(len, 0, hlen);
				ps[2] = new Point3D(0, 0, hlen);
				ps[3] = new Point3D(0, 0, -hlen);
				break;
			default:
				System.out.println("Edge index error!");
				break;
			}
		}

		return ps[(e_index + 1) % this.getN()];
	}

	public void setSquare(int e_index, boolean isLeft) {
		if (isDebug)
			System.out.println("Polygon3D::setSquare");

		Point3D[] ps = new Point3D[4];
		double hlen = len / 2;

		if (isLeft) {
			switch (e_index) {
			case 0:		
				ps[0] = new Point3D(0, 0, hlen);
				ps[1] = new Point3D(-len, 0, hlen);
				ps[2] = new Point3D(-len, 0, -hlen);
				ps[3] = new Point3D(0, 0, -hlen);
				break;
			case 1:
				ps[0] = new Point3D(0, 0, -hlen);
				ps[1] = new Point3D(0, 0, hlen);
				ps[2] = new Point3D(-len, 0, hlen);
				ps[3] = new Point3D(-len, 0, -hlen);
				break;
			case 2:
				ps[0] = new Point3D(-len, 0, -hlen);
				ps[1] = new Point3D(0, 0, -hlen);
				ps[2] = new Point3D(0, 0, hlen);
				ps[3] = new Point3D(-len, 0, hlen);
				break;
			case 3:
				ps[0] = new Point3D(-len, 0, hlen);
				ps[1] = new Point3D(-len, 0, -hlen);
				ps[2] = new Point3D(0, 0, -hlen);
				ps[3] = new Point3D(0, 0, hlen);
				break;
			default:
				System.out.println("Edge index error!");
				break;
			}
		} else {
			switch (e_index) {
			case 0:
				ps[0] = new Point3D(0, 0, -hlen);
				ps[1] = new Point3D(len, 0, -hlen);
				ps[2] = new Point3D(len, 0, hlen);
				ps[3] = new Point3D(0, 0, hlen);
				break;
			case 1:
				ps[0] = new Point3D(0, 0, hlen);
				ps[1] = new Point3D(0, 0, -hlen);
				ps[2] = new Point3D(len, 0, -hlen);
				ps[3] = new Point3D(len, 0, hlen);
				break;
			case 2:
				ps[0] = new Point3D(len, 0, hlen);
				ps[1] = new Point3D(0, 0, hlen);
				ps[2] = new Point3D(0, 0, -hlen);
				ps[3] = new Point3D(len, 0, -hlen);
				break;
			case 3:
				ps[0] = new Point3D(len, 0, -hlen);
				ps[1] = new Point3D(len, 0, hlen);
				ps[2] = new Point3D(0, 0, hlen);
				ps[3] = new Point3D(0, 0, -hlen);
				break;
			default:
				System.out.println("Edge index error!");
				break;
			}
		}

		setVertexAt(0, ps[0]);
		setVertexAt(1, ps[1]);
		setVertexAt(2, ps[2]);
		setVertexAt(3, ps[3]);
		setEdgeAt(0, ps[3], ps[0]);
		setEdgeAt(1, ps[0], ps[1]);
		setEdgeAt(2, ps[1], ps[2]);
		setEdgeAt(3, ps[2], ps[3]);
	}

	public Point3D setOppPentagon(int e_index, boolean isLeft) {
		if (isDebug)
			System.out.println("Polygon3D::setPentagon");
		
		Point3D[] ps = new Point3D[5];
		double hlen = len / 2;
		double s1 = len * Math.sin(Math.toRadians(72));
		double s2 = len * Math.cos(Math.toRadians(72));
		double s3 = len * Math.sin(Math.toRadians(36));

		if (isLeft) {
			switch (e_index) {
			case 0:
				ps[4] = new Point3D(0, 0, -hlen);
				ps[3] = new Point3D(-s1, 0, -hlen - s2);
				ps[2] = new Point3D(-s1 - s3, 0, 0);
				ps[1] = new Point3D(-s1, 0, hlen + s2);
				ps[0] = new Point3D(0, 0, hlen);
				break;
			case 1:
				ps[1] = new Point3D(0, 0, hlen);
				ps[0] = new Point3D(0, 0, -hlen);
				ps[4] = new Point3D(-s1, 0, -hlen - s2);
				ps[3] = new Point3D(-s1 - s3, 0, 0);
				ps[2] = new Point3D(-s1, 0, hlen + s2);
				break;
			case 2:
				ps[3] = new Point3D(-s1, 0, hlen + s2);
				ps[2] = new Point3D(0, 0, hlen);
				ps[1] = new Point3D(0, 0, -hlen);
				ps[0] = new Point3D(-s1, 0, -hlen - s2);
				ps[4] = new Point3D(-s1 - s3, 0, 0);
				break;
			case 3:
				ps[0] = new Point3D(-s1 - s3, 0, 0);
				ps[4] = new Point3D(-s1, 0, hlen + s2);
				ps[3] = new Point3D(0, 0, hlen);
				ps[2] = new Point3D(0, 0, -hlen);
				ps[1] = new Point3D(-s1, 0, -hlen - s2);
				break;
			case 4:
				ps[2] = new Point3D(-s1, 0, -hlen - s2);
				ps[1] = new Point3D(-s1 - s3, 0, 0);
				ps[0] = new Point3D(-s1, 0, hlen + s2);
				ps[4] = new Point3D(0, 0, hlen);
				ps[3] = new Point3D(0, 0, -hlen);
				break;
			default:
				System.out.println("Edge index error!");
				break;
			}
		} else {
			switch (e_index) {
			case 0:
				ps[4] = new Point3D(0, 0, hlen);
				ps[3] = new Point3D(s1, 0, hlen + s2);
				ps[2] = new Point3D(s1 + s3, 0, 0);
				ps[1] = new Point3D(s1, 0, -hlen - s2);
				ps[0] = new Point3D(0, 0, -hlen);
				break;
			case 1:
				ps[1] = new Point3D(0, 0, -hlen);
				ps[0] = new Point3D(0, 0, hlen);
				ps[4] = new Point3D(s1, 0, hlen + s2);
				ps[3] = new Point3D(s1 + s3, 0, 0);
				ps[2] = new Point3D(s1, 0, -hlen - s2);
				break;
			case 2:
				ps[3] = new Point3D(s1, 0, -hlen - s2);
				ps[2] = new Point3D(0, 0, -hlen);
				ps[1] = new Point3D(0, 0, hlen);
				ps[0] = new Point3D(s1, 0, hlen + s2);
				ps[4] = new Point3D(s1 + s3, 0, 0);
				break;
			case 3:
				ps[0] = new Point3D(s1 + s3, 0, 0);
				ps[4] = new Point3D(s1, 0, -hlen - s2);
				ps[3] = new Point3D(0, 0, -hlen);
				ps[2] = new Point3D(0, 0, hlen);
				ps[1] = new Point3D(s1, 0, hlen + s2);
				break;
			case 4:
				ps[2] = new Point3D(s1, 0, hlen + s2);
				ps[1] = new Point3D(s1 + s3, 0, 0);
				ps[0] = new Point3D(s1, 0, -hlen - s2);
				ps[4] = new Point3D(0, 0, -hlen);
				ps[3] = new Point3D(0, 0, hlen);
				break;
			default:
				System.out.println("Edge index error!");
				break;
			}
		}

		return ps[(e_index + 1) % this.getN()];
	}

	public void setPentagon(int e_index, boolean isLeft) {
		if (isDebug)
			System.out.println("Polygon3D::setPentagon");
		
		Point3D[] ps = new Point3D[5];
		double hlen = len / 2;
		double s1 = len * Math.sin(Math.toRadians(72));
		double s2 = len * Math.cos(Math.toRadians(72));
		double s3 = len * Math.sin(Math.toRadians(36));

		if (isLeft) {
			switch (e_index) {
			case 0:
				ps[4] = new Point3D(0, 0, -hlen);
				ps[3] = new Point3D(-s1, 0, -hlen - s2);
				ps[2] = new Point3D(-s1 - s3, 0, 0);
				ps[1] = new Point3D(-s1, 0, hlen + s2);
				ps[0] = new Point3D(0, 0, hlen);
				break;
			case 1:
				ps[1] = new Point3D(0, 0, hlen);
				ps[0] = new Point3D(0, 0, -hlen);
				ps[4] = new Point3D(-s1, 0, -hlen - s2);
				ps[3] = new Point3D(-s1 - s3, 0, 0);
				ps[2] = new Point3D(-s1, 0, hlen + s2);
				break;
			case 2:
				ps[3] = new Point3D(-s1, 0, hlen + s2);
				ps[2] = new Point3D(0, 0, hlen);
				ps[1] = new Point3D(0, 0, -hlen);
				ps[0] = new Point3D(-s1, 0, -hlen - s2);
				ps[4] = new Point3D(-s1 - s3, 0, 0);
				break;
			case 3:
				ps[0] = new Point3D(-s1 - s3, 0, 0);
				ps[4] = new Point3D(-s1, 0, hlen + s2);
				ps[3] = new Point3D(0, 0, hlen);
				ps[2] = new Point3D(0, 0, -hlen);
				ps[1] = new Point3D(-s1, 0, -hlen - s2);
				break;
			case 4:
				ps[2] = new Point3D(-s1, 0, -hlen - s2);
				ps[1] = new Point3D(-s1 - s3, 0, 0);
				ps[0] = new Point3D(-s1, 0, hlen + s2);
				ps[4] = new Point3D(0, 0, hlen);
				ps[3] = new Point3D(0, 0, -hlen);
				break;
			default:
				System.out.println("Edge index error!");
				break;
			}
		} else {
			switch (e_index) {
			case 0:
				ps[4] = new Point3D(0, 0, hlen);
				ps[3] = new Point3D(s1, 0, hlen + s2);
				ps[2] = new Point3D(s1 + s3, 0, 0);
				ps[1] = new Point3D(s1, 0, -hlen - s2);
				ps[0] = new Point3D(0, 0, -hlen);
				break;
			case 1:
				ps[1] = new Point3D(0, 0, -hlen);
				ps[0] = new Point3D(0, 0, hlen);
				ps[4] = new Point3D(s1, 0, hlen + s2);
				ps[3] = new Point3D(s1 + s3, 0, 0);
				ps[2] = new Point3D(s1, 0, -hlen - s2);
				break;
			case 2:
				ps[3] = new Point3D(s1, 0, -hlen - s2);
				ps[2] = new Point3D(0, 0, -hlen);
				ps[1] = new Point3D(0, 0, hlen);
				ps[0] = new Point3D(s1, 0, hlen + s2);
				ps[4] = new Point3D(s1 + s3, 0, 0);
				break;
			case 3:
				ps[0] = new Point3D(s1 + s3, 0, 0);
				ps[4] = new Point3D(s1, 0, -hlen - s2);
				ps[3] = new Point3D(0, 0, -hlen);
				ps[2] = new Point3D(0, 0, hlen);
				ps[1] = new Point3D(s1, 0, hlen + s2);
				break;
			case 4:
				ps[2] = new Point3D(s1, 0, hlen + s2);
				ps[1] = new Point3D(s1 + s3, 0, 0);
				ps[0] = new Point3D(s1, 0, -hlen - s2);
				ps[4] = new Point3D(0, 0, -hlen);
				ps[3] = new Point3D(0, 0, hlen);
				break;
			default:
				System.out.println("Edge index error!");
				break;
			}
		}

		setVertexAt(0, ps[0]);
		setVertexAt(1, ps[1]);
		setVertexAt(2, ps[2]);
		setVertexAt(3, ps[3]);
		setVertexAt(4, ps[4]);
		setEdgeAt(0, ps[4], ps[0]);
		setEdgeAt(1, ps[0], ps[1]);
		setEdgeAt(2, ps[1], ps[2]);
		setEdgeAt(3, ps[2], ps[3]);
		setEdgeAt(4, ps[3], ps[4]);
	}

	public Point3D setOppHexagon(int e_index, boolean isLeft) {
		if (isDebug)
			System.out.println("Polygon3D::setHexagon");
		// Point3D p0 = null, p1 = null, p2 = null, p3=null, p4=null, p5=null;
		Point3D[] ps = new Point3D[6];
		double hlen = len / 2;
		double s1 = len * Math.sin(Math.toRadians(60));
		double s2 = len * Math.cos(Math.toRadians(60));
		/*
		 * Point3D[] psl = {new Point3D(0, 0, -hlen), new Point3D(-s1, 0,
		 * -hlen-s2), new Point3D(-s1*2, 0, -hlen), new Point3D(-s1*2, 0, hlen),
		 * new Point3D(-s1, 0, hlen+s2), new Point3D(0, 0, hlen)}; Point3D[] psr
		 * = {new Point3D(0, 0, hlen), new Point3D(s1, 0, hlen+s2), new
		 * Point3D(s1*2, 0, hlen), new Point3D(s1*2, 0, -hlen), new Point3D(s1,
		 * 0, -hlen-s2), new Point3D(0, 0, -hlen)};
		 */

		if (isLeft) {
			switch (e_index) {
			case 0:
				ps[0] = new Point3D(0, 0, -hlen);
				ps[1] = new Point3D(-s1, 0, -hlen - s2);
				ps[2] = new Point3D(-s1 * 2, 0, -hlen);
				ps[3] = new Point3D(-s1 * 2, 0, hlen);
				ps[4] = new Point3D(-s1, 0, hlen + s2);
				ps[5] = new Point3D(0, 0, hlen);
				break;
			case 1:
				ps[0] = new Point3D(0, 0, hlen);
				ps[1] = new Point3D(0, 0, -hlen);
				ps[2] = new Point3D(-s1, 0, -hlen - s2);
				ps[3] = new Point3D(-s1 * 2, 0, -hlen);
				ps[4] = new Point3D(-s1 * 2, 0, hlen);
				ps[5] = new Point3D(-s1, 0, hlen + s2);
				break;
			case 2:
				ps[0] = new Point3D(-s1, 0, hlen + s2);
				ps[1] = new Point3D(0, 0, hlen);
				ps[2] = new Point3D(0, 0, -hlen);
				ps[3] = new Point3D(-s1, 0, -hlen - s2);
				ps[4] = new Point3D(-s1 * 2, 0, -hlen);
				ps[5] = new Point3D(-s1 * 2, 0, hlen);
				break;
			case 3:
				ps[0] = new Point3D(-s1 * 2, 0, hlen);
				ps[1] = new Point3D(-s1, 0, hlen + s2);
				ps[2] = new Point3D(0, 0, hlen);
				ps[3] = new Point3D(0, 0, -hlen);
				ps[4] = new Point3D(-s1, 0, -hlen - s2);
				ps[5] = new Point3D(-s1 * 2, 0, -hlen);
				break;
			case 4:
				ps[0] = new Point3D(-s1 * 2, 0, -hlen);
				ps[1] = new Point3D(-s1 * 2, 0, hlen);
				ps[2] = new Point3D(-s1, 0, hlen + s2);
				ps[3] = new Point3D(0, 0, hlen);
				ps[4] = new Point3D(0, 0, -hlen);
				ps[5] = new Point3D(-s1, 0, -hlen - s2);
				break;
			case 5:
				ps[0] = new Point3D(-s1, 0, -hlen - s2);
				ps[1] = new Point3D(-s1 * 2, 0, -hlen);
				ps[2] = new Point3D(-s1 * 2, 0, hlen);
				ps[3] = new Point3D(-s1, 0, hlen + s2);
				ps[4] = new Point3D(0, 0, hlen);
				ps[5] = new Point3D(0, 0, -hlen);
				break;
			default:
				System.out.println("Edge index error!");
				break;
			}
		} else {
			switch (e_index) {
			case 0:
				ps[0] = new Point3D(0, 0, hlen);
				ps[1] = new Point3D(s1, 0, hlen + s2);
				ps[2] = new Point3D(s1 * 2, 0, hlen);
				ps[3] = new Point3D(s1 * 2, 0, -hlen);
				ps[4] = new Point3D(s1, 0, -hlen - s2);
				ps[5] = new Point3D(0, 0, -hlen);
				break;
			case 1:
				ps[0] = new Point3D(0, 0, -hlen);
				ps[1] = new Point3D(0, 0, hlen);
				ps[2] = new Point3D(s1, 0, hlen + s2);
				ps[3] = new Point3D(s1 * 2, 0, hlen);
				ps[4] = new Point3D(s1 * 2, 0, -hlen);
				ps[5] = new Point3D(s1, 0, -hlen - s2);
				break;
			case 2:
				ps[0] = new Point3D(s1, 0, -hlen - s2);
				ps[1] = new Point3D(0, 0, -hlen);
				ps[2] = new Point3D(0, 0, hlen);
				ps[3] = new Point3D(s1, 0, hlen + s2);
				ps[4] = new Point3D(s1 * 2, 0, hlen);
				ps[5] = new Point3D(s1 * 2, 0, -hlen);
				break;
			case 3:
				ps[0] = new Point3D(s1 * 2, 0, -hlen);
				ps[1] = new Point3D(s1, 0, -hlen - s2);
				ps[2] = new Point3D(0, 0, -hlen);
				ps[3] = new Point3D(0, 0, hlen);
				ps[4] = new Point3D(s1, 0, hlen + s2);
				ps[5] = new Point3D(s1 * 2, 0, hlen);
				break;
			case 4:
				ps[0] = new Point3D(s1 * 2, 0, hlen);
				ps[1] = new Point3D(s1 * 2, 0, -hlen);
				ps[2] = new Point3D(s1, 0, -hlen - s2);
				ps[3] = new Point3D(0, 0, -hlen);
				ps[4] = new Point3D(0, 0, hlen);
				ps[5] = new Point3D(s1, 0, hlen + s2);
				break;
			case 5:
				ps[0] = new Point3D(s1, 0, hlen + s2);
				ps[1] = new Point3D(s1 * 2, 0, hlen);
				ps[2] = new Point3D(s1 * 2, 0, -hlen);
				ps[3] = new Point3D(s1, 0, -hlen - s2);
				ps[4] = new Point3D(0, 0, -hlen);
				ps[5] = new Point3D(0, 0, hlen);
				break;
			default:
				System.out.println("Edge index error!");
				break;
			}
		}

		return ps[(e_index + 1) % this.getN()];
	}

	public void setHexagon(int e_index, boolean isLeft) {
		if (isDebug)
			System.out.println("Polygon3D::setHexagon");
		// Point3D p0 = null, p1 = null, p2 = null, p3=null, p4=null, p5=null;
		Point3D[] ps = new Point3D[6];
		double hlen = len / 2;
		double s1 = len * Math.sin(Math.toRadians(60));
		double s2 = len * Math.cos(Math.toRadians(60));
		/*
		 * Point3D[] psl = {new Point3D(0, 0, -hlen), new Point3D(-s1, 0,
		 * -hlen-s2), new Point3D(-s1*2, 0, -hlen), new Point3D(-s1*2, 0, hlen),
		 * new Point3D(-s1, 0, hlen+s2), new Point3D(0, 0, hlen)}; Point3D[] psr
		 * = {new Point3D(0, 0, hlen), new Point3D(s1, 0, hlen+s2), new
		 * Point3D(s1*2, 0, hlen), new Point3D(s1*2, 0, -hlen), new Point3D(s1,
		 * 0, -hlen-s2), new Point3D(0, 0, -hlen)};
		 */

		if (isLeft) {
			switch (e_index) {
			case 0:
				ps[0] = new Point3D(0, 0, -hlen);
				ps[1] = new Point3D(-s1, 0, -hlen - s2);
				ps[2] = new Point3D(-s1 * 2, 0, -hlen);
				ps[3] = new Point3D(-s1 * 2, 0, hlen);
				ps[4] = new Point3D(-s1, 0, hlen + s2);
				ps[5] = new Point3D(0, 0, hlen);
				break;
			case 1:
				ps[0] = new Point3D(0, 0, hlen);
				ps[1] = new Point3D(0, 0, -hlen);
				ps[2] = new Point3D(-s1, 0, -hlen - s2);
				ps[3] = new Point3D(-s1 * 2, 0, -hlen);
				ps[4] = new Point3D(-s1 * 2, 0, hlen);
				ps[5] = new Point3D(-s1, 0, hlen + s2);
				break;
			case 2:
				ps[0] = new Point3D(-s1, 0, hlen + s2);
				ps[1] = new Point3D(0, 0, hlen);
				ps[2] = new Point3D(0, 0, -hlen);
				ps[3] = new Point3D(-s1, 0, -hlen - s2);
				ps[4] = new Point3D(-s1 * 2, 0, -hlen);
				ps[5] = new Point3D(-s1 * 2, 0, hlen);
				break;
			case 3:
				ps[0] = new Point3D(-s1 * 2, 0, hlen);
				ps[1] = new Point3D(-s1, 0, hlen + s2);
				ps[2] = new Point3D(0, 0, hlen);
				ps[3] = new Point3D(0, 0, -hlen);
				ps[4] = new Point3D(-s1, 0, -hlen - s2);
				ps[5] = new Point3D(-s1 * 2, 0, -hlen);
				break;
			case 4:
				ps[0] = new Point3D(-s1 * 2, 0, -hlen);
				ps[1] = new Point3D(-s1 * 2, 0, hlen);
				ps[2] = new Point3D(-s1, 0, hlen + s2);
				ps[3] = new Point3D(0, 0, hlen);
				ps[4] = new Point3D(0, 0, -hlen);
				ps[5] = new Point3D(-s1, 0, -hlen - s2);
				break;
			case 5:
				ps[0] = new Point3D(-s1, 0, -hlen - s2);
				ps[1] = new Point3D(-s1 * 2, 0, -hlen);
				ps[2] = new Point3D(-s1 * 2, 0, hlen);
				ps[3] = new Point3D(-s1, 0, hlen + s2);
				ps[4] = new Point3D(0, 0, hlen);
				ps[5] = new Point3D(0, 0, -hlen);
				break;
			default:
				System.out.println("Edge index error!");
				break;
			}
		} else {
			switch (e_index) {
			case 0:
				ps[0] = new Point3D(0, 0, hlen);
				ps[1] = new Point3D(s1, 0, hlen + s2);
				ps[2] = new Point3D(s1 * 2, 0, hlen);
				ps[3] = new Point3D(s1 * 2, 0, -hlen);
				ps[4] = new Point3D(s1, 0, -hlen - s2);
				ps[5] = new Point3D(0, 0, -hlen);
				break;
			case 1:
				ps[0] = new Point3D(0, 0, -hlen);
				ps[1] = new Point3D(0, 0, hlen);
				ps[2] = new Point3D(s1, 0, hlen + s2);
				ps[3] = new Point3D(s1 * 2, 0, hlen);
				ps[4] = new Point3D(s1 * 2, 0, -hlen);
				ps[5] = new Point3D(s1, 0, -hlen - s2);
				break;
			case 2:
				ps[0] = new Point3D(s1, 0, -hlen - s2);
				ps[1] = new Point3D(0, 0, -hlen);
				ps[2] = new Point3D(0, 0, hlen);
				ps[3] = new Point3D(s1, 0, hlen + s2);
				ps[4] = new Point3D(s1 * 2, 0, hlen);
				ps[5] = new Point3D(s1 * 2, 0, -hlen);
				break;
			case 3:
				ps[0] = new Point3D(s1 * 2, 0, -hlen);
				ps[1] = new Point3D(s1, 0, -hlen - s2);
				ps[2] = new Point3D(0, 0, -hlen);
				ps[3] = new Point3D(0, 0, hlen);
				ps[4] = new Point3D(s1, 0, hlen + s2);
				ps[5] = new Point3D(s1 * 2, 0, hlen);
				break;
			case 4:
				ps[0] = new Point3D(s1 * 2, 0, hlen);
				ps[1] = new Point3D(s1 * 2, 0, -hlen);
				ps[2] = new Point3D(s1, 0, -hlen - s2);
				ps[3] = new Point3D(0, 0, -hlen);
				ps[4] = new Point3D(0, 0, hlen);
				ps[5] = new Point3D(s1, 0, hlen + s2);
				break;
			case 5:
				ps[0] = new Point3D(s1, 0, hlen + s2);
				ps[1] = new Point3D(s1 * 2, 0, hlen);
				ps[2] = new Point3D(s1 * 2, 0, -hlen);
				ps[3] = new Point3D(s1, 0, -hlen - s2);
				ps[4] = new Point3D(0, 0, -hlen);
				ps[5] = new Point3D(0, 0, hlen);
				break;
			default:
				System.out.println("Edge index error!");
				break;
			}
		}

		setVertexAt(0, ps[0]);
		setVertexAt(1, ps[1]);
		setVertexAt(2, ps[2]);
		setVertexAt(3, ps[3]);
		setVertexAt(4, ps[4]);
		setVertexAt(5, ps[5]);
		setEdgeAt(0, ps[5], ps[0]);
		setEdgeAt(1, ps[0], ps[1]);
		setEdgeAt(2, ps[1], ps[2]);
		setEdgeAt(3, ps[2], ps[3]);
		setEdgeAt(4, ps[3], ps[4]);
		setEdgeAt(5, ps[4], ps[5]);
	}
	
	public void setAbsNormal(Point3D nn){
		absNormal = nn;
	}
	
	public Point3D getAbsNormal(){
		return absNormal;
	}

	public String toString() {
		String str = "Polygon3D " + N + ": \n[";
		for (int i = 0; i < N - 1; i++) {
			str += vertices3D[i].toString() + "\n";
		}
		str += vertices3D[N - 1].toString() + "]" + "\n";
		return str;
	}
}
