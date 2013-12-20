/*
 * Author            : AdNovum Informatik AG
 * Version Number    : $Revision: $
 * Date of last edit : $Date: $
 */

package ex4;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * @author AdNovum Informatik AG
 */

public class Point implements Comparable<Point>{
	
	public String name;
	public String[] words;
	public Set<String> wordSet;
	public Integer city;
	public Integer country;

	public Point(String[] line) {
		this.name = line[0].toLowerCase();
		this.city = Integer.parseInt(line[1]);
		this.country = Integer.parseInt(line[2]);
		init();
		
	}
	
	public Point(String name) {
		this.name = name.toLowerCase();
		this.city = -1;
		this.country = -1;
		init();
	}
	
	private void init() {
		this.words = this.name.split(" ");
		this.wordSet = new HashSet<String>();
		for(String word:words) {
			wordSet.add(word);
		}
	}
	
	public Set<Word> createWordSet(StringMetric metric) {
		
		Set<Word> set = new HashSet<Word>();
		for(String word:words) {
			set.add(new Word(word, metric));
		}
		
		return set;
	}

	/**
	 * @return
	 */
	public String[] toStringArrayPrediction() {
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

	public int containsWord(String word,StringMetric metric) {
		int cnt = 0;
		for(String token:words) {
			if(metric.isEqual(token, word)) {
				cnt++;
			}
		}
		return cnt;
	}


}
