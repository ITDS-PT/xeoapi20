package xeo.api.builder.def;

public class XEODefBuilderProperties {
	
	// Class with a static final field to localize the factory class for a object load from the database.
	private String javaDictionaryPackage = "xeo.models.";
	
	// Package for user model generated java source files
	private String javaPackage 			 = "xeo.models.";
	
	// Package for the internal generated source files
	private String javaPackageBase 		 = "xeo.models.impl.";
	
	// Path to boconfig
	private String xeoHome 				 = null;
	
	public XEODefBuilderProperties() {
		
	}
	
	public XEODefBuilderProperties( String xeoHome ) {
		this.xeoHome = xeoHome;
	}
	
	public XEODefBuilderProperties(String javaDictionaryPackage,
			String javaPackage, String javaPackageBase) {
		super();
		this.setJavaDictionaryPackage( javaDictionaryPackage );
		this.setJavaPackage( javaPackage );
		this.setJavaPackageBase( javaPackageBase );
	}

	public String getJavaDictionaryPackage() {
		return javaDictionaryPackage;
	}

	public void setJavaDictionaryPackage(String javaDictionaryPackage) {
		if( !javaDictionaryPackage.endsWith(".") ) javaDictionaryPackage += ".";
		this.javaDictionaryPackage = javaDictionaryPackage;
	}

	public String getJavaPackage() {
		return javaPackage;
	}

	public void setJavaPackage(String javaPackage) {
		if( !javaPackage.endsWith(".") ) javaPackage += ".";
		this.javaPackage = javaPackage;
	}

	public String getJavaPackageBase() {
		return javaPackageBase;
	}

	public void setJavaPackageBase(String javaPackageBase) {
		if( !javaPackageBase.endsWith(".") ) javaPackageBase += ".";
		this.javaPackageBase = javaPackageBase;
	}
	
	public String getXEOHome() {
		return xeoHome;
	}
	
	public void setXEOHome(String xeoHome) {
		this.xeoHome = xeoHome;
	}
	
}
