package emo.recorder;

import com.felix.util.KeyValue;

import com.felix.util.KeyValues;
import com.felix.util.StringUtil;

public class Categories {
	private String _catString;
	KeyValues _catKeys;

	public Categories(String initString) {
		_catKeys = new KeyValues(initString, ";", ",");
		_catString = _catKeys.getUniqValuesAsString();
	}

	public String getCategoryForJudgement(double judgement) {
		String lastCat = _catKeys.getKeyValues()[0].getValue();
		for (int i = 0; i < _catKeys.getKeyValues().length; i++) {
			KeyValue kv = _catKeys.getKeyValues()[i];
			if (judgement < Double.parseDouble(kv.getKey())) {
				return lastCat;
			} else {
				lastCat = kv.getValue();
			}
		}
		return lastCat;
	}

	public String toCommaSeparatedCategoryList() {
		return _catString.replace(" ", ",");
	}

	public String toString() {
		return _catKeys.toString();
	}

	public String[] getCategoryArray() {
		return StringUtil.stringToArray(_catString);
	}
}
