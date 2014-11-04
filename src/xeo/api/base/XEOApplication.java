package xeo.api.base;

import netgest.bo.runtime.EboContext;
import netgest.bo.runtime.boObject;
import netgest.bo.system.boApplication;
import netgest.bo.system.boLoginException;
import netgest.bo.system.boSession;
import netgest.bo.transaction.XTransaction;
import netgest.bo.transaction.XTransactionManager;
import xeo.api.base.impl.XEOApplicationImpl;

public abstract class XEOApplication {
	
	private static XEOApplicationImpl application;
	
	/**
	 * 
	 * Create a new Sessionz
	 * 
	 * @param userName the user name
	 * @param password the password
	 * @return the session created
	 * @throws boLoginException
	 */
	public abstract XEOSession login( String userName, String password ) throws boLoginException;
	
	public static XEOApplication getInstance() {
		if( application == null ) {
			application = new XEOApplicationImpl();
		}
		return application;
	}
	
	/**
	 * 
	 * Wrap a boSession into a XEOSession
	 * 
	 * @param bosession current boSesssion
	 * @return the XEOSession
	 */
	public abstract XEOSession 	wrapSession( boSession bosession );
	
	/**
	 * 
	 * Wrap a bosession in the same scope of boSesssion
	 * 
	 * @param session current boSesssion
	 * @param transactionManager current XTransactionManager
	 * @return the Wrapped Session
	 */
	public abstract XEOSession 	wrapScope( boSession session, XTransactionManager transactionManager );

	/**
	 * Wrap a boSession in the same scope of a XTransaction
	 * 
	 * @param session the current boSession
	 * @param transaction the current XTransaction
	 * @return XEOScope in the same context/scope of the XTransaction 
	 */
	public abstract XEOScope 	wrapScope( boSession session, XTransaction transaction );

	/**
	 * Wrap a EboContext in and the current scope of the EboContext
	 * 
	 * @param eboContext the current EboContext
	 * @return XEOScope in the same context/scope of the EboContext supplied 
	 */
	public abstract XEOScope 	wrapScope( EboContext eboContext );

	/**
	 * Wrap a EboContext in and the current scope of the EboContext
	 * 
	 * @param eboContext the current EboContext
	 * @return XEOScope in the same context/scope of the EboContext supplied 
	 */
	public static XEOScope 	wrapScope() {
		return getInstance().wrapScope( boApplication.currentContext().getEboContext() );
	}
	
	/**
	 * Wrap a boObject with EboContext in and the current scope of the EboContext who loaded
	 * 
	 * @param boObject the boObject to wrap
	 * @return XEOModel of wrapped object 
	 */
	public static XEOModelBase wrapModel( boObject boobject ) {
		return XEOApplicationImpl.wrapObject( boobject );
	}
	
	/**
	 * Wrap a boObject with EboContext in and the current scope of the EboContext who loaded
	 * 
	 * @param boObject the boObject to wrap
	 * @return XEOModel of wrapped object 
	 */
	public static XEOModelBase wrapModel( long boui ) {
		return XEOApplicationImpl.wrapObject( boui );
	}

	/**
	 * Wrap a boObject with EboContext in and the current scope of the EboContext who loaded
	 * 
	 * @param boObject the boObject to wrap
	 * @return XEOModel of wrapped object 
	 */
	@SuppressWarnings("unchecked")
	public static <T> T wrapModel( Class<T> modelClass, long boui ) {
		return (T)XEOApplicationImpl.wrapObject( boui );
	}
	
	public static <T extends XEOModelBase> XEOQLBuilder<T> ql( Class<T> modelclass, String ql ) {
		return XEOApplicationImpl.ql( modelclass, ql );
	}
	
	public static <T extends XEOModelBase> XEOQLBuilder<T> ql( XEOScope scope, Class<T> modelclass, String ql ) {
		return XEOApplicationImpl.ql( scope, modelclass, ql );
	}
	
}
