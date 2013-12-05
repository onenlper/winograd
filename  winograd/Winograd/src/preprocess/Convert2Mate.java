package preprocess;

import java.util.ArrayList;
import java.util.Arrays;

import model.stanford.StanfordResult;
import model.stanford.StanfordSentence;
import model.stanford.StanfordToken;
import model.stanford.StanfordXMLReader;
import util.Common;

public class Convert2Mate {

	public static void main(String args[]) {
		if(args.length!=0 && args[0].equals("concat")) {
			concatAgain();
			return;
		}
		
		String filename = "chiRaw.txt.xml";
		StanfordResult result = StanfordXMLReader.read(filename);
		ArrayList<String> mateLines = new ArrayList<String>();
		for (StanfordSentence s : result.sentences) {

			for (int i = 0; i < s.getTokens().size(); i++) {
				StanfordToken w = s.getToken(i);
				StringBuilder sb = new StringBuilder();
				sb.append(i).append('\t');
				sb.append(w.word).append('\t');
				sb.append('_').append('\t');
				sb.append('_').append('\t');
				sb.append('_').append('\t');
				sb.append(w.POS).append('\t');
				sb.append('_').append('\t');
				sb.append('_').append('\t');
				sb.append('0').append('\t');
				sb.append('0').append('\t');
				sb.append('_').append('\t');
				sb.append('_').append('\t');
				sb.append('_').append('\t');
				sb.append('_').append('\t');
				mateLines.add(sb.toString());

				if (w.word.equals(",") || w.word.equals("，")) {
					mateLines.add("");
				}
			}
			mateLines.add("");
		}
		Common.outputLines(mateLines, filename + ".mate.in");
	}

	public static void concatAgain() {
		ArrayList<String> lines = Common.getLines("chiSRL.out2");
		ArrayList<String> newLines = new ArrayList<String>();
		ArrayList<ArrayList<String>> subLines = new ArrayList<ArrayList<String>>();
		int dentOffset = 0;
		int idOffset = 0;
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);
			ArrayList<String> tks = new ArrayList<String>(Arrays.asList(line
					.trim().split("\\s+")));
			if (line.isEmpty()) {
				for (ArrayList<String> tmp : subLines) {
					int index = Integer.parseInt(tmp.get(0)) + idOffset;
					
					for(int k=0;k<dentOffset;k++) {
						tmp.add(14, "_");
					}
					
					StringBuilder sb = new StringBuilder();
					sb.append(index).append("\t");
					for (int j=1;j<tmp.size();j++) {
						String t = tmp.get(j);
						if(j==8 || j==9) {
							t = Integer.toString(Integer.parseInt(t) + idOffset);
						}
						sb.append(t).append("\t");
					}
					newLines.add(sb.toString().trim());

				}
				ArrayList<String> last = subLines.get(subLines.size()-1);
				if (!last.get(1).equals(",") && !last.get(1).equals("，")) {
					dentOffset = 0;
					idOffset = 0;
					newLines.add("");
				} else {
					if(last.size()>14) {
						dentOffset = last.size()-14;
					}
					idOffset += Integer.parseInt(last.get(0));
				}
				subLines.clear();
			} else {
				subLines.add(tks);
			}
		}
		
		// 2nd parse, fill previous dent
		subLines.clear();
		ArrayList<String> tmpLines = new ArrayList<String>();
		ArrayList<String> newLines2 = new ArrayList<String>();
		for(int i=0;i<newLines.size();i++) {
			String newLine = newLines.get(i);
			if(newLine.trim().isEmpty()) {
				int maxDent = tmpLines.get(tmpLines.size()-1).split("\\s+").length;
				
				for(int j=0;j<tmpLines.size();j++) {
					int dent = tmpLines.get(j).split("\\s+").length;
					StringBuilder sb = new StringBuilder();
					sb.append(tmpLines.get(j));
					for(int k=dent;k<maxDent;k++) {
						sb.append("\t").append("_");
					}
					newLines2.add(sb.toString().trim());
				}
				newLines2.add("");
				tmpLines.clear();
			} else {
				tmpLines.add(newLine);
			}
		}
		
		Common.outputLines(newLines2, "chiSRL.out");
	}
}
