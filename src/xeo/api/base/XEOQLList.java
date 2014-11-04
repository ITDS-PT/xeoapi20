package xeo.api.base;

public interface XEOQLList<T extends XEOModelBase> extends XEOCollection<T> {
	
	public XEOQLBuilder<T> builder( String ql );
	
}
