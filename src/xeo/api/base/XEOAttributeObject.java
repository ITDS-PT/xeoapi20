package xeo.api.base;

public interface XEOAttributeObject<T extends XEOModelBase> extends XEOAttribute<T> {
	
	/**
	 * Retrieve the model instance reference of the attribute
	 * @return model instance reference
	 */
	public abstract long getBoui();
	
}
