package preprocess;

import java.util.ArrayList;

import util.Common;

public class Convert2Mate2 {
	public static void main(String args[]) {
		String filename = "searchParse.txt.token";
		
		ArrayList<String> lines = Common.getLines(filename);
		
		ArrayList<String> mateLines = new ArrayList<String>();
		for (String line : lines) {
			String tks[] = line.split("\\s+");
			for (int i = 0; i < tks.length; i++) {
				String word = tks[i];
				StringBuilder sb = new StringBuilder();
				sb.append(i).append('\t');
				sb.append(word).append('\t');
				sb.append('_').append('\t');
				sb.append('_').append('\t');
				sb.append('_').append('\t');
				sb.append('_').append('\t');
				sb.append('_').append('\t');
				sb.append('_').append('\t');
				sb.append('0').append('\t');
				sb.append('0').append('\t');
				sb.append('_').append('\t');
				sb.append('_').append('\t');
				sb.append('_').append('\t');
				sb.append('_').append('\t');
				mateLines.add(sb.toString());
			}
			mateLines.add("");
		}
		Common.outputLines(mateLines, "searchParse.mate.in.search");
	}
}
