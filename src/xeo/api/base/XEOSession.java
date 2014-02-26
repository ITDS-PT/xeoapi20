package xeo.api.base;


public interface XEOSession {
	
	/**
	 * 
	 * Create a scope associated with this XEOSession
	 * 
	 * @return
	 */
	public XEOScope createScope();
	
	/**
	 * Get the current logged user
	 * @return
	 */
	public String getUserName();
	
	/**
	 * Get the current logged user identifier
	 * @return
	 */
	public Long getUserBoui();
	
	/**
	 * Close this session and release all objects in memory hold by this XEOSession
	 */
	public void close();
	
	/**
	 * Release all object in memory hold by this XEOSession if not in the Statefull state. 
	 * 
	 * Statefull state is triggered when a change occurs in the object.
	 */
	public void release();
	
	/**
	 * Release all objects in memory hold by this XEOSession
	 */
	public void flush();

}
