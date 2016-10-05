package emo.recorder;

import javax.swing.event.ChangeListener;

public interface IPlayer {

	/**
	 * invoked to start player
	 */
	public abstract void startMe();

	/**
	 * invoked to start player at current position
	 */
	public abstract void startMe(int offset);

	/**
	 * invoked to stop player
	 */
	public abstract int stopMe();

	public abstract boolean isPlaying();
	
	/** changeListener to get notified on playback progress
	 * @param cl Listener to notify 
	 */ 
	public void addChangeListener( ChangeListener cl );
	
	/** get the Recording currently played
	 * 
	 *  @return current Recording Object
	 */
	public abstract Recording getRecording();

	public abstract IRecorder getRecorder();
	
	public int getCurrentPosition();

	public int getCurrentPositionInSec();

}