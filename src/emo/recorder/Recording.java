package emo.recorder;

import java.io.File;
import java.util.StringTokenizer;

/**
 * class to describe a recording.
 * 
 * @version 1.0
 * @author Felix Burkhardt
 */
public class Recording implements Comparable<Recording>, Angerable {

	public String name;

	public String path;

	public String recognition="";
	
	public int size;

	public String dialog = "unknown";

	public String words;

	public ClassificationResult _prediction;

	public double lab[] = null;
	private Categories _categories;
public IRecorder _recorder;
	
	public Recording(String path, String size, String words, double lab[],
			String prediction, String cateogories, IRecorder recorder) {
		this.path = path;
		_categories = new Categories(cateogories);
		// String sf[] = path.split(System.getProperty("file.separator"));
		// this.name = sf[sf.length-1];
		// this.dialog = sf[sf.length-2];
		File tmpF = new File(path);
		this.name = tmpF.getName();
		File parFile = tmpF.getParentFile();
		if (parFile != null) {
			this.dialog = parFile.getName();
		}
		this.size = Integer.parseInt(size);
		this.words = words;
		if (lab.length > 1 || lab[0] != -1) {
			this.lab = lab;
		}
		this._prediction = new ClassificationResult(prediction);
		_recorder = recorder;
	}

	public void setPrediction(String prediction) {
		this._prediction = new ClassificationResult(prediction);
	}

	public int compareTo(Recording o) {
		if (dialog.compareTo(o.dialog) == 0) {
			return -name.compareTo(o.name);
		}
		return -dialog.compareTo(o.dialog);
	}

	public String toString() {
		return dialog + " " + name + " " + size + " " + words + " "
				+ getAngerLabString() + " " + getAngerPredString();
	}

	public String labToString() {
		String ret = "";
		if (lab == null || lab.length == 0) {
			return "-1";
		}
		for (int i = 0; i < lab.length; i++) {
			ret += (int) lab[i] + " ";
		}
		return ret.trim();
	}

	public String getAngerPredString() {
		if (_prediction==null) return "-";
		String retString = _prediction.getWinner() != null ? _prediction
				.getWinner().toString() : "-";
		return retString;
	}

	public void removeLastLabel() {
		double newD[];
		if (lab != null && lab.length > 1) {
			newD = new double[lab.length - 1];
			for (int i = 0; i < lab.length - 1; i++) {
				newD[i] = lab[i];
			}
			this.lab = newD;
			return;
		}
		this.lab = null;
	}

	public void removePrediction() {
		this._prediction = null;
	}

	public String getAngerLabString() {
		if (getAngerLab() != null) {
			return getAngerLab() + " ("
					+ String.valueOf(Util.roundDoubleToOne(computeLab(lab)))
					+ ")" + " " + labToString();
		}
		return "-";
	}

	public String getTimeInSecString() {
		return Integer.toString(size / _recorder.getSampleRate());
	}

	public int getTimeInSec() {
		return size / _recorder.getSampleRate();
	}

	public void setAngerLab(double lab[]) {
		this.lab = lab;
	}

	public void addAngerLab(double d) {
		double newD[];
		System.err.println("adding label: " + d);
		if (lab != null && lab.length > 0) {
			newD = new double[lab.length + 1];
			for (int i = 0; i < lab.length; i++) {
				newD[i] = lab[i];
			}
			newD[lab.length] = d;
		} else {
			newD = new double[1];
			newD[0] = d;
		}
		this.lab = newD;
	}

	/**
	 * return the mean value. if majority is 0 return 0.
	 * 
	 * @param lab
	 * @return
	 */
	private double computeLab(double lab[]) {
		if (lab == null || lab.length == 0) {
			return -1;
		}
		double sum = 0;
		int sumOfZero = 0;
		;
		for (int i = 0; i < lab.length; i++) {
			sum += lab[i];
			if (lab[i] == 0) {
				sumOfZero++;
			}
		}
		// if most labelers judge "NA" return "NA"
		if (sumOfZero > lab.length / 2) {
			return 0;
		}
		return sum / lab.length;
	}

	public void setWords(String words) {
		this.words = words;
	}

	public String getAngerLab() {
		return _categories.getCategoryForJudgement(computeLab(lab));
	}

	public boolean isAngry() {
		if (computeLab(lab) >= 3) {
			return true;
		}
		return false;
	}

	public boolean seemsAngry(double threshold) {
		if (_prediction != null && _prediction.getWinner() != null
				&& _prediction.getWinner().getCat().startsWith("A")) {
			return true;
		}
		return false;

	}

	public int getSize() {
		return size;
	}

}