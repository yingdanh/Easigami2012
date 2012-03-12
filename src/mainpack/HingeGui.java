package mainpack;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class HingeGui extends JFrame{
	private static final int HINGE_GUI_WIDTH =  400;
	private static final int HINGE_GUI_HEIGHT = 600;
	
	private MainController ctrl;
	private JTextArea hingeInfo;
	private DataStructure ds;
	
	public HingeGui(MainController control) {
		super("Hinge Infomation");
		this.ctrl = control;
		this.ds = this.ctrl.getDataStructure();
	}
	
	protected JComponent createHingePane(){
		JPanel hingePanel = new JPanel();
		hingePanel.setLayout(new BorderLayout());
		
		hingeInfo = new JTextArea();
		hingeInfo.setBackground(Color.black);
		hingeInfo.setForeground(Color.yellow);
		JScrollPane scrollPane = new JScrollPane(hingeInfo, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		hingePanel.add(hingeInfo, BorderLayout.CENTER);

		return hingePanel;
	}
	
	public void setText(Vector<Hinge> hs){
		Hinge h;
		for(int i=0; i<hs.size(); i++){
			h = hs.get(i);
			hingeInfo.setText("0x" + Integer.toString(h.getAddress(), 16) + "____" + 
			h.getLeftPot() + "____" + h.getRightPot() + "____" + h.getAngleDeg() + 
			"____" + h.getAngle(false) + "\n");
		}
	}
	
	public void openDbox(){
		Container cp = getContentPane();
		cp.add(this.createHingePane());
		
		this.setSize(HINGE_GUI_WIDTH, HINGE_GUI_HEIGHT);
		this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                dispose();
            }
        });
        this.setVisible(true);
	}

}
