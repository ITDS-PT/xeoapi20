package xeo.api.base;

import java.util.Collection;
import java.util.Iterator;

public interface XEOAttributeCollection<T extends XEOModelBase> extends Collection<T>, XEOAttribute< XEOAttributeCollection<T> > {
	/**
	 * Return a iterator over the identifiers of the models.
	 * @return a iterator of identifiers
	 */
	public abstract Iterable<Long> bouis();
	
	/**
	 * Add a new instance of a model to this collection
	 * @return the instance created.
	 */
	public abstract T add();
	

	/**
	 * Add a model to a specific index
	 * @return the instance created.
	 */
	public abstract void add( int idx, T object );
	
	/**
	 * Set a model to a specific index
	 * @return the instance created.
	 */
	public abstract void set( int idx, T object );
	
	
	/**
	 * Ensures that this collection has the object. If the model instance is already on the collection, the collection is kept unchanged 
	 * @param object the instance to ensure in the collection
	 * @return true - if the instance was inserted at the bottom of the collection. false - If the instance is already present on the collection
	 */
	public abstract boolean add( T object );
	/**
	 * Ensures that this collection has a instance reference. If the reference is already on the collection, the collection is kept unchanged 
	 * @param boui reference to ensure in the collection
	 * @return true - if the was inserted at the bottom of the collection. false - If the instance has already the same reference
	 */
	public abstract boolean addBoui( long boui );
	
	
	/**
	 * Return a model instance at a absolute position of the collection
	 * @param line the position of the collection to retrieve the instance.
	 * @return
	 */
	public abstract T get( int index );
	
	/**
	 * Check if a instance is already present in the collection
	 * @param object the instance to check the presence in the collection.
	 * @return true if the instance passed is present in the collection
	 */
	public abstract boolean contains( T object );
	/**
	 * Check if a reference to a model instance is present in the collection
	 * @param boui the reference to check the presence in the collection
	 * @return true if the referenced passed is present in the collection
	 */
	public abstract boolean contains( long boui );
	
	/**
	 * Retrieves the absolute position of a instance.  
	 * @param object the instance to retrieve the position
	 * @return -1 if the instance is not present in the collection, otherwise the absolute position of the element.
	 */
	public abstract int indexOf( T object );
	
	/**
	 * Retrieves the absolute position of a instance reference.  
	 * @param boui the reference of a instance to retrieve the position
	 * @return -1 if the reference is not present in the collection, otherwise the absolute position of the element.
	 */
	public abstract int indexOfBoui( long boui );
	
	/**
	 * Creates a iterator over the collection
	 */
	public abstract Iterator<T> iterator();
	
	/**
	 * Remove a instance reference from the collection
	 * @param boui the instance referenced to remove
	 * @return true if the reference was found on the collection, false if was not found.
	 */
	public abstract boolean removeBoui( long boui );
	
	/**
	 * Remove a instance from the collection
	 * @param object the instance to remove
	 * @return true if the instance was found on the collection, false if was not found.
	 */
	public abstract boolean remove( T object );

	/**
	 * Remove the element in the absolute position of the collection
	 * @param index the absolute position of the element
	 * @return true if position is valid and the element was removed, false if the elements was not found, or not removed.
	 */
	public abstract boolean remove( int index );
	
	/**
	 * Remove all elements of the collection
	 */
	public abstract void clear();
	
	/**
	 * Check if the collection has no elements
	 */
	public abstract boolean isEmpty();
	
	/**
	 * Retreive the total number of elements in the collection
	 * @return
	 */
	public abstract int size();
	
	/**
	 * Check if the collection was modified since was loaded from the database.
	 */
	public abstract boolean wasChanged();

	/**
	 * Returns a collection of the elements validated by a filter
	 */
	public Collection<T> filter(XEOCollectionFilter<T> filter);
	
	
}
