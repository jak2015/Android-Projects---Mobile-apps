package edu.stevens.cs522.chat.managers;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;

public class SimpleQueryBuilder<T> implements IContinue<Cursor> {

	private IEntityCreator<T> helper;
	private ISimpleQueryListener<T> listener;
	
	private SimpleQueryBuilder( IEntityCreator<T> helper, ISimpleQueryListener<T> listener) {
		this.helper = helper;
		this.listener = listener;
	}

	public void kontinue(Cursor cursor) {
		// TODO Auto-generated method stub
		List<T> instances = new ArrayList<T>();
		if(cursor.moveToFirst()) {
			do {
				T instance = helper.create(cursor);
				instances.add(instance);
			} while (cursor.moveToNext());
		}
		cursor.close();
		listener.handleResults(instances);
	}
	
	public static <T> void executeQuery(Activity context, Uri uri, IEntityCreator<T> helper, ISimpleQueryListener<T> listener) {
		SimpleQueryBuilder<T> qb = new SimpleQueryBuilder<T>(helper, listener);
		AsyncContentResolver resolver = new AsyncContentResolver(context.getContentResolver());
		resolver.queryAsync(uri, null, null,null, null, qb);
	}
	
	public static <T> void executeQuery(Activity context, 
										Uri uri, 
										String[] projection, 
										String selection, 
										String[] selectionArgs, 
										IEntityCreator<T> helper, 
										ISimpleQueryListener<T> listener) {
		SimpleQueryBuilder<T> qb = new SimpleQueryBuilder<T>(helper, listener);
		AsyncContentResolver resolver = new AsyncContentResolver(context.getContentResolver());
		resolver.queryAsync(uri, projection, selection, selectionArgs, null, qb);
	}
}
