package xeo.api.builder.def;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import xeo.api.builder.def.XEOTypes.SimpleDataType;

public class XEOBridgeDef extends XEOAttributeObjectDef {
	
	
	private Map<String, XEOAttributeDef> attributes = new LinkedHashMap<String, XEOAttributeDef>();
	
	public XEOBridgeDef(String modelName, String description, XEOModelDef referencedModel ) {
		super(modelName, description, SimpleDataType.Collection, referencedModel );
	}

	void putAttribute( XEOAttributeDef attribute ) {
		this.attributes.put( attribute.getModelName(), attribute );
	}

	public Collection<XEOAttributeDef> getAttributes() {
		return this.attributes.values();
	}
	
}
