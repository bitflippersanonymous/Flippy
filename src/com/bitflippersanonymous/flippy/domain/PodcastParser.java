package com.bitflippersanonymous.flippy.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParserException;

import com.bitflippersanonymous.flippy.db.FlippyDatabaseAdapter;
import com.bitflippersanonymous.flippy.domain.PlsEntry.Tags;
import com.bitflippersanonymous.flippy.util.Util;

import android.content.res.XmlResourceParser;
import android.util.Log;

public class PodcastParser {
	
	private static final String ITEM = "item";
	private static final String URL = "url";
	private InputStream mInputStream = null;
	private FlippyDatabaseAdapter mDbAdapter = null;
	private String mPath = null;
	
	private class SAXExceptionEnough extends SAXException {
		private static final long serialVersionUID = 240553706960958221L;
	};
	
	public PodcastParser(String path, FlippyDatabaseAdapter adapter) {
		mPath = path;
		mDbAdapter = adapter;
		
	}

	public void parse() {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			SAXParser parser = factory.newSAXParser();
			RssHandler handler = new RssHandler();
			mInputStream  = getInputStream(mPath);
			if ( mInputStream != null )
				parser.parse(mInputStream, handler);
		} catch (SAXExceptionEnough e) {
			
		} catch (Exception e) {
			Log.w(getClass().getName(), "Exception http get", e);
		}
		
		if ( mInputStream != null ) {
			try {
				mInputStream.close();
			} catch (IOException e) {
				Log.w(getClass().getName(), "Exception http get", e);
			}
		}
	}	
	
	private class RssHandler extends DefaultHandler {
		private HashMap<PlsEntry.Tags, String> mData = new HashMap<PlsEntry.Tags, String>();
		private StringBuilder mBuilder = new StringBuilder();
		private boolean mInItem = false;
		private String mEnclosure;
	    
	    @Override
	    public void characters(char[] ch, int start, int length)
	            throws SAXException {
	        super.characters(ch, start, length);
	        mBuilder.append(ch, start, length);
	    }

	    @Override
	    public void endElement(String uri, String localName, String name)
	            throws SAXException {
	        super.endElement(uri, localName, name);

	        if ( mData != null ) {
		        if (localName.equalsIgnoreCase(ITEM)) {
		        	long id = mDbAdapter.insertEntry(new PlsEntry(mData));
		        	if ( id == -1 ) {
		        		throw new SAXExceptionEnough();
		        	}
		            mInItem = false;
		        } 
		        if ( mInItem ) {
		        	Tags tag = null;
		        	try { tag = PlsEntry.Tags.valueOf(localName); }
		        	catch(IllegalArgumentException ex) { }
		        	String value = null;
		        	if ( tag == Tags.enclosure )
		        		value = mEnclosure;
		        	else
		        		value = mBuilder.toString();
		        	mData.put(tag, value.trim());
		        }
	        	mBuilder.setLength(0);    
	        }
	    }

	    @Override
	    public void startElement(String uri, String localName, String name, Attributes attributes) 
	    		throws SAXException {
	        super.startElement(uri, localName, name, attributes);
	        
	        mEnclosure = attributes.getValue(URL);
	        if (localName.equalsIgnoreCase(ITEM)) {
	            mData.clear();
	            mInItem = true;
	        }
	    }
	}
	
	private InputStream getInputStream(String path) {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet();
			request.setURI(new URI(path));
			HttpResponse response = client.execute(request);
			return response.getEntity().getContent();
		} catch (Exception e) {
			Log.w(getClass().getName(), "Exception http get", e);
		}
		return null;
	}
	
}

	/*  Old DOM parser
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
		
	}*/


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
	
