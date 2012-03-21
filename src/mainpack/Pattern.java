package mainpack;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Date;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.SwingUtilities;

import mygeom.Point3D;
import mygeom.Line3D;
import mygeom.Polygon3D;
import mygeom.VO3D;

public class Pattern implements GLEventListener, MouseListener,
		MouseMotionListener {
	private static final GLU glu = new GLU();
	private int controlState = 0; // (0:Rotate | 1:Translate | 2:Scale)
	private boolean leftPressed = false, middlePressed = false,
			rightPressed = false;

	private float g_mousePos[] = { 0.0f, 0.0f };
	private float startDragX, startDragY;
	private float g_landRotate[] = { 0.0f, 0.0f, 0.0f };
	private float g_landTranslate[] = { 0.0f, 0.0f, 0.0f };
	private float g_landScale[] = { 1.0f, 1.0f, 1.0f };

	private GLCanvas myCanvas;
	private MainController control;
	private Draw draw;
	private Light light;
	//private FoldingModel fm_ori, fm_cp;
	private DataStructure ds;
	//private Hinge hhead = null;
	//private EasigamiPolygon phead = null;
	double curmat3d[]; // 4x4 matrix represented as 16x1 matrix
	
	private final boolean isDebug = false;
	private Date timeStamp;

	public Pattern(GLCanvas canvas, MainController ctrl) {
		if(isDebug)System.out.println("in Render Constructor\n");
		myCanvas = canvas;
		control = ctrl;
		draw = new Draw();
		light = new Light();
		//fm_ori = new FoldingModel();
		//fm_cp = new FoldingModel();
		ds =ctrl.getDataStructure();
		//bak = new Vector<Polygon3D>();
		curmat3d = new double[16];
	}

	public void init(GLAutoDrawable glDrawable) {
		if(isDebug){
			System.out.println("Renderer::init");
			//timeStamp = new Date();
			//System.out.println("  Current Time  : " + timeStamp.getTime());
			//System.out.println();
		}
		final GL gl = glDrawable.getGL();
		light.light_init(gl);
		
		gl.glClearColor((float) (216.0 / 255.0), (float) (191.0 / 255.0),
				(float) (216.0 / 255.0), 0.0f);
		gl.glShadeModel(GL.GL_SMOOTH);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_AUTO_NORMAL);

		// Setup viewport
		gl.glViewport(0, 0, 600, 600);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(45.0f, 600.0 / 500.0f, 1.0f, 20.0f);
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();

		// gl.glEnable(GL.GL_POLYGON_SMOOTH);
		// gl.glEnable(GL.GL_LINE_SMOOTH);
	}

	public void reshape(GLAutoDrawable glDrawable, int x, int y, int width,
			int height) {
		if(isDebug){
			System.out.println("Renderer::reshape");
			//timeStamp = new Date();
			//System.out.println("  Current Time  : " + timeStamp.getTime());
			//System.out.println();
		}
		// myCanvas.repaint();
	}

	public void displayChanged(GLAutoDrawable glDrawable, boolean modeChanged,
			boolean deviceChanged) {
		if(isDebug){
			System.out.println("Renderer::displayChanged");
			//timeStamp = new Date();
			//System.out.println("  Current Time  : " + timeStamp.getTime());
		}
	}

	public void display(GLAutoDrawable glDrawable) {
		if(isDebug){
			System.out.println("Renderer:: display");
			//timeStamp = new Date();
			//System.out.println("  Current Time  : " + timeStamp.getTime());
			//System.out.println();
		}
		final GL gl = glDrawable.getGL();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
		gl.glPolygonMode(GL.GL_BACK, GL.GL_LINE);
		gl.glFrontFace(GL.GL_CW);
		gl.glLoadIdentity();

		gl.glPushMatrix();
		glu.gluLookAt(0.0, 9.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0);
		// glu.gluLookAt(0.0, -5.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0);
		gl.glTranslatef(-1.0f, 0.0f, -1.0f);
		gl.glScalef(0.2f, 0.2f, 0.2f);

		// Translate
		m_translate(gl);
		// Rotate
		m_rotate(gl);
		// Scale
		m_scale(gl);

		// reference
		draw.drawGlobalCoords(gl);

		// get the current matrix on the MODELVIEW stack
		gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, curmat3d, 0);
		
		if(ds.getReady()){
			BFS(gl);
			//myCanvas.repaint();
		}
		// the end
		gl.glPopMatrix();
		gl.glFlush();
		// glutSwapBuffers();
	}
	
	public void BFS(GL gl){
		if(isDebug) System.out.println("Renderer::BFS");
		
		MyQueue<Polygon> queue = new MyQueue<Polygon>(); 	// queue for BFS
		double nextmat3d[] = new double[16];				// 4x4 matrix represented as 16x1 matrix
		Vector<Hinge> hvec = ds.getHingeVector();
		Vector<Polygon> bak = new Vector<Polygon>();//processed polygons to find the next hinge
		Vector<Polygon> pvec; 	//adjacent neighbors to each polygon
		Hinge h = null;
		Polygon phead;			//the 1st Polygon
		Polygon curp;           //the polygon under processing in BFS
		Polygon prevp=null;     //the previous polygon processed in BFS
		Polygon p;              //variable for finding the neighbors of a Polygon
		Polygon3D p3d;
		Line3D edge;			// the edge that the hinge connects to the prev
		Point3D opp; 			// the point that is opposite to the connected edge
		Point3D cen;			// hinge location
		Point3D axis;			// hinge axis
		double[][] T;			//translation matrix
		double[][] ZRuvw;		//transpose matrix of new local coordinate system
		double[][] M; 			//resultant matrix
		float[] hbc = {0.0f, 0.0f, 1.0f}; //head polygon front color - red
		float[] hfc = {1.0f, 0.0f, 0.0f}; //head polygon back color - blue
		float[] bc = {0.9f, 0.9f, 0.9f};  //front color - green
		float[] fc = {0.0f, 1.0f, 0.0f};  //back color - gray
		
		if(hvec.size() == 0)	//there should be >0 hinges
			return;
		
		// find the 1st hinge
		h = hvec.get(0);
		if(isDebug)
			System.out.println("BFS: 1st hinge " + "0x" + Integer.toString(h.getAddress(), 16));
		draw.drawHinge(gl, glu, h.getColor()); // draw the 1st hinge
		
		// find the 1st polygon
		phead = h.getLeftPolygon();
		if (phead == null)
			phead = h.getRightPolygon();
		if (phead == null){
			System.out.println("there is no polygon connected to either side of the head hinge");
			return;
		}				
		if(isDebug)
			System.out.println("1st polygon 0x" + Integer.toString(phead.getAddress(), 16));
		
		// enqueue the starting node and set it as visited
		queue.enqueue(phead);
		phead.setVisited(true);
		
		while (!queue.isEmpty()) {
			if(isDebug)System.out.println("enter into one iteration: ");
			curp = queue.dequeue();
			if(isDebug)
				System.out.println("dequeue 0x" + Integer.toString(curp.getAddress(), 16));
			
			// process the current node
			// draw the current polygon
			if (curp.getAddress() == phead.getAddress()) {// the first polygon
				if(isDebug) System.out.println("Draw the first polygon");
				if (h.isLeftPolygon(curp)) {
					// get the current matrix on the MODELVIEW stack
					gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, curmat3d, 0);
					
					curp.setMatrixPattern(curmat3d);	// set matrix for cur
					curp.setCoordsPolygon3D(h.getLeftPolygonIndex(), true); // set coordinates of the polygon
					draw.drawPolygon(gl, curp, hfc, hbc);
					bak.add(curp);
					if(isDebug)System.out.println("Draw the left polygon");
				} else if (h.isRightPolygon(curp)) {
					// get the current matrix on the MODELVIEW stack
					gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, curmat3d, 0);

					curp.setMatrixPattern(curmat3d);	// set matrix for cur
					curp.setCoordsPolygon3D(h.getRightPolygonIndex(), false); // set coordinates of the polygon
					draw.drawPolygon(gl, curp, hfc, hbc);
					bak.add(curp);
					if(isDebug) System.out.println("Draw the right polygon");
				} 
			} else {// polygons after the 1st
				if(isDebug) System.out.println("Draw the non-first polygon");
				// Find out the neighbor polygon that has been drawn.
				// At the same time, determine the hinge.
				h = ds.findHinge(curp, prevp);
				if (h == null) {
					h = ds.findHinge(curp, bak);
					
					if(h != null){
						prevp = ds.getConnectedPolygon(curp, h);
					}else //h == null
						System.out.println("can't find a hinge that the current polygon connects to.");
						break;
					}
				
				if (isDebug) {
					System.out.println("find a hinge: "
							+ Integer.toString(h.getAddress(), 16));
					System.out.println("prev: "
							+ Integer.toString(prevp.getAddress(), 16));
					System.out.println("curp: "
							+ Integer.toString(curp.getAddress(), 16));
				}
				
				p3d = prevp.getPolygon3D();
				if (h.isLeftPolygon(curp)) {
					if(isDebug) System.out.println("Draw the left polygon");

					// get the previous connected edge
					edge = p3d.getEdgeAt(h.getRightPolygonIndex());
					if(isDebug)System.out.println("check index: "+h.getRightPolygonIndex());
					opp = prevp.setCoordsOpp3D(h.getRightPolygonIndex(), false); //p3d.getVertices()[(h.getRightPolygonIndex() + 1) % p3d.getN()];
					cen = VO3D.middlePoint(edge);
					curp.setCoordsPolygon3D(h.getLeftPolygonIndex(), true); // set coordinates of the polygon			
				} else {//h.isRightPolygon(curp)
					if(isDebug)System.out.println("Draw the right polygon");

					edge = p3d.getEdgeAt(h.getLeftPolygonIndex());
					opp = prevp.setCoordsOpp3D(h.getLeftPolygonIndex(), true); //p3d.getVertices()[(h.getLeftPolygonIndex() + 1) % p3d.getN()];
					cen = VO3D.middlePoint(edge);
					curp.setCoordsPolygon3D(h.getRightPolygonIndex(), false); // set coordinates of the polygon
				}	
				
				// calculate the axis
				if(VO3D.adjustAxis(opp, false)){
					axis = VO3D.diff_vector(edge.getP2(), edge.getP1());
				}else{
					axis = VO3D.diff_vector(edge.getP1(), edge.getP2());
				}
				
				// calculate the new coordinates
				T = VO3D.getMatrix_translation(cen.getX(), cen.getY(), cen.getZ());	
				// use axis as new z axis, and calculate the new local coordinate system
				ZRuvw = VO3D.formLocalCoordinates3D(axis, 0);
				M = VO3D.matrixMultiply(T, ZRuvw);
				
				// load the previous matrix
				gl.glLoadMatrixd(prevp.getMatrixPattern(), 0); // load matrix saved for prev
				//System.out.println("Print previous matrix");
				//printMatrix(prevp.getMatrix());
			
				if(isDebug){
					System.out.println("left polygon index: " + h.getLeftPolygonIndex());
					System.out.println("right polygon index: " + h.getRightPolygonIndex());
					System.out.println("edge: " + edge.getP1() + ", " + edge.getP2());
					System.out.println("opp: " + (h.getLeftPolygonIndex() + 1) % p3d.getN());
					System.out.println("cen: " + cen);
					System.out.println("Hinge angle: " + h.getAngle());
					System.out.println("Axis: " + axis);
					System.out.println("T: ");
					VO3D.printMatrix44(T);
					System.out.println("ZRuvw: ");
					VO3D.printMatrix44(ZRuvw);
					System.out.println("M: ");
					VO3D.printMatrix44(M);
				}
				
				VO3D.flatMatrix44(nextmat3d, M);
				// multiply the new local coordinate system
				gl.glMultMatrixd(nextmat3d, 0);

				// get the matrix for the current polygon
				gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, curmat3d, 0);
				// set matrix for curp
				curp.setMatrixPattern(curmat3d); 
				bak.add(curp);
					
				draw.drawLocalCoords(gl);
				draw.drawHinge(gl, glu, h.getColor());
				// draw the polygon
				draw.drawPolygon(gl, curp, fc, bc);
				
			}

			// enqueue neighboring nodes
			pvec = ds.getNeighbors(curp);
			if(isDebug) System.out.println("neighbors that have not been visited: " + pvec.size());
			// loop through the unvisited neighbors and enqueue them
			for (int i = 0; i < pvec.size(); i++) {
				p = pvec.get(i);
				queue.enqueue(p);
				p.setVisited(true);
				if(isDebug)
					System.out.println("enqueue " + Integer.toString(p.getAddress(), 16));
			}
			
			prevp = curp;
			//System.out.println();
		}
		
		// set all polygons as non-visited
		ds.setPolygonVectorUnvisited();
		bak.clear();
	}
			
	public void printMatrix(double[] m){
		// Print out the contents of this matrix in OpenGL format
		// System.out.println("as an OpenGL matrix");
		for (int row = 0; row < 4; row++)
			for (int col = 0; col < 4; col++)
			   // OpenGL uses column-major order for storage
				System.out.format("%7.3f%c", m[row+col*4], col==3 ? '\n':' ');
	}


	// mouse control
	public void m_translate(GL gl) {
		gl.glTranslatef(g_landTranslate[0], 0.0f, 0.0f);
		gl.glTranslatef(0.0f, g_landTranslate[1], 0.0f);
		gl.glTranslatef(0.0f, 0.0f, g_landTranslate[2]);
	}

	public void m_rotate(GL gl) {
		gl.glRotatef(g_landRotate[0], 1.0f, 0.0f, 0.0f);
		gl.glRotatef(g_landRotate[1], 0.0f, 1.0f, 0.0f);
		gl.glRotatef(g_landRotate[2], 0.0f, 0.0f, 1.0f);
	}

	public void m_scale(GL gl) {
		gl.glScalef(g_landScale[0], g_landScale[1], g_landScale[2]);
	}

	// MouseListener interface
	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e)) {
			leftPressed = true;
		}
		if (SwingUtilities.isMiddleMouseButton(e)) {
			middlePressed = true;
		}
		if (SwingUtilities.isRightMouseButton(e)) {
			rightPressed = true;
		}
		//System.out.println("X = " + e.getX() + "; " + "Y = " + e.getY());
		startDragX = e.getX();
		startDragY = e.getY();
		// myCanvas.repaint();
	}

	public void mouseReleased(MouseEvent e) {
		leftPressed = false;
		middlePressed = false;
		rightPressed = false;
		// myCanvas.repaint();
	}

	// MouseMotionListener interface
	public void mouseMoved(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		if (leftPressed) {
			controlState = 0;
		} else if (middlePressed) {
			controlState = 1;
		} else {
			controlState = 2;
		}

		g_mousePos[0] = e.getX();
		g_mousePos[1] = e.getY();

		float mouseDelta[] = { g_mousePos[0] - startDragX,
				g_mousePos[1] - startDragY };
		/* System.out.println("controlState -> " + controlState); */
		switch (controlState) {
		case 0:
			/*
			 * System.out.println("Rotation Mode: " + "X = " + e.getX() + "; " +
			 * "Y = " + e.getY());
			 */
			g_landRotate[0] += mouseDelta[1];
			g_landRotate[1] += mouseDelta[0];
			g_landRotate[2] += mouseDelta[1];
			myCanvas.repaint();
			break;
		case 1:

			/*
			 * System.out.println("Translation Mode: " + "X = " + e.getX() +
			 * "; " + "Y = " + e.getY());
			 */
			g_landTranslate[0] += mouseDelta[0] * 0.01;
			g_landTranslate[1] += mouseDelta[1] * 0.01;
			g_landTranslate[2] -= mouseDelta[1] * 0.01;
			myCanvas.repaint();
			break;
		case 2:

			// System.out.println(mouseDelta[0] + "; " + mouseDelta[1]);

			/*
			 * System.out.println("Scale Mode: " + "X = " + e.getX() + "; " +
			 * "Y = " + e.getY());
			 */
			float average = (mouseDelta[0] + mouseDelta[1]) / 2;
			g_landScale[0] *= 1.0 + average * 0.01;
			g_landScale[1] *= 1.0 + average * 0.01;
			g_landScale[2] *= 1.0 + average * 0.01;
			myCanvas.repaint();
			break;
		default:
			System.out.println("case default - Rotation Mode\n");
		}

		startDragX = g_mousePos[0];
		startDragY = g_mousePos[1];
	}

	public void refresh() {
		myCanvas.display();
	}
}
