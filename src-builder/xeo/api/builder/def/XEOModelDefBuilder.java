package xeo.api.builder.def;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import netgest.bo.builder.boBuilderOPL;
import netgest.bo.def.boDef;
import netgest.bo.def.boDefAttribute;
import netgest.bo.def.boDefBridge;
import netgest.bo.def.boDefHandler;
import netgest.bo.def.boDefInterface;
import netgest.bo.def.boDefUtils;
import netgest.bo.localizations.MessageLocalizer;
import netgest.bo.system.boApplication;
import netgest.utils.StringUtils;
import netgest.utils.ngtXMLHandler;
import netgest.utils.ngtXMLUtils;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import oracle.xml.parser.v2.XSLException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import xeo.api.builder.def.XEOModelDef.ModelType;
import xeo.api.builder.def.XEOTypes.SimpleDataType;
import xeo.api.builder.generator.XEONamesBeautifier;

public class XEOModelDefBuilder {
	
	XEODefBuilderProperties buildProperties;
	
	// Builder properties e rename of models/attributes
	Properties namesMapping = new Properties();
	
	// Map with all modelsDefinition
	private Map<String, XEOModelDef> modelsDef = new LinkedHashMap<String, XEOModelDef>();
	
	// For lov linked hash map is important because priorities 
	private Map<String, XEOLovDef>   lovsDef = new LinkedHashMap<String, XEOLovDef>();
	
	// bodef folder
	private String p_bodef_dir;
	
	// Map with package of the models
	private Map<String, String> bodefPackage = new LinkedHashMap<String, String>();
	// Map with package of the lovs
	private Map<String, String> boLovPackage = new LinkedHashMap<String, String>();
	
	public XEOModelDefBuilder( XEODefBuilderProperties properties ) {
		this.buildProperties = properties;
	}

	// Return all models definition 
	public Collection<XEOModelDef> getModels() {
		return this.modelsDef.values();
	}
	
	public Collection<String> getLovNames() {
		return this.lovsDef.keySet();
	}

	public XEOLovDef getLovDefinition(String name) {
		return this.lovsDef.get(name);
	}
	
	public Collection<String> getModelsNames() {
		return this.modelsDef.keySet();
	}

	public XEOModelDef getModelDefinition(String name) {
		return this.modelsDef.get(name);
	}
	
	
	public void buildXEOModelDef() {
		
		// Get the bodef dir from bo-config
		p_bodef_dir =  boApplication.getXEO().getApplicationConfig().getDefinitiondir();
		
		// Map with all bodefHandlers
		Map<String, boDefHandler> bodefMap = new HashMap<String, boDefHandler>();
		
		// Map with all lovs. (Parsing xml directly)
		Map<String, List<ngtXMLHandler>> boLovMap = new HashMap<String, List<ngtXMLHandler>>();
		
		// Load the builder properties and renamed models/attributes
		try {
			InputStream is = XEOModelDefBuilder.class.getResourceAsStream( "NameMapping.properties" );
			namesMapping.load( is );
			is.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		// Load bodef files, respecting version name convention in the folder name
		loadBoDefHandlers("", new File(p_bodef_dir), bodefMap);
		
		
		// Parse LOVs in model attributes
		for (boDefHandler defHandler : bodefMap.values()) {
			List<ngtXMLHandler> lovsList = new ArrayList<ngtXMLHandler>(); 
			for( boDefAttribute defAtt : defHandler.getBoAttributes() ) {
				if( defAtt.getLovItems() != null ) {
					lovsList.add( defAtt.getLovItems().getParentNode() );
				}
				if( defAtt.getBridge() != null ) {
					for( boDefAttribute defBridgeAtt : defAtt.getBridge().getBoAttributes() ) {
						if( defBridgeAtt.getLovItems() != null ) {
							lovsList.add( defBridgeAtt.getLovItems().getParentNode() );
						}
					}
				}
			}
			if( lovsList.size() > 0 ) {
				boLovMap.put( defHandler.getName(), lovsList );
				boLovPackage.put( defHandler.getName() , bodefPackage.get( defHandler.getName() ) );
			}
		}
		
		// Parse LOV Files (.xeolov);
		parseLovFiles( "", new File(p_bodef_dir), boLovMap );		
		
		// Build LOV Definition
		for( Entry<String, List<ngtXMLHandler>> lovEntry : boLovMap.entrySet() ) {
			String container = lovEntry.getKey();
			String packageName = boLovPackage.get( container );
			for( ngtXMLHandler xmlHandler : lovEntry.getValue() ) {
				String name;
				String description = null;
				name = xmlHandler.getAttribute("name");
				ngtXMLHandler descriptionNode = xmlHandler.getChildNode("description");
				if( descriptionNode != null ) {
					if( (description = descriptionNode.getText()) == null ) {
						if( descriptionNode.getFirstChild() != null ) {
							description = descriptionNode.getFirstChild().getText();
						}
					}
				}
				
				ngtXMLHandler detailsNode = xmlHandler.getChildNode("details");
				if( detailsNode != null ) {
					List<String> allValues = new ArrayList<String>();
					XEOLovDef lovDef = new XEOLovDef( container, name, packageName, description );
					for( ngtXMLHandler detailNode : detailsNode.getChildNodes() ) {
						String value;
						String label;
						if( "item".equalsIgnoreCase( detailNode.getNodeName() ) ) {
							value = detailNode.getChildNodeText( "value", "");
							label = detailNode.getChildNodeText( "label", "");
							// Avoid duplicated keys of the lov... doesn't make sense
							if( allValues.indexOf( value ) == -1 ) {
								allValues.add( value );
								lovDef.addItem(  value, label );
							}
						}
					}
					lovsDef.put( name , lovDef );
				}
			}
		}
		
		
		// Build Java types LOV
		for( XEOLovDef lovDef : lovsDef.values() ) {
			lovDef.setJavaNames(
					XEONamesBeautifier
					.convertToClassName(lovDef.getName()) + "Lov",
					this.buildProperties.getJavaPackageBase() + XEONamesBeautifier.convertToPackageName( lovDef.getPackageName()  )		
			);

			List<String> duplicatedLabels = new ArrayList<String>();
			List<String> allLabels = new ArrayList<String>();
			
			// Resolve duplicate labels
			for( XEOLovDef.XEOLovItem item : lovDef.getItems() ) {
				String staticName =  XEONamesBeautifier.convertStaticFieldName( item.getLabel() );
				if( allLabels.indexOf( staticName ) != -1 ) {
					duplicatedLabels.add( staticName );
				}
				allLabels.add( staticName );
			}
			allLabels.clear();
			
			for( XEOLovDef.XEOLovItem item : lovDef.getItems() ) {
				String staticName =  XEONamesBeautifier.convertStaticFieldName( item.getLabel() );
				// If the label is duplicated, add a $ and the value two create unique identifier
				if( duplicatedLabels.indexOf( staticName ) != -1 ) {
					staticName +=  "$" + item.getValue();
				}
				item.setJavaNames( staticName );
			}
		}
		
		
		// Build ModelDef
		for (boDefHandler defHandler : bodefMap.values()) {
			XEOModelDef.ModelType type = ModelType.MODEL;
			
			// Check the correct type of the model
			switch( defHandler.getClassType() ) {
				case boDefHandler.TYPE_ABSTRACT_CLASS:
					type = ModelType.ABSTRACT_MODEL;
					break;
				case boDefHandler.TYPE_CLASS:
					type = ModelType.MODEL;
					break;
				case boDefHandler.TYPE_INTERFACE:
					type = ModelType.INTERFACE;
					break;
			}
			
			// Get the description of the model from XML
			String description = "";
			NodeList list = ((XMLElement)defHandler.getNode()).getElementsByTagName("general");
			if( list.getLength() > 0 ) {
				description  = getDescription( list.item(0) );
			}
			
			// Create the instance of XEOModelDef representing the .xeomodel boDefHandler
			XEOModelDef modelDef = new XEOModelDef(type, defHandler.getName(),
					this.bodefPackage.get( defHandler.getName() ), description );
			modelsDef.put( defHandler.getBoName(), modelDef);
		}
		
		// Create XEOModelImpl definition. (The base class for all models classes)
		XEOModelDef modelDefBase = new XEOModelDef( ModelType.ABSTRACT_MODEL, "XEOModelImpl","_SYSTEM$1.0", "");
		modelDefBase.setJavaNames( "XEOModelImpl", "XEOModelImpl", "xeo.api.base.impl" , "xeo.api.base.impl" );
		
		// Create XEOModelBase interface definition. (The base interface for all models)
		XEOModelDef modelInterfaceBase = new XEOModelDef( ModelType.INTERFACE, "XEOModelBase", "", "" );
		modelInterfaceBase.setJavaNames( "XEOModelBase", "XEOModelImpl" , "xeo.api.base", "xeo.api.base");
		
		// Build Hierarchy of the models and update XEOModelDef
		for (XEOModelDef modelDef : modelsDef.values()) {
			
			boDefHandler defHandler = bodefMap.get(modelDef.getModelName());
			if (!StringUtils.isEmpty(defHandler.getBoSuperBo())) {
				XEOModelDef superModelDef = modelsDef.get(defHandler
						.getBoSuperBo());
				
				modelDef.setSuper(superModelDef);
			}
			
			if( modelDef.getSuper() == null ) {
				modelDef.setSuper( modelsDef.get( "boObject" ) );
			}
			
			if( modelDef.getModelType() == ModelType.INTERFACE ) {
				// Override the super on interfaces, the IboObject is the base interface
				// for all models interfaces
				if( "boObject".equals( modelDef.getSuper().getModelName() ) ) {
					modelDef.setSuper( modelsDef.get( "IboObject" ) );
				}
				// All interfaces implements IboObject
				if( !"IboObject".equals( modelDef.getModelName() ) ) {
					modelDef.addInterface( modelsDef.get( "IboObject" ) );
				}
				else {
					// If the model interface all ready extends another one, keep it.
					modelDef.setSuper( modelInterfaceBase );
				}
			}
			else {
				if( "boObject".equals( modelDef.getModelName() ) ) {
					// If the model all ready extends another one, keep it.
					modelDef.setSuper( modelDefBase );
					modelDef.addInterface( modelsDef.get( "IboObject" ) );
				}
			}
			
		}

		// Associate the interface to the model for all declared models in the interface 
		for (XEOModelDef modelDef : modelsDef.values()) {
			boDefHandler defHandler = bodefMap.get(modelDef.getModelName());
			String[] implementedInterfaces = defHandler.getImplements();
			for( String interfaceName : implementedInterfaces ) {
				XEOModelDef interfaceDef = modelsDef.get( interfaceName );
				if( interfaceDef != null ) {
					modelDef.addInterface( interfaceDef );
				}
			}
			
		}
		
		// Associate the interface declared in the model to the XEOModelDef
		for (XEOModelDef modelDef : modelsDef.values()) {
			if( modelDef.getModelType() == ModelType.INTERFACE ) {
				boDefInterface defHandler = (boDefInterface)bodefMap.get(modelDef.getModelName());
				String[] modelNamesImplementing = defHandler.getImplObjects();
				if( modelNamesImplementing != null ) {
					for( String modelNameImplementing : modelNamesImplementing ) {
						XEOModelDef modeImplementingDef = modelsDef.get( modelNameImplementing );
						if( modeImplementingDef != null ) {
							modeImplementingDef.addInterface( modelDef );
						}
					}
				}
			}
		}
		
		// Inherit the attributes from the interfaces
		for (XEOModelDef modelDef : modelsDef.values()) {
			boDefHandler defHandler = bodefMap.get(modelDef.getModelName());
			
			buildAttributes(modelDef, defHandler);
			
			if( modelDef.getModelType() != ModelType.INTERFACE ) {
				for( XEOModelDef interfaces : modelDef.getImplementedInterfaces() ) {
					boDefHandler defInterfaceHandler = bodefMap.get(interfaces.getModelName());
					if( defHandler != null ) {
						buildAttributes(modelDef, defInterfaceHandler );
					}
				}
			}
		}

		// Generate names for java classes and packages of the models
		for (XEOModelDef modelDef : this.modelsDef.values()) {
			
			String packageName = this.bodefPackage.get( modelDef.getModelName() );		
			if( packageName == null ) {
				packageName = "system";
			}
			
			String modelJavaClassName = XEONamesBeautifier
					.convertToClassName(
							getModelMappedName(modelDef.getModelName(), modelDef)
						);

			String modelJavaClassNameBase = XEONamesBeautifier
					.convertToClassName(
							getModelMappedName(modelDef.getModelName(), modelDef)
						) + "Base";
			
			
			boolean found = false;
			int dup = 0;
			
			String newClassName = modelJavaClassName; 
			String newClassBaseName = modelJavaClassNameBase;
		
			do {
				found = false;
				for( XEOModelDef modelDefCheckDup : this.modelsDef.values() ) {
					if( modelDefCheckDup.equals( modelDef ) ) {
						continue;
					}
					if( newClassName.equals( modelDefCheckDup.getJavaClassName() ) ) {
						dup++;
						newClassName = modelJavaClassName + dup;
						found = true;
						break;
					}
					if( newClassBaseName.equals( modelDefCheckDup.getJavaClassName() ) ) {
						dup++;
						newClassBaseName = modelJavaClassNameBase + dup;
						found = true;
						break;
					}
					if( newClassName.equals( modelDefCheckDup.getJavaBaseClassName() ) ) {
						newClassName = modelJavaClassName + dup;
						found = true;
						break;
					}
					if( newClassBaseName.equals( modelDefCheckDup.getJavaBaseClassName() ) ) {
						dup++;
						newClassBaseName = modelJavaClassNameBase + dup;
						found = true;
						break;
					}
				}
			} while( found );
			
			
			
			modelDef.setJavaNames(
							newClassName,
							newClassBaseName,
							this.buildProperties.getJavaPackage() + XEONamesBeautifier.convertToPackageName( packageName  ),		
							this.buildProperties.getJavaPackageBase() + XEONamesBeautifier.convertToPackageName( packageName  )		
			);
		}

		// Generate javaNames for the java attributes
		for (XEOModelDef modelDef : this.modelsDef.values()) {
			for (String attName : modelDef.getAttributesName()) {
				XEOAttributeDef attributeDef = modelDef.attributes(attName);
				
				String mappedName = getModelMappedName(attName, modelDef, attributeDef );
				
				String javaValueClass = getMappedJavaType(attributeDef.getType().getJavaDataType(), modelDef, attributeDef);
				
				modelDef.attributes(attName)
						.setJavaNames(
								XEONamesBeautifier
										.convertToFieldName(mappedName),
								XEONamesBeautifier
										.convertStaticFieldName( mappedName ),
										javaValueClass,
								XEONamesBeautifier
										.convertToJavaGetterSetter( mappedName ),
								XEONamesBeautifier
										.convertToJavaGetterSetter( mappedName )
						);
				
				// Lov
				if( !StringUtils.isEmpty( attributeDef.getLovName()) ) {
					XEOLovDef lovDef;
					if( ( lovDef = lovsDef.get( attributeDef.getLovName() ) ) != null ) {
						javaValueClass = lovDef.getJavaPackage() + "." + lovDef.getJavaClassName();
						attributeDef.setGenerateJavaClass( true );
						attributeDef.setJavaClassIsGeneric( false );						
						attributeDef.setJavaValueClass( javaValueClass );
						attributeDef.setJavaLovClassName( javaValueClass );
						attributeDef.setJavaClassName( XEONamesBeautifier.convertToClassName( attributeDef.getModelName() ) );
					}
				}
				
				
			}

			for (XEOAttributeObjectDef attributeDef : modelDef
					.getAttributesObject()) 
			{
				
				String mappedName = getModelMappedName(attributeDef.getModelName(), modelDef, attributeDef );
				String javaValueClass = getMappedJavaType(attributeDef.getType().getJavaDataType(), modelDef, attributeDef);
				
				attributeDef.setJavaNames(XEONamesBeautifier
						.convertToFieldName(mappedName),
						XEONamesBeautifier.convertStaticFieldName(mappedName), 
						javaValueClass,
						XEONamesBeautifier.convertToJavaGetterSetter(mappedName),
						XEONamesBeautifier.convertToJavaGetterSetter(mappedName)
					);
			}

			for (String bridgeName : modelDef.getBridgesName()) {
				XEOAttributeDef bridgeDef = modelDef.bridges(bridgeName);
				String mappedName = getModelMappedName( bridgeName, modelDef, bridgeDef );
				String javaValueClass = getMappedJavaType(bridgeDef.getType().getJavaDataType(), modelDef, bridgeDef);
				
				bridgeDef
						.setJavaNames(XEONamesBeautifier
								.convertToFieldName(mappedName),
								XEONamesBeautifier
										.convertStaticFieldName(mappedName),
										javaValueClass,
								XEONamesBeautifier
										.convertToJavaGetterSetter(mappedName),
								XEONamesBeautifier
										.convertToJavaGetterSetter(mappedName)
								);
			}
			
			for( XEOAttributeDef attributeDef : modelDef.getAllAttributes() ) {
				if( attributeDef instanceof XEOAttributeObjectDef ) {
					XEOAttributeObjectDef attObjectDef = (XEOAttributeObjectDef) attributeDef;
					if( attObjectDef.getTypedReferences().size() > 0 ) {
						
						attributeDef.setJavaClassIsGeneric(false);
						attributeDef.setJavaClassName(
								XEONamesBeautifier.convertToClassName( attributeDef.getModelName() )
						);
						//attributeDef.setGenerateJavaClass( true );
						
					}
				}
			}
			
		}
		
		// Verify if a bridge generate a Model
		for( XEOModelDef modelDef : this.modelsDef.values() ) {
			for( XEOBridgeDef bridgeDef : modelDef.getBridges() ) {
				if( bridgeDef.getAttributes().size() > 0 ) {
					bridgeDef.setJavaClassIsGeneric( false );
					bridgeDef.setGenerateJavaClass( true );
					bridgeDef.setJavaClassName( modelDef.getJavaClassName() + bridgeDef.getJavaClassName() );
				}
			}
		}
		
		// Create the dependence tree
		DependencieTree tree =  createDependenciesTree();
		
		// Rename fields in conflict
		resolveHierarchyAttributeConflits( tree );	
		
		// Calculate final fields
		calculateFinalProperties( tree );
		
		// Resolve generic conflicts in the hierarchy for XEOAttributeObject Handlers
		resolveHierarchyAttributeNameClash( tree );	
		
		// Calculate final fields
		calculateFinalProperties( tree );
		
		// Generate Imports list for the java source files
		generateImports();
		
	}
	
	private void generateImports() {
		
		for( XEOModelDef model : this.modelsDef.values() ) {
			
			// Interface imports
			for( XEOModelDef interfaceModel : model.getImplementedInterfaces() ) {
				model.addJavaBaseImport( 
						interfaceModel.getJavaPackage() +
						"." +
						interfaceModel.getJavaClassName()
					); 
			}
			
			// Super imports
			XEOModelDef superModelDef = model.getSuper();
			if( superModelDef != null ) {
				model.addJavaBaseImport( 
						superModelDef.getJavaPackage() +
						"." +
						superModelDef.getJavaClassName()
					); 
			}
			
			
			// Attribute Imports
			for( XEOAttributeDef attDef : model.getAllAttributes() ) {
				
				if( !StringUtils.isEmpty(attDef.getLovName()) ) {
					if( this.lovsDef.get( attDef.getLovName() ) != null ) {
						if( model.getModelType() != ModelType.INTERFACE ) {
							model.addJavaBaseImport( "netgest.bo.runtime.boRuntimeException" );						
							model.addJavaBaseImport( "netgest.bo.runtime.AttributeHandler" );
						}
					}
				}
				
				if( attDef instanceof XEOAttributeObjectDef ) {
					
					XEOAttributeObjectDef attObjDef = (XEOAttributeObjectDef) attDef;
					model.addJavaBaseImport( 
								attObjDef.getReferencedModel().getJavaPackage() +
								"." +
								attObjDef.getReferencedModel().getJavaClassName() 
							);
					
					if( model.getModelType() == ModelType.INTERFACE ) {
						if( attDef instanceof XEOBridgeDef ) {
							model.addJavaBaseImport( "xeo.api.base.XEOAttributeCollection" );						
						}
					}
					
					
				}
			}
		}
	}
	
	private void buildAttributes( XEOModelDef modelDef, boDefHandler defHandler ) {
		boDefAttribute[] bodefAttributes = defHandler.getAttributesDef();
		for (boDefAttribute bodefAttribute : bodefAttributes) {

			if (bodefAttribute.getAtributeType() == boDefAttribute.TYPE_OBJECTATTRIBUTE) {
				
				XEOModelDef referencedObjectDefinition =
						this.modelsDef.get( bodefAttribute.getReferencedObjectName() );
				
				if( referencedObjectDefinition != null ) {
					if( "boObject".equals(referencedObjectDefinition.getModelName()) ) {
						referencedObjectDefinition = this.modelsDef.get("IboObject");
					}
				
					if (bodefAttribute.getBridge() == null) {
						// Simple Object Attribute
						XEOAttributeObjectDef attributeDef = new XEOAttributeObjectDef(
								bodefAttribute.getName(),
								getDescription( bodefAttribute.getNode() ),
								convertToJavaTypes(bodefAttribute),
								referencedObjectDefinition);
						modelDef.put(attributeDef);
						addTypedReferenceToAttributeObject( modelDef, bodefAttribute, attributeDef );
					} else {

						// Object Attribute Collection
						XEOBridgeDef attributeDef = new XEOBridgeDef(
								bodefAttribute.getName(),
								getDescription( bodefAttribute.getNode() ),
								referencedObjectDefinition);
						modelDef.put(attributeDef);

						boDefBridge bodefBridge = bodefAttribute
								.getBridge();
						
						addTypedReferenceToAttributeObject( modelDef, bodefAttribute, attributeDef );
						
						// Collection Attributes;
						for (boDefAttribute bodefBridgeAttribute : bodefBridge
								.getBoAttributes()) {
							
							// Skip default bridge attributes;
							if( bodefBridgeAttribute.getName().equals("LIN") ||
								bodefBridgeAttribute.getName().equals( bodefBridge.getName() )
									)
							{
								continue;
							}
							
							if (bodefBridgeAttribute.getAtributeType() != boDefAttribute.TYPE_OBJECTATTRIBUTE) {
								
								String description = getDescription( bodefBridgeAttribute.getNode() );
								
								XEOAttributeDef attributeBridgeDef = new XEOAttributeDef(
										bodefBridgeAttribute.getName(),
										convertToJavaTypes(bodefBridgeAttribute),
										bodefBridgeAttribute.getLOVName(),
										description,
										bodefBridgeAttribute.getMinDecimals(),
										bodefBridgeAttribute.getDecimals(),
										true,
										getGenerateSetter( modelDef.getModelName(), bodefBridge.getName() + "." + bodefBridgeAttribute.getName() )
									);
								attributeDef
										.putAttribute(attributeBridgeDef);
								
							} else {
								XEOAttributeObjectDef attributeBridgeDef = new XEOAttributeObjectDef(
										bodefBridgeAttribute.getName(),
										getDescription( bodefBridgeAttribute.getNode() ),
										modelsDef.get(bodefBridgeAttribute
												.getReferencedObjectName()));
								attributeDef
										.putAttribute(attributeBridgeDef);
								
								addTypedReferenceToAttributeObject( modelDef, bodefBridgeAttribute, attributeBridgeDef );
								
							}
						}
					}
				}
			} else {
				// Simple Attribute
				String description = getDescription( bodefAttribute.getNode() );
				XEOAttributeDef attributeDef = new XEOAttributeDef(
						bodefAttribute.getName(),
						convertToJavaTypes(bodefAttribute),
						bodefAttribute.getLOVName(),
						description,
						bodefAttribute.getMinDecimals(),
						bodefAttribute.getDecimals(),
						true,
						getGenerateSetter( modelDef.getModelName(), bodefAttribute.getName() )
					);
				modelDef.put(attributeDef);

			}
		}
	}
	
	private void addTypedReferenceToAttributeObject( XEOModelDef model, boDefAttribute bodefAtt, XEOAttributeObjectDef attributeDef ) {
		String[] modelNames = bodefAtt.getObjectsName();
		
		XEOModelDef referencedModel = attributeDef.getReferencedModel();
		
		if( modelNames != null ) {
			for( String modelName : modelNames ) {
				XEOModelDef typedReferencedModel = this.modelsDef.get( modelName );
				if( typedReferencedModel != null ) {
					if( "boObject".equals( modelNames ) ) {
						typedReferencedModel = this.modelsDef.get("IboObject");
					}
					
					if( !referencedModel.getModelName().equals( typedReferencedModel.getModelName() ) ) {
						if( canModelCastTo(referencedModel, typedReferencedModel  ) ) {
							attributeDef.addTypedReference( typedReferencedModel );
						}
						else {
							String msg = String.format( 
									"WARNING: Attribute [%s] in model [%s] reference a type [%s] not compatible with [%s]", 
									attributeDef.getModelName(), model.getModelName(),
									typedReferencedModel.getModelName(), referencedModel.getModelName()
									);
							
							System.out.println( msg  );
						}
					}
					
				}
			}
		}
	}
	

	private SimpleDataType convertToJavaTypes(boDefAttribute xeoDeclaredType) {
		// attributeText
		// attributeNumber
		// attributeObject
		// attributeObjectCollection
		// attributeDate
		// attributeDateTime
		// attributeBoolean
		// attributeSequence
		// attributeCurrency
		// attributeBinaryData
		// attributeDuration
		// attributeLongText

		String s_xeoDeclaredType = xeoDeclaredType.getAtributeDeclaredType();
		if (s_xeoDeclaredType == null) {
			s_xeoDeclaredType = "attributeNumber";
		}

		if ("attributeNumber".equals( s_xeoDeclaredType ) && xeoDeclaredType.getDecimals() > 0 ) {
			s_xeoDeclaredType = "attributeDecimal";
		}

		return XEOTypes.SimpleDataType.valueOf(s_xeoDeclaredType.substring(9));
	}

	private static final void fillDefaultAttributes( boDefHandler bodef ) {

		ngtXMLHandler atts = bodef.getChildNode("attributes");
		if (bodef.implementsSecurityRowObjects()) {
			boBuilderOPL builderOPL = new boBuilderOPL(bodef, atts);
			builderOPL.createOPLAttributes();
		}

		fillBrigdesAtts(bodef);
		bodef.refresh();

	}

	private static final void fillBrigdesAtts(boDef def) {
		boDefAttribute[] atts = def.getBoAttributes();

		for (short i = 0; i < atts.length; i++) {
			if ((atts[i].getAtributeType() == boDefAttribute.TYPE_OBJECTATTRIBUTE)
					&& (atts[i].getMaxOccurs() > 1)) {
				if (!atts[i].getBridge().hasAttribute("LIN")) {
					if (atts[i].getChildNode("bridge") == null) {
						atts[i].getNode().appendChild(
								atts[i].getNode().getOwnerDocument()
										.createElement("bridge"));
					}

					if (atts[i].getChildNode("bridge").getChildNode(
							"attributes") == null) {
						atts[i].getChildNode("bridge")
								.getNode()
								.appendChild(
										atts[i].getNode().getOwnerDocument()
												.createElement("attributes"));
					}

					atts[i].getChildNode("bridge")
							.getChildNode("attributes")
							.getNode()
							.appendChild(
									boDefUtils
											.createAttribute(
													"LIN",
													"LIN",
													MessageLocalizer
															.getMessage("LINE"),
													"attributeNumber",
													"NUMBER", 0, false, atts[i]
															.getNode()
															.getOwnerDocument())
							);
				}
			}
		}
	}
	
	public static final void fillSystemAttributes( boDefHandler bodef ) {

		ngtXMLHandler atts = bodef.getChildNode("attributes");

		if (!bodef.getBoIsSubBo()) {
			
			if (!bodef.hasAttribute("PARENT")) {
				atts.getNode().appendChild(
						boDefUtils.createAttribute("PARENT", "PARENT$",
								"", "attributeObject",
								"object.boObject", 0, false, atts.getNode()
										.getOwnerDocument()));
			}
			
			if (!bodef.hasAttribute("PARENTCTX")) {
				atts.getNode().appendChild(
						boDefUtils.createAttribute("PARENTCTX", "PARENTCTX$",
								MessageLocalizer.getMessage("CREATION_CONTEXT"), "attributeObject",
								"object.boObject", 0, false, atts.getNode()
										.getOwnerDocument()));
			}

			if (!bodef.hasAttribute("TEMPLATE")
					&& !bodef.getName().equals("Ebo_Template")
					&& !bodef.getName().equals("Ebo_Map")) {
				atts.getNode().appendChild(
						boDefUtils.createAttribute("TEMPLATE", "TEMPLATE$",
								MessageLocalizer.getMessage("MODEL"), "attributeObject",
								"object.Ebo_Template", 0, false, atts.getNode()
										.getOwnerDocument()));
			}

			if (!bodef.hasAttribute("BOUI")) {
				atts.getNode().appendChild(
						boDefUtils.createAttribute("BOUI", "BOUI", "BOUI",
								"attributeNumber", "", 0, false, atts.getNode()
										.getOwnerDocument()));
			}

			if (!bodef.hasAttribute("CLASSNAME")) {
				atts.getNode().appendChild(
						boDefUtils.createAttribute("CLASSNAME", "CLASSNAME",
								MessageLocalizer.getMessage("OBJECT_CATEGORY"), "attributeText", "",
								50, false, atts.getNode().getOwnerDocument()));
			}

			if (!bodef.hasAttribute("CREATOR")) {
				atts.getNode().appendChild(
						boDefUtils.createAttribute("CREATOR", "CREATOR$",
								MessageLocalizer.getMessage("CREATOR"), "attributeObject",
								"object.iXEOUser", 0, false, atts.getNode()
										.getOwnerDocument()));
			}

			if (!bodef.hasAttribute("SYS_DTCREATE")) {
				atts.getNode().appendChild(
						boDefUtils.createAttribute("SYS_DTCREATE",
								"SYS_DTCREATE", MessageLocalizer.getMessage("CREATION_DATE"),
								"attributeDateTime", "", 0, false, atts
										.getNode().getOwnerDocument()));
			}

			if (!bodef.hasAttribute("SYS_DTSAVE")) {
				atts.getNode().appendChild(
						boDefUtils.createAttribute("SYS_DTSAVE", "SYS_DTSAVE",
								MessageLocalizer.getMessage("LAST_UPDATE_DATE"),
								"attributeDateTime", "", 0, false, atts
										.getNode().getOwnerDocument()));
			}

//			if (!bodef.hasAttribute("SYS_ORIGIN")) {
//				atts.getNode().appendChild(
//						boDefUtils.createAttribute("SYS_ORIGIN", "SYS_ORIGIN",
//								MessageLocalizer.getMessage("DATA_ORIGIN"), "attributeText", "", 30,
//								false, atts.getNode().getOwnerDocument()));
//			}
//			if (!bodef.hasAttribute("SYS_FROMOBJ")) {
//				atts.getNode().appendChild(
//						boDefUtils.createAttribute("SYS_FROMOBJ",
//								"SYS_FROMOBJ$", MessageLocalizer.getMessage("ORIGIN_OBJECT"),
//								"attributeObject", "object.boObject", 0, false,
//								atts.getNode().getOwnerDocument()));
//			}
			if (bodef.implementsSecurityRowObjects())
			{
				boBuilderOPL builderOPL=new boBuilderOPL(bodef, atts);
				builderOPL.createOPLAttributes();
			}
		}

		fillBrigdesAtts(bodef);
		bodef.refresh();
	}
	
	private final String getModelMappedName( String name, XEOModelDef modelDef ) {
		String key = buildNameMappingKey( modelDef, MappingKeys.name );
		String ret = this.namesMapping.getProperty( key );
		if( ret == null ) {
			ret = name;
		}
		return ret;
	}

	private final boolean getGenerateSetter( String modelName, String attributeName ) {
		String key = buildNameMappingKey( modelName, attributeName, MappingKeys.setter );
		String ret = this.namesMapping.getProperty( key );
		if( ret == null ) {
			return true;
		}
		return Boolean.valueOf( ret );
	}
	
	
	private final String getModelMappedName( String name, XEOModelDef modelDef, XEOAttributeDef attributeDef ) {
		String key = buildNameMappingKey( modelDef, attributeDef, MappingKeys.name );
		String ret = this.namesMapping.getProperty( key );
		if( ret == null ) {
			ret = name;
		}
		return ret;
	}
	
	private final String getMappedJavaType( String type, XEOModelDef modelDef, XEOAttributeDef attributeDef ) {
		String key = buildNameMappingKey( modelDef, attributeDef, MappingKeys.javaValueType );
		String ret = this.namesMapping.getProperty( key );
		if( ret == null ) {
			ret = type;
		}
		return ret;
	}
	
	
	private final String buildNameMappingKey( XEOModelDef modelDef, MappingKeys type ) {
		 return "model." + modelDef.getModelName() + "." + type.name();
	}

	private final String buildNameMappingKey( XEOModelDef modelDef, XEOAttributeDef attributeDef, MappingKeys type ) {
		 return "model." + modelDef.getModelName() + "." + attributeDef.getModelName() + "." + type.name();
	}

	private final String buildNameMappingKey( String modelName, String attributeName, MappingKeys type ) {
		 return "model." + modelName + "." + attributeName + "." + type.name();
	}
	
	public enum MappingKeys {
		name,
		javaValueType,
		setter
	}
	
	
	private static final String getDescription( Node node ) {
		String description = "";
		NodeList list;
		list = ((XMLElement)node).getElementsByTagName("description");
		if( list.getLength() > 0 ) {
			description = list.item( 0 ).getTextContent();
		}
		if( description == null ) {
			description  = "";
		}
		return description;
	}
	
	private void resolveHierarchyAttributeNameClash( DependencieTree node ) {

		for( XEOAttributeDef attDef : node.model.getAllAttributes() ) {
			if( attDef instanceof XEOAttributeObjectDef ) {
				
				if( !attDef.getFinal() ) {
					
					XEOModelDef model = ((XEOAttributeObjectDef) attDef).getReferencedModel();
					XEOModelDef superModel = node.model.getSuper();
					
					while( superModel != null ) {
						for( XEOAttributeDef superAttDef : superModel.getAllAttributes() ) {
							if( superAttDef.getJavaAttributeGetterName().equals( attDef.getJavaAttributeGetterName() ) ) {
								XEOModelDef superAttModelDef = ((XEOAttributeObjectDef) superAttDef).getReferencedModel();
								if( canModelCastTo(superAttModelDef, model ) ) {
									if( !superAttModelDef.getModelName().equals( model.getModelName() ) ) {
										
//										String actualName = attDef.getJavaAttributeGetterName();
//										String newName = actualName + "_";
										
										attDef.setJavaAttributeGetterName(
											attDef.getJavaAttributeGetterName() + "_"
										);
										attDef.setFinal( true );
										
										// Change the name in all childs...
										//changeInChildsJavaAttributeGetterName( node, actualName, newName );
									}
								}
							}
						}
						superModel = superModel.getSuper();
					}
				}
			}
		}
		
		for( DependencieTree childNode : node.childs ) {
			resolveHierarchyAttributeNameClash(childNode);
		}
		
	}
	
	@SuppressWarnings("unused")
	private boolean changeInChildsJavaAttributeGetterName( DependencieTree node, String attributeGetterName, String newName ) {
		boolean changed = false;
		for(DependencieTree childNode : node.childs ) {
			for( XEOAttributeDef attDef : childNode.model.getAllAttributes() ) {
				if( attDef.getJavaAttributeGetterName().equals( attributeGetterName ) ) {
					attDef.setJavaAttributeGetterName( newName );
					changed = true;
					break;
				}
			}
			if( changeInChildsJavaAttributeGetterName(childNode, attributeGetterName, newName) ) {
				changed = true;
			}
		}
		return changed;
	}
	
	
	private DependencieTree createDependenciesTree(  ) {
		DependencieTree root = new DependencieTree();
		root.model = this.modelsDef.get( "boObject" );
		root.parent = null;
		buildDependenciesTree( root );
		return root;
	}

	private void buildDependenciesTree( DependencieTree root ) {
		boolean isFinal = true;
		XEOModelDef currentDef = root.model;
		for( XEOModelDef modelDef : this.modelsDef.values() ) {
			if( modelDef.getSuper() == currentDef ) {
				DependencieTree node = new DependencieTree();
				node.model = modelDef;
				node.parent = root;
				root.childs.add( node );
				isFinal = false;
				buildDependenciesTree(node);
			}
		}
		currentDef.setFinal( isFinal );
	}

	private void calculateFinalProperties( DependencieTree treeNode ) {
		
		for( XEOAttributeDef attDef : treeNode.model.getAllAttributes() ) {
			isFinalAttribute(treeNode, attDef);
			for( DependencieTree child : treeNode.childs ) {
				calculateFinalProperties(child);
			}
		}
	}
	
	private void isFinalAttribute( DependencieTree treeNode, XEOAttributeDef attDef ) {
		
		for( DependencieTree child : treeNode.childs ) {
			for( XEOAttributeDef childAtt : child.model.getAllAttributes() ) {
				if( childAtt.getJavaAttributeGetterName().equals( attDef.getJavaAttributeGetterName() ) ) {
					attDef.setFinal( false );
					return;
				}
			}
			isFinalAttribute( child, attDef );
		}
	}
	
	private void resolveHierarchyAttributeConflits( DependencieTree treeNode ) {
		for( XEOAttributeDef attDef : treeNode.model.getAllAttributes() ) {
			checkConflits(treeNode, attDef);
			for( DependencieTree child : treeNode.childs ) {
				resolveHierarchyAttributeConflits(child);
			}
		}
	}
	
	private void checkConflits( DependencieTree treeNode, XEOAttributeDef attDef ) {
		XEOModelDef modelDef = treeNode.model.getSuper();
		
		while( modelDef != null ) {
			for( XEOAttributeDef parentAttDef : modelDef.getAllAttributes() ) {
				if( parentAttDef.getJavaFieldName().equals( attDef.getJavaFieldName() ) ) {
					boolean compatible = attDef.getJavaValueClass().equals( parentAttDef.getJavaValueClass() );
					if( parentAttDef instanceof XEOAttributeObjectDef && attDef instanceof XEOAttributeObjectDef ) {
						
						XEOAttributeObjectDef parentAttObjDef = (XEOAttributeObjectDef)parentAttDef;
						XEOAttributeObjectDef attObjDef = (XEOAttributeObjectDef)attDef;
						
						XEOModelDef parentAttModelRef = parentAttObjDef.getReferencedModel();
						XEOModelDef attModelRef = attObjDef.getReferencedModel();
						
						compatible = canModelCastTo(parentAttModelRef, attModelRef );
						
					}
					if( !compatible ) {
						attDef.setJavaNames(
							attDef.getJavaFieldName() + "_", 
							attDef.getJavaStaticFieldName(), 
							attDef.getJavaValueClass(), 
							attDef.getJavaGetterSetterName() + "_",
							attDef.getJavaGetterSetterName() + "_"
						);
						return;
					}
				}
			}
			modelDef = modelDef.getSuper();
		}
	}
	
	private boolean canModelCastTo( XEOModelDef model1, XEOModelDef model2 ) {
		boolean compatible = true;;
		// Verifica se os modelos s��o diferentes
		if( !model1.getModelName().equals( model2.getModelName() ) ) {
			
			compatible = false;
			XEOModelDef analyseModelRef = model2;
			while( analyseModelRef != null ) {
				// Verifica se este objecto extende o object definido no pai
				if( model1.getModelName().equals( analyseModelRef.getModelName() ) ) {
					compatible = true;
					break;
				}
				
				// Verifica se esta implementa o interface do pai
				for( XEOModelDef implInterface : analyseModelRef.getImplementedInterfaces() ) {
					if( model1.getModelName().equals( implInterface.getModelName() ) ) {
						compatible = true;
						break;
					}
				}
				analyseModelRef = analyseModelRef.getSuper();
			}
		}
		return compatible;
		
	}
	
	private static class DependencieTree {
		
		private XEOModelDef model;
		private DependencieTree parent;
		private List<DependencieTree> childs = new ArrayList<XEOModelDefBuilder.DependencieTree>();
		
		@Override
		public String toString() {
			return (parent!=null?parent.model.getModelName()+" -> ":"")
						+ model.getModelName() + (childs.size() > 0?"[" + childs.toString() + "]":"");
		}
		
	}
	
	
	
	private void parseLovFiles(String boPathParent, File folder, Map<String,List<ngtXMLHandler>> filesMap) {
		File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String lowername = pathname.getName().toLowerCase();
				if (pathname.isDirectory())
					return true;

				if (lowername.endsWith(".xeolov")) {
					return true;
				}
				return false;
			}
		});

		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		int pos;
		pos = folder.getName().toLowerCase().indexOf('$');

		String boFolderName = folder.getName().toLowerCase();

		if (pos != -1)
			boFolderName = boFolderName.substring(0, pos - 1);


		for (File boDefFile : files) {

			if (boDefFile.isDirectory()) {
				boFolderName = boPathParent;
				if( boPathParent.length() > 0 ) {
					boFolderName += ".";
				}
				boFolderName += boDefFile.getName();
				parseLovFiles(boFolderName, boDefFile, filesMap);
				continue;
			}

			String boName = boDefFile.getName();
			
			List<ngtXMLHandler> lovs = new ArrayList<ngtXMLHandler>();
			ngtXMLHandler handler = new ngtXMLHandler( ngtXMLUtils.loadXMLFile( boDefFile.getAbsolutePath() ).getDocumentElement() );
			for( ngtXMLHandler lovXml : handler.getChildNodes() ) {
				if( "Lov".equalsIgnoreCase(lovXml.getNodeName() ) ) {
					lovs.add( lovXml );
				}
			}
			filesMap.put( boFolderName + "." + boName , lovs );
			boLovPackage.put( boFolderName + "." + boName, boPathParent );
			
		}
	}
	
	
	
	private void loadBoDefHandlers(String boPathParent, File folder, Map<String, boDefHandler> filesMap) {
		File[] files = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String lowername = pathname.getName().toLowerCase();
				if (pathname.isDirectory())
					return true;

				if (lowername.endsWith(".xeomodel")
						|| lowername.endsWith(".xeoimodel")) {
					return true;
				}
				return false;
			}
		});

		Arrays.sort(files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		int pos;
		pos = folder.getName().toLowerCase().indexOf('$');

		String boFolderName = folder.getName().toLowerCase();

		if (pos != -1)
			boFolderName = boFolderName.substring(0, pos - 1);


		for (File boDefFile : files) {

			if (boDefFile.isDirectory()) {
				
				boFolderName = boPathParent;
				if( boPathParent.length() > 0 ) {
					boFolderName += ".";
				}
				boFolderName += boDefFile.getName();
				loadBoDefHandlers(boFolderName, boDefFile, filesMap);
				
				continue;
			}

			String boName = boDefFile.getName();

			boolean isInterface;

			isInterface = boName.endsWith(".xeoimodel");

			pos = boName.indexOf('.');
			if (pos != -1)
				boName = boName.substring( 0, pos );

//			String fullName = folder + File.separator + boName;

			boDefHandler bodef;

			XMLDocument xmldoc = ngtXMLUtils.loadXMLFile(boDefFile.getAbsolutePath());

			// If is the boObject, get the object with all default attributes;
			if( boName.equals("boObject")) {
				
				// Generate the base interface
				XMLDocument iboObjectXML = new XMLDocument();
				iboObjectXML = (XMLDocument) xmldoc.cloneNode(true);
				//iboObjectXML.importNode( xmldoc.getDocumentElement(), true );
				try {
					XMLElement generalElement = (XMLElement)iboObjectXML.selectSingleNode("/xeoModel/general");
					generalElement.setAttribute("type", "interface");
					generalElement.setAttribute("name", "IboObject");
					boDefHandler iboObject = boDefHandler.loadInterfaceFromXml( "IboObject", iboObjectXML );
					fillSystemAttributes( iboObject );
					filesMap.put(iboObject.getName(), iboObject);
					bodefPackage.put( iboObject.getBoName() , boPathParent );
					
				} catch (XSLException e) {
				}
					
				bodef = boDefHandler.loadFromXml(xmldoc);
				fillSystemAttributes( bodef);
			}
			else {
				// Create only the default attributes in the bridges
				if (isInterface) {
					bodef = boDefHandler.loadInterfaceFromXml(boName, xmldoc);
				} else {
					bodef = boDefHandler.loadFromXml(xmldoc);
				}
				fillDefaultAttributes(bodef);
			}
			filesMap.put(bodef.getName(), bodef);
			bodefPackage.put( bodef.getBoName() , boPathParent );
		}

	}
	

}
