package xeo.api.builder.generator;

public class XEOModelGeneratorForLocal {
	
	public static void main(String[] args) throws Exception {
		// Example of use
		XEOModelGenerator modelGenerator = new XEOModelGenerator();
		
		// Packages of classes generated
		// Internal java classes (not visible to the user)
		modelGenerator.setInternalJavaPackage( "xeo.models.impl" );
		
		// Public java classes (visible and modifiable by the user)
		modelGenerator.setPublicJavaPackage( "xeo.models" );
		
		modelGenerator.setSourceEncoding("UTF-8");
		
		// Path of the output of the generated files
		
		modelGenerator.setSourceInternalOutputDir( "/Users/jcarreira/Work/workspace-luna/xeoapi20_tests_app/src-internal" );
		modelGenerator.setSourcePublicOutputDir( "/Users/jcarreira/Work/workspace-luna/xeoapi20_tests_app/src-xeogen" );
//
		// Path to xeoHome
		modelGenerator.setXEOHome( "/Users/jcarreira/Work/workspace-luna/xeoapi20_tests_app/" );
		
		
//		modelGenerator.setSourceInternalOutputDir( "/Users/jcarreira/Work/workspace-luna/SGTECV/.src-internal" );
//		modelGenerator.setSourcePublicOutputDir( "/Users/jcarreira/Work/workspace-luna/SGTECV/src-xeogen" );

		// Path to xeoHome
//		modelGenerator.setXEOHome( "/Users/jcarreira/Work/xeo/workspace/SGTECV/" );
		
		
		// Generate the source files
		modelGenerator.generateFiles();
		
	}

}
