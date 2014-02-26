package xeo.api.builder.def;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class XEOModelDef implements Cloneable {
	
	public enum ModelType {
		MODEL,
		ABSTRACT_MODEL,
		INTERFACE
	}
	
	private XEOModelDef superModelDef;

	private Map<String, XEOAttributeDef> 		attributes 		 = new LinkedHashMap<String, XEOAttributeDef>();
	private Map<String, XEOAttributeDef> 		simpleAttributes = new LinkedHashMap<String, XEOAttributeDef>();
	private Map<String, XEOAttributeObjectDef> 	objectAttributes = new LinkedHashMap<String, XEOAttributeObjectDef>();
	private Map<String, XEOBridgeDef> 			bridges 		 = new LinkedHashMap<String, XEOBridgeDef>();
	
	private List<XEOModelDef>					interfaces 		 = new ArrayList<XEOModelDef>();
	
	private List<String>						javaImports 	 = new ArrayList<String>();
	private List<String>						javaBaseImports  = new ArrayList<String>();
	
	private String modelName;
	private String modelPackage;
	
	private String javaPackage;
	private String javaClassName;

	private String javaBasePackage;
	
	private ModelType modelType;

	private String description;
	
	private boolean isFinal;
	
	public XEOModelDef( ModelType type, String modelName, String modelPackage, String description ) {
		this.modelName = modelName;
		this.modelPackage = modelPackage;
		this.modelType = type;
		this.description =  description;
	}
	
	public XEOModelDef getSuper() {
		return superModelDef;
	}
	
	void setSuper( XEOModelDef superModelDef ) {
		this.superModelDef = superModelDef;
	}

	void addInterface( XEOModelDef interfaceModelDef ) {
		if( !this.interfaces.contains( interfaceModelDef ) ) {
			this.interfaces.add( interfaceModelDef );
		}
	}
	
	public List<XEOModelDef> getImplementedInterfaces() {
		return this.interfaces;
	}
	
	public String getJavaClassName() {
		return javaClassName;
	}
	public String getModelName() {
		return modelName;
	}
	public String getJavaPackage() {
		return javaPackage;
	}
	public String getModelPackage() {
		return modelPackage;
	}

	void put( XEOAttributeDef attribute ) {
		this.simpleAttributes.put( attribute.getModelName(), attribute );
		this.attributes.put( attribute.getModelName(), attribute );
	}
	
	void put( XEOAttributeObjectDef attribute ) {
		this.objectAttributes.put( attribute.getModelName(), attribute );
		this.attributes.put( attribute.getModelName(), attribute );
	}

	void put( XEOBridgeDef bridgeDef) {
		this.bridges.put( bridgeDef.getModelName() , bridgeDef );
		this.attributes.put( bridgeDef.getModelName(), bridgeDef );
	}
	
	public XEOAttributeDef attributes( String name ) {
		return simpleAttributes.get( name );
	}

	public XEOAttributeObjectDef attributesObject( String name ) {
		return attributesObject( name );
	}
	
	public XEOBridgeDef bridges( String name ) {
		return bridges.get( name );
	}
	
	public Collection<String> getAttributesName() {
		return simpleAttributes.keySet();
	}

	public Collection<String> getAttributeObjectNames() {
		return objectAttributes.keySet();
	}
	
	public Collection<String> getBridgesName() {
		return this.bridges.keySet();
	}
	
	void setJavaNames( String javaClass, String javaPackage, String javaBasePackage ) {
		this.javaClassName = javaClass;
		this.javaPackage = javaPackage;
		this.javaBasePackage = javaBasePackage;
	}
	
	public List<XEOAttributeDef> getAttributes() {
		List<XEOAttributeDef> list = new ArrayList<XEOAttributeDef>();
		for( XEOAttributeDef att : this.simpleAttributes.values() )
			list.add( att );
		return list;
	}
	
	public Collection<XEOAttributeDef> getAllAttributes() {
		return this.attributes.values();
	}

	public List<XEOAttributeObjectDef> getAttributesObject() {
		List<XEOAttributeObjectDef> list = new ArrayList<XEOAttributeObjectDef>();
		for( XEOAttributeObjectDef att : this.objectAttributes.values() )
			list.add( att );
		return list;
	}
	
	public List<XEOBridgeDef> getBridges() {
		List<XEOBridgeDef> list = new ArrayList<XEOBridgeDef>();
		
		for( XEOBridgeDef att : this.bridges.values() )
			list.add( att );
		
		return list;
	}

	public ModelType getModelType() {
		return modelType;
	}

	public String getDescription() {
		return description;
	}
	
	public boolean isFinal() {
		return isFinal;
	}
	
	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}
	
	@Override
	public String toString() {
		return this.modelName;
	}

	public List<String> getJavaImports() {
		return javaImports;
	}

	void addJavaImport(String importName) {
		if( this.javaImports.indexOf( importName ) == -1 )
			this.javaImports.add( importName );
	}
	
	public String getJavaBasePackage() {
		return javaBasePackage;
	}
	
	void addJavaBaseImport( String importName ) {
		if( this.javaBaseImports.indexOf( importName ) == -1 )
			this.javaBaseImports.add( importName );

	}
	
	public List<String> getJavaBaseImports() {
		return javaBaseImports;
	}
	
	
	public Collection<XEOAttributeDef> getInheritedAttributes() {
		Map<String, XEOAttributeDef> map = new LinkedHashMap<String,XEOAttributeDef>();
		
		XEOModelDef modelDef = this.getSuper();
		while( modelDef != null ) {
			for( XEOAttributeDef defAttribute : modelDef.getAllAttributes() ) {
				if( !map.containsKey( defAttribute.getModelName() ) && !this.attributes.containsKey( defAttribute.getModelName() ) ) {
					map.put( defAttribute.getModelName(), defAttribute );
				}
			}
			modelDef = modelDef.getSuper();
		}
		return map.values();
	}

}
