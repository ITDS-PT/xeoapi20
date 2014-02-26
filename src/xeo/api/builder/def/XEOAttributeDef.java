package xeo.api.builder.def;

import xeo.api.builder.def.XEOTypes.SimpleDataType;

public class XEOAttributeDef {
	
	// BODEF Model Attribute Name
	private String modelName;
	private SimpleDataType modelType;
	private String description;
	private String label;
	private String lovName;
	
	private int    minPrecision;
	private int    maxPrecision;
	
	private boolean generateSetter;
	private boolean generateGetter;
	
	private boolean isFinal = true;
	
	// Value Getter/Setter name
	private String javaGetterSetterName;

	// Java Class holding the value on Wrappers
	private String javaValueClass;
	
	// Java Class holding the value on boObject
	private String javaModelValueClass;
	
	// Static Field Name with the name of the attribute in BODEF
	private String javaStaticFieldName;
	
	// The internal field name of in the class
	private String javaFieldName;
	
	// Class for the attributeHandler
	private String javaClassName;

	// Class for the attributeHandler
	private String javaInterfaceName;
	
	// Specify if the javaClassName is Generic
	private boolean javaClassIsGeneric;

	// Specify if the javaClassName is Generic
	private boolean javaInterfaceIsGeneric;
	
	// Specify if a class must be generated for this attribute
	private boolean generateJavaClass;
	
	// Name of the Getter for the AttributeHandler
	private String javaAttributeGetterName;
	
	// Class LOV Handler
	private String javaLovClassName;
	
	public XEOAttributeDef( String modelName, SimpleDataType modelType, String lovName,String description, int minPrecision, int maxPrecision, boolean generateGetter, boolean generateSetter ) {
		this( modelName, description, modelType );
		this.minPrecision = minPrecision;
		this.maxPrecision = maxPrecision;
		this.javaClassName = modelType.getFieldClassName();
		this.javaModelValueClass = modelType.getModelJavaDataType();
		this.javaInterfaceName = modelType.getFieldInterfaceName();
		this.generateGetter = generateGetter;
		this.generateSetter = generateSetter;
		this.javaClassIsGeneric = modelType.getGeneric();
		this.javaInterfaceIsGeneric = modelType.getInterfaceIsGeneric();
		this.lovName = lovName;
	}
	
	public XEOAttributeDef( String modelName, String description, SimpleDataType modelType ) {

		assert( modelType != null );
		this.modelName = modelName;
		this.modelType = modelType;
		this.javaClassName = modelType.getFieldClassName();
		this.javaInterfaceName = modelType.getFieldInterfaceName();
		this.description = description;
		this.javaClassIsGeneric = modelType.getGeneric();
		
	}
	
	public SimpleDataType getType() {
		return this.modelType;
	}

	public void setJavaNames( 
			String javaFieldName, 
			String javaStaticFieldName, 
			String javaValueClass,
			String javaGetterSetterName,
			String javaAttributeGetterName
		) 
	{
		this.javaFieldName 			 = javaFieldName;
		this.javaStaticFieldName 	 = javaStaticFieldName;
		this.javaValueClass 		 = javaValueClass;
		this.javaGetterSetterName 	 = javaGetterSetterName;
		this.javaAttributeGetterName = javaAttributeGetterName;
	}
	
	void setJavaAttributeGetterName(String javaAttributeGetterName) {
		this.javaAttributeGetterName = javaAttributeGetterName;
	}
	
	public String getJavaStaticFieldName() {
		return javaStaticFieldName;
	}
	
	public String getJavaGetterSetterName() {
		return javaGetterSetterName;
	}
	
	public String getJavaValueClass() {
		return javaValueClass;
	}
	
	public String getModelName() {
		return modelName;
	}
	
	public SimpleDataType getModelType() {
		return modelType;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getJavaFieldName() {
		return javaFieldName;
	}

	public int getMinPrecision() {
		return minPrecision;
	}

	public int getMaxPrecision() {
		return maxPrecision;
	}
	
	public String getJavaClassName() {
		return javaClassName;
	}

	public String getJavaModelValueClass() {
		return javaModelValueClass;
	}
	
	public boolean getGenerateSetter() {
		return generateSetter;
	}
	
	public boolean getGenerateGetter() {
		return generateGetter;
	}
	
	void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}
	
	public boolean getFinal() {
		return isFinal;
	}
	
	@Override
	public String toString() {
		return this.modelName;
	}
	
	public String getJavaAttributeGetterName() {
		return javaAttributeGetterName;
	}
	
	public boolean getJavaClassIsGeneric() {
		return javaClassIsGeneric;
	}
	
	public String getLovName() {
		return lovName;
	}
	
	public String getJavaLovClassName() {
		return javaLovClassName;
	}

	public String getJavaInterfaceName() {
		return javaInterfaceName;
	}
	
	public boolean getGenerateJavaClass() {
		return generateJavaClass;
	}
	
	public boolean getJavaInterfaceIsGeneric() {
		return javaInterfaceIsGeneric;
	}
	
	void setJavaClassIsGeneric( boolean javaClassIsGeneric ) {
		this.javaClassIsGeneric = javaClassIsGeneric;
	}
	
	void setJavaClassName(String javaClass) {
		this.javaClassName = javaClass;
	}

	void setGenerateJavaClass(boolean generateJavaClass) {
		this.generateJavaClass = generateJavaClass;
	}
	
	void setJavaValueClass(String javaValueClass) {
		this.javaValueClass = javaValueClass;
	}
	
	void setJavaLovClassName(String javaLovClassName) {
		this.javaLovClassName = javaLovClassName;
	}
	
	
	
}
