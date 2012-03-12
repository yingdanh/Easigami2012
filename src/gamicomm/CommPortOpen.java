package gamicomm;
import java.awt.*;
import java.io.*;

import javax.comm.*;

import java.util.*;

/**
 * Open a serial port using Java Communications.
 * 
 */
public class CommPortOpen {
	/** How long to wait for the open to finish up. */
	public static final int TIMEOUTSECONDS = 30;
	/** The baud rate to use. */
	public static final int BAUD = 115200;
	/** The parent Frame, for the chooser. */
	protected Frame parent;
	/** The input stream */
	protected DataInputStream is;
	/** The output stream */
	protected PrintStream os;
	/** The last line read from the serial port. */
	protected String response;
	/** A flag to control debugging output. */
	protected boolean debug = true;
	/** The chosen Port Identifier */
	CommPortIdentifier thePortID;
	/** The chosen Port itself */
	CommPort thePort;
	SerialPort myPort;

	/*public static void main(String[] argv) throws IOException,
			NoSuchPortException, PortInUseException,
			UnsupportedCommOperationException {
		new CommPortOpen(null).converse();
		System.exit(0);
	}*/

	/* Constructor */
	public CommPortOpen(String portName, CommPortIdentifier thePortID) 
			throws IOException, NoSuchPortException,
			PortInUseException, UnsupportedCommOperationException {
		
		// Now actually open the port.
		// This form of openPort takes an Application Name and a timeout.
		//
		System.out.println("Trying to open " + thePortID.getName() + "...");
		switch (thePortID.getPortType()) {
		case CommPortIdentifier.PORT_SERIAL:
			thePort = thePortID.open("DarwinSys DataComm",
					TIMEOUTSECONDS * 1000);
			myPort = (SerialPort) thePort;
			// set up the serial port
			myPort.setSerialPortParams(BAUD, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			break;
		default:// Neither parallel nor serial??
			throw new IllegalStateException("Unknown port type " + thePortID);
		}
		// Get the input and output streams
		// Printers can be write-only
		try {
			is = new DataInputStream(thePort.getInputStream());
		} catch (IOException e) {
			System.err.println("Can't open input stream: write-only");
			is = null;
		}
		os = new PrintStream(thePort.getOutputStream(), true);
	}
	
	public void close () throws IOException{
		myPort.close();
		if (is != null)
			is.close();
		if (os != null)
			os.close();
	}

	/**
	 * This method will be overridden by non-trivial subclasses to hold a
	 * conversation.
	 */
	protected void converse() throws IOException {
		System.out.println("Ready to read and write port.");
		// Input/Output code not written -- must subclass.
		// Finally, clean up.
		if (is != null)
			is.close();
		os.close();
	}
}
