package com.drx2.bootmanager.utilities;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.net.Uri;

public class ReadServer {
	
	public static String daerrevreSeliF(String link) {
		
		URL url = 					null;
		String tmp = 				null;
		InputStream in = 			null;
		DataInputStream dis = 		null;
		StringBuilder data = 		null;
		URLConnection con = 		null;		
		String requestURL = String.format(link, Uri.encode("foo bar"), Uri.encode("100% fubar'd"));

//		System.out.println(requestURL);
		
		try {
			url = new URL(requestURL);
			data = new StringBuilder();
			con = url.openConnection();
			con.setConnectTimeout(3000);
			in = con.getInputStream();
			dis = new DataInputStream(new BufferedInputStream(in));			
			
			while((tmp = dis.readLine()) != null) {
				data.append(tmp);
			}
			
			in.close();
			dis.close();
			
		} catch (MalformedURLException e) {

			return null;
		} catch (IOException e) {

			return null;
		}
		return data.toString();
	}
	
public static String daerrevreS(String link) {
		
		URL url = 					null;
		String tmp = 				null;
		InputStream in = 			null;
		DataInputStream dis = 		null;
		StringBuilder data = 		null;
		URLConnection con = 		null;		
		String requestURL = String.format(link, Uri.encode("foo bar"), Uri.encode("100% fubar'd"));

//		System.out.println(requestURL);
		
		try {
			url = new URL(requestURL);
			data = new StringBuilder();
			con = url.openConnection();
			
			in = con.getInputStream();
			dis = new DataInputStream(new BufferedInputStream(in));			
			
			while((tmp = dis.readLine()) != null) {
				data.append(tmp + "\n");
			}
			
			in.close();
			dis.close();
			
		} catch (MalformedURLException e) {

			return null;
		} catch (IOException e) {

			return null;
		}
		return data.toString();
	}
}