package model;

import java.util.ArrayList;
import java.util.List;

import util.Common;

public class SentencePair {

	public SentEntry entries[];

	public SentencePair() {
		this.entries = new SentEntry[2];
	}

	public SentencePair(SentEntry entry1, SentEntry entry2) {
		this.entries = new SentEntry[2];
		this.entries[0] = entry1;
		this.entries[1] = entry2;
	}

	public static class SentEntry {
		public String engS;
		public String chiS;
		public String engAnaphor;
		public String chiAnaphor;

		public String engCand[];
		public String chiCand[];

		public String engAnt;
		public String chiAnt;

		public SentEntry(String engS, String chiS, String engAna,
				String chiAna, String engCand, String chiCand, String engAnt,
				String chiAnt) {
			this.engS = engS;
			this.chiS = chiS;
			this.engAnaphor = engAna;
			this.chiAnaphor = chiAna;
			this.engCand = engCand.trim().split("\\,");
			if (chiCand.contains("\t")) {
				this.chiCand = chiCand.trim().split("\t");
			} else {
				this.chiCand = chiCand.trim().split("\\,");
			}
			
			if(this.chiCand.length==1) {
				Common.bangErrorPOS(chiCand);
			}
			
			for(int i=0;i<this.chiCand.length;i++) {
				this.chiCand[i] = this.chiCand[i].trim();
			}
			
			for(int i=0;i<this.engCand.length;i++) {
				this.engCand[i] = this.engCand[i].trim(); 
			}

			this.engAnt = engAnt;
			this.chiAnt = chiAnt;

			
			if(!this.chiCand[0].equals(this.chiAnt) && !this.chiCand[1].equals(this.chiAnt)) {
				System.out.println("#" + this.chiCand[0] + "#" + this.chiCand[1]+ "#");
				System.out.println(this.chiAnt);
				Common.bangErrorPOS(chiS);
			}
			
			if (this.engCand.length != 2 || this.chiCand.length != 2) {
				System.out.println(this.engCand.length);
				System.out.println(this.chiCand.length);
				Common.bangErrorPOS(chiS);
			}

			if (!this.engS.contains(engAna) || !this.engS.contains(engAnt)) {
				Common.bangErrorPOS(chiS);
			}

			if (!this.chiS.contains(chiAna) || !this.chiS.contains(chiAnt)) {
				System.out.println(this.chiS.contains(chiAna));
				System.out.println(this.chiS.contains(chiAnt));
				Common.bangErrorPOS(chiS);
			}

			for (int m = 0; m < this.chiCand.length; m++) {
				String str = this.chiCand[m];
				if (!this.chiS.contains(str)) {
					System.out.println(m + 1);
					Common.bangErrorPOS(chiS);
				}
			}

			for (int m = 0; m < this.engCand.length; m++) {
				String str = this.engCand[m];
				if (!this.engS.contains(str)) {
					System.out.println(m + 1);
					Common.bangErrorPOS(chiS);
				}
			}
		}

		public SentEntry(List<String> lines) {
			this(lines.get(0), lines.get(1), lines.get(2), lines.get(3), lines
					.get(4), lines.get(5), lines.get(6), lines.get(7));
			for (String line : lines) {
				if (line.isEmpty()) {
					System.out.println(lines);
					Common.bangErrorPOS("!");
				}
			}
		}
	}

	public static void main(String args[]) {
		extractPairs("files/train_set.txt");
		extractPairs("files/test_set.txt");
	}

	public static ArrayList<SentencePair> extractPairs(String fn) {
		ArrayList<String> lines = Common.getLines(fn);
		ArrayList<SentencePair> pairs = new ArrayList<SentencePair>();
		for (int i = 0; i < lines.size(); i++) {
			// System.out.println(i);
			SentEntry entry1 = new SentEntry(lines.subList(i, i + 8));

			i += 10;
			SentEntry entry2 = new SentEntry(lines.subList(i, i + 8));
			// System.out.println(lines.subList(i, i+8));
			// Common.bangErrorPOS("");

			SentencePair pair = new SentencePair(entry1, entry2);
			i += 9;

			if (!entry1.chiCand[0].equals(entry2.chiCand[0])
					|| !entry1.chiCand[1].equals(entry2.chiCand[1])) {
				System.out.println(entry1.chiCand[0] + "#" + entry1.chiCand[1]);
				System.out.println(entry2.chiCand[0] + "#" + entry2.chiCand[1]);
				Common.bangErrorPOS("!!");
			}
			
			if(entry1.chiAnt.equals(entry2.chiAnt)) {
				Common.pause(entry1.chiS);
			}
			
//			if (!entry1.engCand[0].equals(entry2.engCand[0])
//					|| !entry1.engCand[1].equals(entry2.engCand[1])) {
//				System.out.println(entry1.engCand[0] + "#" + entry2.engCand[0]);
//				System.out.println(entry1.engCand[1] + "#" + entry2.engCand[1]);
//				Common.bangErrorPOS("!!");
//			}

			pairs.add(pair);
		}
		return pairs;
	}
}