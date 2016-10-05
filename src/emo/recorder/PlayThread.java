package emo.recorder;

import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.AudioSystem;
import javax.swing.event.ChangeListener;


import java.io.IOException;
import java.io.DataOutputStream;
import java.io.DataInputStream;

/**
 * plays a recording on clients audio-output.
 * 
 * @version 1.0
 * @author Felix Burkhardt
 */
public class PlayThread extends Thread implements IPlayer {
	
	private static final int NOTIFY_INTERVALL = 8000;
	
    String servername;

    Recording recording;

    boolean play_flag;

    Socket s;

    IRecorder rec;

    AudioFormat audioFormat;

    DataOutputStream out;

    DataInputStream in;

    SourceDataLine sourceDataLine;

    AudioInputStream ain = null; // We read audio data from here

    DataLine.Info info;

    byte data[];

    int numBytesRead;

    int numBytesPlayed = 0;

    int portNum;

    boolean playWav = false;

    int offset = 0;

    ArrayList<ChangeListener> cListener;
    
    int nextNotify = 0;
    
    /**
     * @param formatpcam
     *            format of audiofile.
     * @param servername
     *            name of server's host.
     * @param fileName
     *            name of file to be played.
     * @param rec
     *            main class.
     * @param portNum
     *            number of port where server listens
     */
    public PlayThread(AudioFormat format, String servername, int portNum, Recording recording, IRecorder rec, int offset) {
        this.audioFormat = format;
        this.servername = servername;
        this.recording = recording;
        this.rec = rec;
        this.portNum = portNum;
        setOffset(offset);
        this.cListener = new ArrayList<ChangeListener>(5);;
    }

	private void setOffset(int offset) {
        // ggf offset auf gueltigen bytewert setzen
        int adjOffset = offset % audioFormat.getFrameSize();
        if ( adjOffset != 0 ) {
        	System.out.println( "adjust offset by " + adjOffset );
        	offset -= adjOffset; 
        }
		this.offset = offset;
		this.numBytesPlayed = offset;
        this.nextNotify = ( ((int)(offset / NOTIFY_INTERVALL)) + 1 )* NOTIFY_INTERVALL;
	}

    /* (non-Javadoc)
	 * @see emo.recorder.IPlayer#run()
	 */
    public void run() {
    	updateListener( offset );
        // open a connection
        openConnection(Constants.MODE_PLAY);
        if (recording.name.endsWith(".wav")) {
            playWav = true;
        }
        // send filename and offset to server
        try {
            out.writeBytes(recording.path + '\n');
            out.writeBytes(String.valueOf(offset) + '\n');
        } catch (IOException e) {
            System.out.println("unable to send data " + e);
        }
        boolean alawCoded = (audioFormat.getEncoding() == AudioFormat.Encoding.ALAW);
        if (alawCoded && playWav) {
            try {
                // Get an audio input stream from the URL
                ain = AudioSystem.getAudioInputStream(in);
                // Get information about the format of the stream
                AudioFormat format = ain.getFormat();
                info = new DataLine.Info(SourceDataLine.class, format);
                System.out.println("#######: " + info.toString());
                // If the format is not supported directly (i.e. if it is not PCM
                // encoded), then try to transcode it to PCM.
                if (!AudioSystem.isLineSupported(info)) {
                    // This is the PCM format we want to transcode to.
                    // The parameters here are audio format details that you
                    // shouldn't need to understand for casual use.
                    AudioFormat pcm = new AudioFormat(format.getSampleRate(), 16, format.getChannels(), true, false);

                    // Get a wrapper stream around the input stream that does the
                    // transcoding for us.
                    ain = AudioSystem.getAudioInputStream(pcm, ain);

                    // Update the format and info variables for the transcoded data
                    format = ain.getFormat();
                    info = new DataLine.Info(SourceDataLine.class, format);
                }

                // Open the line through which we'll play the streaming audio.
                sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
                sourceDataLine.open(format);

                // Allocate a buffer for reading from the input stream and writing
                // to the line. Make it large enough to hold 4k audio frames.
                // Note that the SourceDataLine also has its own internal buffer.
                int framesize = format.getFrameSize();
                byte[] buffer = new byte[4 * 1024 * framesize]; // the buffer
                int numbytes = 0; // how many bytes

                // We haven't started the line yet.
                boolean started = false;
                play_flag = true;

                while (play_flag) { // We'll exit the loop when we reach the end of stream
                    // First, read some bytes from the input stream.
                    int bytesread = ain.read(buffer, numbytes, buffer.length - numbytes);
                    // If there were no more bytes to read, we're done.
                    if (bytesread == -1)
                        break;
                    numbytes += bytesread;

                    // Now that we've got some audio data to write to the line,
                    // start the line, so it will play that data as we write it.
                    if (!started) {
                        sourceDataLine.start();
                        started = true;
                    }

                    // We must write bytes to the line in an integer multiple of
                    // the framesize. So figure out how many bytes we'll write.
                    int bytestowrite = (numbytes / framesize) * framesize;

                    // Now write the bytes. The line will buffer them and play
                    // them. This call will block until all bytes are written.
                    sourceDataLine.write(buffer, 0, bytestowrite);
                    numBytesPlayed += bytestowrite;
 //                   System.out.println(numBytesPlayed);
                    if ( numBytesPlayed > nextNotify ) {
                    	nextNotify += NOTIFY_INTERVALL;
                    	updateListener( numBytesPlayed );
                    }
                    
                    // If we didn't have an integer multiple of the frame size,
                    // then copy the remaining bytes to the start of the buffer.
                    int remaining = numbytes - bytestowrite;
                    if (remaining > 0)
                        System.arraycopy(buffer, bytestowrite, buffer, 0, remaining);
                    numbytes = remaining;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // init sound
            info = new DataLine.Info(SourceDataLine.class, audioFormat);
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("format " + info.toString() + " not supported");
                Mixer.Info mixerinfo = AudioSystem.getMixerInfo()[0];
                for (int i = 0; i < AudioSystem.getMixer(mixerinfo).getTargetLineInfo().length; i++) {
                    System.out.println(AudioSystem.getMixer(mixerinfo).getTargetLineInfo()[i]);
                }
                rec.setMessage("audio format " + info.toString() + " not supported with headerless files");
                return;
            }
            try {
                sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
                // open Sourceline with Audioformat and 0.5 sek buffer (8000/2)
                sourceDataLine.open(audioFormat, 4000);
            } catch (Exception ex) {
                System.err.println("Play: line unavailable" + ex);
                return;
            }
            // start streaming
//            byte[] buffer = new byte[4 * 1024 * framesize]; // the buffer
            int DATA_SIZE=2048;
            data = new byte[DATA_SIZE];
            sourceDataLine.start();
            play_flag = true;
            System.out.println("Starte Wiedergabe at " + numBytesPlayed + ", next change " + nextNotify);
            boolean first = true;
            if ( numBytesPlayed > 0 ) first = false;
            while (play_flag) {
                try {
                    numBytesRead = in.read(data, 0, data.length);
                } catch (Exception e) {
                    System.err.println("caught read exception: " + e);
                    numBytesRead = -1;
                }
                if (numBytesRead == -1) {
                	sourceDataLine.flush();
                    System.out.println("finished playback");
                    break;
                }
                if (playWav && first) {
                    // omit wav header
                    first = false;
                } else {
                    sourceDataLine.write(data, 0, numBytesRead);
                    numBytesPlayed += numBytesRead;
//                    System.out.println(numBytesPlayed);
                    if ( numBytesPlayed > nextNotify ) {
                    	nextNotify += NOTIFY_INTERVALL;
                    	updateListener( numBytesPlayed );
                    }
                }
            }
        }
        // close the connection
        closeConnection();

        // tell recorder that playing is finished.
        updateListener( numBytesPlayed );
        rec.finishedPlayback();
    }

    /* (non-Javadoc)
	 * @see emo.recorder.IPlayer#startMe()
	 */
    public void startMe() {
    	run();
    }
    
    
    /* (non-Javadoc)
	 * @see emo.recorder.IPlayer#startMe(int)
	 */
	@Override
	public void startMe(int offset) {
		this.numBytesRead = 0;
		setOffset( offset );
		run();
	}

	/* (non-Javadoc)
	 * @see emo.recorder.IPlayer#stopMe()
	 */
    public int stopMe() {
        System.out.println("stopped playback");
        if ( play_flag ) {
        	play_flag = false;
        }
        else {
        	closeConnection();
        }
        return numBytesPlayed;
    }

    public IRecorder getRecorder() {
    	return rec;
    }
    
    
	/* (non-Javadoc)
	 * @see emo.recorder.IPlayer#isPlaying()
	 */
	public boolean isPlaying() {
		return play_flag;
	}

    /* (non-Javadoc)
	 * @see emo.recorder.IPlayer#addChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void addChangeListener(ChangeListener cl) {
		cListener.add( cl );
	}

	
	
	/* (non-Javadoc)
	 * @see emo.recorder.IPlayer#getRecording()
	 */
	@Override
	public Recording getRecording() {
		return recording;
	}

	
	/* (non-Javadoc)
	 * @see emo.recorder.IPlayer#getCurrentPosition()
	 */
	@Override
	public int getCurrentPosition() {
		return numBytesPlayed;
	}

	/* (non-Javadoc)
	 * @see emo.recorder.IPlayer#getCurrentPositionInSec()
	 */
	@Override
	public int getCurrentPositionInSec() {
		return numBytesPlayed / rec.getSampleRate();
	}

	
	private void updateListener( int bytePos ) {
		int i = cListener.size();
		
		if ( i > 0 ) {
			ChangeListener cl = null;
			WavPositionChangeEvent we = new WavPositionChangeEvent(this, bytePos );
			for ( ; --i >= 0; ) {
				if ( (cl = cListener.get(i)) != null ) {
//					System.out.println( "update " + cl );
					cl.stateChanged( we );
				}
			}
		}
	}
	
	/**
     * opens a connection (socket and streams) and sends mode number.
     */
    private void openConnection(int modus) {
        try {
            s = new Socket(servername, portNum);
            System.out.println("Connected to: " + s.getInetAddress());
            out = new DataOutputStream(s.getOutputStream());
            in = new DataInputStream(s.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host: " + servername + ", " + e);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to host: " + servername + ", " + e);
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
     * closes the connection and audio-line.
     */
    private void closeConnection() {
        try {
        	if ( sourceDataLine != null ) {
	            if (play_flag)
	                sourceDataLine.drain();
	            sourceDataLine.stop();
	            sourceDataLine.close();
	            sourceDataLine = null;
        	}
        } catch (Exception e) {
            System.err.println("problem closing line: " + e);
            e.printStackTrace();
        }
        try {
            play_flag = false;
            out.close();
            in.close();
            s.close();
        } catch (Exception e) {
            System.err.println("problem closing streams and socket: " + e);
            e.printStackTrace();
        }
    }
}