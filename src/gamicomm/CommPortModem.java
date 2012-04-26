package gamicomm;
import java.awt.*;
import java.io.*;

import javax.comm.*;

import java.util.*;

/**
 * Subclasses CommPortOpen and adds send/expect handling for dealing with
 * Hayes-type modems.
 * 
 */
public class CommPortModem extends CommPortOpen {
	/** The last line read from the serial port. */
	protected int response;
	/** A flag to control debugging output. */
	protected boolean debug = true;
	
	// The following variables are for the debugging purpose
	private final boolean checkTime = false;
	private Date startTime, timeStamp;
	private long elapsed_time;

	public CommPortModem(String portName, CommPortIdentifier thePortID) throws IOException, NoSuchPortException,
			PortInUseException, UnsupportedCommOperationException {
		super(portName, thePortID);
	}

	/**
	 * Send a line to a PC-style modem. Send \r\n, regardless of what platform
	 * we're on, instead of using println().
	 */
	public void send(char s) throws IOException {
		if (debug) {
			System.out.print(">>> ");
			System.out.print(s);
			System.out.println();
		}
		os.print(s);
		
		//os.print("\r\n");
		// Expect the modem to echo the command.
		/*if (!expect(s)) {
			System.err.println("WARNING: Modem did not echo command.");
		}else{
			System.out.println("Modem echoes command.");
		}*/
		// The modem sends an extra blank line by way of a prompt.
		// Here we read and discard it.
		/*String junk = os.readLine();
		if (junk.length() != 0) {
			System.err.print("Warning unexpected response: ");
			System.err.println(junk);
		}*/
	}

	/**
	 * Read a byte, saving it in "response".
	 * 
	 * @return the received byte
	 */
	public int receive() throws IOException {
		System.out.println("here!");
		response = is.readUnsignedByte();
		System.out.println("there!");
		if (debug) {
			System.out.print("<<< ");
			System.out.print(response);
			System.out.println();
		}
		
		return response;
	}
		
	public void converse() throws IOException {
		System.out.println("In CommPortDial");
		String resp;// the modem response.
		
		startTime = new Date();
		// Send the reset command
		while(true){
			System.out.println("one iteration");
			send('a');
			receive(); //echo
			
			if(receive() == 1){ //check trash
				System.out.print("Hinge address: ");
				receive(); //hinge address
				System.out.print("Pot1: ");
				receive(); //pot1
				System.out.print("Pot2: ");
				receive(); //pot2
				System.out.print("Send value to hinge: ");
				receive(); //echo data
			}
			
			if(receive() == 1){ //check trash
				System.out.print("Polygon address: ");
				receive(); //polygon address
				System.out.print("connection: ");
				receive(); //connection
			}
			
			System.out.println("*** *** ***");
		}
	}
}