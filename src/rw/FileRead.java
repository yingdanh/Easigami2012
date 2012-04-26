package rw;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;

import mainpack.DataStructure;
import mainpack.Hinge;
import mainpack.Polygon;


public class FileRead {
	private String filename = "";
	private DataInputStream in;
	private BufferedReader br;
	private Vector<Hinge> hvec;
	private boolean isDebug = false; 
	
	public FileRead(String fn) throws FileNotFoundException{
		filename = fn;
		// Open the file that is the first 
	    // command line parameter
	    FileInputStream fstream = new FileInputStream(filename);
	    // Get the object of DataInputStream
	    in = new DataInputStream(fstream);
	    br = new BufferedReader(new InputStreamReader(in));
	}
	
	/*public String readOneLine(){
		System.out.println("Reading one line: " + filename);
		String strLine = "";
		try{   
		    //Read File Line By Line
		    if ((strLine = br.readLine()) != null)   {
		    	return strLine;
		    }
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		
		return null;
	}*/
	
	/**
	 * 
	 * @param dummy: folding model
	 */
	public void readFile(DataStructure dummy){
		if(isDebug)
			System.out.println("Reading the file: " + filename);
		StringTokenizer st = null;;
		String strLine=null, word1=null, word2=null;
		Hinge h = null;
		Polygon p = null;
		int i = 0;	//index to loop through hinges
		
		try{   
		    //Read File Line By Line
			strLine = br.readLine();
		    while (strLine != null && strLine.trim().length() != 0)   {
		    	// each iteration reads in a folding model
		    	// Print the content on the console
		  
		    	// read hinges and angles
		    	if(isDebug)
		    		System.out.println("FileRead: Hinges: " + strLine);
		    	while (!strLine.trim().equals("#")){
		    		st = new StringTokenizer(strLine, " :,");
		    		if(st.hasMoreTokens()){	// create a hinge with an address
		    			word1 = st.nextToken();
		    			if(isDebug)
		    				System.out.print("(" + word1 + ", ");
		    			h = new Hinge(Integer.parseInt(word1, 16));	
		    		}
		    		if(st.hasMoreTokens()){	// set the left pot for the hinge
		    			word1 = st.nextToken();
		    			if(isDebug)
		    				System.out.println(word1 + ", ");
		    			h.setLeftPot(Integer.parseInt(word1));
		    		}
		    		if(st.hasMoreTokens()){	// set the left pot for the hinge
		    			word1 = st.nextToken();
		    			if(isDebug)
		    				System.out.println(word1 + ")");
		    			h.setRightPot(Integer.parseInt(word1));
		    		}
		    		dummy.add2HingeVector(h);		// add the hinge to the folding model     
		    		strLine = br.readLine();
		    	}
		    	if(isDebug)
		    		System.out.println("FileRead: Done with reading Hinges' addresses and angles");
		    	
		    	// read polygons
		    	strLine = br.readLine();
		    	if(isDebug)
		    		System.out.println("FileRead: Polygons: " + strLine);
		    	while (!strLine.trim().equals("#")){
		    		st = new StringTokenizer(strLine, " :,");
		    		if(st.hasMoreTokens()){	
		    			word1 = st.nextToken();
		    			if(isDebug)
		    				System.out.println("Polygon type: " + word1);
		    		}
		    		if(st.hasMoreTokens()){	// create a hinge with an address
		    			word2 = st.nextToken();
		    			if(isDebug)
		    				System.out.println("Polygon address: " + word2);
		    			p = new Polygon(word1, Integer.parseInt(word2, 16));	
		    		}

		    		dummy.add2PolygonVector(p);		// add the hinge to the folding model     
		    		strLine = br.readLine();
		    	}
		    	if(isDebug)
		    		System.out.println("FileRead: Done with reading Polygons' addresses");
		    	
		    	//Topology
		    	hvec = dummy.getHingeVector();
		    	strLine = br.readLine();
		    	if(isDebug)
		    		System.out.println("FileRead: Topology: " + strLine);
		    	// read for topology
		    	while (strLine != null && strLine.trim().length() != 0){
		    		if(i==hvec.size()) break;
		    		h = hvec.get(i);
		    		
		    		st = new StringTokenizer(strLine, " :,;");
		    		if(st.hasMoreTokens()){	// check a hinge address
		    			word1 = st.nextToken();
		    			if(isDebug) System.out.print("[" + word1 + ", ");
		    		}
		    		if(st.hasMoreTokens()){	// set the left polygon for the hinge
		    			word1 = st.nextToken();
		    			if(isDebug) System.out.print(word1 + ", ");
		    			h.setLeftPolygon(dummy.getPolygonAt(Integer.parseInt(word1, 16)));
		    		}
		    		if(st.hasMoreTokens()){	// set the left index for the hinge
		    			word2 = st.nextToken();
		    			if(isDebug) System.out.print(word2 + ", ");
		    			h.setLeftPolygonIndex(Integer.parseInt(word2));
		    		}
		    		if(st.hasMoreTokens()){	// set the right polygon for the hinge
		    			word1 = st.nextToken();
		    			if(isDebug) System.out.print(word1 + ", ");
		    			h.setRightPolygon(dummy.getPolygonAt(Integer.parseInt(word1, 16)));
		    		}
		    		if(st.hasMoreTokens()){	// set the right index for the hinge
		    			word2 = st.nextToken();
		    			if(isDebug) System.out.println(word2 + "]");
		    			h.setRightPolygonIndex(Integer.parseInt(word2));
		    		} 
		    		
		    		strLine = br.readLine();
		    		i++;
		    	}
		    	if(isDebug){
		    		System.out.println("FileRead: Done with Topology");
		    		System.out.println();
		    	}
		    }
		}catch (Exception e){//Catch exception if any
			System.err.println("Error when reading the file: " + e.getMessage());
			//System.exit(1);
		}
		
	}
	
	// Close the input stream
	public void close(){
		if(isDebug)System.out.println("Close the input file.");
		try{
			in.close();
		}catch(Exception e){//Catch exception if any
			System.err.println("Close Error: " + e.getMessage());
		}
	}
}
