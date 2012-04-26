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

	private String filename = "4triangles_flat.ezg";
	private boolean isWritingFile = false; // true - write to a file
	private FileWrite fw;
	// read an input file, instead of receiving data from Easigami

	private boolean isReadingFile = false;	//true - read a file
	private FileRead fr;
	private boolean isAdjusted = true;
	
	public GamiThread(MainController ctrl, DataStructure ds, CommPortModem modem){
		super("Easigami 2012");
		this.ctrl = ctrl;
		this.ds = ds;
		portOpen = true;

		if (ctrl.isTestMode()) {
		    isReadingFile = true;
		}
		
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
				System.out.println("is reading a TUI file");
				//reading data from a file, and 
				//form the data structure based on the file
				ds.clearHingeVector();
				ds.clearPolygonVector();
				fr.readFile(ds);
			}else{
				//using the easigami TUI
				System.out.println("using the Easigami TUI");
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
					if(isWritingFile)
						ds.printVectors(isWritingFile, fw);
					//modem.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		
			if(isWritingFile){
				//ds.printVectors(isWritingFile, fw);
				this.isWritingFile = false;
				fw.close();
				break;
			}
		
			//System.out.println("Build the Adjacency Matrix.");
			ds.buildAdjacencyMatrix();
			if(isAdjusted && ds.getPolygonVector().size()>2){
				aa = new AdjustAngles(ds);
				aa.runNewton();
			}
			ds.setReady(true);
			if (!ctrl.isTestMode()) {
				ctrl.getRenderer().setTUIMode(true);
			    ctrl.getRenderer().refresh();
			    ctrl.getPattern().refresh();
			}
			//ds.setReady(false);
			System.out.println("\n&&&&&& &&&&&& &&&&&&\n");

			if (ctrl.isTestMode()) {
			   System.exit(0);
			}
			
			if (this.isReadingFile){
				this.isReadingFile = false;
				fr.close();
				break;
			}
		}
		
		//close the port while not running
		if(modem != null){
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
	
	public void setReadingFile(String fn){
		filename = fn;
		try {
			fr = new FileRead(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		isReadingFile = true;
	}
	
	public void setWritingFile(String fn){
		filename = fn;
		isWritingFile = true;
		if (isWritingFile) {
			try {
				fw = new FileWrite(filename);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setModem(CommPortModem modem){
		this.modem = modem;
	}
}
