package xeo.api.base.impl;

import java.util.ArrayList;

import netgest.bo.boException;
import netgest.bo.runtime.boObject;
import netgest.bo.runtime.boObjectList;
import xeo.api.base.XEOCollection;
import xeo.api.base.XEOModelBase;
import xeo.api.base.XEOQLBuilder;
import xeo.api.base.exceptions.XEOQLParserException;
import xeo.api.base.impl.ql.XEOQLPreProcessor;

/**
 * 
 * Implements the builder pattern to create instances of {@link boObjectList}.
 * Usage would be:
 * 
 * boObjectList list = new
 * boObjectListBuilder(ctx,"select XEOModel").cache(false
 * ).security(true).pagesize(40).build();
 * 
 * 
 * 
 */

public class XEOQLBuilderImpl<T extends XEOModelBase> implements
		XEOQLBuilder<T> {

	/**
	 * The boql expression
	 */
	private String boql = "";

	/**
	 * The scope of the QL
	 */
	private XEOScopeImpl scope;

	/**
	 * The query arguments
	 */
	private Object[] args = new Object[0];

	/**
	 * Individual Arguments
	 */
	private ArrayList<Object> individualArguments = new ArrayList<Object>();

	/**
	 * Whether to use cache or not
	 */
	private boolean useCache = true;

	/**
	 * Whether to use security or not
	 */
	private boolean useSecurity = true;

	/**
	 * Default page
	 */
	private int page = 1;

	/**
	 * The default page size
	 */
	private int pageSize = boObjectList.PAGESIZE_DEFAULT;

	/**
	 * Default order by
	 */
	private String orderBy = "";

	/**
	 * FullText query
	 */
	private String fullText = "";

	/**
	 * 
	 * Creates a boObjectListBuilder with a context and boql expression
	 * 
	 * @param ctx
	 *            The context to execute the query
	 * @param boql
	 *            The boql expression
	 */
	public XEOQLBuilderImpl(XEOScopeImpl scope, String boql) {
		this.scope = scope;
		this.boql = boql;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xeo.api.base.impl.IXEOQLBuilder#args(java.lang.Object[])
	 */
	@Override
	public XEOQLBuilder<T> args(Object[] args) {
		this.args = args;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xeo.api.base.impl.IXEOQLBuilder#argsList(java.lang.Object)
	 */
	@Override
	public XEOQLBuilder<T> argsList(Object... args) {
		this.args = args;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xeo.api.base.impl.IXEOQLBuilder#page(int)
	 */
	@Override
	public XEOQLBuilder<T> page(int page) {
		this.page = page;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xeo.api.base.impl.IXEOQLBuilder#pageSize(int)
	 */
	@Override
	public XEOQLBuilder<T> pageSize(int pageSize) {
		this.pageSize = pageSize;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xeo.api.base.impl.IXEOQLBuilder#cache(boolean)
	 */
	@Override
	public XEOQLBuilder<T> cache(boolean cache) {
		this.useCache = cache;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xeo.api.base.impl.IXEOQLBuilder#security(boolean)
	 */
	@Override
	public XEOQLBuilder<T> security(boolean security) {
		this.useSecurity = security;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xeo.api.base.impl.IXEOQLBuilder#orderBy(java.lang.String)
	 */
	@Override
	public XEOQLBuilder<T> orderBy(String orderBy) {
		this.orderBy = orderBy;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xeo.api.base.impl.IXEOQLBuilder#fullText(java.lang.String)
	 */
	@Override
	public XEOQLBuilder<T> fullText(String fullText) {
		this.fullText = fullText;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xeo.api.base.impl.IXEOQLBuilder#build()
	 */
	@Override
	public XEOCollection<T> execute() throws XEOQLParserException {
		XEOQLPreProcessor preProcessor = new XEOQLPreProcessor(boql,
				this.getArgs());
		String boql = preProcessor.processQl();
		Object[] args = preProcessor.getProcessedParameters();

		try {
			boObjectList boobjectlist = boObjectList.list(
					this.scope.getEboContext(), boql, args, getPage(),
					getPageSize(), getOrderBy(), getFullText(), null, null,
					useSecurity, useCache);
			XEOCollectionImpl<T> returnCollection = new XEOCollectionImpl<T>(
					new ListBoObjectImpl<T>(boobjectlist, scope));
			return returnCollection;
		} catch (boException e) {
			throw new XEOQLParserException(this.boql, e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xeo.api.base.impl.IXEOQLBuilder#getXEOQl()
	 */
	@Override
	public String getXEOQl() {
		return boql;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xeo.api.base.impl.IXEOQLBuilder#getFullText()
	 */
	@Override
	public String getFullText() {
		return fullText;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xeo.api.base.impl.IXEOQLBuilder#getArgs()
	 */
	@Override
	public Object[] getArgs() {

		if (this.individualArguments.size() == 0)
			return args;
		if (this.args.length == 0)
			return individualArguments.toArray();

		Object[] totalArgs = new Object[this.individualArguments.size()
				+ this.args.length];
		int k = 0;
		for (Object curr : args) {
			if (curr instanceof XEOModelBase) {
				totalArgs[k] = ((XEOModelBase) curr).getBoui();
			} else if (curr instanceof boObject) {
				totalArgs[k] = ((boObject) curr).getBoui();
			} else {
				totalArgs[k] = curr;
			}
			k++;
		}
		for (Object curr : individualArguments) {
			if (curr instanceof boObject) {
				totalArgs[k] = ((boObject) curr).getBoui();
			} else {
				totalArgs[k] = curr;
			}
			k++;
		}

		return totalArgs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xeo.api.base.impl.IXEOQLBuilder#isUseCache()
	 */
	@Override
	public boolean isUseCache() {
		return useCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xeo.api.base.impl.IXEOQLBuilder#isUseSecurity()
	 */
	@Override
	public boolean isUseSecurity() {
		return useSecurity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xeo.api.base.impl.IXEOQLBuilder#getPage()
	 */
	@Override
	public int getPage() {
		return page;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xeo.api.base.impl.IXEOQLBuilder#getPageSize()
	 */
	@Override
	public int getPageSize() {
		return pageSize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see xeo.api.base.impl.IXEOQLBuilder#getOrderBy()
	 */
	@Override
	public String getOrderBy() {
		return orderBy;
	}
	
	

}
