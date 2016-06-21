package edu.stevens.cs522.multipane.request;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

public class Unregister extends Request {
	
	public int describeContents() {
		return hashCode();
	}

	public Unregister(long clientID, UUID registrationID, String username,String addr){
		this.id = clientID;
		this.regid = registrationID;

		this.addr = addr;
	}
	public Unregister(Parcel source){
		this.id = source.readLong();
		this.regid = UUID.fromString(source.readString());

		this.addr = source.readString();
	}
	public Unregister(long id, UUID fromString, String parse) {
		this.id = id;
		this.regid = fromString;
		this.addr = parse.toString();
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.id);
		dest.writeString(this.regid.toString());

		dest.writeString(this.addr);
	}
	public static final Parcelable.Creator<Unregister> CREATOR = new Parcelable.Creator<Unregister>() {
		public Unregister createFromParcel(Parcel source) {
			return new Unregister(source);
		}

		public Unregister[] newArray(int size) {
			return new Unregister[size];
		}
	};

	@Override
	public Map<String, String> getRequestHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri getRequestUri() {
		Log.d("unregister",addr +"/chat/"+this.id+"?regid=" + this.regid.toString());
		return Uri.parse(addr +"/chat/"+this.id+"?regid=" + this.regid.toString());
	}

	@Override
	public String getRequestEntity() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response getResponse(HttpURLConnection connection, JsonReader rd) {
		// TODO Auto-generated method stub
		try {
			rd.beginObject();
			while(rd.peek()!= JsonToken.END_OBJECT){
				String name = rd.nextName();
				if(name.equals("id")){
					this.id=rd.nextLong();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}


}
