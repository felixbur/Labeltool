package emo.recorder;

import java.util.Iterator;

import java.util.StringTokenizer;
import java.util.Vector;

import com.felix.util.Util;

public class ClassificationResult {
	final static String NULL = "null";
	Vector<ClassResult> results;
	String _descr = "";
	boolean isNull = false;

	public ClassificationResult() {
	}

	public ClassificationResult(String resultsDescr) {
		if (resultsDescr.compareTo(NULL) == 0) {
			isNull = true;
		} else {
			try {
				StringTokenizer st = new StringTokenizer(resultsDescr, ",");
				while (st.hasMoreElements()) {
					String token = (String) st.nextElement();
					StringTokenizer st2 = new StringTokenizer(token, "=");
					String className = (String) st2.nextElement();
					double prob = Double.parseDouble((String) st2.nextElement());
					addResult(className, prob);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Vector<ClassResult> getResults() {
		return results;
	}

	public void addResult(String cat, double prob) {
		if (results == null) {
			results = new Vector<ClassResult>();
		}
		results.add(new ClassResult(cat, prob));
		_descr += " " + cat + " " + prob;
	}

	public ClassResult getWinner() {
		if (isNull) return null;
		ClassResult winner = null;
		double highestProb = 0;
		for (Iterator<ClassResult> iterator = results.iterator(); iterator
				.hasNext();) {
			ClassResult cr = (ClassResult) iterator.next();
			if (cr.probability > highestProb) {
				winner = cr;
				highestProb = cr.probability;
			}
		}
		return winner;
	}

	public ClassResult getResultForName(String catName) {
		if (isNull) return null;
		for (Iterator<ClassResult> iterator = results.iterator(); iterator
				.hasNext();) {
			ClassResult cr = (ClassResult) iterator.next();
			if (cr.getCat().compareTo(catName.trim()) == 0) {
				return cr;
			}
		}
		return null;
	}

	public String toString() {
		return _descr.trim();
	}

	public class ClassResult {
		private String cat;
		private double probability;

		public ClassResult(String cat, double probability) {
			super();
			this.cat = cat;
			this.probability = probability;
		}

		public String toString() {
			return cat + " (" + Util.cutDouble(probability) + ")";
		}

		public String getCat() {
			return cat;
		}

		public void setCat(String cat) {
			this.cat = cat;
		}

		public double getProbability() {
			return probability;
		}

		public void setProbability(double probability) {
			this.probability = probability;
		}

	}
}
