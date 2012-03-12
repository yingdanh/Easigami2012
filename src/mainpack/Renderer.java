package mainpack;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.nio.ByteBuffer;
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

public class Renderer implements GLEventListener, MouseListener,
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
	// private FoldingModel fm_ori, fm_cp;
	private DataStructure ds;
	// private Hinge hhead = null;
	// private EasigamiPolygon phead = null;
	private double curmat3d[]; // 4x4 matrix represented as 16x1 matrix
	private boolean doneBFS;
	private int mode = GL.GL_RENDER;

	private final boolean isDebug = false;
	private Date timeStamp;

	public Renderer(GLCanvas canvas, MainController ctrl) {
		if (isDebug)
			System.out.println("in Render Constructor\n");
		myCanvas = canvas;
		control = ctrl;
		draw = new Draw();
		light = new Light();
		// fm_ori = new FoldingModel();
		// fm_cp = new FoldingModel();
		ds = ctrl.getDataStructure();
		// bak = new Vector<Polygon3D>();
		curmat3d = new double[16];
		doneBFS = false;
	}

	public void init(GLAutoDrawable glDrawable) {
		if (isDebug) {
			System.out.println("Renderer::init");
			// timeStamp = new Date();
			// System.out.println("  Current Time  : " + timeStamp.getTime());
			// System.out.println();
		}
		final GL gl = glDrawable.getGL();
		light.light_init(gl);

		gl.glClearColor((float) (216.0 / 255.0), (float) (191.0 / 255.0),
				(float) (216.0 / 255.0), 0.0f);
		gl.glShadeModel(GL.GL_SMOOTH);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glEnable(GL.GL_AUTO_NORMAL);
		gl.glEnable(GL.GL_CULL_FACE);

		// Setup viewport
		/*
		 * gl.glViewport(0, 0, 600, 600); gl.glMatrixMode(GL.GL_PROJECTION);
		 * gl.glLoadIdentity(); glu.gluPerspective(45.0f, 600.0 / 500.0f, 1.0f,
		 * 20.0f); gl.glMatrixMode(GL.GL_MODELVIEW); gl.glLoadIdentity();
		 */

		// gl.glEnable(GL.GL_POLYGON_SMOOTH);
		// gl.glEnable(GL.GL_LINE_SMOOTH);
	}

	public void reshape(GLAutoDrawable glDrawable, int x, int y, int width,
			int height) {
		if (isDebug) {
			System.out.println("Renderer::reshape");
			// timeStamp = new Date();
			// System.out.println("  Current Time  : " + timeStamp.getTime());
			// System.out.println();
		}
		GL gl = glDrawable.getGL();
		float ratio;
		if (height == 0)
			height = 1;
		ratio = 1.0f * width / height;
		// Reset the coordinate system before modifying
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();

		// Set the viewport to be the entire window
		gl.glViewport(0, 0, width, height);
		// Set the clipping volume
		glu.gluPerspective(45.0f, ratio, 0.1f, 1000f);
		// setting the camera now
		gl.glMatrixMode(GL.GL_MODELVIEW);
	}

	public void displayChanged(GLAutoDrawable glDrawable, boolean modeChanged,
			boolean deviceChanged) {
		if (isDebug) {
			System.out.println("Renderer::displayChanged");
			// timeStamp = new Date();
			// System.out.println("  Current Time  : " + timeStamp.getTime());
		}
	}

	public void display(GLAutoDrawable glDrawable) {
		if (isDebug) {
			System.out.println("Renderer:: display");
			// timeStamp = new Date();
			// System.out.println("  Current Time  : " + timeStamp.getTime());
			// System.out.println();
		}
		final GL gl = glDrawable.getGL();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
		gl.glPolygonMode(GL.GL_BACK, GL.GL_LINE);
		gl.glFrontFace(GL.GL_CW);
		gl.glLoadIdentity();

		gl.glPushMatrix();
		glu.gluLookAt(9.0, 9.0, 9.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);
		// glu.gluLookAt(0.0, -5.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0);
		// gl.glTranslatef(-1.0f, 0.0f, 0.0f);
		gl.glScalef(0.3f, 0.3f, 0.3f);

		// Translate
		m_translate(gl);
		// Rotate
		m_rotate(gl);
		// Scale
		m_scale(gl);

		if (ds.getReady()) {
			if (mode == GL.GL_SELECT) {
				System.out.println("secondBFS");
				secondBFS(gl);
			} else {
				// reference
				draw.drawGlobalCoords(gl);
				firstBFS(gl);
			}

			if (mode == GL.GL_SELECT) {
				System.out.println("processPick");
				processPick(gl);
				mode = GL.GL_RENDER;
			} else {
				glDrawable.swapBuffers();
			}

			// myCanvas.repaint();
		}

		// the end
		gl.glPopMatrix();
		gl.glFlush();
	}

	public void firstBFS(GL gl) {
		if (isDebug)
			System.out.println("Renderer::firstBFS");

		MyQueue<Polygon> queue = new MyQueue<Polygon>(); // queue for BFS
		double nextmat3d[] = new double[16]; // 4x4 matrix represented as 16x1
		// matrix
		Vector<Hinge> hvec = ds.getHingeVector();
		Vector<Polygon> bak = new Vector<Polygon>();// processed polygons to
		// find the next hinge
		Vector<Polygon> pvec = null; // adjacent neighbors to each polygon
		Hinge h = null;
		Polygon phead; // the 1st Polygon
		Polygon curp = null; // the polygon under processing in BFS
		Polygon prevp = null; // the previous polygon processed in BFS
		Polygon p; // variable for finding the neighbors of a Polygon
		Polygon3D p3d;
		Line3D edge; // the edge that the hinge connects to the prev
		Point3D opp; // the point that is opposite to the connected edge
		Point3D cen; // hinge location
		Point3D axis; // hinge axis
		double[][] T; // translation matrix
		double[][] ZRuvw; // transpose matrix of new local coordinate system
		double[][] M; // resulatant matrix
		float[] hfc = { 0.0f, 0.0f, 1.0f };
		float[] hbc = { 1.0f, 0.0f, 0.0f };
		float[] fc = { 0.9f, 0.9f, 0.9f };
		float[] bc = { 0.0f, 1.0f, 0.0f };
		double h1 = 10 / 2 * Math.tan(Math.toRadians(54));
		double h2 = Math.sqrt(3.0 / 4.0 * 100 - Math.pow(h1, 2));
		// Point3D cone_center = null;

		if (hvec.size() == 0) // there should be >0 hinges
			return;

		// find the 1st hinge
		h = hvec.get(0);
		if (isDebug)
			System.out.println("BFS: 1st hinge " + "0x"
					+ Integer.toString(h.getAddress(), 16));
		draw.drawHinge(gl, glu, h.getColor()); // draw the 1st hinge

		// find the 1st polygon
		phead = h.getLeftPolygon();
		if (phead == null)
			phead = h.getRightPolygon();
		if (phead == null) {
			System.out
					.println("there is no polygon connected to either side of the head hinge");
			return;
		}
		if (isDebug)
			System.out.println("1st polygon " + "0x"
					+ Integer.toString(phead.getAddress(), 16));

		// enqueue the starting node
		queue.enqueue(phead);
		phead.setVisited(true);

		while (!queue.isEmpty()) {
			if (isDebug)
				System.out.println("enter into one iteration: ");
			curp = queue.dequeue();
			if (isDebug)
				System.out.println("dequeue "
						+ Integer.toString(curp.getAddress(), 16));

			// process the current node
			// draw the current polygon
			if (curp.getAddress() == phead.getAddress()) {// the first polygon
				if (isDebug)
					System.out.println("Draw the first polygon");
				if (h.isLeftPolygon(curp)) {
					// get the current matrix on the MODELVIEW stack
					gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, curmat3d, 0);
					// print the current matrix
					// System.out.println("Print current matrix");
					// printMatrix(curmat);

					curp.setMatrix3D(curmat3d); // set matrix for cur
					curp.setCoordsPolygon3D(h.getLeftPolygonIndex(), true); // set
																			// coordinates
																			// of
																			// the
																			// polygon
					draw.drawPolygon(gl, curp, hfc, hbc);
					bak.add(curp);
					if (isDebug)
						System.out.println("Drew the left polygon");
					/*
					 * cone_center = new Point3D(-h1, -h2, 0);
					 * if(curp.isSelected()) draw.drawCone(gl, curp,
					 * cone_center);
					 */
				} else if (h.isRightPolygon(curp)) {
					// get the current matrix on the MODELVIEW stack
					gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, curmat3d, 0);
					// print the current matrix
					// System.out.println("Print current matrix");
					// printMatrix(curmat);

					curp.setMatrix3D(curmat3d); // set matrix for cur
					curp.setCoordsPolygon3D(h.getRightPolygonIndex(), false); // set
																				// coordinates
																				// of
																				// the
																				// polygon
					draw.drawPolygon(gl, curp, hfc, hbc);
					bak.add(curp);
					// cur.setMatrix(VO3D.getMatrix_identity());
					if (isDebug)
						System.out.println("Drew the right polygon");
					/*
					 * cone_center = new Point3D(h1, -h2, 0);
					 * if(curp.isSelected()) draw.drawCone(gl, curp,
					 * cone_center);
					 */
				}
			} else {// polygons after the 1st
				if (isDebug)
					System.out.println("Draw the non-first polygon");
				// Find out the neighbor polygon that has been drawn.
				// At the same time, determine the hinge.
				h = ds.findHinge(curp, prevp);
				if (h == null) {
					for (int i = 0; i < bak.size(); i++) {
						// System.out.println("i = " + i);
						// check if cur is connected to some previous polygon
						h = ds.findHinge(curp, bak.get(i));
						// System.out.println("h = " + h);
						if (h != null) {
							if (h.isLeftPolygon(curp))
								prevp = h.getRightPolygon();
							else if (h.isRightPolygon(curp))
								prevp = h.getLeftPolygon();
							break;
						}
					}
					if (h == null) {
						System.out
								.println("can't find a hinge that the current polygon connects to.");
						System.out.println(curp);
						break;
					}
				}
				if (isDebug) {
					System.out.println("find a hinge: "
							+ Integer.toString(h.getAddress(), 16));
					System.out.println("prev: "
							+ Integer.toString(prevp.getAddress(), 16));
				}

				p3d = prevp.getPolygon3D();
				if (h.isLeftPolygon(curp)) {
					if (isDebug)
						System.out.println("Draw the left polygon");

					// get the previous connected edge
					edge = p3d.getEdgeAt(h.getRightPolygonIndex());
					opp = prevp.setCoordsOpp3D(h.getRightPolygonIndex(), false);
					cen = VO3D.middlePoint(edge);
					curp.setCoordsPolygon3D(h.getLeftPolygonIndex(), true); // set
																			// coordinates
																			// of
																			// the
																			// polygon

					// translate based on the previous matrix
					// and draw the new hinge
					// gl.glTranslated(cen.getX(), cen.getY(), cen.getZ());

					// calculate the axis
					if (VO3D.adjustAxis(opp, false)) {
						axis = VO3D.diff_vector(edge.getP2(), edge.getP1());
					} else {
						axis = VO3D.diff_vector(edge.getP1(), edge.getP2());
					}

					if (isDebug) {
						System.out.println("opp: " + opp);
						System.out.println("Hinge angle: "
								+ h.getAngle(ds.isConfigured()));
						System.out.println("Axis: " + axis);
					}

					// use axis as new z axis, and calculate the new local
					// coordinate system
					// System.out.println("Hinge " + h.getAddress() + " angle: "
					// + h.getAngle(ds.isConfigured()));
					// ZRuvw = VO3D.formLocalCoordinates3D(axis, 45);
					ZRuvw = VO3D.formLocalCoordinates3D(axis,
							h.getAngle(ds.isConfigured()) - Math.PI);
					// cone_center = new Point3D(-h1, -h2, 0);
				} else {
					if (isDebug)
						System.out.println("Draw the right polygon");

					edge = p3d.getEdgeAt(h.getLeftPolygonIndex());
					opp = prevp.setCoordsOpp3D(h.getLeftPolygonIndex(), true);
					cen = VO3D.middlePoint(edge);
					curp.setCoordsPolygon3D(h.getRightPolygonIndex(), false); // set
																				// coordinates
																				// of
																				// the
																				// polygon

					// calculate the new axis
					if (VO3D.adjustAxis(opp, false)) {
						axis = VO3D.diff_vector(edge.getP2(), edge.getP1());
					} else {
						axis = VO3D.diff_vector(edge.getP1(), edge.getP2());
					}

					// use axis as new z axis, and calculate the new local
					// coordinate system
					if (isDebug) {
						System.out.println(h.getLeftPolygonIndex());
						System.out.println(edge.getP1() + ", " + edge.getP2());
						System.out.println("opp index = "
								+ (h.getLeftPolygonIndex() + 1) % p3d.getN());
						System.out.println("opp: " + opp);
						System.out.println("cen: " + cen);
						System.out.println("Hinge angle: "
								+ h.getAngle(ds.isConfigured()));
						System.out.println("Axis: " + axis);
					}

					// System.out.println("Hinge " + h.getAddress() + " angle: "
					// + h.getAngle(ds.isConfigured()));
					// ZRuvw = VO3D.formLocalCoordinates3D(axis, 45);
					ZRuvw = VO3D.formLocalCoordinates3D(axis,
							Math.PI - h.getAngle(ds.isConfigured()));
					// cone_center = new Point3D(h1, -h2, 0);
				}

				// calculate the new coordinates
				T = VO3D.getMatrix_translation(cen.getX(), cen.getY(),
						cen.getZ());
				M = VO3D.matrixMultiply(T, ZRuvw);
				if (isDebug) {
					System.out.println("T: ");
					VO3D.printMatrix44(T);
					System.out.println("ZRuvw: ");
					VO3D.printMatrix44(ZRuvw);
					System.out.println("M: ");
					VO3D.printMatrix44(M);
				}

				VO3D.flatMatrix44(nextmat3d, M);
				// load the previous matrix
				gl.glLoadMatrixd(prevp.getMatrix3D(), 0); // load matrix saved
				// for prev
				// multiply the new local coordinate system
				gl.glMultMatrixd(nextmat3d, 0);

				// get the matrix for the current polygon
				gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, curmat3d, 0);
				// set matrix for curp
				curp.setMatrix3D(curmat3d);
				bak.add(curp);

				// draw.drawLocalCoords(gl);
				draw.drawHinge(gl, glu, h.getColor());
				// draw the left polygon
				draw.drawPolygon(gl, curp, fc, bc);
				/*
				 * if(curp.isSelected()) draw.drawCone(gl, curp, cone_center);
				 */
			}

			// enqueue neighboring nodes
			pvec = ds.getNeighbors(curp);
			if (isDebug)
				System.out.println("neighbors that have not been visited: "
						+ pvec.size());
			for (int i = 0; i < pvec.size(); i++) {
				p = pvec.get(i);
				queue.enqueue(p);
				p.setVisited(true);
				if (isDebug)
					System.out.println("enqueue "
							+ Integer.toString(p.getAddress(), 16));
			}

			prevp = curp;
			// System.out.println();
		}

		// Below is only for debugging
		// ************************
		if (isDebug)
			System.out.println("Back to the first polygon");
		// Find out the neighbor polygon that has been drawn.
		// At the same time, determine the hinge.
		curp = phead;
		h = ds.findHinge(curp, prevp);
		if (h == null) {
			for (int i = 0; i < bak.size(); i++) {
				// System.out.println("i = " + i);
				// check if cur is connected to some previous polygon
				h = ds.findHinge(curp, bak.get(i));
				// System.out.println("h = " + h);
				if (h != null) {
					if (h.isLeftPolygon(curp))
						prevp = h.getRightPolygon();
					else if (h.isRightPolygon(curp))
						prevp = h.getLeftPolygon();
					break;
				}
			}
			if (h == null) {
				System.out
						.println("can't find a hinge that the current polygon connects to.");
				System.out.println(curp);
			}
		}
		if (isDebug) {
			System.out.println("find a hinge: "
					+ Integer.toString(h.getAddress(), 16));
			System.out.println("prev: "
					+ Integer.toString(prevp.getAddress(), 16));
		}

		p3d = prevp.getPolygon3D();
		if (h.isLeftPolygon(curp)) {
			if (isDebug)
				System.out.println("Draw the left polygon");

			// get the previous connected edge
			edge = p3d.getEdgeAt(h.getRightPolygonIndex());
			opp = prevp.setCoordsOpp3D(h.getRightPolygonIndex(), false);
			cen = VO3D.middlePoint(edge);
			curp.setCoordsPolygon3D(h.getLeftPolygonIndex(), true); // set
																	// coordinates
																	// of the
																	// polygon

			// translate based on the previous matrix
			// and draw the new hinge
			// gl.glTranslated(cen.getX(), cen.getY(), cen.getZ());

			// calculate the axis
			if (VO3D.adjustAxis(opp, false)) {
				axis = VO3D.diff_vector(edge.getP2(), edge.getP1());
			} else {
				axis = VO3D.diff_vector(edge.getP1(), edge.getP2());
			}

			if (isDebug) {
				System.out.println("opp: " + opp);
				System.out.println("Hinge angle: "
						+ h.getAngle(ds.isConfigured()));
				System.out.println("Axis: " + axis);
			}

			// use axis as new z axis, and calculate the new local
			// coordinate system
			// System.out.println("Hinge " + h.getAddress() + " angle: " +
			// h.getAngle(ds.isConfigured()));
			// ZRuvw = VO3D.formLocalCoordinates3D(axis, 45);
			ZRuvw = VO3D.formLocalCoordinates3D(axis,
					h.getAngle(ds.isConfigured()) - Math.PI);
			// cone_center = new Point3D(-h1, -h2, 0);
		} else {
			if (isDebug)
				System.out.println("Draw the right polygon");

			edge = p3d.getEdgeAt(h.getLeftPolygonIndex());
			opp = prevp.setCoordsOpp3D(h.getLeftPolygonIndex(), true);
			cen = VO3D.middlePoint(edge);
			curp.setCoordsPolygon3D(h.getRightPolygonIndex(), false); // set
																		// coordinates
																		// of
																		// the
																		// polygon

			// calculate the new axis
			if (VO3D.adjustAxis(opp, false)) {
				axis = VO3D.diff_vector(edge.getP2(), edge.getP1());
			} else {
				axis = VO3D.diff_vector(edge.getP1(), edge.getP2());
			}

			// use axis as new z axis, and calculate the new local
			// coordinate system
			if (isDebug) {
				System.out.println(h.getLeftPolygonIndex());
				System.out.println(edge.getP1() + ", " + edge.getP2());
				System.out.println("opp index = "
						+ (h.getLeftPolygonIndex() + 1) % p3d.getN());
				System.out.println("opp: " + opp);
				System.out.println("cen: " + cen);
				System.out.println("Hinge angle: "
						+ h.getAngle(ds.isConfigured()));
				System.out.println("Axis: " + axis);
			}

			// System.out.println("Hinge " + h.getAddress() + " angle: " +
			// h.getAngle(ds.isConfigured()));
			// ZRuvw = VO3D.formLocalCoordinates3D(axis, 45);
			ZRuvw = VO3D.formLocalCoordinates3D(axis,
					Math.PI - h.getAngle(ds.isConfigured()));
			// cone_center = new Point3D(h1, -h2, 0);
		}

		// calculate the new coordinates
		T = VO3D.getMatrix_translation(cen.getX(), cen.getY(), cen.getZ());
		M = VO3D.matrixMultiply(T, ZRuvw);
		if (isDebug) {
			System.out.println("T: ");
			VO3D.printMatrix44(T);
			System.out.println("ZRuvw: ");
			VO3D.printMatrix44(ZRuvw);
			System.out.println("M: ");
			VO3D.printMatrix44(M);
		}

		VO3D.flatMatrix44(nextmat3d, M);
		// load the previous matrix
		gl.glLoadMatrixd(prevp.getMatrix3D(), 0); // load matrix saved
		// for prev
		// multiply the new local coordinate system
		gl.glMultMatrixd(nextmat3d, 0);

		// get the matrix for the current polygon
		gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, curmat3d, 0);
		// set matrix for curp
		// curp.setMatrix3D(curmat3d);
		// bak.add(curp);
		
		// draw.drawLocalCoords(gl);
		draw.drawHinge(gl, glu, h.getColor());
		// draw the left polygon
		draw.drawPolygon(gl, curp, fc, bc);
		
		if (isDebug)
			System.out.println("Back to the second polygon");
		// Find out the neighbor polygon that has been drawn.
		// At the same time, determine the hinge.
		curp = ds.getPolygonVetor().get(1);
		prevp = phead;
		System.out.println(curp.getAddress() + ", " + prevp.getAddress());
		h = ds.findHinge(curp, prevp);
		if (h == null) {
			for (int i = 0; i < bak.size(); i++) {
				// System.out.println("i = " + i);
				// check if cur is connected to some previous polygon
				h = ds.findHinge(curp, bak.get(i));
				// System.out.println("h = " + h);
				if (h != null) {
					if (h.isLeftPolygon(curp))
						prevp = h.getRightPolygon();
					else if (h.isRightPolygon(curp))
						prevp = h.getLeftPolygon();
					break;
				}
			}
			if (h == null) {
				System.out
						.println("can't find a hinge that the current polygon connects to.");
				System.out.println(curp);
			}
		}
		if (isDebug) {
			System.out.println("find a hinge: "
					+ Integer.toString(h.getAddress(), 16));
			System.out.println("prev: "
					+ Integer.toString(prevp.getAddress(), 16));
		}

		p3d = prevp.getPolygon3D();
		if (h.isLeftPolygon(curp)) {
			if (isDebug)
				System.out.println("Draw the left polygon");

			// get the previous connected edge
			edge = p3d.getEdgeAt(h.getRightPolygonIndex());
			opp = prevp.setCoordsOpp3D(h.getRightPolygonIndex(), false);
			cen = VO3D.middlePoint(edge);
			curp.setCoordsPolygon3D(h.getLeftPolygonIndex(), true); // set
																	// coordinates
																	// of the
																	// polygon

			// translate based on the previous matrix
			// and draw the new hinge
			// gl.glTranslated(cen.getX(), cen.getY(), cen.getZ());

			// calculate the axis
			if (VO3D.adjustAxis(opp, false)) {
				axis = VO3D.diff_vector(edge.getP2(), edge.getP1());
			} else {
				axis = VO3D.diff_vector(edge.getP1(), edge.getP2());
			}

			if (isDebug) {
				System.out.println("opp: " + opp);
				System.out.println("Hinge angle: "
						+ h.getAngle(ds.isConfigured()));
				System.out.println("Axis: " + axis);
			}

			// use axis as new z axis, and calculate the new local
			// coordinate system
			// System.out.println("Hinge " + h.getAddress() + " angle: " +
			// h.getAngle(ds.isConfigured()));
			// ZRuvw = VO3D.formLocalCoordinates3D(axis, 45);
			ZRuvw = VO3D.formLocalCoordinates3D(axis, - Math.PI);
			// cone_center = new Point3D(-h1, -h2, 0);
		} else {
			if (isDebug)
				System.out.println("Draw the right polygon");

			edge = p3d.getEdgeAt(h.getLeftPolygonIndex());
			opp = prevp.setCoordsOpp3D(h.getLeftPolygonIndex(), true);
			cen = VO3D.middlePoint(edge);
			curp.setCoordsPolygon3D(h.getRightPolygonIndex(), false); // set
																		// coordinates
																		// of
																		// the
																		// polygon

			// calculate the new axis
			if (VO3D.adjustAxis(opp, false)) {
				axis = VO3D.diff_vector(edge.getP2(), edge.getP1());
			} else {
				axis = VO3D.diff_vector(edge.getP1(), edge.getP2());
			}

			// use axis as new z axis, and calculate the new local
			// coordinate system
			if (isDebug) {
				System.out.println(h.getLeftPolygonIndex());
				System.out.println(edge.getP1() + ", " + edge.getP2());
				System.out.println("opp index = "
						+ (h.getLeftPolygonIndex() + 1) % p3d.getN());
				System.out.println("opp: " + opp);
				System.out.println("cen: " + cen);
				System.out.println("Hinge angle: "
						+ h.getAngle(ds.isConfigured()));
				System.out.println("Axis: " + axis);
			}

			// System.out.println("Hinge " + h.getAddress() + " angle: " +
			// h.getAngle(ds.isConfigured()));
			// ZRuvw = VO3D.formLocalCoordinates3D(axis, 45);
			ZRuvw = VO3D.formLocalCoordinates3D(axis, Math.PI - 0);
			// cone_center = new Point3D(h1, -h2, 0);
		}

		// calculate the new coordinates
		T = VO3D.getMatrix_translation(cen.getX(), cen.getY(), cen.getZ());
		M = VO3D.matrixMultiply(T, ZRuvw);
		if (isDebug) {
			System.out.println("T: ");
			VO3D.printMatrix44(T);
			System.out.println("ZRuvw: ");
			VO3D.printMatrix44(ZRuvw);
			System.out.println("M: ");
			VO3D.printMatrix44(M);
		}

		VO3D.flatMatrix44(nextmat3d, M);
		// load the previous matrix
		gl.glLoadMatrixd(curmat3d, 0); // load matrix saved
		// for prev
		// multiply the new local coordinate system
		gl.glMultMatrixd(nextmat3d, 0);

		// get the matrix for the current polygon
		gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, curmat3d, 0);
		// set matrix for curp
		// curp.setMatrix3D(curmat3d);
		// bak.add(curp);
		
		// draw.drawLocalCoords(gl);
		draw.drawHinge(gl, glu, h.getColor());
		// draw the left polygon
		draw.drawPolygon(gl, curp, fc, bc);
		
		
		System.out.println("Check Identity: ");
		VO3D.printMatrix44(VO3D.squareMatrix44(phead.getMatrix3D()));
		System.out.println();
				
		VO3D.printMatrix44(VO3D.squareMatrix44(curmat3d));
		System.out.println();
		// ************

		// set all polygons as non-visited
		ds.setPolygonVectorUnvisited();
		bak.clear();
	}

	public void secondBFS(GL gl) {
		if (isDebug)
			System.out.println("Renderer::secondBFS");

		gl.glDisable(GL.GL_DITHER);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glDisable(GL.GL_COLOR_MATERIAL);
		MyQueue<Polygon> queue = new MyQueue<Polygon>(); // queue for BFS
		double nextmat3d[] = new double[16]; // 4x4 matrix represented as 16x1
		// matrix
		Vector<Hinge> hvec = ds.getHingeVector();
		Vector<Polygon> bak = new Vector<Polygon>();// processed polygons to
		// find the next hinge
		Vector<Polygon> pvec; // adjacent neighbors to each polygon
		Hinge h = null;
		Polygon phead; // the 1st Polygon
		Polygon curp; // the polygon under processing in BFS
		Polygon prevp = null; // the previous polygon processed in BFS
		Polygon p; // variable for finding the neighbors of a Polygon
		Polygon3D p3d;
		Line3D edge; // the edge that the hinge connects to the prev
		Point3D opp; // the point that is opposite to the connected edge
		Point3D cen; // hinge location
		Point3D axis; // hinge axis
		double[][] T; // translation matrix
		double[][] ZRuvw; // transpose matrix of new local coordinate system
		double[][] M; // resulatant matrix

		if (hvec.size() == 0) // there should be >0 hinges
			return;

		// find the 1st hinge
		h = hvec.get(0);
		if (isDebug)
			System.out.println("BFS: 1st hinge " + "0x"
					+ Integer.toString(h.getAddress(), 16));
		draw.drawHinge(gl, glu, h.getColor()); // draw the 1st hinge

		// find the 1st polygon
		phead = h.getLeftPolygon();
		if (phead == null)
			phead = h.getRightPolygon();
		if (phead == null) {
			System.out
					.println("there is no polygon connected to either side of the head hinge");
			return;
		}
		if (isDebug)
			System.out.println("1st polygon " + "0x"
					+ Integer.toString(phead.getAddress(), 16));

		// enqueue the starting node
		queue.enqueue(phead);
		phead.setVisited(true);

		while (!queue.isEmpty()) {
			if (isDebug)
				System.out.println("enter into one iteration: ");
			curp = queue.dequeue();
			if (isDebug)
				System.out.println("dequeue "
						+ Integer.toString(curp.getAddress(), 16));

			// process the current node
			// draw the current polygon
			if (curp.getAddress() == phead.getAddress()) {// the first polygon
				if (isDebug)
					System.out.println("Draw the first polygon");
				if (h.isLeftPolygon(curp)) {
					// get the current matrix on the MODELVIEW stack
					gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, curmat3d, 0);
					curp.setMatrix3D(curmat3d); // set matrix for cur
					curp.setCoordsPolygon3D(h.getLeftPolygonIndex(), true); // set
																			// coordinates
																			// of
																			// the
																			// polygon
					draw.drawPolygon(gl, curp, curp.getColor(), curp.getColor());
					bak.add(curp);
					if (isDebug)
						System.out.println("Drew the left polygon");
				} else if (h.isRightPolygon(curp)) {
					// get the current matrix on the MODELVIEW stack
					gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, curmat3d, 0);
					curp.setMatrix3D(curmat3d); // set matrix for cur
					curp.setCoordsPolygon3D(h.getRightPolygonIndex(), false); // set
																				// coordinates
																				// of
																				// the
																				// polygon
					draw.drawPolygon(gl, curp, curp.getColor(), curp.getColor());
					bak.add(curp);
					if (isDebug)
						System.out.println("Drew the right polygon");
				}
			} else {// polygons after the 1st
				if (isDebug)
					System.out.println("Draw the non-first polygon");
				// Find out the neighbor polygon that has been drawn.
				// At the same time, determine the hinge.
				h = ds.findHinge(curp, prevp);
				if (h == null) {
					for (int i = 0; i < bak.size(); i++) {
						// System.out.println("i = " + i);
						// check if cur is connected to some previous polygon
						h = ds.findHinge(curp, bak.get(i));
						// System.out.println("h = " + h);
						if (h != null) {
							if (h.isLeftPolygon(curp))
								prevp = h.getRightPolygon();
							else if (h.isRightPolygon(curp))
								prevp = h.getLeftPolygon();
							break;
						}
					}
					if (h == null) {
						System.out
								.println("can't find a hinge that the current polygon connects to.");
					}
				}
				if (isDebug) {
					System.out.println("find a hinge: "
							+ Integer.toString(h.getAddress(), 16));
					System.out.println("prev: "
							+ Integer.toString(prevp.getAddress(), 16));
				}

				p3d = prevp.getPolygon3D();
				if (h.isLeftPolygon(curp)) {
					if (isDebug)
						System.out.println("Draw the left polygon");

					// get the previous connected edge
					edge = p3d.getEdgeAt(h.getRightPolygonIndex());
					opp = prevp.setCoordsOpp3D(h.getRightPolygonIndex(), false);
					cen = VO3D.middlePoint(edge);
					curp.setCoordsPolygon3D(h.getLeftPolygonIndex(), true); // set
																			// coordinates
																			// of
																			// the
																			// polygon

					// calculate the axis
					if (VO3D.adjustAxis(opp, false)) {
						axis = VO3D.diff_vector(edge.getP2(), edge.getP1());
					} else {
						axis = VO3D.diff_vector(edge.getP1(), edge.getP2());
					}

					if (isDebug) {
						System.out.println("opp: " + opp);
						System.out.println("Hinge angle: "
								+ h.getAngle(ds.isConfigured()));
						System.out.println("Axis: " + axis);
					}

					// use axis as new z axis, and calculate the new local
					// coordinate system
					// System.out.println("Hinge " + h.getAddress() + " angle: "
					// + h.getAngle(ds.isConfigured()));
					// ZRuvw = VO3D.formLocalCoordinates3D(axis,
					// h.getAngle(ds.isConfigured())-Math.PI);
					ZRuvw = VO3D.formLocalCoordinates3D(axis,
							Math.PI - h.getAngle(ds.isConfigured()));
				} else {
					if (isDebug)
						System.out.println("Draw the right polygon");

					edge = p3d.getEdgeAt(h.getLeftPolygonIndex());
					opp = prevp.setCoordsOpp3D(h.getLeftPolygonIndex(), true);
					cen = VO3D.middlePoint(edge);
					curp.setCoordsPolygon3D(h.getRightPolygonIndex(), false); // set
																				// coordinates
																				// of
																				// the
																				// polygon

					// calculate the new axis
					if (VO3D.adjustAxis(opp, false)) {
						axis = VO3D.diff_vector(edge.getP2(), edge.getP1());
					} else {
						axis = VO3D.diff_vector(edge.getP1(), edge.getP2());
					}

					// use axis as new z axis, and calculate the new local
					// coordinate system
					if (isDebug) {
						System.out.println(h.getLeftPolygonIndex());
						System.out.println(edge.getP1() + ", " + edge.getP2());
						System.out.println("opp index = "
								+ (h.getLeftPolygonIndex() + 1) % p3d.getN());
						System.out.println("opp: " + opp);
						System.out.println("cen: " + cen);
						System.out.println("Hinge angle: "
								+ h.getAngle(ds.isConfigured()));
						System.out.println("Axis: " + axis);
					}
					// System.out.println("Hinge " + h.getAddress() + " angle: "
					// + h.getAngle(ds.isConfigured()));
					// ZRuvw = VO3D.formLocalCoordinates3D(axis,
					// Math.PI-h.getAngle(ds.isConfigured()));
					ZRuvw = VO3D.formLocalCoordinates3D(axis,
							h.getAngle(ds.isConfigured()) - Math.PI);
				}

				// calculate the new coordinates
				T = VO3D.getMatrix_translation(cen.getX(), cen.getY(),
						cen.getZ());
				M = VO3D.matrixMultiply(T, ZRuvw);
				if (isDebug) {
					System.out.println("T: ");
					VO3D.printMatrix44(T);
					System.out.println("ZRuvw: ");
					VO3D.printMatrix44(ZRuvw);
					System.out.println("M: ");
					VO3D.printMatrix44(M);
				}

				VO3D.flatMatrix44(nextmat3d, M);
				// load the previous matrix
				gl.glLoadMatrixd(prevp.getMatrix3D(), 0); // load matrix saved
				// for prev
				// multiply the new local coordinate system
				gl.glMultMatrixd(nextmat3d, 0);

				// get the matrix for the current polygon
				gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, curmat3d, 0);
				// set matrix for curp
				curp.setMatrix3D(curmat3d);
				bak.add(curp);

				// draw.drawLocalCoords(gl);
				draw.drawHinge(gl, glu, h.getColor());
				// draw the left polygon
				draw.drawPolygon(gl, curp, curp.getColor(), curp.getColor());

			}

			// enqueue neighboring nodes
			pvec = ds.getNeighbors(curp);
			if (isDebug)
				System.out.println("neighbors that have not been visited: "
						+ pvec.size());
			for (int i = 0; i < pvec.size(); i++) {
				p = pvec.get(i);
				queue.enqueue(p);
				p.setVisited(true);
				if (isDebug)
					System.out.println("enqueue "
							+ Integer.toString(p.getAddress(), 16));
			}

			prevp = curp;
			// System.out.println();
		}

		// set all polygons as non-visited
		ds.setPolygonVectorUnvisited();
		bak.clear();
		gl.glEnable(GL.GL_DITHER);
		gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_COLOR_MATERIAL);
	}

	private void processPick(GL gl) {
		int viewport[] = new int[4];
		java.nio.ByteBuffer pixelsRGB =
		// BufferUtils.newByteBuffer
		ByteBuffer.allocateDirect(3);
		Polygon p;
		Integer address;

		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);

		gl.glReadPixels((int) g_mousePos[0], viewport[3] - (int) g_mousePos[1],
				1, 1, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, pixelsRGB);
		// System.out.println(pixelsRGB.get(0) + " " +
		// Integer.toHexString(pixelsRGB.get(0)) + " "
		// + Integer.toHexString(pixelsRGB.get(1)) + " "
		// + Integer.toHexString(pixelsRGB.get(2)));
		address = ds.getPolygonByColor(pixelsRGB);
		if (address != null) {
			System.out.println("select " + Integer.toHexString(address));
			p = ds.getPolygonAt(address.intValue());
			p.setSelected(true);
			System.out.println(p);
		}
		/*
		 * else if ((pixelsRGB.get(1) & (0xff)) == 255)
		 * System.out.println("You picked the 1st snowman on the 2nd row"); else
		 * if ((pixelsRGB.get(2) & (0xff)) == 255)
		 * System.out.println("You picked the 2nd snowman on the 1st row"); else
		 * if ((pixelsRGB.get(0) & (0xff)) == 130 && (pixelsRGB.get(2) & (0xff))
		 * == 130)
		 * System.out.println("You picked the 2nd snowman on the 2nd row"); else
		 * System.out.println("You didn't click a snowman!");
		 */
		System.out.println();
	}

	public void printMatrix(double[] m) {
		// Print out the contents of this matrix in OpenGL format
		// System.out.println("as an OpenGL matrix");
		for (int row = 0; row < 4; row++)
			for (int col = 0; col < 4; col++)
				// OpenGL uses column-major order for storage
				System.out.format("%7.3f%c", m[row + col * 4], col == 3 ? '\n'
						: ' ');
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
		// System.out.println("X = " + e.getX() + "; " + "Y = " + e.getY());
		startDragX = e.getX();
		startDragY = e.getY();
		g_mousePos[0] = startDragX;
		this.g_mousePos[1] = startDragY;
		mode = GL.GL_SELECT;
	}

	public void mouseReleased(MouseEvent e) {
		leftPressed = false;
		middlePressed = false;
		rightPressed = false;
		myCanvas.repaint();
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
