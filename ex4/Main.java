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
	
	public static String DATA_DIR = "/home/hacke/projects/svm1";
	
	
	public static Metric metrics[] = {new Levenshtein()};
	public static List<CityCluster> cities_list = new LinkedList<CityCluster>();
	public static Point[] training_data = null;
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		training_data = readTraingData(cities_list);
		
		Point[] newPoints = readFile(DATA_DIR + "/validation.csv");
		
		int cnt = 0;
		for(Point point:newPoints) {
			
			// shortcut
			//if(directMatch(point, cities_list)) {
			//	continue;
			//}
			
			predict2(point,metrics[0]);
			
			if(cnt++ == 10){
				break;
			}
			
		}
		
		writePrediction(newPoints,  DATA_DIR + "/valres-j.csv");
		
	}


	


	private static boolean directMatch(Point point, List<CityCluster> clusters) {
		
		for(CityCluster cluster:clusters) {
			if(cluster.directMatch(point.name)) {
				point.city = cluster.city;
				point.country = cluster.country;
				
				System.out.println("Found direct match for '"+point.name+"'");
				return true;
			}
		}
		return false;
	}

	/**
	 * @param point
	 * @param metric
	 */
	private static void predict2(Point point, Metric metric) {
		
		int minDist = -1;
		Point prediction = null;
		for(Point train:training_data) {
			
			int dist = metric.distance(train.name, point.name);
			
			if(minDist == -1 || dist < minDist) {
				minDist = dist;
				prediction = train;
			}
		}
		
		point.city = prediction.city;
		point.country = prediction.country;
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
				printMatches(intersection, point, System.out);
				
				CityCluster cluster = (CityCluster) clusters.iterator().next();
				point.city = cluster.city;
				point.country = cluster.country;
				
				System.out.println("Found unique match for '"+point.name+"'");
				
				break;
			}
			else {
				printMatches(intersection, point, System.out);
				
				//FIXME: more then one cluster
				
				//we rake the first one
				CityCluster cluster = (CityCluster) intersection.iterator().next();
				point.city = cluster.city;
				point.country = cluster.country;
				
				System.out.println("Found not unique match for '"+point.name+"' clusters="+clustersToString(intersection));
				
				break;
			}

		}
	}


	/**
	 * @param intersection
	 * @return
	 */
	private static String clustersToString(Set<Cluster> clusters) {
		StringBuffer buff = new StringBuffer();
		for(Cluster cluster:clusters) {
			buff.append(cluster.id+",");
			
		}
		
		return buff.toString();
	}
	
	private static void printMatches(Set<Cluster> clusters,Point point,PrintStream out) {
		
		out.println("Matches for name='"+point.name+"'");
		for(Cluster cluster:clusters) {
			cluster.printSetMatch(point, out);
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
		
		CSVWriter writer = new CSVWriter(new FileWriter(filename),',',CSVWriter.NO_QUOTE_CHARACTER);
		int cnt=0;
		for(Point point:newPoints) {
			writer.writeNext(point.toStringArrayPrediction());
			
		}
		writer.close();
		
		System.out.println("Wrote " + cnt + " data sets  to " + filename);
	}


	/**
	 * @param args
	 * @return 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private static Point[] readTraingData(List<CityCluster> cities_list) throws FileNotFoundException, IOException {
		
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
				cities_list.add(cluster);
				
			}
			
		}
		
		dump(countries,dir + "/training-by-country.csv");
		dump(cities,   dir + "/training-by-city.csv");
		
		System.out.println("#CityClusters:    "+cities.size());
		System.out.println("#CountryClusters: "+countries.size());
		
		
		printClusters(cities,new PrintStream("CityCluster-Dump.txt"));
		printClusters(countries,new PrintStream("CountryCluster-Dump.txt"));
		
		
		Dictionary.print(new PrintStream("Dictionary-Dump.txt"));
		//Dictionary.print(System.out);
		
		return data;
		
	}

	private static void printClusters(HashMap<Integer, Cluster> cities,PrintStream out) throws FileNotFoundException {
		
		int cnt=0;
		Iterator<Cluster> iter = cities.values().iterator();
		while(iter.hasNext()) {
			Cluster cluster = iter.next();
			cluster.print(out);
			cnt += cluster.getPoints().size();
		}
		out.println("#points in Clusters:    "+cnt);
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
