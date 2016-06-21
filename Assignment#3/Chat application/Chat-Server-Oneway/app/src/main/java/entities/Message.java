package entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import contracts.PeerContract;

public class Message implements Parcelable
{
    public long fk_id;
    public String messageText;
    public String Sender;

    public int describeContents() {
        return 0;
    }

    public Message(long id, String messageText, String Sender)
    {
        this.fk_id = id;
        this.messageText = messageText;
        this.Sender = Sender;
    }

    public Message(Parcel in)
    {
        fk_id = in.readInt();
        messageText = in.readString();
        Sender = in.readString();
    }

    public Message(Cursor c)
    {
        this.fk_id = PeerContract.getIdm(c);
        this.Sender = PeerContract.getSender(c);
        this.messageText = PeerContract.getMessageText(c);
    }

    public void writeToProvider(ContentValues values)
    {
        PeerContract.putMessageText(values, this.messageText);
        PeerContract.putSender(values, this.Sender);
        PeerContract.putFdm(values, this.fk_id);
    }

    public void writeToParcel(Parcel out, int i)
    {
        out.writeLong(fk_id);
        out.writeString(messageText);
        out.writeString(Sender);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}
