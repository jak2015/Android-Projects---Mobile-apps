package entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import contracts.PeerContract;

public class Peer implements Parcelable
{
    public long id;
    public String name;
    public InetAddress address;
    public int port;

    public Peer(long id, String name, InetAddress address, int port)
    {
        this.id = id;
        this.name = name;
        this.address = address;
        this.port = port;
    }
    public Peer(Cursor c)
    {
        this.id = PeerContract.getId(c);
        this.name = PeerContract.getName(c);
        String s = PeerContract.getAddress(c);
        try {
            this.address = InetAddress.getByName(s);
        }catch(UnknownHostException e)
        {
            e.printStackTrace();
        }
        this.port= PeerContract.getPort(c);
    }

    public void writeToProvider(ContentValues values)
    {
        PeerContract.putName(values, this.name);
        String s=this.address.toString();
        PeerContract.putAddress(values, s);
        PeerContract.putPort(values, this.port);
    }

    public Peer(Parcel in)
    {
        id=in.readLong();
        name=in.readString();
        address=(InetAddress)in.readValue(InetAddress.class.getClassLoader());
        port=in.readInt();
    }

    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel out,int i)
    {
        out.writeLong(id);
        out.writeString(name);
        out.writeValue(address);
        out.writeInt(port);
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public Peer createFromParcel(Parcel in) {
            return new Peer(in);
        }

        public Peer[] newArray(int size) {
            return new Peer[size];
        }
    };
}
