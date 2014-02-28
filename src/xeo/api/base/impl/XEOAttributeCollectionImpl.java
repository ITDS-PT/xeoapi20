package xeo.api.base.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import netgest.bo.data.DataRow;
import netgest.bo.data.DataSet;
import netgest.bo.runtime.boBridgeIterator;
import netgest.bo.runtime.boObject;
import netgest.bo.runtime.boRuntimeException;
import netgest.bo.runtime.bridgeHandler;
import xeo.api.base.XEOAttributeCollection;
import xeo.api.base.XEOCollectionFilter;
import xeo.api.base.XEOModelBase;

public class XEOAttributeCollectionImpl<T extends XEOModelBase> implements XEOAttributeCollection<T> {
	
	protected bridgeHandler bridge;
	protected String    	bridgeName;
	protected XEOModelImpl  model;
	
	public XEOAttributeCollectionImpl( XEOModelImpl model, String bridgeName ) {
		this.bridgeName = bridgeName;
		this.model  = model;
	}

	@Override
	public boolean add(T model) {
		inititalizeCollection();
		try {
			synchronized (this.bridge) {
				if( !bridge.haveBoui( model.getBoui() ) ) {
					bridge.add( model.getBoui() );
					return true;
				}
				return false;
			}
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean addAll(Collection<? extends T> elements) {
		boolean ret = false;
		for(T element : elements ) {
			ret = addBoui( element.getBoui() ) || ret;
		}
		return ret;
	};

	@Override
	public boolean contains(T o) {
		inititalizeCollection();
		synchronized (this.bridge) {
			int row = this.bridge.getRow();
			try {
				boolean ret = this.bridge.haveBoui( o.getBoui() );
				return ret;
			}
			finally {
				this.bridge.moveTo( row );
			}
		}
	}

	@Override
	public boolean isEmpty() {
		inititalizeCollection();
		return bridge.getRowCount() == 0;
	}

	@Override
	public Iterator<T> iterator() {
		inititalizeCollection();
		return new XEOCollectionIteratorImpl<T>( model, this.bridge.iterator() );
	}
	
	@Override
	public int size() {
		inititalizeCollection();
		return bridge.getRowCount();
	}

	@Override
	public boolean remove(T o) {
		return removeBoui( o.getBoui() );
	}

	@Override
	public Iterable<Long> bouis() {
		inititalizeCollection();
		return new XEOBouisIterableImpl( model, this.bridge );
	}

	@Override
	public boolean addBoui(long boui) {
		inititalizeCollection();
		try {
			synchronized (this.bridge) {
				int row = bridge.getRow();
				try {
					if( !bridge.haveBoui( boui ) ) {
						this.bridge.add( boui );
						return true;
					}
				} finally {
					bridge.moveTo( row );
				}
			}
			return false;
		} catch (boRuntimeException e) {
			throw new RuntimeException( e );
		}
	}

	@Override
	public T get(int line) {
		inititalizeCollection();
		boBridgeIterator bridgeIterator = this.bridge.iterator();
		if ( bridgeIterator.absolute( line + 1 ) ) {
			try {
				return wrapObject( bridgeIterator.currentRow().getValueLong() ) ;
			} catch (boRuntimeException e) {
				throw new RuntimeException(e);
			}
		}
		throw new IndexOutOfBoundsException();
	}

	@Override
	public boolean contains(long boui) {
		return indexOfBoui( boui ) != -1;
	}

	@Override
	public int indexOf(T object) {
		return indexOfBoui( object.getBoui() );
	}

	@Override
	public int indexOfBoui(long boui) {
		int idx;
		int ret = -1;
		inititalizeCollection();
		synchronized ( this.bridge ) {
			idx = this.bridge.getRow();
			try {
				if( this.bridge.haveBoui( boui ) ) {
					ret = this.bridge.getRow()-1;
				}
			}
			finally {
				this.bridge.moveTo( idx );
			}
		}
		return ret;
	}

	@Override
	public boolean removeBoui(long boui) {
		inititalizeCollection();
		int index;
		boolean ret = false;
		index = indexOfBoui( boui );
		if( index != -1 ) {
			synchronized ( this.bridge ) {
				this.bridge.moveTo( index + 1 );
				try {
					this.bridge.remove();
					ret = true;
				} catch (boRuntimeException e) {
					throw new RuntimeException( e );
				}
			}
		}
		return ret;
	}

	@Override
	public boolean wasChanged() {
		inititalizeCollection();
		return this.bridge.isChanged();
	}

	@Override
	public String getLabel() {
		this.model.inititalizeBoObject();
		return this.model.boobject.get().getBoDefinition().getAttributeRef( this.bridgeName ).getLabel();
	}

	@Override
	public String getName() {
		return this.bridgeName;
	}

	@Override
	public T add() {
		inititalizeCollection();
		try {
			boObject newObject;
			synchronized (this.bridge) {
				newObject = this.bridge.addNewObject();
				return wrapObject( newObject );
			}
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public T remove(int index) {
		inititalizeCollection();
		try {
			synchronized (this) {
				if( this.bridge.moveTo( index + 1 ) ) {
					this.bridge.remove();
				}
			}
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
	private final void 	inititalizeCollection() {
		this.model.inititalizeBoObject();
		if( this.bridge == null ) {
			this.bridge = this.model.boobject.get().getBridge( this.bridgeName );
		}
	}

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public XEOAttributeCollection<T> getValue() {
		return this;
	}

	//TODO: Remove setValue from XEOAttribute Interface
	@Override
	public void setValue(XEOAttributeCollection<T> value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public XEOAttributeCollection<T> getPersistedValue() {
		inititalizeCollection();
		List<Long[]> rowsFlashback = new ArrayList<Long[]>();
		DataSet dataSet = this.bridge.getParent().getBridgeDataSet( this.bridgeName );
		
		for( int i=1; i <= dataSet.getDeletedRowsCount(); i++ ) {
			DataRow row = dataSet.deletedRows( i );
			if( row.getFlashBackRow() != null ) {
				row = row.getFlashBackRow();
			}
			rowsFlashback.add(
					new Long[] {
						row.getLong( "LIN" ), 
						row.getLong( "CHILD$" )
					}
				);
		}
		
		for( int i=1; i <= dataSet.getRowCount(); i++ ) {
			DataRow row = dataSet.rows( i );
			if( !row.isNew() ) {
				if( row.getFlashBackRow() != null ) {
					row = row.getFlashBackRow();
				}
				rowsFlashback.add(
					new Long[] {
						row.getLong( "LIN" ), 
						row.getLong( "CHILD$" )
					}
				);
			}
		}
		
		Collections.sort( rowsFlashback, new Comparator<Long[]>() {
			@Override
			public int compare(Long[] o1,Long[] o2) {
				 if (o1[0] < o2[0])
				     return -1;
				 if (o1[0] > o2[0])
				     return 1;
				 return 0;
			}
		} );
		
		List<Long> bouis = new ArrayList<Long>();
		for( Long[] rowFlashback : rowsFlashback ) {
			bouis.add( rowFlashback[1] );
		}
		return new XEOAttributeCollectionFlashbackImpl<T>( this.model, this.bridgeName, Collections.unmodifiableList( bouis ) );
	}

	@Override
	public boolean isRequired() {
		inititalizeCollection();
		try {
			return this.bridge.required();
		} catch (boRuntimeException e) {
			throw new RuntimeException( e );
		}
	}

	@Override
	public boolean isRecommended() {
		return false;
	}

	@Override
	public boolean isDisabled() {
		inititalizeCollection();
		try {
			return this.bridge.required();
		} catch (boRuntimeException e) {
			throw new RuntimeException( e );
		}
	}

	@Override
	public void setDisabled(boolean disabled) {
		inititalizeCollection();
		if( disabled )
			this.bridge.setDisabled();
		else
			this.bridge.setEnabled();
	}

	@Override
	// TODO: Remove set visible from Attribute
	public boolean isVisible() {
		return true;
	}

	@Override
	// TODO: Remove set visible from Attribute
	public void setVisible(boolean disabled) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean validate() {
		inititalizeCollection();
		try {
			return this.bridge.validate();
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	// TODO: Remove set visible from Attribute
	public void setValid(boolean valid) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isValid() {
		return this.isValid();
	}

	@Override
	public void setInvalidMessage(String invalidReason) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getInvalidMessage() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int idx, T object ) {
		inititalizeCollection();
		try {
			synchronized ( this.bridge ) {
				int row = this.bridge.getRow();
				try {
					this.bridge.add( object.getBoui() );
					this.bridge.moveRowTo( idx + 1 );
				}
				finally {
					this.bridge.moveTo( row );
				}
			}
		} catch (boRuntimeException e) {
			throw new RuntimeException( e );
		}
	}
	
	@Override
	public T set(int idx, T object) {
		inititalizeCollection();
		synchronized ( this.bridge ) {
			int row = this.bridge.getRow();
			if( this.bridge.moveTo( idx + 1 ) ) {
				try {
					this.bridge.setValue( object.getBoui() );
				} catch (boRuntimeException e) {
					throw new RuntimeException(e);
				}
				this.bridge.moveTo( row );
			}
			else {
				throw new IllegalArgumentException();
			}
		}
		return null;
	}

	@Override
	public void clear() {
		inititalizeCollection();
		try {
			synchronized (this.bridge) {
				bridge.truncate();
			}
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean contains(Object arg0) {
		for( T element : this ) {
			if( arg0 == null ) {
				return false;
			}
			else if( element.equals( arg0 ) ) {
				return true;
			}
			else if( arg0 instanceof XEOModelBase ) {
				if( indexOfBoui( ((XEOModelBase)arg0).getBoui() ) != -1 ) {
					return true;
				}
			}
			else if( arg0 instanceof Long ) {
				if( indexOfBoui( ((Long)arg0) ) != -1 ) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> elements) {
		for( Object element : elements ) {
			if( !contains( element ) ) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean remove(Object element) {
		boolean ret = false;
		if( element instanceof XEOModelBase ) {
			ret = removeBoui( ((XEOModelBase) element).getBoui() );
		}
		else if( element instanceof Long ) {
			ret = removeBoui( (Long)element );
		}
		return ret;
	}

	@Override
	public boolean removeAll(Collection<?> elements) {
		boolean ret = false;
		for( Object element : elements ) {
			ret = remove( element ) || ret;
		}
		return ret;
	}

	@Override
	public boolean retainAll(Collection<?> elements) {
		List<Long> toRemove = new ArrayList<Long>();
		for( Long boui : this.bouis() ) {
			if( !elements.contains( boui ) && !elements.contains( this.model.factory.scope.wrap(boui) ) ) {
				toRemove.add( boui );
			}
		}
		removeAll( toRemove );
		return false;
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
	
	private T wrapObject( boObject obj ) {
		return wrapObject( obj.getBoui() );
	}
	@SuppressWarnings("unchecked")
	private T wrapObject( Long boui ) {
		return (T)XEOModelFactoryImpl.wrap( 
				new XEOModelRefereceDebugInfo(  this.model.boui, this.bridgeName, this.bridge.getRow(), this.model.name() ), 
				model.factory.scope, boui
			);
	}

}
