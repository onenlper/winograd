package preprocess;

import java.util.ArrayList;
import java.util.HashMap;

import util.Common;

public class ConcatStat {

	static HashMap<String, Integer> map = new HashMap<String, Integer>();
	
	public static void main(String args[]) {
		collect("aa");
		collect("ab");
		collect("ac");
		collect("ad");
		collect("ae");
		collect("af");
		collect("ag");
		collect("ah");
		collect("ai");
		collect("aj");
		Common.outputHashMap(map, "verbPair");
	}
	
	
	private static void collect(String file) {
		System.out.println(file);
		ArrayList<String> lines = Common.getLines(file);
		for(String line : lines) {
			Integer i = map.get(line);
			if(i==null) {
				map.put(line, 1);
			} else {
				map.put(line, i.intValue() + 1);
			}
		}
	}
}
