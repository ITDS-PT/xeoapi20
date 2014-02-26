package xeo.api.base.exceptions;

public class XEODuplicateKeyException extends XEOException {

	private static final long serialVersionUID = 1L;
	
	public static final String CODE = "BO-3054";
	
	private long boui;

	public XEODuplicateKeyException(Long boui, String message, Throwable cause) {
		super(message, cause);
		this.boui = boui;
	}
	
	public long getBoui() {
		return boui;
	}
	
}
