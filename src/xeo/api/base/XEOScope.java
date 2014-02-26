package xeo.api.base;


public interface XEOScope {

	
	/**
	 * 
	 * Gets a factory in to produce objects in the same scope.
	 * 
	 * @param modelClass the class of the factory
	 * @return a instance of the factory
	 */
	public abstract <T extends XEOModelFactory<? extends XEOModelBase>> T getFactory(
			Class<T> modelClass);

	/**
	 * Wrap a object indentifier in the typed model object
	 * @param boui identifier to be wrapped
	 * @return the XEOModel wrapped
	 */
	public abstract <T extends XEOModelBase> T wrap( long boui );

	/**
	 * Close and release all objects in memory of this scope 
	 * 
	 */
	public abstract void close();

	/**
	 * Release all objects in memory that are not in statefull state
	 * Statefull is triggered when a change occurs in the object.
	 * 
	 */
	
	public void release();
	
	/**
	 * Release all objects in memory of this scope.
	 */
	public void flush();
	
	
	/**
	 * 
	 * Get the session associated to this Scope
	 * 
	 * @return the current XEOSession
	 */
	public XEOSession getSession();
	
	
}