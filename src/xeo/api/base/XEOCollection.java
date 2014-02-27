package xeo.api.base;

import java.util.Collection;

public interface XEOCollection<T extends XEOModelBase> extends Collection<T> {
	
	public int getRecordCount();

}
