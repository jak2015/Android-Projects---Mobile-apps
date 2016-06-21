package managers;

public interface IQueryListener<T> {

	public void handleResults (TypedCursor<T> results);
	
	public void closeResults();
	
}
