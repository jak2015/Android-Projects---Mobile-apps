package edu.stevens.cs522.entities;

import edu.stevens.cs522.contracts.Contract;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class ChatMessage implements Parcelable{
	public long id;
	public String messageText;
	public String sender;
	public long date;
	public long messageId;
	public long senderId;

	public String latitude = "40.7439905";
	public String longitude = "-74.0323626";

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	public ChatMessage() {
	}
	public ChatMessage(int id, String messageText, String sender, long messageid, long date) {
		this.id=id;
       this.messageText  = messageText;
       this.sender = sender;
       this.date=date;
       this.messageId=messageid;
       this.senderId=0;
	}
    public ChatMessage(int id, String messageText, String sender, long messageid, long date, String latitude, String longitude) {
        this.id=id;
        this.messageText  = messageText;
        this.sender = sender;
        this.date=date;
        this.messageId=messageid;
        this.senderId=0;
        this.latitude=latitude;
        this.longitude=longitude;
    }
	
	public void writeToParcel(Parcel out, int arg1) {
		out.writeLong(id);
		out.writeStringArray(new String[] {this.sender,
                this.messageText});
	}
	
	public ChatMessage(Parcel in) {
		readFromParcel(in) ;
	}
	public void readFromParcel(Parcel in) { 
		this.id=in.readLong();
		 String[] data = new String[2];
        in.readStringArray(data);
        this.messageText  = data[0];
        this.sender = data[1];
	    }  
	 public static final Parcelable.Creator<ChatMessage> CREATOR = new Parcelable.Creator<ChatMessage>() {  
		    
	        public ChatMessage createFromParcel(Parcel in) {  
	            return new ChatMessage(in);  
	        }  
	   
	        public ChatMessage[] newArray(int size) {  
	            return new ChatMessage[size];  
	        }  
	          
	   };
	public void writeToProvider(ContentValues values) {
			Contract.putId(values,id);
			Contract.putSender(values, sender);
			Contract.putText(values, messageText);
			Contract.putDate(values, date);
			Contract.putMessageID(values, messageId);
			Contract.putSenderId(values, senderId);
            Contract.putLatitude(values, latitude);
            Contract.putLongitude(values, longitude);
		// TODO Auto-generated method stub
	}
	public  ChatMessage(Cursor cursor){
		this.messageText = Contract.getText(cursor);
		this.sender = Contract.getSender(cursor);
        this.latitude = Contract.getLatitude(cursor);
        this.longitude = Contract.getLongitude(cursor);
	}

}
