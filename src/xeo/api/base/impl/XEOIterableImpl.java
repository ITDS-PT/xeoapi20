package xeo.api.base.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;

import netgest.bo.runtime.boBridgeIterator;
import netgest.bo.runtime.boRuntimeException;
import netgest.bo.runtime.bridgeHandler;
import xeo.api.base.XEOModelBase;

public abstract class XEOIterableImpl<T extends XEOModelBase> implements Iterable<T> {
	
	private bridgeHandler bridge;
	private XEOModelImpl  modelImpl;
	
	public XEOIterableImpl( XEOModelImpl model, bridgeHandler bridge ) {
		this.bridge = bridge;
	}

	@Override
	public Iterator<T> iterator() {
		return new BridgeIterator( this.bridge.iterator() );
	}
	
	public class BridgeIterator extends XEOCollectionIteratorImpl<T> {

		public BridgeIterator(boBridgeIterator bridgeIterator) {
			super( modelImpl, bridgeIterator);
		}

		@SuppressWarnings("unchecked")
		@Override
		public T next() {
			if ( bridgeIterator.next() ) {
				try {
					return (T)XEOModelFactoryImpl.wrap( 
							new XEOModelRefereceDebugInfo(  this.model.boui, this.bridgeIterator.getBridgeHandler().getName(), this.bridgeIterator.getRow()-1, this.model.name() ), 
							modelImpl.factory.scope, bridgeIterator.currentRow().getValueLong() 
						) ;
				} catch (boRuntimeException e) {
					throw new RuntimeException( e );
				}
			}
			throw new NoSuchElementException();
		}
		
	}
	
	
}
