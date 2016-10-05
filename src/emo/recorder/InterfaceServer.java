package emo.recorder;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * ask the server something
 * 
 * @version 1.0
 * @author Felix Burkhardt
 */
public class InterfaceServer {
	/**
	 * name of server's host.
	 */
	String servername;
	/**
	 * feedback from server.
	 */
	String sendStatus;
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
	DataInputStream in;

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
	public InterfaceServer(String servername, int portNum) {
		this.servername = servername;
		this.portNum = portNum;
	}

	/**
	 * Send a message to the server and get a response, finalized by the string
	 * "finished".
	 * 
	 * @param question
	 *            The message.
	 * 
	 */
	public String getAnswer(String question) {
		// open a connection
		openConnection(7);
		// send filename and recipient to server
		try {
			out.writeBytes(question + '\n');
		} catch (IOException e) {
			System.out.println("unable to send data " + e);
		}
		// read feedback from server (emoresult).
		stringReader = new BufferedReader(new InputStreamReader(in));
		String receivedString = "", resultString = "";
		try {
			while (!(receivedString = stringReader.readLine())
					.equals("finished")) {
				resultString += receivedString + "\n";
			}
		} catch (Exception e) {
			System.err.println("problem receiving send status: " + e);
		}
		// close
		closeConnection();
		return resultString;
	}
	/**
	 * Send a message to the server and get a response, finalized by the string
	 * "finished".
	 * 
	 * @param question
	 *            The message.
	 * 
	 */
	public void sendMessage(String message) {
		// open a connection
		openConnection(7);
		// send filename and recipient to server
		try {
			out.writeBytes(message + '\n');
		} catch (IOException e) {
			System.out.println("unable to send data " + e);
		}
		// close
		closeConnection();
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
			out.close();
			s.close();
		} catch (Exception e) {
			System.err.println("problem closing streams and socket: " + e);
		}
	}
}