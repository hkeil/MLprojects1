package ex4;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Dictionary {

	private static HashMap<String,Word> words = new HashMap<String,Word>();


	public static synchronized void addWords(String [] words,Cluster cluster){

		for(String word:words) {
			addWord(word, cluster);
		}
	}


	public static synchronized void addWord(String word,Cluster cluster){

		Word obj = words.get(word);
		if(obj == null) {
			obj = new Word(word);
			words.put(word, obj);
		}

		obj.addCluster(cluster);
	}
	
	public static List<Set<Cluster>> getClusters(String[] words,Metric metric,int distance) {

		List<Set<Cluster>> clusters = new LinkedList<Set<Cluster>>();

		for(String word:words) {
			clusters.add(getClusters(word, metric, distance));
		}
		
		return clusters;
	}

	public static Set<Cluster> getClusters(String word,Metric metric,int distance) {

		Set<Cluster> clusters = new HashSet<Cluster>();

		Iterator<Word> iter = words.values().iterator();		
		while(iter.hasNext()) {
			Word obj = iter.next();
			if(metric.distance(obj.word, word) < distance) {
				clusters.addAll(obj.clusters);
			}
		}
		
		return clusters;
	}


	public static void print(PrintStream out) throws FileNotFoundException {

		out.println("Words:");

		Iterator<Word> iter = words.values().iterator();		
		int cnt=0;
		while(iter.hasNext()) {
			out.println("   "+iter.next().toString());
			cnt++;
		}

		out.println("#words="+cnt);
		out.close();
	}

}
