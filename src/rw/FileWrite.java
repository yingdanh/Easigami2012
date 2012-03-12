package rw;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class FileWrite {
	private String filename = "";
	private BufferedWriter out;
	
	public FileWrite(String fn) throws IOException{
		filename = fn;
		File f = new File(filename);
	    if(!f.exists()){
	    	// Create file 
	    	FileWriter fstream = new FileWriter(f);
	    	out = new BufferedWriter(fstream);
	    }
	}
	
	public void write2file(String str){
		try{
		    out.write(str);
		}catch (Exception e){//Catch exception if any
			System.err.println("Write Error: " + e.getMessage());
		}
	}
	
	// Close the output stream
	public void close(){
		System.out.println("Close the output file.");
		try{
			out.close();
		}catch(Exception e){//Catch exception if any
			System.err.println("Close Error: " + e.getMessage());
		}
	}
}
