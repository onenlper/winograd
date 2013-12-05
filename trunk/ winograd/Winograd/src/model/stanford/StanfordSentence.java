package model.stanford;

import java.util.ArrayList;
import java.util.HashMap;

import util.Common;

import model.stanford.StanfordXMLReader.StanfordDep;
import model.syntaxTree.GraphNode;
import model.syntaxTree.MyTree;

public class StanfordSentence {
	public int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ArrayList<StanfordToken> getTokens() {
		return tokens;
	}
	
	public StanfordToken getToken(int i) {
		return this.tokens.get(i);
	}

	public void setTokens(ArrayList<StanfordToken> tokens) {
		this.tokens = tokens;
	}

	public String getParse() {
		return parse;
	}

	public void setParse(String parse) {
		this.parse = parse;
	}

	public MyTree getParseTree() {
		return parseTree;
	}

	public void setParseTree(MyTree parseTree) {
		this.parseTree = parseTree;
	}

	public ArrayList<StanfordDep> getBasicDependencies() {
		return basicDPLST;
	}

	public void setBasicDependencies(ArrayList<StanfordDep> basicDependencies) {
		this.basicDPLST = basicDependencies;
	}

	public ArrayList<StanfordDep> getCollapsedDependencies() {
		return collapDPLst;
	}

	public void setCollapsedDependencies(ArrayList<StanfordDep> collapsedDependencies) {
		this.collapDPLst = collapsedDependencies;
	}

	public ArrayList<StanfordDep> getCollapsedCcprocessedDependencies() {
		return collapCCDPLst;
	}

	public void setCollapsedCcprocessedDependencies(ArrayList<StanfordDep> collapsedCcprocessedDependencies) {
		this.collapCCDPLst = collapsedCcprocessedDependencies;
	}

	public ArrayList<StanfordToken> tokens;
	public String parse = "";
	public MyTree parseTree;
	public ArrayList<StanfordDep> basicDPLST;
	public ArrayList<StanfordDep> collapDPLst;
	public ArrayList<StanfordDep> collapCCDPLst;
	
	public HashMap<Integer, GraphNode> basicDP;
	public HashMap<Integer, GraphNode> collapDP;
	public HashMap<Integer, GraphNode> collapCCDP;

	public StanfordSentence() {
		this.tokens = new ArrayList<StanfordToken>();
		this.basicDPLST = new ArrayList<StanfordDep>();
		this.collapDPLst = new ArrayList<StanfordDep>();
		this.collapCCDPLst = new ArrayList<StanfordDep>();
		
		this.basicDP = new HashMap<Integer, GraphNode>();
		this.collapDP = new HashMap<Integer, GraphNode>();
		this.collapCCDP = new HashMap<Integer, GraphNode>();
	}

	public void addDP(HashMap<Integer, GraphNode> map, ArrayList<StanfordDep> depList, StanfordDep sd) {
		depList.add(sd);
		GraphNode dep = map.get(sd.dependentId);
		if(dep==null) {
			dep = new GraphNode(sd.dependentId);
			map.put(dep.value, dep);
		}
		
		GraphNode gov = map.get(sd.governorId);
		if(gov==null) {
			gov = new GraphNode(sd.governorId);
			map.put(gov.value, gov);
		}
		
		dep.addEdge(gov, sd.type + "+");
		gov.addEdge(dep, sd.type + "-");
	}
	
	public void addToken(StanfordToken token) {
		this.tokens.add(token);
	}
	
	public String getText() {
		StringBuilder sb = new StringBuilder();
		for(StanfordToken st : this.tokens) {
			sb.append(st.word).append(" ");
		}
		return sb.toString().trim();
	}
}