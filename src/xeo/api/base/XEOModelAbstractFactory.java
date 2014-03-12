package xeo.api.base;

import java.util.Collection;

public interface XEOModelAbstractFactory<T extends XEOModelBase> {

	public abstract XEOScope getScope();
	
	public abstract Collection<T> list();
	
	public abstract Collection<T> list( String boqlWhere );
	
	public abstract Collection<T> list( String boqlWhere, Object...args );

	public abstract XEOQLBuilder<T> listBuilder(String boqlWhere);

}
