/**********************************************************************
CalcuList (Calculator with List manipulation) is an educational 
language for teaching functional programming extended with some 
imperative and side-effect features, which are enabled under explicit 
request by the programmer.

Copyright (C) 2018  Prof. Saccà Domenico - University of Calabria (Italy)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
**********************************************************************/

package ioHandler;

import java.util.regex.*;

import error.Exc;
import sem.SymbH;
import sem.SymbolTableH;
import exec.Debug;
import exec.Exec;
import exec.PrintOutput;
import synt.Lex.BIFType;;

public class OutputH {
	  public static final String prompt = ">> ";
	  public static final String promptF = ".. ";
	  private static boolean isPrompt=true; // true if the previous line ends with semicolon
	  static private String release = "4.3.1";
	  static private String releaseDate = "December 24, 2020";
	  
	  static String labelP = "[a-zA-Z]\\w*\\u002E.*";

	  
	  static void printStart() {
		    System.out.print (
					"     *************************************************\n" +
					"                      *** CalcuList ***               \n" +
					"     *************************************************\n"+
					"     ********* Release "+release+" of "+releaseDate+" *******\n\n");
		    System.out.println ("** type '!!;' to display the list of service commands\n");

	  }
	  
	  static void errorCursor( int iLine ) {
		  	for (int i =0; i<=iLine; i++)
		  		System.out.print("-");
		  	System.out.print("^"); 
	}
	  
	  public  static void printRelease () {
		  System.out.println("Release "+release+" of "+releaseDate);
	  }

		public static void printMemory () {
			  printSize();
		      //System.out.println("======================");
		      System.out.println("++ STACK");
		      System.out.print("---------------");
		      int iStack = 0;
		      for ( int i=0; i < SymbH.nVar(); i++ ) {
		    	  System.out.println(" [GV "+i+"]: \""+SymbolTableH.idGV(i)+"\"");
		    	  System.out.format("%05d",iStack);
		    	  System.out.print(": ");
		    	  Debug.printValue(Exec.valGV(i), Exec.typeGV(i));
		    	  iStack++;
		    	  System.out.format("%05d: ",iStack);
		    	  System.out.format("%-6d",Exec.typeGV(i));
		    	  System.out.println("\t"+Debug.printType(Exec.typeGV(i))
		    			  );
		    	  iStack++;
			      System.out.print("---------------");		    	  
		      }
		      System.out.println();
		      System.out.println("======================");
		      Debug.printHeap();
		}

		public static void printSize () {
		      System.out.println("===========================");
		      System.out.println("- Size of MEM = "+Exec.getMS());
		      System.out.println("- Size of STACK = "+(Exec.getSP()+1));
		      System.out.println("- Number of GVs = "+SymbH.nVar());
		      System.out.println("- Size of HEAP = "+(Exec.getMS()-Exec.getHP()-1));
		      System.out.println("- Size of used MEM = "+(Exec.getMS()-Exec.getHP()+Exec.getSP()));
		      System.out.println("- Size of free MEM = "+(Exec.getHP()-Exec.getSP()));
		      System.out.println("- Size of CODE = "+Exec.getCS());
		      System.out.println("- Size of OUTPUT = "+Exec.getOS());
		      System.out.println("===========================");
		}

	  static void error( Exc e ) {
		  	System.out.println("\n" + e.print());
		  	System.out.println("-- skipping input until ';'");
	  }

	  static void errorNoSkip( Exc e ) {
		  	System.out.println("\n" + e.print());
	  }

	  static void error( Exc e, String s ) {
		  	System.out.println("\n" + e.print());
		  	System.out.println("-- please report possible CalcuList bug");
	  }

	  static void print( String s ) { 	
		  System.out.print(s);
	  }

	 static public void setPrompt( boolean p )  {
		 isPrompt=p;
	 }
	 
	 static void prompt() {
		if ( isPrompt )
			System.out.print(prompt);
		else
			System.out.print(promptF);
		isPrompt=false;
	  }
	  
	  static void bye() {
	   	System.out.println("\n" + "Bye");
	  }
	  	  
	  	static void printVarVal ( int ivar ) throws Exc {
			int tSymb = Exec.typeGV(ivar);
			double val = Exec.valGV(ivar);
			System.out.print(": "+Exec.types[tSymb]);
			if ( tSymb != Exec.NullT ) {
				if ( tSymb !=Exec.StringT && tSymb !=Exec.ListT && tSymb !=Exec.JsonT) {
					System.out.print(" = ");
					PrintOutput.printVarVal(tSymb,val);
				}
				else {
					System.out.print(" = ");
					System.out.print((int)val+" (->Heap)");	
				}
			}
	  	}
	  	
		public static void printVars() throws Exc{
			System.out.print("\n------GLOBAL VARIABLES------\n");
			for ( int i =0; i < SymbolTableH.nSymb(); i++)
				if ( SymbolTableH.tSymb(i) == SymbH.varType ) {
					int iVar = SymbolTableH.iVarFunc(i);
					System.out.print("(GV"+(iVar)+")@["+Exec.addrGV(iVar)+"] ");
					System.out.print(SymbolTableH.idSymb(i));
					printVarVal(iVar);
					System.out.println("");
				}
			System.out.print("---End of GLOBAL VARIABLES---\n \n");					
		}

		public static void printOneLabel(String label) throws Exc {
			System.out.print("\n------VARIABLES/FUNCTIONS WITH LABEL "+label+"-------\n");
			int iL = SymbolTableH.indLabel(label);
			if ( SymbolTableH.labelComment(iL) != null )
				System.out.println("/*"+SymbolTableH.labelComment(iL)+"*/");		
			int iLabel = 1; int nL=label.length();
			for ( int i =0; i < SymbolTableH.nSymb(); i++)
				if ( SymbolTableH.hasLabelSymb(i) && SymbolTableH.labelSymb(i).equals(label) ) {
					int iVarFunct = SymbolTableH.iVarFunc(i);
					if ( SymbolTableH.tSymb(i)== SymbH.varType) {
						System.out.print(iLabel+": (GV"+(iVarFunct)+")@["+Exec.addrGV(iVarFunct)+"] ");
						System.out.print("\t"+SymbolTableH.idSymb(i).substring(nL+1));
						printVarVal(iVarFunct);
						System.out.println("");
					}
					else
					{
						System.out.print(iLabel+": (F"+(iVarFunct)+") ");
						String source = SymbolTableH.finfo_source(i);
						if ( Pattern.matches(labelP,source) )
							System.out.print("\t"+source.substring(nL+1));
						else
							System.out.print("\t"+source);							
						System.out.println("");						
					}
					iLabel++;
				}
			System.out.print("---End of VARIABLES/FUNCTIONS WITH LABEL "+label+"---\n \n");					
		}


		public static void printLabels( boolean setLabel, String setLabelID ) {
			System.out.print("\n------LABELS-------\n");
			int iLabel =1;
			for ( int i =0; i < SymbolTableH.nLabels(); i++) {
					System.out.print("(L"+iLabel+") "+SymbolTableH.label(i));
					System.out.println("");
					iLabel++; 
				}
			System.out.println("---End of LABELS---\n");	
			if ( setLabel )
				System.out.print("*  CURRENT LABEL: "+setLabelID+"\n \n");
			else
				System.out.print("*  NO CURRENT LABEL\n \n");
		}

		public static void printFuns( int nUtil) {
			System.out.print("\n------FUNCTIONS------\n");
			System.out.println("Built-in: _exp ( P1 )");
			System.out.println("Built-in: _ind ( P1, P2 )");
			System.out.println("Built-in: _ind ( P1, P2, P3 )");
			System.out.println("Built-in: _isKey ( P1, P2 )");
			System.out.println("Built-in: _len ( P1 )");
			System.out.println("Built-in: _log ( P1 )");
			System.out.println("Built-in: _pow ( P1, P2 )");
			System.out.println("Built-in: _rand ( )");
			System.out.println("Built-in: _tuple ( P1 )");
			System.out.println("Built-in: _gDate ( )");
			System.out.println("Built-in: _pDate ( P1 )");
			System.out.println("Built-in: _pDate ( P1, P2 )");
			char UorF ='U';
			for ( int i =0; i < SymbolTableH.nSymb(); i++)
				if ( SymbolTableH.tSymb(i) == SymbH.functType ) {
					int iFunct = SymbolTableH.iVarFunc(i);
					if ( iFunct >= nUtil )
						UorF='F';
					System.out.print("("+UorF+(SymbolTableH.iVarFunc(i)+1)+") ");
					System.out.print(SymbolTableH.idSymb(i));
					if ( SymbH.hasSideEffect(i) )
						System.out.print('*');
					System.out.print(" ( ");
					for (int j =0; j<SymbH.nPar(i); j++ ) {
						if ( j > 0 )
							System.out.print(", ");
						if (SymbolTableH.finfo_typePar(i,j) == SymbH.functType ) {
							System.out.print("P"+(j+1));
							System.out.print("/"+SymbolTableH.finfo_functParNP(i,j));
						}
						else 
							System.out.print("P"+(j+1));
					}
					System.out.print(" ) ");
					if (SymbolTableH.finfo_retType(i) == SymbH.functType )
						System.out.print("/"+SymbolTableH.finfo_retArity(i));
					System.out.println("");			
				}
			System.out.print("---End of FUNCTIONS---\n \n");							
		}
		
		public static void printHist ( int iH, boolean index  ) {
			if ( index )
				System.out.print("\n----FULL HISTORY------\n");
			else
				System.out.print("\n------HISTORY FROM "+(iH+1)+" ON ------\n");
			for ( int i=iH; i < History.nCommand(); i++ ) {
				if ( index )
					System.out.print((i+1)+": ");
				else
					System.out.print(""+(i+1)+": ");
				System.out.println(History.command(i));
			}
			System.out.print("---End of HISTORY---\n \n");
		}
	  
		public static void printDebug ( boolean isOn ) {
			if ( isOn )
				System.out.println("\n------DEBUG ON------\n");
			else
				System.out.println("\n------DEBUG OFF------\n");			
		}
		
		public static void printTailOpt ( boolean isOn ) {
			if ( isOn )
				System.out.println("\n------Tail Recursion Optimization ON------\n");
			else
				System.out.println("\n------Tail Recursion Optimization OFF------\n");			
		}
		
		public static void printOneBuiltIn (BIFType builtInF) {
			if ( builtInF ==  BIFType.RAND ) {
				System.out.print("_rand() ");
				System.out.print("/* generate a random double ");
				System.out.println("in the range <0.0, 1.0> */");
				return;
			}
			if ( builtInF ==  BIFType.EXP ) {
				System.out.print("_exp(x) ");
				System.out.print("/* computes the natural exponential function e^x, ");
				System.out.println("where e is the Euler's number and x is a number */");
				return;
			}
			if ( builtInF ==  BIFType.ISKEY ) {
				System.out.print("_isKey(x,y) ");
				System.out.println("/* return true if the string y is a key of the json x  */");
				return;
			}
			if ( builtInF ==  BIFType.LOG ) {
				System.out.print("_log(x) ");
				System.out.println("/* computes the natural logarithm of a non-negative number x */");
				return;
			}
			if ( builtInF ==  BIFType.POW ) {
				System.out.print("_pow(x,y) ");
				System.out.println("/* computes the value of the number x raised to the power of the number y */");
				return;
			}
			if ( builtInF ==  BIFType.LEN ) {
				System.out.print("_len(S) ");
				System.out.print("/* computes the length of S, ");
				System.out.println("where S is a list or a string or a json */");
				return;
			}
			if ( builtInF ==  BIFType.TUPLE ) {
				System.out.print("_tuple(J) ");
				System.out.println("/* if J is a json it returns the tuple of the json values or otherwise, ");
				System.out.print("if J is a long, it returns the tuple of the values ");
				System.out.println("of the date corresponding to J (Y/M/D/h/m/s/ms/dW/dY) */");
				return;
			}
			if ( builtInF ==  BIFType.GDATE ) {
				System.out.print("_gDate() ");
				System.out.println("/* returns a long value representing the current date */\n");
				System.out.print("_gDate(D,F) ");
				System.out.print("/* given two strings D and F, it returns a long value \nrepresenting the date D ");
				System.out.println("written in the format F */");
				return;
			}
			if ( builtInF ==  BIFType.PDATE ) {
				System.out.print("_pDate(d) ");
				System.out.print("/* given a long d, it returns the date corresponding to d, \nrepresented as a string ");
				System.out.println("written in the standard format */\n");
				System.out.print("_pDate(d,F) ");
				System.out.print("/* given a long d and a string F, it returns the date corresponding to d, \nrepresented as a string ");
				System.out.println("written in the format F */");
				return;
			}
			System.out.print("_ind(x,y) ");
			System.out.println("/* returns the index within the string x of the first occurrence of the string y");
			System.out.println("or -1 if y does not occur in x */\n");
			System.out.print("_ind(x,y,z) ");
			System.out.println("/* returns the index within the string x of the first occurrence of the string y, starting at the index z");
			System.out.println("or -1 if y does not occur in x, starting at the index z */");
			return;			
		}
		public static void printOneFunct (String idAbout, int nUtil ) {
			int i =SymbH.iSymb(idAbout); 
			int iFunct = SymbolTableH.iVarFunc(i);
			if ( iFunct < nUtil )
				System.out.print("(U"+(iFunct+1)+") ");
			else
				System.out.print("(F"+(iFunct+1)+") ");
			System.out.println(SymbolTableH.finfo_source(i));
			if ( SymbolTableH.finfo_comment(i) != null )
				System.out.println("/*"+SymbolTableH.finfo_comment(i)+"*/");
		}
		public static void printOneVar (String idAbout) throws Exc{
			int i =SymbH.iSymb(idAbout);
			int iVar = SymbolTableH.iVarFunc(i);
			int addr = Exec.addrGV(iVar);
			System.out.print("(GV"+(iVar)+")@["+addr+"] ");
			System.out.print(idAbout);
			printVarVal(iVar);
			int tSymb = Exec.typeGV(iVar);
			double val = Exec.valGV(iVar);
			if ( tSymb == Exec.ListT ) {
				System.out.print("\n[ ");
				PrintOutput.printQ1VarVal(Exec.ListT,val);
				System.out.println();
				return;
			}
			if ( tSymb == Exec.JsonT ) {
				System.out.print("\n{ ");;
				PrintOutput.printQ1VarVal(Exec.JsonT,val);
				System.out.println();
				return;
			}
			if ( tSymb == Exec.StringT ) {
				System.out.println();
				PrintOutput.printQuotedString((int)val);
				System.out.println();
				return;
			}
			System.out.println();
		}
		public static void printServiceList(boolean isDefault ) throws Exc{
			if (! isDefault ) {
				System.out.println("\n------TABLE OF SYMBOLS------");
				for (int i=0; i<SymbolTableH.nSymb(); i++) {
					System.out.print(" ("+i+")");
					System.out.print("\t idN="+SymbolTableH.idSymb(i));
					System.out.print("\t type="+SymbolTableH.tSymb(i));
					System.out.print("\t iVarFunct="+SymbolTableH.iVarFunc(i));
					System.out.print("\t label="+SymbolTableH.labelSymb(i));
					System.out.println();
				}
				System.out.println("\n---END OF TABLE OF SYMBOLS---\n \n");
			}
			else {
				System.out.println("\n------LIST OF SERVICE COMMANDS------");
				System.out.println(" - !variables: lists all global variables (including the labeled ones) and their values");
				System.out.println(" - !functions: lists all function names and their arguments");
				System.out.println(" - !labels: lists all labels that have been defined in the current session");
				System.out.println(" - !about ID: describes some info on the variable, function or label with name ID");
				System.out.println(" - !history k: lists all statements (definitions and queries) of the session, ordered from 1 to n, corresponding to the last command\n"+
						"               -  if k > 0, lists the statements from k to n\n" +
						"               -  if k < 0, lists the statements from n-k to n\n" +
						"               -  if k = 0, lists the last statement only\n" +
						"               -  if k is missing, lists all statements");			
				System.out.println(" - !exec k: executes the statement with index p, where \n"+
						"               -  p = k if k > 0\n" +
						"               -  p = n-k if k < 0\n" +
						"               -  p = n if k = 0 or k is missing");			
				System.out.println(" - !import echo/n0echo: imports the statements stored into a file \n"+
						"               -  the name of the file is to be given after the prompt\n"+
						"               -  option 'echo':  the imported statements are displayed on the screen\n"+
						"               -  option 'noecho':  no display\n"+
						"               -  no option:  the option 'echo' is assumed");
				System.out.println(" - !save: all session statements are saved into a text file \n"+
						"               -  the name of the file is to be given after the prompt");
				System.out.println(" - !clops: displays the number of clops (micro-instructions of CalcuList Machine) for the last query or variable definition");
				System.out.println(" - !release: displays details on the current CalcuList release");
				System.out.println(" - !memory: displays the internal memory, i.e., the global variables in the stack and dynamic structures in the heap");
				System.out.println(" - !tailOpt on/off: optimization of tail recursion implementation will be enabled or disabled\n"+
						"               -  by default the optimization is enabled");
				System.out.println(" - !debug on/off: debug will be enabled or disabled\n"+
						"               -  by default the debug is disabled");
				System.out.println(" * each of the above commands can be abbreviated by any non-empty prefix of it");
				System.out.print("---End of LIST OF SERVICE COMMANDS---\n \n");
			}
		}
}
