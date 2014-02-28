package xeo.api.base;


public interface XEOModelFactory<T extends XEOModelBase> extends XEOModelAbstractFactory<T> {

	public abstract T create();

	public abstract T load(long boui);

	public abstract T create(XEOModelBase parent);

}