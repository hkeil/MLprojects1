/*
 * Author            : AdNovum Informatik AG
 * Version Number    : $Revision: $
 * Date of last edit : $Date: $
 */

package ex4;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author AdNovum Informatik AG
 */
public class Main {
	
	public static String DATA_DIR = "C:\\Users\\hacke\\Documents\\CAS\\machine-learning\\exercise4\\java\\svm1\\svm1";
	
	
	public static Metric metrics[] = {new Levenshtein()};
	public static List<CityCluster> cities_list = new LinkedList<CityCluster>();
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		readTraingData(cities_list);
		
		Point[] newPoints = readFile(DATA_DIR + "/validation.csv");
		
		
		for(Point point:newPoints) {
			
			// shortcut
			if(directMatch(point, cities_list)) {
				continue;
			}
			
			predict(point,metrics[0]);
			
			break;
		}
		
		writePrediction(newPoints,  DATA_DIR + "/valres-j.csv");
		
	}


	private static boolean directMatch(Point point, List<CityCluster> clusters) {
		
		for(CityCluster cluster:clusters) {
			if(cluster.directMatch(point.name)) {
				point.city = cluster.city;
				point.country = cluster.country;
				return true;
			}
		}
		return false;
	}


	private static void predict(Point point,Metric metric) {
		
		
		int distance = 0;
		while(true) {
			List<Set<Cluster>> clusters = Dictionary.getClusters(point.words, metric, distance);
			
			Set<Cluster> intersection = intersect(clusters);

			if(intersection.size() == 0) {
				distance++;
			}
			else if(intersection.size() == 1) {
				CityCluster cluster = (CityCluster) clusters.iterator().next();
				point.city = cluster.city;
				point.country = cluster.country;
				
				System.out.println("Found unique match for '"+point.name+"'");
				
				break;
			}
			else {
				//FIXME: more then one cluster
				
				//we rake the first one
				CityCluster cluster = (CityCluster) intersection.iterator().next();
				point.city = cluster.city;
				point.country = cluster.country;
				
				System.out.println("Found not unique match for '"+point.name+"' size="+intersection.size());
				
				break;
			}

		}
	}


	private static Set<Cluster> intersect(List<Set<Cluster>> list) {
		
		Set<Cluster> intersection = new HashSet<Cluster>(list.get(0));
		
		for(Set<Cluster> clusters:list) {
			
			// we have a word that is completely new
			if(clusters.size() == 0) {
				continue;
			}
			
			intersection.retainAll(clusters);
			
		}
		
		return intersection;
	}


	private static void writePrediction(Point[] newPoints, String filename)throws IOException {
		
		CSVWriter writer = new CSVWriter(new FileWriter(filename),',');
		int cnt=0;
		for(Point point:newPoints) {
			writer.writeNext(point.toStringArray());
			
		}
		writer.close();
		
		System.out.println("Wrote " + cnt + " data sets  to " + filename);
	}


	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private static void readTraingData(List<CityCluster> cities_list2) throws FileNotFoundException, IOException {
		
		String dir = DATA_DIR;

		Point[] data = readFile(dir + "/training.csv");

		HashMap<Integer, Cluster> countries = new HashMap<Integer, Cluster>();
		HashMap<Integer, Cluster>    cities = new HashMap<Integer, Cluster>();
	
		for (Point rec : data) {
			
			// unused for the moment
			Cluster country = countries.get(rec.country);
			if(country == null) {
				Cluster cluster = new CountryCluster(metrics[0],rec.country, data);
				countries.put(rec.country,cluster);
			}
			
			Cluster city = cities.get(rec.city);
			if(city == null) {
				CityCluster cluster = new CityCluster(metrics[0],rec.country, rec.city, data);
				
				cluster.addWords();
				
				cities.put(rec.city, cluster);
				cities_list2.add(cluster);
				
			}
			
		}
		
		dump(countries,dir + "/training-by-country.csv");
		dump(cities,   dir + "/training-by-city.csv");
		
		System.out.println("#CityClusters:    "+cities.size());
		System.out.println("#CountryClusters: "+countries.size());
		
		
		printCityClusters(cities_list2,new PrintStream("CityCluster-Dump.txt"));
		//printCityClusters(cities_list2,System.out);
		
		
		Dictionary.print(new PrintStream("Dictionary-Dump.txt"));
		//Dictionary.print(System.out);
		
	}

	private static void printCityClusters(List<CityCluster> cities_list2,PrintStream out) throws FileNotFoundException {
		
		int cnt=0;
		for(Cluster cluster:cities_list2) {
			cluster.print(out);
			cnt += cluster.getPoints().size();
		}
		out.println("#points in CityClusters:    "+cnt);
	}

	static void dump(HashMap<Integer,Cluster> map,String filename) throws IOException {
		
		CSVWriter writer = new CSVWriter(new FileWriter(filename),',');
		
		Iterator<Integer> keys = map.keySet().iterator();
		int cnt = 0;
		while(keys.hasNext()){
			List<Point> data = map.get(keys.next()).getPoints();
			Collections.sort(data);
			for(Point rec:data) {
				writer.writeNext(rec.toStringArray1());
				cnt++;
			}
		}
		
		writer.close();
		
		System.out.println("Wrote " + cnt + " data sets  to " + filename);
	}

	
	private static Point[] readFile(String filename) throws FileNotFoundException, IOException {
		CSVReader reader = new CSVReader(new FileReader(filename), ',');

		ArrayList<String[]> lines = new ArrayList<String[]>();
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			lines.add(nextLine);
		}

		Point[] data = new Point[lines.size()];
		int cnt = 0;
		for (String[] line : lines) {
			// no labels, validation data
			if(line.length == 1) {
				data[cnt++] = new Point(line[0]);
			}
			// labels, training data
			else {
				data[cnt++] = new Point(line);
			}
		}
		reader.close();

		System.out.println("Readed " + data.length + " data sets  from " + filename);

		return data;
	}

}
