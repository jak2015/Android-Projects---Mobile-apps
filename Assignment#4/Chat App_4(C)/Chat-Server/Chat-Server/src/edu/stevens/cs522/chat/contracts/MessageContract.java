package edu.stevens.cs522.chat.contracts;

import android.content.ContentValues;
import android.database.Cursor;

public class MessageContract {
	
	public static final String MESSAGE_ID = "_id";
	public static final String MESSAGE_MESSAGE = "message";
	public static final String MESSAGE_SENDER = "sender";
	public static final String MESSAGE_PEER_FK = "peer_fk";
	
	public static int getMessageId(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndexOrThrow(MESSAGE_ID));
	}
	public static void putMessageId(ContentValues values, long key_id) {
		values.put(MESSAGE_ID, key_id);
	}
	
	public static String getMessageMessage(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndexOrThrow(MESSAGE_MESSAGE));
	}
	public static void putMessageMessage(ContentValues values, String message_message) {
		values.put(MESSAGE_MESSAGE, message_message);
	}
	
	public static String getMessageSender(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndexOrThrow(MESSAGE_SENDER));
	}
	public static void putMessageSender(ContentValues values, String message_sender) {
		values.put(MESSAGE_SENDER, message_sender);
	}

	public static int getMessagePeerFK(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndexOrThrow(MESSAGE_PEER_FK));
	}
	public static void putMessagePeerFK(ContentValues values, long message_peerfk) {
		values.put(MESSAGE_PEER_FK, message_peerfk);
	}
}
