package mainpack;

import java.util.Vector;

import mygeom.Line3D;
import mygeom.Point3D;
import mygeom.Polygon3D;
import mygeom.VO3D;

public class AdjustAngles {
	private DataStructure ds;
	private Vector<Polygon> polygonVector;
	private Vector<Hinge> hingeVector;
	private Hinge[][] adjmat;

	public AdjustAngles(DataStructure ds) {
		this.ds = ds;
		polygonVector = ds.getPolygonVector();
		hingeVector = ds.getHingeVector();
		adjmat = ds.getAdjacentMatrix();
	}

	public void runNewton(){
		System.out.println("in runNewton");
		//double ao = Math.acos(1.0 / 3.0);
		//double ao = Math.PI - Math.acos(-1.0 / 3.0);
		double[] angles = new double[hingeVector.size()];
		for (int i = 0; i < angles.length; ++ i) {
			angles[i] = Double.NaN;
		}
		
		Vector<Vector<Integer>> cycles = this.findCyclesVertex();
		printAllCycles(cycles);
		MatrixChains chains = getVertexChains(cycles);
		for (int i = 0; i < chains.getLength(); i++){
			chains.chains[i].printChain();
		}
		
		for (int i = 0; i < chains.getLength(); i++){
			MatrixChain chain = chains.chains[i];
			for (int j = 0; j < chain.matrices.length; ++ j) {
				MatrixHolder holder = chain.matrices[j];
				if (holder instanceof DihedralAngle) {
					DihedralAngle da = (DihedralAngle) holder;
					double origAngle = da.sign *(Math.PI - da.angle);
					if (origAngle < 0) {
						origAngle += 2 * Math.PI;
					}
					angles[da.edgeindex] = origAngle;
				}
			}
		}
		boolean cycleCover = true;
		for (int i = 0; i < angles.length; ++ i) {
			if (Double.isNaN(angles[i])) {
				cycleCover = false;
				System.out.println("*@*@*@* Failure in cycle cover for shape - not running optimiser *@*@*@*@");
			}
		}
		if (cycleCover) {
		    OptimisationReport report = Newton.runNewton(chains, angles);
		    this.setAdjustedAngles2Hinges(report.angles);
		}
	}


	public MatrixChains getVertexChains(Vector<Vector<Integer>> cycles) {
		System.out.println("in getVertexChains");
		MatrixChains chains = new MatrixChains(cycles.size());
		Vector<Integer> cycle;
		MatrixChain togo;

		for (int i = 0; i < cycles.size(); i++) {
			cycle = cycles.get(i);
			togo = getVertexChain(cycle);
			chains.setValueAt(i, togo);
		}

		return chains;
	}

	// compute MatrixChain from a cycle
	public MatrixChain getVertexChain(Vector<Integer> cycle) {
		System.out.println("in getVertexChain " + cycle.size());
		MatrixChain togo = new MatrixChain((cycle.size() + 1) * 3);
		int prevIndex = 0;
		int curIndex = 1;
		Polygon first = polygonVector.get(cycle.get(prevIndex));
		Polygon prevp = first;
		Polygon curp = polygonVector.get(cycle.get(curIndex));
		Hinge h = adjmat[cycle.get(prevIndex)][cycle.get(curIndex)];
		Polygon3D p3d;
		Line3D edge; // the edge that the hinge connects to the prev
		Point3D opp; // the point that is opposite to the connected edge
		Point3D cen; // hinge location
		Point3D axis; // hinge axis
		double[][] Ruvw; // transpose matrix of new local coordinate system
		double[][] T; // translation matrix
		double[][] M; // resultant matrix
		double[][] ZRuvw; // transpose matrix of new local coordinate system
		double[][] resM = VO3D.getIdentityMatrix44();
		double dihedralAngle;
		double angle; // = Math.acos(1.0/3.0);

		// set coordinates for the first polygon
		if (h.isLeftPolygon(prevp)) {
			prevp.setCoordsPolygon3D(h.getLeftPolygonIndex(), true);
		} else {
			prevp.setCoordsPolygon3D(h.getRightPolygonIndex(), false);
		}

		// get the matrices following the cycle
		for (int i = 0; i <= cycle.size(); i++) {
			prevIndex = i % cycle.size();
			curIndex = (i + 1) % cycle.size();

			curp = polygonVector.get(cycle.get(curIndex));
			h = adjmat[cycle.get(prevIndex)][cycle.get(curIndex)];
			if (i == cycle.size()) {
				angle = Math.PI;
			} else {
				angle = h.getRawAngleFromPot();
				// angle = Math.acos(-1.0 / 3.0) + 0.1;
			}

			// System.out.println("h: 0x" + Integer.toString(h.getAddress(),
			// 16));
			// System.out.println("prevp: 0x" +
			// Integer.toString(prevp.getAddress(), 16) + "; " +
			// "curp: 0x" + Integer.toString(curp.getAddress(), 16));
			p3d = prevp.getPolygon3D();
			double sign;
			if (h.isLeftPolygon(curp)) {
				// System.out.println("Left!");
				edge = p3d.getEdgeAt(h.getRightPolygonIndex());
				opp = prevp.setCoordsOpp3D(h.getRightPolygonIndex(), false);
				cen = VO3D.middlePoint(edge);
				curp.setCoordsPolygon3D(h.getLeftPolygonIndex(), true);

				dihedralAngle = angle - Math.PI;
				sign = -1.0;
			} else {
				// System.out.println("Right!");
				edge = p3d.getEdgeAt(h.getLeftPolygonIndex());
				opp = prevp.setCoordsOpp3D(h.getLeftPolygonIndex(), true);
				cen = VO3D.middlePoint(edge);
				curp.setCoordsPolygon3D(h.getRightPolygonIndex(), false);

				dihedralAngle = Math.PI - angle;
				sign = 1.0;
			}

			// calculate the new axis
			if (VO3D.adjustAxis(opp, false)) {
				axis = VO3D.diff_vector(edge.getP2(), edge.getP1());
			} else {
				axis = VO3D.diff_vector(edge.getP1(), edge.getP2());
			}
			// calculate the new coordinates
			Ruvw = VO3D.GetBasisTranspose(axis);
			T = VO3D.getMatrix_translation(cen.getX(), cen.getY(), cen.getZ());
			togo.append(T);
			togo.append(Ruvw);
			if (i == cycle.size()) {
				togo.append(VO3D.getMatrix_rotateZ(dihedralAngle));
			} else {
				togo.append(hingeVector.indexOf(h), sign, dihedralAngle,
						VO3D.getMatrix_rotateZ(dihedralAngle));
			}
			// code for checking identity
			ZRuvw = VO3D.formLocalCoordinates3D(axis, dihedralAngle);
			M = VO3D.matrixMultiply(T, ZRuvw);
			resM = VO3D.matrixMultiply(resM, M);

			prevp = curp;
		}

		System.out.println("check resultant matrix");
		VO3D.printMatrix44(resM);
		return togo;
	}

	// find the minimum complete set of cycles based on the spanning tree
	public Vector<Vector<Integer>> findCyclesSpanningTree() {
		// System.out.println("in findCyclesSpanningTree");
		Vector<Vector<Integer>> cycles = new Vector<Vector<Integer>>();
		MyQueue<Polygon> queue = new MyQueue<Polygon>(); // queue for BFS
		Hinge h;
		Polygon phead;
		Polygon curp;
		Vector<Polygon> pvec = null; // adjacent neighbors to each polygon
		Polygon p; // variable for finding the neighbors of a Polygon
		SpanningTree st = new SpanningTree(ds);
		boolean[][] tfmat = new boolean[polygonVector.size()][polygonVector
				.size()];

		if (hingeVector.size() == 0)
			return cycles;

		h = hingeVector.get(0);

		// find the 1st polygon
		phead = h.getLeftPolygon();
		if (phead == null)
			phead = h.getRightPolygon();
		if (phead == null) {
			System.out
					.println("there is no polygon connected to either side of the head hinge");
			return cycles;
		}

		// enqueue the starting node
		queue.enqueue(phead);
		phead.setVisited(true);

		while (!queue.isEmpty()) {
			curp = queue.dequeue();
			st.insert(polygonVector.indexOf(curp));

			// enqueue neighboring nodes
			pvec = ds.getNeighbors(curp);
			for (int i = 0; i < pvec.size(); i++) {
				p = pvec.get(i);
				// System.out.println( "in findCyclesSpanningTree - neighbors: "
				// + Integer.toString(p.getAddress(), 16));
				queue.enqueue(p);
				p.setVisited(true);
				tfmat[polygonVector.indexOf(curp)][polygonVector.indexOf(p)] = true;
				tfmat[polygonVector.indexOf(p)][polygonVector.indexOf(curp)] = true;
			}
		}
		// set all polygons as non-visited
		ds.setPolygonVectorUnvisited();

		/*
		 * System.out.println("print out true/false table"); for(int i=0;
		 * i<tfmat.length; i++){ for(int j=0; j<tfmat.length; j++){
		 * System.out.print(tfmat[i][j] + " "); } System.out.println(); }
		 */
		// find unvisited edges in adjmat
		for (int i = 0; i < polygonVector.size(); i++) {
			for (int j = i; j < polygonVector.size(); j++) {
				if (adjmat[i][j] != null && tfmat[i][j] == false) {
					cycles.add(findOneCycle(i, j, st));
				}
			}
		}

		// st.printSpanningTree();
		return cycles;
	}

	// with 1 additional edge
	// find one cycle in the spanning tree
	public Vector<Integer> findOneCycle(int i, int j, SpanningTree st) {
		// System.out.println("in findOneCycle");
		Vector<Integer> cycle1 = st.path2Root(i); // size >=2
		Vector<Integer> cycle2 = st.path2Root(j); // size >=2
		/*
		 * for(int p=0; p<cycle1.size(); p++){ System.out.print(cycle1.get(p) +
		 * " "); } System.out.println(); for(int q=0; q<cycle2.size(); q++){
		 * System.out.print(cycle2.get(q) + " "); } System.out.println();
		 */

		int pivot = -1;
		;
		for (int x = 0, y = 0; x < cycle1.size() && y < cycle2.size(); x++, y++) {
			if (cycle1.get(x) == cycle2.get(y)) {
				pivot = cycle1.get(x);
				cycle1.remove(x);
				cycle2.remove(x);
				x--;
				y--;
			}
		}

		// join cycle1, pivot and cycle2 to make the final cycle
		cycle1.insertElementAt(pivot, 0);
		for (int z = cycle2.size() - 1; z >= 0; z--) {
			cycle1.add(cycle2.get(z));
		}
		return cycle1;
	}

	// print all cycles
	public void printAllCycles(Vector<Vector<Integer>> cycles) {
		System.out.println("printAllCycles: ");
		Vector<Integer> cycle;

		for (int i = 0; i < cycles.size(); i++) {
			// System.out.println("print one cycle");
			cycle = cycles.get(i);
			for (int j = 0; j < cycle.size(); j++) {
				System.out.print(cycle.get(j) + " ");
			}
			System.out.println();
		}

		for (int i = 0; i < polygonVector.size(); i++) {
			System.out.print("0x"
					+ Integer.toString(polygonVector.get(i).getAddress(), 16)
					+ " ");
		}
		System.out.println();
	}

	// find cycles based on each vertex
	public Vector<Vector<Integer>> findCyclesVertex() {
		System.out.println("in findCyclesVertex");
		Vector<Vector<Integer>> cycles = new Vector<Vector<Integer>>();
		Vector<Integer> cycle;
		Polygon p;

		for (int i = 0; i < polygonVector.size(); i++) {
			p = polygonVector.get(i);
			// System.out.println("Handling 0x" +
			// Integer.toString(p.getAddress(), 16));
			while (p.getUnivisitedVertex() != -1) {
				// handle each vertex
				cycle = findCycleVertex(p, p.getUnivisitedVertex());
				if (cycle != null) {
					cycles.add(cycle);
				}
			}
		}

		/*
		 * System.out.println("print out all cycles based on vertices"); for(int
		 * i=0; i<cycles.size(); i++){ cycle = cycles.get(i);
		 * System.out.println("find one cycle"); for(int j=0; j<cycle.size();
		 * j++){ System.out.print(cycle.get(j) + " "); } System.out.println();
		 * System.out.println(); }
		 */
		return cycles;
	}

	// vindex - vertex index
	public Vector<Integer> findCycleVertex(Polygon p, int vindex) {
		// System.out.println("in findCycleVertex 0x" +
		// Integer.toString(p.getAddress(), 16) + ", " + vindex);
		Vector<Integer> cycle = new Vector<Integer>();
		int nextedgeindex = vindex;
		int nextvertexindex = -1;
		Polygon prevp = p;
		Polygon nextp;
		Hinge h;

		do {
			nextp = ds.getNeighbor(prevp, nextedgeindex);
			if (nextp == null) {// cannot find the neighbor polygon
				// System.out.println("Didnt find a cycle\n");
				p.getUnVisitedVertices()[vindex] = true;
				ds.setPolygonVectorUnvisited();
				return null;
			}
			nextp.setVisited(true);
			h = ds.findHinge(prevp, nextp);
			// System.out.println("prevp is 0x" +
			// Integer.toString(prevp.getAddress(), 16) +
			// ", nextp is 0x" + Integer.toString(nextp.getAddress(), 16) +
			// "; h is 0x" + Integer.toString(h.getAddress(), 16));
			if (h.isLeftPolygon(prevp)) {// nextp is on the right
				// System.out.println("left");
				nextedgeindex = h.getRightPolygonIndex();
			} else if (h.isRightPolygon(prevp)) {// nextp is on the left
				// System.out.println("right");
				nextedgeindex = h.getLeftPolygonIndex();
			}
			
			nextedgeindex = (nextedgeindex + nextp.getPolygon3D().getN() - 1)
					% nextp.getPolygon3D().getN();
			nextvertexindex = nextedgeindex;
			// System.out.println("nextedgeindex=" + nextedgeindex +
			// ", nextvertexindex=" + nextvertexindex);

			cycle.add(polygonVector.indexOf(nextp));
			nextp.getUnVisitedVertices()[nextvertexindex] = true;
			prevp = nextp;
		} while (p.getAddress() != nextp.getAddress()
				|| nextedgeindex != vindex);
		ds.setPolygonVectorUnvisited();

		// print out once cycle if it exists
		/*
		 * for(int j=0; j<cycle.size(); j++){ System.out.print(cycle.get(j) +
		 * " "); } System.out.println("\n");
		 */

		return cycle;
	}

	// set new angles back to hinges
	public void setAdjustedAngles2Hinges(double[] angles) {
		for (int i = 0; i < hingeVector.size(); i++) {
			hingeVector.get(i).setAngle(angles[i]);
		}
	}
}
