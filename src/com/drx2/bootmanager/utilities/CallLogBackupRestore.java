package com.drx2.bootmanager.utilities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;

public class CallLogBackupRestore {
	Utilities u = new Utilities();
	String[] strFields = {
	        android.provider.CallLog.Calls.NUMBER, 
	        android.provider.CallLog.Calls.TYPE,
	        android.provider.CallLog.Calls.CACHED_NAME,
	        android.provider.CallLog.Calls.CACHED_NUMBER_TYPE,
	        android.provider.CallLog.Calls.CACHED_NUMBER_LABEL,
	        android.provider.CallLog.Calls.DATE,
	        android.provider.CallLog.Calls.DURATION
	        };
	String[] date = {android.provider.CallLog.Calls.DATE};
	
	
	public void getSMS(Context context){
		u.log("Backing up sms for sync");
		//Not completely working here but a good start
		Cursor mCallCursor = context.getContentResolver().query(Uri.parse("content://sms"), null, null, null, null);
		//int id = mCallCursor.getColumnIndex("_id"); Don't need this now but might later
		int address = mCallCursor.getColumnIndex("address");
		int person = mCallCursor.getColumnIndex("person");
		int date = mCallCursor.getColumnIndex("date");
		int type = mCallCursor.getColumnIndex("type");
		int callbacknumber = mCallCursor.getColumnIndex("callback_number");
		int body = mCallCursor.getColumnIndex("body");
		int read = mCallCursor.getColumnIndex("read");
		int status = mCallCursor.getColumnIndex("status");
		int protocol = mCallCursor.getColumnIndex("protocol");
		int locked = mCallCursor.getColumnIndex("locked");
		//String idS = null; Don't need now but might later
		String addressS = null;
		String personS = null;
		String dateS = null;
		String typeS = null;
		String callbacknumberS = null;
		String bodyS = null;
		String readS = null;
		String statusS = null;
		String protocolS = null;
		String lockedS = null;
		//JSONArray jsonA = new JSONArray();
		File smslog = new File(u.getExternalDirectory()+"/BootManager/.zips/smslog");
		if(mCallCursor.moveToFirst()){
			do{
				if(address>=0){
					if(mCallCursor.getString(address)!=null)
						addressS = mCallCursor.getString(address);
				} 
				if(person>=0){
					if(mCallCursor.getString(person)!=null)
						personS = mCallCursor.getString(person);
				} 
				if(date>=0){
					if(mCallCursor.getString(date)!=null)
						dateS = mCallCursor.getString(date);
				} 
				if(type>=0){
					if(mCallCursor.getString(type)!=null)
						typeS = mCallCursor.getString(type);
				} 
				if(callbacknumber>=0){
					if(mCallCursor.getString(callbacknumber)!=null)
						callbacknumberS = mCallCursor.getString(callbacknumber);
				} 
				if(body>=0){
					if(mCallCursor.getString(body)!=null)
						bodyS = mCallCursor.getString(body);
				} 
				if(read>=0){
					if(mCallCursor.getString(read)!=null)
						readS = mCallCursor.getString(read);
				}
				if(status>=0){
					if(mCallCursor.getString(status)!=null)
						statusS = mCallCursor.getString(status);
				}
				if(protocol>=0){
					if(mCallCursor.getString(protocol)!=null)
						protocolS = mCallCursor.getString(protocol);
				}
				if(locked>=0){
					if(mCallCursor.getString(locked)!=null)
						lockedS = mCallCursor.getString(locked);
				}
				writeJSONObj(addJSONobj2(addressS, personS, dateS, typeS, callbacknumberS, bodyS, readS, statusS, protocolS, lockedS), smslog);
				//jsonA.put(addJSONobj2(addressS, personS, dateS, typeS, callbacknumberS, bodyS, readS, statusS, protocolS, lockedS));
			} while (mCallCursor.moveToNext());
			mCallCursor.close();
			
			//writeJSONArray(jsonA, smslog);
		}
		
	}
	 
	 public void getCallLog(Context context){
		 	u.log("Backing up call log for sync");
			String strOrder = android.provider.CallLog.Calls.DATE + " DESC"; 
			Cursor mCallCursor = context.getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI, strFields, null, null, strOrder);
			int numberColumn = mCallCursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER);
			int nameColumn = mCallCursor.getColumnIndex(android.provider.CallLog.Calls.CACHED_NAME);
			int numberlabelColumn = mCallCursor.getColumnIndex(android.provider.CallLog.Calls.CACHED_NUMBER_LABEL);
			int numbertypeColumn = mCallCursor.getColumnIndex(android.provider.CallLog.Calls.CACHED_NUMBER_TYPE);
			int dateColumn = mCallCursor.getColumnIndex(android.provider.CallLog.Calls.DATE);
			int durationColumn = mCallCursor.getColumnIndex(android.provider.CallLog.Calls.DURATION);
			int calltypeColumn = mCallCursor.getColumnIndex(android.provider.CallLog.Calls.TYPE);
			String number = null;
			String name = null;
			String numberlabel = null;
			String numbertype = null;
			String type = null;
			String date = null;
			String duration = null;
			JSONArray jsonA = new JSONArray();
			if(mCallCursor.moveToFirst()){
				do{
				  if(numberColumn>=0){
					  if(mCallCursor.getString(numberColumn)!=null) 
						  number = mCallCursor.getString(numberColumn);
					  else number = null;
					  
				  }
				  if(nameColumn>=0){
					  if(mCallCursor.getString(nameColumn)!=null) 
						  name = mCallCursor.getString(nameColumn);
					  else name = null;
				  }
				  if(numberlabelColumn>=0){
					  if(mCallCursor.getString(numberlabelColumn)!=null)
						  numberlabel = mCallCursor.getString(numberlabelColumn);
					  else numberlabel = null;
				  }
				  if(numbertypeColumn>=0){
					  if(mCallCursor.getString(numbertypeColumn)!=null)
						  numbertype = mCallCursor.getString(numbertypeColumn);
					  else numbertype = null;
				  }
				  if(dateColumn>=0){
					  if(mCallCursor.getString(dateColumn)!=null)
						  date = mCallCursor.getString(dateColumn);
					  else date = null;
				  }
				  if(durationColumn>=0){
					  if(mCallCursor.getString(durationColumn)!=null)
						  duration = mCallCursor.getString(durationColumn);
					  else duration = null;
				  }
				  if(calltypeColumn>=0){
					  if(mCallCursor.getString(calltypeColumn)!=null)
						  type = mCallCursor.getString(calltypeColumn);
					  else type = null;
				  }
				  jsonA.put(addJSONobj(number, name, numberlabel, numbertype, type, date, duration));
			 
			  } while (mCallCursor.moveToNext());
			mCallCursor.close();
			File phonelog = new File(u.getExternalDirectory()+"/BootManager/.zips/phonelog");
			writeJSONArray(jsonA, phonelog);
			}
			
	 }
	 
	 private String readphonelog(File log){
		 	
			
			StringBuilder phonelog = new StringBuilder();
			try {
				FileInputStream fis = new FileInputStream(log);
				DataInputStream dis = new DataInputStream(fis);
				BufferedReader br = new BufferedReader(new InputStreamReader(dis));
				String data;
				while((data = br.readLine()) != null) {
					phonelog.append(data+"\n");
				}
				fis.close();
				dis.close();
				
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
				u.log(e.toString());
			}
			return phonelog.toString();
	 }
	 
	 public void restoreSMS(Context context){
		 	//Checking what collumns are available...this is only for debugging purposes
		    /*Cursor mCursor2 = context.getContentResolver().query(Uri.parse("content://sms"), null, null, null, null);
		    String[] array = mCursor2.getColumnNames();
		    int threadid = mCursor2.getColumnIndex("thread_id");
		    if(mCursor2.moveToFirst()){
				do{
					if(threadid>=0){
						if(mCursor2.getString(threadid)!=null)
							System.out.println(mCursor2.getString(threadid));
					}
				  } while (mCursor2.moveToNext());
		    }*/
		    //Done check
		    //Seems ok from limited testing. Appears to put all messages back in the right threads 
		    //based on the phone numbers so I don't think we need to sort them or anything
		    //Read the backup of the sms here
		  	File log = new File(u.getExternalDirectory()+"/BootManager/.zips/smslog");
		    if(log.exists()){
			  	//String phonelog = new String(readphonelog(log));
			    //Cycle through the json file
			    try {
					//JSONArray jsonArray = new JSONArray(phonelog);
			    	FileInputStream fis = new FileInputStream(log);//
					DataInputStream dis = new DataInputStream(fis);//
					BufferedReader br = new BufferedReader(new InputStreamReader(dis));//
					String data;
					while((data = br.readLine()) != null) {
					//for (int i = 0; i < jsonArray.length(); i++) {
						//JSONObject object = jsonArray.getJSONObject(i);
						JSONObject object = new JSONObject(data);//
						//Read each object here and add it to values to be inserted
						ContentValues values = new ContentValues();
					    if(object.has("addressS"))values.put("address", object.getString("addressS"));
					    if(object.has("dateS"))values.put("date", object.getString("dateS"));
					    if(object.has("personS"))values.put("person", object.getString("personS"));
					    if(object.has("typeS"))values.put("type", object.getString("typeS"));
					    if(object.has("callbacknumberS"))values.put("callback_number", object.getString("callbacknumberS"));
					    if(object.has("bodyS"))values.put("body", object.getString("bodyS"));
					    if(object.has("subjectS"))values.put("subject", object.getString("subjectS"));
					    if(object.has("lockedS"))values.put("locked", object.getString("lockedS"));
					    if(object.has("statusS"))values.put("status", object.getString("statusS"));
					    if(object.has("readS"))values.put("read", object.getString("readS"));
					    if(object.has("protocolS"))values.put("protocol", object.getString("protocolS"));
					    //Here we want to check the dates so we don't duplicate entries
					    Cursor mCursor = context.getContentResolver().query(Uri.parse("content://sms"), date, null, null, null);
					    int dateColumn = mCursor.getColumnIndex("date");
					    String checkdate = "0000000000";
					    Boolean insert = true;
					    if(mCursor.moveToFirst()){
							do{
							  if(dateColumn>=0){
								  if(mCursor.getString(dateColumn)!=null)
									  checkdate = mCursor.getString(dateColumn);
							  }
							  if(checkdate.equals(object.get("dateS"))){
								  insert=false;
							  }
						  } while (mCursor.moveToNext());
					    }
					    mCursor.close();
					    //Inserting values to the sms on phone
					    if(insert)context.getContentResolver().insert(Uri.parse("content://sms"), values);
					    
					}
					fis.close();//
					dis.close();//
					br.close();//
				} catch (Exception e) {
					e.printStackTrace();
				}
				//Delete log when done
				log.delete(); 
				//Trick to refresh timestamps :)
				context.getContentResolver().delete(Uri.parse("content://sms/conversations/-1"), null, null);
		    }
	 }
	 
	 public void addCall(Context context){
		    //Working good may need to be in background thread though
		 	File log = new File(u.getExternalDirectory()+"/BootManager/.zips/phonelog");
		    String phonelog = new String(readphonelog(log));
		 	try {
				JSONArray jsonArray = new JSONArray(phonelog);
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject object = jsonArray.getJSONObject(i);
					//Read each object here and insert to phone log
					ContentResolver mCallCursor = context.getContentResolver();
					ContentValues values = new ContentValues();
				    if(object.has("number"))values.put(CallLog.Calls.NUMBER, object.getString("number"));
				    if(object.has("date"))values.put(CallLog.Calls.DATE, object.getString("date"));
				    if(object.has("duration"))values.put(CallLog.Calls.DURATION, object.getString("duration"));
				    if(object.has("type"))values.put(CallLog.Calls.TYPE, object.getString("type"));
				    values.put(CallLog.Calls.NEW, 0);
				    if(object.has("name"))values.put(CallLog.Calls.CACHED_NAME, object.getString("name"));
				    //Name will dissapear if it's not in thier contacts still.
				    if(object.has("numbertype"))values.put(CallLog.Calls.CACHED_NUMBER_TYPE, object.getString("numbertype"));
				    if(object.has("numberlabel"))values.put(CallLog.Calls.CACHED_NUMBER_LABEL, object.getString("numberlabel"));
				    Cursor mCursor = context.getContentResolver().query(android.provider.CallLog.Calls.CONTENT_URI, date, null, null, null);
				    int dateColumn = mCursor.getColumnIndex(android.provider.CallLog.Calls.DATE);
				    String checkdate = "0000000000";
				    Boolean insert = true;
				    if(mCursor.moveToFirst()){
						do{
						  if(dateColumn>=0){
							  if(mCursor.getString(dateColumn)!=null)
								  checkdate = mCursor.getString(dateColumn);
						  }
						  if(checkdate.equals(object.get("date"))){
							  insert=false;
						  }
					  } while (mCursor.moveToNext());
				    }
				    mCursor.close();
				    if(insert)mCallCursor.insert(CallLog.Calls.CONTENT_URI, values);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			log.delete();
	 }
	 
	 private JSONObject addJSONobj(String number, String name, String numberlabel, String numbertype, String type, String date, String duration) {
			JSONObject object = new JSONObject();
			try {
				object.put("name", name);
				object.put("number", number);
				object.put("numberlabel", numberlabel);
				object.put("numbertype", numbertype);
				object.put("type", type);
				object.put("date", date);
				object.put("duration", duration);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return object;
		}
	 
	 private JSONObject addJSONobj2(String addressS, String personS, String dateS, String typeS, String callbacknumberS, String bodyS, String readS, String statusS, String protocolS, String lockedS) {
			JSONObject object = new JSONObject();
			try {
				//object.put("idS", idS); Don't need now but might later
				object.put("addressS", addressS);
				object.put("personS", personS);
				object.put("typeS", typeS);
				object.put("dateS", dateS);
				object.put("callbacknumberS", callbacknumberS);
				object.put("bodyS", bodyS);
				object.put("readS", readS);
				object.put("statusS", statusS);
				object.put("protocolS", protocolS);
				object.put("lockedS", lockedS);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return object;
		}
	 
	 private void writeJSONArray(JSONArray ja, File phonelog){
		 
			try {
			if(!phonelog.exists()){
				phonelog.createNewFile();
			}
			FileWriter out = new FileWriter(phonelog, true);
			out.write(ja.toString());
			out.flush();
			out.close();
			} catch (IOException e){
        	 e.printStackTrace();
			}
	 }
	 
	 private void writeJSONObj(JSONObject ja, File phonelog){
		 
			try {
			if(!phonelog.exists()){
				phonelog.createNewFile();
			}
			FileWriter out = new FileWriter(phonelog, true);
			out.write(ja.toString()+"\n");
			out.flush();
			out.close();
			} catch (IOException e){
     	 e.printStackTrace();
			}
	 }
	 
}
