package preprocess;

import java.util.ArrayList;

import machineLearning.WinoUtil;
import util.Common;

public class ExtractSearchRaw {

	public static void main(String args[]) {
		// loadSearchXML();
		// System.out.println(searchMap.size());
		// System.out.println(searchMap.keySet().iterator().next());
		// System.out.println(searchMap.get(searchMap.keySet().iterator().next()).size());
		// if (true) {
		// return;
		// }
		WinoUtil.loadCounts();
		System.out.println("Load Complete");
		ArrayList<String> lines = new ArrayList<String>();

		double bad = 0;

		ArrayList<String> parseLines = new ArrayList<String>();

		for (String key : WinoUtil.countsMap.keySet()) {
			lines.add(key);
			long count = WinoUtil.countsMap.get(key);
			lines.add(Long.toString(count));
			ArrayList<String[]> tuples = WinoUtil.tuplesMap.get(key);

			parseLines.add(key + "。");

			for (String[] tuple : tuples) {
				lines.add(tuple[0]);
				lines.add(tuple[1]);
				lines.add(tuple[2]);
				lines.add("");
				lines.add("###");

				String t1 = WinoUtil.findText(key, tuple[1]);
				String t2 = WinoUtil.findText(key, tuple[2]);
				// System.out.println(key + " " + t1);
				// System.out.println(key + " " + t2);

				if (!t1.isEmpty()) {
					parseLines.add(t1 + "。");
				} else {
					if (!t2.isEmpty()) {
						parseLines.add(t2 + "。");
					}
				}

				// if (t1.isEmpty() && t2.isEmpty()) {
				// System.out.println(key);
				// System.out.println(tuple[1]);
				// System.out.println(tuple[2]);
				// System.out.println("----------");
				// bad ++;
				// }
			}
			lines.add("------------");
			parseLines.add("我是用来分隔关键词的。");
		}
		Common.outputLines(lines, "counts.allCC");
		Common.outputLines(parseLines, "searchParse.txt");
		System.out.println(bad / WinoUtil.countsMap.size());
	}
}
