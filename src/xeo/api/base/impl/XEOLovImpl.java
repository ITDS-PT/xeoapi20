package xeo.api.base.impl;

import xeo.api.base.XEOLovPair;

public abstract class XEOLovImpl implements XEOLovPair<String>  {
	
	private String label;
	protected String value;
	
	public XEOLovImpl( String value, String label ) {
		this.value = value;
		this.label = label;
	}
	
	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getValue() {
		return value;
	}

}
