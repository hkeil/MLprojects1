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
import java.util.Arrays;
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

	//public static String DATA_DIR = "/home/hacke/projects/svm1";
	public static String DATA_DIR = "C:\\Users\\hacke\\Documents\\CAS\\machine-learning\\exercise4\\java\\svm1\\svm1";

	public static int STRING_EQUALS_THRESHOLD = 1;
	public static StringMetric string_metrics[] = {new Levenshtein(STRING_EQUALS_THRESHOLD)};
	public static ClusterMetric cluster_metrics[] = {new WeightMetric()};

	public static List<CityCluster>    cities_list = new LinkedList<CityCluster>();
	public static List<CountryCluster> countries_list = new LinkedList<CountryCluster>();
	public static Point[] training_data = null;
	public static List<Point> training_data_list = null;

	public static void main(String[] args) throws FileNotFoundException, IOException {

		training_data = readTraingData(cities_list,countries_list,string_metrics[0]);
		training_data_list = Arrays.asList(training_data);

		Point[] newPoints = readFile(DATA_DIR + "/validation.csv");

		int cnt = 0;
		int cntDirect = 0;
		for(Point point:newPoints) {



			// shortcut
			//if(directMatch(point, cities_list)) {
			//cntDirect++;
			//continue;
			//}

			//predict1(point,string_metrics[0]);
			predict2(point,string_metrics[0]);
			//predict3(point,cluster_metrics[0]);
			//predict4(point,cluster_metrics[0],string_metrics[0]);
			//predict5(point,string_metrics[0]);

			cnt++;
			if(cnt %100  == 0){
				System.out.println("Prediction: cnt='"+cnt+"'");
			}

		}
		System.out.println("Prediction: cntDirect='"+cntDirect+"' "+(100 *cntDirect/cnt)+"%");

		writePrediction(newPoints,  DATA_DIR + "/valres-j-predict2.csv");

	}

	private static boolean directMatch(Point point, List<CityCluster> clusters) {

		for(CityCluster cluster:clusters) {
			if(cluster.directMatch(point.name)) {
				point.city = cluster.city;
				point.country = cluster.country;

				//System.out.println("Found direct match for '"+point.name+"'");
				return true;
			}
		}
		return false;
	}


	// nearest city cluster search
	private static void predict4(Point point, ClusterMetric clusterMetric,StringMetric metric) {

		List<String> words = new LinkedList<String>(Arrays.asList(point.words));

		// find country with the nearest distance
		CountryCluster country = null;

		for(int dist=1;dist < 10;dist++) {

			for(int idx =0;idx < words.size();idx++ ){

				String word = words.get(idx);
				for(CountryCluster cluster:countries_list ) {
					if( metric.distance(cluster.wordFrequenciesList.get(0).word,word) < dist) {

						country = cluster;
						// remove the word, associated with the country
						words.remove(idx);
						break;
					}
				}
			}

			if(country != null) {
				break;
			}
		}


		//find city in the same manner
		CityCluster city = null;

		for(int dist=1;dist < 10;dist++) {

			for(int idx =0;idx < words.size();idx++ ){

				String word = words.get(idx);
				for(CityCluster cluster:cities_list) {
					if( metric.distance(cluster.wordFrequenciesList.get(0).word,word) < dist) {

						city = cluster;
						// remove the word, associated with the country
						words.remove(idx);
						break;
					}
				}
			}

			if(city != null) {
				break;
			}
		}

		if(country == null) {
			System.out.println("No country found for name='"+point.name+"'");
			point.country = 0;
		}
		else {
			point.country = country.country;
		}

		if(city == null) {
			System.out.println("No city found for name='"+point.name+"'");
			point.city = 0;
		}
		else {
			point.city = city.city;
		}


		//System.out.println("CC for name='"+point.name+"':");
		//prediction.print(System.out);
	}

	// nearest city cluster search
	private static void predict3(Point point, ClusterMetric clusterMetric,StringMetric metric) {

		double maxWeight = -1;
		CityCluster prediction = null;

		for(CityCluster cluster:cities_list ) {

			double weigth = clusterMetric.distance(cluster, point,metric);
			if(maxWeight == -1 || weigth > maxWeight) {
				maxWeight = weigth;
				prediction = cluster;
			}
		}

		point.city = prediction.city;
		point.country = prediction.country;

		//System.out.println("CC for name='"+point.name+"':");
		//prediction.print(System.out);
	}

	/**
	 * @param point
	 * @param metric
	 */
	// nearest point search
	private static void predict2(Point point, StringMetric metric) {

		Point prediction = nnSearch(point, training_data_list, metric);
		point.country = prediction.country;
		point.city    = prediction.city;
	}

	// predict in a combinatorial fashion
	private static void predict1(Point point,StringMetric metric) {
		
		int distance = 0;
		Set<Cluster> countries = null;
		Set<Cluster> cities    = null;

		distance = 0;
		// get all countries containing at least one word of the name
		while(true) {
			List<Set<Cluster>> clusters = Dictionary.getCountryClusters(point.words, metric, distance);
			countries = intersect(clusters);

			if(countries.size() == 0) {
				distance++;
				continue;
			}
			break;
		}

		distance = 0;
		// get all cities containing at least one word of the name
		while(true) {
			List<Set<Cluster>> clusters = Dictionary.getCityClusters(point.words, metric, distance);
			cities = intersect(clusters);

			if(cities.size() == 0) {
				distance++;
				continue;
			}
			break;
		}

		//remove all cities that have a country code we have not found
		Set<Cluster> remove = new HashSet<Cluster>();
		if(cities.size() > 1) {
			for(Cluster city:cities) {
				boolean found = false;
				for(Cluster country:countries) {
					if( ((CountryCluster)country).country == ((CityCluster)city).country) {
						found = true;
						break;
					}
				}
				if(found == false) {
					remove.add(city);
				}
			}
			cities.removeAll(remove);
		}



		if(cities.size() == 0) {
			//System.out.println("Empty city list for name='"+point.name+"' nnSearch over countries");
			//System.out.println("       countries='"+clustersToString(countries)+"'");
			//System.out.println("       cities='"+clustersToString(cities)+"'");
			
			Point prediction = nnSearch(point, Cluster.getAllPoints(countries), metric);
			point.country = prediction.country;
			point.city    = prediction.city;
		}
		else if(cities.size() == 1) {
			//System.out.println("Unique match name='"+point.name+"'");
			CityCluster city = (CityCluster) cities.iterator().next();
			point.country = city.country;
			point.city    = city.city;
			
		}
		else {
			//System.out.println("Not Unique match name='"+point.name+"' size="+cities.size()+" nnSearch");
			Point prediction = nnSearch(point, Cluster.getAllPoints(cities), metric);
			point.country = prediction.country;
			point.city    = prediction.city;
		}
	}
	
	private static Point nnSearch(Point point, List<Point> training,StringMetric metric) {

		int minDist = -1;
		Point prediction = null;
		for(Point train:training) {

			int dist = metric.distance(train.name, point.name);

			if (dist < 0) {
				throw new IllegalStateException();
			}

			if(minDist == -1 || dist < minDist) {
				minDist = dist;
				prediction = train;
				if(minDist == 0) {
					//System.out.println("NN direct match for name='"+point.name+"' is '"+prediction.name+"'");
					break;
				}
			}
		}
		
		return prediction;
		//System.out.println("NN for name='"+point.name+"' is '"+prediction.name+"'");
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
			cnt++;
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
	private static Point[] readTraingData(List<CityCluster> cities_list,List<CountryCluster> countries_list,StringMetric metric) throws FileNotFoundException, IOException {

		String dir = DATA_DIR;

		Point[] data = readFile(dir + "/training.csv");

		HashMap<Integer, Cluster> countries = new HashMap<Integer, Cluster>();
		HashMap<Integer, Cluster>    cities = new HashMap<Integer, Cluster>();

		for (Point rec : data) {

			// unused for the moment
			Cluster country = countries.get(rec.country);
			if(country == null) {
				CountryCluster cluster = new CountryCluster(rec.country, data);
				cluster.createSets(metric);
				cluster.addWords();

				countries.put(rec.country,cluster);
				countries_list.add(cluster);

			}

			Cluster city = cities.get(rec.city);
			if(city == null) {
				CityCluster cluster = new CityCluster(rec.country, rec.city, data);
				cluster.createSets(metric);
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
