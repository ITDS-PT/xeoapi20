package xeo.api.base.exceptions;

public class __XEOQLParserException extends XEORuntimeException {


	private static final long serialVersionUID = 1L;
	
	private String boql;
	
	public __XEOQLParserException(String boql, String message, Throwable cause) {
		super(message, cause);
	}
	
	public String getBoql() {
		return boql;
	}

}
