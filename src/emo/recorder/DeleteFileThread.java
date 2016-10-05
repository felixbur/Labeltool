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
public class DeleteFileThread extends Thread {
    /**
     * name of server's host.
     */
    String servername;
    /**
     * file to be removed.
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

    /**
     *
     * @param servername name of server's host.
     * @param portNum num of port where server listens.
     * @param filename name of recording to be removed.
     */
    public DeleteFileThread (String servername, int portNum, Recording recording)
    {
        this.servername = servername;
        this.recording = recording;
        this.portNum = portNum;
    }

    /**
     * called by thread.start()
     */
    public void run () {
        // open a connection
        openConnection(3);

        try {
            out.writeBytes(recording.path+'\n');
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

