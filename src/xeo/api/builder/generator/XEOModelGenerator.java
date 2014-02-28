package xeo.api.builder.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import xeo.api.builder.def.XEODefBuilderProperties;
import xeo.api.builder.def.XEOLovDef;
import xeo.api.builder.def.XEOModelDef;
import xeo.api.builder.def.XEOModelDef.ModelType;
import xeo.api.builder.def.XEOModelDefBuilder;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class XEOModelGenerator {
	
	Template templateModelBase;
	Template templateModelFactoryBase;
	Template templateModel;
	Template templateModelFactory;
	Template templateModelInterface;
	Template templateModelRegistry;
	Template templateModelLov;
	
	
	// Output Folder user model generated java source files
	private String publicOutputDir;
	
	// Output folder for internal generated source files 
	private String internalOutputDir;

	private XEODefBuilderProperties buildProperties = new XEODefBuilderProperties( );
	
	public static void main(String[] args) throws Exception {
		
		// Example of use
		XEOModelGenerator modelGenerator = new XEOModelGenerator();
		
		// Packages of classes generated
		// Internal java classes (not visible to the user)
		modelGenerator.setInternalJavaPackage( "xeo.models.impl" );
		// Public java classes (visible and modifiable by the user)
		modelGenerator.setPublicJavaPackage( "xeo.models" );
		
		// Path of the output of the generated files 
		modelGenerator.setSourceInternalOutputDir( "/Users/jcarreira/Work/xeo/workspace/xwc_portal/xwc_portal/src-internal" );
		modelGenerator.setSourcePublicOutputDir( "/Users/jcarreira/Work/xeo/workspace/xwc_portal/xwc_portal/src-xeogen" );
		
		// Path to xeoHome
		modelGenerator.setXEOHome( "/Users/jcarreira/Work/xeo/workspace/xwc_portal/xwc_portal/" );
		
		// Generate the source files
		modelGenerator.generateFiles();
		
	}
	
	private void setUp( ) throws Exception {
		Configuration freemarkerConf = new Configuration();
		freemarkerConf.setObjectWrapper(new DefaultObjectWrapper());
		freemarkerConf.setClassForTemplateLoading( XEOModelGenerator.class, "" );
		
		templateModelInterface = freemarkerConf.getTemplate( "ModelInterface.jftl" );
		templateModelBase = freemarkerConf.getTemplate( "ModelBase.jftl" );
		templateModelFactoryBase = freemarkerConf.getTemplate( "ModelFactoryBase.jftl" );
		templateModel = freemarkerConf.getTemplate( "Model.jftl" );
		templateModelFactory = freemarkerConf.getTemplate( "ModelFactory.jftl" );
		templateModelRegistry = freemarkerConf.getTemplate( "ModelRegistry.jftl" );
		
		templateModelLov = freemarkerConf.getTemplate( "ModelLov.jftl" );
	}
	
	public void generateFiles() throws Exception {
		
		
		System.setProperty("xeo.home", this.getXEOHome() );
		System.setProperty("xeo.threads.enable", "false" );
		
		int modelsGenerated = 0;
		
		// Setup freemarker templates
		setUp();
		
		long init = System.currentTimeMillis();
		
		// Read bodef and create the builder auxiliar objects
		XEOModelDefBuilder defBuilder = new XEOModelDefBuilder( this.buildProperties );
		defBuilder.buildXEOModelDef();
		
		// Generate base source files
		for( String modelName : defBuilder.getModelsNames() ) {
			XEOModelDef modelDef = defBuilder.getModelDefinition( modelName );
			if( modelDef.getModelType() == ModelType.ABSTRACT_MODEL || modelDef.getModelType() == ModelType.MODEL ) {
				generateJavaForModelFiles( modelDef );
				modelsGenerated++;
			}
		}
		
		// Generate source for interfaces
		for( String modelName : defBuilder.getModelsNames() ) {
			XEOModelDef modelDef = defBuilder.getModelDefinition( modelName );
			if( modelDef.getModelType() == ModelType.INTERFACE ) {
				generateJavaForInterfaceModelFiles( modelDef );
				modelsGenerated++;
			}
		}
		
		// Generate LOV source files
		for( String lovDefName : defBuilder.getLovNames() ) {
			generateLovFile( defBuilder.getLovDefinition( lovDefName ) );
		}
		
		// Generate java class with XEOModelFactories dictionary
		generateXEOModelFactories(defBuilder);
		
		System.out.println( 
				String.format( 
					"[%d] Models generated in  [%d ms]", modelsGenerated, System.currentTimeMillis() - init
				)
		);
		
	}
	
	private void generateLovFile( XEOLovDef lovDef ) throws Exception {
		
		Map<String,XEOLovDef> map = new HashMap<String, XEOLovDef>();
		map.put("root", lovDef );
		

		StringWriter out = new StringWriter();
		try {
			FileWriter fw;
			
			System.out.println( lovDef.getName() );
			
			templateModelLov.process( map, out);

			File dir = (new File(internalOutputDir + convertPackageToPath( lovDef.getJavaPackage() )));
			dir.mkdirs();
			
			fw = new FileWriter( 
					dir.getAbsolutePath()  + File.separator + lovDef.getJavaClassName() + ".java" 
			);
			
			fw.write( out.getBuffer().toString() );
			fw.close();
		}
		catch( IOException e ) {
			throw new RuntimeException( e );
		}
		
	}
	
	
	private void generateXEOModelFactories( XEOModelDefBuilder modelDefBuilder ) throws Exception {
		Map<String,Collection<XEOModelDef>> map = new HashMap<String, Collection<XEOModelDef>>();
		map.put("root", modelDefBuilder.getModels() );

		StringWriter out = new StringWriter();
		try {
			FileWriter fw;
			
			templateModelRegistry.process(  map, out);

			File dir = (new File(internalOutputDir + convertPackageToPath( this.buildProperties.getJavaDictionaryPackage() )));
			dir.mkdirs();
			fw = new FileWriter( 
				dir.getAbsolutePath()  + File.separator +  "XEOModelFactories.java" 
			);
			fw.write( out.getBuffer().toString() );
			fw.close();
		}
		catch( IOException e ) {
			throw new RuntimeException( e );
		}
	}
	
	
	private void generateJavaForInterfaceModelFiles( XEOModelDef modelDef ) throws Exception {
		Map<String,XEOModelDef> map = new HashMap<String, XEOModelDef>();
		map.put("root", modelDef );
		
		StringWriter out = new StringWriter();
		try {
			FileWriter fw;
			
			System.out.println( modelDef.getModelName() );
			
			templateModelInterface.process(  map, out);

			File dir = (new File(publicOutputDir + convertPackageToPath( modelDef.getJavaPackage() )));
			dir.mkdirs();
			
			fw = new FileWriter( 
					dir.getAbsolutePath()  + File.separator + modelDef.getJavaClassName() + ".java" 
			);
			
			fw.write( out.getBuffer().toString() );
			fw.close();
		}
		catch( IOException e ) {
			throw new RuntimeException( e );
		}
	}
	
	private void generateJavaForModelFiles( XEOModelDef modelDef ) throws Exception {
		Map<String,XEOModelDef> map = new HashMap<String, XEOModelDef>();
		map.put("root", modelDef );
		
		Configuration freemarkerConf = new Configuration();
		freemarkerConf.setObjectWrapper(new DefaultObjectWrapper());
		freemarkerConf.setClassForTemplateLoading( XEOModelGenerator.class, "" );

		StringWriter out = new StringWriter();
		try {
			FileWriter fw;
			System.out.println( modelDef.getModelName() );
			
			templateModelBase.process(  map, out);

			File dirBase = (new File(internalOutputDir + convertPackageToPath( modelDef.getJavaBasePackage() )));
			dirBase.mkdirs();
			
			
			fw = new FileWriter( 
				dirBase.getAbsolutePath() + File.separator + modelDef.getJavaBaseClassName() + ".java" 
			);
			fw.write( out.getBuffer().toString() );
			fw.close();
			
			out = new StringWriter();
			templateModelFactoryBase.process(  map, out);
			fw = new FileWriter( 
				dirBase.getAbsolutePath()  + File.separator + modelDef.getJavaClassName() + "FactoryBase.java" 
			);
			fw.write( out.getBuffer().toString() );
			fw.close();
			
			
			File dir = (new File(publicOutputDir + convertPackageToPath( modelDef.getJavaPackage() )));
			dir.mkdirs();
			
			
			File modelClass = new File( dir.getAbsolutePath() + File.separator + modelDef.getJavaClassName() + ".java"  );
			if ( !modelClass.exists() ) {
				out = new StringWriter();
				templateModel.process(  map, out);
				fw = new FileWriter( 
						modelClass
				);
				fw.write( out.getBuffer().toString() );
				fw.close();
			}
			
			File modelFactory = new File( dir.getAbsolutePath() + File.separator + modelDef.getJavaClassName() + "Factory.java" );
			if( !modelFactory.exists() ) {
				out = new StringWriter();
				templateModelFactory.process(  map, out);
				fw = new FileWriter( modelFactory );
				fw.write( out.getBuffer().toString() );
				fw.close();
			}
			
		}
		catch( Exception e ) {
			System.out.println( out );
			throw e;
		}
	}
	
	private String convertPackageToPath( String name ) {
		name = name.replace('.',  File.separatorChar );
		if( !name.endsWith( File.separator ) ) {
			name += File.separatorChar;
		}
		return name;
	}
	
	public void setPublicJavaPackage( String javaPackage ) {
		buildProperties.setJavaPackage( javaPackage );
	}
	
	public void setInternalJavaPackage( String internalJavaPackage ) {
		buildProperties.setJavaPackageBase( internalJavaPackage );
	}
	
	public String getPublicJavaPackage() {
		return buildProperties.getJavaPackage();
	}
	
	public String getInternalJavaPackage() {
		return buildProperties.getJavaPackageBase();
	}
	
	public void setSourcePublicOutputDir( String publicOutputDir ) {
		if( !publicOutputDir.endsWith( "/" ) && !publicOutputDir.endsWith( "\\" ) ) {
			publicOutputDir += File.separator;
		}
		this.publicOutputDir = publicOutputDir;
	}
	
	public void setSourceInternalOutputDir( String internalOutputDir ) {
		if( !internalOutputDir.endsWith( "/" ) && !internalOutputDir.endsWith( "\\" ) ) {
			internalOutputDir += File.separator;
		}
		this.internalOutputDir = internalOutputDir;
	}
	
	public void setXEOHome( String xeoHome ) {
		this.buildProperties.setXEOHome( xeoHome );
	}
	
	public String getSourcePublicOutputDir() {
		return this.publicOutputDir;
	}
	
	public String getSourceInternalOutputDir() {
		return this.internalOutputDir;
	}
	
	public String getXEOHome() {
		return this.buildProperties.getXEOHome();
	}
	
	
}
