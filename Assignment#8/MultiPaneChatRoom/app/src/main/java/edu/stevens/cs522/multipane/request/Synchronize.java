package edu.stevens.cs522.multipane.request;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Time;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

public class Synchronize {
	public String userId;
	public ArrayList<String> message;
	public long id = 0;
	public UUID regid;
	public String addr;
	public String chatroom;

	public int describeContents() {
		return hashCode();
	}
	public Synchronize(){};
	public Synchronize(long seqnum, UUID registrationID, String username,
			String addr,String chatroom, ArrayList<String> message) {
		this.id = seqnum;
		this.regid = registrationID;
		this.userId = username;
		this.addr = addr;
		this.chatroom = chatroom;
		this.message = message;
	}
	public Map<String, String> getRequestHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	public Uri getRequestUri() {
		return Uri.parse(addr + "/chat/" + this.userId + "?regid="
				+ this.regid.toString() + "&seqnum=" + String.valueOf(id));

	}

	public String getRequestEntity() throws IOException {
		JSONObject obj = new JSONObject();
		try {
			obj.put("chatroom", chatroom);
			obj.put("timestamp", String.valueOf(new Date().getTime()));
			obj.put("text", this.message);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return obj.toString();
	}

	public StreamingResponse getResponse(HttpURLConnection conn, JsonReader rd) {
		StreamingResponse response = new StreamingResponse();
		List usersList = new ArrayList<String>();
		List msgList = new Vector<String[]>();


		try {
			conn.setRequestMethod("POST");

			conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-latitude", "54.725488");
            conn.setRequestProperty("X-longitude", "53.785742");
            conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setChunkedStreamingMode(0);

			conn.connect();
			JsonWriter wr;

			wr = new JsonWriter(new BufferedWriter(new OutputStreamWriter(
					conn.getOutputStream(), "UTF-8")));

			wr.beginArray();

			for (String msg : message) {
				wr.beginObject();
				wr.name("chatroom");
				wr.value(chatroom);
				wr.name("timestamp");
				wr.value(  Long.parseLong(String.valueOf( new Date().getTime())));
				wr.name("text");
				wr.value(msg);
				wr.endObject();
				Log.d("+++++Writing JSON++++++", chatroom + ":"+Long.parseLong(String.valueOf( new Date().getTime()))+msg);
			}
			wr.endArray();
			wr.flush();
			wr.close();
			JsonReader jrd = new JsonReader(new BufferedReader(
					new InputStreamReader(conn.getInputStream(), "UTF-8")));
			jrd.beginObject();

			jrd.nextName();
			jrd.beginArray();

			while (jrd.hasNext()) {
                jrd.beginObject();
                jrd.nextName();
                String sender=jrd.nextString();
                jrd.nextName();
                Double x=jrd.nextDouble();
                jrd.nextName();
                Double y=jrd.nextDouble();
                usersList.add(sender);
                jrd.endObject();
				//usersList.add(jrd.nextString());
			}
			jrd.endArray();

			jrd.nextName();
			jrd.beginArray();
			while (jrd.hasNext()) {
                jrd.beginObject();
                String[] tmp = new String[7];
                jrd.nextName();
                String chatroom = jrd.nextString();
                tmp[0]=chatroom;
                jrd.nextName();
                String timestamp = jrd.nextString();
                tmp[1]=timestamp;
                jrd.nextName();
                Double x = jrd.nextDouble();
                String s1=Double.toString(x);
                tmp[2]=s1;
                jrd.nextName();
                Double y = jrd.nextDouble();
                String s2=Double.toString(y);
                tmp[3]=s2;
                jrd.nextName();
                long seqnum = jrd.nextLong();
                String s3=Long.toString(seqnum);
                tmp[4]=s3;
                jrd.nextName();
                String sender = jrd.nextString();
                tmp[5]=sender;
                jrd.nextName();
                String text = jrd.nextString();
                tmp[6]=text;
                msgList.add(new String[] { tmp[0], tmp[1], tmp[2], tmp[3],tmp[4],tmp[5],tmp[6]});
                jrd.endObject();
			}

			response.usersList = usersList;
			response.msgList = msgList;

			// sync(jr);
			conn.disconnect();

		} catch (Exception e) {
			Log.e("Error on sync", e.toString());
		}
		return response;
	}


}
