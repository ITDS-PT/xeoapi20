package xeo.api.base.exceptions;


public class XEOConcurrentModificationException extends XEOException {
	
	private static final long serialVersionUID = 1L;
	
	public static final String CODE = "BO-3022"; 
	
	private long boui;
	
	public XEOConcurrentModificationException(Long boui, String message, Throwable cause) {
		super(message, cause);
		this.boui = boui;
	}
	
	public long getBoui() {
		return boui;
	}
	
}
