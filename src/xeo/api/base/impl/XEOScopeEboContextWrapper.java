package xeo.api.base.impl;

import java.util.Hashtable;
import java.util.Map;

import netgest.bo.runtime.EboContext;
import netgest.bo.system.boPoolable;
import xeo.api.base.XEOApplication;
import xeo.api.base.XEOModelAbstractFactory;
import xeo.api.base.XEOModelBase;
import xeo.api.base.XEOThreadLocalScope;

public class XEOScopeEboContextWrapper extends XEOScopeImpl {
	
	private EboContext 		eboContext;
	private XEOScopeImpl  	scope;
	
	private Hashtable<String, XEOScopeImpl> contextOwners = new Hashtable<String, XEOScopeImpl>(); 
	
	protected XEOScopeEboContextWrapper( XEOSessionImpl session, boPoolable owner, EboContext context ) {
		super( session , owner);
		this.eboContext = context;
	}
	
	private void validateScope() {
		
		this.scope  = contextOwners.get(  eboContext.getPreferredPoolObjectOwner() );
		if( this.scope == null ) {
			
			boPoolable poolOwner;
			
			if ( eboContext.poolUniqueId().equals( eboContext.getPreferredPoolObjectOwner() ) ) {
				poolOwner = eboContext;
			}
			else {
				poolOwner =  eboContext.getBoSession().getApplication().getMemoryArchive().getPoolManager()
						.getObjectById( eboContext.getPreferredPoolObjectOwner() );
				
				
				if( poolOwner == null ) {
					throw new IllegalStateException( 
						String.format("Preferred Owner %s no longer exists. Viewer/Transaction already closed or remove from pool!", eboContext.getPreferredPoolObjectOwner() ) 
					);
				}
			}
			
			if( poolOwner instanceof XEOScopePoolable ) {
				this.scope = ((XEOScopePoolable)poolOwner).getScope(); 
			}
			else {
				this.scope = new XEOScopeImpl( (XEOSessionImpl)
						XEOApplication.getInstance().wrapSession( eboContext.getBoSession() ), poolOwner 
				);
			}
			contextOwners.put(eboContext.getPreferredPoolObjectOwner(),  this.scope );
			
		}
	}
	
	public void close() {
		validateScope();
		scope.close();
	}

	public boolean equals(Object arg0) {
		validateScope();
		return scope.equals(arg0);
	}

	public void flush() {
		validateScope();
		scope.flush();
	}

	public XEOSessionImpl getSession() {
		validateScope();
		return (XEOSessionImpl)scope.getSession();
	}

	public int hashCode() {
		validateScope();
		return scope.hashCode();
	}

	public void release() {
		validateScope();
		scope.release();
	}

	public String toString() {
		validateScope();
		return scope.toString();
	}

	public <T extends XEOModelBase> T wrap(long boui) {
		validateScope();
		return scope.wrap(boui);
	}

	@Override
	public <T extends XEOModelAbstractFactory<? extends XEOModelBase>> T getFactory(
			Class<T> modelClass) {
		validateScope();
		return scope.getFactory(modelClass);
	}

	@Override
	public void setCurrentScope() {
		XEOThreadLocalScope.setDefaultScope( this );
	}
	
	@Override
	public EboContext getEboContext() {
		return this.eboContext;
	}
	
	@Override
	public void setEboContext(EboContext context) {
		validateScope();
		scope.setEboContext(context);
	}
	
	public BoObjectFactory getBoManager() {
		validateScope();
		return scope.getBoManager();
	}

	public XEOScopePoolable getPoolable() {
		validateScope();
		return scope.getPoolable();
	}

	public boolean isClosed() {
		validateScope();
		return scope.isClosed();
	}
	
	@Override
	protected <T extends XEOModelBase> T findLoadedXEOModel(Long boui) {
		validateScope();
		return scope.findLoadedXEOModel(boui);
	}
	@Override
	protected Map<Long, XEOModelBase> getLoadModelsMap() {
		validateScope();
		return scope.getLoadModelsMap();
	}
	
}
