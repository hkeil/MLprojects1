/*
 * Author            : AdNovum Informatik AG
 * Version Number    : $Revision: $
 * Date of last edit : $Date: $
 */

package ex4;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;


class CityCluster  extends Cluster{

	public Integer country;
	public Integer city;
	public List<Point> list;
	
	public CityCluster(Metric metric,Integer country,Integer city,Point[] data) {
		super(metric,Type.CITY);
		this.city = city;
		this.country = country;
		this.list = new LinkedList<Point>();
		
		for (Point rec : data) {
			
			if(rec.city.equals(this.city)) {
				
				if(!rec.country.equals(country)) {
					System.out.println("CityCluster country=" + this.country + "=city "+this.city+" ERROR:  " + rec);
					throw new IllegalArgumentException();
				}
				else {
					list.add(rec);
				}
			}
		}
		
		createSets();
		
		//computeDistribution();
	}

	
	@Override
	public List<Point> getPoints() {
		return list;
	}	
	
	public void print(PrintStream out) {
		out.println("Cluster: type="+type+" id="+id);
		out.println("         city="+city);
		out.println("         country="+country);
		super.print(out);
	}

}
