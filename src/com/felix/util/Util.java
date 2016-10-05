package com.felix.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;

import com.felix.util.logging.Log4JLogger;
import com.felix.util.logging.LoggerInterface;

/**
 * Several helper methods.
 * 
 * @author felix
 * 
 */
public class Util {
	public final static int PROGRESS_MODULO = 10;

	/**
	 * Test if an object is null or doesnt contain values.
	 * 
	 * @param o
	 *            The object, e.g. ""
	 * @return The outcome, e.g. true.
	 */
	public static final boolean isEmpty(Object o) {
		if (null == o)
			return true;
		if (o instanceof String && ((String) o).length() == 0)
			return true;
		if (o instanceof Vector<?> && ((Vector<?>) o).size() == 0)
			return true;
		if (o instanceof Map && ((Map) o).size() == 0)
			return true;

		return false;
	}

	/**
	 * Print a dot to stdout once in a while.
	 * 
	 */
	public static void printProgress(int counter) {
		if (counter % PROGRESS_MODULO == 0)
			System.out.print(".");
	}

	/**
	 * Test if some String is contained in an array of Strings.
	 * 
	 * @param key
	 *            The key, e.g. "a".
	 * @param array
	 *            The array, e.g. {"A", "b" "c"}
	 * @return The result, e.g. "true".
	 */
	public static boolean isStringContainedInArray(String key, String array[]) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].compareTo(key) == 0)
				return true;
		}
		return false;
	}

	/**
	 * Get an array of unique random integers distributed between 0 and num.
	 * 
	 * @param num
	 *            The max integer.
	 * @return The array.
	 */
	public static int[] getRandomInts(int num) {
		int[] a = new int[num];
		for (int i = num - 1; i >= 0; --i) {
			a[i] = i;
		}
		return getRandomIntArray(a);
	}

	/**
	 * Get an array of randomized integers.
	 * 
	 * @param src
	 *            The ordered integer array.
	 * @return The randomized array.
	 */

	public static int[] getRandomIntArray(int[] src) {
		if (src == null) {
			return null;
		}

		Random rd = new Random();
		int[] tmp = new int[src.length];

		int num = src.length;

		int index;

		for (int i = 0; i < src.length; i++) {

			index = Math.abs(rd.nextInt()) % num;
			tmp[i] = src[index];
			src[index] = src[num - 1];
			num--;
		}
		return tmp;
	}

	public static int getRandomInteger(int max) {
		return new Random().nextInt(max);
	}

	/**
	 * Return a random alphanumeric identifer
	 * 
	 * @param length
	 *            The string length.
	 * @return The identifier.
	 */
	public static String getRandomId(int length) {
		Random random = new Random();
		return UUID.randomUUID().toString().replace("-", "")
				.substring(0, length);
	}

	/**
	 * Print stacktrace and report to logger.
	 * 
	 * @param msg
	 *            The message.
	 * @param logger
	 *            The logger.
	 */
	public static void reportError(Exception e, Log4JLogger logger) {
		e.printStackTrace();
		logger.error(e.getMessage());
	}

	/**
	 * Print msg to system.err and logger.error
	 * 
	 * @param msg
	 *            The message.
	 * @param logger
	 *            The logger.
	 */
	public static void errorToSystem(String msg, Log4JLogger logger) {
		System.err.println(msg);
		logger.error(msg);
	}

	/**
	 * Return a subset of an array.
	 * 
	 * @param in
	 * @param start
	 * @param end
	 * @return
	 */
	public static Object[] subArray(Object in[], int start, int end) {
		Object[] ret = new Object[end - start + 1];
		int j = 0;
		for (int i = start; i <= end; i++) {
			ret[j++] = in[i];
		}
		return ret;
	}

	/**
	 * Return a subset of an array.
	 * 
	 * @param in
	 * @param start
	 * @param end
	 * @return
	 */
	public static String[] subStringArray(String in[], int start, int end) {
		String[] ret = new String[end - start];
		int j = 0;
		for (int i = start; i < end; i++) {
			ret[j++] = in[i];
		}
		return ret;
	}

	/**
	 * Report an error (print stack and log).
	 * 
	 * @param e
	 * @param logger
	 */
	public static void errorOut(Exception e, LoggerInterface logger) {
		logger.error(e.getMessage());
		e.printStackTrace();
	}

	/**
	 * Return array string values as blank separated string.
	 * 
	 * @param in
	 * @return
	 */
	public static String arrayToString(String in[]) {
		if (in == null)
			return null;
		String ret = "";
		for (int i = 0; i < in.length; i++) {
			ret += in[i] + " ";
		}
		return ret.trim();
	}

	/**
	 * Wait for some milliseconds.
	 * 
	 * @param millis
	 */
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Restrain double to maximum two after comma numbers.
	 * 
	 * @param d
	 * @return
	 */
	public static double cutDouble(double d) {
		return Math.round(d * 100) / 100.0;
	}

	/**
	 * Restrain double to maximum one after comma numbers.
	 * 
	 * @param d
	 * @return
	 */
	public static double cutDoubleToOne(double d) {
		return Math.round(d * 10) / 10.0;
	}

	/**
	 * Restrain double to maximum two after comma numbers.
	 * 
	 * @param d
	 * @return
	 */
	public static double cutDoubleToTwo(double d) {
		return Math.round(d * 100) / 100.0;
	}

	/**
	 * Restrain double to maximum three after comma numbers.
	 * 
	 * @param d
	 * @return
	 */
	public static double cutDoubleToThree(double d) {
		return Math.round(d * 1000) / 1000.0;
	}

	/**
	 * Restrain double to maximum four after comma numbers.
	 * 
	 * @param d
	 * @return
	 */
	public static double cutDoubleToFour(double d) {
		return Math.round(d * 10000) / 10000.0;
	}

	/**
	 * Get percentage value of fraction as integer, e.g. 1/2 = 50.
	 * 
	 * @param part
	 * @param whole
	 * @return
	 */
	public static int percentage(int part, int whole) {
		return (int) (((double) part * 100.0) / (double) whole);
	}

	/**
	 * Print to system.out.
	 * 
	 * @param msg
	 * @param newLine
	 */
	public static void printOut(String msg, boolean newLine) {
		System.out.print(msg);
		if (newLine) {
			System.out.println();
		}
	}


	/**
	 * Execute a command on the system and report on logger.
	 * 
	 * @param cmd
	 * @param logger
	 * @return Exit code.
	 * @throws Exception
	 */
	public static int execCmd(String cmd, LoggerInterface logger)
			throws Exception {
		logger.debug("executing: " + cmd);
		Process proc = Runtime.getRuntime().exec(cmd);
		// any error message?
		StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(),
				"ERROR", logger);
		StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(),
				"OUTPUT", logger);
		errorGobbler.start();
		outputGobbler.start();
		return proc.waitFor();
	}

	/**
	 * Execute a command on the system.
	 * 
	 * @param cmd
	 * @return Output of the command.
	 * @throws Exception
	 */
	public static String execCmd(String cmd) throws Exception {
		Process proc = Runtime.getRuntime().exec(cmd);
		// any error message?
		StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream());
		StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream());
		errorGobbler.start();
		outputGobbler.start();
		return outputGobbler.getOutput() + "\n" + errorGobbler.getOutput();
	}

	private static class StreamGobbler extends Thread {
		InputStream is;
		String type;
		LoggerInterface _logger = null;
		String _ret = "";

		public StreamGobbler(InputStream is) {
			this.is = is;
		}

		public StreamGobbler(InputStream is, String type, LoggerInterface logger) {
			this.is = is;
			this.type = type;
			_logger = logger;
		}

		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				if (_logger != null) {
					while ((line = br.readLine()) != null)
						if (type.compareTo("ERROR") == 0) {
							_logger.error(line);
						} else {
							_logger.debug(line);

						}
				} else {
					while ((line = br.readLine()) != null)
						_ret += line + "\n";
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		public String getOutput() {
			return _ret;
		}
	}

	/**
	 * Force an integer as string to take at least three chars space.
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
	 * Copy elements of an array into a vector.
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

	/**
	 * Print elements of a Vector blank-separated to a Printstream.
	 * 
	 * @param vec
	 *            The vector.
	 * @param out
	 *            The printStream.
	 */
	public static void printVec(Vector<String> vec, PrintStream out) {
		for (Iterator<String> iter = vec.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			out.print(element.toString() + " ");
		}
	}

	/**
	 * Print all elements of a vector in a row.
	 * 
	 * @param vec
	 *            The vector of elements.
	 * @return The output of toString() method in a row.
	 */
	public static String printVec(Vector<String> vec) {
		String ret = "";
		for (Iterator<String> iter = vec.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			ret += element.toString() + " ";
		}
		return ret;
	}

	/**
	 * Use all toString() methods linewise on a vector.
	 * 
	 * @param in
	 *            The Vector
	 * @param out
	 *            The print stream, e.g. System.out.
	 */
	public static void printToString(Vector<Object> in, PrintStream out) {
		for (Iterator<Object> iter = in.iterator(); iter.hasNext();) {
			Object element = (Object) iter.next();
			out.println(element.toString());
		}
	}

	public static void main(String[] args) {
		System.out.println(getRandomInteger(6));
	}
}
