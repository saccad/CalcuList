package synt;

import ioHandler.History;
import ioHandler.IOH;
import error.Exc;
import error.Exc_Synt;
import error.Exc.ErrorType;
import exec.Instruction.Operator;
import exec.Exec;

public class Lex {

	enum Token {
		NOTOKEN("Null Token"), DOUBLE("DOUBLE"), INT("INT"), 
		LONG("LONG"),ID("ID"), IDL ("labeled ID"),
		TYPE("TYPE"), QMARK("'?'"), BUILTIN("'_'"), DOT("'.'"),
		OPAR("'('"), CPAR("')'"), PLUS("'+'"), MINUS("'-'"), STAR("'*'"), 
		DIV("'/'"), DIVTR("'//'"), COMMA("','"), 
		SCOLON("';'"), ASSIGN("'='"), OPASSIGN("<OP>="), DUMMY("'&'"),
		GTLT("GTLT"), EQNEQ("EQNEQ"),
		OSPAR("'['"), CSPAR("']'"), HASH("'#'"),
		BAR("'|'"), AND("'&&'"), OR("'||'"), EMARK("'!'"), COLON("':'"), 
		TRUE("'true'"), FALSE("'false'"), HALT("'halt'"), COMMENT ("COMMENT"),
		NULL("'null'"), CHAR("CHAR"), STRING("STRING"), EXC("exc"),
		PRINT("'^'"), OBRACE("'{'"), CBRACE("'}'"),
		OBRACESET("'{!'"), CBRACESET("'!}'"),OBRACEPRINT("'{^'"),CBRACEPRINT("'^}'"),  
		PRINTADD("'%+'"), PRINTOPT ("'%' OPTION"), REMAINDER("'%'"),
		CAST("'@'"),
		FILEOUT("'>>'"), FILEIN("'<<'"), LAMBDA("'lambda'"), END("'null char'");
		String stToken;
		Token ( String st ) {
			stToken=st;
		}
	}

	enum AssOpType {
		PLUS(Token.PLUS), MINUS(Token.MINUS), MULT(Token.STAR), DIV(Token.DIV), DIVTR(Token.DIVTR);
		Token opToken;
		AssOpType ( Token t ) {
			this.opToken = t;
		}
	}
	//* operator types of OPASSIGN

	enum GTLTOpType {
		GT(Operator.GT), GTE(Operator.GTE), LT(Operator.LT), LTE(Operator.LTE);
		Operator relOp;
		GTLTOpType ( Operator relOp) {
			this.relOp = relOp;
		}
	}
	//* types of GTLT

	enum EQNEQOpType {
		EQ(Operator.EQ), NEQ(Operator.NEQ);
		Operator relOp;
		EQNEQOpType ( Operator relOp) {
			this.relOp = relOp;
		}
	}
	//* types of RELOP

	public enum BIFType {
		EXP(Operator.EXP), POW(Operator.POW), LOG(Operator.LOG), LEN(Operator.LEN), 
		RAND(Operator.RAND), IND(Operator.STRIND), TUPLE(Operator.TUPLE), ISKEY(Operator.ISKEY),
		GDATE(Operator.GDATE),PDATE(Operator.PDATE);
		Operator biOp;
		BIFType (Operator biOp) {
			this.biOp=biOp;
		}
	 }


	private Token token; //  current token
 	private AssOpType assOpType; //  type of assign operator (PLUS, MINUS, MULT, DIV, DIVTR)
 	private GTLTOpType GTLTOpToken; //  type of relational operator (GT, GTE, LT, LTE)
 	private EQNEQOpType EQNEQOpToken; //  type of relational operator (GT, GTE, LT, LTE)
 	private int typeType; //  type of type (INTT, CHART, DOUBLET, BOOLT, STRINGT, LISTT, TYPET)
	private String svalue; // token string for ID or NUM
	private String label; // possible label for ID
	private char cvalue; // character value for CHAR
	private double dval; // double value for DOUBLE
	private int iPrintOption=0; // 1: QUOTED, 2: INDENTED, 3: FULL INDENTED
	private long ival; // long value for INT or LONG
	private IOH ioh; // passed by the parser
	private boolean commentOn = false; // true if a comment must be considered as a token 
	static final int maxLenComment= 1024; // 
	static final int maxLenString= 256; // 
	private static final int maxLenName= 64; // 
	private boolean stringOver;
	private boolean commentOver;
	private boolean truncatableString;


  	Lex( IOH ioh) {
  	 	this.ioh = ioh;
    	token = Token.NOTOKEN; // no token at the beginning
    	commentOn=false;
    	truncatableString=true;
  	}
  	
  	int indPrint() {
  		return iPrintOption;
  	}

   void nextToken() throws Exc {
 		ioh.skipWhite();
	    	if ( ioh.isLetter() )
	    		getName();
	    	else
	    		if ( ioh.isDigit() )
	    			getNum("",false);
	    		else  
	     			getOther();
   }
   
   BIFType getBuiltIn  ( ) throws Exc {
		 if ( svalue.equals("rand") )
			 return BIFType.RAND;
		if ( svalue.equals("exp")) 
			return BIFType.EXP;
		if ( svalue.equals("log")) 
			return BIFType.LOG;
		if ( svalue.equals("pow")) 
			return BIFType.POW;
		if ( svalue.equals("len")) 
			return BIFType.LEN;
		if ( svalue.equals("ind")) 
			return BIFType.IND;
		if ( svalue.equals("tuple")) 
			return BIFType.TUPLE;
		if ( svalue.equals("isKey")) 
			return BIFType.ISKEY ;
		if ( svalue.equals("gDate")) 
			return BIFType.GDATE ;
		if ( svalue.equals("pDate")) 
			return BIFType.PDATE ;
		throw new Exc_Synt(ErrorType.WRONG_ID, "'"+svalue+
								"' - expected: 'exp' or 'log' or 'pow' or 'len' or 'rand' or 'ind' or 'tuple'");
	 }

   
  String tokenFullDescr() {
	  if ( token == Token.BUILTIN || token == Token.GTLT || token == Token.EQNEQ || token == Token.TYPE ) 
		  return "\'"+svalue+"\'";
	  else 
		  if ( token == Token.DOUBLE || token == Token.LONG || token == Token.INT || token==Token.ID )
			  return token+"(\'"+svalue+"\')";
		  else
			  return token.stToken;
  }

  Token currToken() {
  		return token;
  }

  String tokenDescr( Token t ) {
	  return t!= null? t.stToken:"null token";
}

  String currTokenDescr ( ) {
			return tokenDescr(token);
}

  //**  returns the label of the ID
  String currLabel() {
  		return label;
  }

  //**  return the type of operator for the current token OPASSIGN.
 Token currAssOp() {
 		return assOpType.opToken;
 }

   //**  returns the type of relational operator for the current token GTLT.
  GTLTOpType GTLT_Op() {
  		return GTLTOpToken;
  }

  //**  returns the type of relational operator for the current token EQNEQ.
  EQNEQOpType EQNEQ_Op() {
 		return EQNEQOpToken;
 }

  //**  returns the type of type for the token TYPE.
  int currType() {
  		return typeType;
  }

  //**  set commentOn to true or false
  void setComment( boolean isOn ) {
  		commentOn = isOn;
  }
  
  void setTruncatableString( boolean isOn ) {
	  truncatableString=isOn;
  }


  //** gets token ID or TRUE or FALSE
  void getName() throws Exc {
   		svalue = ""; label = ""; 
   		int nChar=0;
    	do {
    		nChar++;
    		if ( nChar > maxLenName )
				throw new Exc_Synt(ErrorType.NAME_TOO_LONG,maxLenName+"char - '"+svalue+"'");
    	 	svalue += ioh.currChar();
      		ioh.nextChar();

    	} while ( ioh.isLetterOrDigitOrUnderscore() );
    	getName1();   		    		
    	History.addSubStr(svalue);
  }
  
  //** gets token ID or TRUE or FALSE
  void getName1() throws Exc {
        if ( svalue.equals("true")) {
    		token = Token.TRUE;
    		return;
        }
	    if ( svalue.equals("false")) {
		token = Token.FALSE;
		return;
	    }
		if ( svalue.equals("halt")) {
			token=Token.HALT;
			return;
		  }
		if (svalue.equals("null")) {
				token=Token.NULL;
				return;
		}
		if (svalue.equals("long")) {
			token=Token.TYPE; typeType=Exec.LongT;
			return;
		}
		if (svalue.equals("int")) {
			token=Token.TYPE; typeType=Exec.IntT;
			return;
		}
		if (svalue.equals("double")) {
			token=Token.TYPE; typeType=Exec.DoubleT;
			return;
		}
		if (svalue.equals("char")) {
			token=Token.TYPE; typeType=Exec.CharT;
			return;
		}
		if (svalue.equals("bool")) {
			token=Token.TYPE; typeType=Exec.BoolT;
			return;
		}
		if (svalue.equals("string")) {
			token=Token.TYPE; typeType=Exec.StringT;
			return;
		}
		if (svalue.equals("list")) {
			token=Token.TYPE; typeType=Exec.ListT;
			return;
		}
		if (svalue.equals("json")) {
			token=Token.TYPE; typeType=Exec.JsonT;
			return;
		}
		if (svalue.equals("type")) {
			token=Token.TYPE; typeType=Exec.TypeT;
			return;
		}
		if (svalue.equals("exc")) {
			token=Token.EXC; 
			return;
		}
		if (svalue.equals("lambda")) {
			token=Token.LAMBDA; 
			return;
		}
    	if ( ioh.currChar() == '.') {
    		ioh.nextChar();
    		if ( !ioh.isLetter()  ) {
				token=Token.NOTOKEN;
				throw new Exc(ErrorType.INVALID_SYM, " '.'");
    		}
    		label = svalue; svalue +='.';
        	do {
        	 	svalue += ioh.currChar();
          		ioh.nextChar();

        	} while ( ioh.isLetterOrDigitOrUnderscore() ); 
        	token = Token.IDL;
    	}
    	else
    		token = Token.ID;
  }

  //** gets token NUM
  void getNum( String sIn, boolean initialPoint ) throws Exc {
   		token = initialPoint? Token.DOUBLE: Token.LONG;
    	svalue = sIn;
    	getInt();
    	if ( !initialPoint &&  ioh.currChar() == '.' ) {
    			token= Token.DOUBLE;
    			svalue += ioh.currChar();
			    ioh.nextChar();
			    if ( ioh.isDigit() ) 
			    	getInt();
     	}
		if ( ioh.currChar() == 'e' || ioh.currChar() == 'E' ) {
			token= Token.DOUBLE;
		 	svalue += ioh.currChar();
		    ioh.nextChar();
		    if ( ioh.currChar() == '+' || ioh.currChar() == '-' ) {
		    	svalue += ioh.currChar();
		    	ioh.nextChar();
			};
			if ( ioh.isDigit() )
				getInt();
			else
				throw new Exc_Synt(ErrorType.WRONG_DOUBLE);
  		}
		if ( token==Token.DOUBLE ) 
			try {
				dval = Double.parseDouble(svalue); 
				History.addSubStr(Double.toString(dval));
			}
			catch ( NumberFormatException e ) {
				throw new Exc_Synt(ErrorType.WRONG_DOUBLE);
			}
		else 
			try {
				ival = Long.parseLong(svalue); 
				History.addSubStr(Long.toString(ival));				
				if ( ival >= Integer.MIN_VALUE && ival <= Integer.MAX_VALUE )
					token=Token.INT;
			}
			catch ( NumberFormatException e ) {
				if ( svalue.equals("9223372036854775808"))
					throw new Exc_Synt(ErrorType.WRONG_MAX_VAL_LONG);
				else
					throw new Exc_Synt(ErrorType.WRONG_INT_LONG);
			}
  }
  
  void getInt(  ) {
		while  ( ioh.isDigit() ) {
			svalue += ioh.currChar();
			try {
				ioh.nextChar();
			}
			catch (Exception e) {
				// no exception expected!!
			}
		};
	  
  }

  //** recognize other tokens
  void getOther() throws Exc {
  		switch ( ioh.currChar() ) {
  			case (char)0: token =Token.END; break;
  			case '.': 	ioh.nextChar();
  						if ( ioh.isDigit() )
  							getNum(".",true); // initialPoint is true
  						else {
  							History.addChar('.');
  							token=Token.DOT;
  						} 							
  						break;
  			case '{': token = Token.OBRACE; ioh.nextChar(); History.addSubStr(" {");
  						if ( ioh.currChar()=='!' ) {
  							token= Token.OBRACESET; ioh.nextChar(); History.addSubStr("! ");
  						}
  						else
  							if ( ioh.currChar()=='^' ) {
  	  							token= Token.OBRACEPRINT; ioh.nextChar(); History.addSubStr("^ "); 								
  							}  
  							else
  								History.addChar(' ');
  						break;
  			case '}': token = Token.CBRACE; History.addSubStr(" } ");
  						ioh.nextChar(); break;
  			case '?': token = Token.QMARK; History.addSubStr("? "); 
  						ioh.nextChar(); break;
  			case '(': token = Token.OPAR; History.addChar('(');
  						ioh.nextChar(); break;
  			case ')': token = Token.CPAR; History.addChar(')');
  						ioh.nextChar(); break;
  			case '#': token = Token.HASH; History.addChar('#');
				ioh.nextChar(); break;
  			case '_': token = Token.BUILTIN; History.addChar('_');
				ioh.nextChar(); break;
  			case '+': 	ioh.nextChar();
  						if ( ioh.currChar() == '=' ) {
  							token=Token.OPASSIGN; assOpType=AssOpType.PLUS;
  							ioh.nextChar();
  						}
  						else {
  							token = Token.PLUS; 
  							History.addChar('+');
  						}
  						break;
  			case '-':   ioh.nextChar();
						if ( ioh.currChar() == '=' ) {
							token=Token.OPASSIGN; assOpType=AssOpType.MINUS;
							ioh.nextChar();
						}
						else {
							token = Token.MINUS; 
							History.addChar('-');
						}
  						break;
  			case '*':   ioh.nextChar();
						if ( ioh.currChar() == '=' ) {
							token=Token.OPASSIGN; assOpType=AssOpType.MULT;
							ioh.nextChar();
						}
						else {
							token = Token.STAR; 
							History.addChar('*');
						}
  						break;
  			case '^': token = Token.PRINT; ioh.nextChar();
					  if ( ioh.currChar()=='}' ) {
						  token= Token.CBRACEPRINT; ioh.nextChar(); History.addSubStr(" ^} "); 								
					  }  
					  else
						  History.addChar('^');				 
  					  break;
  			case ',': token = Token.COMMA;
  						ioh.nextChar(); break;
  			case ';': token = Token.SCOLON; History.addChar(';');
  						ioh.nextChar(); break;
  			case '[': token = Token.OSPAR; History.addChar('[');
  						ioh.nextChar(); break;
  			case ']': token = Token.CSPAR; History.addChar(']');
  						ioh.nextChar(); break;
  			case '/':  ioh.nextChar(); 
  				  if ( ioh.currChar() == '*' ) {
  					    ioh.setPrompt(false);
  					    boolean endComment = false; token=Token.NOTOKEN;
  					    if ( commentOn ) {
  					    	token=Token.COMMENT;
  					    	svalue="";
  					    }
  					    ioh.nextChar();
  					    int nChar=0;
 	  					do {
		  	  		   		while ( ioh.currChar() != '*' ) {
		  	  		   			if ( token == Token.COMMENT  && nChar <=maxLenComment ) {
		  	  		   				nChar++;
			  	  		   			if ( ioh.currChar() == '\\') {
			  	  		   				ioh.nextChar();
			  	  		   				svalue += acceptEscapeChar(ioh.currChar());
			  	   		   			}
			  	  		   			else
			  	  		   				svalue += ioh.currChar();
		  	  		   			}
		  	  		   		 	ioh.nextChar();
		  	  		   		};
		  	  		   	    ioh.nextChar();
		  	  		   	    if ( ioh.currChar() == '/') {
		  	  		   	    	endComment = true;
		  	  		   	    }
		  	  		   	    else
		  	  		   			if ( token == Token.COMMENT && nChar <=maxLenComment ) {
		  	  		   				nChar++;
		  	  		   				svalue +='*';
		  	  		   			}
	  					} while (! endComment);
	  					ioh.nextChar(); 
	  					if ( token == Token.NOTOKEN ) 
	  						nextToken();
	  					else
	  						if ( svalue.length()>maxLenComment ) {
	  							commentOver=true;
	  							svalue= svalue.substring(0, maxLenComment-3)+"...";
	  						}
	  						else
	  							commentOver=false;
  				  }
  				  else {
  					  if ( ioh.currChar() == '/' ) {
  						ioh.nextChar();
  						if ( ioh.currChar() == '=' ) {
  							token=Token.OPASSIGN; assOpType=AssOpType.DIVTR;
  							ioh.nextChar();
  						}
  						else {
  							token = Token.DIVTR; 
  							History.addSubStr("//");
  						}
   					  }
  					  else
    						if ( ioh.currChar() == '=' ) {
      							token=Token.OPASSIGN; assOpType=AssOpType.DIV;
      							ioh.nextChar();
      						}
      						else {
      							token = Token.DIV; 
      							History.addChar('/');
      						}
  				  }
  				  break;
  			case '=': token = Token.ASSIGN; ioh.nextChar(); 
  					  if ( ioh.currChar() == '=' ) {
  						  	History.addSubStr("==");
  					  		token = Token.EQNEQ; EQNEQOpToken = EQNEQOpType.EQ; svalue="==";ioh.nextChar();
  					  }
  					  break;
  			case '>': token = Token.GTLT; GTLTOpToken = GTLTOpType.GT; svalue=">";
  					  History.addChar('>'); ioh.nextChar();
  					  if ( ioh.currChar() == '=' ) {
  						  	History.addChar('=');
  					  		GTLTOpToken = GTLTOpType.GTE; svalue=">="; ioh.nextChar();
  					  }
  					  else
  						  if ( ioh.currChar() == '>' ) {
  						  	History.addChar('>');
  					  		token = Token.FILEOUT; svalue=">>";ioh.nextChar();						  
  						  }
  					  break;
  			case '<': token = Token.GTLT; GTLTOpToken = GTLTOpType.LT; svalue="<";
  					  History.addChar('<'); ioh.nextChar();
  					  if ( ioh.currChar() == '=' ) {
						  	History.addChar('=');
  					  		GTLTOpToken = GTLTOpType.LTE; svalue="<="; ioh.nextChar();
  					  }
  					  else
  						  if ( ioh.currChar() == '<' ) {
  						  	History.addChar('<');
  					  		token = Token.FILEIN; svalue="<<";ioh.nextChar();
  					  }						  
    				 break;
  			case ':': token = Token.COLON; ioh.nextChar(); 
	  				 break;
  			case '!': token = Token.EMARK; ioh.nextChar();
  					  if ( ioh.currChar() == '=' ) {
  						    History.addSubStr("!=");
  					  		token = Token.EQNEQ; EQNEQOpToken = EQNEQOpType.NEQ; svalue="!=";ioh.nextChar();
  					  }
  					  else
  						  if ( ioh.currChar() == '}' ) {
    							token= Token.CBRACESET; ioh.nextChar(); History.addSubStr(" !} ");  							  
  						  }
  						  else
  							History.addChar('!');
 					  break;
  			case '|': token = Token.BAR; ioh.nextChar();
			  if ( ioh.currChar() == '|' ) {
			  		token = Token.OR; History.addSubStr(" || "); ioh.nextChar();
			  }
			  else
				  History.addChar('|'); 
			  break;
  			case '&': token = Token.DUMMY; ioh.nextChar();
			  if ( ioh.currChar() == '&' ) {
				  token = Token.AND; History.addSubStr(" && "); ioh.nextChar();
			  }
			  else 
				  History.addSubStr("& "); 
			  break;
  			case '\"': token = Token.STRING; History.addChar('\"'); ioh.nextChar();
  		   		svalue = ""; int nChar = 0;
  		   		while ( ioh.currChar() != '\"' ) {
  		   			if ( !truncatableString || nChar <=maxLenString ) {
  		   				nChar++;
	  		   			if ( ioh.currChar() == '\\') {
	  		   				History.addChar('\\'); ioh.nextChar();
	  		   				svalue += acceptEscapeCharHist(ioh.currChar());
	   		   			}
	  		   			else {
	   		   				svalue += ioh.currChar(); History.addChar(ioh.currChar());
	  		   			}
  		   			}
  		   			else 
  		   				if ( ioh.currChar() == '\\') {
  		   					ioh.nextChar();
  		   					acceptEscapeChar(ioh.currChar());
  		   				}
  		   		 	ioh.nextChar();
  		   		};
				if ( truncatableString && svalue.length()>maxLenString ) {
						stringOver=true;
						svalue= svalue.substring(0, maxLenString-3)+"...";
					}
					else
						stringOver=false;
				truncatableString=true;
   		   		History.addChar('\"'); ioh.nextChar();
				break;
 			case '\'': token = Token.CHAR; History.addChar('\''); ioh.nextChar();
 				if ( ioh.currChar() == '\\' ) {
 					History.addChar('\\'); 
 					ioh.nextChar();
  					cvalue=acceptEscapeCharHist(ioh.currChar()); 	
 				}
 				else {
 					cvalue = ioh.currChar(); ival = cvalue;
 					History.addChar(ioh.currChar());
 				}
  				ioh.nextChar();
  				if ( ioh.currChar () == '\'' ) {
  					History.addChar('\''); ioh.nextChar();
  				}
  				else
  					throw new Exc(ErrorType.MISSING_QUOTE);
			  break;
  			case '%': ioh.nextChar(); 
  				switch ( ioh.currChar() ) {
	 				case '"':  History.addSubStr(" %\" "); token=Token.PRINTOPT; 
	 					iPrintOption=1; ioh.nextChar(); break;
	 				case '*':  History.addSubStr(" %* "); token=Token.PRINTOPT; 
 					iPrintOption=3; ioh.nextChar(); break;
	 				case '>':  History.addSubStr(" %> "); token=Token.PRINTOPT; 
 					iPrintOption=2; ioh.nextChar(); break;
	 				case '+': History.addSubStr(" %+ "); token=Token.PRINTADD; 
	 					ioh.nextChar(); break;
	 				default: 
	 					token=Token.REMAINDER; History.addChar('%');
 				}
				break;
  			case '@': token = Token.CAST; History.addSubStr(" @"); ioh.nextChar();
				break; 			
			default:
 				char tchar = ioh.currChar(); token=Token.NOTOKEN; ioh.nextChar();
 				throw new Exc_Synt(ErrorType.INVALID_SYM, " '"+tchar+"'"+" (code = "+(int) tchar+')');
 		}
  }
  
  char acceptEscapeChar ( char c ) throws Exc {
		switch( c ) {
			case 'n': return '\n';  // newline
			case 'r': return '\r'; // carriage return
			case 'f': return '\f';  // formfeed
			case 't': return '\t';  // tab
			case '\"': return '\"';  // double quote
			case '\'': return '\'';  // single quote
			case '\\': return '\\';  // backslash 
			case 'b': return '\b';  // backspace
			case 'u': ioh.nextChar(); String ust = ""+ioh.currChar();
					  ioh.nextChar(); ust += ioh.currChar();
					  ioh.nextChar(); ust += ioh.currChar();
					  ioh.nextChar(); ust += ioh.currChar();
					  Integer code;
					  try {
						  code = Integer.parseInt(ust,16);
					  }
					  catch (Exception e ) {
							throw new Exc_Synt(ErrorType.WRONG_ESCAPE, " \\u"+ust+
									" - valid ones are \\b \\t \\n \\f \\r \\\" \\\' \\\\ \\u(0000-ffff)");
						  
					  }
				return Character.toChars(code)[0];  // unicode 
			default:
				throw new Exc_Synt(ErrorType.WRONG_ESCAPE, " \\"+c+
						" - valid ones are \\b \\t \\n \\f \\r \\\" \\\' \\\\ \\u(0000-ffff)");
			}

}
  
  char acceptEscapeCharHist ( char c ) throws Exc {
			switch( c ) {
 				case 'n': History.addChar('n'); return '\n';  // newline
 				case 'r': History.addChar('r'); return '\r'; // carriage return
 				case 'f': History.addChar('f'); return '\f';  // formfeed
 				case 't': History.addChar('t'); return '\t';  // tab
 				case '\"': History.addChar('\"'); return '\"';  // double quote
 				case '\'': History.addChar('\''); return '\'';  // single quote
 				case '\\': History.addChar('\\'); return '\\';  // backslash 
 				case 'b': History.addChar('b'); return '\b';  // backspace
 				case 'u': History.addChar('u');
 					ioh.nextChar(); String ust = ""+ioh.currChar();
				  	ioh.nextChar(); ust += ioh.currChar();
				  	ioh.nextChar(); ust += ioh.currChar();
				  	ioh.nextChar(); ust += ioh.currChar();
				  	History.addSubStr(ust);
					  Integer code;
					  try {
						  code = Integer.parseInt(ust,16);
					  }
					  catch (Exception e ) {
							throw new Exc_Synt(ErrorType.WRONG_ESCAPE, " \\u"+ust+
									" - valid ones are \\b \\t \\n \\f \\r \\\" \\\' \\\\ \\u(0000-ffff)");
						  
					  }
					return Character.toChars(code)[0];  // unicode 
 				default:
 					throw new Exc_Synt(ErrorType.WRONG_ESCAPE, " \\"+c+
 							" - valid ones are \\b \\t \\n \\f \\r \\\" \\\' \\\\ \\u(0000-ffff)");
 				}

  }
  
  void printH_OpAssign( Token t, boolean spaced ) {
	  if ( spaced )
		  History.addChar(' ');
	  History.addSubStr(t.stToken.substring(1, t.stToken.length()-1)+"=");
	  if ( spaced )
		  History.addChar(' ');
  }

  void printH_Assign( boolean spaced ) {
	  if ( spaced ) 
		  History.addSubStr(" = ");
	  else
		  History.addChar('=');
  }

  void printH_Colon( boolean spaced ) {
	  if ( spaced ) 
		  History.addSubStr(": ");
	  else
		  History.addChar(':');
  }

  void printH_Comma( boolean spaced ) {
	  if ( spaced ) 
		  History.addSubStr(", ");
	  else
		  History.addChar(',');
  }

// void printH_Dot( ) {
//		  History.addChar('.');
//  }

void accept( Token t ) throws Exc {
   	if ( token != t )
  			throw new Exc_Synt(ErrorType.WRONG_TOKEN,tokenFullDescr()+" - expected: "+t.stToken);
  		else 
  			nextToken();
  }

   void skipStatement ( ) {
	    setComment(false);
		if ( token != Token.SCOLON ) {
		   	while ( ioh.currChar() != ';'  ) 
				try {
					ioh.nextChar();
				}
				catch (Exception e) {
					// no exception expected!!
				}
		   	token = Token.SCOLON;
			try {
				ioh.nextChar();
			}
			catch (Exception e) {
				// no exception expected!!
			}
		}
	  }

   char CHARvalue() throws Exc {
 		if ( token == Token.CHAR  )  {
 			return cvalue;
 		}
 		else
 			throw new Exc_Synt(ErrorType.WRONG_TOKEN,token.stToken+" - expected: "+Token.CHAR.stToken);
 }
//
//   String mathName() throws Exc {
// 		if ( token == Token.BUILTIN  )  {
// 			return svalue;
// 		}
// 		else
// 			throw new Exc_Synt(ErrorType.WRONG_TOKEN,token.stToken+" - expected: "+Token.BUILTIN.stToken);
// }

  String IDName() throws Exc {
  		if ( token == Token.ID  || token == Token.IDL )  {
  			return svalue;
  		}
  		else
  			throw new Exc_Synt(ErrorType.WRONG_TOKEN,token.stToken+" - expected: "+Token.ID.stToken);
  }

  String stringVal() throws Exc {
		if ( token == Token.STRING  )  {
			return svalue;
		}
		else
			throw new Exc_Synt(ErrorType.WRONG_TOKEN,token.stToken+" - expected: "+Token.STRING.stToken);
 }
  
  String stringValNoCheckStr() throws Exc {
			return svalue;
 }
  
  boolean isStringOverflow() throws Exc {
		if ( token == Token.STRING  )  {
			return stringOver;
		}
		else
			throw new Exc_Synt(ErrorType.WRONG_TOKEN,token.stToken+" - expected: "+Token.STRING.stToken);	  
  }

  String commentString() throws Exc {
		if ( token == Token.COMMENT  )  {
			return svalue;
		}
		else
			throw new Exc_Synt(ErrorType.WRONG_TOKEN,token.stToken+" - expected: "+Token.COMMENT.stToken);
}

  boolean isCommentOverflow() throws Exc {
		if ( token == Token.COMMENT  )  {
			return commentOver;
		}
		else
			throw new Exc_Synt(ErrorType.WRONG_TOKEN,token.stToken+" - expected: "+Token.COMMENT.stToken);	  
  }

  double doubleVal() throws Exc {
		if ( token == Token.DOUBLE  )  {
  			return dval;
  		}
  		else
  			throw new Exc_Synt(ErrorType.WRONG_TOKEN,token.stToken+" - expected: "+Token.DOUBLE.stToken);
  }

  int intVal() throws Exc {
		if ( token == Token.INT ||  token == Token.CHAR )  {
  			return (int) ival;
  		}
  		else
  			throw new Exc_Synt(ErrorType.WRONG_TOKEN,token.stToken+" - expected: "+Token.INT.stToken);
  }

  long longVal() throws Exc {
		if ( token == Token.LONG )  {
  			return ival;
  		}
  		else
  			throw new Exc_Synt(ErrorType.WRONG_TOKEN,token.stToken+" - expected: "+Token.LONG.stToken);
  }

} // end class Lex
