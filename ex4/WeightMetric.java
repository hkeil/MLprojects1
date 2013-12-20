package ex4;

import java.util.HashMap;

public class WeightMetric implements ClusterMetric {

	@Override
	public double distance(Cluster cluster, Point point,StringMetric metric) {

		HashMap<String, Integer> frequencies = null;//cluster.wordFrequencies;

		int cnt = 0;
		for(String word:point.words) {
			cnt += cluster.wordFrequency(word,metric);
		}

		return cnt /= frequencies.size();
	}

}
