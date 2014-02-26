package xeo.api.base.impl;

import java.io.File;
import java.io.InputStream;

import netgest.bo.data.DataRow;
import netgest.bo.runtime.AttributeHandler;
import netgest.bo.runtime.boRuntimeException;
import netgest.io.FSiFile;
import netgest.io.iFile;
import netgest.io.iFilePermissionDenied;
import xeo.api.base.XEOAttributeFile;

public class XEOAttributeFileImpl extends XEOAttributeImpl<iFile> implements XEOAttributeFile {

	public XEOAttributeFileImpl( XEOModelImpl model, AttributeHandler attributeHandler ) {
		
		super( iFile.class, String.class, model, attributeHandler);
		
	}
	
	@Override
	public iFile getValue() {
		try {
			return attributeHandler.getValueiFile();
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public iFile getPersistedValue() {
		try {
			DataRow currentRow = attributeHandler.getParent().getDataRow();
			DataRow flashbackRow = currentRow.getFlashBackRow();
			if( flashbackRow == null && !attributeHandler.getParent().exists() ) {
				return null ;
			}
			else if ( flashbackRow == null ) {
				return getValue();
			}
			String attributeName = attributeHandler.getDefAttribute().getDbName();
			String currentValue  = currentRow.getString( attributeName );
			String previousValue = flashbackRow.getString( attributeName );
			if( previousValue != null ) {
				try {
					currentRow.updateString( attributeName, previousValue );
					return getValue();
				}
				finally {
					currentRow.updateString( attributeName, currentValue );
				}
			}
			else {
				return null;
			}
			
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	};

	@Override
	public void setValue( File file ) {
		try {
			if( file == null ) {
				attributeHandler.setValueiFile( null );
			}
			else {
				FSiFile fsiFile = new FSiFile( null, file, null );
				attributeHandler.setValueiFile( fsiFile );
			}
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void setValue(iFile value) {
		try {
			attributeHandler.setValueiFile( value );
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		}
	}
	
	public InputStream getInputStream() {
		iFile file;
		try {
			file = attributeHandler.getValueiFile();
			if( file != null ) {
				return file.getInputStream();
			}
		} catch (boRuntimeException e) {
			throw new RuntimeException(e);
		} catch (iFilePermissionDenied e) {
			throw new RuntimeException(e);
		}
		return null;
	}
}
