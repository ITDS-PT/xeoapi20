package xeo.api.base.exceptions;

public class XEOQLParserException extends XEORuntimeException {


	private static final long serialVersionUID = 1L;
	
	private String boql;
	
	public XEOQLParserException(String boql, String message, Throwable cause) {
		super(message, cause);
	}
	
	public String getBoql() {
		return boql;
	}

}
