package mainpack;
import java.io.FileNotFoundException;
import java.io.IOException;

import rw.FileRead;
import rw.FileWrite;

import gamicomm.CommPortModem;


public class GamiThread extends Thread {
	private CommPortModem modem;
	private MainController ctrl;
	private DataStructure ds;
	private AdjustAngles aa;
	private boolean portOpen;
	private boolean isDebug = false;
	
	// read an input file, instead of receiving data from Easigami
	private static final boolean isWritingFile = false; // true - write to a file
	private String filename = "octahedron_7pieces" + ".ezg";
	private FileWrite fw;
	private static final boolean isReadingFile = true;	//true - read a file
	private FileRead fr;
	//private int eg = 0; 
	//0-tetrahedron; 1-half dodecahedron; 2-cone with pentage as base; 3-truncated tetrahedron
	private boolean isAdjusted = true;
	
	public GamiThread(MainController ctrl, DataStructure ds, CommPortModem modem){
		super("Easigami 2012");
		this.ctrl = ctrl;
		this.ds = ds;
		portOpen = true;
		
		if(isReadingFile){
			try {
				fr = new FileRead(filename);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}else{
			this.modem = modem;
		}
				
		if (isWritingFile) {
			try {
				fw = new FileWrite(filename);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
		
	public void run(){
		while(portOpen){
			if(isDebug)System.out.println("GamiThread::Running thread");
			
			char startStr = 'a';
			int response = 0; // signed
		
			if(isReadingFile){
				//reading data from a file, and 
				//form the data structure based on the file
				ds.clearHingeVector();
				ds.clearPolygonVector();
				fr.readFile(ds);
			}else{
				//using the easigami TUI
				do{
					try {
						modem.send(startStr);
						response = modem.receive();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}while((char)response != startStr);
				if(isDebug)
					System.out.println("Master Echo succesfully: " + response);
		
				try {
					//traverse all hinges;
					ds.clearHingeVector();
					traverseHinges();
					this.ctrl.setHingeGui();//set hinge info to the HingeGui
					if(isWritingFile) fw.write2file("#\n");
			
					//traverse all polygons
					ds.clearPolygonVector();
					traversePolygons(AddressBook.Triangle_Equ_Str);
					traversePolygons(AddressBook.Triangle_Iso_Str);
					traversePolygons(AddressBook.Square_Str);
					traversePolygons(AddressBook.Pentagon_Str);
					traversePolygons(AddressBook.Hexagon_Str);
					if(isWritingFile) fw.write2file("#\n");
			
					//topology
					findTopology();
			
					//modem.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		
			if(isWritingFile)
				ds.printVectors(isWritingFile, fw);
			if(isWritingFile)fw.close();
			if(isReadingFile)fr.close();
		
			//System.out.println("Build the Adjacency Matrix.");
			ds.buildAdjacencyMatrix();
			if(isAdjusted){
				aa = new AdjustAngles(ds);
				aa.runNewton();
				//double a = Math.acos(1/3.0);
				//aa.setAdjustedAngles2Hinges(new double[]{a, a, a, a, a});
			}
			ds.setReady(true);
			if (!ctrl.isTestMode()) {
			    ctrl.getRenderer().refresh();
			    ctrl.getPattern().refresh();
			}
			//ds.setReady(false);
			//portOpen = false;
			System.out.println("\n&&&&&& &&&&&& &&&&&&\n");
			
			break;
		}
		
		//close the port while not running
		if(!isReadingFile){
			try {
				modem.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void traverseHinges() throws IOException{
		int response = 0; // signed
		Hinge h;
		
		//traverse all hinges
		if(isDebug) System.out.println("\nTraverse all hinges.* * * * * *");
		for(int i=0; i<AddressBook.HINGE_NUM; i++){
			//if(isDebug)System.out.print("0x" + Integer.toString(AddressBook.hingeAddress[i], 16));
			if(modem.receive()==1){// filter out the non-existent hinges
				response = modem.receive();
				if(isDebug)
					System.out.println("Hinge address: 0x" + Integer.toString(response, 16) + ": ");
				if(isWritingFile) 
					fw.write2file(Integer.toString(response, 16) + ": "); 
				h = new Hinge(response);
					
				response = modem.receive(); //pot1
				if(isDebug) System.out.print("Pot1: " + response + ", ");
				if(isWritingFile)fw.write2file(Integer.toString(response, 10) + ", ");
				h.setLeftPot(response);
				response = modem.receive(); //pot2
				if(isDebug) System.out.println("Pot2: " + response);
				if(isWritingFile)fw.write2file(Integer.toString(response, 10) + "\n");
				h.setRightPot(response);
				if(isDebug) System.out.println("The angle of the hinge: " + h.getRawAngleFromPot());
					
				//add a hinge to DS
				ds.add2HingeVector(h);
			}
		}
		if(isDebug) System.out.println("* * * * * *");
	}
	
	public void traversePolygons(String type) throws IOException{
		if(isDebug) System.out.println("\nTraverse " + type + ".");
		int num = 0;
		int response = 0;
		Polygon p;
		
		if(type.equals("Triangle_Equilateral")){
			num = AddressBook.TRIANGLE_EQU_NUM;
		}else if(type.equals("Triangle_Isosceles")){
			num = AddressBook.TRIANGLE_ISO_NUM;
		}else if(type.equals("Square")){
			num = AddressBook.SQUARE_NUM;
		}else if(type.equals("Pentagon")){
			num = AddressBook.PENTAGON_NUM;
		}else if(type.equals("Hexagon")){
			num = AddressBook.HEXAGON_NUM;
		}
		
		for(int i=0; i<num; i++){
			if(modem.receive()==1){// filter out the non-existent polygons
				response = modem.receive();
				if(isDebug)
					System.out.println(type + " address: 0x" + Integer.toString(response, 16));
				if(isWritingFile){
					fw.write2file(type + ", ");
					fw.write2file(Integer.toString(response, 16) + "\n");
				}
				p = new Polygon(type, response);
					
				if(isDebug) System.out.print("Connection: ");
				response = modem.receive(); //connection value
				if(isDebug)
					System.out.println(type + " address: " + Integer.toString(response, 2));
				p.setConnection(response);
					
				// add a polygon to DS
				ds.add2PolygonVector(p);
			}
		}
		
		if(isDebug) System.out.println("# # # # # #");
	}
	
	public void findTopology() throws IOException{
		Hinge h = null;
		Polygon p = null;
		int response = 0;
		int edgeIndex = -1;
		
		if(isDebug) System.out.println("\nFinding out the topology.");
		// traverse hinges: left -> right
		for(int i=0; i<ds.getHingeVector().size(); i++){ //outer loop: go through the hinge vector
			h = ds.getHingeVector().get(i);
			if(isDebug)System.out.println("Checking Hinge 0x" + Integer.toString(h.getAddress(), 16));
			
			// loop through all valid polygons for the left wing
			if(isDebug)System.out.println("loop through all valid polygons for the left wing...");
			for(int j=0; j<ds.getPolygonVector().size(); j++){//inner loop: go through the polygon vector
				p = ds.getPolygonVector().get(j);
				
				response = modem.receive();
				if(response == p.getAddress()){
					response = modem.receive();
					edgeIndex = p.getConnectionChange(response);
					if(isDebug) System.out.println("edgeIndex = " + edgeIndex);
					// if the edge index changes, the polygon 
					// is connected to the left wing of the hinge
					if(edgeIndex != -1 && edgeIndex != -2){
						if(isDebug)
							System.out.println("found one on the left of 0x" + Integer.toString(h.getAddress(), 16) + 
								": 0x" + Integer.toString(p.getAddress(), 16) + ", edge " + edgeIndex);
						//p.setCoordsPolygon3D(edgeIndex, true); // set coordinates of the polygon
						h.setLeftPolygon(p);
						h.setLeftPolygonIndex(edgeIndex);
					}
				}
			}
			
			// loop through all valid polygons for the right wing
			if(isDebug) System.out.println("loop through all valid polygons for the right wing...");
			for(int j=0; j<ds.getPolygonVector().size(); j++){
				p = ds.getPolygonVector().get(j);
				
				response = modem.receive();
				if(response == p.getAddress()){
					response = modem.receive();
					edgeIndex = p.getConnectionChange(response);
					if(isDebug) System.out.println("edgeIndex = " + edgeIndex);
					// if the edge index changes, the polygon 
					// is connected to the left wing of the hinge
					if(edgeIndex != -1 && edgeIndex != -2){
						if(isDebug)
							System.out.println("found one on the right of " + Integer.toString(h.getAddress(), 16) + 
								": " + Integer.toString(p.getAddress(), 16) + " " + edgeIndex);
						//p.setCoordsPolygon3D(edgeIndex, false); //set coordinates of the polygon
						h.setRightPolygon(p);
						h.setRightPolygonIndex(edgeIndex);
					}
				}
			}
		}
		
		if(isDebug)
			System.out.println("$ $ $ $ $ $\n");
	}
	
}
