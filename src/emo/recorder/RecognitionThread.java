package emo.recorder;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * tells the server to evaluate the classification model and informs the user
 * via message box..
 * 
 * @version 1.0
 * @author Felix Burkhardt
 */
public class RecognitionThread extends Thread {
	/**
	 * name of server's host.
	 */
	String servername;
	/**
	 * to read a string.
	 */
	BufferedReader stringReader;
	/**
	 * port where server listens.
	 */
	int portNum;
	/**
	 * socket to connect.
	 */
	Socket s;
	/**
	 * output stream.
	 */
	DataOutputStream out;
	/**
	 * input stream
	 */
	DataInputStream in;
	/**
	 * instance of applet needed by JOptionPane.
	 */
	IRecorder rec;
	Recording _recording;

	/**
	 * @param fileName
	 *            name of file to send.
	 * @param recipient
	 *            email of recipient.
	 * @param servername
	 *            name of server's host.
	 * @param portNum
	 *            port where server listens.
	 * @param rec
	 *            main applet.
	 */
	public RecognitionThread(String servername, int portNum, IRecorder rec,
			Recording recording) {
		this.servername = servername;
		this.portNum = portNum;
		this.rec = rec;
		_recording = recording;
	}

	/**
	 * called by thread.start().
	 */
	public void run() {
		// open a connection
		openConnection(Constants.MODE_RECOGNIZE);
		// send filename and offset to server
		try {
			out.writeBytes(_recording.path + '\n');
		} catch (IOException e) {
			System.out.println("unable to send data " + e);
		}
		stringReader = new BufferedReader(new InputStreamReader(in));
		String receivedString = "", recoResultString = "";
		try {
			while (!(receivedString = stringReader.readLine())
					.equals("finished")) {
				recoResultString += receivedString + "\n";
			}
		} catch (Exception e) {
			System.err.println("problem receiving send status: " + e);
		}
		// close
		closeConnection();
		_recording.words = recoResultString;
		rec.getRecordings().fireTableDataChanged();

	}

	/**
	 * opens a connection and streams and sends mode number.
	 */
	private void openConnection(int modus) {
		try {
			s = new Socket(servername, portNum);
			System.out.println("Verbindung mit: " + s.getInetAddress());
			out = new DataOutputStream(s.getOutputStream());
			in = new DataInputStream(s.getInputStream());
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: " + servername + ", "
					+ e);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to host: "
					+ servername + ", " + e);
			System.exit(-1);
		}

		// send modus code to server
		try {
			out.writeInt(modus);
		} catch (IOException e) {
			System.out.println("problem sending modus code: " + e);
		}
	}

	/**
	 * closes the connection.
	 */
	private void closeConnection() {
		// Close the Streams and the socket.
		try {
			in.close();
			out.close();
			s.close();
		} catch (Exception e) {
			System.err.println("problem closing streams and socket: " + e);
		}
	}
}