package xeo.api.base.impl;

import java.math.BigDecimal;
import java.util.Date;

import netgest.bo.data.DataRow;
import netgest.bo.runtime.AttributeHandler;
import netgest.bo.runtime.boRuntimeException;
import xeo.api.base.XEOAttribute;

public class XEOAttributeImpl<T> implements XEOAttribute<T> {

	protected static final  Object FLASHBACK_NOT_CHANGED = new Object();
	
	protected AttributeHandler 	attributeHandler;
	protected XEOModelImpl  	model;
	
	// Java type on wrappers
	protected Class<?>			valueClass;
	// Java type on model
	protected Class<?>			modelValueClass;
	
	
	public XEOAttributeImpl( Class<?> classValue, Class<?> modelValueClass, XEOModelImpl model, AttributeHandler attributeHandler ) {
		this.attributeHandler = attributeHandler;
		this.model = model;
		this.valueClass = classValue;
		this.modelValueClass = modelValueClass;
	}

	@Override
	public boolean isNull() {
		try {
			return attributeHandler.getValueObject() == null;
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isEmpty() {
		
		try {
			Object value = attributeHandler.getValueObject();
			
			if( value == null )
				return true;
			
			if( 
					value.equals( DEFAULT_STRING )  
			) {
				return true;
			}
			
			return false;
		}
		catch( boRuntimeException e ) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean wasChanged() {
		try {
			String dbName = attributeHandler.getDefAttribute().getDbName();
			
			DataRow flashbackRow = attributeHandler.getParent().getDataRow()
				.getFlashBackRow();
			
			if( flashbackRow != null ) {
				Object o1 = attributeHandler.getValueObject();
				Object o2 = flashbackRow.getObject( dbName );
				return isValueChanged(o1, o2);
			}
			return false;
		} catch (boRuntimeException e) {
			throw new RuntimeException( e );
		}
	}

	@Override
	public String getLabel() {
		return attributeHandler.getDefAttribute().getLabel();
	}

	@Override
	public String getName() {
		return attributeHandler.getName();
	}

	@Override
	public T getValue() {
		try {
			return wrappValue(  attributeHandler.getValueObject() );
		} catch (boRuntimeException e) {
			throw new RuntimeException( e );
		}
	}

	@Override
	public void setValue(T value) {
		try {
			attributeHandler.setValueObject( unWrappValue(value) );
		} catch (boRuntimeException e) {
			throw new RuntimeException( e );
		}
	}

	@Override
	public T getPersistedValue() {
		Object value = getFlashbackValue();
		if( value == FLASHBACK_NOT_CHANGED ) {
			return getValue();
		}
		return wrappValue( value );
	}

	@Override
	public boolean isRequired() {
		try {
			return attributeHandler.required();
		} catch (boRuntimeException e) {
			throw new RuntimeException( e );
		}
	}

	@Override
	public boolean isRecommended() {
		return attributeHandler.getRecommend();
	}

	@Override
	public boolean isDisabled() {
		try {
			return attributeHandler.isDisabled();
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setDisabled(boolean disabled) {
		if( disabled ) 
			attributeHandler.setDisabled();
		else
			attributeHandler.setEnabled();
	}

	@Override
	public boolean isVisible() {
		try {
			return attributeHandler.isVisible();
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setVisible(boolean visible) {
		if( visible ) 
			attributeHandler.setVisible();
		else
			attributeHandler.setHidden();
	}

	@Override
	public boolean validate() {
		try {
			return attributeHandler.validate();
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setValid(boolean valid) {
		if( valid )
			attributeHandler.setValid();
		else
			attributeHandler.setInvalid();
	}

	@Override
	public boolean isValid() {
		return attributeHandler.isValid();
	}

	@Override
	public void setInvalidMessage(String invalidReason) {
		attributeHandler.setInvalid( invalidReason );
	}

	@Override
	public String getInvalidMessage() {
		return attributeHandler.getErrorMessage();
	}
	
	private boolean isValueChanged( Object o1, Object o2 ) {
		if( o1 == null && o2 != null ) 
			return true;
		if( o2 == null && o1 != null ) 
			return true;
		if( o2 == null && o1 == null )
			return false;
		
		return o1.equals( o2 );
	}
	
	@SuppressWarnings("unchecked")
	private T wrappValue( Object value ) {
		
		Class<?> left = valueClass;
		
		if( value == null ) {
			if( left == String.class )
				return (T)DEFAULT_STRING;
			if( left == Double.class )
				return (T)DEFAULT_DOUBLE;
			if( left == Long.class )
				return (T)DEFAULT_LONG;
			if( left == BigDecimal.class )
				return (T)DEFAULT_BIGDECIMAL;
			if( left == Boolean.class )
				return (T)DEFAULT_BOOLEAN;
			
			return null;
		}
		
		Class<?> right = value.getClass();
		
		if( left == Object.class )
			return (T)left;
		
		if( right == left )
			return (T)value;
		
		if( right == BigDecimal.class && left == Double.class ) 
			return (T)Double.valueOf(((BigDecimal)value).doubleValue());

		if( right == BigDecimal.class && left == Long.class ) 
			return (T)Long.valueOf(((BigDecimal)value).longValue());

		if( right == String.class && left == Boolean.class ) 
			return (T)Boolean.valueOf( "1".equals(((String)value)) );
			
		if( right == java.sql.Timestamp.class && left == java.util.Date.class )
			return (T)new java.util.Date( ((java.sql.Timestamp)value).getTime() );
		
		throw new IllegalArgumentException();
	}

	private Object unWrappValue( T value ) {
		
		if( value == null ) 
			return null;
		
		Class<?> left = modelValueClass;
		Class<?> right = value.getClass();
		
		if( right == left )
			return (T)value;
		
		if( right == Double.class && left == BigDecimal.class ) 
			return BigDecimal.valueOf( (Double)value );
		if( right == Long.class && left == BigDecimal.class ) 
			return BigDecimal.valueOf( (Long)value );
		
		if( right == Boolean.class && left == String.class ) 
			return ((Boolean)value)?"1":"0";
		
		if( right == Date.class && left == java.sql.Timestamp.class ) 
			return new java.sql.Timestamp( ((Date)value).getTime() );
		
		throw new IllegalArgumentException();
	}
	
	@Override
	public String toString() {
		return this.getName() + ":" + this.getValue();
	}
	
	
	protected Object getFlashbackValue() {
		try {
			DataRow flashbackRow = attributeHandler.getParent().getDataRow().getFlashBackRow();
			if( flashbackRow == null && !attributeHandler.getParent().exists() ) {
				return null;
			}
			else if ( flashbackRow == null ) {
				return FLASHBACK_NOT_CHANGED;
			}
			String attributeName = attributeHandler.getDefAttribute().getDbName();
			return flashbackRow.getObject( attributeName );
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}
	
}
