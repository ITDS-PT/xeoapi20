package xeo.api.base.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import netgest.bo.runtime.AttributeHandler;
import xeo.api.base.XEOAttributeDecimal;

public class XEOAttributeDecimalImpl extends XEOAttributeImpl<BigDecimal> implements XEOAttributeDecimal {
	
	private int minDecimals;
	private int maxDecimals;

	public XEOAttributeDecimalImpl(XEOModelImpl model, AttributeHandler attributeHandler, int minDecimals, int maxDecimals ) {
		super(BigDecimal.class, BigDecimal.class, model, attributeHandler);
		this.minDecimals = minDecimals;
		this.maxDecimals = maxDecimals;
	}

	@Override
	public int getMinDecimals() {
		return minDecimals;
	}

	@Override
	public int getMaxDecimals() {
		return maxDecimals;
	}
	
	@Override
	public BigDecimal getValue() {
		BigDecimal value = super.getValue();
		if( value != null ) {
			value = value.setScale( this.maxDecimals, RoundingMode.HALF_UP );
		}
		return value;
	}
	
	@Override
	public void setValue(BigDecimal value) {
		
		if( value != null ) {
			value = value.setScale( this.maxDecimals, RoundingMode.HALF_UP );
		}
		super.setValue(value);
		
	}

	@Override
	public void setValue(double doubleValue) {
		setValue( BigDecimal.valueOf( doubleValue ) );
	}

	@Override
	public double getValueDouble() {
		BigDecimal value = getValue();
		if( value != null ) {
			return value.doubleValue();
		}
		return 0;
	}

}
