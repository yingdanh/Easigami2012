package mainpack;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import javax.comm.CommPortIdentifier;
import javax.media.opengl.*;

public class MainGui extends JFrame {
	private static final int WIN_WIDTH = 1400;
	private static final int WIN_HEIGHT = 800;

	private MainController ctrl;
	// private PortChooser menu;
	private Renderer renderer3d = null;
	private Pattern pattern2d = null;

	// components related to serial connection
	protected HashMap map = new HashMap();
	protected JComboBox serialPortsChoice;
	/** The name of the choice the user made. */
	protected String selectedPortName;
	/** The CommPortIdentifier the user chose. */
	protected CommPortIdentifier selectedPortIdentifier;
	private JButton serialStartBtn;
	private JButton serialStopBtn;
	private JButton stlBtn;
	private JCheckBox chBox;

	private JToolBar toolBar;
	private JTextArea prompt;
	private JButton loadBtn;
	private JButton screenshotBtn;
	private JFileChooser fcScreenshot;
	private JButton clearBtn;
	private JFileChooser fcStl;
	private JButton readTuiBtn;
	private JButton writeTuiBtn;
	private JFileChooser fcTui;
	private JButton monitorHingeBtn;
	private HingeGui hingeGui;
	//private JToggleButton face2faceBtn;
	private JButton edge2edgeBtn;
	//private JToggleButton moveBtn;
	//private JToggleButton scaleBtn;
	//private JToggleButton rotateBtn;
	private JToggleButton[] toggles;
	// private JButton repeatBtn;
	private boolean isDebug;
	

	public MainGui(MainController control) {
		super("Easigami: Tangible Paper Folding (2012 Spring)");
		isDebug = false;
		this.ctrl = control;
		toggles = new JToggleButton[5];
	}

	protected JComponent createMainPane() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		mainPanel.add(createTools(), BorderLayout.NORTH);
		mainPanel.add(createCanvases(), BorderLayout.CENTER);
		mainPanel.add(createPrompt(), BorderLayout.SOUTH);

		if (isDebug)
			System.out.println("The end of creatMainPane()");
		return mainPanel;
	}

	private JTextArea createPrompt() {
		prompt = new JTextArea("Status: ", 2, 50);
		prompt.setForeground(Color.white);
		prompt.setBackground(new Color(0, 0, 205));

		return prompt;
	}

	private JComponent createTools() {
		JPanel toolPanel = new JPanel();
		toolPanel.setLayout(new GridLayout(2, 4, 5, 5));

		toolPanel.add(createInteractionTools());
		toolPanel.add(createCompoundTools());
		toolPanel.add(createSharedTools());
		toolPanel.add(createMyTools());

		return toolPanel;
	}

	private JComponent createInteractionTools() {
		JToolBar tb = new JToolBar();
		tb.setBackground(new Color(255, 215, 0));
		final boolean testMode = ctrl.isTestMode();
		if (testMode) {
			ctrl.connect2Serial(testMode);
			ctrl.startTuiThread();
		}
		tb.setBorder(BorderFactory.createTitledBorder("Easigami Tangible User Interface"));

		// select serial port
		serialPortsChoice = new JComboBox();
		serialPortsChoice.setToolTipText("Select serial port for Easigami");
		serialPortsChoice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectedPortName = (String) ((JComboBox) e.getSource())
						.getSelectedItem();
				selectedPortIdentifier = (CommPortIdentifier) map
						.get(selectedPortName);
			}

		});

		// start serial communication
		serialStartBtn = new JButton(getImageIcon
				("C:\\Documents and Settings\\Yingdan Huang\\workspace\\Easigami2012\\icon\\start.png", 36));
		serialStartBtn.setToolTipText("Start Easigami communication");
		serialStartBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (selectedPortName != null) {
					setPrompt(selectedPortName);
					ctrl.connect2Serial(testMode);
					ctrl.startTuiThread();
				}
			}
		});

		// stop serial communication
		serialStopBtn = new JButton(getImageIcon
				("C:\\Documents and Settings\\Yingdan Huang\\workspace\\Easigami2012\\icon\\stop.png", 36));
		serialStopBtn.setToolTipText("Stop Easigami communication");
		serialStopBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (selectedPortName != null) {
					try {
						ctrl.stopSerial();
						setPrompt("Start to use the Easigami Tangible User Interface for 3D modeling.");
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		
		chBox = new JCheckBox("Convex Hull");
		chBox.setBackground(new Color(255, 215, 0));
		chBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(chBox.isSelected()){
					JOptionPane.showMessageDialog(null, "convex Hull");
					setPrompt("Turned on Convex Hull mode");
				}else{
					setPrompt("Convex Hull mode is disabled.");
				}
			}
		});

		tb.add(serialPortsChoice);
		tb.add(serialStartBtn);
		tb.add(serialStopBtn);
		tb.add(chBox);
		populate();

		return tb;
	}
	
	private JComponent createCompoundTools() {
		JToolBar tb = new JToolBar();
		tb.setBackground(new Color(255, 215, 0));
		tb.setBorder(BorderFactory.createTitledBorder("Create Compound Solids"));

		fcStl = new JFileChooser(
				new File(
						"C:\\Documents and Settings\\Yingdan Huang\\workspace\\Easigami2012\\STL"));

		loadBtn = new JButton(getImageIcon
				("C:\\Documents and Settings\\Yingdan Huang\\workspace\\Easigami2012\\icon\\open.png", 36));
		loadBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					int openVal = fcStl.showOpenDialog(null);
					if (openVal == JFileChooser.APPROVE_OPTION) {
						File file = fcStl.getSelectedFile();
						// This is where a real application would open the file.
						ctrl.loadSTL(file.getName());
						setPrompt("Opening: " + file.getName() + ".");
					} else {
						setPrompt("Open command cancelled by user.");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	
		fcScreenshot = new JFileChooser(
				new File(
						"C:\\Documents and Settings\\Yingdan Huang\\workspace\\Easigami2012"));
		screenshotBtn = new JButton(getImageIcon
				("C:\\Documents and Settings\\Yingdan Huang\\workspace\\Easigami2012\\icon\\screenshot.png", 36));
		screenshotBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					int saveVal = fcScreenshot.showSaveDialog(null);
					if (saveVal == JFileChooser.APPROVE_OPTION) {
						File file = fcScreenshot.getSelectedFile();
						System.out.println(file.getAbsolutePath());
						// This is where a real application would open the file.
						renderer3d.setScreenshotMode(true, file);
						setPrompt("Saved screenshot: " + file.getName() + ".");
					} else {
						setPrompt("Saving screenshot cancelled by user.");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		toggles[3] = new JToggleButton(getImageIcon
				("C:\\Documents and Settings\\Yingdan Huang\\workspace\\Easigami2012\\icon\\eraser.png", 36));
		toggles[3].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(toggles[3].isSelected()){
					ctrl.setDeletionMode(true);
				}else{
					ctrl.setDeletionMode(false);
				}
			}
		});
		
		toggles[0] = new JToggleButton(getImageIcon
				("C:\\Documents and Settings\\Yingdan Huang\\workspace\\Easigami2012\\icon\\move.png", 36));
		toggles[0].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(toggles[0].isSelected()){
					//JOptionPane.showMessageDialog(null, "Move Event");
					//translate the selected object
					renderer3d.setTranslationMode(true);
					renderer3d.disableRest(0);
					disableRestBtn(0);
					setPrompt("Translation Mode");
				}else{
					renderer3d.setTranslationMode(false);
					setPrompt("Translation Mode is disabled.");
				}
			}
		});

		toggles[1] = new JToggleButton(getImageIcon
				("C:\\Documents and Settings\\Yingdan Huang\\workspace\\Easigami2012\\icon\\scale.png", 36));
		toggles[1].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(toggles[1].isSelected()){
					//JOptionPane.showMessageDialog(null, "Scale Event");
					//Scale the selected object
					renderer3d.setScaleMode(true);
					renderer3d.disableRest(1);
					disableRestBtn(1);
					setPrompt("Scale Mode");
				}else{
					renderer3d.setScaleMode(false);
					setPrompt("Scale Mode is disabled.");
				}
			}
		});

		toggles[2] = new JToggleButton(getImageIcon
				("C:\\Documents and Settings\\Yingdan Huang\\workspace\\Easigami2012\\icon\\rotate.png", 36));
		toggles[2].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(toggles[2].isSelected()){
					//JOptionPane.showMessageDialog(null, "Rotate Event");
					//rotate the selected object
					renderer3d.setRotationMode(true);
					renderer3d.disableRest(2);
					disableRestBtn(2);
					setPrompt("Rotation Mode");
				}else{
					renderer3d.setRotationMode(false);
					setPrompt("Rotation Mode is disabled.");
				}
			}
		});

		toggles[4] = new JToggleButton(getImageIcon
				("C:\\Documents and Settings\\Yingdan Huang\\workspace\\Easigami2012\\icon\\face2face.png", 36));
		toggles[4].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if(toggles[4].isSelected()){
					renderer3d.setFace2FaceMode(true);
					renderer3d.disableRest(4);
					disableRestBtn(4);
					setPrompt("Face to Face.");
				}else{
					renderer3d.setFace2FaceMode(false);
					setPrompt("Disabled Face2Face.");
				}
			}
		});

		edge2edgeBtn = new JButton(getImageIcon
				("C:\\Documents and Settings\\Yingdan Huang\\workspace\\Easigami2012\\icon\\edge2edge.png", 36));
		edge2edgeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					// ctrl.loadSTL();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		tb.add(loadBtn);
		tb.add(screenshotBtn);
		tb.add(toggles[3]);
		tb.add(toggles[0]);
		tb.add(toggles[1]);
		tb.add(toggles[2]);
		tb.add(toggles[4]);
		tb.add(edge2edgeBtn);

		return tb;
	}
	
	private JComponent createSharedTools() {
		JToolBar tb = new JToolBar();
		tb.setBackground(new Color(255, 215, 0));
		tb.setBorder(BorderFactory.createTitledBorder("Common Tools"));
		
		stlBtn = new JButton(getImageIcon
				("C:\\Documents and Settings\\Yingdan Huang\\workspace\\Easigami2012\\icon\\saveSTL.png", 36));
		stlBtn.setToolTipText("Save a 3D module created by the Easigami TUI. Or save a compound 3D model.");
		stlBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					int saveVal = fcStl.showSaveDialog(null);
					if (saveVal == JFileChooser.APPROVE_OPTION) {
						File file = fcStl.getSelectedFile();
						// This is where a real application would open the file.
						ctrl.generateSTL(file.getName());
						setPrompt("Saving: " + file.getName() + ".");
					} else {
						setPrompt("Save command cancelled by user.");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		clearBtn = new JButton(getImageIcon
				("C:\\Documents and Settings\\Yingdan Huang\\workspace\\Easigami2012\\icon\\clear.png", 36));
		clearBtn.setToolTipText("Clear convas");
		clearBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ctrl.clearSolids();
			}
		});
		
		tb.add(stlBtn);
		tb.add(clearBtn);
		
		return tb;
	}

	private JComponent createMyTools() {
		JToolBar tb = new JToolBar();
		tb.setBackground(new Color(255, 215, 0));
		tb.setBorder(BorderFactory.createTitledBorder("Developer Tools"));

		fcTui = new JFileChooser(
				new File(
						"C:\\Documents and Settings\\Yingdan Huang\\workspace\\Easigami2012\\tui"));

		readTuiBtn = new JButton("Read TUI");
		readTuiBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					int openVal = fcTui.showOpenDialog(null);
					if (openVal == JFileChooser.APPROVE_OPTION) {
						File file = fcTui.getSelectedFile();
						// This is where a real application would open the file.
						ctrl.setReadingTui(file.getName());
						// ctrl.startTuiThread();
						setPrompt("Opening TUI: " + file.getName() + ".");
					} else {
						setPrompt("Open command cancelled by user.");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		writeTuiBtn = new JButton("Write TUI");
		writeTuiBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					int saveVal = fcTui.showSaveDialog(null);
					if (saveVal == JFileChooser.APPROVE_OPTION) {
						File file = fcTui.getSelectedFile();
						// This is where a real application would open the file.
						ctrl.setWritingTui(file.getName());
						// ctrl.startTuiThread();
						setPrompt("Saving TUI: " + file.getName() + ".");
					} else {
						setPrompt("Save command cancelled by user.");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		monitorHingeBtn = new JButton("Monitor Hinges");
		monitorHingeBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				hingeGui = new HingeGui(ctrl);
				hingeGui.openDbox();
			}
		});

		tb.add(readTuiBtn);
		tb.add(writeTuiBtn);
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
		System.out.println("populate " + pList.hasMoreElements());
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
		// parallelPortsChoice.setSelectedIndex(-1);
	}

	/* The public "getter" to retrieve the chosen port by name. */
	public String getSelectedName() {
		return selectedPortName;
	}

	/* The public "getter" to retrieve the selection by CommPortIdentifier. */
	public CommPortIdentifier getSelectedIdentifier() {
		return selectedPortIdentifier;
	}

	private JComponent createCanvases() {
		JSplitPane twoPanes = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				createCanvas3D(), createCanvas2D());
		twoPanes.setDividerSize(10);
		twoPanes.setDividerLocation(WIN_WIDTH / 2);

		return twoPanes;
	}

	private JComponent createCanvas2D() {
		JPanel patternPanel = new JPanel();
		patternPanel.setLayout(new BorderLayout());
		patternPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "2D Pattern"));

		GLCapabilities capabilities = new GLCapabilities();
		if (!this.ctrl.isTestMode()) {
			capabilities.setHardwareAccelerated(true); // We want hardware
														// acceleration
			capabilities.setDoubleBuffered(true); // And double buffer

			GLCanvas canvas = new GLCanvas(capabilities);
			canvas.requestFocus();
			pattern2d = new Pattern(canvas, ctrl);

			canvas.addGLEventListener(pattern2d);
			canvas.addMouseListener(pattern2d);
			canvas.addMouseMotionListener(pattern2d);

			if (isDebug)
				System.out.println("The end of creatCanvas2D()");

			patternPanel.add(canvas, BorderLayout.CENTER);
		}
		return patternPanel;
	}

	private JComponent createCanvas3D() {
		JPanel gamePanel = new JPanel();
		gamePanel.setLayout(new BorderLayout());
		gamePanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "3D Form Explorer"));

		GLCapabilities capabilities = new GLCapabilities();
		if (!this.ctrl.isTestMode()) {
			capabilities.setHardwareAccelerated(true); // We want
														// hardwareacceleration
			capabilities.setDoubleBuffered(true); // And double buffer

			GLCanvas canvas = new GLCanvas(capabilities);
			canvas.requestFocus();
			renderer3d = new Renderer(canvas, ctrl);

			canvas.addGLEventListener(renderer3d);
			canvas.addMouseListener(renderer3d);
			canvas.addMouseMotionListener(renderer3d);

			if (isDebug)
				System.out.println("The end of creatCanvas3D()");

			gamePanel.add(canvas, BorderLayout.CENTER);
		}
		return gamePanel;
	}

	public void setPrompt(String s) {
		if (prompt != null)
			prompt.setText(s);
	}

	public void openDbox() {
		Container cp = getContentPane();
		cp.add(this.createMainPane());

		this.setSize(WIN_WIDTH, WIN_HEIGHT);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				System.exit(0);
			}
		});
		this.setVisible(true);
		if (isDebug)
			System.out.println("The end of openDBox()");
	}
	
	private ImageIcon getImageIcon(String filename, int size){
		ImageIcon startII = new ImageIcon(filename);
		Image btnImg = startII.getImage();
		btnImg = btnImg.getScaledInstance(size, -1, btnImg.SCALE_SMOOTH);
		return new ImageIcon(btnImg);
	}
	
	public Renderer getRenderer() {
		return renderer3d;
	}

	public Pattern getPattern() {
		return pattern2d;
	}

	public HingeGui getHingeGui() {
		return hingeGui;
	}
	
	public void disableRestBtn(int ex){
		boolean[] modeArray = renderer3d.getModeArray();
		for(int i=0; i<modeArray.length; i++){
			if(!modeArray[i])
				toggles[i].setSelected(false);
		}
	}
}
