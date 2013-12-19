package ex4;

import java.util.HashMap;

public class WeightMetric implements ClusterMetric {

	@Override
	public double distance(Cluster cluster, Point point) {

		HashMap<String, Integer> frequencies = cluster.wordFrequencies;

		int cnt = 0;
		for(String word:point.words) {
			cnt += cluster.wordFrequency(word);
		}

		return cnt /= frequencies.size();
	}

}
