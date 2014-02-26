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
import xeo.api.base.XEOScope;
import xeo.api.base.XEOSession;

public class XEOApplicationImpl extends XEOApplication {

	boApplication boapplication;
	
	public XEOApplicationImpl() {
		this.boapplication = boApplication.getXEO();
	}
	
	public XEOSession wrapSession( boSession bosession ) {
		return new XEOSessionImpl(bosession);
	}
	
	public XEOSession wrapScope( boSession session, XTransactionManager transactionManager ) {
		return new XEOSessionImpl( session, transactionManager );
	}

	public XEOScope wrapScope( boSession session, XTransaction transaction ) {
		return new XEOScopeImpl( (XEOSessionImpl)wrapSession( session ), transaction );
	}
	
	@Override
	public XEOSession login(String userName, String password) throws boLoginException {
		return new XEOSessionImpl( boapplication.boLogin( userName, password ) );
	}


	@Override
	public XEOScope wrapScope(EboContext eboContext) {
		
		boPoolable poolOwner =  eboContext.getBoSession().getApplication().getMemoryArchive().getPoolManager()
			.getObjectById( eboContext.getPreferredPoolObjectOwner() );
		
		if( poolOwner instanceof XEOScope ) {
			return (XEOScope)poolOwner;
		}
		else {
			return new XEOScopeImpl( (XEOSessionImpl)wrapSession( eboContext.getBoSession() ), poolOwner );
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
	

}
