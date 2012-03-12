package mygeom;

public class Line3D {
	private Point3D p1, p2;
	
	/*
	 * constructor
	 */
	public Line3D(Point3D p1, Point3D p2){
		this.p1 = p1;
		this.p2 = p2;
	}
	
	/*
	 * getters
	 */
	public Point3D getP1(){
		return p1;
	}
	
	public Point3D getP2(){
		return p2;
	}
	
	/*
	 * setters
	 */
	public void setP1(Point3D p1){
		this.p1 = p1;
	}
	
	public void setP2(Point3D p2){
		this.p2 = p2;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		String str = "";
		str += p1 + ", " + p2;
		return str;
	}
}
