package model;

import java.util.ArrayList;
import java.util.HashMap;

public class SemanticRole {

	public HashMap<String, ArrayList<Mention>> roles;
	
	public Mention pred;
	
	public SemanticRole() {
		roles = new HashMap<String, ArrayList<Mention>>();
	}
	
	public void addRole(String role, Mention arg) {
		ArrayList<Mention> args = roles.get(role);
		if(args==null) {
			args = new ArrayList<Mention>();
			roles.put(role, args);
		}
		args.add(arg);
	}
	
}
