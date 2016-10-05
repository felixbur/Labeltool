package com.felix.util;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateTimeUtil {

	/**
	 * Gettin a formatted time string, e.g. "MMMM, yyyy"
	 * 
	 * Symbol Meaning Presentation Examples ------ ------- ------------ -------
	 * G era text AD C century of era (>=0) number 20 Y year of era (>=0) year
	 * 1996
	 * 
	 * x weekyear year 1996 w week of weekyear number 27 e day of week number 2
	 * E day of week text Tuesday; Tue
	 * 
	 * y year year 1996 D day of year number 189 M month of year month July;
	 * Jul; 07 d day of month number 10
	 * 
	 * a halfday of day text PM K hour of halfday (0~11) number 0 h clockhour of
	 * halfday (1~12) number 12
	 * 
	 * H hour of day (0~23) number 0 k clockhour of day (1~24) number 24 m
	 * minute of hour number 30 s second of minute number 55 S fraction of
	 * second number 978
	 * 
	 * z time zone text Pacific Standard Time; PST Z time zone offset/id zone
	 * -0800; -08:00; America/Los_Angeles
	 * 
	 * ' escape for text delimiter '' single quote literal '
	 * 
	 * @param format
	 * @return
	 */
	public static String getFormattedNowDate(String format) {
		DateTime dt = new DateTime();
		DateTimeFormatter fmt = DateTimeFormat.forPattern(format);
		return fmt.print(dt);
	}

	/**
	 * Get a future Date of a weekday.
	 * 
	 * @param weekday
	 *            E.g. DateTimeConstants.THURSDAY
	 * @param format
	 *            e.g. "dd.MM.yyyy"
	 * @return
	 */
	public static String calcNextWeekday(int weekday, String format) {
		LocalDate d = new LocalDate();
		if (d.getDayOfWeek() < weekday)
			d = d.withDayOfWeek(weekday);
		else
			d = d.plusWeeks(1).withDayOfWeek(weekday);
		DateTimeFormatter fmt = DateTimeFormat.forPattern(format);
		return fmt.print(d);
	}

	/**
	 * Return a future Date in a certain format.
	 * 
	 * @param format
	 *            The format, e.g. "yyyy-MM-dd hh:mm:ss"
	 * @param inc
	 *            Number of days to increase.
	 * @return
	 */
	public static String getFormattedFutureDate(String format, int inc) {
		DateTime dt = new DateTime();
		dt = dt.plusDays(inc);
		DateTimeFormatter fmt = DateTimeFormat.forPattern(format);
		return fmt.print(dt);
	}

	/**
	 * Return a new timesString,in form "hh:mm", but maximally "23:59"
	 * 
	 * @param time
	 * @param hours
	 * @return
	 */
	public static String addHoursToTime(String time, int hours) {
		String[] parts = time.split(":");
		String hourS = parts[0];
		String minS = parts[1];
		int newHours = Integer.parseInt(hourS) + hours;
		if (newHours > 24) {
			return "23:59";
		}
		// newHours = newHours - 24;
		String newHS = String.valueOf(newHours);
		if (newHS.length() < 2)
			newHS = "0" + newHS;
		return newHS + ":" + minS;
	}

	/**
	 * Return quarter of an hour begfore a time, e.g. "23:45" for "24:00"
	 * 
	 * @param time
	 * @return
	 */
	public static String getMinutesEarlier(String time, int minutes) {
		String[] parts = time.split(":");
		String hourS = parts[0];
		String minS = parts[1];
		int newHours = 0;
		int newMins = Integer.parseInt(minS) - minutes;
		if (newMins < 0) {
			newHours = Integer.parseInt(hourS) - 1;
			newMins = 60 + newMins;
		} else {
			newHours = Integer.parseInt(hourS);
		}
		if (newHours < 0) {
			return "00:00";
		}
		return ensureTwoDigits(newHours) + ":" + ensureTwoDigits(newMins);
	}

	/**
	 * Return a date representation in the form DD.MM.YYYY-HH:MM:SS
	 * 
	 * @return
	 */
	public static String getDate() {
		GregorianCalendar g = new GregorianCalendar();
		int year = g.get(Calendar.YEAR);
		int month = g.get(Calendar.MONTH) + 1;
		int day = g.get(Calendar.DAY_OF_MONTH);
		int hour = g.get(Calendar.HOUR_OF_DAY);
		int min = g.get(Calendar.MINUTE);
		int sec = g.get(Calendar.SECOND);
		String mS = String.valueOf(month);
		String dS = String.valueOf(day);
		String hS = String.valueOf(hour);
		String minS = String.valueOf(min);
		String sS = String.valueOf(sec);
		mS = mS.length() < 2 ? "0" + mS : mS;
		dS = dS.length() < 2 ? "0" + dS : dS;
		hS = hS.length() < 2 ? "0" + hS : hS;
		minS = minS.length() < 2 ? "0" + minS : minS;
		sS = sS.length() < 2 ? "0" + sS : sS;
		return dS + "." + mS + "." + year + "-" + hS + ":" + minS + ":" + sS;
	}

	/**
	 * Get a date string in local format for a specific date.
	 * 
	 * @param date
	 *            The date in millis.
	 * @return The formatted date string.
	 */
	public static String getDate(long date) {
		GregorianCalendar g = new GregorianCalendar();
		g.setTimeInMillis(date);
		int year = g.get(Calendar.YEAR);
		int month = g.get(Calendar.MONTH) + 1;
		int day = g.get(Calendar.DAY_OF_MONTH);
		String mS = String.valueOf(month);
		String dS = String.valueOf(day);
		mS = mS.length() < 2 ? "0" + mS : mS;
		dS = dS.length() < 2 ? "0" + dS : dS;
		return dS + "." + mS + "." + year;
	}

	/**
	 * Get a date string in local format for a specific date.
	 * 
	 * @param date
	 *            The date in millis.
	 * @return The formatted date string.
	 */
	public static String getTime(long date) {
		GregorianCalendar g = new GregorianCalendar();
		g.setTimeInMillis(date);
		int hour = g.get(Calendar.HOUR_OF_DAY);
		int min = g.get(Calendar.MINUTE);
		int sec = g.get(Calendar.SECOND);
		String hS = String.valueOf(hour);
		String minS = String.valueOf(min);
		String sS = String.valueOf(sec);
		hS = hS.length() < 2 ? "0" + hS : hS;
		minS = minS.length() < 2 ? "0" + minS : minS;
		sS = sS.length() < 2 ? "0" + sS : sS;
		return hS + ":" + minS + ":" + sS;
	}

	/**
	 * 
	 * Get the current time.
	 * 
	 * @return The formatted time string, e.g. hh:mm:ss..
	 */
	public static String getTime() {
		GregorianCalendar g = new GregorianCalendar();
		int hour = g.get(Calendar.HOUR_OF_DAY);
		int min = g.get(Calendar.MINUTE);
		int sec = g.get(Calendar.SECOND);
		String hS = String.valueOf(hour);
		String minS = String.valueOf(min);
		String sS = String.valueOf(sec);
		hS = hS.length() < 2 ? "0" + hS : hS;
		minS = minS.length() < 2 ? "0" + minS : minS;
		sS = sS.length() < 2 ? "0" + sS : sS;
		return hS + ":" + minS + ":" + sS;
	}

	/**
	 * Get a date string in local format for a specific date.
	 * 
	 * @param date
	 *            The date in millis.
	 * @return The formatted date string.
	 */
	public static String getDateAndTime(long date) {
		GregorianCalendar g = new GregorianCalendar();
		g.setTimeInMillis(date);
		int year = g.get(Calendar.YEAR);
		int month = g.get(Calendar.MONTH) + 1;
		int day = g.get(Calendar.DAY_OF_MONTH);
		int hour = g.get(Calendar.HOUR_OF_DAY);
		int min = g.get(Calendar.MINUTE);
		int sec = g.get(Calendar.SECOND);
		String mS = String.valueOf(month);
		String dS = String.valueOf(day);
		String hS = String.valueOf(hour);
		String minS = String.valueOf(min);
		String sS = String.valueOf(sec);
		mS = mS.length() < 2 ? "0" + mS : mS;
		dS = dS.length() < 2 ? "0" + dS : dS;
		hS = hS.length() < 2 ? "0" + hS : hS;
		minS = minS.length() < 2 ? "0" + minS : minS;
		sS = sS.length() < 2 ? "0" + sS : sS;
		return dS + "." + mS + "." + year + "-" + hS + ":" + minS + ":" + sS;
	}

	/**
	 * Get the date in format dd.mm.yyyy hh.mm"
	 * 
	 * @param date
	 *            The date to be formatted
	 * @return
	 */
	public static String getDateAndTimeNoSeconds(long date) {
		GregorianCalendar g = new GregorianCalendar();
		g.setTimeInMillis(date);
		int year = g.get(Calendar.YEAR);
		int month = g.get(Calendar.MONTH) + 1;
		int day = g.get(Calendar.DAY_OF_MONTH);
		int hour = g.get(Calendar.HOUR_OF_DAY);
		int min = g.get(Calendar.MINUTE);
		String mS = String.valueOf(month);
		String dS = String.valueOf(day);
		String hS = String.valueOf(hour);
		String minS = String.valueOf(min);
		mS = mS.length() < 2 ? "0" + mS : mS;
		dS = dS.length() < 2 ? "0" + dS : dS;
		hS = hS.length() < 2 ? "0" + hS : hS;
		minS = minS.length() < 2 ? "0" + minS : minS;
		return dS + "." + mS + "." + year + " " + hS + ":" + minS;
	}

	/**
	 * Return today as String dd.mm.yyyy
	 * 
	 * @return
	 */
	public static String getTodayDayString() {
		GregorianCalendar calendar = new GregorianCalendar();
		return getDateDay(calendar.getTimeInMillis());
	}

	/**
	 * Return a date as String dd.mm.yyyy
	 * 
	 * @param date
	 * @return
	 */
	public static String getDateDay(long date) {
		GregorianCalendar g = new GregorianCalendar();
		g.setTimeInMillis(date);
		int year = g.get(Calendar.YEAR);
		int month = g.get(Calendar.MONTH) + 1;
		int day = g.get(Calendar.DAY_OF_MONTH);
		String mS = String.valueOf(month);
		String dS = String.valueOf(day);
		mS = mS.length() < 2 ? "0" + mS : mS;
		dS = dS.length() < 2 ? "0" + dS : dS;
		return dS + "." + mS + "." + year;
	}

	/**
	 * Get the date in format dd.mm.yyyy hh.mm"
	 * 
	 * @return
	 */
	public static String getDateAndTimeNoSeconds() {
		return getDateAndTimeNoSeconds(new GregorianCalendar()
				.getTimeInMillis());
	}

	public static String getFutureDate(int daysPlusToday) {
		GregorianCalendar g = new GregorianCalendar();
		g.add(Calendar.DAY_OF_MONTH, daysPlusToday);
		return getDateAndTimeNoSeconds(g.getTimeInMillis());
	}

	/**
	 * Return date string without time for day in future.
	 * 
	 * @param daysPlusToday
	 *            The increase in days.
	 * @return E-g "23.12.2012"
	 */
	public static String getFutureDay(int daysPlusToday) {
		GregorianCalendar g = new GregorianCalendar();
		g.add(Calendar.DAY_OF_MONTH, daysPlusToday);
		return getDateDay(g.getTimeInMillis());
	}

	/**
	 * Return a date representation in the form YYYY.MM.DD-HH:MM:SS
	 * 
	 * @return
	 */
	public static String getDateSortable() {
		GregorianCalendar g = new GregorianCalendar();
		int year = g.get(Calendar.YEAR);
		int month = g.get(Calendar.MONTH) + 1;
		int day = g.get(Calendar.DAY_OF_MONTH);
		int hour = g.get(Calendar.HOUR_OF_DAY);
		int min = g.get(Calendar.MINUTE);
		int sec = g.get(Calendar.SECOND);
		String mS = String.valueOf(month);
		String dS = String.valueOf(day);
		String hS = String.valueOf(hour);
		String minS = String.valueOf(min);
		String sS = String.valueOf(sec);
		mS = mS.length() < 2 ? "0" + mS : mS;
		dS = dS.length() < 2 ? "0" + dS : dS;
		hS = hS.length() < 2 ? "0" + hS : hS;
		minS = minS.length() < 2 ? "0" + minS : minS;
		sS = sS.length() < 2 ? "0" + sS : sS;
		return year + "." + mS + "." + dS + "-" + hS + ":" + minS + ":" + sS;
	}

	/**
	 * Return a date representation in the form YYYY.MM.DD-HH:MM:SS usable as
	 * filename
	 * 
	 * @return
	 */
	public static String getDateSortableName() {
		GregorianCalendar g = new GregorianCalendar();
		int year = g.get(Calendar.YEAR);
		int month = g.get(Calendar.MONTH) + 1;
		int day = g.get(Calendar.DAY_OF_MONTH);
		int hour = g.get(Calendar.HOUR_OF_DAY);
		int min = g.get(Calendar.MINUTE);
		int sec = g.get(Calendar.SECOND);
		String mS = String.valueOf(month);
		String dS = String.valueOf(day);
		String hS = String.valueOf(hour);
		String minS = String.valueOf(min);
		String sS = String.valueOf(sec);
		mS = mS.length() < 2 ? "0" + mS : mS;
		dS = dS.length() < 2 ? "0" + dS : dS;
		hS = hS.length() < 2 ? "0" + hS : hS;
		minS = minS.length() < 2 ? "0" + minS : minS;
		sS = sS.length() < 2 ? "0" + sS : sS;
		return year + "." + mS + "." + dS + "-" + hS + "." + minS + "." + sS;
	}

	/**
	 * Get a String derived from current date and time usable as unique
	 * filename. Format "YYYY.MM.DD-HH.MM.SS" .
	 * 
	 * @return The String.
	 */
	public static String getDateName() {
		GregorianCalendar g = new GregorianCalendar();
		int year = g.get(Calendar.YEAR);
		int month = g.get(Calendar.MONTH) + 1;
		int day = g.get(Calendar.DAY_OF_MONTH);
		int hour = g.get(Calendar.HOUR_OF_DAY);
		int min = g.get(Calendar.MINUTE);
		int sec = g.get(Calendar.SECOND);
		String mS = String.valueOf(month);
		String dS = String.valueOf(day);
		String hS = String.valueOf(hour);
		String minS = String.valueOf(min);
		String sS = String.valueOf(sec);
		mS = mS.length() < 2 ? "0" + mS : mS;
		dS = dS.length() < 2 ? "0" + dS : dS;
		hS = hS.length() < 2 ? "0" + hS : hS;
		minS = minS.length() < 2 ? "0" + minS : minS;
		sS = sS.length() < 2 ? "0" + sS : sS;
		return year + "." + mS + "." + dS + "-" + hS + "." + minS + "." + sS;
	}

	public static void main(String[] args) {
		System.out
				.println(calcNextWeekday(DateTimeConstants.THURSDAY, "dd.MM.yyyy"));

		// System.out.println(addHoursToTime("05:00", 2));
		// System.out.println(addHoursToTime("24:00", 2));
		// System.out.println(getMinutesEarlier("24:00", 25));
		// System.out.println(getMinutesEarlier("06:30", 25));

	}

	/**
	 * Ensure that a string has two slots, e.g. 5 -> "05".
	 * 
	 * @param num
	 * @return
	 */
	public static String ensureTwoDigits(int num) {
		String digitString = String.valueOf(num);
		if (digitString.length() < 2)
			return "0" + digitString;
		return digitString;
	}
}
