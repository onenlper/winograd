package preprocess;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import machineLearning.BuildFeatures;
import model.Mention;
import model.Mention.GramType;
import model.SentAttri;

public class CollectSV {

	public static class SVOStat implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public int unigramsAll = 0;
		public int svAll = 0;
		public int voAll = 0;

		public HashMap<String, Integer> unigramSet;
		public HashMap<String, Integer> svSet;
		public HashMap<String, Integer> voSet;

		public SVOStat() {
			this.unigramSet = new HashMap<String, Integer>();
			this.svSet = new HashMap<String, Integer>();
			this.voSet = new HashMap<String, Integer>();
		}

	}

	private static void put(String key, HashMap<String, Integer> map,
			HashMap<String, Integer> mapAll) {
		if (map.containsKey(key)) {
			return;
		} else {
			map.put(key, mapAll.containsKey(key) ? mapAll.get(key) : 0);
		}
	}

	public static int readMap(HashMap<String, Integer> map, String fn) {
		int count = 0;
		System.out.println("Reading.." + fn);
		try {
			BufferedReader br = new BufferedReader(new FileReader(fn));
			String line = "";
			while((line=br.readLine())!=null) {
				
				int a = line.trim().lastIndexOf(" ");
				
				String key = line.substring(0, a);
				int c = Integer.parseInt(line.substring(a+1));
				
				map.put(key, c);
				count += c;
			}
			
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return count;
	}
	
	public static void main(String args[]) throws Exception {
		
		ArrayList<SentAttri> sentAttris = BuildFeatures.initiate();
		SVOStat stat = new SVOStat();
		
		HashMap<String, Integer> unigAll= new HashMap<String, Integer>();
		HashMap<String, Integer> svAll = new HashMap<String, Integer>();
		HashMap<String, Integer> voAll = new HashMap<String, Integer>();
		
		stat.unigramsAll = readMap(unigAll, "unigram.giga");
		stat.svAll = readMap(svAll, "sv.giga.wino");
		stat.voAll = readMap(voAll, "vo.giga.wino");
		
		for(SentAttri sa : sentAttris) {
			
			Mention ant1 = sa.cands[0];
			Mention ant2 = sa.cands[1];
			
			Mention anaphor = sa.anaphor;
			
			put(ant1.head, stat.unigramSet, unigAll);
			put(ant2.head, stat.unigramSet, unigAll);
			
			if(anaphor.verbM!=null) {
				
				String verb = anaphor.verbM.extent;
				put(verb, stat.unigramSet, unigAll);
				
				if(anaphor.gramType==GramType.Subj) {
					put(ant1.head + " " + verb, stat.svSet, svAll);
					put(ant2.head + " " + verb, stat.svSet, svAll);
				} else if(anaphor.gramType==GramType.Obj) {
					put(ant1.head + " " + verb, stat.voSet, voAll);
					put(ant2.head + " " + verb, stat.voSet, voAll);
				}
			}
		}
		
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("svostat.object"));
		oos.writeObject(stat);
		oos.close();
	}
}
