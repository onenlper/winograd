package machineLearning;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import model.Mention;
import model.SemanticRole;
import model.SentAttri;
import model.stanford.StanfordResult;
import model.stanford.StanfordSentence;
import model.stanford.StanfordToken;
import model.stanford.StanfordXMLReader;
import model.stanford.StanfordXMLReader.StanfordDep;
import model.syntaxTree.GraphNode;
import model.syntaxTree.MyTreeNode;
import util.ChineseConvert;
import util.Common;
import util.Hownet;
import util.Hownet.Entry;

public class WinoUtil {

	static HashSet<String> posSentSet;
	static HashSet<String> negSentSet;

	static HashSet<String> posComSet;
	static HashSet<String> negComSet;

	static HashSet<String> ntuPosSet;
	static HashSet<String> ntuNegSet;

	static HashSet<String> attriWord;
	public static HashSet<String> humanWord;

	public static Hownet hownet;
	public static HashMap<String, ArrayList<StanfordSentence>> searchMap;

	public static HashMap<String, Integer> verbPairCount;

	/*
	 * existent, experiencer, scope, cause, partof, sourcewhole, location,
	 * locationthru, direction, resultisa, beneficiary, contrast,
	 * patientproduct, patient, resultwhole, isa, partner, relevant, stateini,
	 * possessor, cost, agent, locationini, content, possession, source, whole,
	 * target, locationfin, resultevent
	 */

	public static HashSet<String> A0s = new HashSet<String>(Arrays.asList(
			"existent", "experiencer", "scope", "resultisa", "sourcewhole",
			"partner", "stateini", "possessor", "agent", "content", "source",
			"whole"));
	public static HashSet<String> A1s = new HashSet<String>(Arrays.asList(
			"partof", "location", "direction", "contrast", "locationfin",
			"patient", "isa", "PatientProduct", "resultwhole", "relevant",
			"locationini", "possession", "target", "locationfin"));
	public static HashSet<String> A2s = new HashSet<String>(
			Arrays.asList("beneficiary"));
	public static HashSet<String> Others = new HashSet<String>(Arrays.asList(
			"cause", "locationthru", "cost", "ResultEvent"));

	public static int getCount(String v1, String v2, String conn) {
		String key1 = v1 + "#" + v2 + "#" + conn;
		int c = 0;
		if (verbPairCount.containsKey(key1)) {
			c += verbPairCount.get(key1);
		}
		String key2 = v2 + "#" + v1 + "#" + conn;
		if (verbPairCount.containsKey(key2)) {
			c += verbPairCount.get(key2);
		}
		return c;
	}

	public static void init() {
		posSentSet = readDic("dict/utf8/pos_sentiment_chi.txt.utf8");
		negSentSet = readDic("dict/utf8/neg_sentiment_chi.txt.utf8");

		negComSet = readDic("dict/utf8/neg_comment_chi.txt.utf8");
		posComSet = readDic("dict/utf8/pos_comment_chi.txt.utf8");

		ntuPosSet = readDic("dict/utf8/NTUSD_positive_simplified.txt.utf8");
		ntuNegSet = readDic("dict/utf8/NTUSD_negative_simplified.txt.utf8");

		attriWord = new HashSet<String>();
		humanWord = new HashSet<String>();
		ArrayList<String> hownetLines = Common
				.getLines("dict/utf8/glossary.dat.utf8");
		for (String line : hownetLines) {
			String tokens[] = line.trim().split("\\s+");
			if (tokens[2].startsWith("aValue|属性值")
					|| tokens[2].startsWith("attribute|属性")) {
				attriWord.add(tokens[0]);
			}

			if (tokens[2].contains("human|人")) {
				humanWord.add(tokens[0]);
			}
		}
		loadCounts();
		initGoogleAltaf();
		loadVerbPair("verbPair");

		hownet = Hownet
				.loadHownet("/users/yzcchen/chen3/Winograd/Winograd/src/dict/utf8/glossary.dat.utf8");
		loadEventsTransfer();
	}

	private static boolean isInVerbPair(String str1, String str2) {
		ArrayList<Entry> entries1 = hownet.getEntryFromWord(str1);
		ArrayList<Entry> entires2 = hownet.getEntryFromWord(str2);

		for (Entry e1 : entries1) {
			for (String m : e1.meanings) {
				ArrayList<EventPair> eps = eventPairsMap.get(m);
			}
		}

		return false;
	}

	public static void loadVerbPair(String fn) {
		verbPairCount = new HashMap<String, Integer>();
		ArrayList<String> lines = Common.getLines(fn);
		for (String line : lines) {
			int a = line.lastIndexOf(" ");
			String key = line.substring(0, a);
			int b = key.lastIndexOf("#");
			key = key.substring(0, b + 1);
			int count = Integer.parseInt(line.substring(a + 1));
			verbPairCount.put(key, count);
		}
	}

	public static class EventPair {

		public String entry1;
		public String entry2;

		public enum ArgComb {
			arg0arg0, arg1arg1, arg0arg1, arg1arg0
		};

		public HashMap<String, String> right2Left;
		public HashMap<String, String> left2Right;

		public String type;

		public EventPair(String entry1, String entry2, String type) {
			this.entry1 = entry1;
			this.entry2 = entry2;
			this.right2Left = new HashMap<String, String>();
			this.left2Right = new HashMap<String, String>();
			this.type = type;
		}
	}

	public static ArrayList<EventPair> eventPairs;

	public static HashMap<String, ArrayList<EventPair>> eventPairsMap;

	static HashSet<String> types = new HashSet<String>();

	/*
	 * [implication, cause, possible consequence, precondition, mutual
	 * precondition, purpose, mutual precondition, precondition,
	 * interchangeable, mutual implication, hypernym, consequence]
	 */

	public static HashSet<String> left2Rights = new HashSet<String>(
			Arrays.asList("implication", "cause", "possible consequence",
					"precondition", "mutual precondition", "purpose",
					"interchangeable", "mutual implication", "hypernym",
					"consequence"));
	public static HashSet<String> right2Lefts = new HashSet<String>(
			Arrays.asList("mutual precodition", "interchangeable",
					"mutual implication"));

	public static void loadEventsTransfer() {
		eventPairs = new ArrayList<EventPair>();
		eventPairsMap = new HashMap<String, ArrayList<EventPair>>();
		ArrayList<String> lines = Common
				.getLines("/users/yzcchen/chen3/Winograd/Winograd/src/dict/utf8/eventsTransfer.txt");
		ArrayList<String> subLines = new ArrayList<String>();
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.trim().isEmpty()) {

				String l = subLines.get(0).trim();
				if (l.endsWith(";") || l.endsWith(".")) {
					l = l.substring(0, l.length() - 1);
				}
				int b = l.indexOf('[');
				int c = l.indexOf(']');
				String type = l.substring(b + 1, c);
				types.add(type);

				boolean left2Right = false;
				boolean right2Left = false;

				if (left2Rights.contains(type)) {
					left2Right = true;
				}
				if (right2Lefts.contains(type)) {
					right2Left = true;
				}
//				System.out.println(type);
				// System.out.println(l);
				l = l.replaceAll("\\[[^\\]]*\\]", "").replaceAll(
						"\\([^\\)]*\\)", "");
				String entry1 = "";
				String entry2 = "";
				int a = l.indexOf("==");
				entry1 = l.substring(0, a).trim();
				entry2 = l.substring(a + 2).trim();
				// System.out.println(entry1);
				// System.out.println(entry2);

				EventPair ep = new EventPair(entry1, entry2, type);
				eventPairs.add(ep);

				if (left2Right) {
					ArrayList<EventPair> ep1 = eventPairsMap.get(entry1);
					if (ep1 == null) {
						ep1 = new ArrayList<EventPair>();
						eventPairsMap.put(entry1, ep1);
					}
					ep1.add(ep);
				}
				if (right2Left) {
					ArrayList<EventPair> ep2 = eventPairsMap.get(entry2);
					if (ep2 == null) {
						ep2 = new ArrayList<EventPair>();
						eventPairsMap.put(entry2, ep2);
					}
					ep2.add(ep);
				}

				for (int j = 1; j < subLines.size(); j++) {
					l = subLines.get(j).trim();
					if (l.contains("(") || !l.contains("OF")) {
						// System.out.println(l);
						continue;
					}
					String tks[] = l.split("=");
					a = tks[0].indexOf(" OF");
					if (a == -1) {
						// System.out.println(l);
						continue;
					}
					String r1 = tks[0].substring(0, a).trim().toLowerCase();
					b = tks[1].indexOf(" OF");
					if (b == -1) {
						// System.out.println(l);
						continue;
					}
					String r2 = tks[1].substring(0, b).trim().toLowerCase();
					ep.left2Right.put(r1, r2);
					ep.right2Left.put(r2, r1);
				}
				subLines.clear();
			} else {
				subLines.add(line);
			}

		}
	}

	// public static boolean isRelation(String verb1, String verb2) {
	//
	//
	//
	// }

	//
	public static HashSet<String> getPassiveVerb(String verb) {
		ArrayList<Entry> entries = hownet.getEntryFromWord(verb, "V");
		HashSet<String> ret = new HashSet<String>();
		for (Entry entry : entries) {
			HashSet<String> means = entry.meanings;
			for (String mean : means) {
				if (eventPairsMap.containsKey(mean)) {
					for (EventPair ep : eventPairsMap.get(mean)) {
						HashMap<String, String> map = ep.left2Right;
						boolean opposite = false;
						for (String key : map.keySet()) {
							String val = map.get(key);
							if ((A0s.contains(key) && A1s.contains(val))
									|| (A1s.contains(key) && A0s.contains(val))) {
								opposite = true;
								break;
							}
						}

						if (opposite) {
							String oppositeMean = ep.entry1.equals(mean) ? ep.entry2
									: ep.entry1;
							ArrayList<Entry> oppEntries = hownet
									.getEntryFromMean(oppositeMean);
							for (Entry oppE : oppEntries) {
								if (!hownet.isRelaxSynonym(oppE.word, verb)
										&& oppE.POS.equals("V")) {
									ret.add(oppE.word);
								}
							}
						}
					}
				}
			}
		}
		return ret;
	}

	public static void main(String args[]) {
		init();
		// HashSet<String> pvs = getPassiveVerb("战胜");
		// System.out.println(pvs);
		loadEventsTransfer();
		System.out.println(types);
		// boolean syn = hownet.isRelaxSynonym("战胜", "打败");
		// System.out.println(syn);
		// loadSearchXML();
		// System.out.println(searchMap.size());
		// System.out.println(searchMap.keySet().iterator().next());
		// System.out.println(searchMap.get(searchMap.keySet().iterator().next()).size());
		// if (true) {
		// return;
		// }
		// loadSearchMate();
		// getPercent("枪喜欢", new HashSet<String>());
		// loadEventsTransfer();
		// HashSet<String> roles = new HashSet<String>();
		// for (EventPair ep : eventPairs) {
		// roles.addAll(ep.left2Right.keySet());
		// roles.addAll(ep.right2Left.keySet());
		// System.out.println(ep.entry1);
		// System.out.println(ep.entry2);
		//
		// for (String key : ep.left2Right.keySet()) {
		// System.out.println(key + "#" + ep.left2Right.get(key));
		// }
		// }
	}

	public static double getPercent(String key, HashSet<String> expectedDeps) {
		if (searchMapMate == null) {
			loadSearchMate();
		}
		ArrayList<SentAttri> sas = searchMapMate.get(key);
		if (sas == null) {
			return 1;
		}
		double good = 0;
		double all = 0;

		for (int i = 0; i < sas.size(); i++) {
			SentAttri sa = sas.get(i);
			HashSet<String> deps = new HashSet<String>();
			for (int id : sa.vertexMap.keySet()) {
				GraphNode node = sa.vertexMap.get(id);
				if (node.value == 0) {
					continue;
				}
				String w1 = sa.tokens.get(node.value - 1);
				for (GraphNode next : node.nexts) {
					if (next.value == 0) {
						continue;
					}
					String w2 = sa.tokens.get(next.value - 1);
					String edge = node.getEdgeName(next);
					String s = w1 + "-" + edge + "-" + w2;
					// System.out.println(s);
					deps.add(s);
				}
			}
			boolean same = true;
			for (String dep : expectedDeps) {
				if (!deps.contains(dep)) {
					same = false;
				}
			}
			if (same) {
				good++;
			}
			all++;
		}
		if (all == 0) {
			return 0;
		} else {
			return good / all;
		}
	}

	@Deprecated
	public static double getPercent2(String key, HashSet<String> expectedDeps) {
		boolean sw = false;
		if (key.equals("驴子饿了")) {
			sw = true;
		} else {
			sw = false;
		}

		if (searchMap == null) {
			loadSearchXML();
		}
		ArrayList<StanfordSentence> sss = searchMap.get(key);
		if (sss == null) {
			System.out.println(searchMap.size());
			return 1;
		}

		double good = 0;
		double all = 0;
		for (int i = 0; i < sss.size(); i++) {
			StanfordSentence ss = sss.get(i);
			String text = ss.getText().replaceAll("\\s+", "");

			boolean contain = contain(text, key);

			if (contain) {
				HashSet<String> deps = new HashSet<String>();
				for (StanfordDep dep : ss.basicDPLST) {
					String go = dep.getGovernor();
					String de = dep.getDependent();
					String ty = dep.type;
					String s = go + "-" + ty + "-" + de;
					deps.add(s);
					if (sw) {
						System.out.println(s);
					}
				}
				boolean same = true;
				for (String str : expectedDeps) {
					if (!deps.contains(str)) {
						same = false;
						// System.out.println(str);
						break;
					}
					if (sw) {
						System.out.println(str);
					}
				}
				if (same) {
					// System.out.println("@@@@@@@@@@@@@@@@@@@@@@as");
					good++;
				}
				all++;
			}
		}
		if (all == 0) {
			return 0;
		} else {
			return good / all;
		}
	}

	static HashMap<String, ArrayList<SentAttri>> searchMapMate;

	public static void loadSearchMate() {
		searchMapMate = new HashMap<String, ArrayList<SentAttri>>();
		ArrayList<String> lines = Common
				.getLines("/users/yzcchen/chen3/Winograd/Winograd/src/search.mate.out");

		ArrayList<String[]> subLines = new ArrayList<String[]>();
		ArrayList<SentAttri> sents = null;
		boolean isKeyword = true;
		String keyword = "";
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);

			if (line.trim().isEmpty()) {
				SentAttri sa = new SentAttri();

				GraphNode root = new GraphNode(0);
				sa.vertexMap.put(0, root);
				for (int j = 0; j < subLines.size(); j++) {
					GraphNode dn = new GraphNode(j + 1);
					sa.vertexMap.put(j + 1, dn);
					sa.tokens.add(subLines.get(j)[1]);
					sa.text += subLines.get(j)[1];
				}

				if (isKeyword) {
					keyword = sa.text;
					sents = new ArrayList<SentAttri>();
					searchMapMate.put(
							keyword.substring(0, keyword.length() - 1), sents);
					isKeyword = false;
					subLines.clear();
					continue;
				}

				if (sa.text.equals("我是用来分隔关键词的。")) {
					isKeyword = true;
					subLines.clear();
					continue;
				}

				for (int j = 0; j < subLines.size(); j++) {
					GraphNode dn = sa.vertexMap.get(j + 1);
					GraphNode node = sa.vertexMap.get(Integer.parseInt(subLines
							.get(j)[8]));
					String edgeName = subLines.get(j)[10];
					dn.addEdge(node, edgeName);
				}

				sents.add(sa);

				for (int j = 12; j < subLines.get(0).length; j++) {
					if (j == 13) {
						continue;
					}
					SemanticRole srl = new SemanticRole();
					sa.srls.add(srl);
				}

				int yid = 0;
				try {
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
								sa.srls.get(yid++).pred = pred;
							} else if (!token.equals("_")) {
								Mention arg = new Mention();
								arg.start = arg.end = j;
								arg.extent = subLines.get(j)[1];
								sa.srls.get(k - 14).addRole(token, arg);
							}
						}
					}
				} catch (Exception e) {
					for (String tks[] : subLines) {
						System.out.println(tks);
					}
				}
				subLines.clear();
			} else {
				subLines.add(line.split("\\s+"));
			}
		}
		// System.out.println(searchMapMate.size());
		// System.out.println(searchMapMate.keySet().iterator().next());
		// System.out.println(searchMapMate.get(searchMapMate.keySet().iterator().next()).size());
	}

	public static void loadSearchXML() {
		searchMap = new HashMap<String, ArrayList<StanfordSentence>>();
		StanfordResult sr = StanfordXMLReader.read("searchParse.txt.xml");
		int i = 0;
		while (true) {
			StanfordSentence ss = sr.sentences.get(i++);
			String key = ss.getText().replaceAll("\\s+", "");
			key = key.substring(0, key.length() - 1);
			ArrayList<StanfordSentence> sss = new ArrayList<StanfordSentence>();
			searchMap.put(key, sss);
			while (true) {
				ss = sr.sentences.get(i++);
				if (ss.getText().replaceAll("\\s+", "").equals("我是用来分隔关键词的。")) {
					break;
				} else {
					sss.add(ss);
				}
				if (i == sr.sentences.size()) {
					break;
				}
			}
			if (i == sr.sentences.size()) {
				break;
			}
		}
	}

	public static HashMap<String, ArrayList<String>> altafGoogle = new HashMap<String, ArrayList<String>>();

	public static void initGoogleAltaf() {
		ArrayList<String> lines = Common.getLines("googleAltaf");

		ArrayList<String> sublines = new ArrayList<String>();

		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			if (line.equals("------")) {
				String key = sublines.get(0);
				ArrayList<String> value = new ArrayList<String>();
				altafGoogle.put(key, value);
				if (sublines.size() > 1) {
					value.addAll(sublines.subList(1, sublines.size()));
				}
				sublines.clear();
			} else {
				sublines.add(line);
			}
		}
	}

	public static HashSet<String> querySet = new HashSet<String>();

	public static long getCount(String e) {
		if (e.endsWith("是") || e.endsWith("有") || e.endsWith("变得")) {
			return 0;
		}
		if (e.endsWith("有") || e.endsWith("给") || e.endsWith("要")
				|| e.endsWith("想") || e.endsWith("希望") || e.endsWith("会")
				|| e.endsWith("把") || e.endsWith("喜欢")) {
			return 0;
		}

		e = e.replace("》", "").replace("《", "");
		e = e.replaceAll("\\s+", "");
		if (countsMap.containsKey(e)) {
			long c = countsMap.get(e);
			if (c < 0) {
				querySet.add(e);
				return 0;
			}
			return countsMap.get(e);
		} else {
			querySet.add(e);
			return 0;
		}
	}

	public static HashMap<String, Long> countsMap = new HashMap<String, Long>();

	public static HashMap<String, ArrayList<String[]>> tuplesMap = new HashMap<String, ArrayList<String[]>>();

	public static boolean contain(String big, String small) {
		int j = -1;
		for (int i = 0; i < small.length(); i++) {
			char c = small.charAt(i);
			j = big.indexOf(c, j + 1);
			if (j == -1) {
				return false;
			}
		}
		return true;
	}

	public static String findText(String keyword, String wholeS) {
		wholeS = wholeS.replace("</b><br/><b>", "").replace("<br/>", "")
				.replace("</em><wbr></wbr><em>", "").toLowerCase()
				.replace("。", ",").replace(".", ",");
		wholeS = ChineseConvert.convertToSimplized(wholeS);
		keyword = keyword.toLowerCase();
		int start = -1;
		int end = -1;
		boolean find = false;
		while (true && wholeS.contains("<b>")) {
			start = wholeS.indexOf("<b>", start + 1);
			if (start != -1) {
				end = wholeS.indexOf("</b>", start + 1);
				if (end != -1) {
					String inner = wholeS.substring(start + 3, end);
					if (contain(inner, keyword)) {
						find = true;
						break;
					}
				} else {
					break;
				}
			} else {
				break;
			}
		}

		if (!find) {
			while (true && wholeS.contains("<em>")) {
				start = wholeS.indexOf("<em>", start + 1);
				if (start != -1) {
					end = wholeS.indexOf("</em>", start + 1);
					if (end != -1) {
						String inner = wholeS.substring(start + 3, end);
						if (contain(inner, keyword)) {
							find = true;
							break;
						}
					} else {
						break;
					}
				} else {
					break;
				}
			}
		}
		HashSet<Character> filter = new HashSet<Character>(Arrays.asList('：',
				'_', ':', '。', '，', '.', ',', ' ', '-', '（', '(', '）', ')',
				'？', '！', '|', '?', '!'));
		if (find) {
			while (start >= 0) {
				char c = wholeS.charAt(start - 1);
				if (filter.contains(c)) {
					break;
				} else {
					start--;
				}
			}

			while (end < wholeS.length()) {
				char c = wholeS.charAt(end);
				if (filter.contains(c)) {
					break;
				} else {
					end++;
				}
			}

			return wholeS.substring(start, end).replace("<em>", "")
					.replace("</em>", "").replace("<b>", "")
					.replace("</b>", "").replace("<span>", "")
					.replace("</span>", "").replace("<wbr>", "")
					.replace("</wbr>", "");
		} else {
			return "";
		}
	}

	public static void loadCounts() {
		ArrayList<String> lines = Common.getLines("counts.all");
		int i = 0;
		loop: while (i != lines.size()) {
			String query = lines.get(i++).trim();
			long count = Long.parseLong(lines.get(i++).trim());

			ArrayList<String[]> tuples = new ArrayList<String[]>();
			while (!lines.get(i).startsWith("------------")) {
				String url = "";
				String title = "";
				String content = "";

				while (!lines.get(i).startsWith("title:")) {
					url += lines.get(i++);
				}

				while (!lines.get(i).startsWith("content:")) {
					title += lines.get(i++);
				}

				while (!lines.get(i).trim().isEmpty()) {
					content += lines.get(i++);
				}
				String tuple[] = new String[3];
				tuple[0] = url;
				tuple[1] = title;
				tuple[2] = content;
				tuples.add(tuple);
				i++;
				i++;
				// System.out.println("URL:" + url);
				// System.out.println("TITLE:" + title);
				// System.out.println("CONTENT:" + content);
			}
			if (count <= 0 && countsMap.containsKey(query)) {
				i++;
				continue;
			}
			countsMap.put(query, count);
			tuplesMap.put(query, tuples);
			i++;
		}
	}

	public static boolean sameSemantic(SentAttri sa) {
		Mention cand1 = sa.cands[0];
		Mention cand2 = sa.cands[1];
		if (cand1.NE.equals(cand2.NE) && !cand1.NE.equals("O")) {
			return true;
		}
		return false;
	}

	public static Mention findCommonPred(SentAttri sa) {

		Mention cands[] = sa.cands;
		if (sa.cands[0].verbM != null && sa.cands[1].verbM != null
				&& sa.cands[0].verbM.equals(sa.cands[1].verbM)) {
			return sa.cands[0].verbM;
		}

		for (int i = 0; i < cands[0].srls.size(); i++) {
			String role1 = cands[0].roles.get(i);
			SemanticRole sr1 = cands[0].srls.get(i);
			for (int j = 0; j < cands[1].srls.size(); j++) {
				String role2 = cands[1].roles.get(j);
				SemanticRole sr2 = cands[1].srls.get(j);
				if (sr1 == sr2
						&& ((role1.equals("A0") && role2.equals("A1") || (role1
								.equals("A1") && role2.equals("A0"))))) {
					return sr1.pred;
				}
			}
		}
		return null;
	}

	public static boolean isCandidatesA0A1(SentAttri sa) {

		Mention cands[] = sa.cands;
		if (sa.cands[0].verbM != null && sa.cands[1].verbM != null
				&& sa.cands[0].verbM.equals(sa.cands[1].verbM)) {
			return true;
		}

		for (int i = 0; i < cands[0].srls.size(); i++) {
			String role1 = cands[0].roles.get(i);
			SemanticRole sr1 = cands[0].srls.get(i);
			for (int j = 0; j < cands[1].srls.size(); j++) {
				String role2 = cands[1].roles.get(j);
				SemanticRole sr2 = cands[1].srls.get(j);
				if (sr1 == sr2
						&& ((role1.equals("A0") && role2.equals("A1") || (role1
								.equals("A1") && role2.equals("A0"))))) {
					return true;
				}
			}
		}

		Mention first = cands[0].end < cands[1].start ? cands[0] : cands[1];
		Mention second = cands[0].end < cands[1].start ? cands[1] : cands[0];

		// second ends one largest NP
		MyTreeNode secondLastLeaf = sa.ss.parseTree.leaves.get(second.end);
		ArrayList<MyTreeNode> secondAncestors = secondLastLeaf.getAncestors();
		for (MyTreeNode ancestor : secondAncestors) {
			if (ancestor.value.equals("NP")) {
				if (ancestor.getLeaves().get(ancestor.getLeaves().size() - 1).leafIdx != second.end) {
					return false;
				}
			}
		}

		int verb = 0;
		for (int i = first.end + 1; i < second.start; i++) {
			StanfordToken st = sa.ss.tokens.get(i);
			if (st.POS.startsWith("V")) {
				verb++;
			}
			if (st.POS.startsWith("N")) {
				return false;
			}
			if (st.POS.startsWith("PU")) {
				return false;
			}
		}
		if (verb == 1) {
			return true;
		}
		return false;
	}

	public static boolean isAttriWord(String w) {
		boolean att = attriWord.contains(w);
		if (!att && w.startsWith("不")) {
			att = attriWord.contains(w.substring(1));
		}
		return att;
	}

	public static Mention getExtentFromHead(int head, StanfordSentence ss,
			int from) {
		MyTreeNode leaf = ss.parseTree.leaves.get(head);
		ArrayList<MyTreeNode> ancestors = leaf.getAncestors();
		int start = -1;
		for (MyTreeNode ancestor : ancestors) {
			if (ancestor.value.equals("NP")) {
				start = ancestor.getLeaves().get(0).leafIdx;
				if (start > from) {
					break;
				}
			}
		}
		Mention extent = new Mention();
		if (start == -1) {
			start = head;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = start; i <= head; i++) {
			StanfordToken tk = ss.tokens.get(i);
			if (tk.POS.equals("M") || tk.POS.equals("CD")) {
				if (start != head) {
					continue;
				}
			}
			sb.append(tk.word).append(" ");
		}
		extent.extent = sb.toString().trim();
		return extent;
	}

	private static HashSet<String> readDic(String fn) {
		HashSet<String> set = new HashSet<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fn));

			String line = "";

			while ((line = br.readLine()) != null) {
				if (line.trim().isEmpty()) {
					continue;
				}
				if (line.trim().split("\\s+").length > 1) {
					continue;
				}
				String term = line.trim();
				if (term.endsWith("的")) {
					term = term.substring(0, term.length() - 1);
				}
				set.add(term);
			}

			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return set;
	}

	public static String getPolarity(String str) {
		if (str.equals("是")) {
			return "NEUTRAL";
		}
		if (str.equals("多")) {
			return "POSITIVE";
		} else if (str.equals("少")) {
			return "NEGATIVE";
		}
		if (posComSet.contains(str) || posSentSet.contains(str)
				|| ntuPosSet.contains(str)) {
			return "POSITIVE";
		}
		if (negComSet.contains(str) || negSentSet.contains(str)
				|| ntuNegSet.contains(str)) {
			return "NEGATIVE";
		} else {
			return "NEUTRAL";
		}
	}

	public static String findAnaphorPolarity(SentAttri sa) {
		boolean flip = false;
		String polarity = "NEUTRAL";
		String connect = sa.ss.tokens.get(sa.connect).word;

		int negID = -1;
		if (sa.anaphor.verbM != null) {
			for (StanfordDep dep : sa.ss.basicDPLST) {
				if (dep.type.equals("neg")
						&& dep.getGovernorId() == sa.anaphor.verbM.start + 1) {
					flip = !flip;
					negID = dep.getDependentId() - 1;

				}
			}
			if (!sa.anaphor.verbM.extent.equals("是")) {
				polarity = WinoUtil.getPolarity(sa.anaphor.verbM.extent);
			}
		}

		for (int i = sa.anaphor.end + 1; i < sa.ss.tokens.size(); i++) {
			StanfordToken tk = sa.ss.tokens.get(i);
			String word = tk.word;
			if (polarity.equals("NEUTRAL")
			// && (tk.POS.startsWith("V")||tk.POS.startsWith("AD"))
			) {
				polarity = WinoUtil.getPolarity(word);
			}
		}

		if (polarity.equals("NEUTRAL")) {
			polarity = "POSITIVE";
		}

		if (connect.equals("但") || connect.equals("尽管")) {
			flip = !flip;
		}

		for (int i = sa.anaphor.end + 1; i < sa.ss.tokens.size(); i++) {
			if (i == negID) {
				continue;
			}
			StanfordToken tk = sa.ss.tokens.get(i);
			if (tk.word.contains("没") || tk.word.contains("只")
					|| tk.word.contains("不") || tk.word.contains("仅仅")) {
				flip = !flip;
			}
		}

		if (flip) {
			if (polarity.equals("POSITIVE")) {
				polarity = "NEGATIVE";
			} else if (polarity.equals("NEGATIVE")) {
				polarity = "POSITIVE";
			}
			if (polarity.equals("NEUTRAL")) {
				polarity = "NEGATIVE";
			}
		}

		// if (!polarity.equals("NEUTRAL")) {
		// System.out.println(sa.ss.getText());
		// System.out.println("@" + polarity);
		// System.out.println("!" + this.getPolarity("坏") + "#" + flip);
		// }
		return polarity;
	}

	public static String findCandidatePolarity(SentAttri sa, Mention cand,
			Mention compete) {
		String polarity = "NEUTRAL";
		// find compare
		// System.out.println(this.cands[1].end + "@" + this.sa.connect);
		int start = 0;
		int end = sa.connect;
		if (sa.cands[1].end >= sa.connect) {
			start = sa.connect;
			end = sa.ss.tokens.size();
		}

		boolean flip = false;
		for (int i = start; i < end; i++) {
			StanfordToken token = sa.ss.tokens.get(i);
			if (token.word.equals("比")) {
				for (StanfordDep dep : sa.ss.basicDPLST) {
					if (dep.type.equals("prep")
							&& dep.getDependentId() == i + 1) {
						polarity = WinoUtil.getPolarity(dep.governor);
					}
				}
			}
		}

		if (polarity.equals("NEUTRAL")) {
			for (int i = start; i < end; i++) {
				StanfordToken token = sa.ss.tokens.get(i);
				String word = token.word;
				if (word.equals("更")) {
					String op = sa.ss.tokens.get(i + 1).word;
					polarity = WinoUtil.getPolarity(op);
					if (polarity.equals("NEUTRAL") && end + 2 < sa.connect) {
						polarity = WinoUtil
								.getPolarity(sa.ss.tokens.get(i + 2).word);
					}
					break;
				} else if (word.startsWith("更")) {
					String op = word.substring(1);
					polarity = WinoUtil.getPolarity(op);
					break;
				}
			}
		}

		if (polarity.equals("NEUTRAL")) {
			lopo: for (int i = start; i < end; i++) {
				StanfordToken token = sa.ss.tokens.get(i);
				String word = token.word;
				if (word.equals("比")) {
					for (int j = end; j > sa.cands[1].end; j--) {
						String w = sa.ss.tokens.get(j).word;
						polarity = WinoUtil.getPolarity(w);
						if (!polarity.equals("NEUTRAL")) {
							break lopo;
						}
					}
					break lopo;
				}
			}
		}

		if (polarity.equals("NEUTRAL")) {
			for (int i = start; i < end; i++) {
				StanfordToken token = sa.ss.tokens.get(i);
				String word = token.word;
				if (word.endsWith("于") && token.POS.equals("VV")) {
					polarity = WinoUtil.getPolarity(word.substring(0,
							word.length() - 1));
					break;
				}
			}
		}

		if (polarity.equals("NEUTRAL")) {
			if (sa.cands[0].verbM != null && sa.cands[1].verbM != null
					&& sa.cands[0].verbM.equals(sa.cands[1].verbM)) {
				for (StanfordDep dep : sa.ss.basicDPLST) {
					if (dep.type.equals("neg")
							&& dep.getGovernorId() == sa.cands[0].verbM.start + 1) {
						flip = !flip;
					}
				}
				// polarity = this.getPolarity(this.cands[0].verbM.extent);
			}
		}

		// if (polarity.equals("NEUTRAL") && this.cands[0].verbM != null
		// && this.cands[1].verbM != null
		// && this.cands[0].verbM.equals(this.cands[1].verbM)) {
		// System.out.println("HEE");
		// String po = this.getPolarity(this.cands[0].verbM.extent);
		// if(!po.equals("NEUTRAL")) {
		// polarity = po;
		// System.out.println(this.cands[0].verbM.extent + "#" + polarity);
		// }
		// }

		if (cand.end > compete.end) {
			flip = !flip;
		}

		if (flip) {
			if (polarity.equals("POSITIVE")) {
				polarity = "NEGATIVE";
			} else if (polarity.equals("NEGATIVE")) {
				polarity = "POSITIVE";
			}
		}
		// TODO
		// if (polarity.equals("NEUTRAL")
		// && this.polarity.contains(this.sa.entry.engS)
		// ) {
		// System.out.println(sa.ss.getText());
		// // System.out.println(this.sa.entry.engS);
		// System.out.println(this.cand.extent + "@" + polarity);
		// }

		return polarity;
	}

	public static HashSet<Integer> getFullV(SentAttri sa, Mention v) {
		HashSet<Integer> ints = new HashSet<Integer>();

		String ret = v.extent;
		ints.add(v.end);

		if (ret.equals("被")) {
			for (StanfordDep dep : sa.ss.basicDPLST) {
				if (dep.getType().equals("pass")
						&& dep.getDependentId() == v.end + 1) {
					ret = ret + dep.getGovernor();
					ints.add(dep.getGovernorId() - 1);
				}
			}
		}

		for (StanfordDep dep : sa.ss.basicDPLST) {
			if (dep.getType().equals("pass")
					&& dep.getGovernorId() == v.end + 1) {
				ret = "被" + ret;
				ints.add(dep.getDependentId() - 1);
			}
		}

		for (StanfordDep dep : sa.ss.basicDPLST) {
			if (dep.getType().equals("neg") && dep.getGovernorId() == v.end + 1) {
				ret = "不" + ret;
				ints.add(dep.getDependentId());
			}
		}

		for (StanfordDep dep : sa.ss.basicDPLST) {
			if (dep.getType().equals("advmod")
					&& dep.getGovernorId() == v.end + 1
					&& dep.getDependent().contains("没")) {
				ret = dep.getDependent() + ret;
				ints.add(dep.getDependentId());
			}
		}

		if (v.start > 0 && sa.ss.tokens.get(v.start - 1).word.equals("不")) {
			ints.add(v.start - 1);
		}

		if (!ints.contains(v.start - 1) && v.start > 0
				&& sa.ss.tokens.get(v.start - 1).word.equals("被")) {
			ints.add(v.start - 1);
		}

		return ints;
	}

	public static HashSet<Integer> getRestV(SentAttri sa, Mention v, Mention ana) {
		HashSet<Integer> rest = new HashSet<Integer>();

		Stack<Integer> stack = new Stack<Integer>();
		stack.add(v.end);
		rest.add(v.end);
		while (stack.size() != 0) {
			int top = stack.pop();
			for (StanfordDep dep : sa.ss.basicDPLST) {
				if (dep.getGovernorId() == top + 1) {
					if (dep.getDependentId() > ana.end + 1
							&& !rest.contains(dep.getDependentId() - 1)) {
						rest.add(dep.getDependentId() - 1);
						stack.add(dep.getDependentId() - 1);
					}
				}

				if (dep.getDependentId() == top + 1) {
					if (dep.getGovernorId() > ana.end + 1
							&& !rest.contains(dep.getGovernorId() - 1)) {
						rest.add(dep.getGovernorId() - 1);
						stack.add(dep.getGovernorId() - 1);
					}
				}
			}
		}
		return rest;
	}

	public static String getStrFromInts(HashSet<Integer> ints, SentAttri sa) {
		ArrayList<Integer> sortRest = new ArrayList<Integer>(ints);
		Collections.sort(sortRest);
		StringBuilder sb = new StringBuilder();

		for (Integer i : sortRest) {
			StanfordToken tk = sa.ss.tokens.get(i);
			sb.append(tk.word);
		}

		return sb.toString().trim();
	}

	/*
	 * 
	 */
	@Deprecated
	public static Mention getFullV2(SentAttri sa, Mention v) {
		if (true) {
			GraphNode node = sa.ss.basicDP.get(v.end + 1);
			if (node != null) {
				ArrayList<GraphNode> nexts = node.nexts;
				for (GraphNode next : nexts) {
					String dep = node.getEdgeName(next);
					if (dep.contains("pass")) {
						if (!v.extent.startsWith("被")) {
							v.extent = "被" + " " + v.extent;
						}
						break;
					}
				}
			}
			return v;
		}

		Mention verb = v;
		int head = v.end;

		if (sa.ss.tokens.size() > head + 1
				&& sa.ss.tokens.get(head + 1).POS.startsWith("V")) {
			Mention m = new Mention();
			m.start = v.start;
			m.end = v.start + 1;
			m.extent = sa.ss.tokens.get(m.start).word + " "
					+ sa.ss.tokens.get(m.start + 1).word;
			verb = m;
		}
		GraphNode node = sa.ss.basicDP.get(v.end + 1);
		if (node != null) {
			ArrayList<GraphNode> nexts = node.nexts;
			for (GraphNode next : nexts) {
				String dep = node.getEdgeName(next);
				if (dep.contains("pass")) {
					// verb.extent = "被" + " " + verb.extent;
					break;
				}
			}
		}
		return verb;
	}
}
