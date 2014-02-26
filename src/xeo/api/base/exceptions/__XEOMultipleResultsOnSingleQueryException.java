package xeo.api.base.exceptions;

public class __XEOMultipleResultsOnSingleQueryException extends XEORuntimeException {

	private static final long serialVersionUID = 1L;
	
	public static final String CODE = "BO-3017";
	
	private String query;

	public __XEOMultipleResultsOnSingleQueryException(String query, String message, Throwable cause) {
		super(message, cause);
		this.query = query;
	}

	public String getQuery() {
		return query;
	}
	
}
