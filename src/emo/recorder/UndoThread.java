package emo.recorder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * tells the server to send a recording via email attachment.
 * Gives feedback whether sedning was successful.
 * @version 1.0
 * @author Felix Burkhardt
 */
public class UndoThread extends Thread {
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
	
    /**
     * @param fileName name of file to send.
     * @param recipient email of recipient.
     * @param servername name of server's host.
     * @param portNum port where server listens.
     * @param rec main applet.
     */
    public UndoThread (String servername, int portNum)
    {
        this.servername = servername;
        this.portNum = portNum;
    }

    /**
     * called by thread.start().
     */
    public void run () {
			openConnection(9);
        // close
        closeConnection();
    }

    /**
     * opens a connection and streams and sends mode number.
     */
    private void openConnection (int modus)
    {
        try {
            s = new Socket(servername, portNum);
            System.out.println("Verbindung mit: " + s.getInetAddress());
            out = new DataOutputStream(s.getOutputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: "+ servername + ", " + e);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to host: "+ servername + ", " + e);
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
    private void closeConnection ()
    {
        // Close the Streams and the socket.
        try {
            out.close();
            s.close();
        } catch (Exception e) {
            System.err.println("problem closing streams and socket: " + e);
        }
    }
}