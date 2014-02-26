package xeo.api.base;

import java.io.File;
import java.io.InputStream;

import netgest.io.iFile;

public interface XEOAttributeFile extends XEOAttribute<iFile>  {
	
	/**
	 * 
	 * Associate a file to the Attribute
	 * 
	 * @param file the file to be associated
	 */
	public void setValue( File file );
	
	/**
	 * Rtreives a inputstream to the file. If the attributw has a null value, this methos returns null.
	 * 
	 * <strong>This inputstream must be closed. It holds a separated connection to the database, to stream data</strong>
	 * 
	 * @return the InputStream to the file
	 */
	public InputStream getInputStream();
	
	
}
