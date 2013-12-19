/*
 * Author            : AdNovum Informatik AG
 * Version Number    : $Revision: $
 * Date of last edit : $Date: $
 */

package ex4;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;


class CountryCluster extends Cluster{

	public Integer country;
	public List<Point> list;
	
	public CountryCluster(Metric metric,Integer country,Point[] data) {
		super(metric,Type.COUNTRY);
		this.country = country;
		this.list = new LinkedList<Point>();
		
		for (Point rec : data) {
			if(rec.country.equals(this.country)) {
				list.add(rec);
			}
		}
		
		createSets();
		//computeDistribution();
	}
	
	public void print(PrintStream out) {
		out.println("Cluster: type="+type+" id="+id);
		out.println("         country="+country);
		super.print(out);
	}

	/* (non-Javadoc)
	 * @see ex4.Main.Cluster#getPoints()
	 */
	@Override
	public List<Point> getPoints() {
		return list;
	}	
}