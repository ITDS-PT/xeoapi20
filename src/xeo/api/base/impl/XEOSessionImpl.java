package xeo.api.base.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import netgest.bo.runtime.EboContext;
import netgest.bo.runtime.boThread;
import netgest.bo.system.boApplication;
import netgest.bo.system.boMemoryArchive;
import netgest.bo.system.boPoolManager;
import netgest.bo.system.boPoolOwner;
import netgest.bo.system.boPoolable;
import netgest.bo.system.boSession;
import xeo.api.base.XEOScope;
import xeo.api.base.XEOSession;
import xeo.api.base.impl.XEOScopeImpl.XEOScopePoolable;

public class XEOSessionImpl extends boPoolable implements XEOSession, boPoolOwner {

	private Map<String,ArrayList<XEOScopeImpl>> requestTransids = new Hashtable<String,ArrayList<XEOScopeImpl>>();
	private boApplication bapp = null;
	
	
	private ThreadLocal<EboContext>  	myScopeEboContext = new ThreadLocal<EboContext>();
	
	protected boSession bosession;
	private boPoolable  poolOwner = this;
	
	private boolean     closed = false;
	private boolean 	isWrapper = false;
	
	private StackTraceElement[] createIn = (new Throwable()).getStackTrace();
	
	protected XEOSessionImpl( boSession bosession, boolean wrapper ) {
		this.bosession = bosession;
		this.isWrapper = wrapper;
	}

	protected XEOSessionImpl( boSession bosession, boPoolable owner, boolean wrapper ) {
		this.bosession = bosession;
		this.poolOwner = owner;
		this.isWrapper = wrapper;
	}
	
	@Override
	public String getUserName() {
		checkClosed();
		return this.bosession.getUser().getUserName();
	}
	
	@Override
	public Long getUserBoui() {
		checkClosed();
		return this.bosession.getPerformerBouiLong();
	}
	
	public boThread getThread() {
        return null;
    }

	    
    public XEOScopeImpl getScope( String id ) {

		checkClosed();
		
        EboContext      oEboContext = getEboContext();
        if( oEboContext != null ) {
	        boMemoryArchive boMemArchive = oEboContext.getApplication().getMemoryArchive();
	        boPoolManager   boPoolMgr    = boMemArchive.getPoolManager();
	        oEboContext.setPreferredPoolObjectOwner( poolOwner.poolUniqueId() );
	        
	        XEOScopePoolable t = (XEOScopePoolable)boPoolMgr.getObject( oEboContext, "XEOModelScope:ID:" + id );
	        if( t != null )
	        	addScopeToRequest( oEboContext , t.getScope());
	    
	        return t!=null?t.getScope():null;
        }
        return null;
    }
	    
    private void addScopeToRequest( EboContext oContext, XEOScopeImpl t ) {
        ArrayList<XEOScopeImpl> l = requestTransids.get( oContext.poolUniqueId() );
        if( l != null ) {
        	if ( !l.contains( t.getPoolable().poolUniqueId() ) ) {
        		l.add( t );
        	}
        }
        else {
        	l = new ArrayList<XEOScopeImpl>();
        	l.add( t );
        	requestTransids.put( oContext.poolUniqueId(), l );
        }
    }

    public XEOScope createScope() {
		checkClosed();
    
        EboContext      oEboContext = getEboContext();
        if( oEboContext != null ) {
        	
        	synchronized (oEboContext) {
	        	if( this.bapp == null ) {
	        		this.bapp = oEboContext.getApplication();
	        	}
	        	
	        	String previousOwner = oEboContext.getPreferredPoolObjectOwner();
	        	
	        	try {
			        boMemoryArchive boMemArchive = this.bapp.getMemoryArchive();
			        boPoolManager   boPoolMgr    = boMemArchive.getPoolManager();
			
			        XEOScopeImpl    scope = new XEOScopeImpl( this );
			        oEboContext.setPreferredPoolObjectOwner( this.poolOwner.poolUniqueId() );
			        scope.setEboContext( oEboContext );
			        boPoolMgr.putObject( scope.getPoolable(), new Object[] { this.poolOwner.poolUniqueId() });
			        scope.getPoolable().poolSetStateFull( this.poolOwner.poolUniqueId() );
			        addScopeToRequest( oEboContext , scope );
			        return scope;
	        	}
	        	finally {
	        		oEboContext.setPreferredPoolObjectOwner( previousOwner );
	        	}
			}
        }
        return null;
    }
	
    
    @Override
    public void flush() {
    	
    	release();
    	
        if( this.bapp != null ) {
            EboContext      oEboContext = boApplication.currentContext().getEboContext();
            if( oEboContext != null ) {
    	        boApplication   boApp        = oEboContext.getApplication();
    	        boMemoryArchive boMemArchive = boApp.getMemoryArchive();
	        	ArrayList<XEOScopeImpl> l = requestTransids.get( oEboContext.poolUniqueId() );

	        	if( l != null  ) {
	        		for (XEOScopeImpl sTransId : l) {
	        			sTransId.close();
	        		}
	        	}
	        	boMemArchive.getPoolManager().realeaseAllObjects( this.poolOwner.poolUniqueId() );
            }
	        boMemoryArchive boMemArchive = this.bapp.getMemoryArchive();
	        boPoolManager   boPoolMgr    = boMemArchive.getPoolManager();
	    	boPoolMgr.realeaseAllObjects( this.poolOwner.poolUniqueId() );
        }
    };
    
    @Override
    public void close() {
        this.bosession.closeSession();
        this.closed = true;
    }
    
    public void release() {
		checkClosed();

        EboContext      oEboContext = getEboContext();
        if( oEboContext != null ) {
	        boApplication   boApp        = oEboContext.getApplication();
	        boMemoryArchive boMemArchive = boApp.getMemoryArchive();
	        boPoolManager   boPoolMgr    = boMemArchive.getPoolManager();
	
	        String sLastPoolOwner = oEboContext.getPreferredPoolObjectOwner();
	        try {
	        	ArrayList<XEOScopeImpl> l = requestTransids.get( oEboContext.poolUniqueId() );
	        	if( l != null  ) {
	        		for (XEOScopeImpl sTransId : l) {
	        			sTransId.release();
	        		}
	        	}
			} finally {
	        	oEboContext.setPreferredPoolObjectOwner( sLastPoolOwner );
			}
	        boPoolMgr.realeaseObjects(this.poolOwner.poolUniqueId(), oEboContext);
        }
        
		if( this.myScopeEboContext.get() != null ) {
			this.myScopeEboContext.get().releaseAllObjects();
			this.myScopeEboContext.get().close();
			this.myScopeEboContext.set(null);
		}
        
    }
	
    @Override
    public void poolObjectActivate() {
        
    }

    @Override
    public void poolObjectPassivate() {
        
    }
    
    @Override
    public String poolUniqueId() {
    	
    	if( this.poolOwner == this )
    		return super.poolUniqueId();
    	
    	return this.poolOwner.poolUniqueId();
    }
	
	public EboContext getEboContext() {
		checkClosed();
		EboContext scopeEboContext;
		
		if( this.myScopeEboContext.get() != null )
			return myScopeEboContext.get();
		
		scopeEboContext  = boApplication.currentContext().getEboContext();
		
		if( scopeEboContext == null ) {
			myScopeEboContext.set( this.bosession.createEboContext() );
			boApplication.currentContext().addEboContext( myScopeEboContext.get() );
			scopeEboContext = myScopeEboContext.get();
		}
		else {
			myScopeEboContext.set( scopeEboContext );
		}
		
		return scopeEboContext;
	}

	public boolean isClosed() {
		return closed;
	}
	
	private void checkClosed() {
		if( isClosed() )
			throw new java.lang.IllegalStateException( "Session is closed" );
	}
	
	@Override
	protected void finalize() throws Throwable {
		if( !this.closed && !this.isWrapper ) {
			System.err.println( "XEOSession not closed" );
			System.err.println( "Created in:" );
			StringBuilder sb = new StringBuilder();
			for( StackTraceElement stackElement : this.createIn ) {
				sb.append( "\t" + stackElement.toString() + "\n" );
			}
			System.err.println( sb );
		}
	}
    
}
