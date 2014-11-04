package xeo.api.base;


public interface XEOModelFactory<T extends XEOModelBase> extends XEOModelAbstractFactory<T> {

	/**
	 * Create a new XEOModel instance
	 * @return the created instance
	 */
	public abstract T create();

	/**
	 * Load a XEOModel with specified boui
	 * @param boui
	 * @return
	 */
	public abstract T load(long boui);

	/**
	 * Create a new XEOModel instance with specified parent
	 * @param parent the parent of the new XEOModel instance
	 * @return the created XEOModel instance
	 */
	public abstract T create(XEOModelBase parent);
	
	public XEOQLBuilder<T> listBuilder(String boqlWhere);
	
	/**
	 * Return the first XEOModel instance of the result from the where query
	 * @param boqlWhere the where part of the query
	 * @return the first XEOModel instance returned, or null if no results.
	 */
	public T uniqueResult(String boqlWhere);

	/**
	 * Return the first XEOModel instance of the result from the where query
	 * @param boqlWhere the where part of the query
	 * @param args arguments for the query. 
	 * @return the first XEOModel instance returned, or null if no results.
	 */
	public T uniqueResult(String boqlWhere, Object... args);

}