package xeo.api.builder.def;

import java.util.ArrayList;
import java.util.List;

public class XEOMethodDef {
	
	private List<XEOMethodArgument> arguments = new ArrayList<XEOMethodDef.XEOMethodArgument>();
	
	private String name;
	private String returnType;
	private String javaObjectReturnType;
	
	public XEOMethodDef( String name, String returnType, String javaObjectReturnType ) {
		this.name = name;
		this.returnType = returnType;
		this.javaObjectReturnType = javaObjectReturnType;
	}
	
	public void addArgument( XEOMethodArgument argument  ) {
		this.arguments.add( argument );
	}
	
	public String getName() {
		return name;
	}
	
	public String getReturnType() {
		return returnType;
	}
	
	public String getJavaObjectReturnType() {
		return javaObjectReturnType;
	}
	
	public List<XEOMethodArgument> getArguments() {
		return arguments;
	}
	
	public static class XEOMethodArgument {
		String argName;
		String className;
		
		public XEOMethodArgument( String argName, String className ) {
			this.argName = argName;
			this.className = className;
		}

		public String getArgName() {
			return argName;
		}

		public String getClassName() {
			return className;
		}
		
	}
}
