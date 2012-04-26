package mygeom;

public class Point3D implements Comparable{
	private double x, y, z;
		
	/*
	 * Constructor
	 */
	public Point3D (){
		x = y = z = 0.0;
	}
	
	public Point3D(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/*
	 * getters
	 */
	//get relative values of the point
	public double getX(){
		return this.x;
	}
	
	public double getY(){
		return this.y;
	}
	
	public double getZ(){
		return this.z;
	}
	
	
	/*
	 * setters
	 */
	//set relative values of the point
	public void setX(double x){
		this.x = x; 
	}
	
	public void setY(double y){
		this.y = y; 
	}
	
	public void setZ(double z){
		this.z = z; 
	}
		
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		String str = "Point3D: (" + x + ", " + y + ", " + z + ")";
		return str;
	}

	@Override
	public int compareTo(Object o) {
		Point3D p;
		if(o instanceof Point3D){
			p = (Point3D)o;
			//if (p.x==x && p.y==y && p.z==z)
			if(Math.abs(p.x-x)<1E-8 && Math.abs(p.y-y)<1E-8 && Math.abs(p.z-z)<1E-8)
				return 0;
		}
		return -1;
	}

}


