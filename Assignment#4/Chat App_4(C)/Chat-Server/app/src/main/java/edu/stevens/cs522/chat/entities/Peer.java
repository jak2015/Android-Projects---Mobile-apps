package edu.stevens.cs522.chat.entities;

import java.net.InetAddress;
import java.net.UnknownHostException;
import edu.stevens.cs522.chat.contracts.PeerContract;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class Peer implements Parcelable {

	public long id;
	public String name;
	public InetAddress address;
	public int port;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public InetAddress getAddress() {
		return address;
	}
	public void setAddress(InetAddress address) {
		this.address = address;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
	@Override
	public String toString() {
		return "Peer [id=" + id + ", name=" + name + ", address=" + address
				+ ", port=" + port + "]";
	}

	public Peer(long id, String name, InetAddress address, int port) {
		super();
		this.id = id;
		this.name = name;
		this.address = address;
		this.port = port;
	}
	public Peer(String name, InetAddress address, int port) {
		super();
		this.name = name;
		this.address = address;
		this.port = port;
	}
	
    public static final Parcelable.Creator<Peer> CREATOR = new Parcelable.Creator<Peer>() {
        public Peer createFromParcel(Parcel in) {
            return new Peer(in);
        }

        public Peer[] newArray(int size) {
            return new Peer[size];
        }
    };
    
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.id);
        out.writeString(this.name);
        out.writeString(this.address.getHostAddress());
        out.writeInt(this.port);
    }
    
    private Peer(Parcel in) {
    	this.id = in.readLong();
		this.name = in.readString();
		try {
			this.address = InetAddress.getByName(in.readString());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.port = in.readInt();
    }
    
	public int describeContents() {
		return 0;
	}
	
	public Peer(Cursor cursor) {
    	if(null != cursor && cursor.getCount() != 0){
	    	this.setId(PeerContract.getPeerId(cursor));
	    	this.setName(PeerContract.getPeerName(cursor));
	    	this.setAddress(PeerContract.getPeerAddress(cursor));
	    	this.setPort(PeerContract.getPeerAddressPort(cursor));
	    } else {
			this.setAddress(null);
			this.setId(0);
			this.setName(null);
			this.setPort(0);
	    }
    }
	
	public void writeToProvider(ContentValues values) {
    	PeerContract.putPeerId(values, this.getId());
    	PeerContract.putPeerName(values, this.getName());
    	PeerContract.putPeerAddress(values, this.getAddress());
    	PeerContract.putPeerAddressPort(values, this.getPort());
    }
}
