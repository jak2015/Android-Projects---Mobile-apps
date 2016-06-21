package edu.stevens.cs522.requests;

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
	public String message;
	public long id = 0;
	public UUID regid;
	public String addr;
    public double latitude = 40.074;
    public double longitude = -74.32;

	public int describeContents() {
		return hashCode();
	}
	public Synchronize(){};
	public Synchronize(long seqnum, UUID registrationID, String username, String addr, String message) {
		this.id = seqnum;
		this.regid = registrationID;
		this.userId = username;
		this.addr = addr;
		this.message = message;
	}

	public Map<String, String> getRequestHeaders() {
		return null;
	}

	public Uri getRequestUri() {
		return Uri.parse(addr + "/chat/" + this.userId + "?regid="
				+ this.regid.toString() + "&seqnum=" + String.valueOf(id)+"&latitude=40.32&longitude=-74.032");

	}

	public String getRequestEntity() throws IOException {
		JSONObject obj = new JSONObject();
		try {
			obj.put("chatroom", "_default");
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
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setChunkedStreamingMode(0);
            conn.setRequestProperty("X-latitude","40");
            conn.setRequestProperty("X-longitude", "74");
		
			conn.connect();
			JsonWriter writer;

			writer = new JsonWriter(new BufferedWriter(new OutputStreamWriter(
					conn.getOutputStream(), "UTF-8")));
			writer.beginObject();
			writer.name("chatroom");
			writer.value("_default");
			writer.name("timestamp");
			writer.value(new Date().getTime());
			writer.name("text");
			writer.value(message);
			writer.endObject();
			writer.flush();
			writer.close();

			JsonReader jrd = new JsonReader(new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8")));
			jrd.beginObject();
			jrd.nextName();
			jrd.beginArray();
			
			while (jrd.hasNext()) {
				usersList.add(jrd.nextString());
			}
			jrd.endArray();

			jrd.nextName();
			jrd.beginArray();
			while (jrd.hasNext()) {
				jrd.beginObject();
				String[] tmp = new String[5];
				jrd.nextName();
				tmp[0] = jrd.nextString();
				jrd.nextName();
				tmp[1] = jrd.nextString();
				jrd.nextName();
				tmp[2] = jrd.nextString();
				jrd.nextName();
				tmp[3] = jrd.nextString();
				jrd.nextName();
				tmp[4] = jrd.nextString();
				msgList.add(new String[] { tmp[0], tmp[1], tmp[2], tmp[3],tmp[4]});
				jrd.endObject();
			}

			response.usersList = usersList;
			response.msgList = msgList;
			conn.disconnect();

		} catch (Exception e) {
			Log.e("Error on sync", e.toString());
		}
		return response;
	}

}
