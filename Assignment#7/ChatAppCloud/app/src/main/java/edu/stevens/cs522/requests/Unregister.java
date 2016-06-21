package edu.stevens.cs522.requests;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.UUID;
import android.net.Uri;
import android.os.Parcel;
import android.util.JsonReader;

public class Unregister extends Request
{
	private Uri serverUri;
	public Unregister(long clientID, UUID registrationID, Uri serverUri) {
		super(clientID, registrationID);
		this.serverUri = serverUri;
	}
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, String> getRequestHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri getRequestUri() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestEntity() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response getResponse(HttpURLConnection connection, JsonReader rd) {
		// TODO Auto-generated method stub
		return null;
	}

}
