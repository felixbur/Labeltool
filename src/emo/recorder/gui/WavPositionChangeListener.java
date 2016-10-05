package emo.recorder.gui;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import emo.recorder.IPlayer;
import emo.recorder.IRecorder;
import emo.recorder.WavPositionChangeEvent;

public class WavPositionChangeListener implements ChangeListener {

	int lastPos;
	JSlider slider;
	IRecorder recorder;
	
	public WavPositionChangeListener(  ) {
		lastPos = 0;
		recorder = null;
		slider = null;
	}
	
	public WavPositionChangeListener( JSlider slider, IRecorder rec ) {
		this.lastPos = 0;
		this.recorder = rec;
		this.slider = slider;
	}
	
	
	public void stateChanged(ChangeEvent ce) {

		// Event from player to update slider position
		if ( ce.getSource() instanceof IPlayer ) {
//			System.out.println( "wav event from player=" + lastPos );
			lastPos = ((WavPositionChangeEvent)ce).getPosition();
			slider.setValue( (int)(slider.getMaximum()*lastPos) );
		}
		// Event from slider side received - notify recorder 
		else if ( ce.getSource() instanceof JSlider ) {
//			System.out.println( "wav event from slider=" + lastPos );
			JSlider source = (JSlider)ce.getSource();
		    if (!source.getValueIsAdjusting()) {
		        int newPos = source.getValue();
		        if ( newPos != lastPos ) {
			        recorder.stopRecordingPlayback();
			        recorder.resume( lastPos = newPos );
		        }
			}
		}
		else {
//			System.out.println( "wav event" + ce );
		}
	}

	public void setRecorder( IRecorder rec ) {
		this.recorder = rec; 
	}
	
	public double getLastPosition() {
		return lastPos;
	}
}
