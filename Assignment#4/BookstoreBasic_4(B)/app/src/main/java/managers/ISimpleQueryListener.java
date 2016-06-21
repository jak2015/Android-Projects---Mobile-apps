package managers;

import java.util.List;


public interface ISimpleQueryListener<T> {

    public void handleResults(List<T> results);
}
