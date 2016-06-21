package edu.stevens.cs522.requests;

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
	public Response perform(Unregister ur){
		return null;
	}
}
