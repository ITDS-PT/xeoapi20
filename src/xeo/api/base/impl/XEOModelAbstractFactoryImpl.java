package xeo.api.base.impl;

import netgest.bo.runtime.boObjectList;
import xeo.api.base.XEOCollection;
import xeo.api.base.XEOModelAbstractFactory;
import xeo.api.base.XEOModelBase;
import xeo.api.base.XEOQLBuilder;
import xeo.api.base.XEOScope;
import xeo.api.base.impl.ql.XEOQLPreProcessor;

/**
 *
 *
 * @param <T> the type of models produced by this factory
 */
public abstract class XEOModelAbstractFactoryImpl<T extends XEOModelBase> implements XEOModelAbstractFactory<T> {
	
	protected XEOScopeImpl scope;

	protected abstract String getModelName();
	
	
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
