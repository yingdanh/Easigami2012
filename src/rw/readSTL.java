package rw;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;

import mygeom.FacetSTL;
import mygeom.SolidSTL;

public class readSTL {
	private String filename = "";
	private DataInputStream in;
	private BufferedReader br;
	private Vector<SolidSTL> solids;
	private boolean isDebug = true;
	
	
	public readSTL(String fn) throws FileNotFoundException {
		filename = fn;
		FileInputStream fstream = new FileInputStream(filename);
		// Get the object of DataInputStream
		in = new DataInputStream(fstream);
		br = new BufferedReader(new InputStreamReader(in));
		solids = new Vector<SolidSTL>();
	}

	public Vector<SolidSTL> readingSTL() {
		if (isDebug)
			System.out.println("Reading the STL file: " + filename);

		SolidSTL solid;
		StringTokenizer st = null;
		String strLine = null, word = null;

		try {
			//Read File Line By Line
			strLine = br.readLine();
		    while(strLine != null && strLine.trim().length() != 0) {
		    	//System.out.println("strLine: " + strLine);
		    	st = new StringTokenizer(strLine, " ");
		    	if(st.hasMoreTokens()){
		    		//init a solid with name
		    		word = st.nextToken();
		    		if(word.equals("solid")){
		    			solid = processSolid(st, st.nextToken());
		    			solids.add(solid);
		    		}
		    	}
		    	
		    	strLine = br.readLine();
		    }
		} catch (Exception e) {
			System.err
					.println("Error when reading STL file: " + e.getMessage());
		}
		
		return solids;
	}
	
	public SolidSTL processSolid(StringTokenizer st, String solidname){
		//System.out.println("in readSTL::processSolid");
		String strLine = null, word = null;
		SolidSTL solid = new SolidSTL(solidname);
		FacetSTL face = null;
		
		try {
			//Read File Line By Line
			strLine = br.readLine();
		    while(!strLine.equals("endsolid")) {
		    	//System.out.println("strLine: " + strLine);
		    	if(strLine.equals("outer loop") || strLine.equals("endloop")){
		    		strLine = br.readLine();
		    		continue;
		    	}
		    	
		    	if(strLine.equals("endfacet")){
		    		//System.out.println("one face is added");
		    		solid.addFace(face);
		    	}
		    	
		    	st = new StringTokenizer(strLine, " ");
		    	if(st.hasMoreTokens()){
		    		//init a solid with name
		    		word = st.nextToken();
		    		if(word.equals("facet")){
		    			face = new FacetSTL();
		    			word = st.nextToken(); //skip "normal"
		    			face.setNormal(Double.parseDouble(st.nextToken()), 
		    					Double.parseDouble(st.nextToken()), 
		    					Double.parseDouble(st.nextToken()));
		    		}else if(word.equals("vertex")){
		    			face.addVertex(Double.parseDouble(st.nextToken()), 
		    					Double.parseDouble(st.nextToken()), 
		    					Double.parseDouble(st.nextToken()));
		    		}
		    	}
		    	
		    	strLine = br.readLine();
		    }
		}catch (Exception e) {
			System.err
			.println("Error when reading STL file: " + e.getMessage());
		}
		
		return solid;
	}

	// Close the input stream
	public void close() {
		if (isDebug)
			System.out.println("Close the input file.");
		try {
			in.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Close Error: " + e.getMessage());
		}
	}
}
