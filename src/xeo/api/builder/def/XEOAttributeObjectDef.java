package xeo.api.builder.def;

import java.util.ArrayList;
import java.util.List;

import xeo.api.builder.def.XEOTypes.SimpleDataType;

public class XEOAttributeObjectDef extends XEOAttributeDef {
	
	private List<XEOModelDef> typedReferences = new ArrayList<XEOModelDef>();
	
	private XEOModelDef referencedModel;

	public XEOAttributeObjectDef(String modelName, String description, XEOModelDef referencedModel ) {
		super(modelName, description, SimpleDataType.Object );
		this.referencedModel = referencedModel;
	}

	protected XEOAttributeObjectDef(String modelName, String description, SimpleDataType type, XEOModelDef referencedModel ) {
		super(modelName, description, type );
		this.referencedModel = referencedModel;
	}
	
	public XEOModelDef getReferencedModel() {
		return referencedModel;
	}
	
	void addTypedReference( XEOModelDef modelDef ) {
		
		this.typedReferences.add( modelDef );
		
	}
	
	public List<XEOModelDef> getTypedReferences() {
		return this.typedReferences;
	}
	
	
	
}
