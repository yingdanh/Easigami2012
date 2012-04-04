package rw;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

public class readSTL {
	private String filename = "";
	private DataInputStream in;
	private BufferedReader br;
	
	public readSTL(String fn) throws FileNotFoundException{
		filename = fn;
		FileInputStream fstream = new FileInputStream(filename);
	    // Get the object of DataInputStream
	    in = new DataInputStream(fstream);
	    br = new BufferedReader(new InputStreamReader(in));
	}
}
