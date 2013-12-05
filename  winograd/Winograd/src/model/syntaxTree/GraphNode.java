package model.syntaxTree;

import java.util.ArrayList;
import java.util.HashMap;

import util.Common;

public class GraphNode {

	public ArrayList<GraphNode> nexts;

	public int value;

	public HashMap<Integer, String> edgeName;

	public GraphNode backNode;
	
	public GraphNode(int value) {
		this.value = value;
		this.nexts = new ArrayList<GraphNode>();
		this.edgeName = new HashMap<Integer, String>();
	}

	public void addEdge(GraphNode node, String edgeName) {
		if(node==null || edgeName==null) {
			Common.bangErrorPOS("DONOT insert null");
		}
		
		if(!this.edgeName.containsKey(node.value)) {
			this.nexts.add(node);
			this.edgeName.put(node.value, edgeName);
		}
	}
	
	public String getEdgeName(GraphNode n2) {
		return this.edgeName.get(n2.value);
	}
}
