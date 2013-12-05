package gigaword;

import java.io.File;
import java.util.ArrayList;

import model.syntaxTree.MyTree;
import model.syntaxTree.MyTreeNode;
import util.Common;
import util.StanfordToCoNLL;

public class Convert2CoNLL {

	public static void main(String args[]) {
		String folder = "/users/yzcchen/chen3/Winograd/zyparser";
		int i = 0;
		for (File subF : (new File(folder).listFiles())) {
			if (subF.isDirectory()) {
				for (File file : subF.listFiles()) {
					if (file.getAbsolutePath().endsWith(".text")) {
						i++;
						System.out.println(file.getAbsolutePath() + " " + i);
						if (!(new File(file.getAbsoluteFile() + ".conll")
								.exists())) {
							convert(file.getAbsolutePath());
						}
					}
				}
			}
		}

	}

	public static void convert(String filename) {
		ArrayList<String> lines = Common.getLines(filename);
		ArrayList<String> subLines = new ArrayList<String>();

		ArrayList<String> conllLines = new ArrayList<String>();
		for (int i = 0; i < lines.size(); i++) {
			String line = lines.get(i);

			if (line.equals("(NP (NR xxxx))")) {
				conllLines.add("#begin document conll");
				for (String treeStr : subLines) {
					MyTree tree = Common.constructTree(treeStr);
//					System.out.println(treeStr + " # " + tree.leaves.size());
//					for(MyTreeNode leaf : tree.leaves) {
//						System.out.println(leaf.value);
//					}
					StanfordToCoNLL.transform(conllLines, tree);
				}
				conllLines.add("#end document");
				subLines.clear();
			} else {
				subLines.add(line);
			}
		}
		Common.outputLines(conllLines, filename + ".conll");
	}
}
