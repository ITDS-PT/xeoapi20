package xeo.api.base.impl;

import java.math.BigDecimal;

import netgest.bo.data.DataRow;
import netgest.bo.runtime.AttributeHandler;
import netgest.bo.runtime.boRuntimeException;
import xeo.api.base.XEOAttributeObject;
import xeo.api.base.XEOModelBase;

public class XEOAttributeObjectImpl<T extends XEOModelBase> extends XEOAttributeImpl<T> implements XEOAttributeObject<T> {

	public XEOAttributeObjectImpl(XEOModelImpl model, AttributeHandler attributeHandler) {
		super(BigDecimal.class, BigDecimal.class,model, attributeHandler);
	}

	@Override
	public long getBoui() {
		try {
			return super.attributeHandler.getValueLong();
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T getValue() {
		try {
			long valueBoui = attributeHandler.getValueLong();
			if( valueBoui != 0 ) {
				return (T)XEOModelFactoryImpl.wrap( 
						new XEOModelRefereceDebugInfo(  this.model.boui, this.getName(), -1, this.model.name() ), 
						model.factory.scope, 
						valueBoui 
				);
			}
			else {
				return null;
			}
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void setValue(T value) {
		try {
			if( value != null ) {
				attributeHandler.setValueLong( value.getBoui() );
			}
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T getPersistedValue() {
		try {
			DataRow flashbackRow = attributeHandler.getParent().getDataRow().getFlashBackRow();
			if( flashbackRow == null && !attributeHandler.getParent().exists() ) {
				return null ;
			}
			else if ( flashbackRow == null ) {
				return getValue();
			}
			String attributeName = attributeHandler.getDefAttribute().getDbName();
			
			long valueBoui = flashbackRow.getLong( attributeName );
			if( valueBoui != 0 ) {
				return (T)XEOModelFactoryImpl.wrap( 
						new XEOModelRefereceDebugInfo(  this.model.boui, this.getName(), -1, this.model.name() ), 
						model.factory.scope, valueBoui 
						) ;
			}
			else {
				return null;
			}
			
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}

}
