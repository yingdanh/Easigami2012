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
	//private boolean isConfigured;
	private boolean isReady;
	private boolean isDebug = true;

	public DataStructure() {
		hingeVector = new Vector<Hinge>();
		polygonVector = new Vector<Polygon>();
		// danglingVector = new Vector<Hinge>();
		isReady = false;
		//isConfigured = false;
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

		if (isDebug){
			System.out.println("print all polygons");
			for(int i=0; i<polygonVector.size(); i++){
				System.out.println(Integer.toString(polygonVector.get(i).getAddress(), 16) + " ");
			}
			System.out.println();
			
			System.out.println("print all hinges");
			for(int i=0; i<hingeVector.size(); i++){
				System.out.println(Integer.toString(hingeVector.get(i).getAddress(), 16) + " ");
			}
			System.out.println();
			
			printAdjacencyMatrix(msize);
		}
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

	public Vector<Polygon> getPolygonVector() {
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
	
	public Polygon getConnectedPolygon(Polygon curp, Hinge h){
		Polygon prevp = null;
		
		if (h.isLeftPolygon(curp))
			prevp = h.getRightPolygon();
		else if (h.isRightPolygon(curp))
			prevp = h.getLeftPolygon();
		
		return prevp;
	}

	// locate the hinge that joins 2 polygons
	public Hinge findHinge(Polygon curp, Polygon prevp) {
		if (polygonVector.indexOf(curp) != -1
				&& polygonVector.indexOf(prevp) != -1)
			return adjmat[polygonVector.indexOf(curp)][polygonVector
					.indexOf(prevp)];
		return null;
	}
	
	// find out if there is any polygon in bak which is 
	// connected to curp
	// return the hinge
	public Hinge findHinge(Polygon curp, Vector<Polygon> bak) {
		Hinge h = null;
		for (int i = 0; i < bak.size(); i++) {
			// System.out.println("i = " + i);
			// check if cur is connected to some previous polygon
			h = findHinge(curp, bak.get(i));
			// System.out.println("h = " + h);
			if (h != null) {
				return h;
			}
		}
		return h;
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
	/*public boolean isConfigured() {
		return isConfigured;
	}

	public void setConfigured(boolean configured) {
		isConfigured = configured;
	}*/

	/*public void advantagedConfigure() {
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
	}*/

	/*private void setAngles(Hinge h1, Hinge h2, Hinge h, Polygon left,
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
	}*/
}
