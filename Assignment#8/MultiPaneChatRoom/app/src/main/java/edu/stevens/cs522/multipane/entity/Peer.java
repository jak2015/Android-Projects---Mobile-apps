package edu.stevens.cs522.multipane.entity;

import java.net.InetAddress;

import edu.stevens.cs522.multipane.contract.Contract;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class Peer implements Parcelable {
	public long id;
	public String name;
	public Peer() {
	}
	public Peer(String n,long id) {
		this.id = id;
		this.name=n;
	
	}
	public Peer(Parcel in) {
		readFromParcel(in);
	}
	public Peer(Cursor c) {
		this.id = Contract.getId(c);
		this.name = Contract.getName(c);
			}
	public void readFromParcel(Parcel in) { 
		this.id=in.readLong();
        this.name  = in.readString();
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
	// TODO Auto-generated method stub
	
}

}
