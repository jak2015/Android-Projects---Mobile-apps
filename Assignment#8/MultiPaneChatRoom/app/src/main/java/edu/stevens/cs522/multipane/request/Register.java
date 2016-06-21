package edu.stevens.cs522.multipane.request;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.UUID;

//import edu.stevens.cs522.helper.ServiceHelper;


import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;
import android.util.JsonToken;

public class Register extends Request{

//	public long clientID;
//	public UUID registrationID; // sanityCheck
	public String username;
	
	public int describeContents() {
		return hashCode();
	}

	public Register(long clientID, UUID registrationID, String username,String addr){
		this.id = clientID;
		this.regid = registrationID;
		this.username = username;
		this.addr = addr;
	}
	public Register(Parcel source){
		this.id = source.readLong();
		this.regid = UUID.fromString(source.readString());
		this.username = source.readString();
		this.addr = source.readString();
	}
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.id);
		dest.writeString(this.regid.toString());
		dest.writeString(this.username);
		dest.writeString(this.addr);
	}
	public static final Parcelable.Creator<Register> CREATOR = new Parcelable.Creator<Register>() {
		public Register createFromParcel(Parcel source) {
			return new Register(source);
		}

		public Register[] newArray(int size) {
			return new Register[size];
		}
	};

	@Override
	public Map<String, String> getRequestHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri getRequestUri() {
		
		return Uri.parse(addr +"/chat?username="+this.username+"&regid=" + this.regid.toString());
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
