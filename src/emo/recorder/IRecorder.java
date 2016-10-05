package emo.recorder;

/**
 * Interface to define alle recorder related issues. Needed to free all threads
 * from gui related methods.
 * 
 * @author Stefan
 * 
 */
public interface IRecorder {

	/**
	 * Initializes the recorder. The name of the server's host is given as a
	 * parameter. The main pane to draw something gets called.
	 */
	public abstract void init();

	public abstract void setNrOfRecordings();

	public abstract void analyse();

	/**
	 * Gets called after the init method. Initializes the table with recordings,
	 * therefore starting an update thread.
	 */
	public abstract void start();

	public abstract void setEmoResult(String prediction, String configString);

	/**
	 * Method to set the Buttons and start a record thread.
	 * 
	 * @see RecordThread
	 */
	public abstract void record();

	public abstract int getSampleRate();

	/**
	 */
	public abstract void undo();

	/**
	 * Method to stop either playing or recording, depends on which thread isn't
	 * null. Recording and playing (full duplex) at the same time isn't
	 * possible. The button-enabling gets changed. The filelist gets updated if
	 * recording was stopped.
	 */
	public abstract void stopRecordingPlayback();

	/**
	 * Starts a play thread with the selected filename.
	 */
	public abstract void play();

	/**
	 * Resumes a play thread with the selected filename at the last position.
	 */
	public abstract void resume();

	/**
	 * Resumes a play thread with the selected filename at the given position no
	 * of byte to start with).
	 */
	public abstract void resume(int bytePos);

	/**
	 * Called by the play thread if playing is finished because file was played.
	 * Sets the button enabling.
	 */
	public abstract void finishedPlayback();

	/**
	 * Starts a thread to judge a file.
	 */
	public abstract void judge();

	/**
	 * Starts a thread to judge all files.
	 */
	public abstract void judgeAll();

	/**
	 * ONly example, might be used later on
	 */
	public abstract void resetRatings();

	public abstract void setEmotion(String emo);

	public abstract void removeLastLabel();

	public abstract void setMessage(String msg);

	public abstract void transcribe();

	public abstract void label();

	/**
	 * Starts a thread to delete the selected file from the server.
	 * 
	 * @see DeleteFileThread
	 */
	public abstract void deleteFile();

	/**
	 * If yes, transcript should be displayed, otherwise recognition result.
	 * 
	 * @return
	 */
	public abstract boolean showTranscript();

	/**
	 * @return the recordings
	 */
	public abstract RecordingTable getRecordings();

	/**
	 * @param recordings
	 *            the recordings to set
	 */
	public abstract void setRecordings(RecordingTable recordings);

}