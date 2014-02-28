package xeo.api.base.impl.ql;

import java.util.ArrayList;
import java.util.List;

import xeo.api.base.exceptions.XEOQLParserException;

public class XEOQLPreProcessor {
	
	private Object[] inParametersArray;
	private List<Object> outParametersArray;
	private String   inBoql;
	
	public static void main(String[] args) {
		XEOQLPreProcessor qlPreProc = new XEOQLPreProcessor("select * from Ebo_Perf where username={5} and {1} and boui={1} and xx=? and x={0}", "joao", 32312,"s" );
		String result = qlPreProc.processQl();
		System.out.println( result );
		
		for( Object x: qlPreProc.getProcessedParameters() ) {
			System.out.println( x );
		}
	}
	
	public XEOQLPreProcessor( String boql, Object ... inParametersArray ) {
		this.inBoql = boql;
		this.inParametersArray = inParametersArray;
		if( inParametersArray != null && inParametersArray.length > 0 ) {
			this.outParametersArray = new ArrayList<Object>( this.inParametersArray.length );
		}
	}
	
	public String processQl() {
		
		// No parameters,
		if( inParametersArray == null || inParametersArray.length == 0 ) {
			return inBoql;
		}
		
		outParametersArray.clear();
		
		StringBuilder result = new StringBuilder();
		StringBuilder content = new StringBuilder(1);
		
		boolean inSide = false;
		boolean inSideChar = false;
		int questionCount = 0;
		
		for( char c : inBoql.toCharArray() ) {
			switch( c ) {
				case '{':
					content = new StringBuilder();
					if( !inSide && !inSideChar ) {
						inSide = true;
					}
					else {
						result.append( c );
					}
					break;
				case '}':
					if( inSide) {
						int idx = 0;
						try {
							idx = Integer.parseInt( content.toString() );
							if( idx >= this.outParametersArray.size() ) {
								questionCount++;
							}
							this.outParametersArray.add( this.inParametersArray[ idx ] );
							result.append( '?' );
						}
						catch( IndexOutOfBoundsException e ) {
							throw new XEOQLParserException(this.inBoql, "Missing IN/OUT parameter for index " + idx , null);
						}
						catch( NumberFormatException e ) {
							result.append( '{' ).append( content ).append( c );
						}
					}
					else {
						result.append( '}' );
					}
					inSide = false;
					break;
				case '?':
					if( !inSideChar ) {
						try {
							this.outParametersArray.add( this.inParametersArray[ questionCount ] );
							questionCount++;
						} catch (IndexOutOfBoundsException e) {
							throw new XEOQLParserException(this.inBoql, "Missing IN/OUT parameter at index " + (questionCount+1) , null);
						}
					}
					result.append( c );
					break;
				case '\'':
					inSideChar = !inSideChar;
				default:
					if( inSide ) {
						if( Character.isDigit( c ) ) {
							content.append( c );
						}
						else {
							result.append( '{' ).append( content ).append( c );
							inSide=false;
						}
					}
					else
						result.append( c );
			}
			
		}
		return result.toString();
	}
	
	public Object[] getProcessedParameters() {
		return this.outParametersArray.toArray();
	}
	

}
