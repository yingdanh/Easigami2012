package mainpack;

import java.io.IOException;

import javax.comm.NoSuchPortException;
import javax.comm.PortInUseException;
import javax.comm.UnsupportedCommOperationException;
import javax.swing.UIManager;

public class MainDriver {
	public static void main(String[] args) 
		throws IOException, NoSuchPortException, PortInUseException,
			UnsupportedCommOperationException{
		System.out.println("Easigami.");
		try {
			   //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel" );
			   UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel" );

			}
		catch (Exception e) { }

		MainController application = new MainController();
		//application.communicate();
	}

}
