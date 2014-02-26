package xeo.api.base.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

import netgest.bo.runtime.boBridgeIterator;
import netgest.bo.runtime.boRuntimeException;
import netgest.bo.runtime.bridgeHandler;

public class XEOBouisIterableImpl implements Iterable<Long> {
	
	private bridgeHandler bridge;
	XEOModelImpl  model;
	
	public XEOBouisIterableImpl( XEOModelImpl model, bridgeHandler bridge ) {
		this.bridge= bridge;
		this.model = model;
	}
	
	@Override
	public Iterator<Long> iterator() {
		return new BridgeBouisIterator(this.bridge.iterator() );
	}
	
	public class BridgeBouisIterator extends XEOCollectionIteratorImpl<Long> {
		
		BridgeBouisIterator( boBridgeIterator bridgeIterator ) {
			super( XEOBouisIterableImpl.this.model, bridgeIterator );
		}
	
		@Override
		public Long next() {
			if( this.bridgeIterator.next() ) {
				try {
					return this.bridgeIterator.currentRow().getValueLong();
				} catch (boRuntimeException e) {
					throw new RuntimeException( e );
				}
			}
			throw new NoSuchElementException();
		}
	
	}
	
	
}
