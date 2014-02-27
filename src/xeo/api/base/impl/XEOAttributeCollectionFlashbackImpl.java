package xeo.api.base.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import netgest.bo.runtime.boRuntimeException;
import netgest.bo.runtime.bridgeHandler;
import xeo.api.base.XEOAttributeCollection;
import xeo.api.base.XEOCollectionFilter;
import xeo.api.base.XEOModelBase;

public class XEOAttributeCollectionFlashbackImpl<T extends XEOModelBase> implements XEOAttributeCollection<T> {
	
	protected bridgeHandler bridge;
	protected String    	bridgeName;
	protected XEOModelImpl  model;
	protected List<Long> flashbackCollection;
	
	public XEOAttributeCollectionFlashbackImpl( XEOModelImpl model, String bridgeName, List<Long> flashCollection ) {
		this.bridgeName = bridgeName;
		this.model  = model;
		this.flashbackCollection = flashCollection;
	}

	@Override
	public boolean add(T model) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(T o) {
		inititalizeCollection();
		return flashbackCollection.contains( o );
	}

	@Override
	public boolean isEmpty() {
		inititalizeCollection();
		return flashbackCollection.size() == 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<T> iterator() {
		inititalizeCollection();
		List<T> newList = new ArrayList<T>();
		for( Long boui : this.flashbackCollection ) {
			newList.add(  (T)XEOModelFactoryImpl.wrap( 
					new XEOModelRefereceDebugInfo(  this.model.boui, this.bridgeName, -1, this.model.name() ), 
					model.factory.scope, boui 
					) 
			);
		}
		return Collections.unmodifiableList( newList ).iterator();
	}
	
	@Override
	public int size() {
		inititalizeCollection();
		return flashbackCollection.size();
	}

	@Override
	public boolean remove(T o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<Long> bouis() {
		return Collections.unmodifiableCollection( this.flashbackCollection );
	}

	@Override
	public boolean addBoui(long boui) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T get(int line) {
		inititalizeCollection();
		Long boui = this.flashbackCollection.get( line ); 
		return (T)XEOModelFactoryImpl.wrap( 
				new XEOModelRefereceDebugInfo(  this.model.boui, this.bridgeName, -1, this.model.name() ), 
				model.factory.scope, boui 
				) ;
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
		return this.flashbackCollection.indexOf( boui );
	}

	@Override
	public boolean removeBoui(long boui) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean wasChanged() {
		return false;
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
		throw new UnsupportedOperationException();
	}

	@Override
	public T remove(int index) {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isRequired() {
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
		try {
			return this.bridge.required();
		} catch (boRuntimeException e) {
			throw new RuntimeException( e );
		}
	}

	@Override
	public void setDisabled(boolean disabled) {
		throw new UnsupportedOperationException();
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
		throw new UnsupportedOperationException();
	}

	@Override
	// TODO: Remove set visible from Attribute
	public void setValid(boolean valid) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isValid() {
		throw new UnsupportedOperationException();
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
	public void add(int idx, T object) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T set(int idx, T object) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
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
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
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
	
	
	
}
