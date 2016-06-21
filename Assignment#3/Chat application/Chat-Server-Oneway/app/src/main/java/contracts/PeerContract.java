package contracts;

import android.content.ContentValues;
import android.database.Cursor;

public class PeerContract {
    public static final String ID = "_id";
    public static final String NAME = "Name";
    public static final String ADDRESS = "Address";
    public static final String PORT = "Port";
    public static final String ID_M = "id_m";
    public static final String FornKey_M = "Peer_fk";
    public static final String SENDER = "Sender";
    public static final String MESSAGE_TEXT = "Message";
    public static final String MESSAGE = "msgs";

    public static int getId(Cursor cursor)
    {
        int i=0;
        try {
            i=cursor.getInt(cursor.getColumnIndexOrThrow(ID));
        }catch (Exception e){
            e.printStackTrace();
        }
        return i;
    }
    public static int getIdm(Cursor cursor)
    {
        return cursor.getInt(cursor.getColumnIndexOrThrow(ID_M));
    }
    public static int getFkm(Cursor cursor)
    {
        return cursor.getInt(cursor.getColumnIndexOrThrow(FornKey_M));
    }
    public static String getSender(Cursor cursor)
    {
        return cursor.getString(cursor.getColumnIndexOrThrow(SENDER));
    }
    public static String getMessageText(Cursor cursor)
    {String c = null;
        try {
            c = cursor.getString(cursor.getColumnIndexOrThrow(MESSAGE_TEXT));
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return c;
    }
    public static String getMessage(Cursor cursor)
    {
        return cursor.getString(cursor.getColumnIndexOrThrow(MESSAGE));
    }
    public static String getName(Cursor cursor)
    {
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndexOrThrow(NAME));
    }
    public static String getAddress(Cursor cursor)
    {
        return cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS));
    }
    public static int getPort(Cursor cursor)
    {
        return cursor.getInt(cursor.getColumnIndexOrThrow(PORT));
    }
    public static void putId(ContentValues values,long id)
    {
        values.put(ID,id);
    }
    public static void putName(ContentValues values,String name)
    {
        values.put(NAME,name);
    }
    public static void putAddress(ContentValues values,String address)
    {
        values.put(ADDRESS,address);
    }
    public static void putPort(ContentValues values,int port)
    {
        values.put(PORT,port);
    }
    public static void putFdm(ContentValues values,long id)
    {
        values.put(FornKey_M,id);
    }
    public static void putSender(ContentValues values,String sender)
    {
        values.put(SENDER,sender);
    }
    public static void putMessageText(ContentValues values,String messageText)
    {
        values.put(MESSAGE_TEXT,messageText);
    }
}