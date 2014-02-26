package xeo.api.base.impl.bridgemodel;

import netgest.bo.runtime.boBridgeRow;
import netgest.bo.runtime.boObject;
import netgest.bo.runtime.boRuntimeException;
import xeo.api.base.XEOModelBase;
import xeo.api.base.exceptions.XEOException;

public abstract class XEOModelBridgeImpl implements XEOModelBase {
	
	private boBridgeRow bridgeRow;
	
	public XEOModelBridgeImpl( boBridgeRow bridgeRow ) {
		this.bridgeRow = bridgeRow;
	}

	@Override
	public Long getBoui() {
		try {
			return this.bridgeRow.getValueLong();
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public boObject unWrap() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void save() throws XEOException {
		throw new UnsupportedOperationException();
	}

//	public abstract <T extends XEOModelBase> T getParent();
//
//	public abstract <T extends XEOModelBase> T getChild();
	
}
