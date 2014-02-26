package xeo.api.base.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

import netgest.bo.runtime.boBridgeIterator;
import netgest.bo.runtime.boRuntimeException;

public class XEOCollectionIteratorImpl<T> implements Iterator<T> {
	
	protected boBridgeIterator bridgeIterator;
	protected XEOModelImpl	   model;
	

	public XEOCollectionIteratorImpl( XEOModelImpl model, boBridgeIterator bridgeIterator )  {
		this.bridgeIterator = bridgeIterator;
		this.model = model;
	}

	@Override
	public boolean hasNext() {
		return !this.bridgeIterator.isLast();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T next() {
		if ( bridgeIterator.next() ) {
			try {
				return (T)XEOModelFactoryImpl.wrap( 
						new XEOModelRefereceDebugInfo(  this.model.boui, bridgeIterator.getBridgeHandler().getName(),this.bridgeIterator.getRow()-1, this.model.name() ), 
						model.factory.scope, bridgeIterator.currentRow().getValueLong() 
					) ;
			} catch (boRuntimeException e) {
				throw new RuntimeException(e);
			}
		}
		throw new NoSuchElementException();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	

}
