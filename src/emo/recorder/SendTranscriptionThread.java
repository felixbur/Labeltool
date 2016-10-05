package emo.recorder;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;


/**
 * thread to remove a recording from the server.
 * @version 1.0
 * @author Felix Burkhardt
 */
public class SendTranscriptionThread extends Thread {
	String _charEncoding;
    /**
     * name of server's host.
     */
    String servername;
    /**
     * name of file to be removed.
     */
    Recording recording;
    /**
     * socket to connetct.
     */
    Socket s;
    /**
     * stream to write modeNum and fileName.
     */
    DataOutputStream out;
    /**
     * port where server listens.
     */
    int portNum;
	String label = "";
    /**
     *
     * @param servername name of server's host.
     * @param portNum num of port where server listens.
     * @param filename name of recording to be removed.
     */
    public SendTranscriptionThread (String servername, int portNum, Recording recording, String charEnc)
    {
        this.servername = servername;
        this.recording = recording;
        this.portNum = portNum;
        this.label = recording.words;
        _charEncoding = charEnc;
    }

    /**
     * called by thread.start()
     */
    public void run () {
        // open a connection
        openConnection(9);
        System.out.println("Sending transcript: "+label); 
        try {
            out.writeBytes(recording.path+'\n');
//			out.writeBytes(label+'\n');
			out.writeBytes(URLEncoder.encode(label, _charEncoding)+'\n');
        } catch (IOException e) {
            System.out.println("unable to send data " + e);
        }

        // close connection
        try {
            out.close();
            s.close();
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
            s = new Socket(servername, portNum);
            System.out.println("Verbindung mit: " + s.getInetAddress());
            out = new DataOutputStream(s.getOutputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: "+ servername + ", " + e);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to host: "+ servername + ", " + e);
        }

        // send modus code to server
        try {
            out.writeInt(modus);
        } catch (IOException e) {
            System.out.println("problem sending modis code: " + e);
        }
    }
}

