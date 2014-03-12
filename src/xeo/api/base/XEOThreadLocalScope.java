package xeo.api.base;

public class XEOThreadLocalScope {

	private static ThreadLocal<XEOScope> defaultScope = new ThreadLocal<XEOScope>();
	/**
	 * Associate this scope as the default in the current Thread
	 */
	public static void setDefaultScope( XEOScope scope ) {
		defaultScope.set( scope );
	}
	
	/**
	 * Release the association of this thread of the current scope
	 */
	public static void unSetDefaultScope() {
		defaultScope.set( null );
	}
	
	/**
	 * Get the current scope associated with this thread
	 * @return the scope
	 */
	public static XEOScope get() {
		XEOScope scope = defaultScope.get();
		if( scope == null ) {
			throw new IllegalStateException("There is no scope associated with the thread. Make sure the method setDefaultScope was called in the current Thread");
		}
		return scope;
	}

}
