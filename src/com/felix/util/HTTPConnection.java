package com.felix.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import com.felix.util.logging.LoggerInterface;

public class HTTPConnection {
	public static String getJSonFromURL(String urlS, LoggerInterface logger,
			int urlConnectTimeout) {
		try {
			StringBuilder ret = new StringBuilder(2000);
			if (logger != null && logger.isDebugEnabled())
				logger.debug("asking url: " + urlS);
			URL url = new URL(urlS);
			URLConnection connection = url.openConnection();
//			connection.setRequestProperty("Accept-Charset", "UTF-8");
//			connection.setRequestProperty("Content-Type",
//					"application/x-www-form-urlencoded;charset=utf-8");
			connection.setConnectTimeout(urlConnectTimeout);
			connection.addRequestProperty("Accept", "application/json");
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				ret.append(line).append("\n");
			}
			return ret.toString();
		} catch (Exception e) {
			if (logger != null)
				logger.error(e.getMessage());
			e.printStackTrace();
		}
		return "";
	}
	public static String getStringFromURL(String urlS, LoggerInterface logger,
			int urlConnectTimeout) {
		try {
			StringBuilder ret = new StringBuilder(2000);
			if (logger != null)
				logger.debug("asking url: " + urlS);
			URL url = new URL(urlS);
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("Pragma", "no-cache");
			connection.setRequestProperty("Cache-Control", "no-cache");
//			connection.setRequestProperty("Accept-Charset", "UTF-8");
//			connection.setRequestProperty("Content-Type",
//					"application/x-www-form-urlencoded;charset=utf-8");
			connection.setConnectTimeout(urlConnectTimeout);
//			connection.addRequestProperty("Accept", "application/json");
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				ret.append(line).append("\n");
			}
			in.close();
			return ret.toString();
		} catch (Exception e) {
			if (logger != null)
				logger.error(e.getMessage());
			e.printStackTrace();
		}
		return "";
	}

}
