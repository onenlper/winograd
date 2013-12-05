package gigaword;

import java.util.ArrayList;

import util.Common;

import model.CoNLL.CoNLLDocument;
import model.CoNLL.CoNLLPart;
import model.CoNLL.CoNLLSentence;
import model.CoNLL.CoNLLWord;
import model.syntaxTree.MyTreeNode;

public class ParseGiga {

	public static void main(String args[]) {
		if(args.length<1) {
			System.err.println("Java ~ ");
			System.exit(1);
		}
		ArrayList<String> files = Common.getLines(args[0]);
		for(int i=0;i<files.size();i++) {
			String file = files.get(i);
			System.err.println(file + "\t" + i);
			try {
			run(file);
			} catch(Exception e) {
				System.err.println("An exception");
			}
		}
	}

	private static void run(String file) {
		CoNLLDocument doc = new CoNLLDocument(
				file);
//		System.out.println("Read in..");
//		System.out.println(doc.getParts().size());
		
		for (CoNLLPart part : doc.getParts()) {
		loop2:for (CoNLLSentence s : part.getCoNLLSentences()) {
				for (int i = 0; i < s.words.size(); i++) {
					CoNLLWord w = s.words.get(i);
					if (i != 0
							&& (s.words.get(i - 1).word.equals(",") || s.words
									.get(i - 1).word.equals("，"))) {
						
						//skip AD if any
						int a = i;
						while(s.words.get(a).posTag.equals("AD")||s.words.get(a).posTag.equals("CS")) {
							a++;
							if(a==s.words.size()) {
								continue loop2;
							}
						}
						w = s.words.get(a);
						String connector = "";
						if(a!=i) {
							connector = s.words.get(i).word;
						}
						MyTreeNode leaf = s.getSyntaxTree().leaves
								.get(w.indexInSentence);
						ArrayList<MyTreeNode> ancestors = leaf.getAncestors();
						MyTreeNode VP = null;
						for (MyTreeNode ancestor : ancestors) {
							if(ancestor.getLeaves().get(0)==leaf && ancestor.value.equals("VP")) {
								VP = ancestor;
								break;
							}
						}
						int start = i-2;
						if(VP==null||start<0) {
							continue;
						}
						// find previous big vp
						while(start>0) {
							if(s.words.get(start).word.equals(",")||s.words.get(start).word.equals("，")) {
								break;
							}
							start--;
						}
						MyTreeNode vp2 = null;
						loop: for(int k=i;k>=start;k--) {
							CoNLLWord t = s.words.get(k);
							if(t.posTag.startsWith("V")) {
								ArrayList<MyTreeNode> ancestor2 = s.getSyntaxTree().leaves.get(t.indexInSentence).getAncestors();
								for(MyTreeNode m : ancestor2) {
									ArrayList<MyTreeNode> leaves2 = m.getLeaves();
									if(m.value.equals("VP") && leaves2.get(0).leafIdx>=start && leaves2.get(leaves2.size()-1).leafIdx<i) {
										vp2 = m;
										break loop;
									}
								}
							}
						}
						if(vp2==null) {
							continue;
						}
						String h1 = getHead(VP);
						String h2 = getHead(vp2);
//						if(!connector.isEmpty() && (h1.startsWith("被")||h2.startsWith("被"))) {
//							System.out.println(s.getText());
							System.out.println(h2 + "#" + h1 + "#" + connector);
//						}
					}
				}
			}
		}
	}
	
	public static String getHead(MyTreeNode node) {
		String ret = "";
		boolean bei = false;
		ArrayList<MyTreeNode> leaves = node.getLeaves();
		boolean bu = false;
		for(int i=0;i<leaves.size();i++) {
			MyTreeNode leaf = leaves.get(i);
			if(leaf.parent.value.startsWith("LB") || leaf.parent.value.startsWith("SB")) {
				bei = true;
			}
			if(leaf.parent.value.startsWith("V")) {
				ret = leaf.value;
				
				if(i>0 && leaves.get(i-1).value.equals("不")) {
					bu = true;
				}
				
				while(i+1<leaves.size() && leaves.get(i+1).parent.value.startsWith("V")) {
					ret = ret + " " + leaves.get(i+1).value;
					i++;
				}
				
				break;
			}
		}
		if(bei) {
			ret = "被 " + ret;
		}
		if(bu) {
			ret = "不 " + ret;
		}
		return ret;
	}
}
