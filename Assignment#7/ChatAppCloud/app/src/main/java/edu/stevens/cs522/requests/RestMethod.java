package edu.stevens.cs522.requests;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import android.util.JsonReader;
import android.util.Log;

public class RestMethod
{
	String destination = "http://192.168.1.217:8080";
	String data = "";

	public Response perform(Register request) {
		long clientId;
		Response response = new Response();

		try
		{
			URL url = new URL(request.getRequestUri().toString());
			Log.d("RestMethod", url.toString());
			URLConnection connection = url.openConnection();
			HttpURLConnection conn = (HttpURLConnection) connection;
			conn.setReadTimeout(1500);
			conn.setConnectTimeout(2000);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("X-latitude", "53.77305");
            conn.setRequestProperty("X-longitude", "54.844726");
			conn.setRequestMethod("POST");
			response.status = conn.getResponseCode();
			Log.d("Response Code", String.valueOf(response.status));

			if (response.status == 201)
			{
				response.headers = conn.getHeaderFields();
				InputStream in = conn.getInputStream();
				JsonReader jr = new JsonReader(new InputStreamReader(in,"UTF-8"));
				jr.beginObject();
				if (jr != null)
				{
					jr.nextName();
					clientId = jr.nextLong();
					response.body = String.valueOf(clientId);
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
		try
		{
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

	public Response perform(Unregister request) {
		return null;

	}
}
