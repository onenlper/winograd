package preprocess;

import java.util.ArrayList;

import util.Common;

public class ReplaceComma {
	
	public static void main(String args[]) {
		ArrayList<String> lines = Common.getLines("files/test_set.txt");
		
		ArrayList<String> output = new ArrayList<String>();
		for(int i=0;i<lines.size();i++) {
			String line = lines.get(i);
			
			if(i%10==1) {
				output.add(line.replace(",", "，"));
			} else {
				output.add(line);
			}
		}
		
		Common.outputLines(output, "files/test_set.txt2");
		
//		ArrayList<String> lines = Common.getLines("test_set.txt");
	}
	
	
}
