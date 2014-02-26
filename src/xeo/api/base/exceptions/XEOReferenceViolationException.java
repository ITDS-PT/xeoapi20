package xeo.api.base.exceptions;

public class XEOReferenceViolationException extends XEOException {
	
	private static final long serialVersionUID = 1L;

	public static final String CODE = "BO-3023";
	
	private long boui;
	
	public XEOReferenceViolationException(Long boui, String message, Throwable cause) {
		super(message, cause);
	}
	
	public long getBoui() {
		return boui;
	}

}
