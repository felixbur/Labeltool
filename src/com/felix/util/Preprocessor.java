package com.felix.util;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Pattern;

/**
 * Functionality to do a pattern matched and vocabulary based search and
 * replacement in text. Might be used as a preprocessor for text to speech
 * applications. The possibility to have different types of preprocessors for
 * specific use cases can be done by creating several instances. The
 * Preprocessors are configures by text files or string streams. The
 * configuration file (or stream) matches the type integers to vocabulary and
 * rule files where the replacements are specified.
 * 
 * 
 * @author felix
 * 
 */
public class Preprocessor {
	private static Object context;
	private static KeyValues config;
	private static HashMap<Integer, Preprocessor> preprocessors;
	private final static String RULES_KEY_EXT = "_rules";
	private final static String VOCAB_KEY_EXT = "_vocab";
	private KeyValue[] _rules;
	private KeyValue[] _vocab;
	private static String _langId = "";
	private Vector<Pattern> _rulePatterns;
	private boolean _useVocab = true;
	private static String _folder = "";

	/**
	 * <pre>
	 * Get an instance of a preprocessor for a certain type. The types are
	 * declared in the configuration file.
	 * Here's an example of a config file;
	 * TYPE_EMAIL=1
	 * TYPE_NEWS=2
	 * 1_rules.de=res/email_rules.txt
	 * 2_rules.de=res/news_rules.txt
	 * 1_vocab.de=res/email_vocab.txt
	 * 2_vocab.de=res/news_vocab.txt
	 * </pre>
	 * 
	 * @param type
	 *            The type of the preprocessor.
	 * @param configPath
	 *            The path to the configuration file.
	 * @return The preprocessor.
	 */
	public static synchronized Preprocessor getInstance(int type,
			String configPath, boolean forceNew) {
		if (config == null) {
			loadConfig(configPath);
		}
		return setUpInstances(type, forceNew);
	}

	/**
	 * Initialize an instance given the type, the configuration path and a
	 * parent folder containing the configuration files.
	 * 
	 * @param type
	 *            The type.
	 * @param configPath
	 *            The path to the configuration file.
	 * @param parentFolder
	 *            The folder containing the configuration.
	 * @param forceNew Get new instance even if exists
	 * @return
	 */
	public static synchronized Preprocessor getInstance(int type,
			String configPath, File parentFolder, boolean forceNew) {
		if (config == null) {
			loadConfig(configPath);
		}
		_folder = parentFolder.getAbsolutePath()
				+ System.getProperty("file.separator");
		return setUpInstances(type, forceNew);
	}

	public static synchronized Preprocessor getInstance(int type, boolean forceNew) {
		return setUpInstances(type, forceNew);
	}

	/**
	 * Get an instance with vocab and rulefle from specified path, omitting the
	 * configuration file. This will be assigned type == 0.
	 * 
	 * @param rulesFile
	 * @param vocabFile
	 * @return
	 */
	public static synchronized Preprocessor getInstance(String vocabFile,
			String rulesFile, boolean forceNew) {
		return setUpInstances(0, vocabFile, rulesFile, forceNew);
	}

	/**
	 * Get an instance with vocab and rulefle from specified path, omitting the
	 * configuration file. This will be assigned type == 0.
	 * 
	 * @param rulesFile
	 * @param vocabFile
	 * @param type
	 * @return
	 */
	public static synchronized Preprocessor getInstance(int type,
			String vocabFile, String rulesFile, boolean forceNew) {
		return setUpInstances(type, vocabFile, rulesFile, forceNew);
	}

	/**
	 * Constructor with two file names for simple use cases not needing
	 * preprocessors of multiple kinds and ignoring the Locale.
	 * 
	 * @param rulesFile
	 * @param vocabFile
	 */
	public Preprocessor(String rulesFile, String vocabFile) {
		try {
			_rules = readVocabData(rulesFile);
			if (vocabFile != null)
				_vocab = readVocabData(vocabFile);
			else
				_useVocab = false;
			buildRulePatterns();
		} catch (Exception e) {
			System.err.println("problem reading " + vocabFile + " and/or "
					+ rulesFile);
		}
	}

	/**
	 * Der Preprozessor sollte dahingehend erweitert werden, das er
	 * sprachspezifische Regeln verarbeiten kann.
	 * 
	 * Beispiel:
	 * 
	 * 8_mail_vocab 8_mail_vocab.de 8_mail_vocab.en
	 * 
	 * Preprozessor-Klasse bietet Methode setLocale(java.util.Locale) mit der
	 * festgelegt wird, welche Datei genutzt wird. Entweder eine vorhanden
	 * Sprachspezifische oder die Default-Datei (ohne Sprachangabe)
	 * 
	 * @param locale
	 *            The (global) Locale of all preprocessors.
	 */
	public static synchronized void setLocale(Locale locale) {
		_langId = "." + locale.getLanguage();
	}

	/**
	 * Get an instance of a preprocessor for a certain type. The types are
	 * declared in the configuration stream.
	 * 
	 * @param type
	 *            The type.
	 * @param configStream
	 *            The configuration.
	 * @return The Preprocessor.
	 */
	public static synchronized Preprocessor getInstance(int type,
			InputStream configStream) {
		if (config == null) {
			try {
				loadConfig(configStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return setUpInstances(type, false);
	}

	/**
	 * Get an instance of a preprocessor for a certain type. The types are
	 * declared in the configuration stream. The context object is needed to
	 * denote the application path.
	 * 
	 * @param type
	 * @param configStream
	 * @param context
	 *            Unspecified context (e.g. Android or Axis). Used in
	 *            AndroidHelper.
	 * @return
	 */
	public static synchronized Preprocessor getInstance(int type,
			InputStream configStream, Object context) {
		if (config == null) {
			Preprocessor.context = context;
			try {
				loadConfig(configStream);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return setUpInstances(type, false);
	}

	/**
	 * Search and replace patterns or vocabulary tokens like specified in the
	 * configuration,
	 * 
	 * @param input
	 *            The input String.
	 * @return The processed String.
	 */
	public String process(String input) {
		if (input == null) {
			System.err.println("WARNING: Preprocessor: null input");
			return "";
		}
		try {
			String ret = processRules(input);
			if (_useVocab)
				return replaceVocab(ret);
			else
				return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String getConfigString(String key) {
		return config.getString(key);
	}

	public static String getConfigPath(String key) {
		return config.getPathValue(key);
	}

	private static synchronized Preprocessor setUpInstances(int type, boolean forceNew) {
		// lookup ob typ schon gibt
		if (preprocessors == null) {
			// ansonsten neuen preprocessor f�r diesen typ initialisieren UND in
			// hashmap ablegen.
			preprocessors = new HashMap<Integer, Preprocessor>(0);
		}
		if (preprocessors.containsKey(Integer.valueOf(type))&&!forceNew) {
			return preprocessors.get(Integer.valueOf(type));
		} else {
			Preprocessor preprocessor = new Preprocessor(type);
			preprocessors.put(Integer.valueOf(type), preprocessor);
			return preprocessor;
		}

	}

	/**
	 * Remove all data about preprocessors.
	 */
	public static synchronized void removeAllInstances() {
		preprocessors = null;
	}

	private static synchronized Preprocessor setUpInstances(int type,
			String vocabFile, String rulesFile, boolean forceNew) {
		// lookup ob typ schon gibt
		if (preprocessors == null) {
			// ansonsten neuen preprocessor für diesen typ initialisieren UND in
			// hashmap ablegen.
			preprocessors = new HashMap<Integer, Preprocessor>(0);
		}
		if (preprocessors.containsKey(type) && !forceNew) {
			return preprocessors.get(type);
		} else {
			Preprocessor preprocessor = new Preprocessor(type, vocabFile,
					rulesFile);
			preprocessors.put(type, preprocessor);
			return preprocessor;
		}

	}
	private Preprocessor(int type) {
		String vocabFile = _folder
				+ Preprocessor.getConfigString(String.valueOf(type)
						+ VOCAB_KEY_EXT)+_langId;
		String rulesFile = _folder
				+ Preprocessor.getConfigString(String.valueOf(type)
						+ RULES_KEY_EXT)+_langId;
		if (context == null) {
			try {
				_rules = readVocabData(rulesFile);
			} catch (FileNotFoundException fnfe) {
				rulesFile = _folder
						+ Preprocessor.getConfigString(String.valueOf(type)
								+ RULES_KEY_EXT);
				try {
					_rules = readVocabData(rulesFile);
				} catch (Exception e) {
					System.err.println(" problem openening file " + rulesFile);
				}
			} catch (Exception e) {
				System.err.println(" problem openening file " + rulesFile);
			}
			try {
				_vocab = readVocabData(vocabFile);
			} catch (FileNotFoundException fnfe) {
				// try to get files without language extension
				vocabFile = _folder
						+ Preprocessor.getConfigString(String.valueOf(type)
								+ VOCAB_KEY_EXT);
				try {
					_vocab = readVocabData(vocabFile);
				} catch (Exception e) {
					System.err.println(" problem openening file " + vocabFile);
				}
			} catch (Exception e) {
				System.err.println(" problem openening file " + vocabFile);
			}
		} else {
			if (rulesFile != null && AndroidHelper.isUsable) {
				InputStream is = AndroidHelper.getRessourceInputStream(context,
						rulesFile);
				try {
					_rules = readVocabData(is);
				} catch (Exception e) {
					try {
						// try to get files without language extension
						rulesFile = _folder
								+ Preprocessor.getConfigString(String
										.valueOf(type) + RULES_KEY_EXT);
						is = AndroidHelper.getRessourceInputStream(context,
								rulesFile);
						_rules = readVocabData(is);
					} catch (Exception e2) {
						e.printStackTrace();
						System.err
								.println(" problem opening file " + rulesFile);
					}
				}
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if (vocabFile != null && AndroidHelper.isUsable) {
				InputStream is = AndroidHelper.getRessourceInputStream(context,
						vocabFile);
				try {
					_vocab = readVocabData(is);
				} catch (Exception e) {
					try {
						// try to get files without language extension
						vocabFile = _folder
								+ Preprocessor.getConfigString(String
										.valueOf(type) + VOCAB_KEY_EXT);
						is = AndroidHelper.getRessourceInputStream(context,
								vocabFile);
						_vocab = readVocabData(is);
					} catch (Exception e2) {
						e.printStackTrace();
						System.err.println(" problem openening file "
								+ vocabFile);
					}
				}
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		buildRulePatterns();
	}

	private Preprocessor(int type, String vocFile, String rulFile) {
		String vocabFile = null;
		if (vocFile != null)
			vocabFile = vocFile + _langId;
		String rulesFile = rulFile + _langId;
		try {
			_rules = readVocabData(rulesFile);
			if (vocFile != null)
				_vocab = readVocabData(vocabFile);
			else
				_useVocab = false;
		} catch (FileNotFoundException fnfe) {
			// try to get files without language extension
			if (vocFile != null)
				vocabFile = Preprocessor.getConfigString(String.valueOf(type)
						+ VOCAB_KEY_EXT);
			rulesFile = Preprocessor.getConfigString(String.valueOf(type)
					+ RULES_KEY_EXT);
			try {
				_rules = readVocabData(rulesFile);
				if (vocFile != null)
					_vocab = readVocabData(vocabFile);
				else
					_useVocab = false;
			} catch (Exception e) {
				System.err.println(" problem openening file " + vocabFile
						+ " and/or " + rulesFile);
			}
		} catch (Exception e) {
			System.err.println(" problem openening file " + vocabFile
					+ " and/or " + rulesFile);
		}
		buildRulePatterns();
	}

	private void buildRulePatterns() {
		_rulePatterns = new Vector<Pattern>();
		if (_rules == null)
			return;
		try {
			for (KeyValue rule : _rules) {
				_rulePatterns.add(Pattern.compile(rule.getKey()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String processRules(String input) {
		if (_rules == null) {
			return input;
		}
		String temp = input;
		int i = 0;
		try {
			for (Pattern rulePattern : _rulePatterns) {
				KeyValue srcDest = _rules[i++];
				temp = rulePattern.matcher(temp).replaceAll(srcDest.getValue());
			}
		} catch (IndexOutOfBoundsException ie) {
			return temp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return temp;
	}

	public String processRulesWithoutPatterns(String input) {
		if (_rules == null) {
			return input;
		}
		String temp = input;
		try {
			for (int i = 0; i < _rules.length; i++) {
				KeyValue srcDest = _rules[i];
				temp = temp.replaceAll(srcDest.getKey(), srcDest.getValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return temp;
	}

	private String replaceVocab(String input) {
		if (_vocab == null) {
			return input;
		}
		String temp = input;
		try {
			for (int i = 0; i < _vocab.length; i++) {
				KeyValue srcDest = _vocab[i];
				String orig = srcDest.getKey();
				String replace = srcDest.getValue();
				temp = StringUtil.replaceAll(temp, orig, replace);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return temp;
	}

	private static synchronized void loadConfig(String configPath) {
		try {
			config = new KeyValues(new File(configPath), "=",
					FileUtil.ENCODING_UTF_8);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static synchronized void loadConfig(InputStream configStream)
			throws Exception {
		config = new KeyValues(configStream, "=", FileUtil.ENCODING_UTF_8);
	}

	private KeyValue[] readVocabData(String filename) throws Exception {
		KeyValues keyValues = new KeyValues(new File(filename), "=",
				FileUtil.ENCODING_UTF_8);
		return keyValues.getKeyValues();
	}

	private KeyValue[] readVocabData(InputStream stream) throws Exception {
		KeyValues keyValues = new KeyValues(stream, "=",
				FileUtil.ENCODING_UTF_8);
		return keyValues.getKeyValues();
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			// String testString
			// =" (sa)tz mit apfel ,WG: das ist mein  gg afgel <beispiel\n> satz mit apfel ,na so.was [und] er wiederholt sich mit apfel ,na sowas.";
			String testString = "  [*] => blablubb<ref bla>huhu</ref>";
			Preprocessor test = Preprocessor.getInstance(1,
					"res/raw/preprocessor.properties", false);
			System.out.println(testString);
			System.out.println(test.process(testString));
		} else if (args.length == 4) {
			Preprocessor p = new Preprocessor(args[1], args[2]);
			try {
				Vector<String> fileLines = FileUtil.getFileLines(args[3]);
				for (Iterator<String> iterator = fileLines.iterator(); iterator
						.hasNext();) {
					String string = (String) iterator.next();
					if (string.trim().length() > 0) {
						System.out.println(p.process(string));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			String usage = "usage: Preprocessor <rules file> <vocab file> <input file>";
			System.out.println(usage);
		}
	}

	/**
	 * Return a vector with processed Strings.
	 * 
	 * @param in
	 *            The input vector.
	 * @return The result vector.
	 */
	public Vector<String> processVector(Vector<String> in) {
		Vector<String> retVec = new Vector<String>();
		for (String s : in) {
			retVec.add(process(s));
		}
		return retVec;
	}
}
