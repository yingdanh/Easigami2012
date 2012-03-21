package mainpack;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.EtchedBorder;

import javax.comm.CommPortIdentifier;
import javax.media.opengl.*;


public class MainGui extends JFrame{
	private static final int WIN_WIDTH = 1200;
	private static final int WIN_HEIGHT = 600;
	
	private MainController ctrl;
	//private PortChooser menu;
	private Renderer renderer3d = null;
	private Pattern pattern2d = null;
	
	//components related to serial connection
	protected HashMap map = new HashMap();
	protected JComboBox serialPortsChoice;
	/** The name of the choice the user made. */
	protected String selectedPortName;
	/** The CommPortIdentifier the user chose. */
	protected CommPortIdentifier selectedPortIdentifier;
	private JButton serialStartBtn;
	private JButton serialStopBtn;
	
	private JToolBar toolBar;
	private JTextArea prompt;
	private JButton stlBtn;
	private HingeGui hingeGui;
	private boolean isDebug;
	
	public MainGui(MainController control) {
		super("Easigami: Tangible Paper Folding (2009 Summer)");
		isDebug = false;
		this.ctrl = control;
	}
	
	protected JComponent createMainPane(){
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		mainPanel.add(createTools(), BorderLayout.NORTH);
		mainPanel.add(createCanvases(), BorderLayout.CENTER);
		mainPanel.add(createPrompt(), BorderLayout.SOUTH);


		if(isDebug) System.out.println("The end of creatMainPane()");
		return mainPanel;
	}
	
	private JTextArea createPrompt(){
		prompt = new JTextArea("Status: ", 2, 50);
		prompt.setForeground(Color.white);
		prompt.setBackground(new Color(0, 0, 205));
		
		return prompt;
	}
	
	private JComponent createTools(){
		JPanel toolPanel = new JPanel();
		toolPanel.setLayout(new GridLayout(1, 4));
		
		toolPanel.add(createInteractionTools());
		toolPanel.add(createOperationTools());
		toolPanel.add(createFileTools());
		toolPanel.add(createMyTools());
		
		return toolPanel;
	}
	
	private JComponent createInteractionTools(){
		JToolBar tb = new JToolBar();
		tb.setBackground(new Color(255, 215, 0));
		//tb.setBorder(BorderFactory.createLoweredBevelBorder());
		
		//select serial port
		serialPortsChoice = new JComboBox();
		serialPortsChoice.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				selectedPortName = (String) ((JComboBox) e.getSource()).getSelectedItem();
				selectedPortIdentifier = (CommPortIdentifier) map.get(selectedPortName);
			}
			
		});
		
		//start serial communication
		serialStartBtn = new JButton("Start");
		serialStartBtn.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent event) {
	    		if(selectedPortName != null){
					setPrompt(selectedPortName);
					ctrl.connect2Serial();
				}
            }
        });
		
		//stop serial communication
		serialStopBtn = new JButton("Stop");
		serialStopBtn.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent event) {
	    		if(selectedPortName != null){
					try {
						ctrl.stopSerial();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
            }
        });
		
		tb.add(serialPortsChoice);
		tb.add(serialStartBtn);
		tb.add(serialStopBtn);
		populate();

		return tb;
	}
	
	private JComponent createOperationTools(){
		JToolBar tb = new JToolBar();
		tb.setBackground(new Color(255, 215, 0));
		//tb.setBorder(BorderFactory.createLoweredBevelBorder());
		
		return tb;
	}
	
	private JComponent createFileTools(){
		JToolBar tb = new JToolBar();
		tb.setBackground(new Color(255, 215, 0));
		//tb.setBorder(BorderFactory.createLoweredBevelBorder());
		
		stlBtn = new JButton("STL");
		stlBtn.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent event) {
	    		try {
					ctrl.generateSTL();
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
        });
				
		tb.add(stlBtn);
		return tb;
	}
	
	private JComponent createMyTools(){
		JToolBar tb = new JToolBar();
		tb.setBackground(new Color(255, 215, 0));
		//tb.setBorder(BorderFactory.createLoweredBevelBorder());
		
		JButton monitorHingeBtn = new JButton("Monitor Hinges");
		monitorHingeBtn.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent event) {
	    		hingeGui = new HingeGui(ctrl);
	    		hingeGui.openDbox();
            }
        });
		
		tb.add(monitorHingeBtn);
		return tb;
	}
	
	/**
	 * Populate the ComboBoxes by asking the Java Communications API what ports
	 * it has. Since the initial information comes from a Properties file, it
	 * may not exactly reflect your hardware.
	 */
	protected void populate() {
		// get list of ports available on this particular computer,
		// by calling static method in CommPortIdentifier.
		Enumeration pList = CommPortIdentifier.getPortIdentifiers();
		// Process the list, putting serial and parallel into ComboBoxes
		while (pList.hasMoreElements()) {
			CommPortIdentifier cpi = (CommPortIdentifier) pList.nextElement();
			map.put(cpi.getName(), cpi);
			if (cpi.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				serialPortsChoice.setEnabled(true);
				serialPortsChoice.addItem(cpi.getName());
			} 
		}
		serialPortsChoice.setSelectedIndex(-1);
		//parallelPortsChoice.setSelectedIndex(-1);
	}
	
	/* The public "getter" to retrieve the chosen port by name. */
	public String getSelectedName() {
		return selectedPortName;
	}

	/* The public "getter" to retrieve the selection by CommPortIdentifier. */
	public CommPortIdentifier getSelectedIdentifier() {
		return selectedPortIdentifier;
	}
	
	private JComponent createCanvases(){
		JSplitPane twoPanes = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createCanvas3D(), createCanvas2D());
		twoPanes.setDividerSize(10);
		twoPanes.setDividerLocation(WIN_WIDTH/2);

		return twoPanes;
	}
	
	private JComponent createCanvas2D(){		
		JPanel patternPanel = new JPanel();
		patternPanel.setLayout(new BorderLayout());
		patternPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "2D Pattern"));
		
		GLCapabilities capabilities = new GLCapabilities();
        capabilities.setHardwareAccelerated(true); //We want hardware acceleration
        capabilities.setDoubleBuffered(true);      //And double buffer
        
        GLCanvas canvas = new GLCanvas(capabilities);
        canvas.requestFocus();
        pattern2d = new Pattern(canvas, ctrl);
        
        canvas.addGLEventListener(pattern2d);
        canvas.addMouseListener(pattern2d);
        canvas.addMouseMotionListener(pattern2d);
        
        if(isDebug) System.out.println("The end of creatCanvas2D()");
        
        patternPanel.add(canvas, BorderLayout.CENTER);
		return patternPanel;
	}
	
	private JComponent createCanvas3D(){
		JPanel gamePanel = new JPanel();
		gamePanel.setLayout(new BorderLayout());
		gamePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "3D Form Explorer"));
		
		GLCapabilities capabilities = new GLCapabilities();
        capabilities.setHardwareAccelerated(true); //We want hardware acceleration
        capabilities.setDoubleBuffered(true);      //And double buffer
        
        GLCanvas canvas = new GLCanvas(capabilities);
        canvas.requestFocus();
        renderer3d = new Renderer(canvas, ctrl);
        
        canvas.addGLEventListener(renderer3d);
        canvas.addMouseListener(renderer3d);
        canvas.addMouseMotionListener(renderer3d);
        
        if(isDebug) System.out.println("The end of creatCanvas3D()");
        
        gamePanel.add(canvas, BorderLayout.CENTER);
		return gamePanel;
	}
	
	public void setPrompt(String s){
		if(prompt != null)
			prompt.setText(s);
	}	
	
	public void openDbox(){
		Container cp = getContentPane();
		cp.add(this.createMainPane());
		
		this.setSize(WIN_WIDTH, WIN_HEIGHT);
		this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                System.exit(0);
            }
        });
        this.setVisible(true);
        if(isDebug) System.out.println("The end of openDBox()");
	}
	
	public Renderer getRenderer(){
		return renderer3d;
	}
	
	public Pattern getPattern(){
		return pattern2d;
	}
	
	public HingeGui getHingeGui(){
		return hingeGui;
	}
}

