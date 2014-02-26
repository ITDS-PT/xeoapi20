package xeo.api.base.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import netgest.bo.runtime.EboContext;
import netgest.bo.runtime.boObject;
import netgest.bo.runtime.boRuntimeException;
import netgest.bo.runtime.boThread;
import netgest.bo.system.boApplication;
import netgest.bo.system.boMemoryArchive;
import netgest.bo.system.boPoolManager;
import netgest.bo.system.boPoolOwner;
import netgest.bo.system.boPoolable;
import xeo.api.base.XEOModelBase;
import xeo.api.base.XEOModelFactory;
import xeo.api.base.XEOScope;
import xeo.api.base.exceptions.XEOConcurrentModificationException;
import xeo.api.base.exceptions.XEODuplicateKeyException;
import xeo.api.base.exceptions.XEOException;
import xeo.api.base.exceptions.XEOModelNotDeployedException;
import xeo.api.base.exceptions.XEOModelValidationException;
import xeo.api.base.exceptions.XEOReferenceViolationException;
import xeo.api.base.exceptions.XEORuntimeException;
import xeo.api.base.exceptions.XEOSaveException;
import xeo.api.base.exceptions.XEOUnknownBouiException;

public class XEOScopeImpl extends boPoolable implements boPoolOwner, XEOScope  {
	
	private static Map<boPoolable,Map<Long,XEOModelBase>> 			MODELS_LOADED 	= 
			new WeakHashMap<boPoolable, Map<Long,XEOModelBase>>();
	
	private static Map<boPoolable,Map<Class<?>,XEOModelFactory<?>>> MODELS_FACTORIES = 
			new WeakHashMap<boPoolable, Map<Class<?>,XEOModelFactory<?>>>();
	
	private boThread 		oBoThread;
	private BoObjectFactory boObjectFactory = new BoObjectFactory();
	
	private XEOSessionImpl  session;
	private boPoolable		poolOwner = this;
	
	private boolean			closed = false;
	private StackTraceElement[] createIn = (new Throwable()).getStackTrace();
	
	
	protected XEOScopeImpl( XEOSessionImpl session ) {
		this.session = session;
	}
	
	protected XEOScopeImpl( XEOSessionImpl session, boPoolable owner ) {
		this.session = session;
		this.poolOwner = owner!=null?owner:this.poolOwner;
	}
	
	@Override
	public EboContext getEboContext() {
		checkClosed();
		return session.getEboContext();
	}
	
	protected Map<Long,XEOModelBase> getLoadModelsMap() {
		Map<Long,XEOModelBase> modelsLoaded = MODELS_LOADED.get( this.poolOwner );
		if( modelsLoaded == null ) {
			synchronized ( MODELS_LOADED ) {
				if( (modelsLoaded = MODELS_LOADED.get( this.poolOwner )) == null ) {
					modelsLoaded = new HashMap<Long, XEOModelBase>();
					MODELS_LOADED.put( this.poolOwner, modelsLoaded );
				}
			}
		}
		return modelsLoaded;
	}
	
	/* (non-Javadoc)
	 * @see xeo.api.base.impl.XEOScope#getFactory(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends XEOModelFactory<? extends XEOModelBase>> T getFactory( Class<T> modelClass ) {
		
		checkClosed();
		
		T factory = null;
		
		Map<Class<?>,XEOModelFactory<?>> modelsFactory = MODELS_FACTORIES.get( this.poolOwner );
		
		if( modelsFactory == null ) {
			synchronized (MODELS_FACTORIES) {
				if( (modelsFactory = MODELS_FACTORIES.get( this.poolOwner )) == null ) {
					modelsFactory = new HashMap<Class<?>, XEOModelFactory<?>>();
					MODELS_FACTORIES.put( this.poolOwner,modelsFactory );
				}
			}
		}
		else {
			factory = (T)modelsFactory.get( modelClass );
		}
		
		if( factory == null ) {
			synchronized (modelsFactory) {
				if( (factory = (T) modelsFactory.get( modelClass )) == null ) {
					factory = XEOModelFactoryImpl.getModelFactory( modelClass );
					((XEOModelFactoryImpl<?>)factory).setScope( this );
					modelsFactory.put(modelClass, factory);
				}
			}
		}
		return factory;
	}
	
	@Override
	public boThread getThread() {
        if( oBoThread == null )
        {
            oBoThread = new boThread();
        }
        return oBoThread;
	}
	
	public BoObjectFactory getBoManager() {
		checkClosed();
		return this.boObjectFactory;
	}
	
	
	@Override
	public void flush() {
		checkClosed();
        EboContext      oEboContext = this.getEboContext();
		synchronized ( oEboContext ) {
	        if( oEboContext != null ) {
		        boApplication   boApp        = oEboContext.getApplication();
		        boMemoryArchive boMemArchive = boApp.getMemoryArchive();
		        boPoolManager   boPoolMgr    = boMemArchive.getPoolManager();
		
		        String sLastPoolOwner = oEboContext.getPreferredPoolObjectOwner();
		        try {
					oEboContext.setPreferredPoolObjectOwner( poolOwner.poolUniqueId() );
					boPoolMgr.realeaseAllObjects( poolOwner.poolUniqueId() );
				} finally {
		        	oEboContext.setPreferredPoolObjectOwner( sLastPoolOwner );
				}
	        }
		}
		release();
		if( this.oBoThread != null ) {
			this.oBoThread.clear();
		}
		
		MODELS_FACTORIES.remove( this.poolOwner );
		MODELS_LOADED.remove( this.poolOwner );
	};
	
	/* (non-Javadoc)
	 * @see xeo.api.base.impl.XEOScope#destroy()
	 */
	@Override
	public void close() {
		if( !this.closed ) {
			this.flush();
			this.closed = true;
		}
	}
	
	public class BoObjectFactory {
		
		public boObject load( Long boui ) throws XEOException {
			checkClosed();
			EboContext context = getEboContext();
			String previousContext = context.getPreferredPoolObjectOwner();
			try {
				context.setPreferredPoolObjectOwner( poolOwner.poolUniqueId() );
				return boObject.getBoManager().loadObject( getEboContext(), boui );
			} catch (boRuntimeException e) {
				// Boui 0
				if( "BO-3018".equals(e.getErrorCode()) ) {
					throw new XEOUnknownBouiException(boui, e.getMessage(), e );
				}
				// Boui not found
				else if( "BO-3015".equals(e.getErrorCode()) ) {
					throw new XEOUnknownBouiException(boui, e.getMessage(), e );
				}
				// Object not deployed
				else if( "BO-3019".equals(e.getErrorCode()) ) {
					String name = e.getMessage().substring( e.getMessage().indexOf( '[' )+1, e.getMessage().indexOf( ']' ) );
					throw new XEOModelNotDeployedException( name, e.getMessage(), e );
				}
				throw new XEOException( e.getMessage(), e );
			} finally {
				context.setPreferredPoolObjectOwner( previousContext );
			}
		}
		
		public boObject create( String modelName ) throws XEORuntimeException {
			checkClosed();
			EboContext context = getEboContext();
			synchronized ( context ) {
				String previousContext = context.getPreferredPoolObjectOwner();
				try {
					context.setPreferredPoolObjectOwner( poolOwner.poolUniqueId() );
					return boObject.getBoManager().createObject( getEboContext(), modelName );
				} catch (boRuntimeException e) {
					if( "BO-3001".equals(e.getErrorCode()) ) {
						throw new XEOModelNotDeployedException( modelName, e.getMessage(), e );
					}
					throw new XEORuntimeException( e.getMessage(), e );
				} finally {
					context.setPreferredPoolObjectOwner( previousContext );
				}
			}
		}
		
		public boObject create( String modelName, Long parentBoui ) {
			checkClosed();
			EboContext context = getEboContext();
			synchronized ( context ) {
				String previousContext = context.getPreferredPoolObjectOwner();
				try {
					context.setPreferredPoolObjectOwner( poolOwner.poolUniqueId() );
					return boObject.getBoManager().createObjectWithParent( getEboContext(), modelName, parentBoui );
				} catch (boRuntimeException e) {
					if( "BO-3001".equals(e.getErrorCode()) ) {
						throw new XEOModelNotDeployedException( modelName, e.getMessage(), e );
					}
					else if( "BO-3018".equals(e.getErrorCode()) ) {
						throw new XEOUnknownBouiException(parentBoui, e.getMessage(), e );
					}
					// Boui not found
					else if( "BO-3015".equals(e.getErrorCode()) ) {
						throw new XEOUnknownBouiException(parentBoui, e.getMessage(), e );
					}
					throw new RuntimeException( e );
				} finally {
					context.setPreferredPoolObjectOwner( previousContext );
				}
			}
		}
		
		public String getModelNameFromBoui( Long boui ) {
			checkClosed();
			EboContext context = getEboContext();
			synchronized ( context ) {
				String previousContext = context.getPreferredPoolObjectOwner();
				try {
					context.setPreferredPoolObjectOwner( poolOwner.poolUniqueId() );
					return boObject.getBoManager().getClassNameFromBOUI( getEboContext(), boui );
				} catch (boRuntimeException e) {
					if( "BO-3018".equals(e.getErrorCode()) ) {
						throw new XEOUnknownBouiException( boui, e.getMessage(), e );
					}
					else if( "BO-3015".equals(e.getErrorCode()) ) {
						throw new XEOUnknownBouiException( boui, e.getMessage(), e );
					}
					throw new RuntimeException( e );
				} finally {
					context.setPreferredPoolObjectOwner( previousContext );
				}
			}
		}
		
		public void save(boObject boobject) 
					throws  XEOSaveException,
							XEOModelValidationException, 
							XEOReferenceViolationException,
							XEOConcurrentModificationException,
							XEODuplicateKeyException
		{
			checkClosed();
			try {
				boobject.update();
			} catch (boRuntimeException e) {
				if( "BO-3021".equals( e.getErrorCode() ) ) { // Invalid attributes
					throw new XEOModelValidationException(boobject.getBoui(), e.getMessage(), e  );
				}
				else if( "BO-3023".equals( e.getErrorCode() ) ) {// Reference
					throw new XEOReferenceViolationException(boobject.getBoui(), e.getMessage(), e  );
				}
				else if( "BO-3022".equals( e.getErrorCode() ) ) { // Changed by another user
					throw new XEOConcurrentModificationException(boobject.getBoui(), e.getMessage(), e);
				}
				else if( "BO-3054".equals( e.getErrorCode() ) ) { // Duplicate Key
					throw new XEODuplicateKeyException(boobject.getBoui(), e.getMessage(), e);
				}
				else if( "BO-3016".equals( e.getErrorCode() ) ) { // Other error
					// throw new XEOException(e.getMessage(), e);
				}
				throw new XEOSaveException(boobject.getBoui(),e.getErrorCode(), e);
			}
		}

		public void destroy(boObject boobject) 
				throws  XEOSaveException,
						XEOReferenceViolationException,
						XEOConcurrentModificationException
						{
			try {
				boobject.destroy();
			} catch (boRuntimeException e) {
				if( "BO-3023".equals( e.getErrorCode() ) ) {// Reference
					throw new XEOReferenceViolationException(boobject.getBoui(), e.getMessage(), e  );
				}
				else if( "BO-3022".equals( e.getErrorCode() ) ) { // Changed by another user
					throw new XEOConcurrentModificationException(boobject.getBoui(), e.getMessage(), e);
				}
				else if( "BO-3016".equals( e.getErrorCode() ) ) { // Other error
					// throw new XEOException(e.getMessage(), e);
				}
				throw new XEOSaveException(boobject.getBoui(),e.getErrorCode(), e);
			}
			
		}
		
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends XEOModelBase> T findLoadedXEOModel( Long boui ) {
		Map<Long, XEOModelBase> map = getLoadModelsMap();
		XEOModelBase model = map.get( boui );
		return (T)model;
	}
	
	
	@Override
	public void poolObjectPassivate() {
	}

	@Override
	public void poolObjectActivate() {
	}

	public XEOSessionImpl getSession() {
		checkClosed();
		return session;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends XEOModelBase> T wrap(long boui) {
		checkClosed();
		return (T)XEOModelFactoryImpl.wrap( new XEOModelRefereceDebugInfo(),this, boui);
	}

	@Override
	public void release() {
		checkClosed();
		EboContext eboContext = getEboContext();
		String lastOwner = eboContext.getPreferredPoolObjectOwner();
		try {
			synchronized (eboContext) {
				eboContext.setPreferredPoolObjectOwner( this.poolUniqueId() );
				eboContext.getApplication().getMemoryArchive().getPoolManager().realeaseObjects( this.poolUniqueId(), eboContext );
				eboContext.setPreferredPoolObjectOwner( this.poolOwner.poolUniqueId() );
				eboContext.releaseObjects();
			}
		}
		finally {
			eboContext.setPreferredPoolObjectOwner( lastOwner );
		}
		
	}
	
	@Override
	public String poolUniqueId() {
		if( this.poolOwner == this ) 
			return super.poolUniqueId();
		
		return this.poolOwner.poolUniqueId();
	}
	
	public boolean isClosed() {
		return closed;
	}
	
	private void checkClosed() {
		if( isClosed() )
			throw new IllegalStateException("XEOScope is closed");
	}
	
	@Override
	protected void finalize() throws Throwable {
		if( !this.closed ) {
			System.err.println( "XEOScope not closed" );
			System.err.println( "Created in:" );
			StringBuilder sb = new StringBuilder();
			for( StackTraceElement stackElement : this.createIn ) {
				sb.append( "\t" + stackElement.toString() + "\n" );
			}
			System.err.println( sb );
		}
	}
	
}
