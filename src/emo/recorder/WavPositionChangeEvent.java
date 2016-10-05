package emo.recorder;

import javax.swing.event.ChangeEvent;

public class WavPositionChangeEvent extends ChangeEvent {

	private static final long serialVersionUID = 1L;
	
	/** position inside the audio file, number of bytes played */
	int position;
	
	public WavPositionChangeEvent(Object src) {
		super(src);
		this.position = 0;
	}

	public WavPositionChangeEvent(Object src, int newPosition ) {
		super(src);
		this.position = newPosition;
	}

	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}

}
