package exec;
import java.io.*;

import error.Exc;
import error.Exc_Exec;
import error.Exc.ErrorType;

/*
 * Printing the output stored in OUTPUT 
 * @author Domenico Sacca'
 */
public class PrintOutput {
	
	static int iOUT;
	
	static boolean isStandardOutStream=true;
	static String outStreamFile;
	
	static String tab  ="     "; // for pretty printing
	static String tab2 ="   "; // 2 spaces less than tab
	
	static public void setStandardOutStream() {
		isStandardOutStream = true;
	}

	static public void setFileOutStream( String fileName ) {
		isStandardOutStream = false;
		outStreamFile = fileName;
	}

	static public void printOutput ( boolean skipLine) throws Exc {
		if ( !isStandardOutStream  ) {
			try {
				FileOutputStream f = new FileOutputStream(outStreamFile);		    
				System.setOut(new PrintStream(f));
	          } catch ( Exception e ) {
	        	  System.setOut(System.out);
	        	  throw new Exc_Exec(ErrorType.WRONG_FILE, e.getMessage()+
	        			  " - output not saved!");
	          }			
		}
		iOUT=0; 
		try {
			while ( iOUT < Exec.OP ) {
				typedPrintV(Exec.OUTPUT[iOUT], (int) Exec.OUTPUT[iOUT+1]); 
				iOUT +=2;
			}
			if ( skipLine || Exec.OP > 0) 
				System.out.print("\n");
			if ( !isStandardOutStream ) {
				System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
				System.out.println("- written output to file '"+outStreamFile+"'");
			}
		}	catch ( Exc e ) {
				System.setOut(System.out);
				throw e;
			}
			catch ( Exception e) {
				System.setOut(System.out);
				throw new Exc_Exec(Exc.ErrorType.WRONG_ADDRESS, " to OUTPUT");
		}
		Exec.OP=0;
	}
	
	static String getStringHeap (int v) throws Exc {
		String s="";
		int n = (int)Exec.MEM[v];
		int kk = (int)v-1;
		for ( int i = 0; i < n; i++ ) {
			s+= (char) Exec.MEM[kk];
			kk--;
		}
		return s;
	}
	
	static public String getStringGV(int v) throws Exc {
		v= (int)Exec.MEM[v*2];
		return getStringHeap(v);
	}

	static void printStringExc(int start, int end ) throws Exc {
		for ( int i = start; i >  end; i-- ) 
			System.out.print(""+ (char) Exec.MEM[i]);
	}
	
	static int findColon(int kk, int n ) throws Exc {
		int kk0=kk;
		for ( int i = 0; i < n; i++ ) {
			if ( (char) Exec.MEM[kk] == ':' )
				return kk;
			else
				kk--;
		}
		return kk0;
	}

	static void typedPrintV ( double v, int t ) throws Exc {
		switch ( t ) {
		case Exec.StringExcF:
			System.out.print("\n**Computation stopped by exception \"");
			printString((int)v);
			System.out.print ("\" thrown by ");
			break;
		case Exec.NullT:
			System.out.print("");
			break;
		case Exec.NullQF:
			System.out.print("null");
			break;
		case Exec.IntT:
			System.out.print(""+(int) v);
			break;
		case Exec.LongT:
			System.out.print(""+Double.doubleToRawLongBits(v));
			break;
		case Exec.CharQF:
			printQuotedChar((char)v);
			break;
		case Exec.CharT:
			System.out.print(""+(char) v);
			break;
		case Exec.BoolT:
			printBool((int)v);
			break;
		case Exec.DoubleT:
			System.out.print(""+ v);
			break;
		case Exec.ListT:
			printVarVal(Exec.ListT,v);
			break;
		case Exec.ListIndentAllF:
			System.out.print("\n[ ");;
			printQVarVal(Exec.ListT,v,0,true);
			break;
		case Exec.ListIndent1F:
			System.out.print("\n[ ");
			printQ1VarVal(Exec.ListT,v);
			break;
		case Exec.JsonT:
			printVarVal(Exec.JsonT,v);
			break;
		case Exec.JsonIndentAllF:
			System.out.print("\n{ ");
			printQVarVal(Exec.JsonT,v,1,true);
			break;
		case Exec.JsonIndent1F:
			System.out.print("\n{ ");
			printQ1VarVal(Exec.JsonT,v);
			break;
		case Exec.StringQF:
			printQuotedString((int)v);
			break;
		case Exec.StringT: 
			printString((int)v);
			break;
		case Exec.TypeT: case Exec.MetaTypeT:
			System.out.print(""+ Exec.types[(int)v]);
			break;
		default:
			throw new Exc_Exec(Exc.ErrorType.WRONG_PRINT_FORMAT);		
		}
	}
	
	private static void printBool( int v) {
		if ( v == 0 )
			System.out.print("false");
		else
			System.out.print("true");		
	}

	private static void printEscapeChar(char v) {
		System.out.print(""+acceptEscapeChar(v));
	}

	public static void printQuotedChar(char v) {
		System.out.print("'");
		printEscapeChar(v);
		System.out.print("'");		
	}
	
	public static void printQuotedString(int v) {
		System.out.print('"');
		int n = (int)Exec.MEM[(int) v];
		int kk = (int)v-1;
		for ( int i = 0; i < n; i++ ) {
			printEscapeChar( (char) Exec.MEM[kk]);
			kk--;
		}
		System.out.print('"');		
	}

	public static void printString(int v) {
		int n = (int)Exec.MEM[(int) v];
		int kk = (int)v-1;
		for ( int i = 0; i < n; i++ ) {
			System.out.print(""+ (char) Exec.MEM[kk]);
			kk--;
		}
	}

//	private static void printQuotedStringNoEsc(int v) {
//		System.out.print('"');
//		int n = (int)Exec.MEM[(int) v];
//		int kk = (int)v-1;
//		for ( int i = 0; i < n; i++ ) {
//			System.out.print(""+ (char) Exec.MEM[kk]);
//			kk--;
//		}
//		System.out.print('"');		
//	}

	public static void printVarVal(int tSymb, double val ) throws Exc {
		if( tSymb == Exec.NullT ) {
			System.out.print("null");
			return;
		}
		if( tSymb == Exec.TypeT || tSymb == Exec.MetaTypeT ) {
			System.out.print(Exec.types[(int)val]);
			return;
		}
		if( tSymb == Exec.BoolT ) {
			printBool((int)val);
			return;
		}
		if( tSymb == Exec.DoubleT ) {
			System.out.print(val);
			return;
		}
		if( tSymb == Exec.IntT ) {
			System.out.print((int)val);
			return;
		}
		if( tSymb == Exec.LongT ) {
			System.out.print(Double.doubleToRawLongBits(val));
			return;
		}
		if( tSymb == Exec.CharT ) {
			System.out.print("'"+(char)val+"'");
			return;
		}
		if( tSymb == Exec.StringT ) {
			// printQuotedStringNoEsc((int)val);
			printQuotedString((int)val);
			return;
		}
		if( tSymb == Exec.ListT ) {
			System.out.print("[ ");
			int ind = (int) val; 
			ind=skipNullElements(ind); // skip possible deleted list elements
			boolean first =true;
			if ( ind != Exec.nullValue  ) {
				do {
					if ( !first )
						System.out.print(", ");
					val = Exec.MEM[ind];
					int type = (int)Exec.MEM[ind-1];
					printVarVal(type,val);
					ind = (int)Exec.MEM[ind-2];
					ind=skipNullElements(ind); // skip possible deleted list elements
					first=false;
				} while (ind!=0);
			}
			System.out.print(" ]");
			return;
		}
		if( tSymb == Exec.JsonT ) {
			System.out.print("{ ");
			int ind = (int) val; 
			ind = (int)Exec.MEM[ind-2]; // skip the initial null field
			ind=skipNullFields(ind); // skip possible deleted fields
			boolean first =true;
			if ( ind != Exec.nullValue ) {
				do {
					int indField = (int) Exec.MEM[ind];
					int type = (int)Exec.MEM[indField-1];
						if ( !first )
							System.out.print(", ");
					val = -Exec.MEM[indField-2];
					printVarVal(Exec.StringT,val);
					System.out.print(": ");
					val = Exec.MEM[indField];
					printVarVal(type,val);
					first=false;
					ind = (int)Exec.MEM[ind-2];
					ind=skipNullFields(ind); // skip possible deleted fields
				} while (ind!=0);
			}
			System.out.print(" }");
		}
	}
	
	public static void printQ1VarVal(int tSymb, double val ) throws Exc {
		if( tSymb == Exec.ListT ) {
			int ind = (int)val; 
			ind=skipNullElements(ind); // skip possible deleted list elements
			if ( ind !=Exec.nullValue ) {
				boolean first = true;
				do {
					if ( !first )
						printTab(1);
					else
						System.out.print(tab2);
					val = Exec.MEM[ind];
					int type = (int)Exec.MEM[ind-1];
					printVarVal(type,val);
					ind = (int)Exec.MEM[ind-2];
					first=false;
					ind=skipNullElements(ind); // skip possible deleted list elements
					if ( ind!=Exec.nullValue ) 
						System.out.print(",");
				} while (ind!=0);
				printTab(0);
			}
			System.out.print("]");
			return;
		}
		if( tSymb == Exec.JsonT ) {
			int ind = (int) val; 
			ind = (int)Exec.MEM[ind-2]; // skip the initial null field
			ind=skipNullFields(ind); // skip possible deleted fields
			if ( ind != Exec.nullValue ) {
				do {
					int indField = (int) Exec.MEM[ind];
					int type = (int)Exec.MEM[indField-1];
					printTab(1);
					val = -Exec.MEM[indField-2];
					printVarVal(Exec.StringT,val);
					System.out.print(": ");
					val = Exec.MEM[indField];
					printVarVal(type,val);
					ind = (int)Exec.MEM[ind-2]; 
					ind=skipNullFields(ind); // skip possible deleted fields
					if ( ind!=Exec.nullValue )
						System.out.print(",");
				} while (ind!=0);
				printTab(0);
			}
			System.out.print("}");
			return;
		}
		printQVarVal(tSymb,val,0,true);
	}
	
	public static void printQVarVal(int tSymb, double val, int ntab, boolean first ) throws Exc {
		if( tSymb == Exec.NullT ) {
			System.out.print("null");
			return;
		}
		if( tSymb == Exec.TypeT || tSymb == Exec.MetaTypeT ) {
			System.out.print(Exec.types[(int)val]);
			return;
		}
		if( tSymb == Exec.BoolT ) {
			printBool((int)val);
			return;
		}
		if( tSymb == Exec.DoubleT ) {
			System.out.print(val);
			return;
		}
		if( tSymb == Exec.IntT ) {
			System.out.print((int)val);
			return;
		}
		if( tSymb == Exec.LongT ) {
			System.out.print(Double.doubleToRawLongBits(val));
			return;
		}
		if( tSymb == Exec.CharT ) {
			System.out.print("'"+(char)val+"'");
			return;
		}
		if( tSymb == Exec.StringT ) {
			// printQuotedStringNoEsc((int)val);
			printQuotedString((int)val);
			return;
		}
		if( tSymb == Exec.ListT ) {
			int ind = (int)val; 
			ind=skipNullElements(ind); // skip possible deleted list elements
			if ( ind !=Exec.nullValue ) {
				ntab++;
				do {
					if ( !first )
						printTab(ntab);
					else
						System.out.print(tab2);
					val = Exec.MEM[ind];
					int type = (int)Exec.MEM[ind-1];
					if ( type==Exec.ListT ) 
						System.out.print("[ ");
					else
						if ( type==Exec.JsonT ) {
							System.out.print("{ ");
							ntab++;
						}						
					printQVarVal(type,val,ntab,true);
					ind = (int)Exec.MEM[ind-2];
					ind=skipNullElements(ind); // skip possible deleted list elements
					if ( ind!=Exec.nullValue )
						System.out.print(",");
					first=false;
					if ( type==Exec.JsonT ) 
						ntab--;
				} while (ind!=0);
				ntab--;
				printTab(ntab);
			}
			System.out.print("]");
			return;
		}
		if( tSymb == Exec.JsonT ) {
			int ind = (int) val; 
			ind = (int)Exec.MEM[ind-2]; // skip the initial null field
			ind=skipNullFields(ind); // skip possible deleted fields
			first =true;
			if ( ind != Exec.nullValue ) {
				do {
					int indField = (int) Exec.MEM[ind];
					int type = (int)Exec.MEM[indField-1];
					printTab(ntab);
					val = -Exec.MEM[indField-2];
					printVarVal(Exec.StringT,val);
					System.out.print(": ");
					if ( type==Exec.ListT ) {
						System.out.print("[");
					}
					else
						if ( type==Exec.JsonT ) {
							System.out.print("{ ");
							ntab++;
						}						
					val = Exec.MEM[indField];
					printQVarVal(type,val,ntab, false);
					if ( type==Exec.JsonT  ) 
						ntab--;						
					first=false;
					ind = (int)Exec.MEM[ind-2]; 
					ind=skipNullFields(ind); // skip possible deleted fields
					if ( ind!=Exec.nullValue )
						System.out.print(",");
				} while (ind!=0);
			}
			if ( !first ) {
				ntab--;
				printTab(ntab);
			}
			System.out.print("}");
		}
	}
	
	private static int skipNullElements(int ind ) throws Exc {
		if ( ind == Exec.nullValue )
			return Exec.nullValue;
		if ( Exec.MEM[ind-1] == Exec.NoType ) 
      	  throw new Exc_Exec(ErrorType.LIST_DELETED);			
		if ( Exec.MEM[ind]==Exec.deletedNullValue && Exec.MEM[ind-1] == Exec.NullT ) { 
			ind=(int)Exec.MEM[ind-2];
			return skipNullElements(ind);
		}
		else
			return ind;
	}
	
	private static int skipNullFields(int ind ) throws Exc {
		if ( ind == Exec.nullValue )
			return Exec.nullValue;
//		if ( Exec.MEM[ind-1] == Exec.NoType ) 
//	      	  throw new Exc_Exec(ErrorType.JSON_DELETED);			
		int indF= (int)Exec.MEM[ind];
		if ( Exec.MEM[indF]==Exec.deletedNullValue && Exec.MEM[indF-1] == Exec.NullT ) {
			ind=(int)Exec.MEM[ind-2];
			return skipNullFields(ind);
		}
		else
			return ind;
	}
	
	private static void printTab( int n ) {
		System.out.println();
		for ( int i =1; i<=n; i++ )
			System.out.print(tab);
	}
	
	  private static String acceptEscapeChar ( char c ) {
			switch( c ) {
				case '\n': return "\\n";  // newline
				case '\r': return "\\r"; // carriage return
				case '\f': return "\\f";  // formfeed
				case '\t': return "\\t";  // tab
				case '\"': return "\\\"";  // double quote
				case '\'': return "\\\'";  // single quote
				case '\\': return "\\\\";  // backslash 
				case '\b': return "\\b";  // backspace
				default: return ""+c;
				}

	  }


}
