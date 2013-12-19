package ex4;

import java.util.HashSet;
import java.util.Set;

class Word {
	String word;
	Set<Cluster> clusters;
	
	public Word(String word) {
		super();
		this.word = word;
		this.clusters = new HashSet<Cluster>();
	}
	
	public String toString() {
		
		StringBuffer buff = new StringBuffer("word='"+word+"' clusters=");
		
		for(Cluster cluster:clusters) {
			buff.append(cluster.id);
			buff.append(",");
		}
		
		return buff.toString();
	}
	
	public void addCluster(Cluster cluster) {
		clusters.add(cluster);
	}
}