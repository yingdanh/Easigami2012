package mainpack;

import java.util.Vector;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import mygeom.FacetSTL;
import mygeom.Point3D;
import mygeom.Polygon3D;
import mygeom.SolidSTL;
import mygeom.VO3D;

public class Draw{
	//private VectorOp3d vecOp = null;
	//private double D2R = Math.PI/180.0;
	
	public Draw(){
		//vecOp = new VectorOp3d();
	}
	
	public void drawPoint3D(GL gl, Point3D p){
		gl.glBegin(GL.GL_LINES);
		gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glVertex3f((float)p.getX(), (float)p.getY(), (float)p.getZ());
        gl.glEnd();
	}
	
	public void drawGlobalCoords(GL gl) {
        gl.glLineWidth(2.0f);
        gl.glBegin(GL.GL_LINES);
        /* x-axis: Red */
        gl.glColor3f(1.0f, 0.0f, 0.0f);
        gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glVertex3f(1000.0f, 0.0f, 0.0f);

        /* y-axis: Green */
        gl.glColor3f(0.0f, 1.0f, 0.0f);
        gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glVertex3f(0.0f, 1000.0f, 0.0f);

        /* z-axis: Blue */
        gl.glColor3f(0.0f, 0.0f, 1.0f);
        gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glVertex3f(0.0f, 0.0f, 1000.0f);
        gl.glEnd();
        
    }
	
	public void drawLocalCoords(GL gl) {
		final float len = 10.0f;
		
        gl.glLineWidth(5.0f);
        gl.glBegin(GL.GL_LINES);
        /* x-axis: yellow */
        gl.glColor3f(1.0f, 1.0f, 0.0f);
        gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glVertex3f(len, 0.0f, 0.0f);

        /* y-axis: magenta */
        gl.glColor3f(1.0f, 0.0f, 1.0f);
        gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glVertex3f(0.0f, len, 0.0f);

        /* z-axis: cyan */
        gl.glColor3f(0.0f, 1.0f, 1.0f);
        gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glVertex3f(0.0f, 0.0f, len);
        gl.glEnd();
        
    }
	
	public void drawPolygon(GL gl, Polygon poly, float[] fc, float[] bc){
		//System.out.println("Draw::drawPolygon - float");
		Polygon3D p = poly.getPolygon3D();
		Point3D pt;
		
		gl.glLineWidth(1.0f);
		gl.glBegin(GL.GL_POLYGON);
		if(fc!=null)
			gl.glColor3f(fc[0], fc[1], fc[2]);
		else
			gl.glColor3f(0.7f, 0.7f, 0.7f); //Gray
		for(int i=0; i<p.getN(); i++){
			pt = p.getVertexAt(i);
			gl.glVertex3d(pt.getX(), pt.getY(), pt.getZ());
		}
		gl.glEnd();
		
		gl.glBegin(GL.GL_POLYGON);
		if(bc!=null)
			gl.glColor3f(bc[0], bc[1], bc[2]);
		else
			gl.glColor3f(0.0f, 1.0f, 0.0f); //green
		for(int i=p.getN()-1; i>=0; i--){
			pt = p.getVertexAt(i);
			gl.glVertex3d(pt.getX(), pt.getY(), pt.getZ());
		}
		gl.glEnd();
		
		gl.glLineWidth(4.0f);
		gl.glBegin(GL.GL_LINE_LOOP); //draw wireframe
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		for(int i=0; i<p.getN(); i++){
			pt = p.getVertexAt(i);
			gl.glVertex3d(pt.getX(), pt.getY(), pt.getZ());
		}
		gl.glEnd();
	}
	
	public void drawPolygon(GL gl, Polygon poly, byte[] fc, byte[] bc){
		//System.out.println("Draw::drawPolygon - byte");
		Polygon3D p = poly.getPolygon3D();
		Point3D pt;
		
		gl.glLineWidth(1.0f);
		gl.glBegin(GL.GL_POLYGON);
		if(fc!=null)
			gl.glColor3f(fc[0]/255f, fc[1]/255f, fc[2]/255f);
		else
			//gl.glColor3f(0.0f, 1.0f, 0.0f); //green
			gl.glColor3f(0.7f, 0.7f, 0.7f); //Gray
		for(int i=0; i<p.getN(); i++){
			pt = p.getVertexAt(i);
			gl.glVertex3d(pt.getX(), pt.getY(), pt.getZ());
		}
		gl.glEnd();
		
		gl.glBegin(GL.GL_POLYGON);
		if(bc!=null)
			gl.glColor3f(bc[0]/255f, bc[1]/255f, bc[2]/255f);
		else
			gl.glColor3f(0.0f, 1.0f, 0.0f); //green
		for(int i=p.getN()-1; i>=0; i--){
			pt = p.getVertexAt(i);
			gl.glVertex3d(pt.getX(), pt.getY(), pt.getZ());
		}
		gl.glEnd();
		
		gl.glLineWidth(4.0f);
		gl.glBegin(GL.GL_LINE_LOOP); //draw wireframe
		gl.glColor3f(1.0f, 1.0f, 1.0f);
		for(int i=0; i<p.getN(); i++){
			pt = p.getVertexAt(i);
			gl.glVertex3d(pt.getX(), pt.getY(), pt.getZ());
		}
		gl.glEnd();
	}
	
	/*
	 * (x, y, z) is the center of the hinge
	 */
	public void drawHinge(GL gl, GLU glu, float[] c){
		//draw hinge
        gl.glColor3f(c[0], c[1], c[2]);
        drawSphere(glu, 0.5);
	}
	
	public void drawHinge(GL gl, GLU glu, Point3D loc){
		//draw hinge
        gl.glColor3f(1.0f, 0.0f, 0.0f);
        gl.glTranslated(loc.getX(), loc.getY(), loc.getZ());
        drawSphere(glu, 0.5);
        gl.glTranslated(-loc.getX(), -loc.getY(), -loc.getZ());
	}
	
	public void drawSolidSTL(GL gl, GLU glu, SolidSTL solid, float[] fc, float[] bc){
		Vector<FacetSTL> faces = solid.getFaces();
		//System.out.println("This solid has " + faces.size() + " faces.");
		FacetSTL face;
		byte[] color;
		Point3D pt;
		Point3D centroid;
		Point3D normal, nn;
		
		for(int i=0; i<faces.size(); i++){
			face = faces.get(i);
			color = face.getColor();
			//System.out.println(color[0] + " " + color[1] + " " + color[2]);
			
			//draw centroid
			centroid = face.getCentroid();
			if(centroid != null){
				drawHinge(gl, glu, centroid);
			}
			//draw normal
			normal = face.getNormal();
			nn = VO3D.matrix41_mult(VO3D.getMatrix_translation(centroid.getX(), centroid.getY(), centroid.getZ()), normal);
			this.drawLine(gl, centroid, nn);
			
			//draw front
			gl.glBegin(GL.GL_POLYGON);
			if(fc!=null)
				gl.glColor3f(fc[0], fc[1], fc[2]);
			else
				gl.glColor3f(0.7f, 0.7f, 0.7f); 
			for(int j=0; j<face.getN(); j++){
				pt = face.getVertexAt(j);
				gl.glVertex3d(pt.getX(), pt.getY(), pt.getZ());
			}
			gl.glEnd();
			
			//draw back
			gl.glBegin(GL.GL_POLYGON);
			if(bc!=null)
				gl.glColor3f(bc[0], bc[1], bc[2]);
			else
				gl.glColor3ub((byte)color[0], (byte)color[1], (byte)color[2]);
			for(int j=face.getN()-1; j>=0; j--){
				pt = face.getVertexAt(j);
				gl.glVertex3d(pt.getX(), pt.getY(), pt.getZ());
			}
			gl.glEnd();
			
			//draw wireframe
			gl.glLineWidth(4.0f);
			gl.glBegin(GL.GL_LINE_LOOP); 
			gl.glColor3f(1.0f, 1.0f, 1.0f);
			for(int j=0; j<face.getN(); j++){
				pt = face.getVertexAt(j);
				gl.glVertex3d(pt.getX(), pt.getY(), pt.getZ());
			}
			gl.glEnd();
		}
	}
	
	public void drawConvexHull(GL gl, GLU glu, SolidSTL solid, float[] fc, float[] bc){
		Vector<FacetSTL> chfaces = solid.getConvexHull();
		System.out.println("This solid has " + solid.getConvexHull().size() + " faces.");
		FacetSTL face;
		byte[] color;
		Point3D pt;
		
		for(int i=0; i<chfaces.size(); i++){
			face = chfaces.get(i);
			color = face.getColor();
			//System.out.println(color[0] + " " + color[1] + " " + color[2]);
						
			//draw front
			gl.glBegin(GL.GL_POLYGON);
			if(fc!=null)
				gl.glColor3f(fc[0], fc[1], fc[2]);
			else
				gl.glColor3f(0.7f, 0.7f, 0.7f); 
			for(int j=0; j<face.getN(); j++){
				pt = face.getVertexAt(j);
				gl.glVertex3d(pt.getX(), pt.getY(), pt.getZ());
			}
			gl.glEnd();
			
			//draw back
			gl.glBegin(GL.GL_POLYGON);
			if(bc!=null)
				gl.glColor3f(bc[0], bc[1], bc[2]);
			else
				gl.glColor3ub((byte)color[0], (byte)color[1], (byte)color[2]);
			for(int j=face.getN()-1; j>=0; j--){
				pt = face.getVertexAt(j);
				gl.glVertex3d(pt.getX(), pt.getY(), pt.getZ());
			}
			gl.glEnd();
			
			//draw wireframe
			gl.glLineWidth(4.0f);
			gl.glBegin(GL.GL_LINE_LOOP); 
			gl.glColor3f(1.0f, 1.0f, 1.0f);
			for(int j=0; j<face.getN(); j++){
				pt = face.getVertexAt(j);
				gl.glVertex3d(pt.getX(), pt.getY(), pt.getZ());
			}
			gl.glEnd();
		}
	}
	
	public void drawCone(GL gl, Polygon poly, Point3D center){
		Polygon3D p = poly.getPolygon3D();
		Point3D pt1, pt2;
		
		for(int i=0; i<p.getN(); i++){
			pt1 = p.getVertexAt(i);
			pt2 = p.getVertexAt((i+1)%p.getN());
			gl.glBegin(GL.GL_POLYGON);
			gl.glColor4f(0.0f, 1.0f, 1.0f, 0.2f); //cyan
			gl.glVertex3d(center.getX(), center.getY(), center.getZ());
			gl.glVertex3d(pt2.getX(), pt2.getY(), pt2.getZ());
			gl.glVertex3d(pt1.getX(), pt1.getY(), pt1.getZ());
			gl.glEnd();
		}
		
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f); //white
		for(int i=0; i<p.getN(); i++){
			pt1 = p.getVertexAt(i);
			pt2 = p.getVertexAt((i+1)%p.getN());
			
			gl.glBegin(GL.GL_LINE_LOOP);			
			gl.glVertex3d(center.getX(), center.getY(), center.getZ());
			gl.glVertex3d(pt2.getX(), pt2.getY(), pt2.getZ());
			gl.glVertex3d(pt1.getX(), pt1.getY(), pt1.getZ());
			gl.glEnd();
		}
	}
	
	public void drawBox(GL gl){
		double step = 10;
		Point3D v0 = new Point3D(0, 0, 0);  	    Point3D v1 = new Point3D(step, 0, 0);
		Point3D v2 = new Point3D(step, 0, step);    Point3D v3 = new Point3D(0, 0, step);
		Point3D v4 = new Point3D(0, step, 0);       Point3D v5 = new Point3D(step, step, 0);
		Point3D v6 = new Point3D(step, step, step); Point3D v7 = new Point3D(0, step, step);
		
		gl.glBegin(GL.GL_QUADS);
		//bottom
        gl.glColor3f(1.0f, 0.0f, 0.0f);
        gl.glVertex3d(v0.getX(), v0.getY(), v0.getZ()); gl.glVertex3d(v1.getX(), v1.getY(), v1.getZ());
        gl.glVertex3d(v2.getX(), v2.getY(), v2.getZ()); gl.glVertex3d(v3.getX(), v3.getY(), v3.getZ());
        
        //top
        gl.glColor3f(0.0f, 0.0f, 1.0f);
        gl.glVertex3d(v4.getX(), v4.getY(), v4.getZ()); gl.glVertex3d(v7.getX(), v7.getY(), v7.getZ());
        gl.glVertex3d(v6.getX(), v6.getY(), v6.getZ()); gl.glVertex3d(v5.getX(), v5.getY(), v5.getZ());
        
        //front
        gl.glColor3f(0.0f, 1.0f, 0.0f);
        gl.glVertex3d(v3.getX(), v3.getY(), v3.getZ()); gl.glVertex3d(v2.getX(), v2.getY(), v2.getZ());
        gl.glVertex3d(v6.getX(), v6.getY(), v6.getZ()); gl.glVertex3d(v7.getX(), v7.getY(), v7.getZ());
       
        //back
        gl.glColor3f(1.0f, 1.0f, 0.0f);
        gl.glVertex3d(v1.getX(), v1.getY(), v1.getZ()); gl.glVertex3d(v0.getX(), v0.getY(), v0.getZ());
        gl.glVertex3d(v4.getX(), v4.getY(), v4.getZ()); gl.glVertex3d(v5.getX(), v5.getY(), v5.getZ());
        
        //left
        gl.glColor3f(0.0f, 1.0f, 1.0f);
        gl.glVertex3d(v0.getX(), v0.getY(), v0.getZ()); gl.glVertex3d(v3.getX(), v3.getY(), v3.getZ());
        gl.glVertex3d(v7.getX(), v7.getY(), v7.getZ()); gl.glVertex3d(v4.getX(), v4.getY(), v4.getZ());
        
        //right
        gl.glColor3f(1.0f, 0.0f, 1.0f);
        gl.glVertex3d(v1.getX(), v1.getY(), v1.getZ()); gl.glVertex3d(v5.getX(), v5.getY(), v5.getZ());
        gl.glVertex3d(v6.getX(), v6.getY(), v6.getZ()); gl.glVertex3d(v2.getX(), v2.getY(), v2.getZ());
        gl.glEnd();
	}
	
	public void drawLine(GL gl, Point3D p1, Point3D p2){
		gl.glBegin(GL.GL_LINES);
		gl.glColor3f(0.0f, 0.0f, 0.5f);
		gl.glVertex3d(p1.getX(), p1.getY(), p1.getZ());
		gl.glVertex3d(p2.getX(), p2.getY(), p2.getZ());
		gl.glEnd();
	}
	
	public void drawSphere(GLU glu, double radius){
		GLUquadric quadric = glu.gluNewQuadric();
		glu.gluQuadricDrawStyle(quadric, glu.GLU_LINE);
		//glu.gluQuadricDrawStyle(quadric, glu.GLU_FILL);
		glu.gluQuadricOrientation(quadric, GLU.GLU_OUTSIDE);
	    glu.gluSphere(quadric, radius, 24, 24);		
	}
}
