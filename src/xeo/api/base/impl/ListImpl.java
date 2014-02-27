package xeo.api.base.impl;

import java.util.Collection;

import xeo.api.base.XEOCollection;
import xeo.api.base.XEOCollectionFilter;
import xeo.api.base.XEOModelBase;

public interface ListImpl<T extends XEOModelBase> extends XEOCollection<T> {
	
	public int getRecordCount();

	Collection<T> filter(XEOCollectionFilter<T> filter);

}
