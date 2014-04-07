package xeo.api.base.impl.ql;

import java.util.ArrayList;
import java.util.List;

import xeo.api.base.XEOLovPair;
import xeo.api.base.XEOModelBase;
import xeo.api.base.exceptions.XEOQLParserException;

public class XEOQLPreProcessor {
	
	private Object[] inParametersArray;
	private List<Object> outParametersArray;
	private String   inBoql;
	
	public XEOQLPreProcessor( String boql, Object ... inParametersArray ) {
		this.inBoql = boql;
		if( inParametersArray != null ) {
			this.inParametersArray = new Object[ inParametersArray.length ];
			for( int i=0; i < inParametersArray.length; i++ ) {
				Object parameter = this.inParametersArray[i] = inParametersArray[i];
				if( parameter instanceof XEOModelBase ) {
					this.inParametersArray[i] = ((XEOModelBase)parameter).getBoui();
				}
				else if( parameter instanceof XEOLovPair<?> ) {
					this.inParametersArray[i] = ((XEOLovPair<?>)parameter).getValue();
				}
			}
		}
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
		int count = 0;
		
		for( char c : inBoql.toCharArray() ) {
			count++;
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
							String boql = this.inBoql.substring(0,count-content.length()-2)
									+ "{<<" + content + ">>}" + this.inBoql.substring( count );
							throw new XEOQLParserException(
									this.inBoql, 
									"Missing parameter for index " + idx + " Query:" + boql, 
									null
							);
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
							String boql = this.inBoql.substring(0,count-1)
									+ "<<?>>" + this.inBoql.substring( count );
							throw new XEOQLParserException(
									this.inBoql, 
									"Missing parameter for index " + questionCount + " Query:" + boql, 
									null
							);
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
			
			if( questionCount != this.outParametersArray.size() ) {
				throw new XEOQLParserException(this.inBoql,
						String.format(
								"The number of parameters in the query are difer from the argument. In the query are %d and was passed %d \n Query:%s",
								questionCount, 
								this.outParametersArray.size(),
								this.inBoql
						), 
						null
				);
				
			}
		}
		return result.toString();
	}
	
	public Object[] getProcessedParameters() {
		return this.outParametersArray.toArray();
	}
	

}
