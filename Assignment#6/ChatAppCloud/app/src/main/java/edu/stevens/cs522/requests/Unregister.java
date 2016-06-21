package edu.stevens.cs522.requests;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.UUID;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.JsonReader;

public class Unregister extends Request implements Parcelable {
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
		dest.writeParcelable(this.serverUri, 0);
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

	protected Unregister(Parcel in) {
		this.serverUri = in.readParcelable(Uri.class.getClassLoader());
	}

	public static final Creator<Unregister> CREATOR = new Creator<Unregister>() {
		public Unregister createFromParcel(Parcel source) {
			return new Unregister(source);
		}

		public Unregister[] newArray(int size) {
			return new Unregister[size];
		}
	};

}
