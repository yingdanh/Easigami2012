package mainpack;
import java.util.Vector;

import mygeom.Point3D;
import mygeom.Polygon3D;
import mygeom.VO3D;


public class Polygon {
	// attributes
	private int address;
	private String ptype;
	private int connection;
	private Polygon3D poly = null;	//geometry attributes of Polygon
	private double mat3d[];		// relative coordinate reference
	private double matpat[];
	private boolean isVisited;  // for BFS
	private boolean isSelected; // for selection
	private boolean[] unVisitedVertices;
	private boolean isDebug = false;
	
	public Polygon(String ptype, int address){
		this.ptype = ptype;
		this.address = address;
		isVisited = false;
		isSelected = false;
		mat3d = new double[16];
		matpat = new double[16];
		
		if(ptype.equals(AddressBook.Triangle_Equ_Str) || ptype.equals(AddressBook.Triangle_Iso_Str))
			poly = new Polygon3D(3);
		else if(ptype.equals(AddressBook.Square_Str))
			poly = new Polygon3D(4);
		else if(ptype.equals(AddressBook.Pentagon_Str))
			poly = new Polygon3D(5);
		else if(ptype.equals(AddressBook.Hexagon_Str))
			poly = new Polygon3D(6);
		
		//init the unVisitedVertices for cycles around vertices
		unVisitedVertices = new boolean[poly.getN()];
		for(int i=0; i<unVisitedVertices.length; i++){
			unVisitedVertices[i] = false;
		}
	}
	
	public int getAddress(){
		return this.address;
	}
	
	public String getType(){
		return this.ptype;
	}
	
	public byte[] getColor(){
		return AddressBook.pentagonColorMap.get(Integer.valueOf(this.address));
	}
	
	public boolean isSelected(){
		return isSelected;
	}
	
	public void setSelected(boolean s){
		isSelected = s;
	}
	
	public boolean isVisited(){
		return isVisited;
	}
	
	public void setVisited(boolean v){
		isVisited = v;
	}
	
	public void setMatrix3D(double m[]){
		System.arraycopy(m, 0, mat3d, 0, 16);
		/*for(int i=0; i<16; i++){
			mat2d[i] = m[i];
		}*/
	}
	
	public double[] getMatrix3D(){
		/*for(int i=0; i<16; i++){
			System.out.print(mat3d[i] + " ");
		}
		System.out.println();*/
		return mat3d;
	}
	
	public void setMatrixPattern(double m[]){
		System.arraycopy(m, 0, matpat, 0, 16);
	}
	
	public double[] getMatrixPattern(){
		return matpat;
	}
	
	public Polygon3D getPolygon3D(){
		return this.poly;
	}
		
	public void setConnection (int connection){
		this.connection = connection;
	}
		
	public int getConnectionChange(int conn){
		if(isDebug)
			System.out.println(connection + " : " + conn);
		if (connection == conn){ //no change
			return -1;
		}
		
		else{//something changed
			if((0x1 & connection) != (0x1 & conn))		//1st bit
				return 0;
			else if((0x2 & connection) != (0x2 & conn))	//2nd bit
				return 1;
			else if((0x4 & connection) != (0x4 & conn))	//3rd bit
				return 2;
			else if((0x8 & connection) != (0x8 & conn)) //4th bit
				return 3;
			else if((0x16& connection) != (0x16& conn)) //5th bit
				return 4;
			else if((0x32& connection) != (0x32& conn)) //6th bit
				return 5;
			else										//error!
				return -2;
		}
	}
	
	public Point3D setCoordsOpp3D(int e_index, boolean isLeft){
		if(isDebug)
			System.out.println("Polygon::setCoordsOpp");
		
		if(ptype.equals(AddressBook.Triangle_Equ_Str))
			return poly.setOppEuilateral(e_index, isLeft);
		else if(ptype.equals(AddressBook.Triangle_Iso_Str))
			return poly.setOppIsosceles(e_index, isLeft);
		else if(ptype.equals(AddressBook.Square_Str))
			return poly.setOppSquare(e_index, isLeft);
		else if(ptype.equals(AddressBook.Pentagon_Str))
			return poly.setOppPentagon(e_index, isLeft);
		else if(ptype.equals(AddressBook.Hexagon_Str))
			return poly.setOppHexagon(e_index, isLeft);
		else
			System.out.println("Error: " + ptype);
		
		return null;
	}
	
	public void setCoordsPolygon3D(int e_index, boolean isLeft){
		if(isDebug)
			System.out.println("Polygon::setCoordsPolygon3D");
		
		if(ptype.equals(AddressBook.Triangle_Equ_Str))
			poly.setEuilateral(e_index, isLeft);
		else if(ptype.equals(AddressBook.Triangle_Iso_Str))
			poly.setIsosceles(e_index, isLeft);
		else if(ptype.equals(AddressBook.Square_Str))
			poly.setSquare(e_index, isLeft);
		else if(ptype.equals(AddressBook.Pentagon_Str))
			poly.setPentagon(e_index, isLeft);
		else if(ptype.equals(AddressBook.Hexagon_Str))
			poly.setHexagon(e_index, isLeft);
		else
			System.out.println("Error: " + ptype);
	}
	
	// calculate absolute vertices based on the relative coordinates 
	// and the transformation matrix of each polygon
	public void calculateAbsVertices() throws Exception{
		System.out.println("Polygon::calculateAbsVertices");
		Point3D[] vs = poly.getVertices();
		Point3D np;
		//Point3D normal = new Point3D(0, 1, 0);
		
		//Point3D nnormal = VO3D.calPoint(this.getMatrix3D(), normal);
		//poly.setAbsNormal(nnormal);
		//System.out.println("nnormal: " + nnormal);
		for(int j=0; j<vs.length; j++){//for each vertex
			np = VO3D.calPoint(this.getMatrix3D(), vs[j]);
			System.out.println("np: " + np);
			poly.setAbsVertexAt(j, np);
		}
		
		Point3D normal = VO3D.getNormal(poly.getAbsVertexAt(0), poly.getAbsVertexAt(1), poly.getAbsVertexAt(2));
		poly.setAbsNormal(normal);
	}
	
	public boolean[] getUnVisitedVertices(){
		return unVisitedVertices;
	}
	
	public int getUnivisitedVertex(){
		for(int i=0; i<unVisitedVertices.length; i++){
			if(unVisitedVertices[i] == false){
				return i;
			}
		}
		return -1;
	}
	
	public String toString(){
		String str = this.ptype + " " + Integer.toString(this.address, 16) + " ";
		str += "<connection: " + connection + ">";
		//str += "\n" + this.poly.toString();
		return str;
	}
}
