package xeo.api.base.impl;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import netgest.bo.runtime.boObjectList;
import netgest.bo.runtime.boRuntimeException;
import xeo.api.base.XEOModelBase;
import xeo.api.base.XEOScope;

public class ListBoObjectIteratorImpl<T extends XEOModelBase> implements Iterator<T> {

	boObjectList boobjectList;
	XEOScope	 scope;
	int 		 index	= 0;
	
	public ListBoObjectIteratorImpl(boObjectList boobjectList, XEOScope scope) {
		this.scope = scope;
		this.boobjectList = boobjectList;
	}

	@Override
	public boolean hasNext() {
		boolean ret = index < boobjectList.getRowCount();
		if( !ret ) {
			ret = boobjectList.haveMorePages();
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T next() {
		if( hasNext() ) {
			index++;
			if( index > boobjectList.getRowCount() ) {
				this.boobjectList.nextPage();
				if ( this.boobjectList.getRowCount() <= 0 ) {
					throw new ConcurrentModificationException();					
				}
				index = 1;
			}
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
