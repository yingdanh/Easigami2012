package mainpack;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.SwingUtilities;

import mygeom.FacetSTL;
import mygeom.Point3D;
import mygeom.Line3D;
import mygeom.Polygon3D;
import mygeom.SolidSTL;
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
	private boolean TUI_mode;
	private boolean STL_mode;
	private Vector<SolidSTL> solids;
	private int[] selectedIndicesA = { -1, -1 }; // 0-solid index; 1-face index
	private int[] selectedIndicesB = { -1, -1 };

	private boolean ScreenshotMode;
	private File screenshotFile;
	// transformation state variables
	private boolean[] modeArray;
	private int TranslationMode = 0;;
	private int ScaleMode = 1;
	private int RotationMode = 2;
	private int DeletionMode = 3;
	private int Face2FaceMode = 4;

	// debugging variables
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
		TUI_mode = false;
		STL_mode = false;
		ScreenshotMode = false;
		screenshotFile = null;
		modeArray = new boolean[5];
		modeArray[TranslationMode] = false;
		modeArray[ScaleMode] = false;
		modeArray[RotationMode] = false;
		modeArray[DeletionMode] = false;
		modeArray[Face2FaceMode] = false;
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

		if (TUI_mode) {
			System.out.println("TUI_mode");
			draw.drawGlobalCoords(gl);
			firstBFS(gl);
		} else if (STL_mode) {
			if (mode == GL.GL_SELECT) {
				displaySolidsPickingMode(gl, glu);
				if (this.modeArray[Face2FaceMode]) {
					System.out.println("A: " + selectedIndicesA[0] + ", "
							+ selectedIndicesA[1]);
					if (!solidSeclected(selectedIndicesA)) {
						selectedIndicesA = processPick(gl);
						System.out.println("A: " + selectedIndicesA[0] + ", "
								+ selectedIndicesA[1]);
					} else {
						selectedIndicesB = processPick(gl);
						System.out.println("B: " + selectedIndicesB[0] + ", "
								+ selectedIndicesB[1]);
						// union
						joinAB(gl, selectedIndicesA, selectedIndicesB);
						// empty A, B
						this.emptySelectionA();
						this.emptySelectionB();
					}
				} else {
					selectedIndicesA = processPick(gl);
					System.out.println("in display " + selectedIndicesA[0]
							+ ", " + selectedIndicesA[1]);
				}
				mode = GL.GL_RENDER;
				myCanvas.repaint();
			} else {
				// reference
				draw.drawGlobalCoords(gl);
				// displaySolids(gl, glu);
				displayConvexHull(gl, glu);
				glDrawable.swapBuffers();
			}
		}
		
		if(ScreenshotMode){
			if(screenshotFile != null)
				writeBufferToFile(glDrawable, screenshotFile);
		}

		// the end
		gl.glPopMatrix();
		gl.glFlush();
	}

	public void setSolids(Vector<SolidSTL> solids) {
		this.solids = solids;
	}

	/*
	 * public Vector<SolidSTL> getSolids(){ return this.solids; }
	 */
	public void displaySolids(GL gl, GLU glu) {
		if (isDebug)
			System.out.println("in displaySolids " + solids.size());

		SolidSTL solid;
		Vector<FacetSTL> faces;
		FacetSTL face;
		Point3D normal, nnormal;

		float[] fc = { 0.7f, 0.7f, 0.7f }; // gray
		float[] bc = { 0.0f, 1.0f, 0.0f }; // green

		for (int i = 0; i < solids.size(); i++) {
			solid = solids.get(i);
			// --- delete after correcting calculateAbsVertices in Polygon
			faces = solid.getFaces();

			for (int j = 0; j < faces.size(); j++) {
				face = faces.get(j);
				normal = VO3D.getNormal(face.getVertexAt(0),
						face.getVertexAt(1), face.getVertexAt(2));
				nnormal = VO3D.unit(normal);
				face.setNormal(nnormal.getX() * 5, nnormal.getY() * 5,
						nnormal.getZ() * 5);
			}
			// ---
			draw.drawSolidSTL(gl, glu, solids.get(i), fc, bc);
		}
	}

	// draw for picking
	public void displaySolidsPickingMode(GL gl, GLU glu) {
		if (isDebug)
			System.out.println("Renderer::secondBFS");

		gl.glDisable(GL.GL_DITHER);
		gl.glDisable(GL.GL_LIGHTING);
		gl.glDisable(GL.GL_COLOR_MATERIAL);

		for (int i = 0; i < solids.size(); i++) {
			draw.drawSolidSTL(gl, glu, solids.get(i), null, null);
		}

		gl.glEnable(GL.GL_DITHER);
		gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_COLOR_MATERIAL);
	}

	public void displayConvexHull(GL gl, GLU glu) {
		// if (isDebug)
		System.out.println("in displayConvexHull ");

		SolidSTL solid;

		float[] fc = { 0.0f, 1.0f, 1.0f }; //
		float[] bc = { 1.0f, 0.0f, 1.0f }; // maganta

		for (int i = 0; i < solids.size(); i++) {
			solid = solids.get(i);
			solid.incrementalCovexhull();
			draw.drawConvexHull(gl, glu, solid, fc, bc);
		}
	}

	public void firstBFS(GL gl) {
		if (isDebug)
			System.out.println("Renderer::firstBFS");

		MyQueue<Polygon> queue = new MyQueue<Polygon>(); // queue for BFS
		double startmat3d[] = new double[16];
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
		double[][] M; // resultant matrix
		float[] hfc = { 0.0f, 0.0f, 1.0f }; // blue
		float[] hbc = { 1.0f, 0.0f, 0.0f }; // red
		float[] fc = { 0.7f, 0.7f, 0.7f }; // gray
		float[] bc = { 0.0f, 1.0f, 0.0f }; // green
		// double h1 = 10/2*Math.tan(Math.toRadians(54));
		// double h2 = Math.sqrt(3.0/4.0*100-Math.pow(h1, 2));
		// Point3D cone_center = null;

		if (hvec.size() == 0) // there should be >0 hinges
			return;

		// find the 1st hinge
		h = hvec.get(0);
		if (isDebug)
			System.out.println("BFS: 1st hinge " + "0x"
					+ Integer.toString(h.getAddress(), 16));
		// draw.drawHinge(gl, glu, h.getColor()); // draw the 1st hinge

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
					gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, startmat3d, 0);

					// set matrix for cur
					curp.setMatrix3D(VO3D.getIdentityMatrix());
					// set coordinates of the polygon
					curp.setCoordsPolygon3D(h.getLeftPolygonIndex(), true);
					draw.drawPolygon(gl, curp, hfc, hbc);
					bak.add(curp);
					if (isDebug)
						System.out.println("Drew the left polygon");
					// cone_center = new Point3D(-h1, -h2, 0);
					// if(curp.isSelected())
					// draw.drawCone(gl, curp, cone_center);
				} else if (h.isRightPolygon(curp)) {
					// get the current matrix on the MODELVIEW stack
					gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, startmat3d, 0);

					// set matrix for cur
					curp.setMatrix3D(VO3D.getIdentityMatrix());
					// set coordinates of the polygon
					curp.setCoordsPolygon3D(h.getRightPolygonIndex(), false);
					draw.drawPolygon(gl, curp, hfc, hbc);
					bak.add(curp);
					// cur.setMatrix(VO3D.getMatrix_identity());
					if (isDebug)
						System.out.println("Drew the right polygon");
					// cone_center = new Point3D(h1, -h2, 0);
					// if(curp.isSelected())
					// draw.drawCone(gl, curp, cone_center);
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
					// set coordinates of the polygon
					curp.setCoordsPolygon3D(h.getLeftPolygonIndex(), true);

					// calculate the axis
					if (VO3D.adjustAxis(opp, false)) {
						axis = VO3D.diff_vector(edge.getP2(), edge.getP1());
					} else {
						axis = VO3D.diff_vector(edge.getP1(), edge.getP2());
					}

					// use axis as new z axis, and calculate the new local
					// coordinate system
					ZRuvw = VO3D.formLocalCoordinates3D(axis, h.getAngle()-Math.PI);
					// cone_center = new Point3D(-h1, -h2, 0);
				} else {
					if (isDebug)
						System.out.println("Draw the right polygon");

					edge = p3d.getEdgeAt(h.getLeftPolygonIndex());
					opp = prevp.setCoordsOpp3D(h.getLeftPolygonIndex(), true);
					cen = VO3D.middlePoint(edge);
					// set coordinates of the polygon
					curp.setCoordsPolygon3D(h.getRightPolygonIndex(), false);

					// calculate the new axis
					if (VO3D.adjustAxis(opp, false)) {
						axis = VO3D.diff_vector(edge.getP2(), edge.getP1());
					} else {
						axis = VO3D.diff_vector(edge.getP1(), edge.getP2());
					}

					// use axis as new z axis, and calculate the new local
					// coordinate system
					ZRuvw = VO3D.formLocalCoordinates3D(axis,
							Math.PI-h.getAngle());
					// cone_center = new Point3D(h1, -h2, 0);
				}

				// calculate the new coordinates
				T = VO3D.getMatrix_translation(cen.getX(), cen.getY(),
						cen.getZ());
				M = VO3D.matrixMultiply(T, ZRuvw);

				if (isDebug) {
					System.out.println("opp: " + opp);
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
				// load the starting matrix
				gl.glLoadMatrixd(startmat3d, 0);
				// load prev matrix saved
				gl.glMultMatrixd(prevp.getMatrix3D(), 0);
				// multiply the new local coordinate system
				gl.glMultMatrixd(nextmat3d, 0);

				// set matrix for curp
				curp.setMatrix3D(VO3D.matrixMultiply(prevp.getMatrix3D(),
						nextmat3d));
				VO3D.printMatrix16(VO3D.matrixMultiply(prevp.getMatrix3D(),
						nextmat3d));
				bak.add(curp);

				// draw.drawLocalCoords(gl);
				// draw.drawHinge(gl, glu, h.getColor());
				draw.drawPolygon(gl, curp, fc, bc);
				// if(curp.isSelected())
				// draw.drawCone(gl, curp, cone_center);
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
	}

	private int[] processPick(GL gl) {
		// System.out.println("Renderer::processPick");
		int viewport[] = new int[4];
		java.nio.ByteBuffer pixelsRGB = ByteBuffer.allocateDirect(3);
		// BufferUtils.newByteBuffer
		int[] indices;

		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);

		gl.glReadPixels((int) g_mousePos[0], viewport[3] - (int) g_mousePos[1],
				1, 1, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, pixelsRGB);
		// System.out.println("Checking color: " + (pixelsRGB.get(0)&(0xff)) +
		// " " +
		// (pixelsRGB.get(1)&(0xff)) + " " +
		// (pixelsRGB.get(2)&(0xff)));
		indices = control.getSolids().getFaceByColor(pixelsRGB);
		return indices;
	}

	public void joinAB(GL gl, int[] A, int[] B) {
		SolidSTL solidA = solids.get(A[0]);
		FacetSTL faceA = solidA.getFaceAt(A[1]);
		Point3D cenA = faceA.getCentroid();
		// Line3D edgeA = new Line3D(faceA.getVertexAt(1),
		// faceA.getVertexAt(0));

		SolidSTL solidB = solids.get(B[0]);
		Vector<FacetSTL> faces = solidB.getFaces();
		FacetSTL faceB = solidB.getFaceAt(B[1]);
		Point3D cenB = faceB.getCentroid();
		Vector<Point3D> vertices; // = faceB.getVertices();
		FacetSTL face;
		Point3D newp;

		Point3D normalA = faceA.getNormal();
		System.out.println("normalA: " + normalA);
		Point3D normalB = faceB.getNormal();
		System.out.println("normalB " + normalB);

		// get the angle between normalA and normalB
		double theta = VO3D.getAngleRad(normalA, normalB);
		System.out.println("theta = " + theta);
		Point3D nz = VO3D.cross(normalA, normalB);
		System.out.println("new z: " + nz);
		double[][] T1 = VO3D.getMatrix_translation(-cenB.getX(), -cenB.getY(),
				-cenB.getZ());
		double[][] T2 = VO3D.getMatrix_translation(cenA.getX(), cenA.getY(),
				cenA.getZ());
		// rotate about the new z with angle theta
		double[][] ZRuvw = VO3D.point_by_angle(VO3D.unit(nz), Math.PI - theta);
		double[][] M1 = VO3D.matrixMultiply(ZRuvw, T1);
		double[][] M2 = VO3D.matrixMultiply(T2, M1);

		for (int i = 0; i < faces.size(); i++) {
			face = faces.get(i);
			vertices = face.getVertices();
			for (int j = 0; j < vertices.size(); j++) {// change vertices for
														// each face
				newp = VO3D.matrix41_mult(M2, vertices.get(j));
				// System.out.println("hi? " + VO3D.matrix41_mult(Rz,
				// normalBx));
				face.setVertexAt(j, newp);
			}
			/*
			 * double d1 = VO3D.distance(faceB.getVertexAt(0),
			 * faceB.getVertexAt(1)); double d2 =
			 * VO3D.distance(faceB.getVertexAt(1), faceB.getVertexAt(2)); double
			 * d3 = VO3D.distance(faceB.getVertexAt(2), faceB.getVertexAt(0));
			 * System.out.println(d1 + " " + d2 + " " + d3);
			 */
		}

		// rotate about the normal at the attaching face
		// Line3D edgeB = new Line3D(faceB.getVertexAt(1),
		// faceB.getVertexAt(0));
		double beta = VO3D.getAngleRad(
				VO3D.diff_vector(faceA.getVertexAt(1), faceA.getVertexAt(0)),
				VO3D.diff_vector(faceB.getVertexAt(1), faceB.getVertexAt(0)));
		T1 = VO3D.getMatrix_translation(-cenA.getX(), -cenA.getY(),
				-cenA.getZ());
		ZRuvw = VO3D.point_by_angle(VO3D.unit(normalA), Math.PI - beta);
		T2 = VO3D.getMatrix_translation(cenA.getX(), cenA.getY(), cenA.getZ());
		M1 = VO3D.matrixMultiply(ZRuvw, T1);
		M2 = VO3D.matrixMultiply(T2, M1);

		for (int i = 0; i < faces.size(); i++) {
			face = faces.get(i);
			vertices = face.getVertices();
			for (int j = 0; j < vertices.size(); j++) {// change vertices for
														// each face
				newp = VO3D.matrix41_mult(M2, vertices.get(j));
				face.setVertexAt(j, newp);
			}
		}
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

	public void writeBufferToFile(GLAutoDrawable drawable, File outputFile) {
		int width = drawable.getWidth();
		int height = drawable.getHeight();

		ByteBuffer pixelsRGB = ByteBuffer.allocateDirect(width*height*3);
				//BufferUtils.newByteBuffer(width * height * 3);

		GL gl = drawable.getGL();

		gl.glReadBuffer(GL.GL_BACK);
		gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);

		gl.glReadPixels(0, // GLint x
				0, // GLint y
				width, // GLsizei width
				height, // GLsizei height
				GL.GL_RGB, // GLenum format
				GL.GL_UNSIGNED_BYTE, // GLenum type
				pixelsRGB); // GLvoid *pixels

		int[] pixelInts = new int[width * height];

		// Convert RGB bytes to ARGB ints with no transparency. Flip image
		// vertically by reading the
		// rows of pixels in the byte buffer in reverse - (0,0) is at bottom
		// left in OpenGL.

		int p = width * height * 3; // Points to first byte (red) in each row.
		int q; // Index into ByteBuffer
		int i = 0; // Index into target int[]
		int w3 = width * 3; // Number of bytes in each row

		for (int row = 0; row < height; row++) {
			p -= w3;
			q = p;
			for (int col = 0; col < width; col++) {
				int iR = pixelsRGB.get(q++);
				int iG = pixelsRGB.get(q++);
				int iB = pixelsRGB.get(q++);

				pixelInts[i++] = 0xFF000000 | ((iR & 0x000000FF) << 16)
						| ((iG & 0x000000FF) << 8) | (iB & 0x000000FF);
			}
		}

		BufferedImage bufferedImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		bufferedImage.setRGB(0, 0, width, height, pixelInts, 0, width);

		try {
			ImageIO.write(bufferedImage, "PNG", outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		// System.out.println("MouseClicked");
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		// System.out.println("mousePressed");
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
		myCanvas.repaint();
	}

	public void mouseReleased(MouseEvent e) {
		// System.out.println("mouseReleased");
		leftPressed = false;
		middlePressed = false;
		rightPressed = false;

		// for deletion
		if (modeArray[DeletionMode] && selectedIndicesA[0] != -1) {
			control.deleteSolid(selectedIndicesA[0]);
		} else if (modeArray[Face2FaceMode]) {// for add

		}
		myCanvas.repaint();
	}

	// MouseMotionListener interface
	public void mouseMoved(MouseEvent e) {
		// System.out.println("mouseMoved");
	}

	public void mouseDragged(MouseEvent e) {
		// System.out.println("mouseDragged");
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

		if (modeArray[TranslationMode] || modeArray[ScaleMode]
				|| modeArray[RotationMode]) {
			mode = GL.GL_SELECT;
			float average = (mouseDelta[0] + mouseDelta[1]) / 2;
			double sf = 1.0 + average * 0.01; // scale factor

			if (selectedIndicesA[0] != -1) {
				SolidSTL solid = solids.get(selectedIndicesA[0]);

				if (modeArray[TranslationMode]) {
					solid.translate(mouseDelta[0] * 0.15,
							-mouseDelta[1] * 0.15, 0);
				} else if (modeArray[ScaleMode]) {
					solid.scale(sf, sf, sf);
				} else if (modeArray[RotationMode]) {
					solid.rotate(mouseDelta[1] * 0.15, mouseDelta[0] * 0.15, 0);
				}
			}
		} else {
			/* System.out.println("controlState -> " + controlState); */
			switch (controlState) {
			case 0:
				/*
				 * System.out.println("Rotation Mode: " + "X = " + e.getX() +
				 * "; " + "Y = " + e.getY());
				 */
				g_landRotate[0] += mouseDelta[1];
				g_landRotate[1] += mouseDelta[0];
				g_landRotate[2] += mouseDelta[1];
				break;
			case 1:
				/*
				 * System.out.println("Translation Mode: " + "X = " + e.getX() +
				 * "; " + "Y = " + e.getY());
				 */
				g_landTranslate[0] += mouseDelta[0] * 0.01;
				g_landTranslate[1] += mouseDelta[1] * 0.01;
				g_landTranslate[2] -= mouseDelta[1] * 0.01;
				break;
			case 2:
				// System.out.println(mouseDelta[0] + "; " + mouseDelta[1]);
				/*
				 * System.out.println("Scale Mode: " + "X = " + e.getX() + "; "
				 * + "Y = " + e.getY());
				 */
				float average = (mouseDelta[0] + mouseDelta[1]) / 2;
				g_landScale[0] *= 1.0 + average * 0.01;
				g_landScale[1] *= 1.0 + average * 0.01;
				g_landScale[2] *= 1.0 + average * 0.01;
				break;
			default:
				System.out.println("case default - Rotation Mode\n");
			}
		}
		myCanvas.repaint();
		startDragX = g_mousePos[0];
		startDragY = g_mousePos[1];
	}

	public void refresh() {
		myCanvas.display();
	}
	
	public void setTUIMode(boolean tf) {
		TUI_mode = tf;
		STL_mode = TUI_mode == true ? false : true;
	}

	public void setSTLMode(boolean tf) {
		STL_mode = tf;
		TUI_mode = STL_mode == true ? false : true;
	}
	
	public void setScreenshotMode(boolean tf, File f){
		ScreenshotMode = tf;
		screenshotFile = f;
		refresh();
	}

	// control transformation state
	public void setTranslationMode(boolean tf) {
		System.out.println("setTranslationMode " + tf);
		modeArray[TranslationMode] = tf;
	}

	public boolean getTranslationMode() {
		return modeArray[TranslationMode];
	}

	public void setScaleMode(boolean tf) {
		modeArray[ScaleMode] = tf;
	}

	public boolean getScaleMode() {
		return modeArray[ScaleMode];
	}

	public void setRotationMode(boolean tf) {
		modeArray[RotationMode] = tf;
	}

	public boolean getRotationMode() {
		return modeArray[RotationMode];
	}

	public void setDeletionMode(boolean tf) {
		modeArray[DeletionMode] = tf;
	}

	public boolean getDeletionMode() {
		return modeArray[DeletionMode];
	}

	public void setFace2FaceMode(boolean tf) {
		emptySelectionA();
		emptySelectionB();
		modeArray[Face2FaceMode] = tf;
	}
	
	public boolean getFace2FaceMode() {
		return modeArray[Face2FaceMode];
	}

	private void emptySelectionA() {
		selectedIndicesA[0] = selectedIndicesA[1] = -1;
	}

	private void emptySelectionB() {
		selectedIndicesB[0] = selectedIndicesB[1] = -1;
	}

	//disable the rest of the mode controls
	//except ex
	public void disableRest(int ex) {
		for (int i = 0; i < modeArray.length; i++) {
			if (i == ex)
				continue;
			modeArray[i] = false;
		}
	}

	public boolean[] getModeArray() {
		return modeArray;
	}

	public boolean solidSeclected(int[] arr) {
		if (arr[0] != -1 && arr[1] != -1) {
			return true;
		}
		return false;
	}
}
