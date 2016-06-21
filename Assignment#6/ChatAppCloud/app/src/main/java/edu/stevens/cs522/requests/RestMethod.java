package edu.stevens.cs522.requests;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import android.content.res.Resources;
import android.util.JsonReader;
import android.util.Log;
import edu.stevens.cs522.chatappcloud.R;

public class RestMethod {

	public static final String TAG = RestMethod.class.getCanonicalName();
	String destination = "http://10.0.0.9";
	String data = "";

	public Response perform(Register request) {
		long clientId;
		Response response = new Response();
		// Send data
		try {
			URL url = new URL(request.getRequestUri().toString());
			Log.d("RestMethod", url.toString());
			URLConnection connection = url.openConnection();
			HttpURLConnection conn = (HttpURLConnection) connection;
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(15000);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("X-latitude", "78.7639505");
            conn.setRequestProperty("X-longitude", "-54.023937");
			conn.setRequestMethod("POST");
			response.status = conn.getResponseCode();
			Log.d("Response Code", String.valueOf(response.status));
			if (response.status == 201) {
				response.headers = conn.getHeaderFields();
				InputStream in = conn.getInputStream();
				JsonReader jr = new JsonReader(new InputStreamReader(in,"UTF-8"));
				jr.beginObject();
				if (jr != null) {
					jr.nextName();
					clientId = jr.nextLong();
					response.body = String.valueOf((clientId));
					Log.d("clientIdResponse",String.valueOf(response.body));
				}
				jr.close();
			}
		} catch (Exception ex) {
			Log.e("RestMethod", ex.getMessage());
		}

		return response;
	}

	public StreamingResponse perform(Synchronize request) {
		HttpURLConnection conn;
		URI uri;
		StreamingResponse response = null;
		URL url;
		try {
			uri = new URI(request.getRequestUri().toString());
			url = uri.toURL();
		conn =(HttpURLConnection)url.openConnection();
		JsonReader rd = null;
		response =  request.getResponse(conn, rd);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	HttpURLConnection conn = null;
	public Response perform(Unregister request) {
		long clientId;
		Response response = new Response();
		try {
			URL url = new URL(request.getRequestUri().toString());
			URLConnection connection = url.openConnection();
			conn = (HttpURLConnection) connection;
			conn.setReadTimeout(1000);
			conn.setConnectTimeout(1500);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestMethod("DELETE");
			response.status = conn.getResponseCode();

		} catch (Exception ex) {
			Log.e(TAG, "RestMethod: " + ex.getMessage());
		}finally {
			if(conn != null){
				conn.disconnect();
			}
		}
		return null;
	}
}
