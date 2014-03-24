package xeo.api.builder.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
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
	
	Template templateModel;
	Template templateModelBase;
	Template templateModelFactoryBase;
	Template templateModelFactory;
	
	Template templateModelInterface;
//	Template templateModelRegistry;
	Template templateModelFactoryRegistry;
	Template templateModelLov;
	
	Template templateModelAbstractFactoryBase;
	Template templateModelAbstractFactory;
	
	// Output Folder user model generated java source files
	private String publicOutputDir;
	
	// Output folder for internal generated source files 
	private String internalOutputDir;

	private XEODefBuilderProperties buildProperties = new XEODefBuilderProperties( );
	private String encoding;
	
	public static void main(String[] args) throws Exception {
		if (args==null || args.length==0 || args.length<6) {
    		System.out.print("Usage: XEOModelGenerator [INTERNALJAVAPACKAGE] [PUBLICJAVAPACKACE] [INTERNALOUTDIR] [PUBLICOUTDIR] [XEOHOME] [SRCENCODING]");
    		System.exit(0);
    	}
		
		// Example of use
		XEOModelGenerator modelGenerator = new XEOModelGenerator();
		
		// Packages of classes generated
		// Internal java classes (not visible to the user)
		//modelGenerator.setInternalJavaPackage( "xeo.models.impl" );
		
		modelGenerator.setInternalJavaPackage( args[0] );
		// Public java classes (visible and modifiable by the user)
		//modelGenerator.setPublicJavaPackage( "xeo.models" );
		
		modelGenerator.setPublicJavaPackage( args[1] );
		
		// Path of the output of the generated files 
		//modelGenerator.setSourceInternalOutputDir( "/Users/jcarreira/Work/xeo/workspace/xeoapi20_tests_app/src-internal" );
		//modelGenerator.setSourcePublicOutputDir( "/Users/jcarreira/Work/xeo/workspace/xeoapi20_tests_app/src-xeogen" );
		modelGenerator.setSourceInternalOutputDir(  args[2] );
		modelGenerator.setSourcePublicOutputDir(  args[3]  );
		// Path to xeoHome
		//modelGenerator.setXEOHome( "/Users/jcarreira/Work/xeo/workspace/xeoapi20_tests_app/" );
		modelGenerator.setXEOHome( args[4] );
		
		modelGenerator.setSourceEncoding( args[5] );
		
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
		
//		templateModelRegistry = freemarkerConf.getTemplate( "ModelRegistry.jftl" );
		
		templateModelFactoryRegistry = freemarkerConf.getTemplate( "ModelFactoryRegistry.jftl" );

		templateModelAbstractFactory = freemarkerConf.getTemplate( "ModelAbstractFactory.jftl" );
		templateModelAbstractFactoryBase = freemarkerConf.getTemplate( "ModelAbstractFactoryBase.jftl" );
		
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
			System.out.println( lovDef.getName() );
			
			templateModelLov.process( map, out);

			File dir = (new File(internalOutputDir + convertPackageToPath( lovDef.getJavaPackage() )));
			dir.mkdirs();
			
			writeFile(dir.getAbsolutePath()  + File.separator + lovDef.getJavaClassName() + ".java", out.getBuffer().toString() );
		}
		catch( IOException e ) {
			throw new RuntimeException( e );
		}
		
	}
	
	
	private void generateXEOModelFactories( XEOModelDefBuilder modelDefBuilder ) throws Exception {
		
		Map<String,XEOModelDef> map = new HashMap<String, XEOModelDef>();
		
		for( XEOModelDef modelDef : modelDefBuilder.getModels() ) {
			map.put("root", modelDef );
			StringWriter out = new StringWriter();
			try {
				templateModelFactoryRegistry.process(  map, out);
	
				File dir = (new File(internalOutputDir + convertPackageToPath( "xeo.models.impl._factories" )));
				dir.mkdirs();
				writeFile(dir.getAbsolutePath()  + File.separator + modelDef.getJavaClassName() + "FactoryLocation.java", out.getBuffer().toString() );
			}
			catch( IOException e ) {
				throw new RuntimeException( e );
			}
		}
	}
	
	
	private void generateJavaForInterfaceModelFiles( XEOModelDef modelDef ) throws Exception {
		Map<String,XEOModelDef> map = new HashMap<String, XEOModelDef>();
		map.put("root", modelDef );
		
		StringWriter out = new StringWriter();
		try {
			System.out.println( modelDef.getModelName() );
			
			templateModelInterface.process(  map, out);
			File dir = (new File(publicOutputDir + convertPackageToPath( modelDef.getJavaPackage() )));
			dir.mkdirs();
			writeFile(dir.getAbsolutePath()  + File.separator + modelDef.getJavaClassName() + ".java", out.getBuffer().toString() );
			
			out = new StringWriter();
			templateModelAbstractFactoryBase.process(  map, out);
			File dirFactoryBase = (new File(internalOutputDir + convertPackageToPath( modelDef.getJavaBasePackage() )));
			dirFactoryBase.mkdirs();
			writeFile(dirFactoryBase.getAbsolutePath()  + File.separator + modelDef.getJavaClassName() + "FactoryBase.java", out.getBuffer().toString() );
			
			out = new StringWriter();
			templateModelAbstractFactory.process(  map, out);
			File dirFactory = (new File(publicOutputDir + convertPackageToPath( modelDef.getJavaPackage() )));
			dirFactoryBase.mkdirs();
			
			File outFile = new File( dirFactory.getAbsolutePath()  + File.separator + modelDef.getJavaClassName() + "Factory.java" );
			if( !outFile.exists() ) {
				writeFile( outFile, out.getBuffer().toString() );
			}
			
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
			System.out.println( modelDef.getModelName() );
			
			templateModelBase.process(  map, out);

			File dirBase = (new File(internalOutputDir + convertPackageToPath( modelDef.getJavaBasePackage() )));
			dirBase.mkdirs();
			
			writeFile(dirBase.getAbsolutePath() + File.separator + modelDef.getJavaBaseClassName() + ".java", out.getBuffer().toString() );
			
			out = new StringWriter();
			templateModelFactoryBase.process(  map, out);
			
			writeFile(dirBase.getAbsolutePath()  + File.separator + modelDef.getJavaClassName() + "FactoryBase.java", out.getBuffer().toString() );
			
			File dir = (new File(publicOutputDir + convertPackageToPath( modelDef.getJavaPackage() )));
			dir.mkdirs();
			
			
			File modelClass = new File( dir.getAbsolutePath() + File.separator + modelDef.getJavaClassName() + ".java"  );
			if ( !modelClass.exists() ) {
				out = new StringWriter();
				templateModel.process(  map, out);
				writeFile(modelClass, out.getBuffer().toString() );
			}
			
			File modelFactory = new File( dir.getAbsolutePath() + File.separator + modelDef.getJavaClassName() + "Factory.java" );
			if( !modelFactory.exists() ) {
				out = new StringWriter();
				templateModelFactory.process(  map, out);
				writeFile(modelFactory, out.getBuffer().toString() );
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
	
	private void writeFile( String fileName, String content ) throws IOException {
		writeFile( new File( fileName ), content );
	}
	
	private void writeFile( File file, String content ) throws IOException {
		OutputStreamWriter fw;
		if( this.encoding == null ) {
			fw = new OutputStreamWriter( new FileOutputStream( file ) );
		}
		else {
			fw = new OutputStreamWriter( new FileOutputStream( file ), this.encoding );
		}
		fw.write( content );
		fw.close();
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
	
	public void setSourceEncoding( String encoding ) {
		this.encoding = encoding;
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
