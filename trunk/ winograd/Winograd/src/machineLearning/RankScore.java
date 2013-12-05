package machineLearning;


import java.util.*;

import util.Common;


public class RankScore{

	public RankScore(String[] args){
		ArrayList<String> refs = Common.getLines(args[0]);
		ArrayList<String> preds = Common.getLines(args[1]);
	
		int correct = 0;
		int wrong = 0;
		int nodecision = 0;
		
		for(int i =0; i < refs.size() ; i += 2){
			int winner;
			
			if(Double.parseDouble(preds.get(i)) > Double.parseDouble(preds.get(i+1)))
				winner = 0;
			else if(Double.parseDouble(preds.get(i)) < Double.parseDouble(preds.get(i+1)))
				winner = 1;
			else
				winner = -1;	
			
			int real = -1;
			
			if(refs.get(i).split("\\s+")[0].equals("1"))
				real = 0;
			else if(refs.get(i).split("\\s+")[0].equals("0"))
				real = 1;				
			else{
				System.err.println("what the...hell");
				System.exit(1);	
			}
			
			if(winner != -1){
				if(winner == real) {
					correct++;
				} else {
					wrong++;	
				}	
			} else {
				nodecision++;
			}	
		}
		
		int all = correct + wrong + nodecision;
		
		System.out.println(correct + " : " + wrong + " : " + nodecision + "(" + correct*100.0/all + " : " + wrong*100.0/all + " : " + nodecision*100.0/all );
	}
	
	public static void main(String[] args){
		new RankScore(args);
	}
}