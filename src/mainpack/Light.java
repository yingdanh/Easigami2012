package mainpack;

import javax.media.opengl.*;

public class Light {
	void light_init(GL gl)
	{
		float white2[] = {0.2f, 0.2f,0.2f, 1.0f};
		float white6[] = {0.6f, 0.6f,0.6f, 1.0f};
		float white[] = {1.0f, 1.0f, 1.0f, 1.0f};
		float black[] = {0.0f, 0.0f, 0.0f, 1.0f};

		float mat_shininess[] = {50.0f};

		float light0_position[] = {1.0f, 1.0f, 5.0f, 0.0f};
		float light1_position[] = {1.0f, 5.0f, -2.0f, 0.0f};
		float light2_position[] = {-3.0f, 1.0f, -1.0f, 0.0f};


		/* Material properties */
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, black, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, white6, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, white2, 0);
		gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, mat_shininess, 0);

		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, light0_position, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, white, 0);
		gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, white, 0);
		gl.glEnable(GL.GL_LIGHT0);

		gl.glLightfv(GL.GL_LIGHT1, GL.GL_POSITION, light1_position, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_DIFFUSE, white, 0);
		gl.glLightfv(GL.GL_LIGHT1, GL.GL_SPECULAR, white, 0);
		gl.glEnable(GL.GL_LIGHT1);

		gl.glLightfv(GL.GL_LIGHT2, GL.GL_POSITION, light2_position, 0);
		gl.glLightfv(GL.GL_LIGHT2, GL.GL_DIFFUSE, white, 0);
		gl.glLightfv(GL.GL_LIGHT2, GL.GL_SPECULAR, white, 0);
		gl.glEnable(GL.GL_LIGHT2);

		gl.glEnable(GL.GL_NORMALIZE);
		gl.glLightModeli(GL.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_TRUE);

		gl.glShadeModel(GL.GL_FLAT);

		gl.glEnable(GL.GL_LIGHTING);
		gl.glEnable(GL.GL_COLOR_MATERIAL);
	}
}
