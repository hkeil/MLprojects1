/*
 * Author            : AdNovum Informatik AG
 * Version Number    : $Revision: $
 * Date of last edit : $Date: $
 */

package ex4;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public abstract class Cluster {
	
	static int objCnt=0;
	enum Type {COUNTRY,CITY};
	
	int id;
	Type type;
	Metric metric;	
	Point center;
	double variance;
	
	Set<String> wordIntersection;
	Set<String> wordUnion;
	
	
	public abstract List<Point> getPoints();
	
	public Cluster(Metric metric,Type type) {
		super();
		this.id = objCnt++;
		this.type   = type;
		this.metric = metric;
	}
	
	public void createSets() {
		wordUnion        = new HashSet<String>();
		wordIntersection = new HashSet<String>(Arrays.asList(getPoints().get(0).words));
		
		for(Point point:getPoints()) {
			wordUnion.addAll(Arrays.asList(point.words));
			wordIntersection.retainAll(Arrays.asList(point.words));
		}
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
		out.println("         union="+Cluster.setToString(wordUnion));
		out.println("         all points:");
		for(Point point:getPoints()) {
			out.println("         '"+point.name+"'");
		}
		out.println();
		out.println();
	}
	
	void computeDistribution() {
		
		double minAvDist = -1;
		for(Point point:getPoints()) {
			double avDist = computeAverageDistance(point);
		
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


	private double computeAverageDistance(Point center) {
		
		int avDist = 0;
		for(Point point:getPoints()) {
			int dist = metric.distance(center.name, point.name);
			avDist += dist;
		}
		
		return avDist / getPoints().size();
	}

}