package xeo.api.base.impl;

import java.lang.ref.WeakReference;
import java.util.Hashtable;
import java.util.Map;

import netgest.bo.runtime.EboContext;
import netgest.bo.system.boPoolable;
import xeo.api.base.XEOApplication;
import xeo.api.base.XEOModelAbstractFactory;
import xeo.api.base.XEOModelBase;
import xeo.api.base.XEOThreadLocalScope;

public class XEOScopeEboContextWrapper extends XEOScopeImpl {
	
	private WeakReference<EboContext> eboContext;
	private XEOScopeImpl  	scope;
	
	private Hashtable<String, XEOScopeImpl> contextOwners = new Hashtable<String, XEOScopeImpl>(); 
	
	protected XEOScopeEboContextWrapper( XEOSessionImpl session, boPoolable owner, EboContext context ) {
		super( session , owner);
		this.eboContext = new WeakReference<EboContext>( context );
	}
	
	private void validateScope() {
		EboContext ctx = eboContext.get();
		this.scope  = contextOwners.get(  ctx.getPreferredPoolObjectOwner() );
		if( this.scope == null ) {
			
			boPoolable poolOwner;
			
			if ( ctx.poolUniqueId().equals( ctx.getPreferredPoolObjectOwner() ) ) {
				poolOwner = ctx;
			}
			else {
				poolOwner =  ctx.getBoSession().getApplication().getMemoryArchive().getPoolManager()
						.getObjectById( ctx.getPreferredPoolObjectOwner() );
				
				
				if( poolOwner == null ) {
					throw new IllegalStateException( 
						String.format("Preferred Owner %s no longer exists. Viewer/Transaction already closed or remove from pool!", ctx.getPreferredPoolObjectOwner() ) 
					);
				}
			}
			
			if( poolOwner instanceof XEOScopePoolable ) {
				this.scope = ((XEOScopePoolable)poolOwner).getScope(); 
			}
			else {
				this.scope = new XEOScopeImpl( (XEOSessionImpl)
						XEOApplication.getInstance().wrapSession( ctx.getBoSession() ), poolOwner 
				);
			}
			contextOwners.put(ctx.getPreferredPoolObjectOwner(),  this.scope );
			
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
		return this.eboContext.get();
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
