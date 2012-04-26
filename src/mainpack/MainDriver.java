package mainpack;

import javax.swing.UIManager;

public class MainDriver {
	public static void main(String[] args) {
		System.out.println("Easigami 2012.");
		try {
			   //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel" );
			   UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel" );

			}
		catch (Exception e) { }
		boolean testMode = args.length > 0 && args[0].equals("test");

		MainController application = new MainController(testMode);
		//application.communicate();
	}

}
