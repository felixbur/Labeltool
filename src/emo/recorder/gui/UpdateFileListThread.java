package emo.recorder.gui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import emo.recorder.Constants;
import emo.recorder.Recording;
import emo.recorder.Util;

/**
 * gets the list of available resordings from the server and displays in the
 * table.
 * 
 * @version 1.0
 * @author Felix Burkhardt
 */
public class UpdateFileListThread extends Thread {
	private String _charEncoding;

	/**
	 * name of server's host.
	 */
	String servername;
	/**
	 * tmp var to read name and size from server.
	 */
	String receivedString;
	/**
	 * name of recording.
	 */
	String fileName;
	/**
	 * size of recording.
	 */
	String fileSize;
	/**
	 * help bool to know whether name or size is expected.
	 */
	boolean expectName = true;
	/**
	 * input from server.
	 */
	DataInputStream in;
	/**
	 * output to server. Needed to send the mode number.
	 */
	DataOutputStream out;
	/**
	 * to read string from input stream.
	 */
	BufferedReader stringReader;
	/**
	 * to get a connection.
	 */
	Socket s;
	/**
	 * list of recordings.
	 */
	Recorder rec;
	/**
	 * num of port where server listens.
	 */
	int portNum;
	/**
	 * determine whether audio file manager must be updated (new recordings from
	 * outside the system) as well.
	 */
	boolean _updateAFM = false;
	boolean _sortOrderAscending = true, _sorting = false;

	/**
	 * 
	 * @param servername
	 *            name of server's host.
	 * @param portNum
	 *            num of port where server listens.
	 * @param recordings
	 *            list of recordings.
	 */
	public UpdateFileListThread(String servername, int portNum, Recorder rec,
			boolean updateAFM, String charEnc, boolean sorting,
			boolean sortORderAscending) {
		this.servername = servername;
		this.rec = rec;
		this.portNum = portNum;
		_updateAFM = updateAFM;
		_charEncoding = charEnc;
		_sortOrderAscending = sortORderAscending;
		_sorting = sorting;
	}

	/**
	 * called by thread.start().
	 */
	public void run() {
		System.out.println("Starte Update");
		rec.getRecordings().clear();

		// open a connection
		if (!openConnection(Constants.MODE_SEND_LIST)) {
			return;
		}
		try {
			out.writeBytes(String.valueOf(_updateAFM) + '\n');
		} catch (IOException e) {
			System.out.println("unable to send data " + e);
		}

		// receive file-list
		stringReader = new BufferedReader(new InputStreamReader(in,
				Charset.forName(_charEncoding)));
		Vector<Recording> tmp = new Vector<Recording>();
		int ind = 0;
		try {
			String version = stringReader.readLine();
			rec.setServerVersion(version);
			System.out.println("server version: " + version);
			while (!(receivedString = stringReader.readLine())
					.equals("finished")) {
				// System.out.println(receivedString);
				ind++;
				StringTokenizer st = new StringTokenizer(receivedString, ";");
				String words = "", prediction, recognition = "";
				double lab[];
				String filePath = st.nextToken();
				fileSize = st.nextToken();
				String input = st.nextToken();
				String tokens[] = Util.stringToArray(input);
				lab = new double[tokens.length];
				for (int i = 0; i < tokens.length; i++) {
					lab[i] = Double.parseDouble(tokens[i]);

				}
				prediction = st.nextToken();
				words = URLDecoder.decode(st.nextToken(), _charEncoding);
				recognition = URLDecoder.decode(st.nextToken(), _charEncoding);

				Recording r = new Recording(filePath, fileSize, words.trim(),
						lab, prediction, rec.getParameter("categories"), rec);
				r.recognition = recognition;
				if (ind % 10 == 0) {
					System.out.print(".");
				}
				tmp.add(r);
			}
			rec.setDataDescription(stringReader.readLine());
			System.out.println();
		} catch (IOException e) {
			System.out.println("unable to receive data: " + e);
		}
		if (_sorting) {
			Collections.sort(tmp);
			if (_sortOrderAscending) {
				for (int i = tmp.size() - 1; i >= 0; i--) {
					rec.getRecordings().insertRecording(tmp.elementAt(i));
				}
			} else {
				for (Recording r : tmp) {
					rec.getRecordings().insertRecording(r);
				}
			}
		} else {
			for (Recording r : tmp) {
				rec.getRecordings().insertRecording(r);
			}			
		}
		rec.setNrOfRecordings();

		// close connection
		try {
			in.close();
			out.close();
			s.close();
			System.out.println("Socket closed");
		} catch (IOException e) {
			System.err.println("problem closing streams and socket: " + e);
			e.printStackTrace();
		}
		if (rec._table.getRowCount() > 0) {
			try {
				rec._table.setRowSelectionInterval(0, 0);
			} catch (Exception e) {
				System.err
						.println("not possible to set: rec.table.setRowSelectionInterval(0, 0) : "
								+ e.getMessage());
			}
			rec._table.requestFocus();
		}
		rec.repaintView();
	}

	/**
	 * opens a connection to the server and sends mode number.
	 */
	private boolean openConnection(int modus) {
		try {
			s = new Socket(servername, portNum);
			System.out.println("Verbindung mit: " + s.getInetAddress());
			out = new DataOutputStream(s.getOutputStream());
			in = new DataInputStream(s.getInputStream());
			out.writeInt(modus);
			return true;
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: " + servername + ", "
					+ e);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to host: "
					+ servername + ", " + e);
			rec.setMessage("Couldn't get I/O for the connection to host: "
					+ servername + ", " + e);
		}
		return false;

	}
}