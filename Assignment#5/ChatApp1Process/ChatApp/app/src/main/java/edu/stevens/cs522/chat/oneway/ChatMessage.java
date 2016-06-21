package edu.stevens.cs522.chat.oneway;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

public class ChatMessage implements Parcelable{
    public long id;
    public String messageText;
    public String sender;
    public int describeContents() {
        return 0;
    }

    public ChatMessage(int i,String m,String s) {
        this.id = i;
        this.messageText  = m;
        this.sender = s;
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
        this.id = in.readLong();
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
    }

}
