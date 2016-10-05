package emo.recorder;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

/**
 * <p>
 * �berschrift:
 * </p>
 * <p>
 * Beschreibung:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Organisation: T-Systems
 * </p>
 * 
 * @author burkhardt
 * @version 1.0
 * @date 12.03.2004
 */
public class Util {

	/**
	 * Return a hashmap from a filepath. Format is "<keyString> |
	 * <valueString>" (each line one value pair).
	 * 
	 * @param filename
	 *            The path to the file that containes the values.
	 * @return The hashmap.
	 */
	public static HashMap<String, String> getValuesFromFile(String filename) {
		HashMap<String, String> hm = new HashMap<String, String>();
		try {
			String line = null;
			String key, value;
			StringTokenizer st = null;
			BufferedReader br = new BufferedReader(new FileReader(filename));
			while ((line = br.readLine()) != null) {
				if (line.trim().length() > 0 && !line.trim().startsWith("#")) {
					st = new StringTokenizer(line, "|");
					key = st.nextToken();
					value = "";
					try {
						value = st.nextToken();
					} catch (Exception ex) {
						System.err.println("found no value for: " + key);

					}
					hm.put(key, value);
					// System.err.println("key: "+key+", val: "+value);
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error reading config file: " + e);
		}
		return hm;
	}

	/**
	 * return the tokens of a string.
	 * 
	 * @param s
	 * @return
	 */
	public static Vector<String> stringToVector(String s) {
		Vector<String> ret = new Vector<String>();
		StringTokenizer st = new StringTokenizer(s);
		while (st.hasMoreTokens()) {
			ret.add(st.nextToken());
		}
		return ret;
	}

	/**
	 * return the tokens of a string.
	 * 
	 * @param s
	 * @return
	 */
	public static String[] stringToArray(String s) {
		String ret[] = null;
		StringTokenizer st = new StringTokenizer(s);
		ret = new String[st.countTokens()];
		int i = 0;
		while (st.hasMoreTokens()) {
			ret[i++] = st.nextToken();
		}
		return ret;
	}

	public static void printOut(String msg) {
		System.out.println(msg);
	}

	public static void printOut(String msg, boolean newLine) {
		System.out.print(msg);
		if (newLine) {
			System.out.println();
		}
	}

	public static double roundDouble(double d) {
		return ((int) (d * 100)) / 100.0;
	}
    public static double roundDoubleToOne(double d) {
        return ((int) (d * 10)) / 10.0;
    }

	public static int percentage(int part, int whole) {
		return (int) (((double) part * 100.0) / (double) whole);
	}

	public static String getDate() {
		GregorianCalendar g = new GregorianCalendar();
		int year = g.get(Calendar.YEAR);
		int month = g.get(Calendar.MONTH) + 1;
		int day = g.get(Calendar.DAY_OF_MONTH);
		int hour = g.get(Calendar.HOUR_OF_DAY);
		int min = g.get(Calendar.MINUTE);
		int sec = g.get(Calendar.SECOND);
		return day + "." + month + "." + year + " " + hour + ":" + min + ":"
				+ sec;
	}

	/**
	 * <pre>
	 * file 1 in 2 kopieren.
	 * </pre>
	 * 
	 * @param in
	 * @param out
	 * @throws Exception
	 */
	public static void copyFile(File in, File out) throws Exception {
		FileInputStream fis = new FileInputStream(in);
		FileOutputStream fos = new FileOutputStream(out);
		byte[] buf = new byte[1024];
		int i = 0;
		while ((i = fis.read(buf)) != -1) {
			fos.write(buf, 0, i);
		}
		fis.close();
		fos.close();
	}

	/**
	 * checks if file exists
	 * 
	 * @param fileName
	 *            abssolute filename
	 * @return true if and only if the file denoted by this abstract pathname
	 *         exists; false otherwise
	 * @exception SecurityException
	 *                Description of the Exception
	 */
	public static boolean existFile(String fileName) throws SecurityException {
		boolean exist = false;
		File tmpFile = new File(fileName);
		exist = tmpFile.isFile();
		tmpFile = null;
		return exist;
	}

	/**
	 * checks if path exists
	 * 
	 * @param fileName
	 *            path to test
	 * @return true if path exists, otherwise false
	 * @exception SecurityException
	 *                Description of the Exception
	 */
	public static boolean existPath(String fileName) throws SecurityException {
		boolean exist = false;
		if (fileName != null && fileName.length() > 0) {
			File tmpFile = new File(fileName);
			exist = tmpFile.isDirectory();
			tmpFile = null;
		}
		return exist;
	}

	/**
	 * renames a file to the new filename
	 * 
	 * @param srcFilename
	 *            old filename
	 * @param destFilename
	 *            new filename
	 * @return true if success, otherwise false
	 */
	public static boolean rename(String srcFilename, String destFilename) {
		File srcFile;
		File destFile;
		boolean result = false;

		srcFile = new File(srcFilename);
		destFile = new File(destFilename);
		try {
			result = srcFile.renameTo(destFile);
		} catch (Exception e) {
			System.err.println("failed to rename file: " + e.toString());
			// e.printStackTrace();
		} finally {
			srcFile = null;
			destFile = null;
		}
		return result;
	}

	/**
	 * delete file
	 * 
	 * @param fileName
	 *            file to delete
	 * @return true if success, otherwise false
	 */
	public static boolean delete(String fileName) {
		boolean result = false;
		File tmpFile = new File(fileName);
		result = tmpFile.delete();
		tmpFile = null;
		return result;
	}

	/**
	 * creates a new File
	 * 
	 * @param fileName
	 *            filename of the file to create
	 * @return true if successs, otherwise false
	 * @exception Exception
	 */
	public static boolean create(String fileName) throws Exception {
		boolean result = false;
		File tmpFile = new File(fileName);
		try {
			if (!existPath(fileName)) {
				createDir(fileName);
			}
			result = tmpFile.createNewFile();
		} catch (Exception e) {
			System.err.println("Failed to create file: " + e.toString());
			throw new Exception(e.toString());
		} finally {
			tmpFile = null;
		}
		return result;
	}

	/**
	 * creates a directory from filename
	 * 
	 * @param fileName
	 *            the filename of the file to create
	 * @return true if creation success, otherwise false
	 * @exception Exception
	 */
	public static boolean createDirFromFilename(String fileName)
			throws Exception {
		boolean result = false;
		File tmpFile = new File(fileName);
		try {
			fileName = tmpFile.getParent();
			tmpFile = new File(fileName);
			result = tmpFile.mkdir();
		} catch (Exception e) {
			System.err.println("Failed to create directory: " + e.toString());
			throw new Exception(e.toString());
		} finally {
			tmpFile = null;
		}
		return result;
	}

	/**
	 * Given a path string create all the directories in the path. For example,
	 * if the path string is "java/applet", the method will create directory
	 * "java" and then "java/applet" if they don't exist. The file separator
	 * string "/" is platform dependent system property.
	 * 
	 * @param path
	 *            Directory path string.
	 * @return true if creation success, otherwise false
	 */
	public static boolean createDir(String path) {
		boolean result = false;
		if (path == null || path.length() == 0) {
			result = false;
		}
		File dir = new File(path);
		try {
			if (dir.exists()) {
				result = true;
			} else {
				if (dir.mkdirs()) {
					result = true;
				} else {
					System.out.println("unable to create directory");
					result = false;
				}
			}
		} catch (SecurityException exc) {
			System.err.println("Failed to create file: " + exc.toString());
			// exc.printStackTrace();
			result = false;
		} catch (Exception exc) {
			System.err.println("Failed to create file: " + exc.toString());
			// exc.printStackTrace();
			result = false;
		} finally {
			dir = null;
		}
		return result;
	}

	/**
	 * returns true if fileName is a directory, otherwise false
	 * 
	 * @param fileName
	 *            filename to check if it's a directory
	 * @return true or false
	 */
	public static boolean isDirectory(String fileName) {
		boolean result = false;
		File tmpFile = new File(fileName);
		fileName = fileName.replace('/', File.separatorChar);
		String tmpPath = tmpFile.getAbsolutePath();
		if (tmpPath.equalsIgnoreCase(fileName)) {
			result = true;
		}
		tmpPath = tmpFile.getPath();
		if (tmpPath.equalsIgnoreCase(fileName)) {
			result = true;
		}
		tmpFile = null;
		tmpPath = null;
		return result;
	}

	/**
	 * Get the extension of a file.
	 * 
	 * @param f
	 *            the file you want to get the extension from
	 * @return the extension of the file
	 */
	public static String getExtension(File f) {
		String ext = null;
		if (f != null) {
			String s = f.getName();
			int i = s.lastIndexOf('.');

			if (i > 0 && i < s.length() - 1) {
				ext = s.substring(i + 1).toLowerCase();
			}
		}
		return ext;
	}

	/**
	 * Returns the content of a given file as String.
	 * 
	 * @param fileName
	 *            Description of the Parameter
	 * @return The content in a String or empty String, if an error occured.
	 * @exception Exception
	 *                Description of the Exception
	 */
	public static String getFileContent(String fileName) throws Exception {
		String ret = new String(getFileContentAsByteArray(fileName));

		return ret;
	}

	/**
	 * Returns the content of a given file as byte array.
	 * 
	 * @param fileName
	 *            Description of the Parameter
	 * @return The content in a String or empty String, if an error occured.
	 * @exception Exception
	 *                Description of the Exception
	 */
	public static byte[] getFileContentAsByteArray(String fileName)
			throws Exception {
		RandomAccessFile raf = new RandomAccessFile(fileName, "r");
		Long lengthFile;
		byte[] b;
		try {
			raf.seek(0);
			lengthFile = new Long(raf.length());
			b = new byte[lengthFile.intValue()];
			raf.readFully(b);
		} catch (Exception e) {
			System.err.println("failed to copy file: " + e.toString());
			// e.printStackTrace();
			throw new Exception();
		} finally {
			raf.close();
			raf = null;
			lengthFile = null;
		}
		return b;
	}

	/**
	 * Returns the content of a given file as String.
	 * 
	 * @param f
	 *            Description of the Parameter
	 * @return The content in a String or empty String, if an error occured.
	 * @exception Exception
	 *                Description of the Exception
	 */
	public static String getFileContent(File f) throws Exception {
		return getFileContent(f.getAbsolutePath());
	}

	/**
	 * Returns the content of a given file as String.
	 * 
	 * @param f
	 *            Description of the Parameter
	 * @param content
	 *            Description of the Parameter
	 * @exception Exception
	 *                Description of the Exception
	 */
	public static void writeFileContent(File f, String content)
			throws Exception {
		writeFileContent(f.getAbsolutePath(), content);
	}

	/**
	 * Write some String to a file.
	 * 
	 * @param content
	 *            The String.
	 * @param fileName
	 *            Description of the Parameter
	 * @exception Exception
	 *                Description of the Exception
	 */
	/**
	 * Write some String to a file.
	 * 
	 * @param content
	 *            The String.
	 * @param fileName
	 *            Description of the Parameter
	 * @exception Exception
	 *                Description of the Exception
	 */
	public static void writeFileContent(String fileName, String content)
			throws Exception {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(fileName));
			bw.write(content);
		} catch (Exception e) {
			System.err.println("error trying to get file content"
					+ e.toString());
			// e.printStackTrace();
		} finally {
			bw.close();
			bw = null;
		}
	}

	/**
	 * Write some Strings to a file.
	 * 
	 * @param content
	 *            The String.
	 * @param fileName
	 *            Description of the Parameter
	 * @exception Exception
	 *                Description of the Exception
	 */
	public static void writeFileContent(String fileName, Vector<String> content)
			throws Exception {
		PrintWriter bw = null;
		try {
			bw = new PrintWriter(new FileWriter(fileName));
			for (Iterator<String> iterator = content.iterator(); iterator.hasNext();) {
				String line = iterator.next();
				bw.println(line);
			}
		} catch (Exception e) {
			System.err.println("error trying to get file content"
					+ e.toString());
			// e.printStackTrace();
		} finally {
			bw.close();
			bw = null;
		}
	}

	/**
	 * writes a byte array to file
	 * 
	 * @param fileName
	 *            the filename
	 * @param content
	 *            the byte array
	 * @exception Exception
	 */
	public static void writeFileContent(String fileName, byte[] content)
			throws Exception {
		BufferedOutputStream writer = null;
		try {
			writer = new BufferedOutputStream(new FileOutputStream(fileName));
			writer.write(content);
		} catch (Exception e) {
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	/**
	 * returns the content of the specified file as a Vector where each element
	 * of the vector is a String that represents one line of the file
	 * 
	 * @param f
	 *            the file to get the content from
	 * @return a vector with String objects
	 */
	public static Vector<String> getFileLines(File f) {
		Vector<String> ret = new Vector<String>();
		BufferedReader br = null;
		try {
			String line;
			br = new BufferedReader(new FileReader(f));
			while ((line = br.readLine()) != null) {
				ret.add(line);
			}
		} catch (Exception e) {
			System.err.println("error trying to get file content: "
					+ e.toString());
			// e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
					br = null;
				}
			} catch (Exception e) {
			}
		}
		return ret;
		// return reverseVector( ret );
	}

	/**
	 * force an integer as string to take three chars space.
	 * 
	 * @param i
	 * @return
	 */
	public static String forceThreeChars(int i) {
		String s = String.valueOf(i);
		if (s.length() == 1)
			s = "  " + s;
		if (s.length() == 2)
			s = " " + s;
		return s;
	}

	/**
	 * returns the reversed vector of the input vector
	 * 
	 * @param src
	 *            the Vector to reverse
	 * @return the reversed Vector
	 */
	public static Vector<String> reverseVector(final Vector<String> src) {
		Vector<String> ret = new Vector<String>();
		for (int i = src.size(); i > 0; i--) {
			ret.addElement( src.elementAt(i - 1));
		}
		return ret;
	}

	/**
	 * Function converts a byte array to a short array.
	 * 
	 * @param byteData
	 *            The byte array.
	 * @param writeLittleEndian
	 *            If true data is handled as little endian, else as big endian
	 * @return The short array.
	 */
	public static short[] byteToShort(byte[] byteData, boolean writeLittleEndian) {
		short[] data = new short[byteData.length / 2];
		int size = data.length;
		byte lb, hb;
		if (writeLittleEndian) {
			for (int i = 0; i < size; i++) {
				lb = byteData[i * 2];
				hb = byteData[i * 2 + 1];
				data[i] = (short) (((short) hb << 8) | lb & 0xff);
			}
		} else {
			for (int i = 0; i < size; i++) {
				lb = byteData[i * 2];
				hb = byteData[i * 2 + 1];
				data[i] = (short) (((short) lb << 8) | hb & 0xff);
			}

		}
		return data;
	}

	/**
	 * <pre>
	 * Dump the first 1024 byte of an byte-array. Approximation to remove a Riff-wave header.
	 * </pre>
	 * 
	 * @param data
	 * @return data - first 1024 byte or empty array if was shorter than 1024
	 *         byte.
	 */
	public static byte[] dumpFirst1024Byte(byte[] data) {
		byte[] ret = new byte[0];
		if (data.length >= 1024) {
			ret = new byte[data.length - 1024];
			System.arraycopy(data, 1024, ret, 0, data.length - 1024);
		}
		return ret;
	}

	/**
	 * Convert a bytearray of sound data from (mono, 8bit a-law, 8kHz, little
	 * Endian) to (mono, 16 bit PCM, 8kHz, little Endian)
	 * 
	 * @param data
	 *            source-array
	 * @return destination array
	 * @throws Exception
	 */
	public static byte[] convertFrom8bitALawTo16bitPCM(byte[] data)
			throws Exception {
		byte[] ret = null;
		AudioFormat sourceformat = new AudioFormat(AudioFormat.Encoding.ALAW,
				8000,
				// Samplerate
				8, // quantization
				1, // mono
				2, 8000, false // byteorder: little endian
		);
		AudioFormat targetformat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED, 8000,
				// Samplerate
				16, // quantization
				1, // mono
				2, 8000, false // byteorder: little endian
		);

		AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(
				data), sourceformat, data.length);
		ais = AudioSystem.getAudioInputStream(targetformat, ais);
		ret = getBytesFromInputStream(ais);
		// AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new
		// File("D:\\Work\\SymEmoTester\\RecServer\\recordings\\testsample.raw"));
		return ret;
	}

	/**
	 * Convert a bytearray of sound data from (mono, 8bit mu-law, 8kHz, little
	 * Endian) to (mono, 16 bit PCM, 8kHz, little Endian)
	 * 
	 * @param data
	 *            source-array
	 * @return destination array
	 * @throws Exception
	 */
	public static byte[] convertFrom8bitMuLawTo16bitPCM(byte[] data)
			throws Exception {
		byte[] ret = null;
		AudioFormat sourceformat = new AudioFormat(AudioFormat.Encoding.ULAW,
				8000,
				// Samplerate
				8, // quantization
				1, // mono
				2, 8000, false // byteorder: little endian
		);
		AudioFormat targetformat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED, 8000,
				// Samplerate
				16, // quantization
				1, // mono
				2, 8000, false // byteorder: little endian
		);

		AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(
				data), sourceformat, data.length);
		ais = AudioSystem.getAudioInputStream(targetformat, ais);
		ret = getBytesFromInputStream(ais);
		// AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new
		// File("D:\\Work\\SymEmoTester\\RecServer\\recordings\\testsample.raw"));
		return ret;
	}

	/**
	 * Return an absolute path, uses Global.getAppRootPath if neccessary.
	 * 
	 * @param fp
	 *            The (perhaps relative) filePath
	 * @param appRootPath
	 *            the 'appRootPath' (as it is given in the 'global' object)
	 * @return An absoluteFilePath or null, if getAppRootPath == null.
	 */
	public static String getAbsoluteFilePath(String fp, String appRootPath) {
		String retPath = null;
		if (fp.indexOf(":") > -1) {
			retPath = fp.replace('/', File.separatorChar);
		} else {
			if (!fp.startsWith("/")) {
				fp += "/";
			}
			if (appRootPath != null) {
				retPath = appRootPath + fp.replace('/', File.separatorChar);
			} else {
				System.err
						.println("- getAboluteFilePath WARNING: no absolute filepath given and appRootPath == NULL!");
			}
		}
		return retPath;
	}

	/***************************************************************************
	 * Get bytes array from InputStream
	 */
	public static byte[] getBytesFromInputStream(InputStream is)
			throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while (true) {
			byte[] buffer = new byte[100];
			int noOfBytes = is.read(buffer);
			if (noOfBytes == -1) {
				break;
			} else {
				bos.write(buffer, 0, noOfBytes);
			}
		}
		bos.flush();
		bos.close();
		return bos.toByteArray();
	}

	/**
	 * <pre>
	 * copy elements of an array into a vector.
	 * </pre>
	 * 
	 * @param array
	 * @return a new vector.
	 * @throws Exception
	 *             If array was null.
	 */
	public static Vector<Object> arrayToVector(Object[] array) throws Exception {
		Vector<Object> ret = new Vector<Object>();
		for (int i = 0; i < array.length; i++) {
			ret.add(array[i]);
		}
		return ret;
	}
}
