package xeo.api.base.exceptions;

public class XEOModelNotDeployedException extends XEORuntimeException {

	private static final long serialVersionUID = 1L;
	
	private String modelName;

	public XEOModelNotDeployedException(String modelName, String message, Throwable cause) {
		super(message, cause);
		this.modelName = modelName;
	}
	
	public String getModelName() {
		return modelName;
	}
	
	

}
