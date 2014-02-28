package xeo.api.base.impl;

import java.util.Collection;
import java.util.Iterator;

import xeo.api.base.XEOCollection;
import xeo.api.base.XEOModelBase;

public class XEOCollectionImpl<T extends XEOModelBase> implements XEOCollection<T> {
	
	public int index = 0;
	
	ListImpl<T> list;
	
	protected XEOCollectionImpl( ListImpl<T> baseList  ) {
		this.list = baseList;
	}
	
	@Override
	public boolean add(T e) {
		return list.add( e );
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(Collection<? extends T> c) {
		return list.add( (T) c );
	}
	@Override
	public void clear() {
		list.clear();
	}
	@Override
	public boolean contains(Object o) {
		return list.contains( o );
	}
	@Override
	public boolean containsAll(Collection<?> c) {
		return list.containsAll( c );
	}
	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}
	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}
	@Override
	public boolean remove(Object o) {
		return list.remove( o );
	}
	@Override
	public boolean removeAll(Collection<?> c) {
		return list.removeAll(c);
	}
	@Override
	public boolean retainAll(Collection<?> c) {
		return list.retainAll(c);
	}
	@Override
	public int size() {
		return list.size();
	}
	@Override
	public Object[] toArray() {
		return null;
	}
	@Override
	public <Y> Y[] toArray(Y[] a) {
		return list.toArray( a );
	}
	
	public int getRecordCount() {
		return list.getRecordCount();
	}

}
