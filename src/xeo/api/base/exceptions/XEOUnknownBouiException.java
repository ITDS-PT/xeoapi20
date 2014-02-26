package xeo.api.base.exceptions;

public class XEOUnknownBouiException extends XEORuntimeException {

	private static final long serialVersionUID = 1L;
	
	public static final String CODE = "BO-3015";
	
	private long boui;
	
	public XEOUnknownBouiException(long boui, String message, Throwable cause) {
		super(message, cause);
	}
	
	public long getBoui() {
		return boui;
	}
	
}
