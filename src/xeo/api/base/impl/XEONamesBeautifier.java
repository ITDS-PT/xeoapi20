package xeo.api.base.impl;

public class XEONamesBeautifier {
	
	
	public static String convertToFieldName( String name ) {
		if( name.toUpperCase().equals( name ) ) {
			name = name.toLowerCase();
		}
		return convertToFieldName( name, false );
	}
	
	public static String convertToClassName( String name ) {
		return convertToFieldName( name, true );
	}

	public static String convertToPackageName(String name) {
		
		int idx = name.lastIndexOf( "$" );
		if( idx !=  -1 ) {
			name = name.substring( 0, idx );
		}
		name = name.replace('$', '.');
		
		return convertToFieldName( name );
		
	}
	
	public static String convertToJavaGetterSetter( String name ) {
		if( name.toUpperCase().equals( name ) ) {
			name = name.toLowerCase();
		}
		return convertToFieldName( name, true );
	}
	
	private static String convertToFieldName( String modelName, boolean uppercaseFirstLetter ) {
		int pos;
		char c;
		
		boolean wasUnderscore;
		
		wasUnderscore = false;
		
		StringBuilder newName = new StringBuilder();
		for( pos = 0; pos < modelName.length(); pos++  ) {
			c = modelName.charAt( pos );
			if( pos == 0 ) {
				if( !Character.isJavaIdentifierStart( c ) ) {
					newName.append( '_' );
				}
				else {
					if( uppercaseFirstLetter ) {
						newName.append(
							Character.toUpperCase( c )
						);
					}
					else {
						newName.append(
								Character.toLowerCase( c )
							);
					}
				}
			}
			else {
				if( !Character.isJavaIdentifierPart( c ) ) {
					newName.append( Character.toUpperCase( '_' ) );
				}
				else {
					if( c != '_' ) {
						if( wasUnderscore ) {
							wasUnderscore = false;
							newName.append( Character.toUpperCase( c ) );
						}
						else {
							newName.append( c );
						}
					}
					else {
						wasUnderscore = true;
					}
				}
			}
		}
		return newName.toString();
	}
	
	public static String convertStaticFieldName( String modelName ) {
		int pos;
		char c;
		
		boolean wasLower;
		boolean wasUnderscore;
		
		wasLower = false;
		wasUnderscore = false;
		
		StringBuilder newName = new StringBuilder();
		for( pos = 0; pos < modelName.length(); pos++  ) {
			c = modelName.charAt( pos );
			if(  pos == 0 ) {
				if( !Character.isJavaIdentifierStart( c ) ) {
					newName.append( '_' );
					if( Character.isJavaIdentifierPart( c ) ) {
						newName.append( c );
					}
				}
				else {
					newName.append(
						Character.toUpperCase( c )
					);
				}
			}
			else {
				if( !Character.isJavaIdentifierPart( c ) ) {
					if( !wasUnderscore ) {
						newName.append(
								Character.toUpperCase( '_' )
							);
					}
				}
				else {
					
					if( Character.isUpperCase( c ) && wasLower && !wasUnderscore ) {
						newName.append( '_' );
					}
					
					newName.append(
						Character.toUpperCase( c )
					);
				}
			}
			wasLower = Character.isLowerCase( c );
			wasUnderscore = newName.charAt( newName.length() - 1 ) == '_';
		}
		return newName.toString();
	}
	
}
