package mainpack;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Vector;

import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.UnsupportedCommOperationException;

import mygeom.FacetSTL;
import mygeom.SolidSTL;
import mygeom.VO3D;
import rw.readSTL;
import rw.writeSTL;

import gamicomm.CommPortModem;

public class MainController {
	private MainGui view;
	private CommPortModem modem = null;
	private SolidsSTL solids;
	private Renderer render3d;
	private Pattern pattern2d;
	// private boolean isConnected;
	private GamiThread gamithread = null;
	private DataStructure ds;
	private boolean testMode;

	public boolean isTestMode() {
		return testMode;
	}

	public MainController(boolean testMode) {
		this.testMode = testMode;
		AddressBook lookup = AddressBook.createAddressBook(); // the pre-stored
																// lookup table

		ds = new DataStructure();
		view = new MainGui(this);
		view.openDbox();
		solids = new SolidsSTL();
		render3d = view.getRenderer();
		pattern2d = view.getPattern();
	}

	// connect to the serial port that a user selected
	// start the thread to talk to the easigami TUI or read a file
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
				modem = new CommPortModem(view.getSelectedName(),
						view.getSelectedIdentifier());
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
	}

	public void startTuiThread() {
		System.out.println("in startTuiThread");
		// if(gamithread == null){
		gamithread = new GamiThread(this, ds, modem);
		// }else {
		// gamithread.setModem(modem);
		// }
		// System.out.println(gamithread.getState());
		gamithread.start();
	}

	@SuppressWarnings("deprecation")
	public void stopSerial() throws IOException {
		gamithread.stop();
		modem.close();
	}

	public DataStructure getDataStructure() {
		return ds;
	}
	
	public SolidsSTL getSolids(){
		return this.solids;
	}

	public void generateSTL(String filename) throws Exception {
		Vector<Polygon> polygons = ds.getPolygonVector();
		Polygon poly;
		writeSTL stlw = null;

		System.out.println(polygons.size());
		for (int i = 0; i < polygons.size(); i++) {// for each polygon
			System.out.println("calculateAbsVertices");
			poly = polygons.get(i);
			System.out.println("0x" + Integer.toHexString(poly.getAddress()));
			VO3D.printMatrix16(poly.getMatrix3D());
			poly.calculateAbsVertices();
		}

		// print out polygon info
		for (int i = 0; i < polygons.size(); i++) {
			poly = polygons.get(i);
			System.out.println(poly);
		}

		try {
			stlw = new writeSTL("stl/" + filename, polygons);
			stlw.generateSTL();
			stlw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//read .stl file into solids data structure
	public void loadSTL(String filename) throws Exception {
		System.out.println("in MainController::loadSTL - " + filename);
		
		readSTL stlr = new readSTL("stl/" + filename);
		Vector<SolidSTL> ns = stlr.readingSTL(); //read in new solids
		solids.append(ns); //add the new solid to the list
		stlr.close();
		
		render3d.setSolids(solids.getSolids());
		render3d.setSTLMode(true);
		render3d.refresh();
	}
	
	public void clearSolids(){
		solids.clearSolids();
		render3d.refresh();
	}
	
	public void deleteSolid(int index){
		solids.deleteSolidAt(index);
	}
	
	public void setDeletionMode(boolean tf){
		render3d.setDeletionMode(tf);
		render3d.refresh();
	}

	public void setHingeGui() {
		if (view.getHingeGui() == null)
			return;

		view.getHingeGui().setText(ds.getHingeVector());
	}

	public Renderer getRenderer() {
		return render3d;
	}

	public Pattern getPattern() {
		return pattern2d;
	}

	public void setReadingTui(String filename) {
		// if(gamithread == null)
		gamithread = new GamiThread(this, ds, modem);
		gamithread.setReadingFile("tui/" + filename);
		gamithread.start();
	}

	public void setWritingTui(String filename) {
		// if(gamithread == null)
		gamithread = new GamiThread(this, ds, modem);
		gamithread.setWritingFile("tui/" + filename);
		gamithread.start();
	}
}
