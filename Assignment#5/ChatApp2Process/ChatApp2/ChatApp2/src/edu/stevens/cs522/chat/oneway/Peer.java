package edu.stevens.cs522.chat.oneway;

import java.net.InetAddress;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

public class Peer implements Parcelable {
	public long id;
	public String name;
	public InetAddress address;
	public int port;
	public Peer() {
	}
	public Peer(String n,InetAddress a,int p) {
		name=n;
		address=a;
		port=p;
	}
	public Peer(Parcel in) {
		readFromParcel(in);
	}
	public void readFromParcel(Parcel in) { 
		this.id=in.readLong();
        this.name  = in.readString();
        this.address = (InetAddress)in.readParcelable(InetAddress.class.getClassLoader());
        this.port = in.readInt();
       // this.authors[0]=in.readParcelable(Author.class.getClassLoader());
        
	 } 
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeValue(address);
		dest.writeInt(port);
	}
	public static final Parcelable.Creator<Peer> CREATOR = new Parcelable.Creator<Peer>() {  
	    
        public Peer createFromParcel(Parcel in) {  
            return new Peer(in);  
        }  
   
        public Peer[] newArray(int size) {  
            return new Peer[size];  
        }  
          
    };
    public void writeToProvider(ContentValues values) {
		Contract.putId(values,id);
		Contract.putName(values, name);
		Contract.putAddress(values, address.getHostAddress());
		Contract.putPort(values, port);
	// TODO Auto-generated method stub
	
}

}
