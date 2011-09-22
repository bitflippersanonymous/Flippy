package com.unklegeorge.flippy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParserException;

import com.unklegeorge.flippy.PlsEntry.Tags;

import android.content.res.XmlResourceParser;
import android.os.Parcel;
import android.util.Log;

public class PodcastParser {
	
	private static final String ITEM = "item";
	private static final String URL = "url";

	public static void parse(ArrayList<PlsEntry> entries, XmlResourceParser parser) 
		throws XmlPullParserException, IOException {
		
		int eventType = -1;
		while (eventType != XmlResourceParser.END_DOCUMENT) {
			String strName = parser.getName();
			if (eventType == XmlResourceParser.START_TAG) {
				if (strName.equals(ITEM))
					loadItems(entries, parser);
					
			}
			eventType = parser.next();
		}
	}
	
	public static void loadItems(ArrayList<PlsEntry> entries, XmlResourceParser parser ) 
		throws XmlPullParserException, IOException {
		
		HashMap<PlsEntry.Tags, String> data = new HashMap<PlsEntry.Tags, String>();
		int eventType = parser.next();
		while (eventType != XmlResourceParser.END_DOCUMENT) {
			String strName = parser.getName();
			if (eventType == XmlResourceParser.START_TAG) {
				Tags tag = null;
				try { tag = PlsEntry.Tags.valueOf(strName); }
				catch(IllegalArgumentException ex) { }
				String value = null;
				if ( tag == Tags.enclosure )
					value = parser.getAttributeValue(null, URL);
				else
					value = parser.nextText();
				data.put(tag, value);
			} else if (eventType == XmlResourceParser.END_TAG) {
				if (strName.equals(ITEM)) {
					entries.add(new PlsEntry(data));
					return;
				}
			}
			eventType = parser.next();			
		}
		
	}
}

	/*
	private String readPlaylist(String path, String name, ArrayList<PlsEntry> entries) {
		PlsEntry entry = null;
		String result = executeHttpGet(path);
		String lines[] = result.split(Util.NEWLINE);
		for ( int i=0; i<lines.length; i++ ) {
			String line = lines[i];
			if ( line.startsWith(Util.FILE) ) {
				entry = new PlsEntry(line.substring(Util.FILE.length()+2), name);
				entries.add(entry);
			} else if ( line.startsWith(Util.TITLE) ) {
				entry.setTitle(line.substring(Util.TITLE.length()+2));
			}
		}
		return result;
	}

	private String executeHttpGet(String path) {
		BufferedReader in = null;
		String page = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(new URI(path));
			HttpResponse response = client.execute(request);
			in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			while ((line = in.readLine()) != null) {
				sb.append(line + Util.NEWLINE);
			}
			in.close();
			page = sb.toString();
		} catch (Exception e) {
			Log.w(getClass().getName(), "Exception http get", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					Log.w(getClass().getName(), "Exception http get", e);
				}
			}
		}
		return page;
	}
*/
	
