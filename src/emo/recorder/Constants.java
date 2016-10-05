package emo.recorder;

/**
 * Changelog
 * 0.93 fixed bug: slider now moveable even while playing.
 * 
 * 
 * @author felix
 *
 */
public class Constants {
    public final static double version = 2.21;
    public final static String title = "Labeltool";
    public final static String CMD_ADD="add";
    public final static String CMD_IGNORE="ignore";
    
    public static final int PREROLL = 8000;
	public final static int MODE_RECORD = 0;
	public final static int MODE_PLAY = 1;
	public final static int MODE_JUDGE = 2;
	public final static int MODE_DELETE = 3;
	public final static int MODE_SEND_LIST = 4;
	public final static int MODE_STOP = 5;
	public final static int MODE_EVALUATE = 6;
	public final static int MODE_MESSAGE = 7;
	public final static int MODE_SET_LABEL = 8;
	public final static int MODE_SET_TRANS = 9;
	public final static int MODE_RECOGNIZE = 10;
	public final static String TYPE_SMO = "smo";
	public final static String TYPE_NAIVE_BAYEYS = "naiveBayes";
	public final static String TYPE_J48 = "j48";

}
