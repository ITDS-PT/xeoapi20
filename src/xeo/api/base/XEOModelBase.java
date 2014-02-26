package xeo.api.base;

import java.util.List;

import xeo.api.base.exceptions.XEOConcurrentModificationException;
import xeo.api.base.exceptions.XEODuplicateKeyException;
import xeo.api.base.exceptions.XEOException;
import xeo.api.base.exceptions.XEOModelValidationException;
import xeo.api.base.exceptions.XEOReferenceViolationException;
import netgest.bo.runtime.boObject;
import netgest.bo.runtime.boRuntimeException;

public interface XEOModelBase {
	
	/**
	 * Retreived a instance reference to this model
	 * @return the instance reference of this model
	 */
	public Long getBoui();
	
	/**
	 * Unwrap this object
	 * @return
	 */
	public boObject unWrap();
	
	
	public static class Names {}

	public void save() 		throws XEOModelValidationException, XEOReferenceViolationException, XEOConcurrentModificationException, XEODuplicateKeyException, XEOException;
	public void destroy() 	throws boRuntimeException, XEOReferenceViolationException, XEOConcurrentModificationException, XEOException;
	
	public XEOModelBase getParent();
	public void setParent(XEOModelBase parent);
//	public List<XEOModelBase> getParents();
	
	public Errors 	  	errors();
	public Parameters 	parameters();
	public Parent 		parent();
	
	public XEOScope   	scope(); 
	
	public String 		name();
	public String 		cardId();
	public String 		cardIdIcon();
	
	public boolean 		isValid();
	public boolean 		isDisabled();
	public void     	disable();
	public void     	enable();
	
	public boolean 		isChanged();
	
	public boolean 		exists();
	
	public interface Errors {
		public void addErrorMessage(String errorMessage);
		public List<String> errorMessages();
		
		public boolean hasErrors();
		public boolean hasErrorsInObjectOnly();
		public boolean hasErrorsInAttributes();
		
		public void    clearErrors();
		public void    clearObjectErrors();
		public void    clearAttributeErrors();
		
		public List<XEOAttribute<?>> attributesInError();
	}
	
	public interface Parameters {
		public void 	put(String name, String value);
		public String 	get(String name);
		public void 	remove(String name);
	}
	
	public interface Parent {
		public void 	removeParent();
		public boolean 	hasParent();
		public boolean  addParent( XEOModelBase parentMode );
	}
	
}