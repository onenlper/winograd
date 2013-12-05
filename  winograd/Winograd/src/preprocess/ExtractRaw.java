package preprocess;

import java.util.ArrayList;

import util.Common;

import model.SentencePair;
import model.SentencePair.SentEntry;

public class ExtractRaw {

	public static void main(String args[]) {
		ArrayList<SentencePair> trainPairs = SentencePair
				.extractPairs("files/train_set.c.txt");
		ArrayList<SentencePair> testPairs = SentencePair
				.extractPairs("files/test_set.c.txt");

		ArrayList<String> chiRawTrain = extractRaw(trainPairs, true);
		ArrayList<String> engRawTrain = extractRaw(trainPairs, false);
		Common.outputLines(chiRawTrain, "files/chiRaw_train.txt");
		Common.outputLines(engRawTrain, "files/engRaw_train.txt");

		ArrayList<String> chiRawTest = extractRaw(testPairs, true);
		ArrayList<String> engRawTest = extractRaw(testPairs, false);
		Common.outputLines(chiRawTest, "files/chiRaw_test.txt");
		Common.outputLines(engRawTest, "files/engRaw_test.txt");

		ArrayList<String> chiRaw = new ArrayList<String>();
		chiRaw.addAll(chiRawTrain);
		chiRaw.addAll(chiRawTest);
		Common.outputLines(chiRaw, "files/chiRaw.txt");

//		ArrayList<SentencePair> allPairs = new ArrayList<SentencePair>();
//		allPairs.addAll(trainPairs);
//		allPairs.addAll(testPairs);
//
//		ArrayList<String> trainC = Common
//				.getLines("/users/yzcchen/chen3/Winograd/WinoAltaf/src/train.c.txt");
//		ArrayList<String> testC = Common
//				.getLines("/users/yzcchen/chen3/Winograd/WinoAltaf/src/test.c.txt");
//
//		ArrayList<String> trainCC = new ArrayList<String>();
//		ArrayList<String> testCC = new ArrayList<String>();
//
//		buildSplit(allPairs, trainC, trainCC);
//		buildSplit(allPairs, testC, testCC);
//
//		Common.outputLines(trainCC, "files/train_set.c.txt");
//		Common.outputLines(testCC, "files/test_set.c.txt");
		// int total = 0;
		//
		// ArrayList<String> output = new ArrayList<String>();
		//
		// for (SentencePair pair : allPairs) {
		// String engS1[] = pair.entries[0].engS.trim().split("\\s+");
		// String engS2[] = pair.entries[1].engS.trim().split("\\s+");
		//
		// int diff = 0;
		// if (engS1.length == engS2.length) {
		//
		// for (int i = 0; i < engS1.length; i++) {
		// if (!engS1[i].equalsIgnoreCase(engS2[i])) {
		// diff++;
		// }
		// }
		// }
		// if(diff==1) {
		// System.out.println(pair.entries[0].engS.trim());
		// System.out.println(pair.entries[1].engS.trim());
		// System.out.println("===");
		//
		// output.add(pair.entries[0].engS);
		// output.add(pair.entries[0].engAnaphor);
		// output.add(pair.entries[0].engCand[0] + ", " +
		// pair.entries[0].engCand[1]);
		// output.add(pair.entries[0].engAnt);
		// output.add("");
		//
		// output.add(pair.entries[1].engS);
		// output.add(pair.entries[1].engAnaphor);
		// output.add(pair.entries[1].engCand[0] + ", " +
		// pair.entries[1].engCand[1]);
		// output.add(pair.entries[1].engAnt);
		// output.add("");
		//
		// total++;
		// }
		// }
		// System.out.println("Match: " + total);
		//
		// Common.outputLines(output, "cs4375-f13.txt");
	}

	private static void buildSplit(ArrayList<SentencePair> allPairs,
			ArrayList<String> trainC, ArrayList<String> trainCC) {
		for (int i = 0; i < trainC.size() / 10; i++) {
			String text1 = trainC.get(i * 10);
			String text2 = trainC.get(i * 10 + 5);
			boolean find = false;
			for (int j = 0; j < allPairs.size(); j++) {
				SentencePair pair = allPairs.get(j);
				if (pair.entries[0].engS.equals(text1)
						&& pair.entries[1].engS.equals(text2)) {
					trainCC.add(pair.entries[0].engS);
					trainCC.add(pair.entries[0].chiS);
					trainCC.add(pair.entries[0].engAnaphor);
					trainCC.add(pair.entries[0].chiAnaphor);
					trainCC.add(pair.entries[0].engCand[0] + ", "
							+ pair.entries[0].engCand[1]);
					trainCC.add(pair.entries[0].chiCand[0] + ", "
							+ pair.entries[0].chiCand[1]);
					trainCC.add(pair.entries[0].engAnt);
					trainCC.add(pair.entries[0].chiAnt);
					trainCC.add("");
					trainCC.add(""); 

					trainCC.add(pair.entries[1].engS);
					trainCC.add(pair.entries[1].chiS);
					trainCC.add(pair.entries[1].engAnaphor);
					trainCC.add(pair.entries[1].chiAnaphor);
					trainCC.add(pair.entries[1].engCand[0] + ", "
							+ pair.entries[1].engCand[1]);
					trainCC.add(pair.entries[1].chiCand[0] + ", "
							+ pair.entries[1].chiCand[1]);
					trainCC.add(pair.entries[1].engAnt);
					trainCC.add(pair.entries[1].chiAnt);
					trainCC.add("");
					trainCC.add("");

					allPairs.remove(j);
					find = true;
					break;
				}
			}
			if (!find) {
				System.out.println(i);
				System.out.println(text1);
				System.out.println(text2);
				
				String text = "Godzilla will stomp all over Tokyo if it rises from the ocean.";
				String c = Common.getLines("files/train_set.txt2").get(0);
				System.out.println(c + "#" + c.length());
				System.out.println(text + "#" + text.length());
				System.out.println(c.equals(text));
				System.out.println((int) c.charAt(0));
				Common.bangErrorPOS("!");
			}
		}
	}

	private static ArrayList<String> extractRaw(
			ArrayList<SentencePair> trainPairs, boolean chi) {
		ArrayList<String> chiRaw = new ArrayList<String>();
		for (SentencePair trainPair : trainPairs) {
			for (SentEntry entry : trainPair.entries) {
				if (chi) {
					chiRaw.add(entry.chiS);
				} else {
					chiRaw.add(entry.engS);
				}
			}
		}
		return chiRaw;
	}
}
