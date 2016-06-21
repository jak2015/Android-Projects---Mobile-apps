package edu.stevens.cs522.chat.entities;

import edu.stevens.cs522.chat.contracts.MessageContract;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class Message implements Parcelable {
	
	public long id;
	public String messageText;
	public String sender;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getMessageText() {
		return messageText;
	}
	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	
	@Override
	public String toString() {
		return "Message [id=" + id + ", messageText=" + messageText
				+ ", sender=" + sender + "]";
	}
	public Message(long id, String messageText, String sender) {
		super();
		this.id = id;
		this.messageText = messageText;
		this.sender = sender;
	}
	
	public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
    
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.id);
        out.writeString(this.messageText);
        out.writeString(this.sender);
    }
    
    private Message(Parcel in) {
    	this.id = in.readLong();
		this.messageText = in.readString();
		this.sender = in.readString();
    }
    
	public int describeContents() {
		return 0;
	}
	
	public Message(Cursor cursor) {
    	if(null == cursor || cursor.getCount() == 0){
    		return;
    	}
    	this.id = MessageContract.getMessageId(cursor);
		this.messageText = MessageContract.getMessageMessage(cursor);
		this.sender = MessageContract.getMessageSender(cursor);
    }
	
	public void writeToProvider(ContentValues values) {
		MessageContract.putMessageId(values, this.getId());
		MessageContract.putMessageMessage(values, this.getMessageText());
		MessageContract.putMessageSender(values, this.getSender());
    }
}
