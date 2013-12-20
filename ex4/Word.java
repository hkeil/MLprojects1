package ex4;


public class Word {

	String value;
	StringMetric metric;
	
	public Word(String value,StringMetric metric) {
		super();
		this.metric = metric;
		this.value = value;
	}

	public boolean equals(Object other){
		
		if(other instanceof Word && metric.isEqual(this.value, ((Word) other).value) ) {
			return true;
		}
		else {
			return false;
		}
	}
}
