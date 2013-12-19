/*
 * Author            : AdNovum Informatik AG
 * Version Number    : $Revision: $
 * Date of last edit : $Date: $
 */

package ex4;


/**
 * @author AdNovum Informatik AG
 */

public class Point implements Comparable<Point>{
	
	public String name;
	public String[] words;
	public Integer city;
	public Integer country;

	public Point(String[] line) {
		this.name = line[0].toLowerCase();
		this.city = Integer.parseInt(line[1]);
		this.country = Integer.parseInt(line[2]);
		this.words = this.name.split(" ");
	}
	
	public Point(String name) {
		this.name = name.toLowerCase();
		this.city = -1;
		this.country = -1;
		this.words = this.name.split(" ");
	}

	/**
	 * @return
	 */
	public String[] toStringArray() {
		return new String[] {Integer.toString(city),Integer.toString(country)};
	}
	public String[] toStringArray1() {
		return new String[] {Integer.toString(country),Integer.toString(city),name};
	}
	
	public String toString() {
		return country+" "+city +" "+name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Point o) {
		
		if(this.country.compareTo(o.country) != 0) {
			return this.country.compareTo(o.country);
		}
		
		if(this.city.compareTo(o.city) != 0) {
			return this.city.compareTo(o.city);
		}
		
		return this.name.compareTo(o.name);
	}

}
