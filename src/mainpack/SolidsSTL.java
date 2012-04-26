package mainpack;

import java.nio.ByteBuffer;
import java.util.Vector;

import mygeom.FacetSTL;
import mygeom.SolidSTL;

public class SolidsSTL {
	private Vector<SolidSTL> solids;
	private byte red = 0;
	private byte green = 0;
	private byte blue = 0;
	private boolean isDebug = false;

	public SolidsSTL() {
		solids = new Vector<SolidSTL>();
	}

	public void append(Vector<SolidSTL> ns) {
		assignColor(ns);
		solids.addAll(ns);
	}

	public void assignColor(Vector<SolidSTL> ns) {
		SolidSTL solid;
		Vector<FacetSTL> faces;
		
		// assign colors to each face of each new solid
		for (int i = 0; i < ns.size(); i++) { // for each new solid
			solid = ns.get(i);
			faces = solid.getFaces();
			for (int j = 0; j < faces.size(); j++) { // for each face
				faces.get(j).assignColor(getNextColor());
			}
		}
	}
	
	public int[] getFaceByColor(ByteBuffer bb){
		if(isDebug)
			System.out.println("Check bb: " + bb.get(0) + ", " + bb.get(1) + ", " + bb.get(2));
		
		int[] indices = {-1, -1};
		SolidSTL solid;
		Vector<FacetSTL> faces;
		FacetSTL face;
		byte[] color;
		
		for(int i=0; i<solids.size(); i++){//for each solid
			solid = solids.get(i);
			faces = solid.getFaces();
			for(int j=0; j<faces.size(); j++){//for each face
				face = faces.get(j);
				color = face.getColor();
				if (bb.get(0) == color[0] && bb.get(1) == color[1]
						&& bb.get(2) == color[2]){
					indices[0] = i;
					indices[1] = j;
				}
			}
		}
		
		return indices;
	}
	
	public void deleteSolidAt(int index){
		solids.remove(index);
	}
	
	public void clearSolids(){
		solids.clear();
	}
	
	public Vector<SolidSTL> getSolids() {
		return solids;
	}

	// get next unique color
	public byte[] getNextColor() {
		byte[] color = new byte[3];
		for (; red <= 125 && red >= 0;) {
			for (; green <= 125 && green >= 0;) {
				for (; blue <= 125 && blue >= 0;) {
					color[0] = red;
					color[1] = green;
					color[2] = blue;
					blue += 20;
					return color;
				}
				green += 20;
				blue = 0;
			}
			red += 20;
			green = 0;
		}

		color[0] = color[1] = color[2] = -1;
		return color;
		// throw new Exception("");
	}
}
