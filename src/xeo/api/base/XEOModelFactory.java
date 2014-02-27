package xeo.api.base;

import java.util.Collection;


public interface XEOModelFactory<T extends XEOModelBase> {

	public abstract T create();

	public abstract T load(long boui);

	public abstract T create(XEOModelBase parent);

	public abstract XEOScope getScope();
	
	public abstract Collection<T> list();
	
	public abstract Collection<T> list( String boqlWhere );
	
	public abstract Collection<T> list( String boqlWhere, Object...args );

}