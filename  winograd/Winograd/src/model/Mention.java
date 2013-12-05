package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import machineLearning.WinoUtil;
import model.CoNLL.CoNLLSentence;
import model.syntaxTree.MyTreeNode;

public class Mention implements Comparable<Mention>, Serializable {

	/**
	 * 
	 */
	public HashSet<Integer> verbInts;
	public HashSet<Integer> verbRestInts;

	public String verb;
	public String restVerb;
	public String attribute;

	public String copular;

	private static final long serialVersionUID = 1L;
	public int start = -1;
	public int end = -1;
	public String extent = "";

	public ArrayList<SemanticRole> srls = new ArrayList<SemanticRole>();
	public ArrayList<String> roles = new ArrayList<String>();

	public Mention headM;

	public String str;

	public Mention verbM;

	public Mention adjM;

	public GramType gramType = GramType.Other;

	public static enum GramType {
		Subj, Obj, Other
	};

	public HashSet<String> getVerbExtent(SentAttri sa) {
		HashSet<String> vs = new HashSet<String>();
		if (this.verbM == null) {
			return vs;
		} else {
//			String v = WinoUtil.getStrFromInts(
//					WinoUtil.getFullV(sa, this.verbM), sa);
			String v = this.verbM.extent;
			HashSet<String> set = WinoUtil.hownet.getRelaxSynonym(v, "V");
			
			if (this.gramType != GramType.Subj) {
				HashSet<String> verbs = WinoUtil.getPassiveVerb(v);
				if (verbs.size() == 0) {
					for(String s : set) {
						vs.add("被 " + s);
					}
				} else {
					vs.addAll(verbs);
					// System.out.println(v + ":" + verbs);
				}
			} else {
				vs.addAll(set);
			}
			return vs;
		}
	}

	public Entity entity;

	public Mention antecedent;

	public String msg;

	public double MI;

	public boolean notInChainZero;

	public int sentenceID;

	public CoNLLSentence s;

	public String head = "";

	public int entityIndex;

	public int startInS;
	public int endInS;

	public int headInS;

	public MyTreeNode V;

	public MyTreeNode NP;

	public String NE = "O";

	public boolean isFS = false;

	public boolean isBest = false;

	public boolean isNEPerson() {
		if (this.NE.equals("PERSON")) {
			return true;
		}
		if (WinoUtil.humanWord.contains(this.head)) {
			// return true;
		}
		return false;
	}

	// TODO
	public boolean isQuoted = false;

	public int getSentenceID() {
		return sentenceID;
	}

	public void setSentenceID(int sentenceID) {
		this.sentenceID = sentenceID;
	}

	public int hashCode() {
		String str = this.start + "," + this.end;
		return str.hashCode();
	}

	public boolean equals(Object em2) {
		if (this.start == ((Mention) em2).start
				&& this.end == ((Mention) em2).end) {
			return true;
		} else {
			return false;
		}
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String getExtent() {
		return extent;
	}

	public void setExtent(String extent) {
		this.extent = extent;
	}

	public String getHead() {
		return head;
	}

	public void setHead(String head) {
		this.head = head;
	}

	public Mention() {

	}

	public Mention(int start, int end) {
		this.start = start;
		this.end = end;
	}

	// (14, 15) (20, -1) (10, 20)
	public int compareTo(Mention emp2) {
		int diff = this.start - emp2.start;
		if (diff == 0)
			return emp2.end - this.end;
		else
			return diff;
		// if(this.getE()!=-1 && emp2.getE()!=-1) {
		// int diff = this.getE() - emp2.getE();
		// if(diff==0) {
		// return this.getS() - emp2.getS();
		// } else
		// return diff;
		// } else if(this.getE()==-1 && emp2.headEnd!=-1){
		// int diff = this.getS() - emp2.getE();
		// if(diff==0) {
		// return -1;
		// } else
		// return diff;
		// } else if(this.headEnd!=-1 && emp2.headEnd==-1){
		// int diff = this.getE() - emp2.getS();
		// if(diff==0) {
		// return 1;
		// } else
		// return diff;
		// } else {
		// return this.getS()-emp2.getS();
		// }
	}

	public String toName() {
		String str = this.start + "," + this.end;
		return str;
	}

	public String toString() {
		String str = this.start + "," + this.end;
		return str;
	}
}