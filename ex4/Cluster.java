/*
 * Author            : AdNovum Informatik AG
 * Version Number    : $Revision: $
 * Date of last edit : $Date: $
 */

package ex4;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public abstract class Cluster {

	static int objCnt=0;
	enum Type {COUNTRY,CITY};

	int id;
	Type type;

	//unused
	Point center;
	double variance;

	Set<String> wordIntersection;
	Set<String> wordUnion;
	HashMap<String,WordFrequency> wordFrequencies;
	List<WordFrequency> wordFrequenciesList;


	public abstract List<Point> getPoints();

	public Cluster(Type type) {
		super();
		this.id = objCnt++;
		this.type   = type;
	}

	public void createSets(StringMetric metric) {
		wordUnion        = new HashSet<String>();
		wordIntersection = new HashSet<String>(Arrays.asList(getPoints().get(0).words));

		for(Point point:getPoints()) {
			wordUnion.addAll(Arrays.asList(point.words));
			wordIntersection.retainAll(Arrays.asList(point.words));
		}

		wordFrequencies = new HashMap<String, WordFrequency>();
		wordFrequenciesList = new LinkedList<WordFrequency>();

		for(String word:wordUnion) {
			int cnt = 0;
			for(Point point:getPoints()) {
				cnt += point.containsWord(word,metric);
			}
			WordFrequency freq = new WordFrequency(word, cnt);
			wordFrequencies.put(word, freq);
			wordFrequenciesList.add(freq);
		}

		Collections.sort(wordFrequenciesList);
	}

	public int wordFrequency(String word,StringMetric metric) {

		for(String tok:wordUnion) {
			if(metric.isEqual(tok, word)) {
				return wordFrequencies.get(tok).frequency;
			}
		}

		return 0;
	}

	public static String setToString(Set<String> set) {
		StringBuffer buff = new StringBuffer();
		for(String word:set) {
			buff.append(word+",");
		}
		return buff.toString();
	}

	public void addWords() {
		for(Point point:getPoints()) {
			Dictionary.addWords(point.words, this);
		}
	}

	public void printSetMatch(Point point,PrintStream out) {

		Set<String> intersection = new HashSet<String>(point.wordSet);
		intersection.retainAll(wordIntersection);

		Set<String> difference = new HashSet<String>(point.wordSet);
		difference.removeAll(wordUnion);

		out.println("Match cluster="+this.id);
		out.println("   intersection='"+Cluster.setToString(intersection)+"'");
		out.println("   difference='"+Cluster.setToString(difference)+"'");

	}


	public boolean directMatch(String name) {
		for(Point point:getPoints()) {
			if(point.name.equals(name)) {
				return true;
			}
		}
		return false;
	}


	public void print(PrintStream out) {
		out.println("         #points="+getPoints().size());
		out.println("         intersection="+Cluster.setToString(wordIntersection));
		out.println("         frequencies:");
		for(WordFrequency freq:wordFrequenciesList) {
			out.println("          "+freq.frequency+" '"+freq.word+"'");
		}
		out.println("         all points:");
		for(Point point:getPoints()) {
			out.println("         '"+point.name+"'");
		}
		out.println();
		out.println();
	}

	void computeDistribution(StringMetric metric) {

		double minAvDist = -1;
		for(Point point:getPoints()) {
			double avDist = computeAverageDistance(point,metric);

			if(minAvDist == -1 || avDist < minAvDist) {
				center = point; 
			}
		}

		variance = 0;
		for(Point point:getPoints()) {
			double dist =  metric.distance(center.name, point.name);
			variance += dist*dist;
		}

		variance /= getPoints().size();
	}


	private double computeAverageDistance(Point center,StringMetric metric) {

		int avDist = 0;
		for(Point point:getPoints()) {
			int dist = metric.distance(center.name, point.name);
			avDist += dist;
		}

		return avDist / getPoints().size();
	}

	public static List<Point> getAllPoints(Collection<Cluster> clusters) {
		List<Point> list = new LinkedList<Point>();

		for(Cluster cluster:clusters) {
			list.addAll(cluster.getPoints());
		}

		return list;
	}

}