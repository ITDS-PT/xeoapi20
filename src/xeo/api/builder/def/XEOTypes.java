package xeo.api.builder.def;

public class XEOTypes {
	
	public enum SimpleDataType {
					// New API Java Type     boObject Java Type      Attribute Class   / Interface       Class is Generic / Interface is Generic
		Text(		"String"				,"String"				,"XEOAttributeImpl", "XEOAttribute", true, true),
		Number(		"Long"					,"java.math.BigDecimal"	,"XEOAttributeImpl", "XEOAttribute", true, true),
		Decimal(	"java.math.BigDecimal"	,"java.math.BigDecimal"	,"XEOAttributeDecimalImpl","XEOAttributeDecimal", false, false),
		Date(		"java.util.Date"		,"java.sql.Timestamp"	,"XEOAttributeImpl", "XEOAttribute",true, true),
		DateTime(	"java.util.Date"		,"java.sql.Timestamp"	,"XEOAttributeImpl", "XEOAttribute",true, true),
		Duration(	"Long"					,"java.math.BigDecimal"	,"XEOAttributeImpl", "XEOAttribute",true, true),
		Boolean(	"Boolean"				,"String"				,"XEOAttributeImpl", "XEOAttribute",true, true),
		Sequence(	"Long"					,"java.math.BigDecimal"	,"XEOAttributeImpl", "XEOAttribute",true, true),
		Currency(	"java.math.BigDecimal"	,"java.math.BigDecimal"	,"XEOAttributeDecimalImpl", "XEOAttributeDecimal", false, false),
		BinaryData(	"netgest.io.iFile"		,"netgest.io.iFile"		,"XEOAttributeFileImpl", "XEOAttributeFile",false, false),
		LongText(	"String"				,"String"				,"XEOAttributeImpl", "XEOAttribute",true, true),
		Object(		"XEOAttributeObject"	,"XEOAttributeObject"	,"XEOAttributeObjectImpl", "XEOAttributeObject",true, true),
		Collection(	"XEOAttributeCollection","XEOAttributeCollection","XEOAttributeCollectionImpl", "XEOAttributeCollection",true, true);

		String valueClassName;
		String modelValueClassName;
		String fieldClassName;
		String fieldInterfaceName;
		boolean isGeneric;
		boolean interfaceIsGeneric;
	
		SimpleDataType( String dataType, String modelDataType, String fieldClassName, String fieldInterfaceName, boolean isGeneric, boolean interfaceIsGeneric ) {
			this.valueClassName = dataType;
			this.fieldClassName = fieldClassName;
			this.modelValueClassName = modelDataType;
			this.fieldInterfaceName = fieldInterfaceName;
			this.isGeneric = isGeneric;
			this.interfaceIsGeneric = interfaceIsGeneric;
		}
		
		public String getJavaDataType() {
			return this.valueClassName;
		}

		public String getModelJavaDataType() {
			return this.modelValueClassName;
		}
		
		String getFieldClassName() {
			return fieldClassName;
		}
		
		public boolean getGeneric() {
			return isGeneric;
		}

		public String getFieldInterfaceName() {
			return fieldInterfaceName;
		}
		
		public boolean getInterfaceIsGeneric() {
			return interfaceIsGeneric;
		}
	}
	
}
