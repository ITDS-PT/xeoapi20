package xeo.api.base;

import java.math.BigDecimal;


public interface XEOAttribute<T> {
	
	public static final String		DEFAULT_STRING = "";
	public static final Double		DEFAULT_DOUBLE = Double.valueOf(0);
	public static final Long		DEFAULT_LONG = Long.valueOf(0);
	public static final BigDecimal	DEFAULT_BIGDECIMAL = BigDecimal.valueOf(0);
	public static final Boolean		DEFAULT_BOOLEAN = Boolean.FALSE;
	
	/**
	 * Determines if the value is Null
	 * @return true if is null, false if is empty or not null
	 */
	public abstract boolean isNull();
	/**
	 * Determines if the value is empty. 
	 * It returns true for null values, or if the value matches on of the default values:
	 * 	 	DEFAULT_STRING
	 * 		DEFAULT_DOUBLE
	 * 		DEFAULT_LONG
	 * 		DEFAULT_BIGDECIMAL
	 * 		DEFAULT_BOOLEAN
	 * 
	 * @return true if the value matches the empty conditions
	 */
	public abstract boolean isEmpty();
	
	/**
	 * Compare the persitedValue with the current value and tell if it was modified 
	 * @return true if the value was modified
	 */
	public abstract boolean wasChanged();
	
	/**
	 * Getter to the localized label of the attribute
	 * @return the label of the attribute
	 */
	public abstract String getLabel();
	/**
	 * Return the name declared in the model of the attribute
	 * @return the name declared in the model of the attribute
	 */
	public abstract String getName();
	
	/**
	 * Return the value of the attribute. If the value is null it will be overridden with a default in this cases
	 * 		String 		- DEFAULT_STRING
	 * 		Double 		- DEFAULT_DOUBLE
	 * 		Long   		- DEFAULT_LONG
	 * 		BigDecimal 	- DEFAULT_BIGDECIMAL
	 * 		Float 		- DEFAULT_FLOAT
	 * 
	 *  Note: The default values are only returned, the internal value of the attribute remains null
	 * 
	 * @return the value, or the default if the attribute is null
	 */
	public abstract T getValue();
	
	/**
	 * Set the value of the attribute
	 * @param value the new value for the attribute
	 */
	public abstract void setValue( T value );
	
	/**
	 * Get the original value of the attribute.
	 * 
	 * The original value is the value witch the data was retrieved from the database
	 * 
	 * @return the original value of the attribute
	 */
	public abstract T getPersistedValue();
	
	/**
	 * Check if the attribute is required by the model. If there is a condition, the condition is executed before. 
	 * @return true if the attribute is required
	 */
	public abstract boolean isRequired();
	/**
	 * Check if the attribute is recommended by the model
	 * @return true if the attribute is required.
	 */
	public abstract boolean isRecommended();

	/**
	 * Check if the state of the attribute is set to disabled by the model. 
	 * If there is a condition in the implementation, the condition is executed before.
	 * @return the state of disabled of the attribute
	 */
	public abstract boolean isDisabled();
	
	/**
	 * Set's the state of disabled on the attribute.
	 * This setter don't override the model, if it's disabled by the model the state is kept unchanged
	 * @param disabled the new state of disabled on the attribute
	 */
	public abstract void setDisabled( boolean disabled );

	/**
	 * Check the state of visibility on the attribute.
	 * If there is a condition in the implementation, the condition is executed before.
	 * @return the visibility of the attribute
	 */
	public abstract boolean isVisible();
	/**
	 * Set's the state of visibility on the attribute.
	 * This setter don't override the model, if it's hidden by the model the state is kept unchanged
	 * @param visible the new state of visibility on the attribute
	 */
	public abstract void setVisible( boolean visible );
	
	/**
	 * This method runs the validation conditions of the attribute.
	 * The state of valid property is kept unchanged, if the validation fails, it must set valid property manually. 
	 * @return true if the attribute respect the conditions of the mode, false if doesn't
	 */
	public abstract boolean validate();
	
	/**
	 * Set the state of valid property of the attribute.
	 * This setter only resets a previous state, the validate method is always called before persiting the data on the database.
	 * @param valid
	 */
	public abstract void setValid( boolean valid );
	/**
	 * Get the state of the valid property of the attribute
	 * This method doesn't execute the validation conditions of the attribute, it only returns the current state of the attribute.
	 * @return true if the attribute is marked as valid, false if is marked as invalid
	 */
	public abstract boolean isValid();
	
	/**
	 * Set a the message of the invalid reason in the attribute.
	 * @param invalidReason the condition message that changed the attribute to invalid
	 */
	public abstract void setInvalidMessage(String invalidReason );
	/**
	 * Return the message set by a condition that put the attribute in the state of invalid. 
	 * @return the reason message of the validation condition.
	 */
	public abstract String getInvalidMessage();
	
}
