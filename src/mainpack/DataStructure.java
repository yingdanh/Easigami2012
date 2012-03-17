package mainpack;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Vector;

import mygeom.Line3D;
import mygeom.Point3D;
import mygeom.Polygon3D;
import mygeom.VO3D;

import rw.FileWrite;

public class DataStructure {
	private Vector<Hinge> hingeVector;
	private Vector<Polygon> polygonVector;
	private Hinge adjmat[][];
	// private Vector<Hinge> danglingVector;
	private boolean isConfigured;
	private boolean isReady;
	private boolean isDebug = false;

	public DataStructure() {
		hingeVector = new Vector<Hinge>();
		polygonVector = new Vector<Polygon>();
		// danglingVector = new Vector<Hinge>();
		isReady = false;
		isConfigured = false;
	}

	public void buildAdjacencyMatrix() {
		int msize = polygonVector.size();
		adjmat = new Hinge[msize][msize];

		Hinge h;
		Polygon p1, p2;
		int p1_index, p2_index;
		int p1_edgeIndex, p2_edgeIndex;

		// build the adjacency matrix
		for (int i = 0; i < hingeVector.size(); i++) {// traverse hingeList
			h = hingeVector.get(i);
			// for a hinge that has both left and right polygon
			if (h.getLeftPolygon() != null && h.getRightPolygon() != null) {
				p1 = h.getLeftPolygon();
				p2 = h.getRightPolygon();
				p1_index = polygonVector.indexOf(p1);
				p2_index = polygonVector.indexOf(p2);
				adjmat[p1_index][p2_index] = adjmat[p2_index][p1_index] = h;
			} /*
			 * else if (h.getLeftPolygon() != null || h.getRightPolygon() !=
			 * null){ danglingVector.add(h); }
			 */
			/*
			 * else if (h.getLeftPolygon() != null) { p1 = h.getLeftPolygon();
			 * p1_index = polygonVector.indexOf(p1); p1_edgeIndex =
			 * h.getLeftPolygonIndex(); adjmat[msize + p1_edgeIndex][p1_index] =
			 * h; } else if (h.getRightPolygon() != null) { p2 =
			 * h.getRightPolygon(); p2_index = polygonVector.indexOf(p2);
			 * p2_edgeIndex = h.getRightPolygonIndex(); adjmat[p2_index][msize +
			 * p2_edgeIndex] = h; }
			 */
		}

		if (isDebug)
			printAdjacencyMatrix(msize);
	}

	public void printAdjacencyMatrix(int msize) {
		// print out the adjacency matrix
		System.out.println("print out the adjacency matrix.");
		for (int i = 0; i < msize; i++) {
			for (int j = 0; j < msize; j++) {
				if (adjmat[i][j] != null) {
					System.out.format("%10s", "0x" + Integer.toString(adjmat[i][j].getAddress(), 16));
				} else {
					System.out.format("%10s", "0");
				}
			}

			System.out.println();
		}
	}

	public void add2HingeVector(Hinge h) {
		hingeVector.add(h);
	}

	public void clearHingeVector() {
		hingeVector.clear();
		// danglingVector.clear();
	}

	public void add2PolygonVector(Polygon p) {
		polygonVector.add(p);
	}

	public void clearPolygonVector() {
		polygonVector.clear();
	}

	public void setPolygonVectorUnvisited() {
		for (int i = 0; i < polygonVector.size(); i++) {
			polygonVector.get(i).setVisited(false);
		}
	}

	public void setHingeVectorUnvisited() {
		for (int i = 0; i < hingeVector.size(); i++) {
			hingeVector.get(i).setVisited(false);
		}
	}

	public Vector<Hinge> getHingeVector() {
		return hingeVector;
	}

	public Vector<Hinge> getDanglingHingeVector() {
		return null; // danglingVector;
	}

	public Vector<Polygon> getPolygonVetor() {
		return polygonVector;
	}

	// get the list of neighboring polygons which
	// have not been visited
	public Vector<Polygon> getNeighbors(Polygon p) {
		if (isDebug)
			System.out.println("DS::getNeighbors");

		Vector<Polygon> neighbors = new Vector<Polygon>();
		int p_index = polygonVector.indexOf(p);
		if (p_index == -1)
			return neighbors; // return an empty vector
		Hinge h;
		Polygon pp;

		for (int i = 0; i < polygonVector.size(); i++) {
			// System.out.println(p_index + ", " + i);
			pp = polygonVector.get(i);
			h = adjmat[p_index][i];
			if (h != null && !pp.isVisited()) {
				neighbors.add(polygonVector.get(i));
			}
		}
		return neighbors;
	}
	
	//prevp
	public Polygon getNeighbor(Polygon prevp, int edgeindex){
		Polygon p;
		Hinge h;
		
		//loop through all neighbors from the adjmat
		int p_index = polygonVector.indexOf(prevp);
		for (int i = 0; i < polygonVector.size(); i++) {
			h = adjmat[p_index][i];
			p = polygonVector.get(i);
			if(h != null && !p.isVisited()){
				if((h.isLeftPolygon(prevp) && h.getLeftPolygonIndex()==edgeindex) || 
						(h.isRightPolygon(prevp) && h.getRightPolygonIndex()==edgeindex)){
					return p;
				}
			}
		}
		
		return null;
	}

	// get a polygon based on the address
	public Polygon getPolygonAt(int address) {
		Polygon p;
		for (int i = 0; i < polygonVector.size(); i++) {
			p = polygonVector.get(i);
			if (polygonVector.get(i).getAddress() == address)
				return p;
		}
		return null;
	}

	// locate the hinge that joins 2 polygons
	public Hinge findHinge(Polygon curp, Polygon prevp) {
		if (polygonVector.indexOf(curp) != -1
				&& polygonVector.indexOf(prevp) != -1)
			return adjmat[polygonVector.indexOf(curp)][polygonVector
					.indexOf(prevp)];
		return null;
	}

	// ready to draw on canvas
	public void setReady(boolean ready) {
		this.isReady = ready;
	}

	public boolean getReady() {
		return isReady;
	}
	
	public Hinge[][] getAdjacentMatrix(){
		return this.adjmat;
	}

	public Integer getPolygonByColor(ByteBuffer bb) {
		Iterator<byte[]> itValue = AddressBook.pentagonColorMap.values()
				.iterator();
		Iterator<Integer> itKey = AddressBook.pentagonColorMap.keySet()
				.iterator();
		byte[] color;
		Integer key;

		while (itValue.hasNext() && itKey.hasNext()) {
			color = (byte[]) itValue.next();
			key = itKey.next();
			// System.out.println(color[0] + ", " + color[1] + ", " + color[2]);
			if (bb.get(0) == color[0] && bb.get(1) == color[1]
					&& bb.get(2) == color[2])
				return key;
		}
		return null;
	}

	public void printVectors(boolean output, FileWrite fw) {
		System.out.println("\nPrint hingeVector- - - - - -"
				+ hingeVector.size());
		Hinge h;
		for (int i = 0; i < this.hingeVector.size(); i++) {
			h = hingeVector.get(i);
			System.out.println(h);
			if (output) {
				fw.write2file(Integer.toString(h.getAddress(), 16) + ": ");
				fw.write2file(Integer.toString(h.getLeftPolygon().getAddress(),
						16) + ", " + h.getLeftPolygonIndex() + "; ");
				fw.write2file(Integer.toString(
						h.getRightPolygon().getAddress(), 16)
						+ ", "
						+ h.getRightPolygonIndex() + "\n");
			}
		}

		System.out.println("Print polygonVector");
		for (int i = 0; i < this.polygonVector.size(); i++) {
			System.out.println(polygonVector.get(i));
		}
		System.out.println("- - - - - -");
	}

	// -----------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------
	public boolean isConfigured() {
		return isConfigured;
	}

	public void setConfigured(boolean configured) {
		isConfigured = configured;
	}

	public void advantagedConfigure() {
		if (isDebug)
			System.out.println("DataStructure::advantagedConfigure");

		Hinge h1, h2, h;
		Polygon leftPolygon, rightPolygon, p;
		int posLeft, posRight, pos;

		for (int i = 0; i < hingeVector.size(); i++) {
			h1 = hingeVector.get(i);
			System.out.println("h1 " + Integer.toString(h1.getAddress(), 16));
			leftPolygon = h1.getLeftPolygon();
			rightPolygon = h1.getRightPolygon();
			posLeft = polygonVector.indexOf(leftPolygon);
			posRight = polygonVector.indexOf(rightPolygon);
			double angle;
			if (posLeft == -1 || posRight == -1) // for hinges which don't have
													// both polygons
				continue;

			// check left Polygon
			for (int j = 0; j < polygonVector.size(); j++) {// loop through all
															// the polygons
				h2 = adjmat[posLeft][j]; // get 2nd hinge

				if (h2 != null) {
					if (h1.getAddress() == h2.getAddress())
						continue;
					System.out.println("h2 "
							+ Integer.toString(h2.getAddress(), 16));
					if (h2.isLeftPolygon(leftPolygon)) {
						System.out.println("isLeftPolygon");
						p = h2.getRightPolygon();
						pos = polygonVector.indexOf(p);
						h = adjmat[posLeft][pos];
						if (h != null) {
							// set angles
							setAngles(h1, h2, h, leftPolygon, rightPolygon, p);
						}
					} else if (h2.isRightPolygon(leftPolygon)) {
						System.out.println("isRightPolygon");
						p = h2.getLeftPolygon();
						pos = polygonVector.indexOf(p);
						h = adjmat[posRight][pos];
						System.out.println(posLeft + ", " + posRight + ", "
								+ pos);
						if (h != null) {
							// set angles
							setAngles(h1, h2, h, leftPolygon, rightPolygon, p);
						}
					}
				}
			}
			System.out.println();
		}
	}

	private void setAngles(Hinge h1, Hinge h2, Hinge h, Polygon left,
			Polygon right, Polygon p) {
		double angle;
		if (left.getType().equals(AddressBook.Triangle_Equ_Str)
				&& right.getType().equals(AddressBook.Triangle_Equ_Str)
				&& p.getType().equals(AddressBook.Triangle_Equ_Str)) {
			angle = Math.acos(1.0 / 3.0);
			if (!h1.isAdjusted())
				// if (h1.getAngle(false) < Math.PI / 2)
				h1.setAngle(angle);
			// else
			// h1.setAngle(angle * 2);
			if (!h2.isAdjusted())
				// if (h2.getAngle(false) < Math.PI / 2)
				h2.setAngle(angle);
			// else
			// h2.setAngle(angle * 2);
			if (!h.isAdjusted())
				// if (h.getAngle(false) < Math.PI / 2)
				h.setAngle(angle);
			// else
			// h.setAngle(angle * 2);
		} else if (left.getType().equals(AddressBook.Square_Str)
				&& right.getType().equals(AddressBook.Square_Str)
				&& p.getType().equals(AddressBook.Square_Str)) {
			angle = Math.PI / 2;
			if (!h1.isAdjusted())
				if (Math.abs(h1.getAngle(false) - Math.PI / 2) < Math.PI / 10)
					h1.setAngle(angle);
			// else
			// h1.setAngle(angle * 2);
			if (!h2.isAdjusted())
				if (Math.abs(h2.getAngle(false) - Math.PI / 2) < Math.PI / 10)
					h2.setAngle(angle);
			// else
			// h2.setAngle(angle * 2);
			if (!h.isAdjusted())
				if (Math.abs(h.getAngle(false) - Math.PI / 2) < Math.PI / 10)
					h.setAngle(angle);
			// else
			// h.setAngle(angle * 2);
		}
	}
	
	public void testNewton(){
		System.out.println("in testNewton");
		//double ao = Math.acos(1.0 / 3.0);
		double ao = Math.PI - Math.acos(-1.0 / 3.0);
		double[] angles = new double[hingeVector.size()];
		
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
			chains.chains[i].printChain();
		}
		
		System.out.println("f: " + Newton.f(chains, angles));
		System.out.println();
		
		double dby1 = Newton.dfby(chains, angles, 0);
		System.out.println("dby1: " + dby1);
		
		System.out.println("numDiff: " + Newton.numDiff(chains, angles, 0, 1e-6));
		System.out.println();
		
		double dby11 = Newton.df2by(chains, angles, 0, 0);
		System.out.println("dby11: " + dby11);
		
		System.out.println("numDiff: " + Newton.numDiff(chains, angles, 0, 0, 1e-5));
		for (int i = 0; i < 50; ++ i) {
			boolean printStep = ((i + 1)%24) == 0;
			angles = Newton.nstep(chains, angles, printStep);
		}
	}
	
	public MatrixChains getVertexChains(Vector<Vector<Integer>> cycles){
		System.out.println("in getVertexChains");
		MatrixChains chains = new MatrixChains(cycles.size());
		Vector<Integer> cycle;
		MatrixChain togo;
		
		for(int i=0; i<cycles.size(); i++){
			cycle = cycles.get(i);
			togo = getVertexChain(cycle);
			chains.setValueAt(i, togo);
		}
		
		return chains;
	}
	
	//compute MatrixChain from a cycle
	public MatrixChain getVertexChain(Vector<Integer> cycle){
		System.out.println("in getVertexChain " + cycle.size());
		MatrixChain togo = new MatrixChain((cycle.size()+1)*3);
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
		double[][] Ruvw;  // transpose matrix of new local coordinate system
		double[][] T; // translation matrix
		double[][] M; // resultant matrix
		double[][] ZRuvw; // transpose matrix of new local coordinate system
		double[][] resM = VO3D.getIdentityMatrix44();
		double dihedralAngle;
		double angle; //= Math.acos(1.0/3.0);
		
		//set coordinates for the first polygon
		if(h.isLeftPolygon(prevp)){
			prevp.setCoordsPolygon3D(h.getLeftPolygonIndex(), true);
		}else{
			prevp.setCoordsPolygon3D(h.getRightPolygonIndex(), false);
		}
		
		//get the matrices following the cycle
		for(int i=0; i<=cycle.size(); i++){
			prevIndex = i%cycle.size();
			curIndex = (i+1)%cycle.size();
			
			curp = polygonVector.get(cycle.get(curIndex));
			h = adjmat[cycle.get(prevIndex)][cycle.get(curIndex)];
			if(i == cycle.size()){
				angle = Math.PI;
			}else{
				angle = h.getAngle(false);
//				angle = Math.acos(-1.0 / 3.0) + 0.1;
			}
			
			//System.out.println("h: 0x" + Integer.toString(h.getAddress(), 16));
			//System.out.println("prevp: 0x" + Integer.toString(prevp.getAddress(), 16) + "; " + 
					//"curp: 0x" + Integer.toString(curp.getAddress(), 16));
			p3d = prevp.getPolygon3D();
			double sign;
			if(h.isLeftPolygon(curp)){
				//System.out.println("Left!");
				edge = p3d.getEdgeAt(h.getRightPolygonIndex());
				opp = prevp.setCoordsOpp3D(h.getRightPolygonIndex(), false);
				cen = VO3D.middlePoint(edge);
				curp.setCoordsPolygon3D(h.getLeftPolygonIndex(), true);
				
				dihedralAngle = angle - Math.PI;
				sign = -1.0;
			}else{
				//System.out.println("Right!");
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
			T = VO3D.getMatrix_translation(cen.getX(), cen.getY(),cen.getZ());
			togo.append(T);
			togo.append(Ruvw);
			if (i == cycle.size()) {
				togo.append(VO3D.getMatrix_rotateZ(dihedralAngle));
			}
			else {
			    togo.append(hingeVector.indexOf(h), sign, dihedralAngle, VO3D.getMatrix_rotateZ(dihedralAngle));
			}
			//code for checking identity
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
		//System.out.println("in findCyclesSpanningTree");
		Vector<Vector<Integer>> cycles = new Vector<Vector<Integer>>();
		MyQueue<Polygon> queue = new MyQueue<Polygon>(); // queue for BFS
		Hinge h;
		Polygon phead;
		Polygon curp;
		Vector<Polygon> pvec = null; // adjacent neighbors to each polygon
		Polygon p; // variable for finding the neighbors of a Polygon
		SpanningTree st = new SpanningTree(this);
		boolean[][] tfmat = new boolean[polygonVector.size()][polygonVector.size()];

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
			pvec = getNeighbors(curp);
			for (int i = 0; i < pvec.size(); i++) {
				p = pvec.get(i);
				//System.out.println( "in findCyclesSpanningTree - neighbors: " + Integer.toString(p.getAddress(), 16));
				queue.enqueue(p);
				p.setVisited(true);
				tfmat[polygonVector.indexOf(curp)][polygonVector.indexOf(p)] = true;
				tfmat[polygonVector.indexOf(p)][polygonVector.indexOf(curp)] = true;
			}
		}
		// set all polygons as non-visited
		setPolygonVectorUnvisited();
				
		/*System.out.println("print out true/false table");
		for(int i=0; i<tfmat.length; i++){
			for(int j=0; j<tfmat.length; j++){
				System.out.print(tfmat[i][j] + " ");
			}
			System.out.println();
		}*/
		//find unvisited edges in adjmat
		for(int i=0; i<polygonVector.size(); i++){
			for(int j=i; j<polygonVector.size(); j++){
				if(adjmat[i][j]!=null && tfmat[i][j]==false){
					cycles.add(findOneCycle(i, j, st));
				}
			}
		}

		//st.printSpanningTree();
		return cycles;
	}
	
	// with 1 additional edge
	// find one cycle in the spanning tree
	public Vector<Integer> findOneCycle(int i, int j, SpanningTree st){
		//System.out.println("in findOneCycle");
		Vector<Integer> cycle1 = st.path2Root(i); // size >=2
		Vector<Integer> cycle2 = st.path2Root(j); // size >=2
		/*for(int p=0; p<cycle1.size(); p++){
			System.out.print(cycle1.get(p) + " ");
		}
		System.out.println();
		for(int q=0; q<cycle2.size(); q++){
			System.out.print(cycle2.get(q) + " ");
		}
		System.out.println();*/
				
		int pivot = -1;;
		for(int x=0, y=0; x<cycle1.size() && y<cycle2.size(); x++, y++){
			if(cycle1.get(x) == cycle2.get(y)){
				pivot = cycle1.get(x);
				cycle1.remove(x);
				cycle2.remove(x);
				x--;
				y--;
			}
		}
		
		// join cycle1, pivot and cycle2 to make the final cycle
		cycle1.insertElementAt(pivot, 0);
		for(int z=cycle2.size()-1; z>=0; z--){
			cycle1.add(cycle2.get(z));
		}
		return cycle1;
	}
	
	//print all cycles
	public void printAllCycles(Vector<Vector<Integer>> cycles){
		System.out.println("printAllCycles: ");
		Vector<Integer> cycle;
		
		for(int i=0; i<cycles.size(); i++){
			//System.out.println("print one cycle");
			cycle = cycles.get(i);
			for(int j=0; j<cycle.size(); j++){
				System.out.print(cycle.get(j) + " ");
			}
			System.out.println();
		}
		
		for(int i=0; i<polygonVector.size(); i++){
			System.out.print("0x" + Integer.toString(polygonVector.get(i).getAddress(), 16) + " ");
		}
		System.out.println();
	}

	// find cycles based on each vertex
	public Vector<Vector<Integer>> findCyclesVertex() {
		System.out.println("in findCyclesVertex");
		Vector<Vector<Integer>> cycles = new Vector<Vector<Integer>>();
		Vector<Integer> cycle;
		Polygon p;
		
		for(int i=0; i<polygonVector.size(); i++){
			p = polygonVector.get(i);
			//System.out.println("Handling 0x" + Integer.toString(p.getAddress(), 16));
			while(p.getUnivisitedVertex()!=-1){
				//handle each vertex
				cycle = findCycleVertex(p, p.getUnivisitedVertex());
				if(cycle != null){
					cycles.add(cycle);
				}
			}
		}
		
		for(int i=0; i<polygonVector.size(); i++){
			System.out.print("0x" + Integer.toString(polygonVector.get(i).getAddress(), 16) + " ");
		}
		System.out.println();
		/*System.out.println("print out all cycles based on vertices");
		for(int i=0; i<cycles.size(); i++){
			cycle = cycles.get(i);
			System.out.println("find one cycle");
			for(int j=0; j<cycle.size(); j++){
				System.out.print(cycle.get(j) + " ");
			}
			System.out.println();
			System.out.println();
		}*/
		return cycles;
	}
	
	//vindex - vertex index
	public Vector<Integer> findCycleVertex(Polygon p, int vindex){
		System.out.println("in findCycleVertex 0x" + Integer.toString(p.getAddress(), 16) + ", " + vindex);
		Vector<Integer> cycle = new Vector<Integer>();
		int nextedgeindex = vindex;
		int nextvertexindex= -1;
		Polygon prevp = p;
		Polygon nextp;
		Hinge h;
		
		do{
			nextp = getNeighbor(prevp, nextedgeindex);
			if(nextp == null){//cannot find the neighbor polygon 
				System.out.println("Didnt find a cycle\n");
				p.getUnVisitedVertices()[vindex] = true;
				setPolygonVectorUnvisited();
				return null;
			}
			nextp.setVisited(true);
			h = this.findHinge(prevp, nextp);
			//System.out.println("prevp is 0x" + Integer.toString(prevp.getAddress(), 16) + 
					//", nextp is 0x" + Integer.toString(nextp.getAddress(), 16) + 
					//"; h is 0x" + Integer.toString(h.getAddress(), 16));
			if(h.isLeftPolygon(prevp)){//nextp is on the right
				//System.out.println("left");
				nextedgeindex = h.getRightPolygonIndex();
				nextedgeindex = (nextedgeindex+nextp.getPolygon3D().getN()-1)%nextp.getPolygon3D().getN();
				nextvertexindex = nextedgeindex;
			}else if(h.isRightPolygon(prevp)){//nextp is on the left
				//System.out.println("right");
				nextedgeindex = h.getLeftPolygonIndex();
				nextedgeindex = (nextedgeindex+nextp.getPolygon3D().getN()-1)%nextp.getPolygon3D().getN();
				nextvertexindex = nextedgeindex;
			}
			
			//System.out.println("nextedgeindex=" + nextedgeindex + ", nextvertexindex=" + nextvertexindex);
		
			cycle.add(polygonVector.indexOf(nextp));
			nextp.getUnVisitedVertices()[nextvertexindex] = true;
			prevp = nextp;
		}while(p.getAddress()!=nextp.getAddress() || nextedgeindex != vindex);		
		setPolygonVectorUnvisited();
		
		for(int j=0; j<cycle.size(); j++){
			System.out.print(cycle.get(j) + " ");
		}
		System.out.println("\n");
		
		return cycle;
	}
}
