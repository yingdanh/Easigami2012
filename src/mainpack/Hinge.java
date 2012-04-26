package mainpack;

import mygeom.Point3D;

public class Hinge {
	// attributes
	private int address;
	private int leftPot, rightPot;
	// private double angle; // when there are 2 polygons connected
	// private EasigamiPolygon polygonLeft = null;
	// private EasigamiPolygon polygonRight = null;
	private Polygon leftPolygon = null;
	private Polygon rightPolygon = null;
	private int leftPolygonIndex, rightPolygonIndex;
	// private Point3D loc2d, loc3d;
	// private double left_mountain, left_valley, right_mountain, right_valley;
	// private final boolean isSimulation = true;
	private Point3D cen;
	private double angle;
	private boolean isVisited;
	private boolean isAdjusted;

	// constructor
	public Hinge(int address) {
		this.address = address;
		cen = new Point3D(); // init the center of the hinge as (0, 0, 0)
		angle = 0.0;
		isVisited = false;
		isAdjusted = false;
	}

	/*public boolean isAdjusted() {
		return this.isAdjusted;
	}*/

	public void setVisited(boolean b) {
		this.isVisited = b;
	}

	public boolean isVisited() {
		return this.isVisited;
	}

	public float[] getColor() {
		return AddressBook.hingeColorMap.get(Integer.valueOf(this.address));
	}

	// set, get the center of the hinge
	public void setCenter(Point3D p) {
		this.cen = p;
	}

	public Point3D getCenter() {
		return cen;
	}

	public boolean isLeftPolygon(Polygon p) {
		if (leftPolygon == null) // there is no polygon connected to the left
			return false;
		return p.getAddress() == leftPolygon.getAddress();
	}

	public boolean isRightPolygon(Polygon p) {
		if (rightPolygon == null) // there is no polygon connected to the right
			return false;
		return p.getAddress() == rightPolygon.getAddress();
	}

	// pot readings
	// set, get left potentiometer reading
	public void setLeftPot(int leftPot) {
		this.leftPot = leftPot;
	}

	public int getLeftPot() {
		return this.leftPot;
	}

	// set, get right potentiometer reading
	public void setRightPot(int rightPot) {
		this.rightPot = rightPot;
	}

	public int getRightPot() {
		return this.rightPot;
	}

	// left and right polygons
	// set, get left polygon
	public void setLeftPolygon(Polygon lp) {
		this.leftPolygon = lp;
	}

	public Polygon getLeftPolygon() {
		return this.leftPolygon;
	}

	// set, get right polygon
	public void setRightPolygon(Polygon rp) {
		this.rightPolygon = rp;
	}

	public Polygon getRightPolygon() {
		return this.rightPolygon;
	}

	// left and right polygon indices
	// set, get left polygon index
	public void setLeftPolygonIndex(int lpi) {
		this.leftPolygonIndex = lpi;
	}

	public int getLeftPolygonIndex() {
		return this.leftPolygonIndex;
	}

	// set, get right polygon index
	public void setRightPolygonIndex(int rpi) {
		this.rightPolygonIndex = rpi;
	}

	public int getRightPolygonIndex() {
		return this.rightPolygonIndex;
	}

	// get the address of the hinge
	public int getAddress() {
		return this.address;
	}

	// get the angle based on the 2 potentiometers
	public double getAngleDeg() {
		double leftAngle, rightAngle;
		double bia = 255.0/220.0*20;
		double leftPotAdjusted = leftPot, rightPotAdjusted = rightPot;
		
		if (leftPot < bia) {
			leftPotAdjusted = 0;
		} else {
			if (leftPot > 255-bia) {
				leftPotAdjusted = 255-bia;
			}
			leftPotAdjusted -= bia;
		}
		
		if (rightPot < bia) {
			rightPotAdjusted = 0;
		} else{ 
			if (rightPot > 255-bia) {
				rightPotAdjusted = 255-bia;
			}
			rightPotAdjusted -= bia;
		}

		leftAngle = leftPotAdjusted / 255.0 * 220; // degree, 0-20, 20-235, 235-255
		rightAngle = rightPotAdjusted / 255.0 * 220;
		return leftAngle + rightAngle;
	}
	
	public double getRawAngleFromPot(){
		double leftAngle, rightAngle;
		double bia = 255.0/220.0*20;
		double leftPotAdjusted = leftPot, rightPotAdjusted = rightPot;
		
		if (leftPot < bia) {
			leftPotAdjusted = 0;
		} else {
			if (leftPot > 255-bia) {
				leftPotAdjusted = 255-bia;
			}
			leftPotAdjusted -= bia;
		}
		
		if (rightPot < bia) {
			rightPotAdjusted = 0;
		} else{ 
			if (rightPot > 255-bia) {
				rightPotAdjusted = 255-bia;
			}
			rightPotAdjusted -= bia;
		}

		leftAngle = leftPotAdjusted / 255.0 * 220; // degree, 0-20, 20-235, 235-255
		rightAngle = rightPotAdjusted / 255.0 * 220;
		angle = Math.toRadians(leftAngle + rightAngle);
			
		return angle;
	}

	// get the angle based on the 2 potentiometers
	public double getAngle() {
		if(isAdjusted)
			return angle;
		else
			return this.getRawAngleFromPot();
	}

	public void setAngle(double ang) {
		this.isAdjusted = true;
		this.angle = ang;
	}

	public String getSide(Polygon p) {
		if (this.getLeftPolygon().getAddress() == p.getAddress())
			return "LEFT";
		else if (this.getRightPolygon().getAddress() == p.getAddress())
			return "RIGHT";
		return null;
	}

	public String toString() {
		System.out.println("Hinge " + Integer.toString(this.address, 16));
		String str = "Hinge " + Integer.toString(this.address, 16) + ": ";
		str += "(" + leftPot + ", " + rightPot + ")";
		str += "[" + leftPolygon.getType() + " "
				+ Integer.toString(leftPolygon.getAddress(), 16) + "-"
				+ leftPolygonIndex + ", " + rightPolygon.getType() + " "
				+ Integer.toString(rightPolygon.getAddress(), 16) + "-"
				+ rightPolygonIndex + "]";

		return str;
	}
}
