package xeo.api.base.impl;

public class XEOModelRefereceDebugInfo {
	
	private long   referenceModelBoui;
	private String referenceAttribute;
	private int    referenceCollectionIndex;
	private String referenceModel;
	private StackTraceElement[] referenceStack;
	
	public XEOModelRefereceDebugInfo(long referenceModelBoui,
			String referenceAttribute, int referenceCollectionIndex,
			String referenceModel ) {
		
		this();
		
		this.referenceModelBoui = referenceModelBoui;
		this.referenceAttribute = referenceAttribute;
		this.referenceCollectionIndex = referenceCollectionIndex;
		this.referenceModel = referenceModel;
	}
	
	public XEOModelRefereceDebugInfo() {
		this.referenceStack = (new Throwable()).getStackTrace();
	}
	

	public long getReferenceModelBoui() {
		return referenceModelBoui;
	}

	public String getReferenceAttribute() {
		return referenceAttribute;
	}

	public int getReferenceCollectionIndex() {
		return referenceCollectionIndex;
	}

	public String getReferenceModel() {
		return referenceModel;
	}

	public StackTraceElement[] getReferenceStack() {
		return referenceStack;
	}
	

}
