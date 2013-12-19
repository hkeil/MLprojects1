/*
 * Author            : AdNovum Informatik AG
 * Version Number    : $Revision: $
 * Date of last edit : $Date: $
 */

package ex4;

import java.io.PrintStream;
import java.util.List;


public abstract class Cluster {
	
	static int objCnt=0;
	enum Type {COUNTRY,CITY};
	
	int id;
	Type type;
	Metric metric;	
	Point center;
	double variance;
	
	
	public abstract List<Point> getPoints();
	
	public Cluster(Metric metric,Type type) {
		super();
		this.id = objCnt++;
		this.type   = type;
		this.metric = metric;
	}
	
	public void addWords() {
		for(Point point:getPoints()) {
			Dictionary.addWords(point.words, this);
		}
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
		out.println("Cluster: type="+type);
		//out.println("         center='"+center.name+"'");
		//out.println("         var="+variance);
		out.println("         #points="+getPoints().size());
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