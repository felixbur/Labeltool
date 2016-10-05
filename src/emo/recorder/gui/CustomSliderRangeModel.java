package emo.recorder.gui;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;

import emo.recorder.IPlayer;
import emo.recorder.WavPositionChangeEvent;

public class CustomSliderRangeModel extends DefaultBoundedRangeModel implements ChangeListener {

	private static final long serialVersionUID = 1L;

	private IPlayer _player;
	private boolean _userChangedValue;
	private boolean _wasPlaying;
	private int _sliderPosition;
	
	public CustomSliderRangeModel() {
		super();
		_player = null;
		_userChangedValue = false;
		_wasPlaying = false;
		_sliderPosition = -1;
	}

	public CustomSliderRangeModel(int arg0, int arg1, int arg2, int arg3) {
		super(arg0, arg1, arg2, arg3);
		_player = null;
		_userChangedValue = false;
		_wasPlaying = false;
		_sliderPosition = -1;
	}

	public CustomSliderRangeModel(IPlayer player) {
		super();
		setPlayer( player );
		_userChangedValue = false;
		_wasPlaying = false;
		_sliderPosition = -1;
	}

	public void setPlayer(IPlayer player) {
		_player = player;
		_player.addChangeListener( this );
		setMinimum( 0 );
		setMaximum( player.getRecording().getSize() );
		super.setValue( player.getCurrentPosition() );
	}


	/* (non-Javadoc)
	 * @see javax.swing.DefaultBoundedRangeModel#setValue(int)
	 */
	@Override
	public void setValue(int arg0) {
		// only directly called from slider to set new value after user has moved
//		System.out.println( "adjust: " + getValueIsAdjusting() + ", setValue: " + arg0 );
		if ( getValueIsAdjusting() ) {
			if ( !_userChangedValue ) {
				_userChangedValue = true;
				_wasPlaying = _player.isPlaying();
				// hier wird nur Thread gestoppt, aber nicht Button betaetigt
				//if ( _wasPlaying ) _player.stopMe();
				if ( _wasPlaying ) _player.getRecorder().stopRecordingPlayback();
			}
			super.setValue(arg0);
			_sliderPosition = arg0;
		}
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	@Override
	public void stateChanged(ChangeEvent arg0) {
		// message from playback to set new position within audiostream (for display)
		if ( arg0 instanceof WavPositionChangeEvent ) {
//			System.out.println( "stateChanged: " + ((WavPositionChangeEvent)arg0).getPosition() +
//					"," + getValueIsAdjusting() );
			super.setValue( ((WavPositionChangeEvent)arg0).getPosition() );
		}
		// message from slider that it was/is moved
		else if ( arg0.getSource() instanceof JSlider ) {
			if ( !getValueIsAdjusting() ) {
				if ( _userChangedValue ) {
//					System.out.println( "stateChanged JSlider: " + getValueIsAdjusting() + 
//							"," + _userChangedValue + "," + _wasPlaying);
//					_player.getRecorder().stopRecordingPlayback();
					_userChangedValue = false;
					if ( _wasPlaying ) {
//						System.out.println( "restart at: " + getValue() );
						_wasPlaying = false;
						// hier direkt Thread gestoppt
						//_player.startMe( getValue() );
						_player.getRecorder().play();
					}
				}
			}
		}
		else {
//			System.out.println( "stateChanged ???: " + arg0.getSource() );
		}
	}

	public int getSliderPosition() {
		return _sliderPosition;
	}
}
