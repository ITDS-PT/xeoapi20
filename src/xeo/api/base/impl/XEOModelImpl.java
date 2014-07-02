package xeo.api.base.impl;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import netgest.bo.runtime.AttributeHandler;
import netgest.bo.runtime.boObject;
import netgest.bo.runtime.boRuntimeException;
import netgest.bo.system.boApplication;

import org.json.JSONException;
import org.json.JSONObject;

import xeo.api.base.XEOAttribute;
import xeo.api.base.XEOAttributeCollection;
import xeo.api.base.XEOAttributeObject;
import xeo.api.base.XEOModelBase;
import xeo.api.base.XEOScope;
import xeo.api.base.exceptions.XEOConcurrentModificationException;
import xeo.api.base.exceptions.XEODuplicateKeyException;
import xeo.api.base.exceptions.XEOException;
import xeo.api.base.exceptions.XEOModelValidationException;
import xeo.api.base.exceptions.XEOReferenceViolationException;
import xeo.api.base.exceptions.XEORuntimeException;

public  class XEOModelImpl implements XEOModelBase {
	
	protected WeakReference<boObject> boobject;
	protected Long boui;
	protected XEOModelFactoryImpl<? extends XEOModelBase> factory;
	protected XEOModelRefereceDebugInfo debugInfo;
	
	private Errors 		errorsHandler = new ErrorsImpl();
	private Parameters 	parametersHandler = new ParametersImpl();
	private Parent 		parentHandler = new ParentImpl();
	
	private boolean isBindedToBoObject = false;

	public static final String[] ATTRIBUTES_NAMES = new String[0];

	public static class Names {};

	public class Attributes {
	}
	
	protected final void wrapObject(Long boui) {
		this.boui = boui;
		if( this.boobject != null ) {
			this.isBindedToBoObject = true;
			this.boobject = null;
		}
		this.boobject = null;
	}

	protected final void wrapObject(boObject boobject) {
		this.boui = boobject.getBoui();
		this.boobject = new WeakReference<boObject>(boobject);
		if( isBindedToBoObject ) {
			reBindToBoObject();
		}
		isBindedToBoObject = true;
	}

	protected final void inititalizeBoObject() {
		if( !isInitialized() ) {
			if( isBindedToBoObject ) {
				reBindToBoObject();
			}
			try {
				this.boobject = new WeakReference<boObject>(
					((XEOScopeImpl) this.factory.getScope()).getBoManager()
							.load(this.boui));
			}
			catch ( Exception e ) {
				StringBuilder sb = new StringBuilder();
				sb.append( String.format("Error loading boui %d of type %s.", this.boui, this.getClass().getName() ) );
				if( this.debugInfo.getReferenceModel() != null ) {
					sb.append( "The reference of the object comes from:\n" );
					sb.append( "\t Model Name:\n" );
					sb.append( "\t Model Boui:\n" );
					sb.append( "\t Model Attribute:\n" );
					if( this.debugInfo.getReferenceCollectionIndex() != -1 ) {
						sb.append( "\t Model Index in Collection:\n" );
					}
				}
				sb.append( "StackTrace from the first reference:" );
				for( StackTraceElement stackElement : this.debugInfo.getReferenceStack() ) {
					sb.append( "\t" );
					sb.append( stackElement.toString() );
					sb.append( '\n' );
				}
				if( e instanceof XEORuntimeException ) {
					e.setStackTrace( debugInfo.getReferenceStack() );
					throw (XEORuntimeException)e;
				}
				throw new RuntimeException( sb.toString(), e );
			}
		}
		isBindedToBoObject = true;
	}
	
	private final boolean isInitialized() {
		if (this.boobject == null || this.boobject.get() == null
				|| this.boobject.get().getEboContext() == null ) {
			return false;
		}
		
		// Check if the object still exists in the pool
		// if not update the reference.
		Object objectInPool = 
				boApplication.getXEO().getMemoryArchive().getPoolManager().getObjectById( this.boobject.get().poolUniqueId() );
		
		if( objectInPool != this.boobject.get() ) {
			return false;
		}
		
		return true;
	}
	

	protected <T> XEOAttribute<T> attributeFactory(String attributeName) {
		return null;
	}

	protected <T extends XEOModelBase> XEOAttributeObject<T> attributeObjectFactory(
			String attributeName) {
		return null;
	}

	protected <T extends XEOModelBase> XEOAttributeCollection<T> attributeCollectionFactory(
			String collectionName) {
		return null;
	}

	@Override
	public boObject unWrap() {
		inititalizeBoObject();
		return boobject.get();
	}

	@Override
	public final Long getBoui() {
		return this.boui;
	}

	public XEOAttribute<?> attributes(String name) {
		return null;
	}

	@Override
	public void save() throws XEOModelValidationException,
			XEOReferenceViolationException, XEOConcurrentModificationException,
			XEODuplicateKeyException, XEOException {
		
		inititalizeBoObject();
		this.factory.scope.getBoManager().save(this.unWrap());
		
	}

	/**
	 * 
	 * Deletes this instance
	 * @throws XEOException 
	 * @throws XEOConcurrentModificationException 
	 * @throws XEOReferenceViolationException 
	 * 
	 * @throws boRuntimeException
	 */
	@Override
	public void destroy() throws XEOReferenceViolationException, XEOConcurrentModificationException, XEOException {
		inititalizeBoObject();
		this.factory.scope.getBoManager().destroy( this.unWrap() );
	}

	protected JSONObject toJSONObject() {
		JSONObject j = new JSONObject();
		try {
			inititalizeBoObject();
			j.put("boui" , getBoui() );
			j.put("name" , name() );
			j.put("cardId" , cardId() );
			j.put("changed" , isChanged() );
			j.put("disabled" , isDisabled() );
			j.put("valid" , isValid() );
			j.put("attributes" , new JSONObject() );
			return j;
		} catch (JSONException e) {}
		return j;
	}
	
	 @Override
	public String toString() {
		try {
			return toJSONObject().toString( 4 );
		} catch (JSONException e) {
			return  name() + ":" + getBoui() + ":" + cardId();
		}
	}

	/**
	 * 
	 * Sets the parent for this instance
	 * 
	 * @param parent
	 *            The parent
	 */
	@Override
	public void setParent(XEOModelBase parent) {
		inititalizeBoObject();
		try {
			boobject.get().addParent(parent.unWrap());
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * Returns the parent object for this instance
	 * 
	 * @return The parent of this instance
	 */
	@Override
	public XEOModelBase getParent() {
		inititalizeBoObject();
		boObject parent;
		try {
			parent = boobject.get().getParent();
			if (parent != null)
				return factory.wrapObject(parent);
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
	
//	public List<XEOModelBase> getParents() {
//		inititalizeBoObject();
//		boObject[] parents;
//		try {
//			parents = boobject.get().getParents();
//			if (parents != null) {
//				List<XEOModelBase> parentsList = new ArrayList<XEOModelBase>();
//				for( boObject parent : parents ) {
//					parentsList.add( factory.wrapObject( parent ) );
//				}
//				return Collections.unmodifiableList( parentsList );
//			}
//			return Collections.emptyList();
//		} catch (boRuntimeException e) {
//			throw new RuntimeException(e);
//		}
//	}
	
	
	@Override
	public XEOScope scope() {
		return this.factory.getScope();
	}
	
	/**
	 * 
	 * Retrieves the textual card id
	 * 
	 * @return A Strgin with the card id
	 * 
	 * @throws boRuntimeException
	 */
	@Override
	public String cardId() {
		inititalizeBoObject();
		try {
			return boobject.get().getTextCARDID().toString();
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * Retrieves the CardId with the icon as a string
	 * 
	 * @return An Html String with the icon and the Card id
	 * 
	 * @throws boRuntimeException
	 */
	@Override
	public String cardIdIcon() {
		inititalizeBoObject();
		return "<img width='16' height='16' alt='' src='resources/"
				+ boobject.get().getName() + "/ico16.gif' />"
				+ cardId().toString();
	}

	/**
	 * 
	 * Retrieve the name of the Model
	 * 
	 * @return A string with the name of the model
	 */
	@Override
	public String name() {
		inititalizeBoObject();
		return boobject.get().getName();
	}
	
	/**
	 * 
	 * Checks whether the object is persisted in the database or not
	 * 
	 * @return True if the object exists in the database and false otherwise
	 */
	@Override
	public boolean exists() {
		inititalizeBoObject();
		try {
			return boobject.get().exists();
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * 
	 * Whether the instance was changed since it was first loaded
	 * 
	 * @return True if the instance was changed since it was first loaded
	 *         and false otherwise
	 */
	@Override
	public boolean isChanged() {
		inititalizeBoObject();
		try {
			if (!isInitialized()) {
				return false;
			} else {
				return boobject.get().isChanged();
			}
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * Disables all attributes in the instance
	 * 
	 * @throws boRuntimeException
	 */
	@Override
	public void disable() {
		inititalizeBoObject();
		try {
			boobject.get().setDisabled();
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * Enable all attributes in the instance
	 * 
	 * @throws boRuntimeException
	 */
	@Override
	public void enable() {
		inititalizeBoObject();
		try {
			boobject.get().setEnabled();
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	/**
	 * 
	 * Checks whether the instance is disabled not
	 * 
	 * @return True if the instance is disabled and false otherwise
	 */
	@Override
	public boolean isDisabled() {
		inititalizeBoObject();
		return boobject.get().isDisabled();
	}

	@Override
	public boolean isValid() {
		inititalizeBoObject();
		try {
			return boobject.get().valid();
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public class ErrorsImpl implements XEOModelBase.Errors {
		/**
		 * 
		 * Adds a new error message to the instance
		 * 
		 * @param errorMessage
		 *            The error message
		 */
		@Override
		public void addErrorMessage(String errorMessage) {
			inititalizeBoObject();
			boobject.get().addErrorMessage(errorMessage);
		}
		
		
		@Override
		public void addErrorMessage(XEOAttribute<?> attribute, String errorMessage) {
			inititalizeBoObject();
			addErrorMessage(attribute.getName(), errorMessage);
		}

		@Override
		public void addErrorMessage(String attributeName, String errorMessage) {
			inititalizeBoObject();
			boObject _boobject = boobject.get();
			_boobject.addErrorMessage( _boobject.getAttribute(attributeName) , errorMessage );
		}
		
		/**
		 * 
		 * Checks whether the instance has errors (either object or attribute
		 * errors)
		 * 
		 * @return True if the instance has errors and false otherwise
		 */
		@Override
		public boolean hasErrors() {
			inititalizeBoObject();
			return boobject.get().haveErrors();
		}

		/**
		 * 
		 * Checks whether the instance has errors in its attributes
		 * 
		 * @return True if any error has an attribute and false otherwise
		 */
		@Override
		public boolean hasErrorsInAttributes() {
			inititalizeBoObject();
			return boobject.get().haveAttributeErrors();
		}

		/**
		 * 
		 * Checks whether there are errors in the instance (does not include
		 * attribute errors)
		 * 
		 * @return True if the instance (not its attribute) has errors
		 */
		@Override
		public boolean hasErrorsInObjectOnly() {
			inititalizeBoObject();
			return boobject.get().haveObjectErrors();
		}

		/**
		 * Clears the list of errors (instance and object)
		 */
		@Override
		public void clearErrors() {
			inititalizeBoObject();
			boobject.get().clearErrors();
		}

		/**
		 * Clears errors of the instance
		 */
		@Override
		public void clearObjectErrors() {
			inititalizeBoObject();
			boobject.get().clearObjectErrors();
		}

		/**
		 * Clears all errors associated to attributes
		 */
		@Override
		public void clearAttributeErrors() {
			inititalizeBoObject();
			boobject.get().clearAttributeErrors();
		}
		
		@Override
		public List<XEOAttribute<?>> attributesInError() {
			if( isInitialized() ) {
				@SuppressWarnings("unchecked")
				Hashtable<AttributeHandler, Object>  attributeErrors = boobject.get().getAttributeErrors();
				if( attributeErrors == null || attributeErrors.isEmpty() ) {
					return Collections.emptyList();
				}
				Collection<XEOAttribute<?>> attributeErrorsList = new ArrayList<XEOAttribute<?>>();
				for( AttributeHandler attribute : attributeErrors.keySet() ) {
					attributeErrorsList.add( attributes( attribute.getName() ) );
				}
				return Collections.unmodifiableList( (List<XEOAttribute<?>>) attributeErrorsList );
			}
			else {
				return Collections.emptyList();
			}
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public List<String> errorMessages() {
			if( isInitialized() && boobject.get().getObjectErrors() != null ) {
				return Collections.unmodifiableList( (List<String>)boobject.get().getObjectErrors() );
			}
			return Collections.emptyList();
		}
	}
	
	public class ParametersImpl implements XEOModelBase.Parameters {
		
		private boolean settedStatefull;
		
		@Override
		public void put(String name, String value) {
			inititalizeBoObject();
			boobject.get().setParameter(name, value);
			if( !boobject.get().poolIsStateFull() ) {
				boobject.get().poolSetStateFull();
				settedStatefull = true;
			}
		}

		@Override
		public String get(String name) {
			inititalizeBoObject();
			return boobject.get().getParameter(name);
		}
		
		@Override
		public void remove(String name) {
			inititalizeBoObject();
			boobject.get().removeParameter( name );
			if( settedStatefull && boobject.get().getParametersHandler().getParametersNames().length == 0 ) {
				if( !isChanged() ) {
					boobject.get().poolUnSetStateFull();
				}
			}
		}
		
	}
	
	public class ParentImpl implements XEOModelBase.Parent {
		
		/**
		 * Removes the parent from this instance
		 */
		@Override
		public void removeParent() {
			inititalizeBoObject();
			try {
				boobject.get().removeParent(null, false);
			} catch (boRuntimeException e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * 
		 * Checks whether this object has a parent object
		 * 
		 * @return True if the object has a parent and false otherwise
		 * 
		 * @throws boRuntimeException
		 */
		@Override
		public boolean hasParent() {
			inititalizeBoObject();
			try {
				return boobject.get().getParent() != null;
			} catch (boRuntimeException e) {
				throw new RuntimeException(e);
			}
		}
		


		@Override
		public boolean addParent(XEOModelBase parentMode) {
			// TODO Auto-generated method stub
			return false;
		}

		
	}

	@Override
	public Errors errors() {
		return errorsHandler;
	}

	@Override
	public Parameters parameters() {
		return parametersHandler;
	}

	@Override
	public Parent parent() {
		return parentHandler;
	}
	
	protected void reBindToBoObject(  ) {
		errorsHandler = new ErrorsImpl();
		parametersHandler = new ParametersImpl();
		parentHandler = new ParentImpl();
	}
	
}
