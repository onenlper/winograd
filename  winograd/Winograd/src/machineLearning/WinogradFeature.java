package machineLearning;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import model.Mention;
import model.Mention.GramType;
import model.SentAttri;
import model.stanford.StanfordToken;
import model.stanford.StanfordXMLReader.StanfordDep;
import preprocess.CollectSV.SVOStat;
import resolver.Dispatcher;
import resolver.anaphor.IAnaphor;
import resolver.candidate.CandTwoNominal;
import resolver.candidate.ICand;
import util.Common;
import util.Common.Feature;
import util.YYFeature;

public class WinogradFeature extends YYFeature {

	SVOStat svoStat;

	SentAttri sa;
	Mention cand;
	Mention anaphor;
	String suffix;

	Mention cands[];

	Mention compete;

	HashSet<String> polarity = Common
			.readFile2Set("/users/yzcchen/chen3/Winograd/WinoAltaf/src/polarity.txt");

	public WinogradFeature(boolean train, String name) {
		super(train, name);

		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					"svostat.object"));
			svoStat = (SVOStat) ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		// System.out.println(svoStat.svSet.size());
		// System.out.println(svoStat.voSet.size());
	}

	public void configure(SentAttri sa, Mention cand, Mention cands[],
			Mention anaphor, String suffix) {
		this.sa = sa;
		this.cand = cand;
		this.cands = cands;
		this.anaphor = anaphor;
		this.suffix = suffix;
		this.compete = this.cands[0] == this.cand ? this.cands[1]
				: this.cands[0];
	}

	@Override
	public ArrayList<Feature> getCategoryFeatures() {
		return null;
	}

	@Override
	public ArrayList<String> getStrFeatures() {
		print.clear();
		sw = false;

		ArrayList<String> feas = new ArrayList<String>();

		print.add("=================");
		print.add(sa.ss.getText());
		print.add(sa.cands[0].extent);
		print.add(sa.cands[1].extent);
		print.add("-------");

		feas.addAll(this.lexicalFeature());
		feas.addAll(this.polarityFeature());
//
		feas.addAll(this.getGoogleCompatible());

		feas.addAll(this.getVerbChain());

		// feas.addAll(this.mutualInfo());

		if (sw) {
			for (String line : print) {
				// System.out.println(line);
			}
		}
		return feas;
	}

	public static HashMap<String, Integer> cons = new HashMap<String, Integer>();

	int g = 0;

	private Collection<? extends String> getVerbChain() {
		ArrayList<String> ret = new ArrayList<String>();

		//
		String connector = this.sa.ss.tokens.get(this.sa.connect).word;
		Integer i = cons.get(connector);
		if (i == null) {
			cons.put(connector, 1);
		} else {
			cons.put(connector, i.intValue() + 1);
		}

		/*
		 * {自从=2, 那=8, 以便=2, 而=10, 支持=4, 尽管=22, 除非=4, 让=6, ，=176, 有时=2, 那么=2,
		 * 虽然=16, 使=2, 告诉=2, 而且=14, 当=6, 可是=2, 朝=2, 并=2, 但=360, 授予=4, 直到=4, 有=2,
		 * 即使=6, 于是=6, 现在=2, 但是=116, 从=4, 否则=2, 打败=2, 不过=2, 解雇=2, 变化=2, 所以=218,
		 * 然而=6, 因此=24, 后来=2, 说=4, 这样=76, 把=4, 忠于=2, 地=2, 证明=2, 和=4, 为了=8,
		 * 因为=2456, 在=30, 直=8, 切伤=4, Kyle=4, 不=2, 并且=8, 与=4, 有史以来=4, 了=10, 如果=8,
		 * 然后=80, 向=4}
		 */
		boolean show = false;

		show = this.anaphor.verb != null;
		ArrayList<ICand> candRs = Dispatcher.dispatchCand(sa);
		boolean satisfy = false;
		for (ICand candR : candRs) {
			if (candR instanceof CandTwoNominal) {
				satisfy = true;
			}
		}

		// if (e.endsWith("是") || e.endsWith("有") || e.endsWith("变得")) {
		// return 0;
		// }
		// if(e.endsWith("有") || e.endsWith("给") || e.endsWith("要") ||
		// e.endsWith("想") || e.endsWith("希望")
		// || e.endsWith("会") || e.endsWith("把") || e.endsWith("喜欢")

		// if (connector.equals("因为")) {
		if (show 
				&& !this.anaphor.verb.contains("是")
				&& !this.anaphor.verb.contains("可以")
				&& !this.anaphor.verb.contains("能够")
				&& !this.anaphor.verb.contains("有")
				&& !this.anaphor.verb.contains("做")
				&& !satisfy
				) {
			String v0 = this.anaphor.verb;

			int a1 = 0;
			int a2 = 0;

			HashSet<String> vs1 = this.sa.cands[0].getVerbExtent(sa);
			HashSet<String> vs2 = this.sa.cands[1].getVerbExtent(sa);

			for (String v1 : vs1) {
				a1 += WinoUtil.getCount(v1, v0, "");
			}

			for (String v2 : vs2) {
				a2 += WinoUtil.getCount(v2, v0, "");
			}
//			System.out.println(a1 + ":" + a2);
			// if (connector.equals("因为")) {
			// pair1 = v0 + "#" + v1 + "#";
			// pair2 = v0 + "#" + v2 + "#";
			// }
			int decision = 0;
			if (a1 > a2 * 1.0) {
				decision = 1;
			} else if (a2 > a1 * 1.0) {
				decision = 2;
			} else {
				decision = 0;
			}

			boolean correct = false;
			if ((decision == 1 && this.sa.ant.equals(this.cands[0]))
					|| (decision == 2 && this.sa.ant.equals(this.cands[1]))) {
				good2++;
				correct = true;
			} else if (decision != 0) {
				bad2++;
			}

			if (decision == 1) {
				if (this.cand.equals(this.cands[0])) {
					ret.add("YES");
				} else {
					ret.add("NO");
				}
			} else if (decision == 2) {
				if (this.cand.equals(this.cands[1])) {
					ret.add("YES");
				} else {
					ret.add("NO");
				}
			} else {
				ret.add("PEND");
			}

			// if ((a1 != 0 || a2 != 0) && (!correct)) {
			// if((a1!=0&&v1.startsWith("被"))||(a2!=0 && v2.startsWith("被"))) {
			// System.out.println(this.sa.ss.getText());
			// System.out.println(this.sa.cands[0].extent + "@"
			// + this.sa.cands[0].getVerbExtent(sa));
			// System.out.println(this.sa.cands[1].extent + "@"
			// + this.sa.cands[1].getVerbExtent(sa));
			// System.out.println(this.anaphor.verb);
			// System.out.println(pair1 + "#" + a1);
			// System.out.println(pair2 + "#" + a2);
			// System.out.println("---" + (g++) + "----");
			// System.out.println("---" + good2 + ":" + bad2 + "----");
			// System.out.println(ret.get(0));
			// }
			if (this.sa.cands[0].gramType == GramType.Subj) {

			}
			// }
		}

		return ret;
	}

	@Deprecated
	private HashSet<String> getDepStrs2(SentAttri sa, HashSet<Integer> ints,
			int anaphor, String head, int predID) {
		HashSet<String> set = new HashSet<String>();
		boolean got = false;
		for (StanfordDep dep : sa.ss.basicDPLST) {
			int goID = dep.getGovernorId() - 1;
			int deID = dep.getDependentId() - 1;
			String type = dep.type;

			if (anaphor != goID && !ints.contains(goID)) {
				if (anaphor == goID) {
					Common.bangErrorPOS("");
				}
				continue;
			}

			if (anaphor != deID && !ints.contains(deID)) {
				continue;
			}
			String go = dep.getGovernor();
			String de = dep.getDependent();
			if (anaphor == goID) {
				got = true;
				go = head;
			}
			if (anaphor == deID) {
				got = true;
				de = head;
			}
			String s = go + "-" + dep.type + "-" + de;
			set.add(s);
		}
		if (!got) {
			String s = sa.ss.tokens.get(predID) + "-" + "nsubj" + "-" + head;
			set.add(s);
		}
		// System.out.println(s);

		return set;
	}

	private HashSet<String> getDepStrs(SentAttri sa, HashSet<Integer> ints,
			int anaphor, String head, int predID) {
		HashSet<String> set = new HashSet<String>();

		String s = head + "-" + "SBJ" + "-" + sa.ss.tokens.get(predID).word;
		set.add(s);
		// for (int id : sa.vertexMap.keySet()) {
		// if (id != anaphor && !ints.contains(id)) {
		// continue;
		// }
		// GraphNode node = sa.vertexMap.get(id);
		// id = id - 1;
		//
		// for (GraphNode next : node.nexts) {
		// int id2 = next.value - 1;
		//
		// if(id2!=anaphor && !ints.contains(id2)) {
		// continue;
		// }
		//
		// String type = node.getEdgeName(next);
		// String s1 = sa.ss.tokens.get(id).word;
		// String s2 = sa.ss.tokens.get(id2).word;
		// if(anaphor==id) {
		// s1 = head;
		// }
		// if(anaphor==id2) {
		// s2 = head;
		// }
		// String s = s1 + '-' + type + '-' + s2;
		// // System.out.println(s);
		// // System.out.println(id + "=" + id2);
		// if(type.equals("SBJ")) {
		// set.add(s);
		// }
		// }
		// }
		return set;
	}

	public ArrayList<String> getGoogleCompatible() {
		ArrayList<String> feas = new ArrayList<String>();
		ArrayList<IAnaphor> anaphorRs = Dispatcher.dispatchAnaphor(sa, 0);
		ArrayList<ICand> candRs = Dispatcher.dispatchCand(sa);

		// System.out.println(good + ":" + bad);

		Mention cand1 = sa.cands[0];
		Mention cand2 = sa.cands[1];

		// System.out.println(this.sa.ss.getText());

		boolean satisfy = false;
		for (ICand candR : candRs) {
			if (candR instanceof CandTwoNominal) {
				satisfy = true;
			}
		}

		if (satisfy) {
			String decision = "";
			if (this.anaphor.attribute != null) {
				String att = this.anaphor.attribute;
				String q1 = att + "的" + cand1.str;
				String q2 = att + "的" + cand2.str;
				long c1 = WinoUtil.getCount(q1);
				long c2 = WinoUtil.getCount(q2);

				// System.out.println(q1 + ":" + c1);
				// System.out.println(q2 + ":" + c2);
				decision = getGoogleCompatibleDecision(c1, c2, cand1, cand2,
						"compatible", q1, q2, 1, 1);
			}
			if (this.anaphor.verb != null && this.anaphor.restVerb != null
					&& !decision.startsWith("PENDING")) {

				String verb = this.anaphor.verb;
				String restVerb = this.anaphor.restVerb;
				String q1 = cand1.str + verb;
				String q2 = cand2.str + verb;
				String q3 = cand1.str + restVerb;
				String q4 = cand2.str + restVerb;

				long c1 = WinoUtil.getCount(q1);
				HashSet<String> set1 = getDepStrs(sa, this.anaphor.verbInts,
						anaphor.end, cand1.head, this.anaphor.verbM.end);
				double per1 = WinoUtil.getPercent(q1, set1);
				long c2 = WinoUtil.getCount(q2);
				HashSet<String> set2 = getDepStrs(sa, this.anaphor.verbInts,
						anaphor.end, cand2.head, this.anaphor.verbM.end);
				double per2 = WinoUtil.getPercent(q2, set2);
				long c3 = WinoUtil.getCount(q3);
				HashSet<String> set3 = getDepStrs(sa,
						this.anaphor.verbRestInts, anaphor.end, cand1.head,
						this.anaphor.verbM.end);
				double per3 = WinoUtil.getPercent(q3, set3);
				long c4 = WinoUtil.getCount(q4);
				HashSet<String> set4 = getDepStrs(sa,
						this.anaphor.verbRestInts, anaphor.end, cand2.head,
						this.anaphor.verbM.end);
				double per4 = WinoUtil.getPercent(q4, set4);

				// System.out.println(q1 + ":" + c1 + "\t" + per1);
				// System.out.println(q2 + ":" + c2 + "\t" + per2);
				// System.out.println(q3 + ":" + c3 + "\t" + per3);
				// System.out.println(q4 + ":" + c4 + "\t" + per4);

				decision = getGoogleCompatibleDecision(c3, c4, cand1, cand2,
						"compatible", q3, q4, per3, per4);
				if (decision.startsWith("PENDING")) {
					decision = getGoogleCompatibleDecision(c1, c2, cand1,
							cand2, "compatible", q1, q2, per1, per2);
				}
			}
			feas.add(decision);
		}
		// System.out.println("---" + (this.id++) + "---");
		googleSolved = feas.size()!=0 && feas.get(0).contains("PENDING");
//		feas.clear();
		return feas;
	}
	private boolean polaritySolved = false;
	private boolean googleSolved = false;
	
	public double good = 0;
	public double bad = 0;

	public double good2 = 0;
	public double bad2 = 0;

	public boolean sw = false;

	public boolean correct = false;

	ArrayList<String> print = new ArrayList<String>();

	private String getGoogleCompatibleDecision(double c1, double c2,
			Mention m1, Mention m2, String suffix, String q1, String q2,
			double per1, double per2) {

		print.add(q1 + ":" + c1 + " # " + per1);
		print.add(q2 + ":" + c2 + " # " + per2);
		// print.add(Double.toString(bad));

		// ArrayList<String> engC =
		// WinoUtil.altafGoogle.get(this.sa.entry.engS);
		// if (engC != null) {
		// print.add(this.sa.entry.engS);
		// for (String c : engC) {
		// print.add(c);
		// }
		// }

		// c1 = c1 * per1;
		// c2 = c2 * per2;

		if (c1 < 0 || c2 < 0) {
			return "PENDING" + suffix;
		}
		// if(c1>10000 && c2>10000) {
		// ret.add("NODecision" + suffix);
		// return ret;
		// }
		int decision = 0;
		if (c1 > c2 * 1.0) {
			decision = 1;
		} else if (c2 > c1 * 1.0) {
			decision = 2;
		} else {
			decision = 0;
		}

		if ((decision == 1 && this.sa.ant.equals(this.cands[0]))
				|| (decision == 2 && this.sa.ant.equals(this.cands[1]))) {
			good++;
		} else if (decision != 0) {
			sw = true;
			bad++;
		}

		if (decision == 1) {
			if (this.cand.equals(this.cands[0])) {
				return "YES" + suffix;
			} else {
				return "NO" + suffix;
			}
		} else if (decision == 2) {
			if (this.cand.equals(this.cands[1])) {
				return "YES" + suffix;
			} else {
				return "NO" + suffix;
			}
		} else {
		}
		return "PENDING" + suffix;
	}

	private ArrayList<String> mutualInfo() {
		ArrayList<String> feas = new ArrayList<String>();

		System.out.println(this.sa.ss.getText());

		if (this.anaphor.verbM != null && this.cands[0].NE.equals("O")
				&& this.cands[1].NE.equals("O")) {

			feas.add(this.suffix);

			double pairC1 = 0;
			double pairC2 = 0;

			String key1 = this.cands[0].head + " " + this.anaphor.verb;
			String key2 = this.cands[1].head + " " + this.anaphor.verb;

			if (this.anaphor.gramType == GramType.Subj) {
				if (this.svoStat.svSet.containsKey(key1))
					pairC1 = this.svoStat.svSet.get(key1);
				if (this.svoStat.svSet.containsKey(key2))
					pairC2 = this.svoStat.svSet.get(key2);
			} else {
				if (this.svoStat.voSet.containsKey(key1))
					pairC1 = this.svoStat.voSet.get(key1);
				if (this.svoStat.voSet.containsKey(key2))
					pairC2 = this.svoStat.voSet.get(key2);
			}

			if (pairC1 > pairC2) {
				if (this.cand == this.cands[0]) {
					feas.add("select");
				} else {
					feas.add("noselect");
				}
			} else if (pairC1 < pairC2) {
				if (this.cand == this.cands[0]) {
					feas.add("noselect");
				} else {
					feas.add("select");
				}
			}
		}
		return feas;
	}

	HashMap<String, Integer> groups = new HashMap<String, Integer>();

	int id = 0;

	public ArrayList<String> polarityFeature() {
		ArrayList<String> feas = new ArrayList<String>();
		String candPolarity = WinoUtil.findCandidatePolarity(this.sa,
				this.cand, this.compete);
		if (!candPolarity.equals("NEUTRAL")) {
			String anaphorPolarity = WinoUtil.findAnaphorPolarity(this.sa);
			if (candPolarity.equals(anaphorPolarity)) {
				feas.add("PICKTHIS-polarity");
			} else if (!candPolarity.equals(anaphorPolarity)) {
				feas.add("NOPICKTHIS-polarity");
			} else {
				// feas.add("NODECISION");
			}
			feas.add(candPolarity + "-" + anaphorPolarity);

			String connect = "-";
			if (this.sa.connect != -1) {
				connect = this.sa.ss.tokens.get(this.sa.connect).word;
			}
			feas.add(candPolarity + "-" + connect + "-" + anaphorPolarity);
		} else {
			feas.add("NODECISION-polarity");
		}
		polaritySolved = !feas.get(0).contains("NODECISION");
		return feas;
	}

	private void addOne(String key) {
		if (groups.containsKey(key)) {
			groups.put(key, groups.get(key).intValue() + 1);
		} else {
			groups.put(key, 1);
		}
	}

	public ArrayList<String> lexicalFeature() {
		ArrayList<String> feas = new ArrayList<String>();
		String conn = "#";
		int cut = sa.connect;
		if (sa.connect == -1) {
			cut = sa.anaphor.start;
		} else {
			conn = sa.ss.tokens.get(cut).word;
		}
		ArrayList<Integer> Ws = getW();
		// unigram feature
		unigram(feas, Ws);
		// pair features
		pair(feas, Ws, cut);
		// pair-connect features
		// pairConnect(feas, Ws, conn, cut);
		anteDep(feas);
		return feas;
	}

	private void anteDep(ArrayList<String> feas) {
		// Hc-Vc
		if (cand.verbM != null) {
			feas.add("Hc-Vc-" + cand.headM.extent + "#" + cand.verbM.extent);
		}
		// Hc-Jc
		if (cand.adjM != null) {
			feas.add("Hc-Jc-" + cand.headM.extent + "#" + cand.adjM.extent);
		}
		// Hc-Va
		if (anaphor.verbM != null) {
			// System.out.println(cand.headM.extent + "#" +
			// anaphor.verbM.extent);
			feas.add("Hc-Va-" + cand.headM.extent + "#" + anaphor.verbM.extent);
		}
		// Hc-Ja
		if (anaphor.adjM != null) {
			feas.add("Hc-Ja-" + cand.headM.extent + "#" + anaphor.adjM.extent);
		}
	}

	private void pairConnect(ArrayList<String> feas, ArrayList<Integer> Ws,
			String conn, int cut) {
		for (Integer leftW : Ws) {
			if (leftW >= cut) {
				continue;
			}

			for (Integer rightW : Ws) {
				if (rightW <= cut) {
					continue;
				}
				StanfordToken w1 = sa.ss.tokens.get(leftW);
				StanfordToken w2 = sa.ss.tokens.get(rightW);
				if (w1.POS.startsWith("N") && w2.POS.startsWith("JJ")) {
					continue;
				}
				if (w1.POS.startsWith("JJ") && w2.POS.startsWith("N")) {
					continue;
				}
				feas.add("pair-" + w1.word + "-" + conn + "-" + w2.word
						+ this.suffix);
			}
		}
	}

	private void pair(ArrayList<String> feas, ArrayList<Integer> Ws, int cut) {
		for (Integer leftW : Ws) {
			if (leftW >= cut) {
				continue;
			}
			for (Integer rightW : Ws) {
				if (rightW <= cut) {
					continue;
				}
				StanfordToken w1 = sa.ss.tokens.get(leftW);
				StanfordToken w2 = sa.ss.tokens.get(rightW);
				if (w1.POS.startsWith("N") && w2.POS.startsWith("JJ")) {
					continue;
				}
				if (w1.POS.startsWith("JJ") && w2.POS.startsWith("N")) {
					continue;
				}
				feas.add("pair-" + w1.word + "-" + w2.word + this.suffix);
			}
		}
	}

	private void unigram(ArrayList<String> feas, ArrayList<Integer> Ws) {
		for (Integer i : Ws) {
			feas.add("uni-" + sa.ss.tokens.get(i).word + "-" + this.suffix);
		}
	}

	private ArrayList<Integer> getW() {
		ArrayList<Integer> Ws = new ArrayList<Integer>();
		for (int i = 0; i < sa.ss.tokens.size(); i++) {
			if (sa.ss.tokens.get(i).POS.equals("PU")) {
				continue;
			}
			if (i >= sa.cands[0].start && i <= sa.cands[0].end) {
				continue;
			}
			if (i >= sa.cands[1].start && i <= sa.cands[1].end) {
				continue;
			}
			if (i >= sa.anaphor.start && i <= sa.anaphor.end) {
				continue;
			}
			if (i == sa.connect) {
				continue;
			}
			Ws.add(i);
		}
		return Ws;
	}

}
