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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author AdNovum Informatik AG
 */
public class Transform {

	public static class TWord {

		private String value;
		private int cnt;
		private boolean valid;

		public TWord(String value) {
			super();
			this.value = value.toLowerCase();
			this.cnt = 1;
			this.valid = true;
		}

		public void incCnt() {
			cnt++;
		}

		public int cnt() {
			return cnt;
		}

		public String getValue() {
			return value;
		}
		
		public void invalid() {
			this.valid = false;
		}

		public boolean isValid() {
			return this.valid;
		}
	}


	//play around 
	private static int DIST_THRESHOLD = 5;
	private static int FREQ_THRESHOLD = 10;

	//list containing all words
	private static List<TWord> words = new LinkedList<Transform.TWord>();

	public static void main(String[] args) throws FileNotFoundException, IOException {

		DIST_THRESHOLD = 1;
		FREQ_THRESHOLD = 1;
		
		ArrayList<String[]> training = readFile("training.csv");
		int featureNum = processWords(training);
		
		
		//writeTransformed(training,featureNum,"training-transformed.csv");
		writeLevTransformed(training,featureNum,"training");
		
		ArrayList<String[]> validation = readFile("validation.csv");
		//writeTransformed(validation,featureNum,"validation-transformed.csv");
		writeLevTransformed(validation,featureNum,"validation");
		
		
		ArrayList<String[]> testing = readFile("testing.csv");
		//writeTransformed(testing,featureNum,"testing-transformed.csv");
		writeLevTransformed(validation,featureNum,"testing");
	}
	


	


	/**
	 * @param line
	 * @return
	 */
	private static String[] transform(String[] line,int featureNum) {
	
		String[] transformed = new String[featureNum + line.length - 1];
		
		String[] tokens = line[0].split(" ");
		
		int cnt=0;
		for(TWord word:words ) {
			
			if(!word.isValid()) {
				continue;
			}
			
			// count number of occurrences of word in tokens i.e. name
			int num = 0;
			for(String token:tokens) {
				if(isEqualWord(token, word.getValue())) {
					num++;
				}
			}
			transformed[cnt] = Integer.toString(num);
			cnt++;
		}
		
		// added labels if any
		if(line.length > 1) {
			transformed[cnt] = line[1];
			transformed[cnt+1] = line[2];
		}
		
		
		return transformed;
	}
	

	/**
	 * @param line
	 * @param dist
	 * @return
	 */
	private static String transformLev(String name, int dist) {
		
		String [] tokens = name.split(" ");
		StringBuffer buff = new StringBuffer();
		
		
		for(String token:tokens) {
			
			if(buff.length() != 0) {
				buff.append(" ");
			}
			
			for(TWord word:words) {
				if(computeDistance(word.getValue(), token) < dist) {
					buff.append(word.getValue());
					break;
				}
			}
		}
		
		return buff.toString();
	}




	/**
	 * @param validation
	 * @param featureNum
	 * @param string
	 * @throws IOException 
	 */
	private static void writeLevTransformed(ArrayList<String[]> lines, int featureNum,String name) throws IOException {
		
		for(int dist = 1;dist <10;dist++) {
			String filename = name+"-LevDist"+dist+".csv";
			CSVWriter writer = new CSVWriter(new FileWriter(filename),',',CSVWriter.NO_QUOTE_CHARACTER);
			
			for(String[] line:lines) {
				
				String transformed = transformLev(line[0],dist);
				line[0] = transformed;
				writer.writeNext(line);
			}
			
			writer.close();
			
			System.out.println("Wrote " + lines.size() + " data sets  to "+filename);
			
		}
		
	}





	private static void writeTransformed(ArrayList<String[]> lines, int featureNum, String filename) throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(filename),',',CSVWriter.NO_QUOTE_CHARACTER);
		for(String[] line:lines) {
			
			String[] transformed = transform(line,featureNum);
			writer.writeNext(transformed);
		}
		
		writer.close();
		
		System.out.println("Wrote " + lines.size() + " data sets  to "+filename);
	}



	private static int processWords(ArrayList<String[]> lines) {
		
		for(String[] line:lines) {
			addWords(line[0]);
		}
		
		// disable seldom used words	
		int cnt = 0;
		for(TWord word:words) {
			
			if(word.cnt() < FREQ_THRESHOLD) {
				word.invalid();
			}
			else {
				cnt++;
			}
		}
		
		System.out.println("#words=" + cnt + " FREQ_THRESHOLD="+FREQ_THRESHOLD+" DIST_THRESHOLD="+DIST_THRESHOLD);
		
		return cnt;
	}



	private static ArrayList<String[]> readFile(String filename) throws FileNotFoundException, IOException {
		CSVReader reader = new CSVReader(new FileReader(filename), ',');

		ArrayList<String[]> lines = new ArrayList<String[]>();
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			lines.add(nextLine);
		}
		System.out.println("Readed " + lines.size() + " data sets  from "+filename);
		reader.close();
		return lines;
	}





	public static boolean isEqualWord(String a,String b) {

		int dist = computeDistance(a,b);

		if(dist < DIST_THRESHOLD) {
			return true;
		}
		else {
			return false;
		}
	}


	private static void addWords(String name) {
		
		String [] nameTokens = name.split(" ");
		for(String word:nameTokens) {
			addWord(word);
		}
	}
	
	private static void addWord(String token) {
		
		for(TWord word:words) {
			
			if(isEqualWord(word.getValue(), token)) {
				word.incCnt();
				return;
			}
		}

		TWord word = new TWord(token);
		words.add(word);
	}

	public static int computeDistance(String s1, String s2) {

		//TODO: different distance algorithms
		return computeLevenshteinDistance(s1.toLowerCase(),s2.toLowerCase());
	}

	//see http://rosettacode.org/wiki/Levenshtein_distance#Java
	public static int computeLevenshteinDistance(String s1, String s2) {
		
		if(s1.equals(s2)) {
			return 0;
		}
		
		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();

		int[] costs = new int[s2.length() + 1];
		for (int i = 0; i <= s1.length(); i++) {
			int lastValue = i;
			for (int j = 0; j <= s2.length(); j++) {
				if (i == 0)
					costs[j] = j;
				else {
					if (j > 0) {
						int newValue = costs[j - 1];
						if (s1.charAt(i - 1) != s2.charAt(j - 1))
							newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
						costs[j - 1] = lastValue;
						lastValue = newValue;
					}
				}
			}
			if (i > 0)
				costs[s2.length()] = lastValue;
		}
		return costs[s2.length()];
	}
}
