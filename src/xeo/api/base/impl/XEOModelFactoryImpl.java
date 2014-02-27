package xeo.api.base.impl;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Map;

import netgest.bo.runtime.boObject;
import xeo.api.base.XEOModelBase;
import xeo.api.base.XEOModelFactory;
import xeo.api.base.XEOScope;
import xeo.api.base.exceptions.XEOModelNotDeployedException;
import xeo.api.builder.generator.XEONamesBeautifier;

/**
 *
 *
 * @param <T> the type of models produced by this factory
 */
public abstract class XEOModelFactoryImpl<T extends XEOModelBase> implements XEOModelFactory<T> {
	
	protected XEOScopeImpl scope;
	
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
			model.boobject = new WeakReference<boObject>( boobject );
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
		XEOModelImpl model = (XEOModelImpl)instatiateWrapper( boui );
		model.boui = boui;
		model.debugInfo = new XEOModelRefereceDebugInfo();
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
		
		String factoryClassName = XEONamesBeautifier.convertToClassName(booobjectName) + "Factory";
		
		Class<XEOModelFactoryImpl<XEOModelBase>> factoryClass;
		try {
			Field factoryField = Class.forName( "xeo.models.XEOModelFactories" ).getField( factoryClassName );
			factoryClass = (Class<XEOModelFactoryImpl<XEOModelBase>>)factoryField.get(null);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new XEOModelNotDeployedException(booobjectName, "", e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		return factoryClass;
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
	
	
	static final <Y extends XEOModelFactory<?>> Y getModelFactory( Class<Y> modelClassFactory ) {
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
	
}