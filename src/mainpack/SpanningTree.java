package mainpack;

import java.util.Vector;

public class SpanningTree{
	private TreeNode root = null;
	DataStructure ds;
	
	public SpanningTree(DataStructure ds){
		this.ds = ds;
	}
	
	public void insert(int index){
		if(root == null)
			root = new TreeNode(Integer.valueOf(index), null, new Vector<TreeNode>());
		else
			insert(root, index);
	}
	
	//index - the value to be inserted
	//tn - the root of the subtree
	public boolean insert(TreeNode tn, int index){
		//System.out.println("insert ***");
		//System.out.println("tn - " + tn.getValue() + "; " + "index = " + index);
		Integer v = Integer.valueOf(index);
		Vector<TreeNode> children;
		Hinge[][] adjmat = ds.getAdjacentMatrix();
		boolean found = false;
		
		if(adjmat[tn.getValue()][index] != null){//two polygons are connected
			tn.getChildren().add(new TreeNode(Integer.valueOf(index), tn, new Vector<TreeNode>()));
			return true;
		}else{
			children = tn.getChildren();
			for(int i=0; i<children.size(); i++){
				found = insert(children.get(i), index);
				if(found) break;
			}
		}
		
		//this.printSpanningTree();
		//System.out.println("******");
		return found;
	}
	
	public void printSpanningTree(){
		System.out.println("printSpanningTree");
		if(root == null)
			return;
		printSpanningTreeHelper(root);
	}
	
	public void printSpanningTreeHelper(TreeNode tn){
		System.out.println("n: " + tn.getValue());
		Vector<TreeNode> children = tn.getChildren();
		System.out.print("children: ");
		for(int i=0; i<children.size(); i++){
			System.out.print(children.get(i).getValue() + " ");
		}
		System.out.println();
		
		for(int i=0; i<children.size(); i++){
			printSpanningTreeHelper(children.get(i));
		}
	}
	
	public Vector<Integer> path2Root(int i){
		Vector<Integer> path = new Vector<Integer>();
		TreeNode tn = search(i);

		while(tn!=null){
			path.add(0, tn.getValue());
			tn = tn.getParent();
		}
		return path;
	}
	
	public TreeNode search(int index){
		if(root == null)
			return null;
		
		return search(root, index);
	}
	
	public TreeNode search(TreeNode tn, int index){
		TreeNode n = null;
		if(tn.getValue() == index)
			return tn;
		else{
			for(int i=0; i<tn.getChildren().size(); i++){
				n = search(tn.getChildren().get(i), index);
				if(n != null)
					return n;					
			}
		}
		return n;
	}
	
	private class TreeNode{
		private Integer value;
		private TreeNode parent = null;
		private Vector<TreeNode> children;
		
		public TreeNode(Integer value, TreeNode parent, Vector<TreeNode>kids){
			children = new Vector<TreeNode>();
			
			this.value = value;
			this.parent = parent;
			for(int i=0; i<kids.size(); i++){
				children.add(kids.get(i));
			}
		}
		
		public Integer getValue(){
			return value;
		}
		
		public TreeNode getParent(){
			return parent;
		}
		
		public Vector<TreeNode> getChildren(){
			return children;
		}
	}
}
