package emo.recorder;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * thread to remove a recording from the server.
 * @version 1.0
 * @author Felix Burkhardt
 */
public class SetFileEmotionThread extends Thread {
    /**
     * name of server's host.
     */
    String _servername;
    /**
     * name of file to be removed.
     */
    Recording _recording;
    /**
     * socket to connetct.
     */
    Socket _socket;
    /**
     * stream to write modeNum and fileName.
     */
    DataOutputStream _out;
    /**
     * port where server listens.
     */
    int _portNum;
	String _label = "",  _male="", _german="", _category="";
    /**
     *
     * @param servername name of server's host.
     * @param portNum num of port where server listens.
     * @param filename name of recording to be removed.
     */
    public SetFileEmotionThread (String servername, int portNum, Recording recording, String category, String label)
    {
        this._servername = servername;
        this._recording = recording;
        this._portNum = portNum;
        this._label = label;
        this._category = category;
    }

    /**
     * called by thread.start()
     */
    public void run () {
        // open a connection
        openConnection(8);

        try {
            _out.writeBytes(_recording.path+'\n');
			_out.writeBytes(_category+'\n');
			_out.writeBytes(_label+'\n');
        } catch (IOException e) {
            System.out.println("unable to send data " + e);
        }

        // close connection
        try {
            _out.close();
            _socket.close();
            System.out.println("Socket closed");
        } catch(IOException e) {
            System.out.println("unable to close socket " + e);
        }
    }

    /**
     * opens a connection to the server and sends mode number.
     */
    private void openConnection (int modus)
    {
        try {
            _socket = new Socket(_servername, _portNum);
            System.out.println("Verbindung mit: " + _socket.getInetAddress());
            _out = new DataOutputStream(_socket.getOutputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: "+ _servername + ", " + e);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to host: "+ _servername + ", " + e);
        }

        // send modus code to server
        try {
            _out.writeInt(modus);
        } catch (IOException e) {
            System.out.println("problem sending modis code: " + e);
        }
    }
}

