package com.boxhead.android.morselullabies;

import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

@SuppressLint({ "SimpleDateFormat", "TrulyRandom" })
final class MorseTextProducer {
	private class MorseTextDownloadThread extends Thread {
		private int idx;

		MorseTextDownloadThread(int idx) {
			this.idx = idx;
		}

		public void run() {
			while (urlsToDownload.size() > 0) {
				try {
					URL u = new URL(urlsToDownload.remove(0));
					URLConnection conn = u.openConnection();
					conn.setConnectTimeout(Constants.MORSE_TEXT_TIMEOUT);
					conn.setReadTimeout(Constants.MORSE_TEXT_TIMEOUT);
					DocumentBuilderFactory factory = DocumentBuilderFactory
							.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();
					Document document = builder.parse(conn.getInputStream());
					Element rootElem = document.getDocumentElement();
					NodeList elems = rootElem.getElementsByTagName("item");

					outer: for (int x = 0; x < elems.getLength(); x++) {
						Element elem = (Element) elems.item(x);

						Element titleElem = (Element) elem
								.getElementsByTagName("title").item(0);

						if (titleElem == null)
							continue;

						if (titleElem.getFirstChild() == null)
							continue;

						String title = collapseSpaces(removeHTMLTags(titleElem
								.getFirstChild().getNodeValue()));

						if (title.endsWith(".") == false
								&& title.endsWith("?") == false)
							title = title + ".";

						for (String attrName : NODE_NAMES)
							for (SimpleDateFormat sdf : DATE_FORMATS)
								if (skipBecauseOfDate(cutoffDate, elem,
										attrName, sdf))
									break outer;

						boolean found = false;
						for (String attrName : NODE_NAMES) {
							String v = getValue(elem, attrName);
							if (v != null) {
								found = true;
								break;
							}
						}

						if (found == true) 
							addText(title);
					}
				} catch (Exception e) {
					Log.e(getClass().getName(), "DownloadThread.run()", e);
				}
			}

			downloadThreads[idx] = null;
		}
	}
	private static final String END_INDEX = "endIndex";
	private static final String READ_INDEX = "readIndex";

	private static String[] NODE_NAMES = new String[] { "pubDate", "dc:date",
			"a10:updated" };

	private static SimpleDateFormat[] DATE_FORMATS = new SimpleDateFormat[] {
			new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z"),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"),
			new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"),
			new SimpleDateFormat("dd MMM yyyy HH:mm:ss z"),
			new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a z") };

	static {
		try {
			System.setProperty("sun.net.client.defaultConnectTimeout",
					String.valueOf(Constants.MORSE_TEXT_TIMEOUT));
			System.setProperty("sun.net.client.defaultReadTimeout",
					String.valueOf(Constants.MORSE_TEXT_TIMEOUT));
		} catch (Exception e) {
			Log.e(MorseTextProducer.class.getName(), "static", e);
		}
	}
	private static String collapseSpaces(String in) {
		while (in.contains("  "))
			in = in.replaceAll("  ", " ");

		return in;
	}
	private static String getValue(Element elem, String tagName) {
		NodeList nl = elem.getElementsByTagName(tagName);
		if (nl.getLength() == 0)
			return null;

		return nl.item(0).getFirstChild().getNodeValue();
	}
	private static String removeHTMLTags(String in) {
		StringBuffer sb = new StringBuffer();

		boolean skipping = false;

		for (int i = 0; i < in.length(); i++) {
			char ch = in.charAt(i);

			if (skipping == false && (ch == '<' || ch == '&'))
				skipping = true;
			else if (skipping == true && (ch == '>' || ch == ';'))
				skipping = false;
			else if (skipping == false) {
				if (Character.isLetterOrDigit(ch) || Character.isWhitespace(ch)
						|| ch == '?' || ch == '.' || ch == ',')
					if (ch != '\n')
						sb.append(Character.toUpperCase(ch));
			}
		}

		return sb.toString().trim();
	}

	private static boolean skipBecauseOfDate(Date cutoffDate, Element elem,
			String tagName, SimpleDateFormat sdf) {
		NodeList nl = elem.getElementsByTagName(tagName);
		if (nl.getLength() == 0)
			return false;

		String value = nl.item(0).getFirstChild().getNodeValue();

		try {
			Date parsedDate = sdf.parse(value);
			return parsedDate.before(cutoffDate);
		} catch (ParseException pe) {
			return false;
		}
	}

	private SharedPreferences prefs;

	private List<String> urlsToDownload = new CopyOnWriteArrayList<String>();

	private Date cutoffDate;

	private MorseTextDownloadThread[] downloadThreads = new MorseTextDownloadThread[Constants.MORSE_TEXT_DOWNLOAD_THREAD_COUNT];

	@SuppressLint("TrulyRandom")
	private Random rnd = new SecureRandom();

	MorseTextProducer(Context ctx) {
		this.prefs = ctx.getSharedPreferences(getClass().getName(),
				Context.MODE_PRIVATE);
	}

	private void addText(String text) {
		synchronized (this.prefs) {
			int endIndex = this.prefs.getInt(END_INDEX, 0);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(String.valueOf(endIndex), text);
			editor.putInt(END_INDEX, endIndex + 1);
			editor.commit();
		}
	}

	private boolean isDownloading() {
		synchronized (downloadThreads) {
			for (int i = 0; i < downloadThreads.length; i++)
				if (downloadThreads[i] != null)
					return true;

			return false;
		}
	}

	private void parseOpmlFile() {
		try {
			Document doc = DocumentBuilderFactory
					.newInstance()
					.newDocumentBuilder()
					.parse(getClass().getResourceAsStream(
							Constants.MORSE_TEXT_OPML_FILE));

			NodeList list = doc.getElementsByTagName("outline");

			Set<String> urlSet = new HashSet<String>();
			int m = 0;

			while (m < list.getLength()) {
				Element elem = (Element) list.item(m++);

				String xmlUrl = elem.getAttribute("xmlUrl");

				if (xmlUrl != null && xmlUrl.trim().length() > 0)
					urlSet.add(xmlUrl);
			}

			List<RandomUrl> tempList = new ArrayList<RandomUrl>();
			for (String url : urlSet)
				tempList.add(new RandomUrl(url));

			Collections.sort(tempList);

			for (RandomUrl url : tempList)
				urlsToDownload.add(url.getUrl());

		} catch (Exception e) {
			throw new RuntimeException("could not parse opml file ???", e);
		}
	}
	
	private class RandomUrl implements Comparable<RandomUrl>
	{
		private Integer val=rnd.nextInt();
		private String url;
		RandomUrl(String url)
		{
			this.url=url;
		}
		String getUrl()
		{
			return this.url;
		}
		@Override
		public int compareTo(RandomUrl another) {
			return val.compareTo(another.val);
		}
	}

	String readText() {
		synchronized (this.prefs) {
			int endIndex = this.prefs.getInt(END_INDEX, 0);
			int readIndex = this.prefs.getInt(READ_INDEX, 0);
			if (readIndex < endIndex) {
				String text = prefs.getString(String.valueOf(readIndex), null);
				SharedPreferences.Editor editor = prefs.edit();
				editor.remove(String.valueOf(readIndex));
				editor.putInt(READ_INDEX, readIndex + 1);
				editor.commit();
				return text;
			} else {
				startDownloading();
				return RandomTextUtil.getRandomSentence();
			}
		}
	}

	private void startDownloading() {
		synchronized (downloadThreads) {
			if (isDownloading() == false) {
				Calendar cutoffCalendar = Calendar.getInstance();
				cutoffCalendar.add(Calendar.DATE, -1);
				cutoffDate = cutoffCalendar.getTime();
				parseOpmlFile();
				for (int i = 0; i < downloadThreads.length; i++) {
					downloadThreads[i] = new MorseTextDownloadThread(i);
					downloadThreads[i].start();
				}
			}
		}
	}
}
