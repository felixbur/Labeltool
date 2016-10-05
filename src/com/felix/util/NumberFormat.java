package com.felix.util;

import java.text.DecimalFormat;
import java.util.Locale;

public class NumberFormat {
	public static String add1000SepMark(int val) {
		DecimalFormat df = (DecimalFormat) DecimalFormat
				.getInstance(Locale.GERMAN);
		df.applyPattern("#,###,###,###");
		return df.format(val);
	}

	/**
	 * Alignes to hunderts and thousands for first string.
	 * 
	 * @param first
	 *            The test String, e.g. 15
	 * @param second
	 *            The reference String, e.g. 30000
	 * @return The result, e.g. 15000
	 */
	public static String alignNumbers(String first, String second) {
		int fLen = first.length();
		int sLen = second.length();

		if (fLen > 2 && sLen < 3) {
			// hunderts
			if (fLen == 3) {
				if (sLen == 1)
					return second + "00";
				// thousands
			} else if (fLen < 7) {
				return second + "000";
				// millions
			} else if (fLen < 9) {
				return second + "000000";
			}
		}
		return second;
	}

	/**
	 * Add a fullstop before the last three 0s
	 * 
	 * @param s
	 *            The input, e.g. 400000
	 * @return The output, e.g. 40.000
	 */
	public static String add1000SepForString(String s) {
		if (s.endsWith("000")) {
			return s.substring(0, s.length() - 3) + ".000";
		}
		return s;
	}

	/**
	 * Resolve combined number/word constructs.
	 * 
	 * @param in
	 *            The input, e.g. "3 hundert"
	 * @return The ouput, e.g. "300"
	 */
	public static String combineMixedStringIntegers(String in) {
		return in.replaceAll("(\\d) hundert", "$100")
				.replaceAll("(\\d) tausend", "$1000")
				.replaceAll("(\\d) millionen", "$1000000");
	}
	public static String stringToNumbers(String in) {
		return in.replaceAll(" hunderttausend", " 100000")
				.replaceAll(" tausend", " 1000").replaceAll(" hundert", " 100")
				.replaceAll(" millionen", " 1000000").replaceAll("zwanziger", " 20");
	}

	public static void main(String[] args) {
		System.out
		.println(stringToNumbers("ab 5 tausend euro 3 hundert tst zwanziger hunderttausend 3 hundert millionen 6 millionen"));
//		System.out
//				.println(combineMixedStringIntegers("ab 5 tausend euro 3 hundert tst 3 hundert millionen 6 millionen"));
		// System.out.println(alignNumbers("23000", "5"));
		// System.out.println(alignNumbers("300", "100"));
		// System.out.println(alignNumbers("3000", "1000"));

		// System.out.println(add1000SepMark(20000));
	}

}
