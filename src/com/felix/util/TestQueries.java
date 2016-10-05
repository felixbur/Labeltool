package com.felix.util;

import java.io.InputStream;
import java.util.Vector;

public class TestQueries {
	KeyValues _testQueries = null;
	Vector<String> _sampleQueries = null;
	private int _counter = 0, _sampleQueryNum = 0;
	int[] _randomIndeces = null;
	private String _language = "";

	/**
	 * load test queries with filepath and language extension.
	 * 
	 * @param queryFilename
	 *            The filepath.
	 * @param language
	 *            The language extension, e.g. "de"
	 */
	public TestQueries(String queryFilename, String language) {
		_language = language;
		if (language != null) {
			loadTestQueries(queryFilename + "_" + language);
		} else {
			if (queryFilename != null)
				loadTestQueries(queryFilename);
		}
	}

	public void loadTestQueries(String queryFilename) {
		KeyValues queries = new KeyValues(queryFilename, "=");
		_testQueries = queries;
	}

	public String getLanguage() {
		return _language;
	}

	/**
	 * Load the testqueries from a vector of strings.
	 * 
	 * @param queryVec
	 *            The vector, formatted query=answer
	 */
	public void loadTestQueries(Vector<String> queryVec) {
		KeyValues queries = new KeyValues(queryVec, "=");
		_testQueries = queries;
	}

	/**
	 * Load the samplequeries from a vector of strings.
	 * 
	 * @param queryVec
	 *            The vector, formatted query
	 */
	public void loadSampleQueries(Vector<String> queryVec) {
		_sampleQueries = queryVec;
		_sampleQueryNum = _sampleQueries.size();
		_randomIndeces = Util.getRandomInts(_sampleQueryNum);
		_counter = _sampleQueryNum - 1;
	}

	/**
	 * Method for Android applications.
	 * 
	 * @param queryVec
	 *            An input stream.
	 * @throws Exception
	 */
	public void loadSampleQueries(InputStream queryVec) throws Exception {
		_sampleQueries = FileUtil.getFileLines(queryVec);
		_sampleQueryNum = _sampleQueries.size();
		_randomIndeces = Util.getRandomInts(_sampleQueryNum);
		_counter = _sampleQueryNum - 1;
	}

	public String getNextRandomSample() {
		if (_counter < 0)
			_counter = _sampleQueryNum - 1;
		String ret = _sampleQueries.elementAt(_randomIndeces[_counter]);
		--_counter;
		return ret;
	}

	public Vector<String> getTestQueries() {
		Vector<String> queries = _testQueries.getKeysAsVector();
		return queries;
	}

	public Vector<String> getSampleQueries() {
		return _sampleQueries;
	}
}
