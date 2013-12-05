package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import model.SentencePair.SentEntry;
import model.stanford.StanfordResult;
import model.stanford.StanfordSentence;
import model.stanford.StanfordToken;
import model.stanford.StanfordXMLReader;
import model.syntaxTree.GraphNode;
import util.Common;

public class SentAttri {

	public StanfordSentence ss;
	// SemanticRole
	public HashMap<Integer, GraphNode> vertexMap;

	public ArrayList<SemanticRole> srls;

	public Mention cands[];

	public Mention anaphor;

	public Mention ant;

	public int connect = -1;
	
	public ArrayList<String> polarityFeas;
	
	public SentEntry entry;
	
	public ArrayList<String> tokens;

	public String text;
	
	public SentAttri() {
		this.vertexMap = new HashMap<Integer, GraphNode>();
		this.srls = new ArrayList<SemanticRole>();
		this.text = "";
		this.tokens = new ArrayList<String>();
	}
	
	public SentAttri(StanfordSentence ss) {
		this.ss = ss;
		this.vertexMap = new HashMap<Integer, GraphNode>();
		this.srls = new ArrayList<SemanticRole>();
		this.cands = new Mention[2];
	}

	public static ArrayList<SentAttri> load() {
		ArrayList<SentAttri> sents = new ArrayList<SentAttri>();
		int sentID = 0;
		StanfordResult result = StanfordXMLReader.read("chiRaw.txt.xml");

		ArrayList<String> lines = Common.getLines("chiSRL.out");
		ArrayList<String[]> subLines = new ArrayList<String[]>();
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);

			if (line.trim().isEmpty()) {
				SentAttri sa = new SentAttri(result.getSentence(sentID++));
				sents.add(sa);
				GraphNode root = new GraphNode(0);
				sa.vertexMap.put(0, root);
				for (int j = 0; j < subLines.size(); j++) {
					GraphNode dn = new GraphNode(j + 1);
					sa.vertexMap.put(j + 1, dn);
				}

				for (int j = 0; j < subLines.size(); j++) {
					GraphNode dn = sa.vertexMap.get(j + 1);
					try {
					GraphNode node = sa.vertexMap.get(Integer
							.parseInt(subLines.get(j)[8]));
					
					String edgeName = subLines.get(j)[10];
					dn.addEdge(node, edgeName);
					} catch (Exception e) {
						System.out.println(subLines.get(j));
					}
				}

				for (int j = 12; j < subLines.get(0).length; j++) {
					if (j == 13) {
						continue;
					}
					SemanticRole srl = new SemanticRole();
					sa.srls.add(srl);
				}

				int yid = 0;
				for (int j = 0; j < subLines.size(); j++) {
					for (int k = 12; k < subLines.get(0).length; k++) {
						if (k == 13) {
							continue;
						}
						String token = subLines.get(j)[k];
						if (token.equals("Y")) {
							Mention pred = new Mention();
							pred.start = pred.end = j;
							pred.extent = subLines.get(j)[1];
							try {
							sa.srls.get(yid++).pred = pred;
							} catch (Exception e) {
								System.out.println(new ArrayList(Arrays.asList(subLines.get(j))));
								System.out.println(i);
								System.exit(1);
							}
						} else if (!token.equals("_")) {
							Mention arg = new Mention();
							arg.start = arg.end = j;
							arg.extent = subLines.get(j)[1];
							sa.srls.get(k - 14).addRole(token, arg);
						}
					}
				}

				for (int m = 0; m < sa.ss.tokens.size(); m++) {
					StanfordToken tk = sa.ss.tokens.get(m);
					if (tk.word.equals(",") || tk.word.equals("，")) {
						String nextPOS = sa.ss.tokens.get(m + 1).POS;
						if(nextPOS.equals("AD") || nextPOS.equals("P") || nextPOS.equals("CS")) {
							sa.connect = m + 1;	
						}
						break;
					}
				}
				subLines.clear();
			} else {
				subLines.add(line.split("\\s+"));
			}
		}
		return sents;
	}

	public static void main(String args[]) {
		load();
	}
}
