package mainpack;
import java.util.HashMap;

import javax.media.opengl.GLAutoDrawable;


public class AddressBook {
	public static final int HINGE_NUM = 20;
	public static final int TRIANGLE_EQU_NUM = 10;
	public static final int TRIANGLE_ISO_NUM = 8;
	public static final int SQUARE_NUM = 6;
	public static final int PENTAGON_NUM = 6;
	public static final int HEXAGON_NUM = 4;
	
	public static final String Triangle_Equ_Str = "Triangle_Equilateral";
	public static final String Triangle_Iso_Str = "Triangle_Isosceles";
	public static final String Square_Str   = "Square";
	public static final String Pentagon_Str = "Pentagon";
	public static final String Hexagon_Str  = "Hexagon";
	
	public static final int hingeAddress[]  	= {0x2, 0x4, 0x6, 0x8, 0xA, 0xC, 0xE, 
		0x10, 0x12, 0x14, 0x16, 0x18, 0x1A, 0x1C, 0x1E, 0x20, 0x22, 0x24, 0x26, 0x28};
	public static final int triangleEquilateralAddress[] = {0x30, 0x32, 0x34, 0x36, 0x38, 0x3A, 0x3C, 0x3E, 0x40, 0x42};
	public static final int triangleIsoscelesAddress[]   = {0x50, 0x52, 0x54, 0x56, 0x58, 0x5A, 0x5C, 0x5E};
	public static final int squareAddress[]   = {0x60, 0x62, 0x64, 0x66, 0x68, 0x6A};
	public static final int pentagonAddress[] = {0x70, 0x72, 0x74, 0x76, 0x78, 0x7A};
	public static final int hexagonAddress[]  = {0x80, 0x82, 0x84, 0x86};
	//2D array to store hinge data
	//for each hinge: left - v, m; right - v, m
	private static final int hingeData[][] = {{4, 239, 6, 251}, {0, 243, 6, 252}, {0, 242, 12, 254}, {0, 238, 12, 254}, 
			{0, 236, 6, 246}, {0, 242, 7, 253}, {0, 239, 13, 253}, {1, 232, 20, 254}, {0, 242, 9, 248}};
	private static final float hingeColors[][] = {{0.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 0.0f}, //0x2, 0x4 Green
												  {1.0f, 1.0f, 0.0f}, {1.0f, 1.0f, 0.0f}, //0x6, 0x8 Yellow
												  {0.0f, 0.0f, 1.0f}, {0.0f, 0.0f, 1.0f}, //0xA, 0xC Blue
												  {0.5f, 0.5f, 0.5f}, {0.5f, 0.5f, 0.5f}, //0xE, 0x10 Gray
												  {1.0f, 1.0f, 1.0f}, {1.0f, 1.0f, 1.0f}, //0x12,0x14 White
												  {1.0f, 0.0f, 0.0f}, {1.0f, 0.0f, 0.0f}, //0x16,0x18 Red
												  {0.0f, 1.0f, 0.0f}, {1.0f, 1.0f, 0.0f}, //0x1A,0x1C Green, Yellow
												  {0.0f, 0.0f, 1.0f}, {1.0f, 1.0f, 0.0f}, //0x1E,0x20 Blue, Yellow
												  {1.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 1.0f}, //0x22,0x24 Red, Blue
												  {0.0f, 0.0f, 0.0f}, {0.0f, 0.0f, 0.0f}};//0x26,0x28 Black
	
	//private static final HashMap<Integer, int[]> hingeMap = new HashMap<Integer, int[]>();
	public static final HashMap<Integer, float[]> hingeColorMap = new HashMap<Integer, float[]>();
	
	private static final byte pentagonColors[][] = {{(byte)120, 0, 0}, {(byte)100, 0, 0}, 
		{(byte)80, 0, 0}, {60, 0, 0}, {40, 0, 0}, {20, 0, 0}};
	public static final HashMap<Integer, byte[]> pentagonColorMap = new HashMap<Integer, byte[]>();
	
	public static AddressBook createAddressBook(){
		return new AddressBook();
	}
	
	/*
	 * private constructor
	 */
	private AddressBook(){
		initHingeMap();
		initPentagonMap();
	}
	
	/*
	 * build hashmap <address, angle info>
	 */
	private void initHingeMap(){
		/*for(int i = 0; i < hingeAddress.length; i++){
			hingeMap.put(hingeAddress[i], hingeData[i]);
		}*/
		for(int i = 0; i < HINGE_NUM; i++){
			hingeColorMap.put(hingeAddress[i], hingeColors[i]);
		}
	}
	
	private void initPentagonMap(){
		for(int i=0; i<PENTAGON_NUM; i++){
			pentagonColorMap.put(pentagonAddress[i], pentagonColors[i]);
		}
	}
	/*
	 * get angle info of a hinge based on the address
	 */
	/*public static int[] getHingeDataAt(byte address){
		return hingeMap.get(address);
	}*/
	
	/*
	 * check if a hinge address is valid
	 */
	/*public static boolean isValidHinge(int address){
		for(int i=0; i<hingeAddress.length; i++){
			if(address == hingeAddress[i])
				return true;
		}
		return false;
	}*/
	
	/*
	 * check if a polygon address is valid
	 */
	/*public static boolean isValidTriangleEquilateral(int address){
		for(int i=0; i<triangleEquilateralAddress.length; i++){
			if(address == triangleEquilateralAddress[i])
				return true;
		}
		return false;
	}*/
}
