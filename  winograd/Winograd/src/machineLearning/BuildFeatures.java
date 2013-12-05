package machineLearning;

import java.util.ArrayList;

import model.Mention;
import model.Mention.GramType;
import model.SemanticRole;
import model.SentAttri;
import model.SentencePair;
import model.SentencePair.SentEntry;
import model.stanford.StanfordToken;
import model.syntaxTree.GraphNode;
import util.Common;

public class BuildFeatures {

	static WinogradFeature winFea;

	public static void main(String args[]) {
		WinoUtil.init();
		ArrayList<String> svmlines = new ArrayList<String>();

		ArrayList<SentAttri> sentAttris = initiate();

		for (int i = 0; i < sentAttris.size(); i++) {

			if (i < 1322) {
				// continue;
			}

			if (i >= 1332
			// || i<=500
			) {
				// continue;
			}

			SentAttri sA = sentAttris.get(i);
			build(svmlines, sA);
		}
		winFea.freeze();

		Common.outputLines(svmlines, "allSVM");

		Common.outputLines(svmlines.subList(0, 2644), "allSVM.train");
		Common.outputLines(svmlines.subList(2644, svmlines.size()),
				"allSVM.test");

		for (String key : winFea.groups.keySet()) {
			double percent = winFea.groups.get(key) * 1.0
					/ (svmlines.size() * 1.0);
			System.out.println(key + ": " + winFea.groups.get(key) + "/"
					+ svmlines.size() + "=" + percent);
		}

		// for(String s : winFea.polarity) {
		// System.out.println(s);
		// }

		System.out.println(winFea.good + ":" + winFea.bad + "#" + winFea.good
				/ (winFea.good + winFea.bad) + " # "
				+ (winFea.good + winFea.bad) / winFea.id);
		
		System.out.println(winFea.good2 + ":" + winFea.bad2 + "#" + winFea.good2
				/ (winFea.good2 + winFea.bad2) + " # "
				+ (winFea.good2 + winFea.bad2) / winFea.id);

		Common.outputHashSet(WinoUtil.querySet, "querySet2");

		System.out.println(WinogradFeature.cons);
	}

	public static ArrayList<SentAttri> initiate() {
		ArrayList<SentencePair> pairs = new ArrayList<SentencePair>();
		pairs.addAll(SentencePair.extractPairs("files/train_set.c.txt"));
		pairs.addAll(SentencePair.extractPairs("files/test_set.c.txt"));

		ArrayList<SentAttri> sentAttris = SentAttri.load();

		// System.out.println(pairs.size() + " # " + sentAttris.size());
		winFea = new WinogradFeature(true, "wino");
		for (int i = 0; i < pairs.size(); i++) {
			// System.out.println(i + "/" + pairs.size());
			SentEntry entry1 = pairs.get(i).entries[0];
			SentEntry entry2 = pairs.get(i).entries[1];

			fillMention(entry1, sentAttris.get(i * 2));
			fillMention(entry2, sentAttris.get(i * 2 + 1));
		}
		return sentAttris;
	}

	static int qid = 1;

	private static void build(ArrayList<String> svmlines, SentAttri sA) {

		Mention cand1 = sA.cands[0];
		Mention cand2 = sA.cands[1];
		Mention ant = sA.ant;
		Mention anaphor = sA.anaphor;

		winFea.configure(sA, cand1, sA.cands, anaphor, "X");
		String svm1 = winFea.getSVMFormatString();

		winFea.configure(sA, cand2, sA.cands, anaphor, "Y");
		String svm2 = winFea.getSVMFormatString();

		if (cand1.equals(ant)) {
			svmlines.add("1 qid:" + qid + " " + svm1);
			svmlines.add("0 qid:" + qid + " " + svm2);
		} else if (cand2.equals(ant)) {
			svmlines.add("0 qid:" + qid + " " + svm1);
			svmlines.add("1 qid:" + qid + " " + svm2);
		} else {
			Common.bangErrorPOS("!!!");
		}
		qid++;
	}

	private static void fillMention(SentEntry entry, SentAttri sa) {

		sa.entry = entry;

		String cand1 = entry.chiCand[0];
		String cand2 = entry.chiCand[1];

		String text = entry.chiS;

		Mention candM1 = buildMention(cand1, text, sa, 0);
		Mention candM2 = buildMention(cand2, text, sa, 0);

		Mention antM = buildMention(entry.chiAnt, text, sa, 0);

		Mention anaphor = buildMention(entry.chiAnaphor, text, sa,
				candM2 == null ? 0 : candM2.end + 1);

		// dd
		sa.cands[0] = candM1;
		sa.cands[1] = candM2;
		sa.ant = antM;
		sa.anaphor = anaphor;

		if (sa.connect == -1) {
			sa.connect = anaphor.start - 1;
		}

		String verb = "#";
		if (anaphor.verbM != null) {
			verb = anaphor.verbM.extent;
		} else {
			// System.out.println(sa.ss.getText());
		}

		if (anaphor.gramType == GramType.Obj) {
			// System.out.println(candM1.head + ":" + candM2.head + "#" + verb +
			// ":" + anaphor.gramType);
		}
	}

	private static Mention buildMention(String extent, String text,
			SentAttri sa, int from) {
		Mention m = null;
		int charS = 0;
		while (true) {
			int start = text.indexOf(extent, charS);
			int end = start + extent.length();

			int sID = -1;
			int eID = -1;

			for (int i = from; i < sa.ss.tokens.size(); i++) {
				StanfordToken tk = sa.ss.tokens.get(i);
				if (tk.getCharacterOffsetBegin()
						- sa.ss.tokens.get(0).CharacterOffsetBegin == start) {
					sID = i;
				}
				if (tk.getCharacterOffsetEnd()
						- sa.ss.tokens.get(0).CharacterOffsetBegin + 1 == end) {
					eID = i;
					// break;
				}
			}

			if (sID != -1 && eID != -1) {
				m = new Mention();
				m.extent = extent;
				m.start = sID;
				m.end = eID;
				m.extent = extent;
				break;
			}
			charS = end + 1;
			if (charS >= text.length() || start == -1) {
				break;
			}
		}
		if (m == null) {
			System.out.println(sa.ss.getText());
			System.out.println(extent);
			Common.pause("");
		}

		// assign features
		calMFeaMate(m, sa);
		calMFea(m, sa);

		StringBuilder sb = new StringBuilder();
		if (m.start != m.end) {
			for (int i = m.start; i <= m.end; i++) {
				StanfordToken tk = sa.ss.tokens.get(i);
				if (!tk.POS.equals("CD") && !tk.POS.equals("M")
						&& !tk.POS.equals("DT") && !tk.POS.equals("OD")) {
					sb.append(tk.word);
				}
				if (tk.word.endsWith("的") && i != m.end) {
					sb = new StringBuilder();
				}
			}
			m.str = sb.toString();
		} else {
			m.str = m.extent;
		}
		if (m.str.endsWith("们") && m.str.length() != 1) {
			m.str = m.str.substring(0, m.str.length() - 1);
		}
		// System.out.println(m.extent + "#" + m.str);
		return m;
	}

	public static void calMFea(Mention m, SentAttri sa) {
		m.head = sa.ss.tokens.get(m.end).word;

		Mention headM = new Mention(m.end, m.end);
		headM.extent = sa.ss.tokens.get(headM.end).word;
		m.headM = headM;

		for (int i = m.start; i <= m.end; i++) {
			StanfordToken st = sa.ss.tokens.get(i);
			if (!st.ner.equals("O")) {
				m.NE = sa.ss.tokens.get(i).ner;
			}
			if (st.POS.equals("NR")) {
				m.NE = "PERSON";
			}
		}

		if (m.NE.equals("O") && sa.ss.getToken(m.end).POS.equals("NR")) {
			m.NE = "PERSON";
		}

		if (Character.isUpperCase(m.extent.charAt(0))) {
			m.NE = "PERSON";
		}

		// System.out.println(m.NE + "@" + m.extent);
		// check dependency
		GraphNode node = sa.ss.basicDP.get(m.end + 1);

		if (node != null) {
			for (GraphNode n : node.nexts) {
				if (n.value == 0) {
					continue;
				}
				String head = m.head;
				String edgeName = node.getEdgeName(n);
				String to = sa.ss.tokens.get(n.value - 1).word;

				if (m.verbM == null) {
					if (edgeName.startsWith("nsubj+")) {
						m.gramType = GramType.Subj;
					} else if (edgeName.startsWith("dobj+")) {
						m.gramType = GramType.Obj;
					}
					if (edgeName.startsWith("nsubj+")
							|| edgeName.startsWith("dobj+")) {

						if (sa.ss.tokens.get(n.value - 1).POS.startsWith("V")) {
							Mention verbM = new Mention(n.value - 1,
									n.value - 1);
							verbM.extent = sa.ss.tokens.get(n.value - 1).word;
							m.verbM = verbM;
						}
					}
				}
				if (edgeName.equals("amod-")) {
					Mention adjM = new Mention(n.value - 1, n.value - 1);
					adjM.extent = sa.ss.tokens.get(n.value - 1).word;
					m.adjM = adjM;
				}
				// if(edgeName.startsWith("amod"))
				// System.out.println(head + "#" + to + ":" + edgeName);
				// find VC
				// find subj/obj
				// find head word
				// find adjective modifier
			}
		}
		// node.nexts
	}

	public static void calMFeaMate(Mention m, SentAttri sa) {
		if (m.verbM == null) {
			GraphNode node = sa.vertexMap.get(m.end + 1);
			for (GraphNode next : node.nexts) {
				if (node.getEdgeName(next).equalsIgnoreCase("SBJ")) {
					Mention verbM = new Mention(next.value - 1, next.value - 1);
					verbM.extent = sa.ss.tokens.get(next.value - 1).word;
					m.verbM = verbM;
					m.gramType = GramType.Subj;
				}
			}
			// Mention verbM = new Mention(node.nexts);
			// verbM.extent = sa.ss.tokens.get(n.value - 1).word;
			// m.verbM = verbM;
		}

		// find semantic role labeling
		ArrayList<SemanticRole> srls = sa.srls;
		for (SemanticRole srl : srls) {
			for (String role : srl.roles.keySet()) {
				ArrayList<Mention> ms = srl.roles.get(role);
				for (Mention t : ms) {
					if (m.end == t.end) {
						m.srls.add(srl);
						m.roles.add(role);
						if(role.equals("A1") && m.verbM==null) {
							m.verbM = srl.pred;
							m.gramType = GramType.Obj;
						}
					}
				}
			}
		}
		// node.nexts
	}

}
