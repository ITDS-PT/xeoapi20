package xeo.api.base.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import netgest.bo.runtime.boObjectList;
import netgest.bo.runtime.boRuntimeException;
import xeo.api.base.XEOCollectionFilter;
import xeo.api.base.XEOModelBase;
import xeo.api.base.XEOScope;

public class ListBoObjectImpl<T extends XEOModelBase> implements ListImpl<T> {
	
	private boObjectList boobjectList;
	private XEOScope	 scope;
	
	public ListBoObjectImpl( boObjectList boobjectList, XEOScope scope ) {
		this.scope = scope;
		this.boobjectList = boobjectList;
	}

	@Override
	public boolean add(T arg0) {
		try {
			boobjectList.add( arg0.getBoui() );
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> arg0) {
		for( T t: arg0 ) {
			add( t );
		}
		return true;
	}

	@Override
	public void clear() {
		try {
			while( this.boobjectList.first() )
				boobjectList.removeCurrent();
		} catch (boRuntimeException e) {
			throw new RuntimeException( e );
		}
	}

	@Override
	public boolean contains(Object arg0) {
		if( arg0 == null ) {
			return false;
		}
		else if( arg0 instanceof XEOModelBase ) {
			return this.boobjectList.haveBoui( ((XEOModelBase)arg0).getBoui() );
		}
		else if ( arg0 instanceof Long ) {
			return this.boobjectList.haveBoui( (Long)arg0 );
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		for( Object t : arg0 ) {
			if( !contains( t ) ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return this.boobjectList.getRowCount() == 0;
	}

	@Override
	public Iterator<T> iterator() {
		return new ListBoObjectIteratorImpl<T>( this.boobjectList, this.scope );
	}

	@Override
	public boolean remove(Object arg0) {
		if( this.contains( arg0 ) ) {
			try {
				this.boobjectList.removeCurrent();
			} catch (boRuntimeException e) {
				throw new RuntimeException( e );
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		boolean removed = false;
		for( Object o : arg0 ) {
			removed = remove( o ) || removed;
		}
		return removed;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		List<Long> toRemove = new ArrayList<Long>();
		for( T element : this ) {
			if( !(arg0.contains( element ) || arg0.contains( element.getBoui() )) ) {
				toRemove.add( element.getBoui() );
			}
		}
		this.removeAll( toRemove );
		return toRemove.size() > 0;
	}

	@Override
	public int size() {
		return this.boobjectList.getRowCount();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> E[] toArray(E[] arg0) {
		int size = size();
		E[] r = (E[])java.lang.reflect.Array
				.newInstance(arg0.getClass().getComponentType(), size);
		
		Iterator<T> it = iterator();
		for (int i = 0; i < r.length; i++) {
			r[i] = (E)it.next();
		}
		return r;		
	}
	
	@Override
	public Collection<T> filter( XEOCollectionFilter<T> filter ) {
		List<T> filteredList = new ArrayList<T>();
		for( T element : this ) {
			if( filter.accept( element ) ) {
				filteredList.add( element );
			}
		}
		return Collections.unmodifiableList( filteredList );
	}

	@Override
	public int getRecordCount() {
		return (int) this.boobjectList.getRecordCount();
	}

	@Override
	public Object[] toArray() {
		Object[] ret = new Object[ this.size() ];
		int i = 0;
		for( T element : this ) {
			ret[ i++ ] = element;
		}
		return ret;
	}
	

}
