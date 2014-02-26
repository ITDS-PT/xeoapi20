package xeo.api.base.impl;

import xeo.api.base.XEOLovPair;

public class XEOLovPairImpl implements XEOLovPair<String> {
	
	private String value;
	private String label;
	
	public XEOLovPairImpl( String value, String label ) {
		this.value = value;
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public String getValue() {
		return this.value;
	}

}
