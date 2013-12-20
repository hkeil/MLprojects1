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

	private static HashMap<String,DictionaryWord> words = new HashMap<String,DictionaryWord>();


	public static synchronized void addWords(String [] words,Cluster cluster){

		for(String word:words) {
			addWord(word, cluster);
		}
	}


	public static synchronized void addWord(String word,Cluster cluster){

		DictionaryWord obj = words.get(word);
		if(obj == null) {
			obj = new DictionaryWord(word);
			words.put(word, obj);
		}

		obj.addCluster(cluster);
	}
	
	public static List<Set<Cluster>> getCityClusters(String[] words,StringMetric metric,int distance) {

		List<Set<Cluster>> clusters = new LinkedList<Set<Cluster>>();

		for(String word:words) {
			clusters.add(getCityClusters(word, metric, distance));
		}
		
		return clusters;
	}
	

	public static List<Set<Cluster>> getCountryClusters(String[] words,StringMetric metric,int distance) {

		List<Set<Cluster>> clusters = new LinkedList<Set<Cluster>>();

		for(String word:words) {
			clusters.add(getCountryClusters(word, metric, distance));
		}
		
		return clusters;
	}

	public static Set<Cluster> getCityClusters(String word,StringMetric metric,int distance) {

		Set<Cluster> clusters = new HashSet<Cluster>();

		Iterator<DictionaryWord> iter = words.values().iterator();		
		while(iter.hasNext()) {
			DictionaryWord obj = iter.next();
			if(metric.distance(obj.word, word) < distance) {
				clusters.addAll(obj.cityClusters);
			}
		}
		
		return clusters;
	}
	

	public static Set<Cluster> getCountryClusters(String word,StringMetric metric,int distance) {

		Set<Cluster> clusters = new HashSet<Cluster>();

		Iterator<DictionaryWord> iter = words.values().iterator();		
		while(iter.hasNext()) {
			DictionaryWord obj = iter.next();
			if(metric.distance(obj.word, word) < distance) {
				clusters.addAll(obj.countryClusters);
			}
		}
		
		return clusters;
	}


	public static void print(PrintStream out) throws FileNotFoundException {

		out.println("DictionaryWords:");

		Iterator<DictionaryWord> iter = words.values().iterator();		
		int cnt=0;
		while(iter.hasNext()) {
			out.println("   "+iter.next().toString());
			out.println();
			cnt++;
		}

		out.println("#words="+cnt);
		out.close();
	}

}
