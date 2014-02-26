package xeo.api.base.exceptions;

public class XEOSaveException extends XEOException {
	
	private static final long serialVersionUID = 1L;
	
	public static final String CODE = "BO-3016";
	
	private long boui;
	
	public XEOSaveException(Long boui, String message, Throwable cause) {
		super(message, cause);
		this.boui = boui;
	}
	
	public long getBoui() {
		return boui;
	}
	

}
