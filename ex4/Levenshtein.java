/*
 * Author            : AdNovum Informatik AG
 * Version Number    : $Revision: $
 * Date of last edit : $Date: $
 */

package ex4;

/**
 * @author AdNovum Informatik AG
 */
public class Levenshtein implements StringMetric {
	
	private int equalsThreshold;
	

	public Levenshtein(int equalsThreshold) {
		super();
		this.equalsThreshold = equalsThreshold;
	}

	/* (non-Javadoc)
	 * @see ex4.Metric#distance(java.lang.String, java.lang.String)
	 */
	@Override
	public int distance(String a, String b) {
		// TODO Auto-generated method stub
		return computeDistance(a,b);
	}

	// see http://rosettacode.org/wiki/Levenshtein_distance#Java
	public static int computeDistance(String s1, String s2) {
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

	@Override
	public boolean isEqual(String a, String b) {
		
		return distance(a,b) < equalsThreshold;
	}
}
