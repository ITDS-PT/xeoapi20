package xeo.api.base;

public interface XEOCollectionFilter<T> {

	public boolean accept( T o );
	
}
