package edu.stevens.cs522.multipane.request;

import java.net.HttpURLConnection;
import java.util.List;



public class StreamingResponse {
	public HttpURLConnection connection;
	public Response response;
    public List<String[]> msgList;
    public List<String>  usersList;
    
	public StreamingResponse(){
		connection=null;
	}
}
