package xeo.api.builder.def;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XEOLovDef {
	
	List<XEOLovItem> items = new ArrayList<XEOLovItem>();
	
	private String container;
	private String packageName;
	private String description;
	
	private String name;
	
	private String javaPackage;
	private String javaClassName;
	
	public XEOLovDef( String container, String name, String packageName, String description ) {
		this.container = container;
		this.name      = name;
		this.packageName = packageName;
		this.description = description;
		if( packageName == null ) {
			System.out.println( "ToDebug:" + name );
		}
		
	}
	
	void setJavaNames( String javaClassName, String javaPackage ) {
		this.javaPackage = javaPackage;
		this.javaClassName = javaClassName;
	}
	
	public String getContainer() {
		return container;
	}
	
	public String getPackageName() {
		return packageName;
	}
	
	public String getName() {
		return name;
	}
	
	public List<XEOLovItem> getItems() {
		return Collections.unmodifiableList( items );
	}
	
	void addItem( String value, String label ) {
		items.add( new XEOLovItem( value, label) );
	}
	
	public String getJavaClassName() {
		return javaClassName;
	}
	
	public String getJavaPackage() {
		return javaPackage;
	}
	
	public String getDescription() {
		return description;
	}
	
	public class XEOLovItem {
		
		private String value;
		private String label;
		
		private String javaStaticName;
		
		public XEOLovItem(String value, String label) {
			super();
			this.value = value;
			this.label = label;
		}
		
		public String getValue() {
			return value;
		}

		public String getLabel() {
			return label;
		}
		
		public String getJavaStaticName() {
			return javaStaticName;
		}
		
		void setJavaNames( String javaStaticName ) {
			this.javaStaticName = javaStaticName;
		}
		
		@Override
		public String toString() {
			return this.value + ":" + this.label;
		}
		
	}
	
	
}
