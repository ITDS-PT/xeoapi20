package xeo.api.base;


public interface XEOQLBuilder<T extends XEOModelBase> {

	/**
	 * Sets the query arguments
	 * 
	 * @param args The arguments
	 * 
	 */
	public abstract XEOQLBuilder<T> args(Object[] args);

	/**
	 *
	 * Set the arguments through varargs
	 *  
	 * @param args the Arguments
	 * 
	 * */
	public abstract XEOQLBuilder<T> argsList(Object... args);

	/**
	 * Set the page size
	 * 
	 * @param fetchSize The page size (defaults to page size declared in {@link boObjectList#})
	 * @return
	 */
	public abstract XEOQLBuilder<T> fetchSize(int fetchSize);

	/**
	 * 
	 * Set the cache parameter (defaults to true)
	 * 
	 * @param cache True to use the cache and false to not use the cache
	 * 
	 * @return
	 */
	public abstract XEOQLBuilder<T> cache(boolean cache);

	/**
	 * Sets the security parameter
	 * 
	 * @param security True to use the security and false otherwise
	 * @return
	 */
	public abstract XEOQLBuilder<T> security(boolean security);

	/**
	 * 
	 * Sets the order by parameter
	 * 
	 * @param orderBy
	 * @return
	 */
	public abstract XEOQLBuilder<T> orderBy(String orderBy);

	/**
	 * 
	 * Sets the fulltext parameter
	 * 
	 * @param fullText
	 * @return
	 */
	public abstract XEOQLBuilder<T> fullText(String fullText);


	/**
	 * 
	 * Generates a list from the current
	 * 
	 * @return
	 */
	public abstract XEOCollection<T> execute();

	/**
	 * 
	 * Retrieve the boql expression
	 * 
	 * @return
	 */
	public abstract String getXEOQl();

	public abstract String getFullText();

	/**
	 * 
	 * Retrieve the list of arguments
	 * 
	 * @return
	 */
	public abstract Object[] getArgs();

	/**
	 * 
	 * Whether cache is being used (defaults to true)
	 * 
	 * @return
	 */
	public abstract boolean isUseCache();

	/**
	 * Whether security is being used in the query (defaults to true)
	 * 
	 * @return
	 */
	public abstract boolean isUseSecurity();

	/**
	 * 
	 * The page size for the query (defaults to 50)
	 * 
	 * @return
	 */
	public abstract int getFetchSize();

	/**
	 * 
	 * The order by string (defaults to empty string)
	 * 
	 * @return
	 */
	public abstract String getOrderBy();

}