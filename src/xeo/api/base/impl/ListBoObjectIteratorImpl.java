package xeo.api.base.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

import netgest.bo.runtime.boObjectList;
import netgest.bo.runtime.boRuntimeException;
import xeo.api.base.XEOModelBase;
import xeo.api.base.XEOScope;

public class ListBoObjectIteratorImpl<T extends XEOModelBase> implements Iterator<T> {

	boObjectList boobjectList;
	XEOScope	 scope;
	int index;
	
	public ListBoObjectIteratorImpl(boObjectList boobjectList, XEOScope scope) {
		this.scope = scope;
		this.boobjectList = boobjectList;
	}

	@Override
	public boolean hasNext() {
		boolean ret = index < boobjectList.getRowCount();
		return ret || boobjectList.haveMorePages();
	}

	@Override
	public T next() {
		if( hasNext() ) {
			if( index >= boobjectList.getRowCount() ) {
				this.boobjectList.nextPage();
				index = 0;
			}
			index++;
			this.boobjectList.moveTo( index );
			try {
				// Optimized for preload objects
				return (T)this.scope.wrap( this.boobjectList.getObject().getBoui() );
			} catch (boRuntimeException e) {
				throw new RuntimeException( e );
			}
		}
		else {
			throw new NoSuchElementException();
		}
	}

	@Override
	public void remove() {
		if(  index > 0 ) {
			if( this.boobjectList.moveTo( index ) ) {
				index--;
			}
		}
		throw new NoSuchElementException();
	}

}
