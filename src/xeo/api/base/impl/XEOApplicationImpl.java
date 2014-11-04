package xeo.api.base.impl;

import netgest.bo.runtime.EboContext;
import netgest.bo.runtime.boObject;
import netgest.bo.system.boApplication;
import netgest.bo.system.boLoginException;
import netgest.bo.system.boPoolable;
import netgest.bo.system.boSession;
import netgest.bo.transaction.XTransaction;
import netgest.bo.transaction.XTransactionManager;
import xeo.api.base.XEOApplication;
import xeo.api.base.XEOModelBase;
import xeo.api.base.XEOQLBuilder;
import xeo.api.base.XEOQLList;
import xeo.api.base.XEOScope;
import xeo.api.base.XEOSession;
import xeo.api.base.impl.XEOScopeImpl.XEOScopePoolable;

public class XEOApplicationImpl extends XEOApplication {

	
	//private static final WeakHashMap<EboContext, XEOScope> wrappedScopes = new WeakHashMap<EboContext, XEOScope>();
	
	boApplication boapplication;
	
	public XEOApplicationImpl() {
		this.boapplication = boApplication.getXEO();
	}
	
	public XEOSession wrapSession( boSession bosession ) {
		return new XEOSessionImpl(bosession, true );
	}
	
	public XEOSession wrapScope( boSession session, XTransactionManager transactionManager ) {
		return new XEOSessionImpl( session, transactionManager, true );
	}

	public XEOScope wrapScope( boSession session, XTransaction transaction ) {
		return new XEOScopeImpl( (XEOSessionImpl)wrapSession( session ), transaction );
	}
	
	@Override
	public XEOSession login(String userName, String password) throws boLoginException {
		return new XEOSessionImpl( boapplication.boLogin( userName, password ), false );
	}


	@Override
	public XEOScope wrapScope(EboContext eboContext) {
		
		boPoolable poolOwner =  eboContext.getBoSession().getApplication().getMemoryArchive().getPoolManager()
			.getObjectById( eboContext.getPreferredPoolObjectOwner() );
		
		if( poolOwner instanceof XEOScopePoolable ) {
			return ((XEOScopePoolable)poolOwner).getScope();
		}
		else {
			XEOScope scope = null; //wrappedScopes.get( eboContext );
			if( scope == null ) {
				synchronized(XEOApplication.class) {
					scope = new XEOScopeEboContextWrapper(
							(XEOSessionImpl)wrapSession( eboContext.getBoSession() ), 
							poolOwner==null?eboContext:poolOwner, 
							eboContext
					);
					//wrappedScopes.put(eboContext, scope );
				}
			}
			return scope;
		}
	}
	
	public static XEOModelBase wrapObject( boObject boobject ) {
		XEOScope scope = getInstance().wrapScope( boobject.getEboContext() );
		return scope.getFactory( XEOModelFactoryImpl.findFactoryClass( boobject.getName() ) )
			.wrapObject( boobject );
	}
	
	public static XEOModelBase wrapObject( long boui ) {
		XEOScope scope = XEOApplication.wrapScope();
		return XEOModelFactoryImpl.wrap( new XEOModelRefereceDebugInfo(),(XEOScopeImpl)scope, boui );
	}
	
	public static <T extends XEOModelBase> XEOQLBuilder<T> ql( Class<T> modelclass, String ql ) {
		return new XEOQLBuilderImpl<T>( (XEOScopeImpl)XEOScope.getCurrentScope(),  ql );
	}
	
	public static <T extends XEOModelBase> XEOQLBuilder<T> ql( XEOScope scope, Class<T> modelclass, String ql ) {
		return new XEOQLBuilderImpl<T>( (XEOScopeImpl)scope,  ql );
	}
	
}
