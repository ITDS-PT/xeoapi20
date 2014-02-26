package xeo.api.base;

import java.math.BigDecimal;




public interface XEOAttributeDecimal extends XEOAttribute<BigDecimal> {
	
	/**
	 * Retrieve the minimum decimals for this attribute.
	 * 
	 * @return number of decimals
	 */
	public int getMinDecimals();
	/**
	 * Retrieve the maximum decimals for this attribute.
	 * 
	 * @return number of decimals
	 */
	public int getMaxDecimals();
	
	/**
	 * Set the value using a double value
	 * @param doubleValue the new value for the attribute
	 */
	public void setValue( double doubleValue );
	
	/**
	 * return the value in the form of double
	 * @return the double form of the number
	 */
	public double getValueDouble();
	
}