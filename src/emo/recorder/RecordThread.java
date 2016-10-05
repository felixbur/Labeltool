package emo.recorder;

import java.io.ByteArrayOutputStream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.TargetDataLine;

/**
 * Thread for recording and sending to server.
 * 
 * @version 1.0
 * @author Felix Burkhardt
 */
public class RecordThread extends Thread {
	/**
	 * Name of server's host.
	 */
	String servername;
	/**
	 * Name of file to save the recording at server.
	 */
	String _fileName;
	/**
	 * Line from Microphone.
	 */
	TargetDataLine _line;
	/**
	 * Bytearray to send the recording in chunks.
	 */
	byte[] _data;
	/**
	 * Stream to send data to server.
	 */
	java.io.DataOutputStream out;
	/**
	 * Flag to control the recording.
	 */
	boolean record_flag = true;
	/**
	 * Socket for server-connection.
	 */
	Socket s;
	/**
	 * The format of the recording.
	 */
	AudioFormat _audioFormat;
	Mixer _mixer;
	/**
	 * Port where server listens.
	 */
	int portNum;
	ByteArrayOutputStream _baos;

	/**
	 * @param _audioFormat
	 *            The audioformat.
	 * @param servername
	 *            The name of the server's host.
	 * @param portNum
	 *            The port where the server listens.
	 */
	public RecordThread(AudioFormat format, String servername, int portNum,
			String fn) {
		_audioFormat = format;
		this.servername = servername;
		this.portNum = portNum;
		this._fileName = fn;
	}

	private void printMixers() {
		try {
			Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
			int i = 0;
			for (Mixer.Info info : mixerInfos) {
				Mixer m = AudioSystem.getMixer(info);
				String mixerName = m.getMixerInfo().getName();
				System.out.println("mixer--" + mixerName);
				if (i++ == 0) {
					_mixer = m;
				}
				Line.Info[] lineInfos = m.getSourceLineInfo();
				for (Line.Info lineInfo : lineInfos) {
					System.out.println("source---" + lineInfo);
					Line line = m.getLine(lineInfo);

					System.out.println("\tsource-----" + line);
				}
				Line.Info[] lineInfos2 = m.getTargetLineInfo();
				for (Line.Info lineInfo : lineInfos2) {
					System.out.println("target---" + lineInfo);
					Line line = m.getLine(lineInfo);
					System.out.println("\ttarget-----" + line);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * called by thread.start()
	 */
	public void run() {
		// open a connection
		openConnection(Constants.MODE_RECORD);

		// Send the fileName to the server.
		try {
			out.writeBytes(_fileName + '\n');
		} catch (IOException e) {
			System.out.println("unable to send data " + e);
			e.printStackTrace();
		}

		// Get a line from the Microphone with the specified audioFormat.
		DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class,
				_audioFormat);
		if (!AudioSystem.isLineSupported(dataLineInfo)) {
			Info mixerInfo = _mixer.getMixerInfo();
			String mixerName = mixerInfo.getName();
			System.err.println("\ninitRec: format not supported for "
					+ mixerName + "\n");
		}
		try {
			 _line = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
//			_line = (TargetDataLine) _mixer.getLine(dataLineInfo);
			_line.open(_audioFormat, 2000);
		} catch (Exception ex) {
			String mixerName = _mixer.getMixerInfo().getName();
			System.err.println("Rec: line unavailable for " + mixerName + "\n");
			ex.printStackTrace();
		}

		// Init the byteArray with a size 1/5 of the line's buffer (recommended
		// by sound-api doc)
		_data = new byte[_line.getBufferSize() / 5];
		record_flag = true;
		System.out.println("Starte Aufnahme");
		// Start line for recording.
		_line.start();
		_baos = new ByteArrayOutputStream();
		while (record_flag) {
			// While not stopped read data from line and send to server.
			_line.read(_data, 0, _data.length);
			try {
				out.write(_data);
				out.flush();
				_baos.write(_data);
			} catch (Exception e) {
				if (record_flag) {
					// problem, there still should be recording
					// else thread tried to write when line already closed.
					System.err.println("problem writing data: " + e);
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Method to stop the recording. Should be called by the main class if the
	 * user pushes the stop-button.
	 */
	public void stopMe() {
		record_flag = false;
		try {
			System.out.println("baos.size:  " + _baos.toByteArray().length);
			_line.close();
			_line = null;
			s.close();
			openConnection(Constants.MODE_STOP);
			try {
				out.writeBytes(_fileName + '\n');
			} catch (IOException e) {
				System.err.println("unable to send data " + e);
				e.printStackTrace();
			}
		} catch (Exception e) {
			System.err.println("problem closing socket: " + e);
			e.printStackTrace();
		}
	}

	/**
	 * opens a socket and stream to the server and sends the mode-number.
	 */
	private void openConnection(int modus) {
		try {
			s = new Socket(servername, portNum);
			System.out.println("Verbindung mit: " + s.getInetAddress());
			out = new DataOutputStream(s.getOutputStream());
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: " + servername + ", "
					+ e);
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to host: "
					+ servername + ", " + e);
			e.printStackTrace();
		}

		// send modus code to server
		try {
			out.writeInt(modus);
		} catch (IOException e) {
			System.out.println("problem sending modus code: " + e);
			e.printStackTrace();
		}
	}
}