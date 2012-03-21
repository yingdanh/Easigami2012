package mainpack;


import java.io.IOException;
import java.util.Vector;

import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.UnsupportedCommOperationException;

import mygeom.VO3D;
import rw.writeSTL;

import gamicomm.CommPortModem;


public class MainController {
	private MainGui view;
	private CommPortModem modem;
	//private PortChooser menu;
	private Renderer render3d;
	private Pattern pattern2d;
	//private boolean isConnected;
	private GamiThread gamithread = null;
	private DataStructure ds;
	private boolean testMode;
	public boolean isTestMode() {
	  return testMode;
	}
	
	public MainController(boolean testMode){
	  this.testMode = testMode;
		AddressBook lookup = AddressBook.createAddressBook(); //the pre-stored lookup table
		
		ds = new DataStructure();
		view = new MainGui(this);
		view.openDbox();
		//menu = view.getPortChooser();
		render3d = view.getRenderer();
		pattern2d = view.getPattern();
	}
	
	//connect to the serial port that a user selected 
	//start the thread to talk to the easigami TUI or read a file
	public void connect2Serial(boolean testMode) {
		String portName;
		if (!testMode) {
		do {
			portName = view.getSelectedName();
			if (portName == null)
				System.out.println("No port selected. Try again.\n");
		} while (portName == null);
		
		System.out.println("port name: " + portName);
		
		try {
			modem = new CommPortModem(view.getSelectedName(), view.getSelectedIdentifier());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchPortException e) {
			e.printStackTrace();
		} catch (PortInUseException e) {
			e.printStackTrace();
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
		}
		}
		
		gamithread = new GamiThread(this, ds, modem);
		gamithread.start();
	}
	
	@SuppressWarnings("deprecation")
	public void stopSerial() throws IOException{
		gamithread.stop();
		modem.close();
	}
	
	public DataStructure getDataStructure(){
		return ds;
	}
	
	public void generateSTL() throws Exception{
		Vector<Polygon> polygons = ds.getPolygonVector();
		Polygon poly;
		writeSTL stlw = null;

		System.out.println(polygons.size());
		for(int i=0; i<polygons.size(); i++){//for each polygon
			System.out.println("calculateAbsVertices");
			poly = polygons.get(i);
			System.out.println("0x" + Integer.toHexString(poly.getAddress()));
			VO3D.printMatrix16(poly.getMatrix3D());
			poly.calculateAbsVertices();
		}
		
		//print out polygon info
		for(int i=0; i<polygons.size(); i++){
			poly = polygons.get(i);
			System.out.println(poly);
		}
		
		try {
			stlw = new writeSTL("mySTL_test_12.stl", polygons);
			stlw.generateSTL();
			stlw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * called in display function by render3d 
	 * @param curmat3d
	 */
	/*public void setRendererInfo(GL gl, double[] curmat3d){
		pattern2d.setRendererInfo(gl, curmat3d);
	}
	
	//called in display function by render3d
	public void refreshPattern(){
		pattern2d.refresh();
	}
	
	//precondition: the MainGui view has been initialized
	public void connect(){	
		System.out.println("MainController: connecting....");
		potthread = new PotsThread(this, menu, render3d, pattern2d);
		//potthread.connect2Serial();
		
		if(!potthread.isAlive()){
			potthread.start();
		}
		
		isConnected = true;
	}
	
	public void disconnect() {
		potthread.stopThread();
		isConnected = false;
	}
	
	public boolean isConnected(){
		return isConnected;
	}*/
	
	public void setHingeGui(){
		if(view.getHingeGui() == null) return;
		
		view.getHingeGui().setText(ds.getHingeVector());
	}
	
	public Renderer getRenderer(){
		return render3d;
	}
	
	public Pattern getPattern(){
		return pattern2d;
	}
}
