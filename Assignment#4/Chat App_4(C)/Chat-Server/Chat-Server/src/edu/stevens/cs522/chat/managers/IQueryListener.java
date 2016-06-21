package edu.stevens.cs522.chat.managers;

public interface IQueryListener<T> {

	public void handleResults (TypedCursor<T> results);
	
	public void closeResults();
	
}
