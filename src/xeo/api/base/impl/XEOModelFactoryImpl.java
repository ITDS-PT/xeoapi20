package xeo.api.base.impl;

import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Map;

import netgest.bo.runtime.boObject;
import netgest.bo.runtime.boObjectList;
import netgest.bo.runtime.cacheBouis;
import xeo.api.base.XEOCollection;
import xeo.api.base.XEOModelAbstractFactory;
import xeo.api.base.XEOModelBase;
import xeo.api.base.XEOModelFactory;
import xeo.api.base.XEOQLBuilder;
import xeo.api.base.XEOScope;
import xeo.api.base.exceptions.XEOModelNotDeployedException;
import xeo.api.base.impl.ql.XEOQLPreProcessor;

/**
 *
 *
 * @param <T> the type of models produced by this factory
 */
public abstract class XEOModelFactoryImpl<T extends XEOModelBase> extends XEOModelAbstractFactoryImpl<T> implements XEOModelFactory<T> {
	
	/* (non-Javadoc)
	 * @see xeo.api.base.IXEOModelFactory#create()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T create() {
		boObject boobject = scope.getBoManager().create( getModelName() );
		return (T)wrapObject( boobject );
	}
	
	/* (non-Javadoc)
	 * @see xeo.api.base.IXEOModelFactory#load(long)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T load( long boui ) {
		return (T)wrapObject( boui );
	}
	
	/* (non-Javadoc)
	 * @see xeo.api.base.IXEOModelFactory#create(xeo.api.base.XEOModel)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T create( XEOModelBase parent ) {
		boObject boobject = scope.getBoManager().create( getModelName(), parent.getBoui() );
		return (T)wrapObject( boobject );
	}
	
	protected XEOModelImpl wrapObject( boObject boobject ) {
		XEOModelImpl model = scope.findLoadedXEOModel( boobject.getBoui() );
		if( model == null ) {
			model = wrapObject( boobject.getBoui() );
			model.wrapObject( boobject );
		}
		else if( model.boobject == null || model.boobject.get() != boobject ) {
			model.wrapObject( boobject );
		}
		return model;
	}
	
	protected XEOModelImpl wrapObject( Long boui ) {
		XEOModelImpl model = scope.findLoadedXEOModel( boui );
		if( model == null ) {
			Map<Long,XEOModelBase> loadedModels = scope.getLoadModelsMap();
			
			synchronized ( loadedModels ) {
				if( (model = scope.findLoadedXEOModel( boui )) == null ) {
					model = createWrapper( boui );
					scope.getLoadModelsMap().put( boui, model );
				}
			}
			
		}
		return model;
	}
	
	private XEOModelImpl createWrapper( Long boui ) {
		
		XEOModelImpl model = null;
		
		
		// Check the type of object
		
		// Obtain model factory to this object
		// Load the object
		
		// Workaround for a cacheBoui Bug
		// Objects go to class cacheBoui before they exists... this cause to the object to be visible to other context.
		String boobjectName;
		boobjectName = cacheBouis.getClassName( boui );
		if( boobjectName == null ) {
			try {
				boobjectName = scope.getBoManager().getModelNameFromBoui( boui );
				Field field = cacheBouis.class.getDeclaredField( "bouis" );
				field.setAccessible( true );
				((Hashtable<?, ?>)field.get( null )).remove( boui );
			} catch (SecurityException e) {
				throw new RuntimeException( e );
			} catch (IllegalArgumentException e) {
				throw new RuntimeException( e );
			} catch (NoSuchFieldException e) {
				throw new RuntimeException( e );
			} catch (IllegalAccessException e) {
				throw new RuntimeException( e );
			}
		}
		
		
		Class<XEOModelFactoryImpl<XEOModelBase>> factoryClass = findFactoryClass( boobjectName );
		if( factoryClass == this.getClass() ) {
			model = (XEOModelImpl)instatiateWrapper( boui );
			model.boui = boui;
			model.debugInfo = new XEOModelRefereceDebugInfo();
		}
		else {
			model = scope.wrap( boui );
		}
		return model;
	}
	protected abstract T instatiateWrapper( Long boui );
	
	protected abstract String getModelName();
	
	
	@SuppressWarnings("unchecked")
	static <Y extends XEOModelImpl> Y wrap( XEOModelRefereceDebugInfo debugInfo, XEOScopeImpl scope, Long boui ) {
		try {
			
			Y modelInstance = scope.findLoadedXEOModel( boui );
			if( modelInstance == null ) {
				
				// Load the object
				String boobjectName = scope.getBoManager().getModelNameFromBoui( boui );
				
				// Obtain model factory to this object
				Class<XEOModelFactoryImpl<XEOModelBase>> factoryClass = findFactoryClass( boobjectName );
				XEOModelFactoryImpl<XEOModelBase> factory = scope.getFactory( factoryClass );
				factory.setScope( scope );
				modelInstance = (Y)factory.wrapObject( boui );
				modelInstance.debugInfo = debugInfo;
			}
			return modelInstance;
		} catch (SecurityException e) {
			throw new RuntimeException( e );
		} catch (IllegalArgumentException e) {
			throw new RuntimeException( e );
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Class<XEOModelFactoryImpl<XEOModelBase>> findFactoryClass( String booobjectName ) {
		
		String factoryClassName = XEONamesBeautifier.convertToClassName(booobjectName) + "FactoryLocation";
		
		Class<XEOModelFactoryImpl<XEOModelBase>> factoryClass;
		try {
			factoryClass = (Class<XEOModelFactoryImpl<XEOModelBase>>)Class.forName( "xeo.models.impl._factories." + factoryClassName );
			Field f = factoryClass.getField("FACTORY");
			f.setAccessible( true );
			return (Class<XEOModelFactoryImpl<XEOModelBase>>)f.get( null );
		} catch (ClassNotFoundException e) {
			throw new XEOModelNotDeployedException(booobjectName, "", e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	void setScope( XEOScopeImpl scope ) {
		this.scope = scope;
	}
	
	/* (non-Javadoc)
	 * @see xeo.api.base.IXEOModelFactory#getScope()
	 */
	@Override
	public XEOScope getScope() {
		return scope;
	}
	
	
	static final <Y extends XEOModelAbstractFactory<?>> Y getModelFactory( Class<Y> modelClassFactory ) {
		try {
			// Obtain model factory to this object
			@SuppressWarnings("unchecked")
			Class<Y> modelFactoryClass = (Class<Y>) Class.forName( modelClassFactory.getName() );
			Y modelFactory = modelFactoryClass.newInstance();
			return modelFactory;
		} catch (SecurityException e) {
			throw new RuntimeException( e );
		} catch (IllegalArgumentException e) {
			throw new RuntimeException( e );
		} catch (IllegalAccessException e) {
			throw new RuntimeException( e );
		} catch (ClassNotFoundException e) {
			throw new RuntimeException( e );
		} catch (InstantiationException e) {
			throw new RuntimeException( e );
		}
	}
	
	@Override
	public XEOCollection<T> list() {
		boObjectList list = boObjectList.list( this.scope.getEboContext(), "select " + getModelName() );
		return new ListBoObjectImpl<T>( list , scope);
	}
	
	@Override
	public XEOCollection<T> list(String boqlWhere) {
		boObjectList list = boObjectList.list( this.scope.getEboContext(), "select " + getModelName() + " where " + boqlWhere );
		return new ListBoObjectImpl<T>( list , scope);
	}
	
	@Override
	public T uniqueResult(String boqlWhere, Object ...args ) {
		XEOCollection<T> list = (XEOCollection<T>) listBuilder( boqlWhere ).args( args ).execute();  
		if( list.size() > 0 ) {
			return list.iterator().next();
		}
		return null;
	}
	
	@Override
	public T uniqueResult(String boqlWhere) {
		return uniqueResult( boqlWhere, (Object)null );
	}

	@Override
	public XEOCollection<T> list(String boqlWhere, Object...args) {
		XEOQLPreProcessor preproc = new XEOQLPreProcessor(boqlWhere, args);
		boObjectList list = boObjectList.list( this.scope.getEboContext(), 
				"select " + getModelName() + " where " + preproc.processQl(), 
				preproc.getProcessedParameters() 
		);
		return new ListBoObjectImpl<T>( list , scope);
	}
	
	@Override
	public XEOQLBuilder<T> listBuilder(String boqlWhere) {
		return new XEOQLBuilderImpl<T>(scope, "select " + getModelName() + " where " + boqlWhere );
	}
	
	
}
