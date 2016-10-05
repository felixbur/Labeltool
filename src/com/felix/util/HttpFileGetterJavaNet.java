package com.felix.util;

import java.io.BufferedInputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.codec.binary.Base64;


public class HttpFileGetterJavaNet {
	public static final String DTEP_URL = "https://pm-prepro.t-online.de/glf/remrecDelivery.zip";
	public static final String DTEP_USER = "tlabs";
	public static final String DTEP_PW = "TLabS_glf75";
	public static final String TMPFILENAME = "tmp.zip";

	public static void fetchURLandExtract(String urlString, String user,
			String pass, String dirname, boolean extractOnly, boolean output,
			String proxyName, int proxyPort) {

		try {
			URL url;
			URLConnection urlConn;
			BufferedInputStream input;

			// Properties sysProperties = System.getProperties();
			// sysProperties.put("proxyHost", "proxy.cyberway.com.sg");
			// sysProperties.put("proxyPort", "8080");
			// sysProperties.put("proxySet", "true");
			if (!extractOnly) {
				url = new URL(urlString);
				if (proxyName != null) {
					Proxy proxy = new Proxy(Proxy.Type.HTTP,
							new InetSocketAddress(proxyName, proxyPort));
					urlConn = url.openConnection(proxy);
				} else {
					urlConn = url.openConnection();
				}
				String userPassword = user + ":" + pass;
				String encoding = Base64.encodeBase64String(userPassword.getBytes());
				urlConn.setRequestProperty("Authorization", "Basic " + encoding);
				urlConn.setUseCaches(false);
				urlConn.setConnectTimeout(100000);

				input = new BufferedInputStream(urlConn.getInputStream());
				FileOutputStream fos = new FileOutputStream(TMPFILENAME);
				if (output)
					System.out.println("getting zip file");
				byte[] buffer = new byte[65535];
				int len = 0;
				while ((len = input.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				input.close();
				fos.close();
			}
			if (output)
				System.out.println("got zip file, unzipping it");

			ZipFile zipFile = new ZipFile(TMPFILENAME);

			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			FileUtil.createDir(dirname);
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();

				if (entry.isDirectory()) {
					// // Assume directories are stored parents first then
					// // children.
					// System.out.println("Extracting directory: "
					// + entry.getName());
					// // This is not robust, just for demonstration purposes.
					// FileUtil.createDir(dirname+"/"+entry.getName());
					continue;
				}
				if (output)
					System.out.println("Extracting file: " + entry.getName());
				try {
					String path = new File(dirname + "/" + entry.getName())
							.getParent();
					FileUtil.createDir(path);
				} catch (Exception e) {
					// TODO: handle exception
				}
				try {
					FileUtil.writeFileContent(dirname + "/" + entry.getName(),
							zipFile.getInputStream(entry));
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			zipFile.close();
			if (output)
				System.out.println("done extracting zip file");

		} catch (Exception e) {
			System.err.println("Error while trying: "+urlString);
			e.printStackTrace();
		}
	}

	public static void fetchURLToFile(String urlString, String user,
			String pass, String filename, String proxyName, int proxyPort) {

		try {
			URL url;
			URLConnection urlConn;
			BufferedInputStream input;

			// Properties sysProperties = System.getProperties();
			// sysProperties.put("proxyHost", "proxy.cyberway.com.sg");
			// sysProperties.put("proxyPort", "8080");
			// sysProperties.put("proxySet", "true");
			url = new URL(urlString);
			if (proxyName != null) {
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
						proxyName, proxyPort));
				urlConn = url.openConnection(proxy);
			} else {
				urlConn = url.openConnection();
			}
			if (user != null) {
				String userPassword = user + ":" + pass;
				String encoding = Base64.encodeBase64String(userPassword.getBytes());
				urlConn.setRequestProperty("Authorization", "Basic " + encoding);
			}
			urlConn.setUseCaches(false);
			urlConn.setConnectTimeout(100000);

			input = new BufferedInputStream(urlConn.getInputStream());
			FileOutputStream fos = new FileOutputStream(filename);
			byte[] buffer = new byte[65535];
			int len = 0;
			while ((len = input.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
			fos.close();
			input.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String fetchURLToString(String urlString, String user,
			String pass, String proxyName, int proxyPort) {
		String ret = "";

		try {
			URL url;
			URLConnection urlConn;
			url = new URL(urlString);
			if (StringUtil.isFilled(proxyName)) {
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
						proxyName, proxyPort));
				urlConn = url.openConnection(proxy);
			} else {
				urlConn = url.openConnection();
			}
			if (user != null) {
				String userPassword = user + ":" + pass;
				String encoding = Base64.encodeBase64String(userPassword.getBytes());
				urlConn.setRequestProperty("Authorization", "Basic " + encoding);
			}
			urlConn.setUseCaches(false);
			urlConn.setConnectTimeout(100000);

			// Problem, wenn mitten in einem Multibyte Char read(buffer) zu Ende!
//			BufferedInputStream input = new BufferedInputStream(urlConn.getInputStream());
//			byte[] buffer = new byte[65535];
//			int len = 0;
//			while ((len = input.read(buffer)) > 0) {
//				ret += new String (buffer, 0, len);
//			}
//			input.close();
			
			BufferedReader br = new BufferedReader( 
					new InputStreamReader( urlConn.getInputStream() ) );
			char[] buffer = new char[65535];
			StringBuffer sb = new StringBuffer(1000);
			int len = 0;
			while ( (len = br.read(buffer)) > 0 ) {
				sb.append( buffer, 0, len );
			}
			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public static void main(String args[]) {
		HttpFileGetterJavaNet.fetchURLandExtract(DTEP_URL, DTEP_USER, DTEP_PW,
				"./remrec", false, false, null, 0);
	}
}