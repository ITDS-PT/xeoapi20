package xeo.api.base.impl;

import java.sql.Connection;
import java.util.Vector;

import netgest.bo.controller.Controller;
import netgest.bo.data.Driver;
import netgest.bo.preferences.PreferenceManager;
import netgest.bo.runtime.EboContext;
import netgest.bo.runtime.boObject;
import netgest.bo.runtime.boRuntimeException;
import netgest.bo.runtime.boThread;
import netgest.bo.system.boApplication;
import netgest.bo.system.boConnectionManager;
import netgest.bo.system.boSessionUser;

public class EboContextWrapper extends EboContext {
	
	EboContext wrappedEboContext;
	String	   preferredPoolOwner;
	Vector<String> sharedOwners = new Vector(1);
	
	public EboContextWrapper( String preferredPoolOwner, EboContext eboContext ) {
		super(eboContext.getBoSession(), eboContext.getRequest(), eboContext.getResponse(), eboContext.getPageContext() );
		this.wrappedEboContext = eboContext;
		this.preferredPoolOwner = preferredPoolOwner;
		sharedOwners.add( this.preferredPoolOwner );
	}
	
	public void poolSetStateFull() {
		wrappedEboContext.poolSetStateFull();
	}

	public void setModeBatch() {
		wrappedEboContext.setModeBatch();
	}

	public void poolSetStateFull(String owner) {
		wrappedEboContext.poolSetStateFull(owner);
	}

	public void setModeBatch(int options) {
		wrappedEboContext.setModeBatch(options);
	}

	public boolean havePoolChilds() {
		return wrappedEboContext.havePoolChilds();
	}

	public boolean isInModeBatch() {
		return wrappedEboContext.isInModeBatch();
	}

	public String poolOwnerContext() {
		return wrappedEboContext.poolOwnerContext();
	}

	public boolean isInModeBatch(int option) {
		return wrappedEboContext.isInModeBatch(option);
	}

	public boolean isInTransaction() {
		return wrappedEboContext.isInTransaction();
	}

	public void setModeBatchOff() {
		wrappedEboContext.setModeBatchOff();
	}

	public String poolUniqueId() {
		if( wrappedEboContext != null ) {
			return wrappedEboContext.poolUniqueId();
		}
		return null;
	}

	public EboContext getEboContext() {
		return wrappedEboContext.getEboContext();
	}

	public void setEboContext(EboContext boctx) {
		wrappedEboContext.setEboContext(boctx);
	}

	public String getXeoWin32Client_adress() {
		return wrappedEboContext.getXeoWin32Client_adress();
	}

	public boThread getThread() {
		return wrappedEboContext.getThread();
	}

	public boSessionUser getSysUser() {
		return wrappedEboContext.getSysUser();
	}

	public int hashCode() {
		return wrappedEboContext.hashCode();
	}

	public EboContext removeEboContext() {
		return wrappedEboContext.removeEboContext();
	}

	public Connection getConnectionData() {
		return wrappedEboContext.getConnectionData();
	}

	public Driver getDataBaseDriver() {
		return wrappedEboContext.getDataBaseDriver();
	}

	public Connection getConnectionDef() {
		return wrappedEboContext.getConnectionDef();
	}

	public Connection getConnectionSystem() {
		return wrappedEboContext.getConnectionSystem();
	}

	public Connection getDedicatedConnectionData() {
		return wrappedEboContext.getDedicatedConnectionData();
	}

	public Connection getDedicatedConnectionDef() {
		return wrappedEboContext.getDedicatedConnectionDef();
	}

	public void beginContainerTransaction() throws boRuntimeException {
		wrappedEboContext.beginContainerTransaction();
	}

	public void commitContainerTransaction() throws boRuntimeException {
		wrappedEboContext.commitContainerTransaction();
	}

	public void rollbackContainerTransaction() throws boRuntimeException {
		wrappedEboContext.rollbackContainerTransaction();
	}

	public boApplication getApplication() {
		return wrappedEboContext.getApplication();
	}

	public PreferenceManager getPreferencesManager() {
		return wrappedEboContext.getPreferencesManager();
	}

	public boConnectionManager getConnectionManager() {
		return wrappedEboContext.getConnectionManager();
	}

	public void addShareOwner(String owner) {
		wrappedEboContext.addShareOwner(owner);
	}

	public void removeShareOwner(String owner) {
		wrappedEboContext.removeShareOwner(owner);
	}

	public Vector getSharedOwners() {
		return this.sharedOwners;
	}

	public String setPreferredPoolObjectOwner(String owner) {
		if( !owner.equals( this.preferredPoolOwner ) ) {
			throw new IllegalArgumentException(  "In a EboContextWrapper the preferred owner can't be changed!" );
		}
		return owner;
	}

	public String getPreferredPoolObjectOwner() {
		return preferredPoolOwner;
	}

	public void beginTransaction() {
		wrappedEboContext.beginTransaction();
	}

	public boObject[] getOnlyDbTransactedObject() {
		return wrappedEboContext.getOnlyDbTransactedObject();
	}

	public void addTransactedObject(boObject object) throws boRuntimeException {
		wrappedEboContext.addTransactedObject(object);
	}

	public boObject[] getObjectsInTransaction() {
		return wrappedEboContext.getObjectsInTransaction();
	}

	public void endTransaction() {
		wrappedEboContext.endTransaction();
	}

	public void clearObjectInTransaction() {
		wrappedEboContext.clearObjectInTransaction();
	}

	public boolean objectIsInTransaction(boObject object) {
		return wrappedEboContext.objectIsInTransaction(object);
	}

	public void forceAllInTransaction(boolean value) {
		wrappedEboContext.forceAllInTransaction(value);
	}

	public boolean getForceAllInTransaction() {
		return wrappedEboContext.getForceAllInTransaction();
	}

	public Controller getController() {
		return wrappedEboContext.getController();
	}

	public void setController(Controller controller) {
		wrappedEboContext.setController(controller);
	}

	public void releaseObjects() {
		wrappedEboContext.releaseObjects();
	}

	public EboContext unWrap() {
		return wrappedEboContext;
	}
	
}
