package edu.stevens.cs522.chat.managers;

import android.database.Cursor;

public interface IEntityCreator<T> {

	public T create(Cursor cursor);
}
