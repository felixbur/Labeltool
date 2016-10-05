package emo.recorder.gui;

import javax.sound.sampled.AudioFormat;
import javax.swing.*;
import javax.swing.table.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.event.*;

import com.felix.util.AudioUtil;
import com.felix.util.DateTimeUtil;
import com.felix.util.FileUtil;
import com.felix.util.KeyValues;
import com.felix.util.NumberToWord;
import com.felix.util.StringUtil;
import com.felix.util.SwingUtil;
import com.felix.util.Util;

import emo.recorder.Analyser;
import emo.recorder.ClassificationResult;
import emo.recorder.Constants;
import emo.recorder.Dialog;
import emo.recorder.EvaluateThread;
import emo.recorder.GetAnswerThread;
import emo.recorder.IRecorder;
import emo.recorder.InterfaceServer;
import emo.recorder.JudgeThread;
import emo.recorder.PlayThread;
import emo.recorder.RecognitionThread;
import emo.recorder.RecordThread;
import emo.recorder.Recording;
import emo.recorder.RecordingTable;
import emo.recorder.ResetThread;
import emo.recorder.SendMessageThread;
import emo.recorder.SetFileEmotionThread;
import emo.recorder.SendTranscriptionThread;
import emo.recorder.UndoThread;
import emo.recorder.ClassificationResult.ClassResult;

/**
 * The main class for the webrecorder-client. Usable as an applet or
 * application. Every communication with the server is implemented as a thread,
 * so that the window won't freeze if the connection takes time.
 * 
 * @version 1.0
 * @author Felix Burkhardt
 */
public class Recorder extends JApplet implements ActionListener, IRecorder {

	private static final long serialVersionUID = 1L;
	private RecordThread recThread;
	private JudgeThread judgeThread;
	private UpdateFileListThread updateThread;
	private UndoThread undoThread;
	private PlayThread playThread;
	private InterfaceServer interfaceServer;
	private int lastBytesPlayed = 0;
	private JLabel neutralLab, haLab, configlabel, emoConfigLabel;
	private JPanel pane;
	private JSlider wavPositionSlider;
	boolean showTranscript = true;
	private String servername, _audioType;
	private RecordingsTableModel _recordings;
	protected JTable _table;
	private int selectedRow;
	private int tableheight;
	private int _samplerate;
	private JTextArea transField;
	private HashMap<String, String> _charReplacements, _classes,
			_abbreviations, _prefixes;
	private JButton _record, _play, _stop, _refresh, _exec, _open, _resume,
			_judge, _train, judgeAll, _delete, statistics, _transcribe,
			evaluate, recognize, recognizeAll, removeUntagged, delAllPreds,
			toggleTranscript, copyRecognition, _synthesize, _synthesizeAll,
			_normalize, _normalizeAll, _wer, _export, _import, setNA,
			removeLastLabel, removePred, _rename, _shuffleButton;
	private JCheckBox fastModeCheck, fastPlayModeCheck, extractFeaturesCheck,
			numberToWordCheck, _openDirectory, _openModel, _evalFiles,
			_checkSpelling, _ttsSexFemale;
	private JLabel fastModeLabel, fastPlayModeLabel, extractFeaturesLabel,
			numberToWordLabel, _titleLabel;
	private AudioFormat _formatPCM;
	private JSpinner _classifierSpinner, _srSpinner, _afSpinner, _ttsLanguages;
	private String _dataDescription = "";
	private boolean numberToWord = false;
	private String _fileDir = "";
	private KeyValues _config;

	public Recorder(KeyValues config) {
		_config = config;
		init();
		start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#init()
	 */
	public void init() {
		try {
			try {
				servername = _config.getString("url");
				_samplerate = Integer.parseInt(_config.getString("sampleRate"));
				_formatPCM = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
						_samplerate, 16, 1, 2, _samplerate, false);
				numberToWord = Boolean.parseBoolean(_config
						.getString("numberToWord"));
			} catch (Exception e) {
				System.err.println("Kein URL Parameter");
			}
			try {
				tableheight = Integer
						.parseInt(_config.getString("tableheight"));
			} catch (Exception e) {
				tableheight = 300;
			}
			getContentPane().add(makeContentPane());
			interfaceServer = new InterfaceServer(servername,
					Integer.parseInt(_config.getString("port")));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Get a parameter String.
	 */
	public String getParameter(String name) {
		return _config.getString(name);
	}

	/**
	 * designs the main drawing area as a combination nof three panes: <br>
	 * header pane - for the logo <br>
	 * table pane - to show the recordings in a table <br>
	 * button pane - for the buttons.
	 * 
	 * @return The main pane
	 */
	public Container makeContentPane() {
		pane = new JPanel();
		pane.add(makeHeaderPane());
		pane.add(makeTablePane());
		if (Boolean.parseBoolean(_config.getString("withRecorderControl"))) {
			pane.add(makeRecorderButtonPane());
		}
		Container controlButtons = makeControlButtonPane();
		pane.add(controlButtons);
		if (!Boolean.parseBoolean(_config
				.getString("withClassificationButtons"))) {
			controlButtons.setVisible(false);
		}
		if (!Boolean.parseBoolean(_config.getString("transcribeOnly"))) {
			pane.add(makeJudgeButtonPane());
		}
		Container transcriptionPane = makeTranscriptionPane();
		pane.add(transcriptionPane);
		if (!Boolean.parseBoolean(_config.getString("withTranscribe"))) {
			transcriptionPane.setVisible(false);
		}
		if (Boolean.parseBoolean(_config.getString("classification"))) {
			pane.add(makeResultPane());
		}
		if (Boolean.parseBoolean(_config.getString("withRecognizer"))) {
			pane.add(makeRecognitionPane());
		}
		if (Boolean.parseBoolean(_config.getString("withSynthesizer"))) {
			pane.add(makeSynthesizerPane());
		}
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		return (pane);
	}

	public void repaintView() {
		System.out.println("refreshing view, recordings no: "
				+ _recordings.getRowCount());
		this.setNrOfRecordings();
		// if (recordings.getRowCount()>0) {
		lastSelectedRow = 0;
		setFocusTable();
		// }
		//
		// this.recordings.fireTableDataChanged();
		// this.repaint();
	}

	/**
	 * Designs the header pane showing a centered logo. Expects an image called
	 * "webRecorderLogo.gif" in the html directory.
	 * 
	 * @return The header pane.
	 */
	public Container makeHeaderPane() {
		_titleLabel = new JLabel(Constants.title + ", version: "
				+ Constants.version, JLabel.CENTER);
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.X_AXIS));
		pane.setPreferredSize(new Dimension(600, 50));
		pane.add(_titleLabel);
		return pane;
	}

	public void setServerVersion(String version) {
		_titleLabel = new JLabel(Constants.title + ", version: "
				+ Constants.version + " (server version: " + version + ")");
		// @todo: doesn't work
	}

	/**
	 * Designs the pane to Vshow the result from emotion-judgement.
	 * 
	 * @return The pane.
	 */
	JLabel emoResultNeutralLab;

	JProgressBar emoResultNeutralPG;
	JLabel emoResultColdAngerLab;

	JProgressBar emoResultColdAngerPG;

	JLabel emoResultHotAngerLab;

	JTextArea _nrOfRecordingsLab;

	JProgressBar emoResultHotAngerPG;

	int lastSelectedRow = 0;

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#setNrOfRecordings()
	 */
	public void setNrOfRecordings() {
		_nrOfRecordingsLab.setText("no. of recordings: "
				+ _recordings.getRowCount());
	}

	public boolean showTranscript() {
		return showTranscript;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#analyse()
	 */
	public void analyse() {
		_nrOfRecordingsLab.setText(_dataDescription);
	}

	public Container makeResultPane() {
		neutralLab = new JLabel(_config.getString("noAnger.label"));
		haLab = new JLabel(_config.getString("anger.label"));
		emoResultNeutralLab = new JLabel("");
		emoResultNeutralPG = new JProgressBar(0, 100);
		emoResultNeutralPG.setForeground(Color.green);
		emoResultColdAngerLab = new JLabel("");
		emoResultColdAngerPG = new JProgressBar(0, 100);
		emoResultColdAngerPG.setForeground(Color.red);
		emoResultHotAngerLab = new JLabel("");
		emoResultHotAngerPG = new JProgressBar(0, 100);
		emoResultHotAngerPG.setForeground(Color.red);
		JPanel pane = new JPanel();
		pane.setBackground(new Color(210, 210, 210));
		pane.setBorder(BorderFactory.createMatteBorder(1, 1, 2, 2, Color.CYAN));
		pane.add(neutralLab);
		pane.add(emoResultNeutralLab);
		pane.add(emoResultNeutralPG);
		pane.add(haLab);
		pane.add(emoResultHotAngerLab);
		pane.add(emoResultHotAngerPG);
		configlabel = new JLabel("Konfiguration: ");
		emoConfigLabel = new JLabel("");
		pane.add(configlabel);
		pane.add(emoConfigLabel);
		return pane;
	}

	/**
	 * Designs a pane to draw the table with recordings. The behaviour of the
	 * table depends on its tablemodel.
	 * 
	 * @return The table pane.
	 */
	public Container makeTablePane() {
		_recordings = new RecordingsTableModel(this,
				Boolean.parseBoolean(_config.getString("classification")),
				Boolean.parseBoolean(_config.getString("hideLabel")),
				Boolean.parseBoolean(_config.getString("hideName")),
				Boolean.parseBoolean(_config.getString("audioFormatAlaw")),
				_config);
		_table = new JTable(_recordings);
		_table.setPreferredScrollableViewportSize(new Dimension(400,
				tableheight));
		TableColumn col = null;
		_table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		_table.getColumnModel().getColumn(0).setPreferredWidth(2);
		_table.getColumnModel().getColumn(1).setPreferredWidth(50);
		_table.getColumnModel().getColumn(2).setPreferredWidth(100);
		_table.getColumnModel().getColumn(3).setPreferredWidth(5);
		_table.getColumnModel().getColumn(4).setPreferredWidth(100);

		if (!Boolean.parseBoolean(_config.getString("hideLabel")))
			_table.getColumnModel().getColumn(5).setPreferredWidth(5);

		if (Boolean.parseBoolean(_config.getString("classification"))) {
			_table.getColumnModel().getColumn(6).setPreferredWidth(5);
		}
		JPanel pane = new JPanel();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		_nrOfRecordingsLab = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(_table);
		if (Boolean.parseBoolean(_config.getString("markAngerInTable"))) {
			TableCellRenderer customRenderer = new CustomTableCellRenderer();
			try {
				_table.setDefaultRenderer(Class.forName("java.lang.String"),
						customRenderer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		pane.add(scrollPane);
		pane.add(_nrOfRecordingsLab);
		wavPositionSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 0);
		wavPositionSlider.setMajorTickSpacing(200);
		wavPositionSlider.setMinorTickSpacing(20);
		wavPositionSlider.setEnabled(false);
		wavPositionSlider.setBackground(new Color(150, 150, 150));
		pane.add(wavPositionSlider);
		return pane;

	}

	public String getDataDescription() {
		return _dataDescription;
	}

	public void setDataDescription(String dataDescription) {
		_dataDescription = dataDescription;
	}

	/**
	 * Designs the pane for the buttons as a horizontal row. Sets keyboard
	 * shortcuts and tooltips.
	 * 
	 * @return pane The button pane.
	 */
	public Container makeRecorderButtonPane() {
		Color recorderButtonsBGColor = Color.BLACK;
		/**
		 * For each Feature: create a new button with the appropriate label.
		 * Generate a keyboard shortcut: <alt><mnemonic key>. Set a tool-tip
		 * text. Set an action listener.
		 */
		_record = initButton("record");
		_record.setIcon(createImageIcon("images/record.gif", ""));
		_record.setBackground(recorderButtonsBGColor);

		_play = initButton("play");
		_play.setIcon(createImageIcon("images/play.gif", ""));
		_play.setBackground(recorderButtonsBGColor);

		_stop = initButton("stop");
		_stop.setIcon(createImageIcon("images/stop.gif", ""));
		_stop.setBackground(recorderButtonsBGColor);

		_resume = initButton("resume");
		_resume.setIcon(createImageIcon("images/resume.gif", ""));
		_resume.setBackground(recorderButtonsBGColor);

		_delete = initButton("delete");
		_delete.setMnemonic(_config.getString("delete.short").charAt(0));
		_delete.setIcon(createImageIcon("images/delete.gif", ""));
		_delete.setBackground(recorderButtonsBGColor);
		_rename = initButton("rename");
		_rename.setMnemonic(_config.getString("rename.short").charAt(0));
		_rename.setIcon(createImageIcon("images/rename.gif", ""));
		_rename.setBackground(recorderButtonsBGColor);
		_refresh = initButton("refresh");
		_refresh.setIcon(createImageIcon("images/refresh.gif", ""));
		_refresh.setBackground(recorderButtonsBGColor);
		_exec = initButton("exec");
		_exec.setIcon(createImageIcon("images/exec.gif", ""));
		_exec.setBackground(recorderButtonsBGColor);

		_open = initButton("open");
		_open.setIcon(createImageIcon("images/open.gif", ""));
		_open.setBackground(recorderButtonsBGColor);

		_openDirectory = new JCheckBox();
		_openDirectory.setToolTipText(_config
				.getString("openDirectory.tooltip"));
		JLabel openDirectoryL = new JLabel(
				_config.getString("openDirectory.label"));
		openDirectoryL.setToolTipText(_config
				.getString("openDirectory.tooltip"));
		openDirectoryL.setForeground(Color.white);

		String[] srArray = StringUtil.stringToArray(_config
				.getString("sampleRate.values"));
		SpinnerListModel srModel = new SpinnerListModel(srArray);
		srModel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				changeSampleRate((String) _srSpinner.getValue());
			}
		});
		_srSpinner = new JSpinner(srModel);
		_srSpinner.setValue(String.valueOf(_samplerate));
		_srSpinner.setPreferredSize(new Dimension(60, 20));

		String[] afArray = StringUtil.stringToArray(_config
				.getString("audioFormat.values"));
		SpinnerListModel afModel = new SpinnerListModel(afArray);
		afModel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				changeAudioFormat((String) _afSpinner.getValue());
			}
		});
		_afSpinner = new JSpinner(afModel);
		_afSpinner.setPreferredSize(new Dimension(45, 20));
		_audioType = (String) _afSpinner.getValue();
		JPanel pane = new JPanel();
		pane.setBackground(new Color(80, 80, 80));
		pane.setBorder(BorderFactory.createMatteBorder(1, 1, 2, 2, Color.blue));
		if (!Boolean.parseBoolean(_config.getString("transcribeOnly"))) {
			pane.add(_record);
		}
		pane.add(_play);
		pane.add(_stop);
		if (!Boolean.parseBoolean(_config.getString("transcribeOnly"))) {
			pane.add(_delete);
			pane.add(_rename);
		}
		pane.add(_refresh);
		pane.add(_exec);
		pane.add(_open);
		pane.add(openDirectoryL);
		pane.add(_openDirectory);
		_openModel = new JCheckBox();
		if (Boolean.parseBoolean(_config.getString("classification"))) {
			_openModel.setToolTipText(_config.getString("openModel.tooltip"));
			JLabel openModelL = new JLabel(_config.getString("openModel.label"));
			openModelL.setToolTipText(_config.getString("openModel.tooltip"));
			openModelL.setForeground(Color.white);
			pane.add(openModelL);
			pane.add(_openModel);
		}
		JLabel srLab = new JLabel(_config.getString("sampleRate.label"));
		srLab.setForeground(Color.white);
		pane.add(srLab);
		pane.add(_srSpinner);
		JLabel afLab = new JLabel(_config.getString("audioFormat.label"));
		afLab.setForeground(Color.white);
		pane.add(afLab);
		pane.add(_afSpinner);
		_shuffleButton = initButton("shuffle");
		pane.add(_shuffleButton);

		pane.setBackground(new Color(150, 150, 150));
		pane.setBorder(BorderFactory.createMatteBorder(1, 1, 2, 2, Color.black));
		_record.setEnabled(true);
		_delete.setEnabled(false);
		_play.setEnabled(false);
		return pane;
	}

	public Container makeControlButtonPane() {
		JPanel pane = new JPanel();
		_judge = initButton("judge");
		_train = initButton("train");
		judgeAll = initButton("judgeAll");
		evaluate = initButton("evaluate");
		delAllPreds = initButton("deleteAllPredictions");
		if (Boolean.parseBoolean(_config.getString("classification"))) {
			pane.add(_judge);
			pane.add(_train);
			pane.add(judgeAll);
			pane.add(delAllPreds);
			pane.add(evaluate);
			_evalFiles = new JCheckBox();
			_evalFiles.setToolTipText(_config.getString("evalFiles.tooltip"));
			JLabel evalFilesLabel = new JLabel(
					_config.getString("evalFiles.label"));
			evalFilesLabel.setForeground(Color.white);
			evalFilesLabel.setToolTipText(_config
					.getString("evalFiles.tooltip"));
			pane.add(evalFilesLabel);
			pane.add(_evalFiles);
		}
		pane.setBackground(new Color(150, 150, 150));
		pane.setBorder(BorderFactory.createMatteBorder(1, 1, 2, 2, Color.black));
		fastPlayModeCheck = new JCheckBox();
		fastPlayModeCheck.setToolTipText(_config
				.getString("fastPlayMode.tooltip"));
		fastPlayModeLabel = new JLabel(_config.getString("fastPlayMode.label"));
		fastPlayModeLabel.setToolTipText(_config
				.getString("fastPlayMode.tooltip"));
		fastPlayModeLabel.setForeground(Color.white);
		numberToWordCheck = new JCheckBox();
		numberToWordCheck.setToolTipText(_config
				.getString("numberToWord.tooltip"));
		numberToWordLabel = new JLabel(_config.getString("numberToWord.label"));
		numberToWordLabel.setToolTipText(_config
				.getString("numberToWord.tooltip"));
		numberToWordLabel.setForeground(Color.white);
		if (Boolean.parseBoolean(_config.getString("classification"))) {
			extractFeaturesCheck = new JCheckBox();
			extractFeaturesCheck.setToolTipText(_config
					.getString("extractFeatures.tooltip"));
			extractFeaturesCheck.setSelected(true);
			extractFeaturesLabel = new JLabel(
					_config.getString("extractFeatures.label"));
			extractFeaturesLabel.setToolTipText(_config
					.getString("extractFeatures.tooltip"));
			extractFeaturesLabel.setForeground(Color.white);
			String[] modelArray = new String[] { Constants.TYPE_SMO,
					Constants.TYPE_NAIVE_BAYEYS, Constants.TYPE_J48 };
			SpinnerListModel classifierTypeModel = new SpinnerListModel(
					modelArray);
			classifierTypeModel.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					changeClassifierType((String) _classifierSpinner.getValue());
				}
			});
			_classifierSpinner = new JSpinner(classifierTypeModel);
			_classifierSpinner.setPreferredSize(new Dimension(90, 20));

		}
		// pane.add(transcribe);
		pane.add(fastPlayModeLabel);
		pane.add(fastPlayModeCheck);
		pane.add(numberToWordLabel);
		pane.add(numberToWordCheck);

		if (Boolean.parseBoolean(_config.getString("classification"))) {
			pane.add(extractFeaturesLabel);
			pane.add(extractFeaturesCheck);
			pane.add(_classifierSpinner);
		}
		numberToWordCheck.setSelected(numberToWord);

		return pane;
	}

	/** Returns an ImageIcon, or null if the path was invalid. */
	protected ImageIcon createImageIcon(String path, String description) {
		return new ImageIcon(path, description);
	}

	public Container makeRecognitionPane() {
		JPanel pane = new JPanel();
		pane.setBackground(new Color(40, 40, 40));
		pane.setBorder(BorderFactory.createMatteBorder(1, 1, 2, 2, Color.GREEN));
		recognizeAll = initButton("recognizeAll");
		recognize = initButton("recognize");
		toggleTranscript = initButton("toggleTranscript");
		copyRecognition = initButton("copyRecognition");
		_normalize = initButton("normalize");
		_normalizeAll = initButton("normalizeAll");
		_wer = initButton("wer");

		pane.add(recognizeAll);
		pane.add(recognize);
		pane.add(toggleTranscript);
		pane.add(copyRecognition);
		pane.add(_normalize);
		pane.add(_normalizeAll);
		pane.add(_wer);
		return pane;
	}

	public Container makeSynthesizerPane() {
		JPanel pane = new JPanel();
		pane.setBackground(new Color(110, 110, 110));
		pane.setBorder(BorderFactory.createMatteBorder(1, 1, 2, 2,
				Color.magenta));
		_synthesize = initButton("synthesize");
		_synthesizeAll = initButton("synthesizeAll");
		pane.add(_synthesizeAll);
		pane.add(_synthesize);
		_ttsSexFemale = new JCheckBox();
		_ttsSexFemale.setToolTipText(_config.getString("ttsSexFemale.tooltip"));
		JLabel label = new JLabel(_config.getString("ttsSexFemale.label"));
		label.setToolTipText(_config.getString("ttsSexFemale.tooltip"));
		label.setForeground(Color.white);
		pane.add(label);
		pane.add(_ttsSexFemale);
		String[] afArray = StringUtil.stringToArray(_config
				.getString("ttsLanguages.values"));
		SpinnerListModel model = new SpinnerListModel(afArray);
		_ttsLanguages = new JSpinner(model);
		_ttsLanguages.setPreferredSize(new Dimension(60, 20));
		_ttsLanguages.setToolTipText(_config.getString("ttsLanguages.tooltip"));
		JLabel label2 = new JLabel(_config.getString("ttsLanguages.label"));
		label2.setToolTipText(_config.getString("ttsLanguages.tooltip"));
		label2.setForeground(Color.white);
		pane.add(label2);
		pane.add(_ttsLanguages);
		return pane;
	}

	public Container makeTranscriptionPane() {
		// Add Components to a JPanel, using the default FlowLayout.
		JPanel pane = new JPanel();
		if (Boolean.parseBoolean(_config.getString("withTranscribe"))) {
			if (Boolean.parseBoolean(_config
					.getString("transcriptionFieldInline"))) {
				KeyListener transFieldListener = new KeyListener() {
					NumberToWord numberToWordTool = new NumberToWord();

					public void keyPressed(KeyEvent evt) {
					}

					public void keyReleased(KeyEvent evt) {
						if (evt.getKeyChar() == '\n') {
							label();
						}
						// check for shortcuts
						if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
							int caretPos = transField.getCaretPosition() - 1;
							for (Iterator<String> iterator = _charReplacements
									.keySet().iterator(); iterator.hasNext();) {
								String key = ((String) iterator.next());
								// System.err.println("key: >"+key+"<");
								String text = transField.getText();
								try {
									String lastChar = text.substring(caretPos,
											caretPos + 1);
									// System.err.println("lastChar: >"+lastChar+"<");
									if (lastChar.compareTo(key) == 0) {
										// System.err.println("yep");
										text = text.substring(0, caretPos)
												+ _charReplacements.get(key)
												+ text.substring(caretPos + 1,
														text.length());
										transField.setText(text);
										transField.setCaretPosition(caretPos
												+ _charReplacements.get(key)
														.length());
										break;
									}
								} catch (Exception e) {
									System.err
											.println("problem to set auto text: "
													+ text);
								}
							}
						}
						// check for class annotations
						if (evt.getKeyCode() == KeyEvent.VK_CONTROL) {
							int caretPos = transField.getCaretPosition() - 1;
							for (Iterator<String> iterator = _classes.keySet()
									.iterator(); iterator.hasNext();) {
								String key = ((String) iterator.next());
								// System.err.println("key: >"+key+"<");
								String text = transField.getText();
								try {
									String lastChar = text.substring(
											caretPos - 1, caretPos + 1);
									// System.err.println("lastChar: >"+lastChar+"<");
									if (lastChar.compareTo(key) == 0) {
										int start = StringUtil.lastPositionOf(
												text, " ", caretPos);
										String classExp = text.substring(start,
												caretPos - 1);
										String elemName = _classes.get(key);
										classExp = StringUtil.tagString(
												classExp, elemName);
										text = text.substring(0, start)
												+ classExp
												+ " "
												+ text.substring(caretPos + 1,
														text.length());
										transField.setText(text);
										transField.setCaretPosition(caretPos
												+ classExp.length() - 3);
										break;
									}
								} catch (Exception e) {
									System.err
											.println("problem to set auto text: "
													+ text);
								}
							}
						}
						// check for prefixes
						if (evt.getKeyCode() == KeyEvent.VK_F1) {
							int caretPos = transField.getCaretPosition() - 1;
							for (String key : _prefixes.keySet()) {
								// System.err.println("key: >"+key+"<");
								String text = transField.getText();
								try {
									String lastChar = text.substring(caretPos,
											caretPos + 1);
									// System.err.println("lastChar: >"+lastChar+"<");
									if (lastChar.compareTo(key) == 0) {
										int start = StringUtil.lastPositionOf(
												text, " ", caretPos);
										String word = text.substring(start,
												caretPos);
										String prefix = _prefixes.get(key);
										// System.err.println("prefix: >"+prefix+"<");
										word = prefix + word;
										text = text.substring(0, start) + word;
										transField.setText(text);
										transField.setCaretPosition(caretPos
												+ word.length() - 4);
										break;
									}
								} catch (Exception e) {
									e.printStackTrace();
									System.err
											.println("problem to set auto text: "
													+ text);
								}
							}
						}
						// check last word
						if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
							int caretPos = transField.getCaretPosition() - 1;
							String text = transField.getText();
							try {
								int start = StringUtil.lastPositionOf(text,
										" ", caretPos);
								String test = text.substring(start, caretPos);
								// search for abbreviations
								for (Iterator<String> iterator = _abbreviations
										.keySet().iterator(); iterator
										.hasNext();) {
									String key = ((String) iterator.next());
									if (key.compareTo(test) == 0) {
										String newtext = _abbreviations
												.get(key);
										text = text.substring(0, start)
												+ newtext
												+ " "
												+ text.substring(caretPos + 1,
														text.length());
										transField.setText(text);
										transField.setCaretPosition(caretPos
												+ newtext.length());
									}
								}
								// convert numbers automatically
								if (numberToWordCheck.isSelected()) {
									String newtext = numberToWordTool
											.filtNum(test);
									if (newtext.compareTo(test) != 0) {
										newtext = newtext.trim();
										// System.err.println("#### test = [" +
										// test
										// + "] newtext = [" + newtext + "]");
										text = text.substring(0, start)
												+ newtext
												+ " "
												+ text.substring(caretPos + 1,
														text.length());
										transField.setText(text);
										transField.setCaretPosition(caretPos
												+ newtext.length());
									}
								}
								// check spelling
								if (_checkSpelling.isSelected()) {
									boolean hasPunctuationMark = false;
									if (test.endsWith("?")
											|| test.endsWith(",")
											|| test.endsWith(".")
											|| test.endsWith("!")) {
										hasPunctuationMark = true;
									}
									String answer = interfaceServer.getAnswer(
											"check;" + test).trim();
									if (answer.compareTo("ok") != 0) {
										Vector<String> suggestions = StringUtil
												.stringToVector(answer);
										Object[] options = new Object[suggestions
												.size() + 2];
										int i = 0;
										for (String s : suggestions) {
											options[i++] = s;
										}
										options[i++] = Constants.CMD_ADD;
										options[i++] = Constants.CMD_IGNORE;
										int n = JOptionPane
												.showOptionDialog(
														null,
														"unknown word",
														"Spellchecker",
														JOptionPane.YES_NO_CANCEL_OPTION,
														JOptionPane.QUESTION_MESSAGE,
														null, options,
														options[0]);
										String choice = (String) options[n];
										if (choice.compareTo(Constants.CMD_ADD) == 0) {
											// add the word to dictionary
											interfaceServer
													.sendMessage("addWord;"
															+ test);
										} else if (choice
												.compareTo(Constants.CMD_IGNORE) != 0) {
											text = text.substring(0, start)
													+ choice
													+ " "
													+ text.substring(
															caretPos + 1,
															text.length());
											transField.setText(text);
											transField
													.setCaretPosition(caretPos
															+ choice.length());
										}

									}
								}
							} catch (Exception e) {
								System.err.println("problem to set auto text: "
										+ text);
							}
						}
					}

					public void keyTyped(KeyEvent evt) {
					}
				};
				transField = new JTextArea();
				transField.addKeyListener(transFieldListener);
				JScrollPane transFieldScroll = new JScrollPane(transField);
				transField.setLineWrap(true);
				transFieldScroll.setPreferredSize(new Dimension(
						Integer.parseInt(_config
								.getString("transcriptionField.width")),
						Integer.parseInt(_config
								.getString("transcriptionField.height"))));
				pane.add(transFieldScroll);
			} else {
				_transcribe = new JButton(_config.getString("transcribe.label"));
				_transcribe.setActionCommand("transcribe");
				_transcribe.setToolTipText(_config
						.getString("transcribe.tooltip"));
				_transcribe.addActionListener(this);
				_transcribe.setMnemonic(_config.getString("transcribe.short")
						.charAt(0));
				pane.add(_transcribe);
			}
		}
		_export = initButton("export");
		_import = initButton("import");
		_checkSpelling = new JCheckBox();
		_checkSpelling.setToolTipText(_config
				.getString("checkSpelling.tooltip"));
		JLabel label = new JLabel(_config.getString("checkSpelling.label"));
		label.setForeground(Color.white);
		label.setToolTipText(_config.getString("checkSpelling.tooltip"));
		JPanel butPane = new JPanel();
		butPane.setBackground(new Color(25, 25, 25));
		butPane.add(_import);
		butPane.add(_export);
		JPanel checkPane = new JPanel();
		checkPane.setBackground(new Color(25, 25, 25));
		checkPane.setLayout(new BoxLayout(checkPane, BoxLayout.Y_AXIS));
		checkPane.add(label);
		checkPane.add(_checkSpelling);
		pane.add(checkPane);
		butPane.setLayout(new BoxLayout(butPane, BoxLayout.Y_AXIS));
		pane.add(butPane);
		pane.setBackground(new Color(25, 25, 25));
		pane.setBorder(BorderFactory.createMatteBorder(1, 1, 2, 2, Color.black));
		return pane;
	}

	private class CustomActionLister implements ActionListener {
		Vector<String> _names;
		ValueReturnFrameButtonPanel _vrfbp;

		public CustomActionLister(Vector<String> names,
				ValueReturnFrameButtonPanel vrfbp) {
			_names = names;
			_vrfbp = vrfbp;
		}

		public void actionPerformed(ActionEvent e) {
			for (String s : _names) {
				if (e.getActionCommand().equals(s)) {
					_vrfbp.returnValue(s);
				}
			}
		}
	}

	private class ValueReturnFrameButtonPanel extends JFrame {
		ActionListener _ac;

		public String returnValue(String val) {
			this.dispose();
			return val;
		}

		public void setActionlistener(ActionListener ac, Vector<String> names) {
			_ac = ac;
			JPanel pane = new JPanel();
			for (String s : names) {
				JButton b = SwingUtil.makeButton(s, _ac);
				pane.add(b);
			}
			setContentPane(pane);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			pack();
			setVisible(true);
		}

	}

	public Container makeJudgeButtonPane() {
		JPanel pane = new JPanel();
		String[] buttonNames = StringUtil.stringToArray(_config
				.getString("buttons"));
		for (int i = 0; i < buttonNames.length; i++) {
			JButton act = initButton(buttonNames[i]);
			pane.add(act);
		}
		removeLastLabel = initButton("removeLastLabel");
		removePred = initButton("removePred");
		removeUntagged = initButton("removeUntagged");
		statistics = initButton("statistics");
		pane.add(removeLastLabel);
		pane.add(removeUntagged);
		if (Boolean.parseBoolean(_config.getString("classification"))) {
			pane.add(removePred);
			pane.add(statistics);
		}
		fastModeCheck = new JCheckBox();
		fastModeCheck.setToolTipText(_config
				.getString("fastTranscritpionMode.tooltip"));
		fastModeLabel = new JLabel(
				_config.getString("fastTranscritpionMode.label"));
		fastModeLabel.setToolTipText(_config
				.getString("fastTranscritpionMode.tooltip"));
		fastModeLabel.setForeground(Color.white);
		pane.add(fastModeLabel);
		pane.add(fastModeCheck);

		pane.setBackground(new Color(20, 20, 20));
		pane.setBorder(BorderFactory.createMatteBorder(1, 1, 2, 2, Color.black));
		return pane;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#start()
	 */
	public void start() {
		KeyValues kv = new KeyValues(_config.getString("replacements"), ",",
				":");
		_charReplacements = kv.getHashMap();
		kv = new KeyValues(_config.getString("classes"), ",", ":");
		_classes = kv.getHashMap();
		kv = new KeyValues(_config.getString("prefixes"), ",", ":");
		_prefixes = kv.getHashMap();
		kv = new KeyValues(_config.getString("abbreviations"), ",", ":");
		_abbreviations = kv.getHashMap();

		// setting up the table's listener (for row-selection)

		// _table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		ListSelectionModel rowSM = _table.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				// Ignore extra messages.
				if (e.getValueIsAdjusting())
					return;
				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty()) {
				} else {
					selectedRow = lsm.getMinSelectionIndex();
					_play.setEnabled(true);
					if (Boolean.parseBoolean(_config
							.getString("classification"))) {
						_judge.setEnabled(true);
						_train.setEnabled(true);
					}
					_delete.setEnabled(true);
					wavPositionSlider.setModel(new DefaultBoundedRangeModel());
					if (Boolean.parseBoolean(_config
							.getString("transcriptionFieldInline"))) {
						if (transField.isVisible()) {
							transField.setText(_recordings
									.getRecordingAtRow(selectedRow).words);
						}
					}
				}
			}
		});
		// looking for recordings at server
		refresh();
	}

	/**
	 * , Listens for Button-Click and determines what to do.
	 * 
	 * @param the
	 *            actionEvent fired by the button.
	 */
	public void actionPerformed(ActionEvent e) {
		String[] buttonNames = StringUtil.stringToArray(_config
				.getString("buttons"));
		for (int i = 0; i < buttonNames.length; i++) {
			if (e.getActionCommand().equals(buttonNames[i])) {
				setEmotion(_config.getString(buttonNames[i] + ".value"));
			}
		}

		if (e.getActionCommand().equals("record")) {
			record();
		} else if (e.getActionCommand().equals("play")) {
			_stop.setIcon(createImageIcon("images/stop.gif", ""));
			play();
		} else if (e.getActionCommand().equals("stop")) {
			if (!Boolean.parseBoolean(_config.getString("audioFormatAlaw"))) {
				if (playThread != null || recThread != null) {
					_stop.setIcon(createImageIcon("images/resume.gif", ""));
					_stop.setToolTipText(_config.getString("resume.tooltip"));
					stopRecordingPlayback();
				} else {
					_stop.setIcon(createImageIcon("images/stop.gif", ""));
					_stop.setToolTipText(_config.getString("stop.tooltip"));
					resume();
				}

			} else {
				stopRecordingPlayback();
			}
		} else if (e.getActionCommand().equals("resume")) {
			resume();
		} else if (e.getActionCommand().equals("open")) {
			open();
		} else if (e.getActionCommand().equals("rename")) {
			rename();
		} else if (e.getActionCommand().equals("exec")) {
			exec();
		} else if (e.getActionCommand().equals("export")) {
			export();
		} else if (e.getActionCommand().equals("import")) {
			importFile();
		} else if (e.getActionCommand().equals("refresh")) {
			refresh();
		} else if (e.getActionCommand().equals("judge")) {
			_table.requestFocus();
			judge();
		} else if (e.getActionCommand().equals("deleteAllPredictions")) {
			_table.requestFocus();
			deleteAllPredictions();
		} else if (e.getActionCommand().equals("train")) {
			_table.requestFocus();
			train();
		} else if (e.getActionCommand().equals("shuffle")) {
			shuffle();
		} else if (e.getActionCommand().equals("synthesize")) {
			_table.requestFocus();
			synthesize();
		} else if (e.getActionCommand().equals("synthesizeAll")) {
			_table.requestFocus();
			synthesizeAll();
		} else if (e.getActionCommand().equals("wer")) {
			_table.requestFocus();
			wer();
		} else if (e.getActionCommand().equals("normalize")) {
			_table.requestFocus();
			normalize();
		} else if (e.getActionCommand().equals("normalizeAll")) {
			_table.requestFocus();
			normalizeAll();
		} else if (e.getActionCommand().equals("judgeAll")) {
			_table.requestFocus();
			judgeAll();
		} else if (e.getActionCommand().equals("recognizeAll")) {
			_table.requestFocus();
			recognizeAll();
		} else if (e.getActionCommand().equals("evaluate")) {
			_table.requestFocus();
			evaluate();
		} else if (e.getActionCommand().equals("delete")) {
			deleteFile();
		} else if (e.getActionCommand().equals("na")) {
			setEmotion("0");
		} else if (e.getActionCommand().equals("removeLastLabel")) {
			removeLastLabel();
		} else if (e.getActionCommand().equals("removeUntagged")) {
			removeUntagged();
		} else if (e.getActionCommand().equals("removePred")) {
			removePred();
		} else if (e.getActionCommand().equals("statistics")) {
			analyse();
		} else if (e.getActionCommand().equals("transcribe")) {
			label();
		} else if (e.getActionCommand().equals("recognize")) {
			recognize();
		} else if (e.getActionCommand().equals("toggleTranscript")) {
			toggleTranscript();
		} else if (e.getActionCommand().equals("copyRecognition")) {
			copyRecognition();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#setEmoResult(java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String)
	 */
	public void setEmoResult(String prediction, String configString) {
		try {
			emoConfigLabel.setText(configString);
			ClassificationResult cr = new ClassificationResult(prediction);
			String neutralS = "na";
			ClassResult neutralR = cr.getResultForName("N");
			if (neutralR != null) {
				neutralS = String.valueOf(neutralR.getProbability());
			}
			String angerS = "na";
			ClassResult angerR = cr.getResultForName("A");
			if (angerR != null) {
				angerS = String.valueOf(angerR.getProbability());
			}
			emoResultNeutralLab.setText(neutralS);
			double dNeutral = Double.parseDouble(neutralS);
			int iNeutral = (int) (dNeutral * 100.0);
			emoResultNeutralPG.setValue(iNeutral);
			emoResultHotAngerLab.setText(angerS);
			double dHotAnger = Double.parseDouble(angerS);
			int iHotAnger = (int) (dHotAnger * 100.0);
			emoResultHotAngerPG.setValue(iHotAnger);
			if (dNeutral == 1) {
				neutralLab.setForeground(Color.red);
				haLab.setForeground(Color.black);
			} else if (dHotAnger == 1) {
				haLab.setForeground(Color.red);
				neutralLab.setForeground(Color.black);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		_table.requestFocus();
		setFocusTable();
	}

	public void refresh() {
		updateThread = new UpdateFileListThread(servername,
				Integer.parseInt(_config.getString("port")), this,
				Boolean.parseBoolean(_config.getString("refreshFromHardDisc")),
				_config.getString("charEnc"), Boolean.parseBoolean(_config
						.getString("sortRecordings")),
				Boolean.parseBoolean(_config.getString("sortOrderAscending")));
		updateThread.start();
	}

	private void shuffle() {
		_recordings.shuffleData();
		repaintView();
	}

	public void open() {
		JFileChooser chooser = new JFileChooser();
		if (_fileDir.length() == 0) {
			_fileDir = FileUtil.getCurrentDirPath();
		}
		chooser.setCurrentDirectory(new java.io.File(_fileDir));
		if (_openDirectory.isSelected()) {
			chooser.setDialogTitle(_config.getString("diropen.question"));
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				_fileDir = file.getParent();
				SendMessageThread sendMessageThread = new SendMessageThread(
						"openDir;" + file.getAbsolutePath(), servername,
						Integer.parseInt(_config.getString("port")), this);
				sendMessageThread.start();
			} else {
				return;
			}
		} else if (_openModel.isSelected()) {
			chooser.setDialogTitle(_config.getString("open.question"));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				_fileDir = file.getParent();
				SendMessageThread sendMessageThread = new SendMessageThread(
						"openModel;" + file.getAbsolutePath(), servername,
						Integer.parseInt(_config.getString("port" + "")), this);
				sendMessageThread.start();
			} else {
				return;
			}
		} else {
			chooser.setDialogTitle(_config.getString("open.question"));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				_fileDir = file.getParent();
				SendMessageThread sendMessageThread = new SendMessageThread(
						"open;" + file.getAbsolutePath(), servername,
						Integer.parseInt(_config.getString("port" + "")), this);
				sendMessageThread.start();
			} else {
				return;
			}
		}
	}

	public void export() {
		JFileChooser chooser = new JFileChooser();
		if (_fileDir.length() == 0) {
			_fileDir = FileUtil.getCurrentDirPath();
		}
		chooser.setCurrentDirectory(new java.io.File(_fileDir));
		chooser.setDialogTitle(_config.getString("export.question"));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			_fileDir = file.getParent();
			SendMessageThread sendMessageThread = new SendMessageThread(
					"exportTranscriptsToFile;" + file.getAbsolutePath(),
					servername,
					Integer.parseInt(_config.getString("port" + "")), this);
			sendMessageThread.start();
		} else {
			return;
		}
	}

	public void importFile() {
		JFileChooser chooser = new JFileChooser();
		if (_fileDir.length() == 0) {
			_fileDir = FileUtil.getCurrentDirPath();
		}
		chooser.setCurrentDirectory(new java.io.File(_fileDir));

		chooser.setDialogTitle(_config.getString("import.question"));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			_fileDir = file.getParent();
			SendMessageThread sendMessageThread = new SendMessageThread(
					"importTranscriptsFromFile;" + file.getAbsolutePath(),
					servername,
					Integer.parseInt(_config.getString("port" + "")), this);
			sendMessageThread.start();
		} else {
			return;
		}
	}

	private void rename() {
		String fileName = JOptionPane.showInputDialog(_config
				.getString("rename.question"));
		if (fileName != null) {
			fileName = FileUtil.enforceExtension(fileName, _audioType);
			Recording recording = _recordings.getRecordingAtRow(selectedRow);
			SendMessageThread sendMessageThread = new SendMessageThread(
					"rename;" + recording.path + ";" + fileName, servername,
					Integer.parseInt(_config.getString("port")), this);
			sendMessageThread.start();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#record()
	 */
	public void record() {
		// if a recording was started, stop it.
		if (recThread != null) {
			System.out.println("Stoppe Aufnahme");
			recThread.stopMe();
			recThread = null;
			_record.setIcon(createImageIcon("images/record.gif", ""));

			refresh();
		} else {
			String fileName = "_neutralPred.raw";
			if (Boolean
					.parseBoolean(_config.getString("recordingAudioAskName"))) {
				// Prompt the user for a fileName.
				fileName = JOptionPane.showInputDialog(_config
						.getString("record.question"));
				if (fileName != null) {
					// Wenn der User keinen Dateinamen angibt, heisst die Datei
					// lokales Datum - lokale Uhrzeit
					if (fileName.length() == 0) {
						fileName = FileUtil.enforceExtension(
								DateTimeUtil.getDateSortableName(), _audioType);
					} else {
						fileName = FileUtil.enforceExtension(fileName,
								_audioType);
					}
				}
			} else {
				fileName = FileUtil.enforceExtension(
						DateTimeUtil.getDateSortableName(), _audioType);
			}
			_stop.setEnabled(true);
			_record.setIcon(createImageIcon("images/stop.gif", ""));
			recThread = new RecordThread(_formatPCM, servername,
					Integer.parseInt(_config.getString("port")), fileName);
			recThread.start();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#undo()
	 */
	public void undo() {
		undoThread = new UndoThread(servername, Integer.parseInt(_config
				.getString("port")));
		undoThread.start();
		refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#stopRecordingPlayback()
	 */
	public void stopRecordingPlayback() {
		if (recThread != null) {
			System.out.println("Stoppe Aufnahme");
			recThread.stopMe();
			recThread = null;
			refresh();
		} else if (playThread != null) {
			System.out.println("Stoppe Wiedergabe");
			lastBytesPlayed = playThread.stopMe();
			finishedPlayback();
		} else {
			lastBytesPlayed = 0;
			System.out.println("No Process started!");
		}
		// stop.setEnabled(false);
		_record.setEnabled(true);
		_table.requestFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#play()
	 */
	public void play() {
		if (Boolean.parseBoolean(_config.getString("useExternalPlayer"))) {
			exec();
		} else {
			// check if multiple playback should be suppresed
			if (Boolean.parseBoolean(_config.getString("noMultiplePlayBack"))) {
				if (playThread != null) {
					// there is someone playing
					return;

				} else {
					// we can play, but don't enable for others
					_play.setEnabled(false);
				}
			}
			// check if slider was moved and resume at position
			if (wavPositionSlider.getModel() instanceof CustomSliderRangeModel) {
				CustomSliderRangeModel csrm = (CustomSliderRangeModel) wavPositionSlider
						.getModel();
				if (csrm.getSliderPosition() > 0) {
					resume(csrm.getSliderPosition());
					return;
				}
			}

			// reset the resume function
			lastBytesPlayed = 0;
			wavPositionSlider.setValue(0);
			_stop.setEnabled(true);
			wavPositionSlider.setEnabled(true);
			Recording recording = _recordings.getRecordingAtRow(selectedRow);
			AudioFormat audioFormat = null;
			if (Boolean.parseBoolean(_config.getString("audioFormatAlaw"))) {
				audioFormat = AudioUtil.FORMAT_ALAW;
			} else {
				audioFormat = _formatPCM;
			}
			playThread = new PlayThread(audioFormat, servername,
					Integer.parseInt(_config.getString("port")), recording,
					this, 0);
			prepareSlider();
			playThread.start();
			_play.setEnabled(false);
			_record.setEnabled(false);
			if (Boolean.parseBoolean(_config.getString("classification"))) {
				_judge.setEnabled(false);
				_train.setEnabled(false);
			}
			_delete.setEnabled(false);
			_table.requestFocus();
		}
		if (fastPlayModeCheck.isSelected()) {
			_table.changeSelection(_table.getSelectedRow() + 1, 0, false, false);
			_table.requestFocus();
		}
		if (fastModeCheck.isSelected()
				&& !Boolean
						.parseBoolean(_config.getString("fastModeSetsAnger"))) {
			transField.requestFocus();
			if (transField.getText().length() == 0) {
				Recording recording = _recordings
						.getRecordingAtRow(selectedRow);
				recording = _recordings.getRecordingAtRow(selectedRow);
				transField.setText(recording.words);
				transField.setSelectionStart(0);
				transField.setSelectionEnd(recording.words.length());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#resume()
	 */
	public void resume() {
		if (playThread == null) {
			_stop.setEnabled(true);
			Recording recording = _recordings.getRecordingAtRow(selectedRow);
			AudioFormat audioFormat = null;
			if (Boolean.parseBoolean(_config.getString("audioFormatAlaw"))) {
				audioFormat = AudioUtil.FORMAT_ALAW;
			} else {
				audioFormat = _formatPCM;
			}
			// int resumebytes = lastBytesPlayed > Constants.PREROLL ?
			// lastBytesPlayed - Constants.PREROLL
			// : lastBytesPlayed;
			playThread = new PlayThread(audioFormat, servername,
					Integer.parseInt(_config.getString("port")), recording,
					this, lastBytesPlayed);
			prepareSlider();
			playThread.start();
			_play.setEnabled(false);
			_record.setEnabled(false);
			_judge.setEnabled(false);
			_train.setEnabled(false);
			_delete.setEnabled(false);
			_table.requestFocus();
		}
	}

	private void prepareSlider() {
		ChangeListener[] cl = wavPositionSlider.getChangeListeners();
		for (int i = cl.length; --i >= 0;) {
			wavPositionSlider.removeChangeListener(cl[i]);
		}
		CustomSliderRangeModel sliderModel = new CustomSliderRangeModel(
				playThread);
		wavPositionSlider.setModel(sliderModel);
		wavPositionSlider.addChangeListener(sliderModel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.IRecorder#resume(int)
	 */
	@Override
	public void resume(int bytePos) {
		lastBytesPlayed = bytePos;
		resume();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#finishedPlayback()
	 */
	public void finishedPlayback() {
		playThread = null;
		// wavPositionSlider.setValueIsAdjusting( true );
		// wavPositionSlider.setValue( 0 );
		// wavPositionSlider.setValueIsAdjusting( false );
		_play.setEnabled(true);
		_record.setEnabled(true);
		if (Boolean.parseBoolean(_config.getString("classification"))) {
			_judge.setEnabled(true);
			_train.setEnabled(true);
		}
		_delete.setEnabled(true);
		// stop.setEnabled(false);
		if (fastModeCheck.isSelected()
				&& !Boolean
						.parseBoolean(_config.getString("fastModeSetsAnger"))) {
			if (Boolean.parseBoolean(_config
					.getString("transcriptionFieldInline"))) {
				transField.requestFocus();
				if (transField.getText().length() == 0) {
					Recording recording = _recordings
							.getRecordingAtRow(selectedRow);
					transField.setText(recording.words);
					transField.setSelectionStart(0);
					transField.setSelectionEnd(recording.words.length());
				}
			} else {
				label();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#judge(boolean)
	 */
	public void judge() {
		Recording recording = _recordings.getRecordingAtRow(selectedRow);
		judgeThread = new JudgeThread(recording, servername,
				Integer.parseInt(_config.getString("port")), this);
		judgeThread.start();
		_table.requestFocus();
		lastSelectedRow = _table.getSelectedRow();
	}

	public void exec() {
		Recording recording = _recordings.getRecordingAtRow(selectedRow);
		SendMessageThread sendMessageThread = new SendMessageThread("exec;"
				+ recording.path, servername, Integer.parseInt(_config
				.getString("port")), this);
		sendMessageThread.start();
	}

	public void setFocusTable() {
		if (_recordings.rowNum > 0) {
			int recIndex = lastSelectedRow;
			_table.changeSelection(recIndex, 0, false, false);
			_table.scrollRectToVisible(_table.getCellRect(recIndex, 0, true));
			_recordings.fireTableDataChanged();
		}
		_table.requestFocus();
		lastBytesPlayed = 0;
	}

	public void judgeAll() {
		SendMessageThread sendMessageThread = new SendMessageThread("judgeAll",
				servername, Integer.parseInt(_config.getString("port")), this);
		sendMessageThread.start();
	}

	public void recognizeAll() {
		SendMessageThread sendMessageThread = new SendMessageThread(
				"recognizeAll", servername, Integer.parseInt(_config
						.getString("port")), this);
		sendMessageThread.start();
	}

	public void changeAudioFormat(String format) {
		SendMessageThread sendMessageThread = new SendMessageThread(
				"audioFormat;" + format, servername, Integer.parseInt(_config
						.getString("port")), this);
		sendMessageThread.start();
		_audioType = format;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#judgeAll()
	 */
	public void deleteAllPredictions() {
		SendMessageThread sendMessageThread = new SendMessageThread(
				"removeAllPredictions", servername, Integer.parseInt(_config
						.getString("port")), this);
		sendMessageThread.start();
	}

	public void changeClassifierType(String type) {
		SendMessageThread sendMessageThread = new SendMessageThread(
				"classifierType;" + type, servername, Integer.parseInt(_config
						.getString("port")), this);
		sendMessageThread.start();
	}

	public int getSampleRate() {
		return _samplerate;
	}

	public void changeSampleRate(String rate) {
		_formatPCM = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				Integer.parseInt(rate), 16, 1, 2, Integer.parseInt(rate), false);
		try {
			_samplerate = Integer.parseInt(rate);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#judgeAll()
	 */
	public void train() {
		SendMessageThread sendMessageThread = new SendMessageThread("train "
				+ extractFeaturesCheck.isSelected(), servername,
				Integer.parseInt(_config.getString("port")), this);
		sendMessageThread.start();
	}

	public void synthesize() {
		if (JOptionPane.showConfirmDialog(this,
				_config.getString("synthesize.question"), "sure",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			String recs = "";
			int[] rowsI = _table.getSelectedRows();
			for (int i = 0; i < rowsI.length; i++) {
				Recording recording = _recordings.getRecordingAtRow(rowsI[i]);
				recs += recording.path + ";";
			}
			String ttsSexFemale = String.valueOf(_ttsSexFemale.isSelected());
			String ttsLanguage = (String) _ttsLanguages.getValue();
			SendMessageThread sendMessageThread = new SendMessageThread(
					"synthesize;" + ttsSexFemale + ";" + ttsLanguage + ";"
							+ recs, servername, Integer.parseInt(_config
							.getString("port")), this);
			sendMessageThread.start();
		}
	}

	public void synthesizeAll() {
		if (JOptionPane.showConfirmDialog(this,
				_config.getString("synthesizeAll.question"), "sure",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			String ttsSexFemale = String.valueOf(_ttsSexFemale.isSelected());
			String ttsLanguage = (String) _ttsLanguages.getValue();
			SendMessageThread sendMessageThread = new SendMessageThread(
					"synthesizeAll;" + ttsSexFemale + ";" + ttsLanguage,
					servername, Integer.parseInt(_config.getString("port")),
					this);
			sendMessageThread.start();
		}
	}

	public void wer() {
		GetAnswerThread thread = new GetAnswerThread("wer", servername,
				Integer.parseInt(_config.getString("port")), this);
		thread.start();
	}

	public void normalize() {
		Recording recording = _recordings.getRecordingAtRow(selectedRow);
		SendMessageThread sendMessageThread = new SendMessageThread(
				"normalize;" + recording.path, servername,
				Integer.parseInt(_config.getString("port")), this);
		sendMessageThread.start();
	}

	public void normalizeAll() {
		SendMessageThread sendMessageThread = new SendMessageThread(
				"normalizeAll", servername, Integer.parseInt(_config
						.getString("port")), this);
		sendMessageThread.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#resetRatings()
	 */
	public void resetRatings() {
		ResetThread resetThread = new ResetThread(false, servername,
				Integer.parseInt(_config.getString("port")), this);
		resetThread.start();
	}

	public void evaluate() {
		String mode = "model";
		if (_evalFiles.isSelected()) {
			mode = "files";
		}
		EvaluateThread evaluateThread = new EvaluateThread(servername,
				Integer.parseInt(_config.getString("port")), this, mode,
				_config.getString("charEnc"));
		evaluateThread.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#setEmotion(java.lang.String)
	 */
	public void setEmotion(String emo) {
		if (Boolean.parseBoolean(_config.getString("hideLabel"))) {
			setMessage("setting Label: " + emo);
		}
		if (playThread != null) {
			System.out.println("Stoppe Wiedergabe");
			playThread.stopMe();
			playThread = null;
			_play.setEnabled(true);
			_record.setEnabled(true);
			_judge.setEnabled(true);
			_train.setEnabled(true);
			_delete.setEnabled(true);
			_stop.setEnabled(false);
		}
		Recording recording = _recordings.getRecordingAtRow(selectedRow);
		recording.addAngerLab(Double.parseDouble(emo));
		int recIndex = selectedRow;
		SetFileEmotionThread sfe = new SetFileEmotionThread(servername,
				Integer.parseInt(_config.getString("port")), recording, emo);
		sfe.start();
		// updateThread = new UpdateFileListThread(servername,
		// Integer.parseInt(_config.getString("port")), this);
		// updateThread.start();
		_delete.setEnabled(false);
		_play.setEnabled(false);
		_judge.setEnabled(false);
		_train.setEnabled(false);
		_recordings.fireTableDataChanged();
		_table.changeSelection(recIndex, 0, false, false);
		_table.scrollRectToVisible(_table.getCellRect(recIndex, 0, true));
		_table.requestFocus();
		if (fastModeCheck.isSelected() && recIndex < _recordings.rowNum - 1
				&& Boolean.parseBoolean(_config.getString("fastModeSetsAnger"))) {
			_table.changeSelection(recIndex + 1, 0, false, false);
			_table.scrollRectToVisible(_table.getCellRect(recIndex, 0, true));
			_table.requestFocus();
			play();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#removeLastLabel()
	 */
	public void removeLastLabel() {
		Recording recording = _recordings.getRecordingAtRow(selectedRow);
		recording.removeLastLabel();
		int recIndex = selectedRow;
		SendMessageThread sendMessageThread = new SendMessageThread(
				"removeLastLabel;" + recording.path, servername,
				Integer.parseInt(_config.getString("port")), this);
		sendMessageThread.start();
		_recordings.fireTableDataChanged();
		_table.changeSelection(recIndex, 0, false, false);
		_table.scrollRectToVisible(_table.getCellRect(recIndex, 0, true));
		_table.requestFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#removeLastLabel()
	 */
	public void removeUntagged() {
		if (JOptionPane.showConfirmDialog(this,
				_config.getString("removeUntagged.question"), "sure",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			SendMessageThread sendMessageThread = new SendMessageThread(
					"removeUntagged;", servername, Integer.parseInt(_config
							.getString("port")), this);
			sendMessageThread.start();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#removeLastLabel()
	 */
	public void removePred() {
		Recording recording = _recordings.getRecordingAtRow(selectedRow);
		recording.removePrediction();
		int recIndex = selectedRow;
		SendMessageThread sendMessageThread = new SendMessageThread(
				"removePred;" + recording.path, servername,
				Integer.parseInt(_config.getString("port")), this);
		sendMessageThread.start();
		_recordings.fireTableDataChanged();
		_table.changeSelection(recIndex, 0, false, false);
		_table.scrollRectToVisible(_table.getCellRect(recIndex, 0, true));
		_table.requestFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#setMessage(java.lang.String)
	 */
	public void setMessage(String msg) {
		JOptionPane.showMessageDialog(this, new JTextArea(msg));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#transcribe()
	 */
	public void transcribe() {
		Recording recording = _recordings.getRecordingAtRow(selectedRow);
		if (recording.words != null) {
			transField.setText(recording.words);
		}
		transField.requestFocus();
	}

	private void toggleTranscript() {
		if (showTranscript) {
			showTranscript = false;
		} else {
			showTranscript = true;
		}
		int recIndex = selectedRow;
		_recordings.fireTableDataChanged();
		_table.changeSelection(recIndex, 0, false, false);
		_table.scrollRectToVisible(_table.getCellRect(recIndex, 0, true));
	}

	private void copyRecognition() {
		Recording recording = _recordings.getRecordingAtRow(selectedRow);
		int recIndex = selectedRow;
		recording.words = recording.recognition;
		sendTranscriptToServer(recording, recIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#label()
	 */
	public void label() {
		Recording recording = _recordings.getRecordingAtRow(selectedRow);
		int recIndex = selectedRow;
		String transcript = "";
		if (Boolean.parseBoolean(_config.getString("transcriptionFieldInline"))) {
			transcript = transField.getText();
			transField.setText("");
		} else {
			// Prompt the user for a fileName.
			transcript = JOptionPane.showInputDialog("transcription:",
					recording.words);
		}
		if (transcript == null) {
			return;
		}
		transcript = transcript.replaceAll("\n", "");
		recording.setWords(transcript);
		sendTranscriptToServer(recording, recIndex);
		if (fastModeCheck.isSelected()
				&& recIndex < _recordings.rowNum - 1
				&& !Boolean
						.parseBoolean(_config.getString("fastModeSetsAnger"))) {
			_table.changeSelection(recIndex + 1, 0, false, false);
			_table.scrollRectToVisible(_table.getCellRect(recIndex, 0, true));
			_table.requestFocus();
			play();
		}
	}

	private void sendTranscriptToServer(Recording recording, int recIndex) {
		SendTranscriptionThread sfl = new SendTranscriptionThread(servername,
				Integer.parseInt(_config.getString("port")), recording,
				_config.getString("charEnc"));
		sfl.start();
		_delete.setEnabled(false);
		_play.setEnabled(false);
		if (Boolean.parseBoolean(_config.getString("classification"))) {
			_judge.setEnabled(false);
			_train.setEnabled(false);
		}
		_recordings.fireTableDataChanged();
		_table.changeSelection(recIndex, 0, false, false);
		_table.scrollRectToVisible(_table.getCellRect(recIndex, 0, true));
		_table.requestFocus();

	}

	public void recognize() {
		Recording recording = _recordings.getRecordingAtRow(selectedRow);
		RecognitionThread recThread = new RecognitionThread(servername,
				Integer.parseInt(_config.getString("port")), this, recording);
		recThread.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#deleteFile()
	 */
	public void deleteFile() {
		if (JOptionPane.showConfirmDialog(this,
				_config.getString("delete.question"), "sure",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

			int[] rowsI = _table.getSelectedRows();
			String recs = "";
			for (int i = 0; i < rowsI.length; i++) {
				Recording recording = _recordings.getRecordingAtRow(rowsI[i]);
				recs += recording.path + ";";
			}
			SendMessageThread sendMessageThread = new SendMessageThread(
					"delete;" + recs, servername, Integer.parseInt(_config
							.getString("port")), this);
			sendMessageThread.start();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see emo.recorder.gui.IRecorder#getRecordings()
	 */
	public RecordingTable getRecordings() {
		return _recordings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeemo.recorder.gui.IRecorder#setRecordings(emo.recorder.gui.
	 * RecordingsTableModel)
	 */
	public void setRecordings(RecordingTable recordings) {
		if (recordings instanceof RecordingsTableModel) {
			this._recordings = (RecordingsTableModel) recordings;

		}
	}

	private JButton initButton(String name) {
		JButton tempButton = new JButton(_config.getString(name + ".label"));
		tempButton.setMnemonic(_config.getString(name + ".short").charAt(0));
		tempButton.setActionCommand(name);
		tempButton.setToolTipText(_config.getString(name + ".tooltip"));
		tempButton.addActionListener(this);
		return tempButton;
	}

	/**
	 * Enables the programm to run as application.
	 * 
	 * @param commandline
	 *            arguments
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame("Application version: Recorder");
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		KeyValues config = null;
		if (args.length > 0) {
			config = new KeyValues(args[0]);
		} else {
			config = new KeyValues("labeltool.config");
		}
		Recorder recorder = new Recorder(config);
		frame.setContentPane(recorder.getContentPane());
		frame.pack();
		frame.setVisible(true);
	}

}