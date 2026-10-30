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

package synt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Vector;

import ioHandler.*;
import synt.Lex.*;
import error.Exc;
import error.Exc_Sem;
import error.Exc_Synt;
import error.Exc_Exec;
import error.Exc.ErrorType;
import exec.Exec;
import exec.Instruction;
import exec.PrintOutput;
import exec.Instruction.Operator;
import sem.Transl;
import sem.FunctInfo;
import sem.Linker;
import sem.SymbH;
import sem.PrintCode;


public class Parser {

	 static private Lex lex;
	 static private IOH ioh;
	 
	 
	// FIRST
	 static Token[] first_program = { // FIRST(<statement>) U { SCOLON , HALT }
		 	Token.ID,
		 	Token.IDL,
		 	Token.PRINT,
		 	Token.EMARK,
		 	Token.SCOLON, 
		 	Token.COLON,
		 	Token.HALT
		 };
	static Token[] first_statement = { 
		 	Token.ID,
		 	Token.IDL,
		 	Token.PRINT,
		 	Token.EMARK,
		 	Token.COLON
		 };
	static Token[] first_varDef = { 
			Token.OSPAR,
			Token.ASSIGN,
			Token.OPASS
		 };
	static Token[] first_varGlobSet = first_varDef;


	static Token[] first_varFunctLabDef = { 
		Token.OSPAR,
		Token.ASSIGN,
		Token.OPASS,
		Token.OPAR, 
		Token.STAR,
		Token.COLON
	 };

	static Token[] first_ifExp = { 
			Token.EMARK,
			Token.OBRACE,
			Token.PLUS,
			Token.MINUS,
			Token.ID,
		 	Token.IDL,
			Token.OSPAR,
			Token.DOUBLE,
			Token.INT,
			Token.LONG,
			Token.STRING,
			Token.CHAR,
			Token.OPAR,
			Token.BOOL,
			Token.NULL,
			Token.BIFUNC,
			Token.LAMBDA,
			Token.TYPE
		 };
	static Token [] first_eqExp = first_ifExp;

	static Token[] first_term = first_ifExp;
	
	static Token[] first_fact = { 
		Token.OBRACE,
		Token.ID,
	 	Token.IDL,
		Token.OSPAR,
		Token.DOUBLE,
		Token.INT,
		Token.LONG,
		Token.STRING,
		Token.CHAR,
		Token.OPAR,
		Token.BOOL,
		Token.NULL,
		Token.BIFUNC,
		Token.LAMBDA,
		Token.TYPE
	 };
	
	static Token[] first_simpleTerm = first_fact;

	static Token[] first_orExp = first_ifExp;
	static Token[] follows_orExp_next = {
		Token.QMARK, 
		Token.COLON, 
		Token.COMMA, 
		Token.SCOLON, 
		Token.PRINTOPT, 
		Token.CONTINUE, 
		Token.CPAR, 
		Token.CSPAR, 
		Token.CBRACE, 
		Token.PIPE,
		Token.OBRACESET,
		Token.CBRACESET,
	};
	static Token[] follows_andExp_next = {
		Token.OR,
		Token.QMARK, 
		Token.COLON, 
		Token.COMMA, 
		Token.SCOLON, 
		Token.PRINTOPT, 
		Token.CONTINUE, 
		Token.CPAR, 
		Token.CSPAR, 
		Token.CBRACE, 
		Token.PIPE, 
		Token.OBRACESET,
		Token.CBRACESET,
	};
	static Token [] first_andExp = first_ifExp;
	static Token [] first_constListElems = first_ifExp;
	static Token[] first_callFunct = { //FIRST(<ifExp>) U {  LAMBDA }
		Token.LAMBDA,
		Token.EMARK,
		Token.OBRACE,
		Token.PLUS,
		Token.MINUS,
		Token.ID,
	 	Token.IDL,
		Token.OSPAR,
		Token.DOUBLE,
		Token.INT,
		Token.LONG,
		Token.STRING,
		Token.CHAR,
		Token.OPAR,
		Token.BOOL,
		Token.NULL,
		Token.BIFUNC,
		Token.TYPE
	};
	static Token[] first_fields = first_ifExp; 

	static Token[] first_listElems = { //FIRST(<ifExp>) U {  DOT, GTLT(GT) , COLON }
			Token.OBRACE,
			Token.CSPAR,
			Token.GTLT,
			Token.COLON,
			Token.EMARK,
			Token.PLUS,
			Token.MINUS,
			Token.ID,
		 	Token.IDL,
			Token.OSPAR,
			Token.DOUBLE,
			Token.INT,
			Token.LONG,
			Token.STRING,
			Token.CHAR,
			Token.OPAR,
			Token.BOOL,
			Token.NULL,
			Token.BIFUNC,
			Token.TYPE, 
			Token.DOT
		 };

	static Token[] first_factFI = { 
		Token.OBRACE,
		Token.OSPAR,
		Token.PLUS,
		Token.MINUS,
		Token.DOUBLE,
		Token.INT,
		Token.LONG,
		Token.STRING,
		Token.CHAR,
		Token.BOOL,
		Token.NULL,
		Token.TYPE
	 };
	static Token[] first_set = { 
			Token.OBRACESET,
	};
	static Token[] first_prSet = { 
			Token.ID,
			Token.IDL,
			Token.EXC,
			Token.OSPAR
	};
	static Token[] first_ioSet = { 
			Token.PRINT,
			Token.GTLT
	};
	static Token[] first_pr_ioSet = { 
			Token.ID,
			Token.IDL,
			Token.EXC,
			Token.OSPAR,
			Token.PRINT,
			Token.GTLT			
	};
	 static int typeDef; // it will take one of the following values
	 static int isFunctDef = 0;
	 static int isVarDef = 1;
	 static int isQuery = 2;
	 static int isService = 3;
	 static int isLabDef = 4;
	 static int isSetLab = 5;
	 
	 static int typeService; // it will take one of the following values
	 static final int isVarService = 0;
	 static final int isFunctService = 1;
	 static final int isDebugOnService = 2;
	 static final int isDebugOffService = 3;
	 static final int isHistoryService = 4;
	 static final int isHistoryNumService = 5;
	 static final int isAboutService = 6;
	 static final int isLabelService = 7;
	 static final int isImportEchoService = 8;
	 static final int isImportNoEchoService = 9;
	 static final int isClops = 10;
	 static final int isExecService = 11;
	 static final int isReleaseService = 12;
	 static final int isSaveService = 13;
	 static final int isMemoryService = 14;
	 // static final int isMemorySizeService = 15;
	 static final int isTailOptOnService = 15;
	 static final int isTailOptOffService = 16;
	 static final int isServiceList = 17;
	 static final int isLambdaList = 18;
	 
	 static int typeAbout; // associated to isAboutService - it will take one of the following values
	 static final int isAboutBuiltIn = 0;
	 static final int isAboutLABEL = 1;
	 static final int isAboutVAR = 2;
	 static final int isAboutFUNCT = 3;

	 static int typeServiceList; // associated to isAboutService - it will take one of the following values
	 static final int isDefaultList = 0;
	 static final int isSymbolTable = 1;

	 static String idAbout; // associated to isAboutLABEL,isAboutVAR, isAboutFUNCT

	 static int iStartLambda; // history index of the start of a lambda definition
	 static int numHistory; // associated to isHistoryNumService

	 static int numMemoryService; // associated to isMemoryService

	 static int nLocVars=0; // used within funcDef
	 
	 static boolean setLabel=false; // true if a label has been set as default for variables/functions
	 static String setLabelID = null; // default label
	 static boolean setLabel_tmp=false; 
	 static String setLabelID_tmp = null; 
 
	 static boolean isDebug = false; 
	 static boolean isTailOpt = true;
	 
//	 static boolean cannotBeJson;
	 
	 static BIFType builtInF;
	 
	 static boolean isNewVarDef; // true if the current statement is the definition of a new global variable

 	 static Vector<String> excludedKeys; // keys to be excluded in <in>
 	 static int initSizeEK = 10, incrSizeEK=5;
 	 static BufferedWriter outputExecF;
 	 static FunctCalled currFunctCalled;
	 
	 // <CalcuListSession> -> { [ <statement> ] SCOLON } HALT
	 public static void CalcuListSession (  ) {
		 boolean fatalError = false;
		 try {
			 setAnsGV();
			 ioh = new IOH();
			 lex = new Lex (ioh);
			 lex.nextToken();
			}
		 catch ( Exc e ) {
			 if ( e.errorType() == ErrorType.FATAL_ERROR)
				 fatalError = true;
			 else
		     	 skipStatement(e,true);
		 }
		 boolean execNewVarDef=false; // true if the definition of a new global variable is under execution
		   							  //  to eventually remove the GV if the execution fails
		 boolean execDone=false; // true if the definition of a new global variable or label or computation is under execution
		 boolean translDone=false; // true if the definition of a new global variable or label or computation is under linkage
		 
	 	 while ( lex.currToken() != Token.HALT && !fatalError ) {
		 		if ( inFirst(first_statement)  ) {
				 	try {
				 		execNewVarDef = false; 
				 		execDone=translDone=false;
						lex.setTruncatableString(true); // by default string are truncatable
		 				statement();
		 				if ( lex.currToken() == Token.SCOLON ) {
		 					History.endCommand();
		 					if ( isDebug && typeDef != isService && typeDef != isSetLab) 
		 						PrintCode.printListing(Transl.end());
				 			if ( typeDef == isVarDef || typeDef == isFunctDef ) 
				 				SymbH.commitVarFunc(History.command(History.nCommand()-1));
				 			else 
				 				if ( typeDef == isLabDef  ) 
				 					SymbH.commitLabDef();
					 			else
					 				if ( typeDef == isService )
					 					serviceCommandExec();
				 			if (typeDef == isVarDef || typeDef == isLabDef || typeDef == isQuery ) {
				 				translDone=true;
				 				int nUnitCode = Transl.end().numInstr();
				 				int nExecCode = Linker.link(Transl.end());
				 				execDone=true;
				 				if ( typeDef == isVarDef && isNewVarDef )
				 					execNewVarDef = true;
				 				if (isDebug)
				 					PrintCode.printExecCode (nExecCode,nUnitCode);
				 				Exec.exec(isDebug,nExecCode);
				 			}
				 			if ( typeDef == isQuery ) 
				 				PrintOutput.printOutput();
				 			else
					 			if ( typeDef == isVarDef ) {
					 				PrintOutput.setStandardOutStream();
					 				PrintOutput.printOutput();
					 			}
					 			else 
					 				if ( typeDef == isSetLab ) {
					 					setLabel=setLabel_tmp;
					 					setLabelID=setLabelID_tmp;
					 				}
		 				}
		 				ioh.setPrompt(true);
			 			lex.accept(Token.SCOLON);
		 			}
				 	catch ( Exc e ) {
						 if ( e.errorType() == ErrorType.FATAL_ERROR) {
							 fatalError = true;
							 ioh.error(e,"Fatal Error - check launching parameters of CalcuList");
						 }
						 else
							 if ( e.errorType() == ErrorType.WRONG_PRINT_FORMAT) {
								 fatalError = true;
								 ioh.error(e, " Unexpected Error - Abnormal End");
							 }
							 else {
								 try {
									 if (execDone && e.errorType() != ErrorType.LIST_DELETED
											      && e.errorType() != ErrorType.JSON_DELETED)
										 // run time error: the OUTPUT is flushed out unless there are deleted lists 
										 PrintOutput.printOutput();
								 }
								 catch( Exc et ) {
									 
								 }
								 ioh.setImportError();
					 			 if ( execNewVarDef )
					 				 try {
					 					 SymbH.checkGV();
					 				 }
					 			 catch ( Exc e1 ) {
									 fatalError = true;
									 ioh.error(e1, " Unexpected Error - Abnormal End");					 				 
					 			 }
					 			 ioh.setPrompt(true);
						     	 skipStatement(e,!translDone);
					 			 History.histOff();
							 }
			     	}	
		 		}
		 		else {	 			
		 			try {
		 				ioh.setPrompt(true);
		 				if ( lex.currToken() != Token.SCOLON  ) {
		 				    ioh.setImportError();
			 				skipStatement(raiseExc(first_statement),false);
		 				}
		 				lex.accept(Token.SCOLON); 
		 				
		 			}
			 		catch ( Exc e ) {
						 if ( e.errorType() == ErrorType.FATAL_ERROR)
							 fatalError = true;
						 else
					     	 skipStatement(e,true);
			 		}
		 		}
		 } ;
		if ( !fatalError)
			ioh.checkHalt();
		ioh.bye();	 
	 }
	 
	 // <statement> ->  COLON [ ID | HASH ] |
	 //					IDL  <varFuncDef> | ID <varFuncLabDef> | 
	 //					PRINT <query> |  EMARK <serviceCommand>
	 static void statement ( ) throws Exc {
		 if ( lex.currToken() == Token.COLON ) {
			 typeDef= isSetLab;
			 History.startCommand(); 
			 lex.printH_Colon(true); // print spaced COLON on history
			 lex.nextToken();
			 if ( lex.currToken() == Token.HASH ) {
				 setLabel_tmp=false; setLabelID_tmp=null;
				 lex.nextToken();
			 }
			 else
				 if ( lex.currToken() == Token.ID ) {
					 String idN = lex.IDName();
					 if (SymbH.isLabel(idN)) {
						 setLabel_tmp=true; setLabelID_tmp=idN;
						 lex.nextToken();
					 }
					 else
		 				 throw new Exc_Synt(ErrorType.UNDEF_LABEL, ": "+idN );				 						 
				 }
				 else
	 				 throw new Exc_Synt(ErrorType.EXPECTED_ID_OR_HASH,
	 						 " - found "+lex.tokenFullDescr() );				 
			 return;
		 }
		 
		 if ( lex.currToken() == Token.IDL ) {
		 	 Transl.start(); 
			 String idN = lex.IDName();
			 boolean hasLabel = true; String label = lex.currLabel();
			 if (!SymbH.isLabel(label)) 
 				 throw new Exc_Synt(ErrorType.UNDEF_LABEL, ": "+label );				 						 
		 	 History.startCommand(); History.addSubStr(idN);
			 lex.nextToken();
			 varFuncDef(idN,hasLabel,label);
 			 return;
		 }
		 if ( lex.currToken() == Token.ID ) {
			 	 Transl.start(); 
				 String idN = lex.IDName();
			 	 History.startCommand(); History.addSubStr(idN);
				 lex.nextToken();
				 varFuncLabDef(idN);
			 return;
		 }
		 if ( lex.currToken() == Token.PRINT ) {
			 History.startCommand(); 
			 History.addSubStr("^ ");
			 lex.nextToken();
			 query();
			 return;
		 }
		 if ( lex.currToken() == Token.EMARK ) {
			 History.startCommand(); 
			 History.addSubStr("! ");
			 lex.nextToken();
			 serviceCommand();
			 History.histOff(); // service command not kept in the history
		 }
		 else
			 throw raiseExc(first_statement);
	 }
	 
	 // <varFuncLabDef> -> <varFuncDef> | COLON <labDef>  
	 static void varFuncLabDef ( String idN ) throws Exc {
		 if (lex.currToken() == Token.COLON ) {
 			 lex.setComment(true); 
			 lex.printH_Colon(true); // print spaced COLON on history
 			 lex.nextToken();
 			 labDef(idN);
		 }
		 else {
				boolean hasLabel=false; String label="";
			 	varFuncDef(idN,hasLabel,label);
		}
	 }
	 // <varFuncDef> -> <varDef> | STAR <funcDef(SE)> |  <funcDef(noSE)>   
	 static void varFuncDef ( String idN, boolean hasLabel, String label ) throws Exc {
		 if ( setLabel && !hasLabel ) { // the usage of implicit label is set
			 String idN_full = setLabelID+"."+idN;
			 if ( SymbH.iSymb(idN_full)>=0 ) { // the labeled ID exists
				 History.replaceLast(idN, idN_full);
				 idN=idN_full; hasLabel=true; label= setLabelID;
			 }			 			
		 };
		 if ( inFirst(first_varDef)  ) {
			typeDef=isVarDef;
			SymbH.startVarDef(idN,hasLabel,label);
 			varDef();
		 }
		 else {
			 boolean se = false;
			 if (lex.currToken() == Token.STAR ) { 
				 se=true;
				 lex.nextToken();
			 }
			 if (lex.currToken() == Token.OPAR ) {
				 typeDef=isFunctDef;
				 SymbH.startFunctDef(idN,hasLabel,label); 
				 funcDef(idN,se,hasLabel,label);
			 }
			 else
	 			 if( lex.currToken() == Token.SCOLON )
	 					throw new Exc_Synt(ErrorType.MISSING_PRINT);
	 				 else
	 					 throw raiseExc(first_varFunctLabDef);
		 }	 
	 }
	 
	 // <labDef> -> [COMMENT] ID [ <labFuncDef> ]  { COMMA ID [ <labFuncDef> ]  }
	 static void labDef ( String idNL ) throws Exc {
 			 Transl.ins(Operator.INIT,SymbH.nVar());
 			 typeDef=isLabDef;
 			 SymbH.startLabelDef(idNL);
 			 String idN;
 			 if ( lex.currToken()==Token.COMMENT ) {
 				 SymbH.addLabelComment(lex.commentString());
 				 if ( lex.isCommentOverflow() )
 					 ioh.print("\n**Warning: comment string truncated to max length "+Lex.maxLenComment+"\n");
 				 lex.nextToken();
 			 }
 			 lex.setComment(false); 
 			 if ( lex.currToken() == Token.ID ) 
 				 idN = lex.IDName();
 			 else
 				 throw new Exc_Synt(ErrorType.EXPECTED_ID,
 						 " - found "+lex.tokenFullDescr() );
 			 lex.nextToken( );
 			 if (lex.currToken() == Token.STAR || lex.currToken() == Token.DIV
 					|| lex.currToken() == Token.OPAR) 
 				 labFuncDef(idN);
 			 else
 				 labVar(idN);
 			 while (lex.currToken() == Token.COMMA) {
				 lex.printH_Comma(true); // print space after comma in history
 				 lex.nextToken(); 
 	 			 if ( lex.currToken() == Token.ID ) 
 	 				 idN = lex.IDName();
 	 			 else
 	 				 throw new Exc_Synt(ErrorType.EXPECTED_ID,
 	 						 " - found "+lex.tokenFullDescr() );		 
 	 			 lex.nextToken( );
 	 			 if (lex.currToken() == Token.STAR || lex.currToken() == Token.DIV
 	 					|| lex.currToken() == Token.OPAR ) 
 	 				 labFuncDef(idN);
 	 			 else
 	 				 labVar(idN);
 			 };
 		 	 Transl.ins(Operator.HALT,0);
 			 return;
	 }
	 
	 // <labFuncDef> ->  [STAR] ( DIV INT | 
	 //                          OPAR [ UNDERSCORE [ DIV INT] { COMMA UNDERSCORE [ DIV INT]} ] } CPAR
	 //                          [ DIV INT] )
	 static void labFuncDef ( String idN ) throws Exc {
		 boolean se = false;
			 if ( lex.currToken() == Token.STAR) {
				 se=true;
				 lex.nextToken();
			 }
			 SymbH.startAddLabFunct(idN,se);
			 if ( lex.currToken() == Token.DIV) {
				 lex.nextToken();
				 if ( lex.currToken()== Token.INT ) {
					 int arity = lex.intVal();
					 lex.nextToken();
					 for (int i=0; i<arity;i++)
						 SymbH.addVarParLabFunct();
		//			 SymbH.addLabFunct(idN,se,arity);
				 }
				 else
						throw new Exc_Sem(ErrorType.WRONG_FUNCT_DEF, 
								"-- expected INT for labeled function arity - found " + lex.tokenFullDescr());
			 }
			 else
				 if ( lex.currToken() == Token.OPAR ) {
					 lex.nextToken();
					 if ( lex.currToken()==Token.UNDERSCORE || 
							 lex.currToken()==Token.ID ) {
						 lex.nextToken();
						 parLabFunc();
					 }
					 while ( lex.currToken() == Token.COMMA ) {
						 lex.printH_Comma(true); // print space after comma in history
						 lex.nextToken();
						 if ( lex.currToken()==Token.UNDERSCORE || 
								 lex.currToken()==Token.ID ) {
							 lex.nextToken();
							 parLabFunc();
						 }
						 else
								throw new Exc_Sem(ErrorType.WRONG_FUNCT_DEF, 
										"-- expected '_' or ID for labeled function parameter - found " + lex.tokenFullDescr());
					 }					 
					 lex.accept(Token.CPAR);			 
					 if ( lex.currToken() == Token.DIV) {
						 lex.nextToken();
						 if ( lex.currToken()== Token.INT ) {
							 int arity = lex.intVal();
							 lex.nextToken();
							 SymbH.addFuncRetLabFunct(arity);
						 }
						 else
								throw new Exc_Sem(ErrorType.WRONG_FUNCT_DEF, 
										"-- expected INT for labeled returned function arity - found " + lex.tokenFullDescr());
					 }	
				 }
				 else
						throw new Exc_Sem(ErrorType.WRONG_FUNCT_DEF, 
								"-- expected INT or '(' for labeled function definition - found " + lex.tokenFullDescr());			 
			 SymbH.closeLabFunctDef(idN,se);
	 }
	 
	 // method called by <labFuncDef>
	 static void parLabFunc (  ) throws Exc {
		 if ( lex.currToken()==Token.DIV ) {
			 lex.nextToken();
			 if ( lex.currToken()== Token.INT ) {
				 int arity = lex.intVal();
				 lex.nextToken();
				 SymbH.addFuncParLabFunct(arity);
			 }
			 else
					throw new Exc_Sem(ErrorType.WRONG_FUNCT_DEF, 
							"-- expected INT for labeled parameter function arity - found " + lex.tokenFullDescr());
			 
		 }
		 else
			 SymbH.addVarParLabFunct();
	 }

	 // method called by <labDef>
	 static void labVar ( String idN ) throws Exc {
		 int indLabVar = SymbH.addLabVar(idN); 
		 if ( indLabVar < 0 ) {
			 Transl.ins(Operator.PUSHN,0); // default null value for new labeled variables
		 }
	 }
	 
	 // <query> -> [ <out> ] <ifExpr(SE)> [PRINTOPT] 
	 //			   { CONTINUE  <ifExpr(SE)> [PRINTOPT] }
	 static void query ( ) throws Exc {
	 	    Transl.start();
	 		SymbH.startComp();
	 		Transl.ins(Operator.INIT,SymbH.nVar());
	 		boolean startPrint = true;
			typeDef=isQuery;
			if (lex.currToken()==Token.OUT ) 
				out();
			else
				PrintOutput.setStandardOutStream();
	 		do {
			 	if ( ! startPrint ) 
			 		lex.nextToken ();
			 	SymbH.startVarDef("ans",false,"");			 		
				Transl.ins(Operator.LOADGV,SymbH.iVar()); 
			 	int	retType=ifExp(true); // side effect is enabled
//			 	if ( isFunction(retType) ) 
//			 		Transl.ins(Operator.OUTF,0);
		 		Transl.ins(Operator.DUPL,0); 
		 		if ( lex.currToken() == Token.PRINTOPT) {
					Transl.ins(Operator.PRINT,lex.indPrint());
					lex.nextToken();
		 		}
		 		else
		 			Transl.ins(Operator.PRINT,0);
	 			if ( isLabel(retType) || isFunction(retType) ) { 
	 				Transl.ins(Operator.POP,0);
	 				Transl.ins(Operator.PUSHN,0);
	 			}
			 	Transl.ins(Operator.MODV,0); 
	 			startPrint = false; /* */
	 		} while ( lex.currToken() == Token.CONTINUE );
	 		Transl.ins(Operator.HALT,0);
	 		return;		 
	 }

	 // checkString: called by <out> and <in>
	 static void checkString() throws Exc {
			if (lex.currToken() == Token.STRING) 
				if ( lex.stringVal() =="") 
					throw new Exc_Synt(ErrorType.EMPTY_STRING);
				else; /* OK */
			else
				throw new Exc_Synt(ErrorType.WRONG_TYPE,lex.tokenFullDescr()+
							 " - expected: string");
		 
	 }
	 // <out> -> OUT OPAR STRING CPAR 
	 static void out ( ) throws Exc {
			lex.nextToken();
			lex.setTruncatableString(false); // a file name string cannot be truncated
			lex.accept(Token.OPAR);
			checkString();
			PrintOutput.setFileOutStream(lex.stringVal());
			lex.setTruncatableString(true); // by default string are truncatable
			lex.nextToken();
			lex.accept(Token.CPAR);
	 }
	 
 
	 // <serviceCommand> -> ID("variables") | ID("functions") | ID("release") | ID("labels") |
	 //						ID("about") (UNDERSCORE ID | ID | IDL) | ID("debug") [ID("off") | ID("on")] | 
	 //						ID("history") [ [MINUS] INT] ) | ID("exec") [ [MINUS] NUM ] | ID("clops") | 
	 //						ID("save") | ID("memory") [ ID("size") ] | 
	 //						ID("import") [ID("echo") | ID("noecho")] | EMARK
	 static void serviceCommand() throws Exc {
	 	 typeDef=isService;
		 if ( lex.currToken() == Token.EMARK  ) {
			 lex.nextToken();
			 typeService=isServiceList;	
			 if (lex.currToken()==Token.STAR) {
				 typeServiceList=isSymbolTable; // only internal
				 lex.nextToken();
			 }
			 else
				 typeServiceList=isDefaultList;
			 return;
		 }
		 lex.printH_space();
		 if ( lex.currToken() == Token.LAMBDA) {
			 lex.nextToken();
			 typeService=isLambdaList;
			 return;
		 }
		 if ( lex.currToken() == Token.ID ) {
			 String name = lex.IDName();
			 if ( checkCommand(name,"variables") ) {
				 typeService=isVarService; lex.nextToken();
				 return;
			 }
			 if ( checkCommand(name,"functions") ) {
				 typeService=isFunctService; lex.nextToken();
				 return;
			 }

			 if ( checkCommand(name,"release") ) {
				 typeService=isReleaseService; lex.nextToken();
				 return;
			 }
			 if ( checkCommand(name,"labels")  ) {
				 lex.nextToken();
				 typeService=isLabelService;
				 return;
			 }

			 if ( checkCommand(name,"about")  ) {
				 lex.nextToken();
				 if ( lex.currToken() == Token.BIFUNC ) {
					 typeService = isAboutService; typeAbout = isAboutBuiltIn;
					 builtInF = lex.BIF_type();
					 lex.nextToken();
					 return;
				 }
				 if ( lex.currToken() == Token.ID || lex.currToken() == Token.IDL ) {
					 idAbout = lex.IDName(); lex.nextToken();
					 if ( SymbH.isLabel(idAbout) ) { 
						 typeService = isAboutService; typeAbout = isAboutLABEL;
						 return;
					 }
					 int tID = SymbH.tSymb(idAbout);
					 if ( tID == SymbH.functType ) {
						 typeService = isAboutService; typeAbout = isAboutFUNCT;
						 return;
					 }			 
					 if ( tID == SymbH.varType ) { 
						 typeService = isAboutService; typeAbout = isAboutVAR;
						 return;
					 }
					 throw new Exc_Synt(ErrorType.UNDEF_ID,idAbout );
				 }
				 else
					 throw new Exc_Synt(ErrorType.WRONG_OPTION,lex.currTokenDescr()+
							 " - expected: ID of a defined label, function or variable" );
			 }

			 if ( checkCommand(name,"debug")  ) {
				 lex.nextToken();
				 if ( lex.currToken() == Token.ID ) {
					 if ( lex.IDName().equals("on")  )
						 isDebug = true;
					 else
						 if ( lex.IDName().equals("off")  )
							 isDebug = false;
						 else
							 throw new Exc_Synt(ErrorType.WRONG_OPTION,lex.IDName()+
									 " - expected: 'on' or 'off' or no argument" );
					 lex.nextToken();
				 }
				 else
					 isDebug= true;
				 typeService = isDebug? isDebugOnService: isDebugOffService;
				 return;
			 }
//			 if ( checkCommand(name,"tailOpt")  ) {
//				 lex.nextToken();
//				 if ( lex.currToken() == Token.ID ) {
//					 if ( lex.IDName().equals("on")  )
//						 isTailOpt = true;
//					 else
//						 if ( lex.IDName().equals("off")  )
//							 isTailOpt = false;
//						 else
//							 throw new Exc_Synt(ErrorType.WRONG_OPTION,lex.IDName()+
//									 " - expected: 'on' or 'off' or no argument" );
//					 lex.nextToken();
//				 }
//				 else
//					 isTailOpt= true;
//				 typeService = isTailOpt? isTailOptOnService: isTailOptOffService;
//				 return;
//			 }
			 if ( checkCommand(name,"history")  ) {
				 lex.nextToken();
				 int sign = 1;
				 if ( lex.currToken() == Token.MINUS ) {
					 sign =-1; lex.nextToken();
				 }
				 if ( lex.currToken() == Token.INT  ) {
					    if ( History.nCommand() == 0 )
					    	throw new Exc_Synt(ErrorType.WRONG_HISTORY_INDEX," - empty history ");
					    int iH = lex.intVal()*sign;
						if ( iH < -History.nCommand()+1 || iH > History.nCommand() ) 
							 throw new Exc_Synt(ErrorType.WRONG_HISTORY_INDEX,iH+
										" - must be between "+ (-History.nCommand()+1)+" and "+History.nCommand());
						typeService=isHistoryNumService; 
						numHistory=iH <= 0? History.nCommand()+iH-1: iH-1;
						lex.nextToken();				 
				 }
				 else
					 if ( sign == 1) {
						 typeService=isHistoryService; numHistory=0;
					 }
					 else
						 throw new Exc_Synt(ErrorType.WRONG_HISTORY_INDEX," - spurious \'-\' ");
				 return;
			 }
			 if ( checkCommand(name,"exec")  ) {
				 lex.nextToken();
				 if ( History.nCommand() == 0 )
					 throw new Exc_Synt(ErrorType.WRONG_EXEC_INDEX," - empty history ");
				 int sign = 1;
				 if ( lex.currToken() == Token.MINUS ) {
					 sign =-1; lex.nextToken();
				 }
				 if ( lex.currToken() == Token.INT  ) {
					 	numHistory = lex.intVal()*sign;
						if ( numHistory < -History.nCommand()+1 || numHistory > History.nCommand() ) 
							 throw new Exc_Synt(ErrorType.WRONG_EXEC_INDEX,numHistory+
										" - must be between "+ (-History.nCommand()+1)+" and "+History.nCommand());
						typeService=isExecService; 
						lex.nextToken();				 
				 }
				 else
					 if ( sign == 1) {
						 typeService=isExecService; numHistory=0;
					 }
					 else
						 throw new Exc_Synt(ErrorType.WRONG_EXEC_INDEX," - spurious \'-\' ");
				 numHistory = numHistory <= 0? History.nCommand()+numHistory-1: numHistory-1;
				 return;
			 }
			 if ( checkCommand(name,"clops")  ) {
				 lex.nextToken();
				 typeService=isClops;			 
				 return;
			 }
			 if ( checkCommand(name,"save")  ) {
				 lex.nextToken();
				 typeService=isSaveService;			 
				 return;
			 }
			 if ( checkCommand(name,"memory")  ) {
				 lex.nextToken();
				 typeService=isMemoryService;
				 if ( lex.currToken() == Token.INT ) {
					 numMemoryService=lex.intVal();
					 lex.nextToken();
				 }
				 else
					 numMemoryService=-1;
				 return;
			 }
			 if( checkCommand(name,"import")  ) {
				 lex.nextToken();
				 boolean echo = true;
				 if ( lex.currToken() == Token.ID ) {
					 if ( lex.IDName().equals("echo")  )
						 echo = true;
					 else
						 if ( lex.IDName().equals("noecho")  )
							 echo = false;
						 else
							 throw new Exc_Synt(ErrorType.WRONG_OPTION,lex.IDName()+
									 " - expected: 'echo' or 'noecho' or no argument" );
					 lex.nextToken();
				 }
				 else
					 echo= true;				 
				 typeService=echo? isImportEchoService: isImportNoEchoService;
			 }
			 else
				 throw new Exc_Synt(ErrorType.WRONG_OPTION,lex.IDName()+
								" - expected: 'variables' or 'functions' or 'debug' or 'history' or 'memory' "+
								"or 'labels' or 'import' or 'exec' or 'clops' or 'release' or 'save' or 'about'"+ " or any prefix of them");
		 }
		 else {
			 throw new Exc_Synt(ErrorType.WRONG_TOKEN,lex.tokenFullDescr()+
					 " - expected: "+Token.ID.stToken);
		 }
	 }
	 
	 // method called by <serviceCommand>
	 static boolean checkCommand( String name, String command ) {
		 if ( name.length() > command.length() )
			 return false;
		 String c1 = command.substring(0, name.length());
		 return name.equals(c1);
	 }

	 // <varDef> -> { OSPAR <ifExpr> CSPAR } ( OPASSIGN <ifExpr> |
	 //				ASSIGN ( HASH NULL | <in> | <ifExpr> ) )
	 static void varDef ( ) throws Exc {
		 int typeVar=SymbH.isNewDef()?Exec.GenT: SymbH.typeGV(SymbH.iVar()); 
		 boolean listElOrField=false;
	 	 boolean se = false;
		 Transl.ins(Operator.INIT,SymbH.nVar()); 
	 	 Transl.ins(Operator.LOADGV,SymbH.iVar()); 
 		 if ( lex.currToken() == Token.OSPAR ) {
 			 typeVar=Exec.GenT;
			 lex.nextToken(); listElOrField=true;
			 if ( inFirst(first_ifExp) ) {
				 int precType = SymbH.prevListJsonDef();
			 	 Transl.ins(Operator.DEREF,0);
			 	 SymbH.noDef();
				 int type=ifExp(se);
				 if ( precType == Exec.ListT ) {
					 if ( type != Exec.GenT && !isIntegerOrChar(type) )
							throw new Exc_Synt(ErrorType.WRONG_TYPE, 
									Exec.types[type]+"- expected types: int or char");
					 Transl.ins(Operator.LISTEL,0); 
				 }
				 else {
					 if ( type != Exec.GenT && type != Exec.StringT )
							throw new Exc_Synt(ErrorType.WRONG_TYPE, 
									Exec.types[type]+"- expected type: string");
					 Transl.ins(Operator.FLDFIND,1); 			 
				 }					 
				 lex.accept(Token.CSPAR);
		 		 while ( lex.currToken() == Token.OSPAR ) {
		 			 lex.nextToken();
					 if ( inFirst(first_ifExp) ) {
					 	 Transl.ins(Operator.HDFLDV,0);
						 type=ifExp(se);
						 if ( type != Exec.GenT && !isIntegerOrChar(type) && type != Exec.StringT )
								throw new Exc_Synt(ErrorType.WRONG_TYPE, 
										Exec.types[type]+"- expected types: int or char or string");
						 Transl.ins(Operator.LJEL,0); 
						 lex.accept(Token.CSPAR);
					 }				 
					 else 
						 throw raiseExc(first_ifExp);
				 }
			 }
			 else 
				 throw raiseExc(first_ifExp);	
 		 }
	 	 isNewVarDef = SymbH.isNewDef();
 		 if ( lex.currToken()==Token.OPASS ) {
  			 Token assOpType = lex.currAssOp();
		 	 lex.printH_OpAssign(assOpType,true); // print op-assign with spaces on history
		 	 if ( isNewVarDef )
					throw new Exc_Synt(ErrorType.UNDEF_VAR, 
							"- the compound assignment cannot be used");		 		 
		 	 if ( typeVar != Exec.GenT &&  
		 			 !(typeVar==Exec.ListT || typeVar==Exec.StringT) && assOpType==Token.PLUSPLUS ) 
				throw new Exc_Synt(ErrorType.WRONG_TYPE, 
							Exec.types[typeVar]+"- expected types: list or string");
		 	 if ( typeVar != Exec.GenT && !isNumber(typeVar) && assOpType!=Token.PLUSPLUS )  
				throw new Exc_Synt(ErrorType.WRONG_TYPE, 
							Exec.types[typeVar]+"- expected types: double or long or int or char");		 		 
 			 opAssign(typeVar, listElOrField, assOpType,true); // se = true
 		 }
 		 else {
		 	 lex.printH_Assign(true); // print assign with spaces on history
		 	 lex.accept(Token.ASSIGN);
		 	 boolean isFileIn = lex.currToken() == Token.IN;
			 if( isFileIn ) 
				 in();
			 else
				 if ( lex.currToken() == Token.HASH ) {
					 lex.nextToken();
					 lex.accept(Token.NULL);
				 	 if ( isNewVarDef )
				 		Transl.ins(Operator.PUSHN, 0);
				 	 else {
						 Transl.ins(Operator.DUPL, 0);
						 if ( listElOrField ) {
							Transl.ins(Operator.DUPL, 0);
							Transl.ins(Operator.HDFLDV, 0);
							Transl.ins(Operator.NULLIFY, 1);
						 }
						 else {
							Transl.ins(Operator.DEREF, 0);
							Transl.ins(Operator.NULLIFY, 0);
						 }
				 	 }
				 }
				 else {
				 	 se = true;
					 int retType=ifExp(se);
						if ( isFunction(retType) )
							throw new Exc_Synt(ErrorType.WRONG_TYPE, 
									Exec.types[Exec.FuncT]+"- a function cannot be assigned to a variable");
						else
							if ( isLabel(retType) )
								throw new Exc_Synt(ErrorType.WRONG_TYPE, 
										Exec.types[Exec.FuncT]+"- a label cannot be assigned to a variable");
							
				 }
 		 }
		 if ( listElOrField )
			 Transl.ins(Operator.MODVEL,0);
		 else
			 Transl.ins(Operator.MODV,0);	
	 	 Transl.ins(Operator.HALT,0);

	 }

	 // <in> -> 	IN OPAR STRING {COMMA   STRING  } CPAR
	 static void in ( ) throws Exc {
		 	// it is read a data structure expression stored into a text file:
		 	// the data structure has type from DoubleT to TypeT  
		 	// It is created a code to parse the expression and store the structure into the HEAP
		 	// The value of the data structure will be eventually assigned
	 	 	boolean correctFileIn=true; 
	 	 	String nameFileIn = null;
			lex.nextToken();
			lex.setTruncatableString(false); // a file name string cannot be truncated
			lex.accept(Token.OPAR);
			Transl.ins(Operator.EXECF, 0); // next instructions: MOD, HALT
			checkString();
			nameFileIn =lex.stringVal();
			lex.setTruncatableString(true); // by default string are truncatable
			lex.nextToken();
			excludedKeys = new Vector<String>(initSizeEK,incrSizeEK);
			while ( lex.currToken()==Token.COMMA ) {
				lex.nextToken();
				checkString();
				String st = lex.stringVal();;
				excludedKeys.add(st);
				lex.nextToken();
			}
			lex.accept(Token.CPAR);
			if ( lex.currToken() != Token.SCOLON )
				correctFileIn=false;
		 	 if (  correctFileIn ) {
				 Lex lexFI = new Lex (new IOH(nameFileIn));
				 ioh.print(" - reading data from file '"+nameFileIn+"'\n");
				 lexFI.nextToken();
		         File file = new File(Exec.fileExecName);
		         try {
		        	 	outputExecF = new BufferedWriter(new FileWriter(file));
		         } 
		         catch (Exception e) {
		        	 	throw new Exc_Synt(ErrorType.WRONG_EXEC_FILE, Exec.fileExecName+": unexpected error while opening");
		        	 }
		 		 History.histOff();
		 		 factFI(lexFI,true);
		         try {
		        	 	outputExecF.close();
		         } 
		         catch (Exception e) {
		        	 	throw new Exc_Synt(ErrorType.WRONG_EXEC_FILE, Exec.fileExecName+": unexpected error while closing");
		        	 }
		 		 if ( lexFI.currToken() != Token.END )
					 throw new Exc_Synt(ErrorType.WRONG_FILE_IN,
							 " - spurious characters at the end of the file");
				 ioh.print("\n - data are correct\n");
		 		 // Transl.ins(Operator.JUMP,iJumpFileIn);
		 		 History.histOn();
		 	 }
	}

	 // called by opAssign
	private static void checkGenNum(int type) throws Exc {
		if ( type!=Exec.GenT && !isNumber(type) )
			throw new Exc_Synt(ErrorType.WRONG_TYPE, 
					Exec.types[type]+"- expected types: double or long or int or char");		 		 		
	}

	 // called by opAssign
	private static void checkGenStringList(int type1,int type2) throws Exc {
		if ( type1==Exec.GenT && type2==Exec.GenT )
			return;
		if ( type1!=Exec.GenT && type1!=Exec.StringT && type1!=Exec.ListT )
			throw new Exc_Synt(ErrorType.WRONG_TYPE, 
					Exec.types[type1]+"- expected types: string or list");		 		 		
		if ( type2!=Exec.GenT && type2!=Exec.StringT && type2!=Exec.ListT )
			throw new Exc_Synt(ErrorType.WRONG_TYPE, 
					Exec.types[type2]+"- expected types: string or list");		 		 		
		if ( type1!=Exec.GenT && type2!=Exec.GenT && type1!=type2 )
			throw new Exc_Synt(ErrorType.WRONG_TYPE, 
					Exec.types[type2]+"- expected type: "+Exec.types[type1]);		 		 		
	}

	// called by varDef and varGlobSet
	private static void opAssign (int type, boolean listElorField, Token opAssType, boolean se ) throws Exc {
		Transl.ins(Operator.DUPL, 0);
		if ( listElorField )
			Transl.ins(Operator.HDFLDV, 0);
		else
			Transl.ins(Operator.DEREF, 0);
		lex.nextToken();
		int typeExp = ifExp(se); 
		if ( isFunction(typeExp) )
			throw new Exc_Synt(ErrorType.WRONG_TYPE, 
					Exec.types[Exec.FuncT]+"- a function cannot be assigned to a variable");		 		 		
		if ( opAssType==Token.PLUS) {
			checkGenNum(type); checkGenNum(typeExp);
			Transl.ins(Operator.ADD, 0);
			return;
		}
		if ( opAssType==Token.PLUSPLUS) {
			checkGenStringList(type,typeExp);
			Transl.ins(Operator.ADDLS, 0);
			return;
		}
		if ( opAssType==Token.MINUS ) {
			checkGenNum(type); checkGenNum(typeExp);
			Transl.ins(Operator.SUB, 0);
			return;
		}
		if ( opAssType==Token.STAR) {
			checkGenNum(type); checkGenNum(typeExp);
			Transl.ins(Operator.MULT, 0);
			return;
		}
		if ( opAssType==Token.DIV) {
			checkGenNum(type); checkGenNum(typeExp);
			Transl.ins(Operator.DIV, 0);
			return;
		}
		// opAssType == Token.DIVTR
		checkGenNum(type); checkGenNum(typeExp);
		Transl.ins(Operator.DIV, 0);
		Transl.ins(Operator.TOINT, 0);
	}
	
	// called by factFI
	private static String opStr( Instruction.Operator op) {
		return Instruction.getOperatorString(op);
	}
	 
	// called by factFI
	private static void writeFI (String st) throws Exc {
		try {
			outputExecF.write(st+"\n");
		}
		catch ( Exception e) {
    	 		throw new Exc_Synt(ErrorType.WRONG_EXEC_FILE, Exec.fileExecName+": unexpected error");			
		}
	}
		//<factFI> -> OSPAR [ <factFI> { [COMMA]  <factFI> } ] CSPAR  | 
	    //			  OBRACE [ STRING COLON <factFI> [ COMMA STRING COLON <factFI> ]* CBRACE |
		// 			    [ PLUS | MINUS ] ( INT | LONG | DOUBLE ) | CHAR | STRING | NULL | BOOL | TYPE
	private static void factFI(Lex lexFI, boolean copyElem ) throws Exc {
		 if ( lexFI.currToken() == Token.OSPAR ) {
			 if ( copyElem )
				 writeFI(opStr(Operator.SLIST)+" "+0);
				 // Transl.ins(Operator.SLIST,0);
			 lexFI.nextToken();
			 if ( inFirstFI(lexFI,first_factFI) ) { 
				 factFI(lexFI,copyElem);
				 if ( copyElem )
					 writeFI(opStr(Operator.HLIST)+" "+0);
					 // Transl.ins(Operator.HLIST,0);
				 while ( lexFI.currToken() == Token.COMMA || lexFI.currToken() != Token.CSPAR ) {
					 if ( lexFI.currToken() == Token.COMMA ) 
						 lexFI.nextToken();
					 factFI(lexFI,copyElem);
					 if ( copyElem )
						 writeFI(opStr(Operator.CLIST)+" "+0);
						 // Transl.ins(Operator.CLIST,0);
				}
			 }
			 if ( copyElem )
				 writeFI(opStr(Operator.ELIST)+" "+0);
				 // Transl.ins(Operator.ELIST,0);
			 lexFI.accept(Token.CSPAR );
			 return ;
		 }
		if ( lexFI.currToken() == Token.OBRACE ) { 
			 if ( copyElem )
				 writeFI(opStr(Operator.SJSON)+" "+0);
				 // Transl.ins(Operator.SJSON,0);
			 lexFI.nextToken();
			 if ( lexFI.currToken() != Token.CBRACE ) {
				 if ( lexFI.currToken() != Token.STRING )
					 throw new Exc_Synt(ErrorType.WRONG_TOKEN,lex.currTokenDescr()+
							 " - expected: 'STRING'");
				 String keyStr=lexFI.stringVal();
				 boolean excludedK = excludedKeys.contains(keyStr);
				 if ( copyElem && !excludedK )
					 getStringFI(keyStr);
				 if ( lexFI.isStringOverflow() )
					 ioh.print("\n**Warning: string truncated to max length "+Lex.maxLenString+"\n");
				 lexFI.nextToken();
				 lexFI.accept(Token.COLON);
				 if ( !copyElem || excludedK )
					 factFI(lexFI,false);
				 else {
					 factFI(lexFI,true);
					 writeFI(opStr(Operator.NEWFLD)+" "+0);
					 // Transl.ins(Operator.NEWFLD,0);
					 writeFI(opStr(Operator.CJSON)+" "+0);
					 // Transl.ins(Operator.CJSON,0);
				 }
				 while ( lexFI.currToken() == Token.COMMA || lexFI.currToken() != Token.CBRACE ) {
					    if ( lexFI.currToken() == Token.COMMA ) 
					    		lexFI.nextToken();
						if ( lexFI.currToken() != Token.STRING )
							 throw new Exc_Synt(ErrorType.WRONG_TOKEN,lex.currTokenDescr()+
									 " - expected: 'STRING'");
						keyStr=lexFI.stringVal();
						excludedK = excludedKeys.contains(keyStr);
						 if ( copyElem && !excludedK )
							 getStringFI(keyStr);
						 if ( lexFI.isStringOverflow() )
							 ioh.print("\n**Warning: string truncated to max length "+Lex.maxLenString+"\n");
						lexFI.nextToken();
						lexFI.accept(Token.COLON);
						 if ( !copyElem || excludedK )
							 factFI(lexFI,false);
						 else {
							 factFI(lexFI,true);
							 writeFI(opStr(Operator.NEWFLD)+" "+0);
							 // Transl.ins(Operator.NEWFLD,0);
							 writeFI(opStr(Operator.CJSON)+" "+0);
							 // Transl.ins(Operator.CJSON,0);
				  }
			 }
			 if ( copyElem )
				 writeFI(opStr(Operator.EJSON)+" "+0);
				 // Transl.ins(Operator.EJSON,0);
			 lexFI.accept(Token.CBRACE );
			 return ;
		 }
		}
		
		 if ( lexFI.currToken() == Token.PLUS || lexFI.currToken() == Token.MINUS ) {
			 boolean isMinus = lexFI.currToken() == Token.MINUS;
			 lexFI.nextToken();
			 if ( lexFI.currToken() == Token.DOUBLE ) {
				 if ( copyElem )
					 writeFI(opStr(Operator.PUSHD)+" "+lexFI.doubleVal());
					 // Transl.ins(Operator.PUSHD,lexFI.doubleVal());
			 }
			 else
				 if ( lexFI.currToken() == Token.INT ) {
					 if ( copyElem )
						 writeFI(opStr(Operator.PUSHI)+" "+lexFI.intVal());
						 // Transl.ins(Operator.PUSHI,lexFI.intVal());
				 }
				 else
					 if ( lexFI.currToken() == Token.LONG ) {
						 if ( copyElem )
							 writeFI(opStr(Operator.PUSHL)+" "+Double.longBitsToDouble(lexFI.longVal()));
							 // Transl.ins(Operator.PUSHL,lexFI.longVal());
					 }
					 else
						 throw new Exc_Synt (ErrorType.WRONG_TOKEN, 
							 lexFI.tokenFullDescr() + " - expected: int or long or double");
			 lexFI.nextToken(); 
			 if ( isMinus && copyElem )
				 writeFI(opStr(Operator.NEG)+" "+0);
				 // Transl.ins(Operator.NEG,0);
			 return;
		 }
		
		 if ( lexFI.currToken() == Token.DOUBLE ) {
			 	 if( copyElem )
					 writeFI(opStr(Operator.PUSHD)+" "+lexFI.doubleVal());
			 		// Transl.ins(Operator.PUSHD,lexFI.doubleVal());
				 lexFI.nextToken();
				 return;
		 }
		 if ( lexFI.currToken() == Token.INT ) {
		 	 if( copyElem )
				 writeFI(opStr(Operator.PUSHI)+" "+lexFI.intVal());
		 		 // Transl.ins(Operator.PUSHI,lexFI.intVal());
			 lexFI.nextToken();
			 return;
		 }
		 if ( lexFI.currToken() == Token.LONG ) {
		 	 if( copyElem )
				 writeFI(opStr(Operator.PUSHL)+" "+Double.longBitsToDouble(lexFI.longVal()));
		 		 // Transl.ins(Operator.PUSHL,lexFI.longVal());
			 lexFI.nextToken();
			 return;
		 }
		 if ( lexFI.currToken() == Token.CHAR ) {
		 	 	if( copyElem )
					 writeFI(opStr(Operator.PUSHC)+" "+(int)lexFI.CHARvalue());
		 	 		// Transl.ins(Operator.PUSHC,lexFI.CHARvalue());
				 lexFI.nextToken();
				 return;
		 }
		 if ( lexFI.currToken() == Token.STRING ) {
		 	 	if( copyElem )
		 	 		getStringFI(lexFI.stringVal());
				 if ( lexFI.isStringOverflow() )
					 ioh.print("\n**Warning: string truncated to max length "+Lex.maxLenString);
				 lexFI.nextToken();
				 return;
		 }
		 if ( lexFI.currToken() == Token.NULL ) {
		 	 if( copyElem )
				 writeFI(opStr(Operator.PUSHN)+" "+0);
		 		 // Transl.ins(Operator.PUSHN,0);
			 lexFI.nextToken();
			 return;
		 }
		 if ( lexFI.currToken() == Token.BOOL ) {
		 	 	 if( copyElem )
		 	 		 if (lexFI.valBool())
		 	 			 writeFI(opStr(Operator.PUSHB)+" "+1);
		 	 		 else
		 	 			writeFI(opStr(Operator.PUSHB)+" "+0);
				 lexFI.nextToken();
				 return;
		 }
		 if ( lexFI.currToken() == Token.TYPE ) {
			 	 if( copyElem )
					 writeFI(opStr(Operator.PUSHT)+" "+lexFI.currType());
			 		 // Transl.ins(Operator.PUSHT,lexFI.currType());
				 lexFI.nextToken();
				 return;
		 }		
		 throw raiseExcFI(lexFI,first_factFI); 
	}

	 // method called by factFI 
	 static void getStringFI(String s ) throws Exc {
		 writeFI(opStr(Operator.SSTRING)+" "+0);
		 for ( int i =0; i <s.length(); i++ ) {
			 writeFI(opStr(Operator.PUSHC)+" "+(int)s.charAt(i));
			 writeFI(opStr(Operator.CSTRING)+" "+0);
		 }
		 writeFI(opStr(Operator.ESTRING)+" "+0);
	 }


	// <funcDef> -> OPAR [<formPar> { COMMA <formPar> } ] CPAR  
	// 					[ DIV INT ] MAP [COMMENT]
	//					[LT ID [STAR] { COMMA ID [STAR] } GT] <fbody>

	 static void funcDef ( String idN, boolean se, boolean hasLabel, String label ) throws Exc {
		 lex.accept(Token.OPAR );
		 SymbH.funcDef(se,ioh.nUtil());
		 if ( lex.currToken() == Token.ID || lex.currToken() == Token.UNDERSCORE) {
			 formPar( se );
			 while ( lex.currToken() == Token.COMMA ) {
				 lex.printH_Comma(true); // print space after comma in history
				 lex.nextToken();
				 formPar( se );
			 }
		 }
		 lex.accept(Token.CPAR );
		 if ( lex.currToken()==Token.DIV ) {
			 lex.nextToken();
			 if ( lex.currToken()== Token.INT ) {
				 int arity = lex.intVal();
				 SymbH.setRetType(SymbH.functType,arity);
				 lex.nextToken();
			 }
			 else
					throw new Exc_Sem(ErrorType.WRONG_FUNCT_DEF, 
							"-- expected INT for return function arity - found " + lex.tokenFullDescr());					 		
		 }
		 else
			 SymbH.setRetType(SymbH.varType,0);
		 SymbH.endPar(); 
		 if ( lex.currToken() == Token.MAP ) {
			 lex.setComment(true); lex.nextToken();
		 }
		 else
			 throw new Exc_Synt(ErrorType.WRONG_TOKEN,lex.currTokenDescr()+
					 " - expected: '->'");
		 if ( lex.currToken()==Token.COMMENT ) {
			 SymbH.addFunctComment(lex.commentString());
			 if ( lex.isCommentOverflow() )
				 ioh.print("\n**Warning: comment string truncated to max length "+Lex.maxLenComment);
			 lex.setComment(false); 
			 lex.nextToken();
		 }
		 lex.setComment(false); 
		 nLocVars=0;
		 if ( hasLabel )
			 SymbH.addCurrLabel(label);
 		 if ( lex.currToken() == Token.GTLT) {
 			 	 if ( lex.GTLT_Op() != GTLTOpType.LT)
 			 		throw new Exc_Synt(ErrorType.WRONG_FUNCT_DEF, ": expected '<' - found "+lex.tokenFullDescr());
	 			 lex.nextToken();
	 			 if ( lex.currToken() == Token.ID ) {
	 				 String idNLV = lex.IDName();
	 				 lex.nextToken();
	 				 if ( lex.currToken() == Token.STAR ) {
	 					 lex.nextToken();
	 	 				 if ( !se )
	 	 					 throw new Exc_Synt(ErrorType.GLOB_VAR_NO_SE_FUNCT);	 					 
	 	 				 SymbH.addCurrLabel(idNLV);
	 				 }
	 				 else {
	 					 SymbH.addLoc(idNLV); 
	 					 nLocVars++;
	 				 } 	
	 				 while (lex.currToken() == Token.COMMA) {
	 					lex.printH_Comma(false); // no space after comma in history
	 	 				 lex.nextToken(); 
	 	 				 locVarOrLab(se);	 				 
	 	 			 };
	 	 			 if ( lex.currToken() == Token.GTLT && lex.GTLT_Op() == GTLTOpType.GT )
	 	 				 lex.nextToken();
	 	 			 else
	 	 				throw new Exc_Synt(ErrorType.WRONG_FUNCT_DEF, ": expected '>' - found "+lex.tokenFullDescr());	 			 }
	 			 else
						throw new Exc_Synt(ErrorType.WRONG_FUNCT_DEF, ": expected ID after '<' - found "+lex.tokenFullDescr());
 		 }
	 	 Transl.ins(Operator.START,SymbH.indCurrDef(),"* "+idN);
	 	 if ( nLocVars>0 )
	 		 Transl.ins(Operator.ALLOC,nLocVars);
//	 	 int retType=ifSet(se,idN);
	 	 GuardRet gr= fBody(se,idN);
	 	 if ( isFunction(gr.exprt) ) 
		 	 if (gr.fc.hasFunctParams)
					throw new Exc_Sem(ErrorType.WRONG_RET_TYPE, 
					"-- a returned function cannot have function parameters");
		 	 else
			 	 if (!se && gr.fc.hasSideEffect)
						throw new Exc_Sem(ErrorType.WRONG_RET_TYPE, 
						"-- returned function with side effects");		 		 
		 	 else	 		 
	 		        SymbH.checkRetType(SymbH.functType,gr.fc.numParams);
	 	 else
		 	 if (isLabel(gr.exprt))
					throw new Exc_Sem(ErrorType.WRONG_RET_TYPE, 
					"-- a function cannot return a label");
		 	 else
		 		 SymbH.checkRetType(SymbH.varType,0);
	 	 Transl.ins(Operator.RETURN,SymbH.nParFdef());
//	 	 if ( isTailOpt )
//	 		 Transl.tailRecOpt(SymbH.indCurrDef(),SymbH.nParFdef());
	 	 SymbH.endFdef(Transl.end());		 
	}

		// method called by <funcDef>
	 static void locVarOrLab ( boolean se ) throws Exc {
		 String idN = ""; 
		 if ( lex.currToken() == Token.ID ) {
			 idN = lex.IDName();
		 }
		 else
				throw new Exc_Synt(ErrorType.WRONG_FUNCT_DEF, ": expected ID inside '< >' - found "+lex.tokenFullDescr());
		 lex.accept(Token.ID );
		 if ( lex.currToken() == Token.STAR ) {
				 lex.nextToken();
 				 if ( !se )
 					 throw new Exc_Synt(ErrorType.GLOB_VAR_NO_SE_FUNCT);	 					 
				 SymbH.addCurrLabel(idN);
		 }
		 else {
			 SymbH.addLoc(idN); 
			 nLocVars++;
		 }
	 }

		// <formalPar> ->  ( ID | UNDERSCORE) [ DIV INT ]
	 static void formPar ( boolean se ) throws Exc {
		 if ( lex.currToken() == Token.ID || lex.currToken() == Token.UNDERSCORE ) {
			 String idN = "_";
			 if ( lex.currToken() == Token.ID ) {
				 idN = lex.IDName();
			 }
			 lex.nextToken();
			 if ( lex.currToken()==Token.DIV ) {
				 lex.nextToken();
				 if ( lex.currToken()== Token.INT ) {
					 int arity = lex.intVal();
					 SymbH.addPar(idN, SymbH.functType,arity);
					 lex.nextToken();
				 }
				 else
						throw new Exc_Sem(ErrorType.WRONG_FUNCT_DEF, 
								"-- expected INT for function parameter arity - found " + lex.tokenFullDescr());					 		
			 }
			 else
				 SymbH.addPar(idN, SymbH.varType,0); 
		 }
		 else
			 throw new Exc_Synt(ErrorType.WRONG_TOKEN,lex.tokenFullDescr()+
					 " - expected: 'ID' or '_'");
	}
	 
	 // <guard> ::= { <set> } <ifExp> ":" { <set> } <ifExp> { <set> }
	 static GuardRet guard( boolean se, boolean firstGuard, String idN, ArrayList<Integer> jumpLab, GuardRet guardp ) throws Exc {
		 while ( inFirst(first_set) ) 
			 set(se,idN);		 
		 int ifExpr1_t = ifExp(se);
		 if ( ifExpr1_t != Exec.BoolT &&  ifExpr1_t != Exec.GenT ) 
				throw new Exc_Synt(ErrorType.WRONG_TYPE, 
							Exec.types[ifExpr1_t]+"- expected type: bool");
		 lex.printH_Colon(true); // print spaced COLON on history
		 lex.accept(Token.COLON);
		 int iJumpE = Transl.ins(Operator.JUMPZ,0);
		 while ( inFirst(first_set) ) 
			 set(se,idN); 
		 int ifExpr2_t = ifExp(se);
		 FunctCalled fc=null;
		 if ( isFunction(ifExpr2_t) )
			 fc=currFunctCalled;
		 int retType=ifExpr2_t;
		 if ( !firstGuard ) 
			 if ( ifExpr2_t == guardp.exprt ) 
				 if ( isFunction(ifExpr2_t) && !FunctCalled.equiv(fc,guardp.fc) )
					 throw new Exc_Synt(ErrorType.WRONG_FTYPE, 
								Exec.types[guardp.exprt]+" and "+Exec.types[ifExpr2_t]+
								"-- the guarded alternative functions must have the same signature");
				 else
					 retType= ifExpr2_t;
			 else
				 if (isNumber(guardp.exprt) && isNumber(ifExpr2_t))
					 retType= guardp.exprt < ifExpr2_t? guardp.exprt: ifExpr2_t;
				 else
					 if ( isFunction(guardp.exprt) || isFunction(ifExpr2_t) )
							throw new Exc_Synt(ErrorType.WRONG_TYPE, 
							Exec.types[guardp.exprt]+" and "+Exec.types[ifExpr2_t]+
							"-- if a guarded alternative is a function, all alternatives must be functions");
					 else // different types are instead allowed for operands of type variable
						 retType = Exec.GenT;	 
		 while ( inFirst(first_set) ) 
			 set(se,idN); 
		 int iJumpT = Transl.ins(Operator.JUMP,0);
		 jumpLab.add(iJumpT);
		 int labelE = Transl.ins(Operator.NEXT,0);
		 Transl.modOperand(iJumpE, labelE);
		 return new GuardRet(retType,fc);
	 }

	 // <fBody> ::= { <set> } [ "|" <guard> "|" { <guard> "|" } ":" ] { <set> } <ifExp> { <set> }
	 static GuardRet fBody ( boolean se, String idN ) throws Exc {
		 boolean  isSE=se;
		 while ( inFirst(first_set) ) 
			 set(se,idN);
		 boolean guarded = false;
		 GuardRet guard1=null, guard2=null;
		 int initCapacityJumpLab=20;
		 ArrayList<Integer> jumpLab=new ArrayList<Integer>(initCapacityJumpLab);
		 if ( lex.currToken() == Token.PIPE ) {
			 guarded=true;
			 lex.nextToken();
			 guard1=guard(isSE,true, idN, jumpLab,null);
			 lex.accept(Token.PIPE);
			 while (inFirst(first_ifExp) || inFirst(first_set)) {
				 guard2=guard(isSE,false,idN,jumpLab,guard1);
				 guard1=guard2;
				 lex.accept(Token.PIPE);
			 }
			 lex.printH_Colon(true); // print spaced COLON on history
			 lex.accept(Token.COLON);
		 }
		 while ( inFirst(first_set) ) 
			 set(se,idN);
		 int ifExpr_t = ifExp(se);
		 FunctCalled fc=null;
		 if ( isFunction(ifExpr_t) )
			 fc=currFunctCalled;
		 GuardRet grv=null;
		 if ( guarded ) {
			 if ( ifExpr_t == guard1.exprt ) 
				 if ( isFunction(ifExpr_t) && !FunctCalled.equiv(fc,guard1.fc) )
					 throw new Exc_Synt(ErrorType.WRONG_FTYPE, 
								Exec.types[guard1.exprt]+" and "+Exec.types[ifExpr_t]+
								"-- the guarded alternative functions must have the same signature");
				 else
					 grv=new GuardRet(ifExpr_t,fc);
			 else
				 if (isNumber(guard1.exprt) && isNumber(ifExpr_t)) {
					 int retType= guard1.exprt < ifExpr_t? guard1.exprt: ifExpr_t;
					 grv=new GuardRet(retType,fc);
				 }
				 else
					 if ( isFunction(guard1.exprt) || isFunction(ifExpr_t) )
							throw new Exc_Synt(ErrorType.WRONG_TYPE, 
							Exec.types[guard1.exprt]+" and "+Exec.types[ifExpr_t]+
							"-- if a guarded alternative is a function, all alternatives must be functions");
					 else // different types are instead allowed for operands of type variable
						 grv=new GuardRet(Exec.GenT, fc);		 
		 }
		 else
			 grv=new GuardRet(ifExpr_t,fc);
		 while ( inFirst(first_set) ) 
			 set(se,idN);		 
		 if ( guarded ) {
			 int lastLab = Transl.ins(Operator.NEXT,0);
			 for ( int i=0; i<jumpLab.size(); i++ ) {
				 int jumpT = jumpLab.get(i);
				 Transl.modOperand(jumpT, lastLab);
			 }				 
		 }
		return grv;  
	}
	 
	 	// called by dSet
	 static void printExc(String idN) throws Exc {
		 Transl.ins(Operator.SSTRING,0);
		 for ( int i =0; i <idN.length(); i++ ) {
			 Transl.ins(Operator.PUSHC, idN.charAt(i));
			 Transl.ins(Operator.CSTRING, 0 );
		 }
		 Transl.ins(Operator.ESTRING,0);
		 
	 }
	 
	 // <set> -> OBRACESET ( <prSet> | <ioSet>)  { CONTINUE ( <prSet> | <ioSet>) } CBRACESET  | 
		 static void set ( boolean se, String idN ) throws Exc {
		 	 if ( lex.currToken() == Token.OBRACESET ) {
			 	 do { 
			 		 lex.nextToken();
					 if ( inFirst(first_prSet) ) 
						 prSet(se,idN);
					 else
						 if ( inFirst(first_ioSet) ) 
							 ioSet(se);
						 
					 else 
						 raiseExc(first_pr_ioSet);
			 	 } while ( lex.currToken() == Token.CONTINUE );
				lex.accept(Token.CBRACESET);
		 	 }
		 	 else 
				throw new Exc_Synt(ErrorType.EXPECTED_SET_BRACE," -- found "+
						 		lex.tokenFullDescr());			 		 
		 }
		 
			 
		 // <prSet> ->  ( ID | IDL ) { OSPAR <ifExp> CSPAR } ( OPASSIGN <ifExp> | ASSIGN ( <ifExp> | HASH NULL  ) 
		 //           | OSPAR <ifExp> CSPAR | EXC OPAR <ifExp> COMMA <ifExp> CPAR
		 static void prSet ( boolean se, String idN ) throws Exc {
				 if ( lex.currToken() == Token.EXC ) {
					 lex.nextToken();
					 lex.accept(Token.OPAR);
					 int ifexpr_t = ifExp(se);
					 if ( ifexpr_t != Exec.BoolT && ifexpr_t != Exec.GenT ) //**
							throw new Exc_Synt(ErrorType.WRONG_TYPE, 
										Exec.types[ifexpr_t]+"- expected type: boolean");
					 int iJumpE = Transl.ins(Operator.JUMPZ,0);
	 				 lex.printH_Comma(false); // no space after comma in history
					 lex.accept(Token.COMMA);
					 lex.setTruncatableString(false); // exc string cannot be truncated
					 int exc_t=ifExp(se);
					 if ( exc_t != Exec.GenT && exc_t != Exec.StringT )
							throw new Exc_Synt(ErrorType.STRING_EXPECTED, 
									 Exec.types[exc_t]+"- expected type: string");
					 printExc(idN);
					 lex.setTruncatableString(true); // by default string are truncatable
					 lex.accept(Token.CPAR);
					 Transl.ins(Operator.THROWE,0);
					 int labelT = Transl.ins(Operator.NEXT,0);
					 Transl.modOperand(iJumpE, labelT);
				 }
				 else 
					 if ( lex.currToken() == Token.OSPAR){
						 lex.nextToken();
						 if ( !se )
								throw new Exc_Synt(ErrorType.NO_SE_FUNC, 
										"- the operator '[exp]' requires the currently defined function to be with side effects");		 		 									 
						 int retType=ifExp(true);
						 if ( isFunction(retType) )
								throw new Exc_Synt(ErrorType.WRONG_TYPE, 
										Exec.types[Exec.FuncT]+"- a function cannot be used into a Global Setting");		 		 									 
						 if ( isLabel(retType) )
								throw new Exc_Synt(ErrorType.WRONG_TYPE, 
										Exec.types[Exec.LabT]+"- a label cannot be used into a Global Setting");		 		 		
						 Transl.ins(Operator.POP,0);
						 lex.accept(Token.CSPAR);
					 }
					 else
						 vSet(se);
		 }
	 
	 // called by vSet and by ioSet
	static boolean vSetLeft ( boolean se ) throws Exc {
		 boolean listElOrField=false;		
		 String idN = lex.IDName(); 
		 if ( SymbH.isLabel(idN) )
				throw new Exc_Synt(ErrorType.WRONG_LAB_SET, " " + idN);
		 boolean hasLabel=lex.currToken()==Token.IDL;
		 String label =   hasLabel? lex.currLabel(): null;
		 if ( hasLabel && !SymbH.isLabel(label) )
				throw new Exc_Sem(ErrorType.UNDEF_LABEL, ": "+label);
		 int type = SymbH.setCurrID(idN,se,hasLabel,label,setLabel,setLabelID);
		 if ( type == SymbH.undefID )
				throw new Exc_Synt(ErrorType.UNDEF_ID, " " + idN);
		 if ( type == SymbH.functType )
				throw new Exc_Synt(ErrorType.WRONG_FUN_SET, " " + idN);
		 if ( SymbH.isParam() ) 
			 if ( se ) {
				 int nPar = SymbH.nParCurrFunDef();
				 Transl.ins(Operator.LOADPAR,nPar-SymbH.iCurrVarFunc());
			 }
			 else
				 throw new Exc_Synt(ErrorType.NO_PAR_SET);
		 else
			 if ( SymbH.isLocVar() )
				 Transl.ins(Operator.LOADLV,SymbH.iCurrVarFunc());
			 else // SymbH.isLabGlobVar() is true
					 Transl.ins(Operator.LOADGV,SymbH.iCurrVarFunc()); 
		 lex.nextToken(); 

		 if ( lex.currToken() == Token.OSPAR ) {
			 lex.nextToken(); 
			 listElOrField=true;
			 if ( inFirst(first_ifExp) ) {
			 	 Transl.ins(Operator.DEREF,0);
				 type=ifExp(se);
				 if ( type != Exec.GenT && !isIntegerOrChar(type) && type != Exec.StringT )
						throw new Exc_Synt(ErrorType.WRONG_TYPE, 
								Exec.types[type]+"- expected types: int or char or string");
				 Transl.ins(Operator.LJEL,0); 
				 lex.accept(Token.CSPAR);
		 		 while ( lex.currToken() == Token.OSPAR ) {
		 			 lex.nextToken();
					 if ( inFirst(first_ifExp) ) {
					 	 Transl.ins(Operator.HDFLDV,0);
						 type=ifExp(se);
						 if ( type != Exec.GenT && !isIntegerOrChar(type) && type != Exec.StringT )
								throw new Exc_Synt(ErrorType.WRONG_TYPE, 
										Exec.types[type]+"- expected types: int or char or string");
						 Transl.ins(Operator.LJEL,0); 
						 lex.accept(Token.CSPAR);
					 }				 
					 else 
						 throw raiseExc(first_ifExp);
				 }
			 }
			 else 
				 throw raiseExc(first_ifExp);	
		 }
		 return listElOrField;
	}

	 // called by prSet to implement the syntax below:
	 // ( ID | IDL ) { OSPAR <ifExp> CSPAR } ( OPASSIGN <ifExp> | ASSIGN ( <ifExp> | HASH NULL  ) 
	 static void vSet ( boolean se ) throws Exc {
		 boolean listElOrField=vSetLeft(se);		
 		 if ( lex.currToken()==Token.OPASS ) {
 			 Token assOpType = lex.currAssOp();
		 	 lex.printH_OpAssign(assOpType,false); // print op-assign without spaces on history
 			 opAssign(Exec.GenT, listElOrField, assOpType,se); 
 		 }
 		 else {
		 	 lex.printH_Assign(false); // print assign without spaces on history
		 	 lex.accept(Token.ASSIGN);
			 if ( lex.currToken() == Token.HASH ) {
				 lex.nextToken();
				 lex.accept(Token.NULL);
				 Transl.ins(Operator.DUPL, 0);
				 if ( listElOrField ) {
					Transl.ins(Operator.DUPL, 0);
					Transl.ins(Operator.HDFLDV, 0);
					Transl.ins(Operator.NULLIFY, 1);
				 }
				 else {
					Transl.ins(Operator.DEREF, 0);
					Transl.ins(Operator.NULLIFY, 0);
				 }
			 }
			 else {
				 int typeExp=ifExp(se);
				 if ( isFunction(typeExp) )
						throw new Exc_Synt(ErrorType.WRONG_TYPE, 
								Exec.types[Exec.FuncT]+"- a function cannot be assigned to a variable");		 		 		
				 if ( isLabel(typeExp) )
						throw new Exc_Synt(ErrorType.WRONG_TYPE, 
								Exec.types[Exec.LabT]+"- a label cannot be assigned to a variable");		 		 		
			 }
 		 }
		 if ( listElOrField )
			 Transl.ins(Operator.MODVEL,0);
		 else
			 Transl.ins(Operator.MODV,0);	
	 }

	 // <ioSet> -> PRINT OPAR <ifExpr> [ PRINTOPT ] CPAR | GT OPAR <ifExpr> CPAR | 
	 // LT OPAR ( ID | IDL ) { OSPAR <ifExp> CSPAR } CPAR 
	 static void ioSet ( boolean se ) throws Exc {
		 if ( lex.currToken() == Token.GTLT && lex.GTLT_Op()==GTLTOpType.GT) { // interactive display
			 if ( !se ) 
					throw new Exc_Synt(ErrorType.SE_DISPLAY);		 		 		
			 lex.nextToken(); lex.accept(Token.OPAR);
			 int typeExp=ifExp(false); // no side effects allowed
			 if ( isFunction(typeExp) )
					throw new Exc_Synt(ErrorType.WRONG_TYPE, 
							Exec.types[Exec.FuncT]+"- a function cannot be displayed");		 		 		
			 if ( isLabel(typeExp) )
					throw new Exc_Synt(ErrorType.WRONG_TYPE, 
							Exec.types[Exec.LabT]+"- a label cannot be displayed");		 		 		
			 Transl.ins(Operator.DISPLAY,0);
			 lex.accept(Token.CPAR);
		 }
		 else
			 if ( lex.currToken() == Token.GTLT && lex.GTLT_Op()==GTLTOpType.LT) { 
				 // (interactive) read of a text line from console 
				 // the text is interpreted as a value to be stored
				 // into a variable or list element or JSON field
				 if ( !se ) 
						throw new Exc_Synt(ErrorType.SE_DISPLAY);		 		 		
				 lex.nextToken(); lex.accept(Token.OPAR);
				 boolean listElOrField=vSetLeft(false); // no side effects allowed
				 Transl.ins(Operator.READL,0);
				 Transl.ins(Operator.EXECF,0);
				 if ( listElOrField )
					 Transl.ins(Operator.MODVEL,0);
				 else
					 Transl.ins(Operator.MODV,0);	
				 lex.accept(Token.CPAR);
			 }
			 else { // print on the output
		 		lex.nextToken(); //Current Token must be PRINT
				lex.accept(Token.OPAR);
		 		ifExp(se); 
//		 		int typeExp=ifExp(se); 
//				if ( isFunction(typeExp) )
//			 		Transl.ins(Operator.OUTF,0);
		 		if ( lex.currToken() == Token.PRINTOPT) {
					Transl.ins(Operator.PRINT,lex.indPrint());
					lex.nextToken();
		 		}
		 		else
		 			Transl.ins(Operator.PRINT,0);
				lex.accept(Token.CPAR);
			 }
	 }
	
	//**<ifExpr> ->  <orExpr(se)> [ QMARK <ifExpr> COLON <ifExpr> ]
	 static int ifExp ( boolean se ) throws Exc {
		 int cexpr_t = orExpr(se);
		 if ( lex.currToken() == Token.QMARK ) {
			 if ( !isBool(cexpr_t) ) 
					throw new Exc_Synt(ErrorType.WRONG_TYPE, 
								Exec.types[cexpr_t]+"- expected type: boolean");
			 lex.nextToken();
			 int iJumpE = Transl.ins(Operator.JUMPZ,0);
			 int ifExpr1_t = ifExp(se);
			 FunctCalled fc1=null;
			 if ( isFunction(ifExpr1_t) )
				 fc1=currFunctCalled;
			 int iJumpT = Transl.ins(Operator.JUMP,0);
			 lex.printH_Colon(true); // print spaced COLON on history
			 lex.accept(Token.COLON );
			 int labelE = Transl.ins(Operator.NEXT,0);
			 Transl.modOperand(iJumpE, labelE);
			 int ifExpr2_t = ifExp(se);
			 FunctCalled fc2=null;
			 if ( isFunction(ifExpr2_t) )
				 fc2=currFunctCalled;
			 int labelT = Transl.ins(Operator.NEXT,0);
			 Transl.modOperand(iJumpT, labelT);
			 if ( ifExpr2_t == ifExpr1_t ) 
				 if ( isFunction(ifExpr1_t) && !FunctCalled.equiv(fc1,fc2) )
					 throw new Exc_Synt(ErrorType.WRONG_FTYPE, 
								Exec.types[ifExpr1_t]+" and "+Exec.types[ifExpr2_t]+
								"-- the two alternative functions for '?:' must have the same signature");
				 else 
					 return ifExpr1_t;
			 if ( isFunction(ifExpr1_t) || isFunction(ifExpr2_t) )
					throw new Exc_Synt(ErrorType.WRONG_TYPE, 
					Exec.types[ifExpr1_t]+" and "+Exec.types[ifExpr2_t]+
					"-- if an alternative for '?:' is a function, both alternatives must be functions");
			 if ( isLabel(ifExpr1_t) || isLabel(ifExpr2_t)  )
					throw new Exc_Synt(ErrorType.WRONG_TYPE, 
					Exec.types[ifExpr1_t]+" and "+Exec.types[ifExpr2_t]+
					"-- if an alternative for '?:' is a label, both alternatives must be labels");		 

			 return mostGeneralType(ifExpr1_t, ifExpr2_t);
		 }
		 else
			 return cexpr_t;
	}

	// <orExpr(se)> -> <andExpr> { OR <andExpr> }
	 static int orExpr ( boolean se ) throws Exc {
		 if ( inFirst(first_orExp) ) {
			 int cterm_t = andExpr(se);
			 if ( lex.currToken() == Token.OR ) { 
				 if ( !isBool(cterm_t) ) 
						throw new Exc_Synt(ErrorType.WRONG_TYPE, 
									Exec.types[cterm_t]+"- expected type: boolean");
				 int iJumpE = Transl.ins(Operator.JUMPNZ,0);
				 int labelE = orExpr_next(se);
				 Transl.modOperand(iJumpE, labelE);
				 return Exec.BoolT;
			 }
			 else
				 if ( inFirst(follows_orExp_next) )
					 return cterm_t;
				 else 
					 throw raiseExc(follows_orExp_next);
		 }
		 else 
			 throw raiseExc(first_orExp);
	}
	 
	 // method called by orExpr
	 static int orExpr_next(boolean se ) throws Exc {
		 lex.nextToken(); // skip token OR
		 int cterm_t = andExpr(se);
		 if ( !isBool(cterm_t) )
				throw new Exc_Synt(ErrorType.WRONG_TYPE, 
						Exec.types[cterm_t]+"- expected type: boolean");
		 int labelE;
		 if ( lex.currToken() == Token.OR ) { 
			 int iJumpE = Transl.ins(Operator.JUMPNZ,0);
			 labelE = orExpr_next(se);
			 Transl.modOperand(iJumpE, labelE);
		 }
		 else 
			 if (inFirst(follows_orExp_next)  ){
				 int iJ1 = Transl.ins(Operator.JUMP,0);
				 labelE =  Transl.ins(Operator.PUSHB,1);
				 Transl.ins(Operator.NEXT,0);
				 Transl.modOperand(iJ1, labelE+1);
			 }
			 else
				 throw raiseExc(follows_orExp_next);
		 return labelE;
	 }



	// <andExpr> ->  <eqExpr> { AND <eqExpr> }
	 static int andExpr ( boolean se ) throws Exc {
		 if ( inFirst(first_andExp) )  {  
			 int cfact_t = eqExpr(se);
			 if ( lex.currToken() == Token.AND ) { 
				 if ( !isBool(cfact_t) )
						throw new Exc_Synt(ErrorType.WRONG_TYPE, 
								Exec.types[cfact_t]+"- expected type: boolean");
				 int iJumpE = Transl.ins(Operator.JUMPZ,0);
				 int labelE = andExpr_next(se);
				 Transl.modOperand(iJumpE, labelE);
				 return Exec.BoolT;
			 }
			 else 
				 if ( inFirst(follows_andExp_next) )
						 return cfact_t;
				 else
					 throw raiseExc(follows_andExp_next);
		 }
		 else 
			 throw raiseExc(first_andExp);
	}
	 
	 // method called by andExpr
	 static int andExpr_next(boolean se ) throws Exc {
		 lex.nextToken();
		 int cfact_t = eqExpr(se);
		 if ( !isBool(cfact_t) )
				throw new Exc_Synt(ErrorType.WRONG_TYPE, 
						Exec.types[cfact_t]+"- expected type: boolean");
		 int labelE;
		 if ( lex.currToken() == Token.AND ) { 
			 int iJumpE = Transl.ins(Operator.JUMPZ,0);
			 labelE = andExpr_next(se);
			 Transl.modOperand(iJumpE, labelE);
		 }
		 else 
			 if ( inFirst(follows_andExp_next) ) {
				 int iJ1 = Transl.ins(Operator.JUMP,0);
				 labelE =  Transl.ins(Operator.PUSHB,0);
				 Transl.ins(Operator.NEXT,0);
				 Transl.modOperand(iJ1, labelE+1);
			 }
			 else
				 throw raiseExc(follows_andExp_next);
		 return labelE;
	 }

	//<eqExpr> -> <relExpr> { EQNEQ <relExpr> }
	 static int eqExpr ( boolean se ) throws Exc {
		 if ( inFirst(first_eqExp) ) { 
			 int expr1_t = relExpr(se);
			 while ( lex.currToken() == Token.EQNEQ ) {
				 EQNEQOpType relOP =lex.EQNEQ_Op(); 
				 String relOpDescr = lex.tokenFullDescr();
				 lex.nextToken();
				 int expr2_t = relExpr(se);
				 if ( !areEQNEQComparable(expr1_t, expr2_t) )
					 throw new Exc_Synt(ErrorType.WRONG_TYPE,
							 Exec.types[expr2_t]+"- it cannot be compared with "+ Exec.types[expr1_t]+
							 " by operator "+relOpDescr);
				 Transl.ins(relOP.relOp,0);
				 expr1_t = Exec.BoolT;
			 }
			return expr1_t;
		 }
		 else
			 throw raiseExc(first_eqExp);
	}
	 
		//<relExpr> -> <expr> { GTLT <expr> }
	 static int relExpr ( boolean se ) throws Exc {
		 if ( inFirst(first_eqExp) ) { 
			 int expr1_t = expr(se);
			 while ( lex.currToken() == Token.GTLT ) {
					 GTLTOpType relOP =lex.GTLT_Op(); String relOpDescr = lex.tokenFullDescr();
					 lex.nextToken();
					 int expr2_t = expr(se);
					 if ( !areRelComparable(expr1_t, expr2_t) )
						 throw new Exc_Synt(ErrorType.WRONG_TYPE,
								 Exec.types[expr2_t]+"- it cannot be compared with '"+ 
						         Exec.types[expr1_t]+ "' by operator "+relOpDescr);
					 Transl.ins(relOP.relOp,0);
					 expr1_t= Exec.BoolT;
			 }
			return expr1_t;
		 }
		 else
			 throw raiseExc(first_eqExp);
	}
	 
	//<expr> -> <term> ( { ( PLUS | MINUS ) <term> } | { PLUSPLUS <term> } )
	 static int expr ( boolean se ) throws Exc {
		 if ( inFirst(first_ifExp) ) { 
			 boolean firstWhile = true;
			 int term1_t = term(se);
			 if (lex.currToken() == Token.PLUSPLUS) 
				 while ( lex.currToken() == Token.PLUSPLUS ) {
					 if ( firstWhile ) {
						 if ( !isStringList(term1_t) )
							throw new Exc_Synt(ErrorType.WRONG_TYPE, 
									Exec.types[term1_t]+"- expected types: string or list");
						 if ( term1_t==Exec.GenT || term1_t==Exec.GsljT )
							 term1_t= Exec.GslT;
						 firstWhile=false;
					 	}
					 lex.nextToken();
					 int term2_t = term(se);
					 if ( !isStringList(term2_t) ) 
							throw new Exc_Synt(ErrorType.WRONG_TYPE, 
									Exec.types[term2_t]+"- expected types: string or list");
					 switch ( term1_t ) {
					 	case Exec.ListT: 
							 if ( term2_t == Exec.StringT )
									throw new Exc_Synt(ErrorType.WRONG_TYPE, 
											Exec.types[term2_t]+"- expected type: list");					 
					 		 break;
					 	case Exec.StringT: 
							 if ( term2_t == Exec.ListT )
									throw new Exc_Synt(ErrorType.WRONG_TYPE, 
											Exec.types[term2_t]+"- expected type: string");					 
					 		 break;
					 	case Exec.GslT: 
							 if ( term2_t==Exec.StringT  ) 
								 term1_t = Exec.StringT;
							 else
								 if ( term2_t==Exec.ListT  ) 
									 term1_t = Exec.ListT;
					 }
					 Transl.ins(Operator.ADDLS,0);
				 }
				 
			else 
			 while ( lex.currToken() == Token.PLUS ||  lex.currToken() == Token.MINUS  ) {
				 if ( firstWhile ) {
					 if ( !isNumber(term1_t) )
						throw new Exc_Synt(ErrorType.WRONG_TYPE, 
								Exec.types[term1_t]+"- expected types: double or long or int or char");
					 term1_t=term1_t==Exec.GenT? Exec.GnumT: term1_t;
					 firstWhile=false;
				 }
				 boolean isMINUS = lex.currToken() == Token.MINUS;
				 lex.nextToken();
				 int term2_t = term(se);
				 if ( !isNumber(term2_t) )
						throw new Exc_Synt(ErrorType.WRONG_TYPE, 
								Exec.types[term2_t]+"- expected type: double or long or int or char");
				 term2_t=term2_t==Exec.GenT? Exec.GnumT: term2_t;
				 term1_t = mostGeneralNumberType(term1_t,term2_t);
				 if ( isMINUS )
					 Transl.ins(Operator.SUB,0);
				 else
					 Transl.ins(Operator.ADD,0);
			 }
			return term1_t;
		 }
		 else
			 throw raiseExc(first_ifExp);
	}
	 
	//<term(se)> -> <uniTerm(se)> { ( STAR | DIV | DIVTR | REMINDER) <uniTerm(se)> }
	 static int term (  boolean se ) throws Exc {
		 if ( inFirst(first_term) ) { 
			 int uniTerm1_t = uniTerm(se);
			 boolean firstWhile = true;
			 while ( lex.currToken() == Token.STAR ||  lex.currToken() == Token.DIV  
					 			||  lex.currToken() == Token.DIVTR ||  lex.currToken() == Token.REMAINDER) {
				 if ( firstWhile  ) {
					 if ( uniTerm1_t!=Exec.GenT && !isNumber(uniTerm1_t) )
							throw new Exc_Synt(ErrorType.WRONG_TYPE, 
									Exec.types[uniTerm1_t]+"- expected type: double or long or int or char");
					 uniTerm1_t=uniTerm1_t==Exec.GenT? Exec.GnumT: uniTerm1_t;
				 }
				 else
					 firstWhile=false;
				 int opType = lex.currToken() == Token.STAR ? 1: lex.currToken() == Token.REMAINDER ? 2: 
					 lex.currToken() == Token.DIV ? 3 : 4;	 
				 lex.nextToken();
				 int uniTerm2_t = uniTerm(se);
				 if ( uniTerm2_t!=Exec.GenT && !isNumber(uniTerm2_t) )
						throw new Exc_Synt(ErrorType.WRONG_TYPE, 
								Exec.types[uniTerm2_t]+"- expected type: double or long or int or char");
				 uniTerm2_t=uniTerm2_t==Exec.GenT? Exec.GnumT: uniTerm2_t;				 
				 switch (opType) {
					 case(1): 
						 Transl.ins(Operator.MULT,0);
						 uniTerm1_t=mostGeneralNumberType(uniTerm1_t,uniTerm2_t);
						 break;
					 case(2): 
						 Transl.ins(Operator.REMAINDER,0);
						 uniTerm1_t=mostGeneralNumberType(uniTerm1_t,uniTerm2_t);
						 break;
					 case(3):case(4): 
						 Transl.ins(Operator.DIV,0);
						 uniTerm1_t=Exec.DoubleT;
						 if ( opType == 4 ) { // operator '//'
							 Transl.ins(Operator.TOINT,0);
							 uniTerm1_t=Exec.IntT;
						 }				 
				 }
			 }
			 return uniTerm1_t;
		 }
		 else
			 throw raiseExc(first_term);
	}

		//<uniTerm> -> ( { PLUS | MINUS } | { NOT } ) <simpTerm> 
	 static int uniTerm ( boolean se ) throws Exc {
		 boolean isMINUS = false;
		 boolean mustBeNumber = false;
		 boolean mustBeBool = false; 
		 boolean isNOT = false;
		 while ( lex.currToken() == Token.PLUS || lex.currToken() == Token.MINUS
				 || lex.currToken() == Token.EMARK ) {
			 if ( lex.currToken() == Token.EMARK && mustBeNumber )
					throw new Exc_Synt(ErrorType.WRONG_UN_OP, 
							" after operator '+' or '-'");
			 if ( (lex.currToken() == Token.PLUS || lex.currToken() == Token.MINUS) 
					 && mustBeBool )
					throw new Exc_Synt(ErrorType.WRONG_UN_OP, 
							" after operator '!'");
			 if ( lex.currToken() == Token.PLUS || lex.currToken() == Token.MINUS ) {
				 mustBeNumber = true;
				 if ( lex.currToken() == Token.MINUS )
					 isMINUS = !isMINUS; 
				 lex.nextToken(); 
			 }
			 else
				 if ( lex.currToken() == Token.EMARK ) {
					 mustBeBool=true;
					 isNOT = !isNOT;
					 lex.nextToken();
				 }
		 }
 		 int currT = simpTerm(se);
		 if ( mustBeNumber ) {
			 if ( currT != Exec.GenT && !isNumber(currT) )
					throw new Exc_Synt(ErrorType.WRONG_TYPE, 
							Exec.types[currT]+"- expected type: double or long or int or char");
			 if ( isMINUS ) // dynamic type check performed by Operator.NEG
				 Transl.ins(Operator.NEG,0);
			 else
				 if ( currT == Exec.GenT ) // forced dynamic type check
					 Transl.ins(Operator.CHECKN,0);
			 currT=currT==Exec.GenT? Exec.GnumT: currT;
		 }
		 if ( mustBeBool ) {
			 if ( currT != Exec.BoolT && currT != Exec.GenT )
					throw new Exc_Synt(ErrorType.WRONG_TYPE, 
							Exec.types[currT]+"- expected type: boolean");
			 if ( isNOT )
				 Transl.ins(Operator.NOT, 0);
			 else
				 if ( currT == Exec.GenT ) // dynamic type check
					 Transl.ins(Operator.CHECKB,0);
			 currT=Exec.BoolT;
		 }	
		 return currT;
	 }

	//<simpTerm> -> <fact> { OPAR <actPars> CPAR }_2 { OSPAR <sliceElems> CSPAR | CAST TYPEC } 
	 static int simpTerm ( boolean se ) throws Exc {
	 	 if ( inFirst(first_simpleTerm) ) { 
	 		 int currT = fact(se); 
	 		 int i=2;
	 		 while ( lex.currToken()== Token.OPAR ) {
	 			 if ( i< 1 )
						throw new Exc_Synt(ErrorType.WRONG_CALL, 
								"- the second nested function call cannot return a function");
	 			 if ( !isFunction(currT) ) 
						throw new Exc_Synt(ErrorType.WRONG_TYPE, 
								Exec.types[currT]+"- expected type: function");
	 			 lex.nextToken();
	 			 currT= actPars(se);
	 			 lex.accept(Token.CPAR);
	 			 i--;
	 		 }
	 		 while ( lex.currToken() == Token.OSPAR  || lex.currToken() == Token.CAST ) {
	 			 if ( lex.currToken() == Token.OSPAR ) {
					 lex.nextToken();
//					 cannotBeJson=false;
					 currT =sliceElems(se,currT);
					 lex.accept(Token.CSPAR );
				 }
	 			 else {
						 lex.nextToken();
						 if ( lex.currToken() == Token.TYPE ) {
							 int castT = lex.currType();
							 Transl.ins(Operator.CAST,castT);
							 switch(castT) {
							 case Exec.DoubleT: case Exec.LongT: case Exec.CharT:
								 if ( currT==Exec.GenT || isNumber(currT)  ) {
									 	currT = castT;
								 }
								 else 
									 throw new Exc_Synt(ErrorType.WRONG_TYPE, 
										 Exec.types[currT]+" cannot be casted to "+Exec.types[castT]+" - expected types: int or long or double or char");
								 break;
							 case Exec.IntT: 
								 if ( currT==Exec.GenT || isNumber(currT) || currT==Exec.BoolT  ) {
									 	currT = castT;
								 }
								 else 
									 throw new Exc_Synt(ErrorType.WRONG_TYPE, 
										 Exec.types[currT]+" cannot be casted to int - expected types: int or long or double or char or bool");
								 break;
							 case Exec.StringT:
								 if ( currT==Exec.GenT || isStringList(currT)  ||
								 		currT==Exec.CharT ) {
									 currT = Exec.StringT;
								 }
								 else
									 throw new Exc_Synt(ErrorType.WRONG_TYPE, 
											 Exec.types[currT]+" cannot be casted to string - expected types: char or list of chars or string");
								 break;
							 case Exec.ListT:
								 if ( currT==Exec.GenT || isStringList(currT) || currT==Exec.JsonT ) {
									 currT = Exec.ListT;										 
								 }
								 else
									 throw new Exc_Synt(ErrorType.WRONG_TYPE, 
											Exec.types[currT]+" cannot be casted to list - expected types: string or json or list");											 
							 break;
							 case Exec.JsonT:
								 if ( currT==Exec.GenT || currT==Exec.ListT || currT==Exec.GslT ||
										 currT==Exec.JsonT ) {
									 currT = Exec.JsonT;										 
								 }
								 else
									 throw new Exc_Synt(ErrorType.WRONG_TYPE, 
											Exec.types[currT]+" cannot be casted to json - expected types: list or json");											 
								break;
							 case Exec.TypeT:
								 if ( currT != Exec.FuncT && currT != Exec.LabT) {
									 currT = Exec.TypeT;
								 }
								 else 
										throw new Exc_Synt(ErrorType.WRONG_TYPE,
												Exec.types[currT]+" cannot be casted to 'type' ");
								 break;
							default: // it should never happen
								throw new Exc_Synt(ErrorType.WRONG_TYPE, 
										"- cast to "+Exec.types[castT]+" not supported");
							 }
												 
							 lex.nextToken();					 
						 }
						 else
								throw new Exc_Synt(ErrorType.WRONG_TOKEN, lex.tokenFullDescr()+
										"- expected types: long or int or char or string or list or json or type");	
		 			 }
				 }
			 return currT;
	 	 }
	 	 else
	 		 throw raiseExc(first_simpleTerm);
	 }

	 // method called by fact
	 static void getString(String s ) throws Exc {
		 Transl.ins(Operator.SSTRING,0);
		 for ( int i =0; i <s.length(); i++ ) {
			 Transl.ins(Operator.PUSHC, s.charAt(i));
			 Transl.ins(Operator.CSTRING, 0 );
		 }
		 Transl.ins(Operator.ESTRING,0);
	 }

		//<fact> -> 	 ( IDL | ID ) | <callBuiltIn> | OSPAR [ <constListElems> ] CSPAR  |  
		//               OBRACE [ <jsonFields> ] CBRACE | <lambda> | OPAR  <ifExpr(se)>  CPAR | 
		// 				 INT | LONG | DOUBLE | CHAR | STRING | NULL | BOOL | TYPE
		 static int fact (  boolean se ) throws Exc {
			 // case ( IDL | ID )
			 if ( lex.currToken() == Token.ID || lex.currToken() == Token.IDL ) {
				 String idName = lex.IDName();
				 if ( SymbH.isLabel(idName) && !SymbH.is_FunctDef()&&!SymbH.isLambdaDef()) {
			 		 Transl.ins(Operator.PUSHLAB,SymbH.indLab(idName));	
					 lex.nextToken();
					 return Exec.LabT;
				 }
				 boolean hasLabel= lex.currToken()==Token.IDL;
				 String label =   hasLabel? lex.currLabel(): null;
				 if ( hasLabel && !SymbH.isLabel(label) )
						throw new Exc_Sem(ErrorType.UNDEF_LABEL, ": "+label);
				 int idType = SymbH.setCurrID(idName,se,hasLabel,label,setLabel,setLabelID);
				 if ( idType == SymbH.undefID )
					 throw new Exc_Synt(ErrorType.UNDEF_ID, " " + idName);
				 if ( SymbH.isIdNameLabeled()) 
					 History.replaceLast(idName, SymbH.idNameLabeled());
				 lex.nextToken();

				 if ( idType == SymbH.functType ) {
					 if ( SymbH.isParam() ) { // case of function parameter in a current function definition
						 int iPar = SymbH.iCurrVarFunc(); 
						 int nPar = SymbH.currFunctParam_NP(); 
						 int nParDefFunc = SymbH.nParCurrFunDef(); 
						if (!SymbH.isLambdaDef()) {
						 	Transl.ins(Operator.LOADPAR,nParDefFunc-iPar); 
						 	Transl.ins(Operator.DEREF,0);
						}
						else {
							 Transl.ins(Operator.LOADXPAR,iPar+1);
							 Transl.ins(Operator.DEREFX,0);						 							 							 							
						}
					 	currFunctCalled=new FunctCalled(idName,SymbH.isCurrFactIdSE(),iPar,nPar,0);
					 }
					 else { // case of function identifier 
						 int i = SymbH.iCurrVarFunc(); // function index
						 int j = SymbH.iCurrVarFuncTot(); // symbol index - j=-1 if it is the currently defined function that is recursively called
						 FunctInfo fi = SymbH.finfo(j); // function info
						 Transl.ins(Operator.PUSHF,i, "*-> "+idName);							 
						 currFunctCalled=new FunctCalled(idName,i,fi);	
					 }
					 return Exec.FuncT;
				 }

				 if (SymbH.isParam() ) { // case of variable parameter in a function definition
					 int iPar = SymbH.iCurrVarFunc(); // index of the parameter 
					 int nParTot = SymbH.nParCurrFunDef(); // for lambda definition, also includes external parameters
					 if (!SymbH.isLambdaDef()) {
						 Transl.ins(Operator.LOADPAR,nParTot-iPar);
						 Transl.ins(Operator.DEREF,0);						 
					 }
					 else {
						 int nParFunc=SymbH.numFuncParLambda();
						 if ( iPar>= nParFunc) { // proper lambda parameter
							 Transl.ins(Operator.LOADPAR,nParTot-iPar);
							 Transl.ins(Operator.DEREF,0);						 							 
						 }
						 else { // parameter of the function hosting the lambda definition
//***						 Transl.ins(Operator.LOADXPAR,nParFunc-iPar-1);
							 Transl.ins(Operator.LOADXPAR,iPar+1);
							 Transl.ins(Operator.DEREFX,0);						 							 							 
						 }
					 }
					 return Exec.GenT;
				 }
				 
				 // case of variable identifier
				 int retType;
				 if ( SymbH.isLocVar() ) { // local variable
					 retType=Exec.GenT;
					 Transl.ins(Operator.LOADLV,SymbH.iCurrVarFunc());
				 }
				 else { // global variable
					 if ( typeDef == isVarDef || typeDef == isQuery  ) 
						 // the variable type is known
						 retType = SymbH.typeGV(SymbH.iCurrVarFunc());
					 else
						 retType=Exec.GenT;
					 Transl.ins(Operator.LOADGV,SymbH.iCurrVarFunc());
				 }
				 Transl.ins(Operator.DEREF,0);
				 return retType; 
			 }
			 
			 // case <callBuiltIn>
			 if ( lex.currToken() == Token.BIFUNC )  
				 return callBuiltIn(se);
			 
			 // case OSPAR [ <constListElems> ] CSPAR
			 if ( lex.currToken() == Token.OSPAR ) {
					 Transl.ins(Operator.SLIST,0);
					 lex.nextToken();
					 if ( lex.currToken() != Token.CSPAR ) { 
						 boolean isApp = constListElems(se);
						 if ( !isApp ) // no end list if a list has been appended
							 Transl.ins(Operator.ELIST,0);
					 }
					 else
						 Transl.ins(Operator.ELIST,0);
					 lex.accept(Token.CSPAR );
					 return Exec.ListT;
			 }
			 
			 // case OBRACE [ <fields> ] CBRACE
			 if ( lex.currToken() == Token.OBRACE ) {
				 Transl.ins(Operator.SJSON,0);
				 lex.nextToken();
				 if ( lex.currToken() != Token.CBRACE  ) 
					 jsonFields(se);
				 Transl.ins(Operator.EJSON,0);
				 lex.accept(Token.CBRACE );
				 return Exec.JsonT;
			 }
			 
			 // case <lambda> 
			 if ( lex.currToken() == Token.LAMBDA ) {
				  lambda(); 
				  return Exec.FuncT;
			 }
			 
			 // case OPAR  <ifExpr(se)>  CPAR
			 if ( lex.currToken() == Token.OPAR ) {
				 lex.nextToken();
				 int retType = ifExp(se);
				 lex.accept(Token.CPAR);
				 return retType;
			 }				 
			 
			 // case DOUBLE
			 if ( lex.currToken() == Token.DOUBLE ) {
					 Transl.ins(Operator.PUSHD,lex.doubleVal());
					 lex.nextToken();
					 return Exec.DoubleT;
			 }
			 
			 // case INT
			 if ( lex.currToken() == Token.INT ) {
				 Transl.ins(Operator.PUSHI,lex.intVal());
				 lex.nextToken();
				 return Exec.IntT;
			 }
			 
			 // case LONG
			 if ( lex.currToken() == Token.LONG ) {
				 Transl.ins(Operator.PUSHL,Double.longBitsToDouble(lex.longVal()));
				 lex.nextToken();
				 return Exec.LongT;
			 }
			 
			 // case CHAR
			 if ( lex.currToken() == Token.CHAR ) {
					 Transl.ins(Operator.PUSHC,lex.CHARvalue());
					 lex.nextToken();
					 return Exec.CharT;
			 }
			 
			 // case STRING
			 if ( lex.currToken() == Token.STRING ) {
					 getString(lex.stringVal());
					 if ( lex.isStringOverflow() )
						 ioh.print("\n**Warning: string truncated to max length "+Lex.maxLenString+"\n");
					 lex.nextToken();
					 return Exec.StringT;
			 }
			 
			 // case NULL
			 if ( lex.currToken() == Token.NULL ) {
				 Transl.ins(Operator.PUSHN,0);
				 lex.nextToken();
				 return Exec.NullT;
			 }
			 
			 // case BOOL (true or false)
			 if ( lex.currToken() == Token.BOOL ) {
				 if( lex.valBool() )
					 Transl.ins(Operator.PUSHB,1);
				 else
					 Transl.ins(Operator.PUSHB,0);
					 lex.nextToken();
					 return Exec.BoolT;
			 }
			 
			 // case TYPE
			 if ( lex.currToken() == Token.TYPE ) {
					 Transl.ins(Operator.PUSHT,lex.currType());
					 lex.nextToken();
					 return Exec.TypeT;
			 }
			 
			 // ERROR: none of the above cases
			throw raiseExc(first_fact); 
		}
		 
	 // <jsonFields> -> <ifExpr> COLON <ifExpr> { COMMA <ifExpr> COLON <ifExpr> }
	 static void jsonFields(boolean se) throws Exc {
			if ( inFirst(first_fields) ) { 
				int type = ifExp(se);
				if ( type != Exec.GenT && type != Exec.StringT )
					throw new Exc_Synt(ErrorType.WRONG_TYPE, 
							Exec.types[type]+"- expected type: string");
				lex.printH_Colon(true); // print spaced COLON on history
				lex.accept(Token.COLON);
				int ifExpr_t=ifExp(se);
				if ( ifExpr_t == Exec.LabT || ifExpr_t == Exec.FuncT )
					throw new Exc_Synt(ErrorType.WRONG_TYPE, "a field of type "+
							Exec.types[ifExpr_t]+" cannot occur in a json");
				Transl.ins(Operator.NEWFLD,0);
				Transl.ins(Operator.CJSON,0);
				while ( lex.currToken() == Token.COMMA || lex.currToken() != Token.CBRACE ) {
					if ( lex.currToken() == Token.COMMA )  {
						lex.printH_Comma(true); // print space after comma in history
						lex.nextToken();
					}
					type = ifExp(se);
					if ( type != Exec.GenT && type != Exec.StringT )
						throw new Exc_Synt(ErrorType.WRONG_TYPE, 
								Exec.types[type]+"- expected type: string");
					lex.printH_Colon(true); // print spaced COLON on history
					lex.accept(Token.COLON);
					ifExpr_t=ifExp(se);
					if ( ifExpr_t == Exec.LabT || ifExpr_t == Exec.FuncT )
						throw new Exc_Synt(ErrorType.WRONG_TYPE, "a field of type "+
								Exec.types[ifExpr_t]+" cannot occur in a json");
					Transl.ins(Operator.NEWFLD,0);
					Transl.ins(Operator.CJSON,0);
				}
			}
			 else
				 throw raiseExc(first_fields); 
	 
	 }
	 

	    //<callBuiltIn> -> ( _ID(EXP | LOG | LEN | TUPLE) OPAR <ifExpr> CPAR | ID(RAND) OPAR CPAR |
		//               _ID(POW | ISKEY) OPAR <ifExpr>  COMMA <ifExpr> CPAR | 
		//				 _ID(IND) OPAR <ifExpr>  COMMA <ifExpr> [COMMA <ifExpr>] CPAR |
	 	//				 _ID(GDATE) OPAR [ <ifExpr> COMMA <ifExpr> ] CPAR
	 	//				 _ID(PDATE) OPAR <ifExpr> [ COMMA <ifExpr> ] CPAR
	 static int callBuiltIn ( boolean se ) throws Exc{
//		 if ( lex.currToken() != Token.ID )
//				throw new Exc_Synt(ErrorType.WRONG_TOKEN, lex.tokenFullDescr()+
//						"- expected: 'exp' or 'log' or 'pow' or 'len' or 'rand' or 'ind' or 'tuple' or 'isKey' or 'pDate' or 'gDate'");						 
		 BIFType builtinF = lex.BIF_type();
		 int operand=0; int ifExp_t; int retType=-1;
		 lex.nextToken();
		 lex.accept(Token.OPAR);
		 switch(builtinF) {
		 case RAND: retType=Exec.DoubleT; break;
		 case POW: case LOG: case EXP: ifExp_t= ifExp(se); 
 			 if ( !isNumber(ifExp_t) && ifExp_t!=Exec.GenT )
				throw new Exc_Synt(ErrorType.WRONG_TYPE, 
						Exec.types[ifExp_t]+"- expected type: double or long or int or char");
 			 if (builtinF==BIFType.POW) {
 				 lex.printH_Comma(false); // no space after comma in history
				 lex.accept(Token.COMMA); ifExp_t= ifExp(se);
				 if ( !isNumber(ifExp_t) && ifExp_t!=Exec.GenT )
						throw new Exc_Synt(ErrorType.WRONG_TYPE, 
								Exec.types[ifExp_t]+"- expected type: double or long or int or char"); 				 
 			 }
 			retType=Exec.DoubleT; break;
		 case LEN: ifExp_t= ifExp(se);
			 if ( ifExp_t!=Exec.ListT && ifExp_t!=Exec.StringT && ifExp_t!=Exec.JsonT && ifExp_t!=Exec.GenT )
					throw new Exc_Synt(ErrorType.WRONG_TYPE, 
							Exec.types[ifExp_t]+"- expected type: string or list or json");
	 			retType=Exec.IntT; break;
		 case TUPLE: ifExp_t= ifExp(se);  
			 if ( ifExp_t!=Exec.JsonT && ifExp_t!=Exec.GenT && ifExp_t!=Exec.LongT )
					throw new Exc_Synt(ErrorType.WRONG_TYPE, 
							Exec.types[ifExp_t]+"- expected type: json or long");						 
	 			retType=Exec.ListT; break;
		 case ISKEY: ifExp_t= ifExp(se);
			 if ( ifExp_t!=Exec.JsonT && ifExp_t!=Exec.GenT )
					throw new Exc_Synt(ErrorType.WRONG_TYPE, 
							Exec.types[ifExp_t]+"- expected type: json");
			 lex.printH_Comma(false); // no space after comma in history
			 lex.accept(Token.COMMA);
			 ifExp_t = ifExp(se);
			 if ( ifExp_t!=Exec.StringT && ifExp_t!=Exec.GenT )
						throw new Exc_Synt(ErrorType.WRONG_TYPE, 
								Exec.types[ifExp_t]+"- expected type: string");
	 			retType=Exec.BoolT; break;
		 case IND: ifExp_t= ifExp(se);  
			 if ( ifExp_t!=Exec.StringT && ifExp_t!=Exec.GenT )
					throw new Exc_Synt(ErrorType.WRONG_TYPE, 
							Exec.types[ifExp_t]+"- expected type: string");
			 lex.printH_Comma(false); // no space after comma in history
			 lex.accept(Token.COMMA);
			 ifExp_t = ifExp(se);
			 if ( ifExp_t!=Exec.StringT && ifExp_t!=Exec.GenT )
						throw new Exc_Synt(ErrorType.WRONG_TYPE, 
								Exec.types[ifExp_t]+"- expected type: string");
			 if (lex.currToken()==Token.COMMA ) {
				 lex.printH_Comma(false); // no space after comma in history
				 lex.nextToken();
				 ifExp_t = ifExp(se);
				 if ( !isIntegerOrChar(ifExp_t) && ifExp_t!=Exec.GenT )
							throw new Exc_Synt(ErrorType.WRONG_TYPE, 
									Exec.types[ifExp_t]+"- expected type: int or char");						 
			 }
			 else 
				 Transl.ins(Operator.PUSHI,0);			 
	 		retType=Exec.IntT; break;
		 case GDATE:
			 if ( lex.currToken()!=Token.CPAR ) {
				 ifExp_t = ifExp(se);
				 if ( ifExp_t!=Exec.StringT && ifExp_t!=Exec.GenT )
							throw new Exc_Synt(ErrorType.WRONG_TYPE, 
									Exec.types[ifExp_t]+"- expected type: string");
 				 lex.printH_Comma(false); // no space after comma in history
				 lex.accept(Token.COMMA);
				 ifExp_t = ifExp(se);
				 if ( ifExp_t!=Exec.StringT && ifExp_t!=Exec.GenT )
						throw new Exc_Synt(ErrorType.WRONG_TYPE, 
								Exec.types[ifExp_t]+"- expected type: string");
				 operand=1;
			 }			 						 
	 		retType=Exec.LongT; break;
		 case PDATE: ifExp_t= ifExp(se);
			 if ( ifExp_t!=Exec.LongT && ifExp_t!=Exec.GenT )
					throw new Exc_Synt(ErrorType.WRONG_TYPE, 
							Exec.types[ifExp_t]+"- expected type: long");
			 if ( lex.currToken()!=Token.CPAR ) {
				 lex.printH_Comma(false); // no space after comma in history
				 lex.accept(Token.COMMA);
				 ifExp_t = ifExp(se);
				 if ( ifExp_t!=Exec.StringT && ifExp_t!=Exec.GenT )
						throw new Exc_Synt(ErrorType.WRONG_TYPE, 
								Exec.types[ifExp_t]+"- expected type: string");
				 operand=1;
			 }
			 retType=Exec.StringT;
		 };
		 lex.accept(Token.CPAR);
		 Transl.ins(builtinF.biOp, operand);
		 return retType;	 
	 }

		// <actPars> -> [ <ifExpr>  { COMMA <ifExpr> } ]
	 static int actPars (  boolean se ) throws Exc {
		 FunctCalled fc = new FunctCalled(currFunctCalled);
		 if ( !se && fc.hasSideEffect )
			 throw new Exc_Synt(ErrorType.WRONG_SIDE_EFFECT, " " + fc.ID);			 
		 Transl.ins(Operator.STOREF, 0); // store the id of the function to be eventually called 
		 int iPar = 0;
		 if ( inFirst(first_callFunct) ) {
			 boolean first = true;
			 do {
				 if ( ! first ) {
					 lex.printH_Comma(false); // no space after comma in history
					 lex.nextToken();
				 }
			 	 if ( iPar == fc.numParams )
			 		 throw new Exc_Synt(ErrorType.WRONG_ACT_PAR_NUM, 
							"- expected number:"+fc.numParams);
				 int formPar_t = fc.pTs.get(iPar);
				 int ifExpr1_t = ifExp(se);
				 if ( isLabel(ifExpr1_t))
						throw new Exc_Synt(ErrorType.WRONG_ACT_PARAM, 
								"- a label cannot be an actual paramater");
				 if ( isFunction(ifExpr1_t) && formPar_t != SymbH.functType )
						throw new Exc_Synt(ErrorType.WRONG_ACT_PARAM, 
								"- expected variable type");
				 if ( !isFunction(ifExpr1_t) && formPar_t == SymbH.functType )
						throw new Exc_Synt(ErrorType.WRONG_ACT_PARAM, 
								"- expected function type");
				 if ( formPar_t == SymbH.functType  ) { // static check on function parameters
					 FunctCalled cfc = currFunctCalled;
					 if ( cfc.numParams != fc.pFn.get(iPar) )
							throw new Exc_Synt(ErrorType.WRONG_ACT_PARAM, 
							"- actual and formal function parameters have different arity");
					 if ( cfc.hasFunctParams )
							throw new Exc_Synt(ErrorType.WRONG_ACT_PARAM, 
									" an actual parameter function cannot have function parameters");
					 if ( cfc.retT == SymbH.functType  )
							throw new Exc_Synt(ErrorType.WRONG_ACT_PARAM, 
									" an actual parameter function cannot return a function");
					 if ( !fc.hasSideEffect && cfc.hasSideEffect )
								throw new Exc_Synt(ErrorType.WRONG_ACT_PARAM, 
										" the actual parameter function cannot have side effects");
				 }
				 iPar++; 
				 first = false;
			 } while ( lex.currToken() == Token.COMMA );
		 }
		 if ( iPar != fc.numParams )
				throw new Exc_Synt(ErrorType.WRONG_ACT_PAR_NUM, 
						iPar+"- expected number: "+fc.numParams);
		 Transl.ins(Operator.GETF,0);
		 Transl.ins(Operator.CALL,0,"*-> "+fc.ID);		 
		 if ( fc.retT == SymbH.functType) {
			 currFunctCalled = new FunctCalled("function returned by "+fc.ID,fc.hasSideEffect,0,fc.retFn,0); 
			 return Exec.FuncT;
		 }
		 else
			 return Exec.GenT;
	}
	 
	// <lambda> -> LAMBDA [ ID { COMMA ID }  ] MAP <ifExpr>
	static void lambda ( ) throws Exc {
		 iStartLambda = History.nCurrComm()-1;
		 lex.nextToken(); // skip LAMBDA
		 lex.accept(Token.OPAR);
		 SymbH.startLambda();
		 int k=SymbH.nLambda()+1;
	 	 int indSL=Transl.ins(Operator.START,-k,"* Lambda "+k);
	 	 Transl.ins(Operator.GETXPAR,0); // set the pointer to possible function parameter array
	 	 int nLP = 0;
		 if ( lex.currToken() != Token.CPAR ) {
			 if ( lex.currToken() == Token.ID ) {
				 String idN = lex.IDName();
				 lex.nextToken();
				 SymbH.addParLambda(idN); 
			 }
			 else
					throw new Exc_Sem(ErrorType.WRONG_FUNCT_DEF, 
							"-- expected ID for lambda function parameter - found " + lex.tokenFullDescr());					 
			 nLP++;
			 while ( lex.currToken() == Token.COMMA ) {
				 lex.printH_Comma(false); // print space after comma in history
				 lex.nextToken();
				 if ( lex.currToken() == Token.ID ) {
					 String idN = lex.IDName();
					 lex.nextToken();
					 SymbH.addParLambda(idN); 
				 }
				 else
						throw new Exc_Sem(ErrorType.WRONG_FUNCT_DEF, 
								"-- expected ID for lambda function parameter - found " + lex.tokenFullDescr());					 
				 nLP++;
			 }
		 } 	
		 lex.nextToken(); // skip CPAR
		 lex.accept(Token.MAP);
		 // check equivalence with formal parameter
		 int retType = ifExp(false); // no side effects
	 	 if ( isFunction(retType) )
     	 	throw new Exc_Synt(ErrorType.WRONG_RET_TYPE, ": a lambda function cannot return a function");
	 	 Transl.ins(Operator.RETURN,SymbH.nParLambdaFdef());
	 	 int nCurrComm= History.currCommand().length();
	 	 String lambdaSource;
	 	 if (lex.currToken()==Token.COLON || lex.currToken()==Token.COMMA ) // COLON and COMMA are printed later in the history
	 		lambdaSource=History.currCommand().substring(iStartLambda,nCurrComm);
	 	 else
		 		lambdaSource=History.currCommand().substring(iStartLambda,nCurrComm-1);
	 	 SymbH.endLambaFdef(Transl.extractLambda(indSL),lambdaSource);
	 	 String kLambda="*-> Lambda "+k;
		 Transl.ins(Operator.PUSHF,-k, kLambda);	
	 	 if ( SymbH.useFunctParams() ) {
		 	Transl.ins(Operator.STOREXPAR, SymbH.nParFdef());
	 		Transl.ins(Operator.LWRAP,-k, "*->"+kLambda);
	 	 }
	 	 currFunctCalled = new FunctCalled(kLambda,false,0,nLP,0); 
		 return;
	}
	
	// <constListElems> ->  <ifExpr> { COMMA <ifExpr> } [ PIPE <ifExpr> ]
	 static boolean constListElems ( boolean se ) throws Exc {
		if ( inFirst(first_constListElems) ) { 
			boolean isApp = false;
			int ifExpr1_t=ifExp(se);
			if ( ifExpr1_t == Exec.LabT || ifExpr1_t == Exec.FuncT )
				throw new Exc_Synt(ErrorType.WRONG_TYPE, "an element of type "+
						Exec.types[ifExpr1_t]+" cannot occur in a list");
			Transl.ins(Operator.HLIST,0);
			while ( lex.currToken() == Token.COMMA || ( lex.currToken() != Token.PIPE && 
					lex.currToken() != Token.CSPAR ) ) {
				if ( lex.currToken() == Token.COMMA ) {
					lex.printH_Comma(true); // print space after comma in history
					lex.nextToken();
				}
				int ifExpr2_t=ifExp(se);
				if ( ifExpr2_t == Exec.LabT || ifExpr2_t == Exec.FuncT )
					throw new Exc_Synt(ErrorType.WRONG_TYPE, "an element of type "+
							Exec.types[ifExpr2_t]+" cannot occur in a list");
				Transl.ins(Operator.CLIST,0);
			}
			if ( lex.currToken() == Token.PIPE ) {
				isApp = true;
				lex.nextToken();
				int ifExpr3_t = ifExp(se);
				if ( ifExpr3_t != Exec.ListT && ifExpr3_t != Exec.GenT )
					throw new Exc_Synt(ErrorType.WRONG_TYPE, 
							Exec.types[ifExpr3_t]+"- expected type: list");
				Transl.ins(Operator.ALIST,0);
			}
			return isApp;
		}
		 else
			 throw raiseExc(first_constListElems); 
	}

	// <sliceElems> ->  DOT | GTLT(GT) [ <ifExpr> ] | <ifExpr> [ COLON [ <ifExpr> ] ] | 
	// 									COLON [ <ifExpr> ]
	 static int sliceElems ( boolean se, int currT) throws Exc {
		 if ( !isStringListJson(currT) )
				throw new Exc_Synt(ErrorType.WRONG_TYPE, 
						Exec.types[currT]+"- expected types: list or string or json");
		 if ( lex.currToken() == Token.DOT ) { // A[.] - head of a list
				if ( currT != Exec.ListT && currT != Exec.GslT && currT != Exec.GenT )
					throw new Exc_Synt(ErrorType.WRONG_TYPE, 
							Exec.types[currT]+" - expected type: list");
				 lex.nextToken();
				 Transl.ins(Operator.HEAD,0);
				 return Exec.GenT;
		 }
		 if ( lex.currToken() == Token.GTLT ) // A[>] or A[>i] - tail or subtail of a list
			 if ( lex.GTLT_Op() != GTLTOpType.GT )
				throw new Exc_Synt(ErrorType.WRONG_TOKEN, 
						lex.currTokenDescr() +"- expected '>'");
			 else {
				 if ( !isList(currT) )
						throw new Exc_Synt(ErrorType.WRONG_TYPE, 
								Exec.types[currT]+" - expected type: list");
				 lex.nextToken();
				 if ( lex.currToken() == Token.CSPAR )
					 Transl.ins(Operator.TAIL,0); // case A[>]
				 else {
					 int ifExpr_t = ifExp(se);
					 if ( !isIntegerOrChar(ifExpr_t) )
							throw new Exc_Synt(ErrorType.WRONG_TYPE, 
									Exec.types[ifExpr_t]+" - expected type: integer or char");
					 Transl.ins(Operator.LISTEL,0); // case A[>i]		
					 Transl.ins(Operator.TAIL,0); //
				 }				 
				 return Exec.ListT;
			 }
		 if ( lex.currToken() == Token.COLON ) { // A[:] or A[:i]
		 	 lex.printH_Colon(false); // print non-spaced COLON on history
			 lex.nextToken();
			 if ( lex.currToken() == Token.CSPAR )  // A[:]
				 return cloneSlice(currT);
			 else { // A[:i]
				 if ( currT == Exec.JsonT )
						throw new Exc_Synt(ErrorType.WRONG_TYPE, 
								"json - expected type: list or string");
				 Transl.ins(Operator.PUSHI,0);
				 int ifExpr1_t = ifExp(se);
				 if ( !isIntegerOrChar(ifExpr1_t) )
						throw new Exc_Synt(ErrorType.WRONG_TYPE, 
								Exec.types[ifExpr1_t]+"- expected types: int or char");			 
				 return subStringListSlice(currT,1);
			 }
	     }
		 if ( inFirst(first_ifExp) ) { // A[i] or A[i:] or A[i:j]
					 int ifExpr2_t = ifExp(se);
					 if ( lex.currToken() == Token.CSPAR ) // A[i]
						 return elemStringListJsonSlice (currT,ifExpr2_t );
					 else {
						 if ( currT == Exec.JsonT )
								throw new Exc_Synt(ErrorType.WRONG_TYPE, 
										"json - expected type: list or string");
						 if ( !isIntegerOrChar(ifExpr2_t) )
								throw new Exc_Synt(ErrorType.WRONG_TYPE, 
										Exec.types[ifExpr2_t]+" - expected type: integer or char");
						 lex.printH_Colon(false); // print non-spaced COLON on history
						 lex.accept(Token.COLON);
						 if ( lex.currToken() == Token.CSPAR ) // A[i:]
							 return subStringListSlice (currT,0 );
						 else { // A[i:j]
							 int ifExpr3_t = ifExp(se);
							 if ( !isIntegerOrChar(ifExpr3_t) )
									throw new Exc_Synt(ErrorType.WRONG_TYPE, 
											Exec.types[ifExpr3_t]+" - expected type: integer or char");
							 return subStringListSlice(currT,1);
						 }
					 }
				 }
		 throw raiseExc(first_listElems); 
	}

	 // method called by <sliceElems>
	 static int elemStringListJsonSlice ( int currT, int exprT ) throws Exc  { // A[i]
		switch(currT) {
		case Exec.JsonT:
			if ( isString(exprT) ) {
				 Transl.ins(Operator.FLDFIND,0);
				 Transl.ins(Operator.FLDVAL,0);
				 return Exec.GenT;
			 }
			 else
				throw new Exc_Synt(ErrorType.WRONG_TYPE, 
							Exec.types[exprT]+"- expected type: string");
		case Exec.ListT:
			 if ( isIntegerOrChar(exprT) ) {
				 Transl.ins(Operator.LISTEL,0);
				 Transl.ins(Operator.HEAD,0);
				 return Exec.GenT;
			 }
			 else
				throw new Exc_Synt(ErrorType.WRONG_TYPE, 
							Exec.types[exprT]+"- expected types: int or char");
		case Exec.StringT:
			 if ( isIntegerOrChar(exprT) ) {
				 Transl.ins(Operator.STRINGEL,0);
				 return Exec.CharT;
			 }
			 else
				throw new Exc_Synt(ErrorType.WRONG_TYPE, 
							 Exec.types[exprT]+"- expected types: int or char or string");
		case Exec.GslT:
			if ( isIntegerOrChar(exprT) ) {
				 Transl.ins(Operator.LSELV,0); 
				 return Exec.GenT;	
			}
			else
				throw new Exc_Synt(ErrorType.WRONG_TYPE, 
						Exec.types[exprT]+"- expected types: int or char");
		case Exec.GsljT: case Exec.GenT:
			if ( isIntegerOrChar(exprT) && !isString(exprT) ) {
				 Transl.ins(Operator.LSELV,0); 
				 return Exec.GenT;			 
			}
			else
				if ( isString(exprT) && !isIntegerOrChar(exprT) ) {
					 Transl.ins(Operator.FLDFIND,0);
					 Transl.ins(Operator.FLDVAL,0);
					 return Exec.GenT;					
				}
				else 
					if ( exprT==Exec.GenT ) {
						 Transl.ins(Operator.LSJELV,0);
						 return Exec.GenT;					
					}
					else
						throw new Exc_Synt(ErrorType.WRONG_TYPE, 
								 Exec.types[exprT]+"- expected types: int or char or string");
		}
		throw new Exc_Synt(ErrorType.WRONG_TYPE, 
				Exec.types[currT]+"- expected types: list or string or json");		 
	 }
	 
//	 // method called by <sliceElems>
//	 static int checkElement1 ( int currT, int exprT ) throws Exc  { // A[i]
//		 if ( currT == Exec.JsonT ) 
//			 if ( exprT==Exec.StringT || exprT==Exec.GenT ) {
//				 Transl.ins(Operator.FLDFIND,0);
//				 Transl.ins(Operator.FLDVAL,0);
//				 return Exec.GenT;
//			 }
//			 else
//				throw new Exc_Synt(ErrorType.WRONG_TYPE, 
//							Exec.types[exprT]+"- expected type: string");
//		 if ( currT == Exec.ListT ) 
//			 if ( isIntegerOrChar(exprT) || exprT==Exec.GenT ) {
//				 Transl.ins(Operator.LISTEL,0);
//				 Transl.ins(Operator.HEAD,0);
//				 cannotBeJson=false;
//				 return Exec.GenT;
//			 }
//			 else
//				throw new Exc_Synt(ErrorType.WRONG_TYPE, 
//							Exec.types[exprT]+"- expected types: int or char");
//		 if ( currT == Exec.StringT ) {
//			 if ( isIntegerOrChar(exprT) || exprT == Exec.GenT ) {
//				 Transl.ins(Operator.STRINGEL,0);
//				 return Exec.CharT;
//			 }
//			 else
//				throw new Exc_Synt(ErrorType.WRONG_TYPE, 
//							 Exec.types[exprT]+"- expected types: int or char or string");
//		 }
//		 // currT is now equal to NullT
//		 if ( cannotBeJson && exprT==Exec.StringT )
//			 throw new Exc_Synt(ErrorType.WRONG_TYPE, 
//					 Exec.types[exprT]+"list or string - expected type: json");	 			 
//		 if ( isIntegerOrChar(exprT) ) {
//			 Transl.ins(Operator.LSELV,0); 
//			 return Exec.GenT;
//		 }
//		 else
//			 if ( exprT==Exec.StringT ) {
//				 Transl.ins(Operator.FLDFIND,0);
//				 Transl.ins(Operator.FLDVAL,0);
//				 return Exec.GenT;
//			 }
//			 else			 
//				 if ( exprT == Exec.GenT ) {
//					 Transl.ins(Operator.LSJELV,0);
//					 return Exec.GenT;
//				 }
//				 else
//					 throw new Exc_Synt(ErrorType.WRONG_TYPE, 
//									 Exec.types[exprT]+"- expected types: int or char or string");	 
//	 }
//
//	 // method called by <sliceElems>
//	 static int checkElement2 ( int currT, int exprT ) throws Exc  { // A[i:]
//		 if ( currT == Exec.ListT ) 
//			 if ( isIntegerOrChar(exprT) || exprT==Exec.GenT ) {
//				 Transl.ins(Operator.LCLONE,0);
//				 return Exec.ListT;
//			 }
//			 else
//				throw new Exc_Synt(ErrorType.WRONG_TYPE, 
//							Exec.types[exprT]+"- expected types: int or char");
//		 if ( currT == Exec.StringT ) {
//			 if ( isIntegerOrChar(exprT) || exprT == Exec.GenT ) {
//				 Transl.ins(Operator.SUBSTR,0);
//				 return Exec.StringT;
//			 }
//			 else
//				 throw new Exc_Synt(ErrorType.WRONG_TYPE, 
//								 Exec.types[exprT]+"- expected types: int or char");
//		 }
//		 // currT is now equal to NullT
//		 if ( isIntegerOrChar(exprT) || exprT == Exec.GenT ) {
//			 Transl.ins(Operator.LSCLONE,0);
//			 cannotBeJson=true;
//			 return Exec.GenT;
//		 }
//		 else
//			 throw new Exc_Synt(ErrorType.WRONG_TYPE, 
//							 Exec.types[exprT]+"- expected types: int or char");	 
//	 }

	 // method called by <sliceElems> [:i]
	 static int cloneSlice ( int currT ) throws Exc  { // A[:]
		 switch(currT) {
		 case Exec.ListT: 
			 Transl.ins(Operator.PUSHI,0);
			 Transl.ins(Operator.LCLONE,0); 
			 return Exec.ListT;	
		 case Exec.StringT: 
			 return Exec.StringT; // string are not cloned
		 case Exec.JsonT: 
			 Transl.ins(Operator.JCLONE,0);
			 return Exec.JsonT;	
		 case Exec.GslT:
			 Transl.ins(Operator.LSJCLONE,0);
			 return Exec.GslT;
		 case Exec.GsljT: case Exec.GenT:
			 Transl.ins(Operator.LSJCLONE,0);
			 return Exec.GsljT;			 
		 } 
		throw new Exc_Synt(ErrorType.WRONG_TYPE, 
					Exec.types[currT]+"- expected types: list or string or json");		 
	 }
	

	 // method called by <sliceElems> [:i]
	 static int subStringListSlice ( int currT, int kk ) throws Exc  { // A[:j] or A[i:j]
		 if ( currT == Exec.ListT ) {
			 Transl.ins(Operator.LCLONE,kk);
			 return Exec.ListT;
		 }
		 else 
			 if (currT == Exec.StringT  ) {
				 Transl.ins(Operator.SUBSTR,kk);
				 return Exec.StringT;
			 }
			 else { // currT is  Exec.GenT or Exec.GsljT or Exec.GslT
				 Transl.ins(Operator.LSCLONE,kk);
				 return Exec.GslT;
			 }
	 }
	
	 // method called by <sliceElems> [:i]
	 static int subStringListSlice_i_end ( int currT ) throws Exc  { // A[:i]
		 if ( currT == Exec.ListT ) {
			 Transl.ins(Operator.LCLONE,0);
			 return Exec.ListT;
		 }
		 else 
			 if (currT == Exec.StringT  ) {
				 Transl.ins(Operator.SUBSTR,1);
				 return Exec.StringT;
			 }
			 else { // currT is  Exec.GenT or Exec.GsljT or Exec.GslT
				 Transl.ins(Operator.LSCLONE,1);
				 return Exec.GslT;
			 }
	 }

//	 // method called by <sliceElems>
//	 static int checkElement3 ( int currT, int exprT1, int exprT2 ) throws Exc  { // A[i:j]
//		 if ( currT == Exec.ListT ) 
//			 if ( isIntegerOrChar(exprT1) || exprT1==Exec.GnumT || exprT1==Exec.GenT ) 
//				 if ( isIntegerOrChar(exprT2) || exprT2==Exec.GnumT || exprT2==Exec.GenT ){
//					 Transl.ins(Operator.LCLONE,1);
//					 return Exec.ListT;
//				 }
//				 else
//					throw new Exc_Synt(ErrorType.WRONG_TYPE, 
//								Exec.types[exprT2]+"- expected types: int or char");
//			 else
//					throw new Exc_Synt(ErrorType.WRONG_TYPE, 
//							Exec.types[exprT1]+"- expected types: int or char");
//		 if ( currT == Exec.StringT ) {
//			 if ( isIntegerOrChar(exprT1) || exprT1 == Exec.GnumT || exprT1 == Exec.GenT ) 
//				 if ( isIntegerOrChar(exprT2) ){
//					 Transl.ins(Operator.SUBSTR,1);
//					 return Exec.StringT;
//				 }
//				 else
//					 if ( exprT2 == Exec.GenT  ) {
//						 Transl.ins(Operator.LSCLONE,1);
//						 cannotBeJson=true;
//						 return Exec.GenT;				 
//					 }
//					 else
//						 throw new Exc_Synt(ErrorType.WRONG_TYPE, 
//								 Exec.types[exprT2]+"- expected types: int or char");
//		 }
//		 // currT is now equal to NullT
//		 if ( isIntegerOrChar(exprT1) || exprT1 == Exec.GenT ) 
//			 if ( isIntegerOrChar(exprT2) || exprT2 == Exec.GenT || exprT2 == Exec.StringT ){
//				 Transl.ins(Operator.LSCLONE,1);
//				 cannotBeJson=true;
//				 return Exec.GenT;
//			 }
//			 else
//				 throw new Exc_Synt(ErrorType.WRONG_TYPE, 
//						 Exec.types[exprT2]+"- expected types: int or char");
//		 else			 
//			 throw new Exc_Synt(ErrorType.WRONG_TYPE, 
//							 Exec.types[exprT1]+"- expected type: int or char");	 
//	 }
	 
	 static void setAnsGV() throws Exc {
		 SymbH.startVarDef("ans",false,""); // no label for ans
	 	 Transl.start(); 
		 Transl.ins(Operator.INIT,SymbH.nVar()); 
	 	 Transl.ins(Operator.LOADGV,SymbH.iVar()); 
		 Transl.ins(Operator.PUSHN,0); // default initial null value for ans
		 Transl.ins(Operator.MODV,0);
		 Transl.ins(Operator.HALT,0);
		 SymbH.commitAnsGV();
		 int nExecCode=Linker.link(Transl.end());
		 Exec.exec(false,nExecCode); // no debug
	 }

	 // return true if the current token is in first
	 static boolean inFirst ( Token[] first ) {
		 for (int i = 0; i < first.length; i++ )
			 if ( first[i] == lex.currToken() )
				 return true;
		 return false;
	 }

	 // return true if the current token is in first
	 static boolean inFirstFI ( Lex lexFI, Token[] first ) {
		 for (int i = 0; i < first.length; i++ )
			 if ( first[i] == lexFI.currToken() )
				 return true;
		 return false;
	 }

	 // return a wrong token error with a message listing the expected ones
	 static Exc_Synt raiseExc ( Token[] first ) {
		 String s = lex.tokenFullDescr() +" - expected: ";
		 for ( int i = 0; i < first.length; i++) {
			 s += lex.tokenDescr(first[i]);
			 if ( i < first.length-1) s += " or ";
		 }
		 return new Exc_Synt (ErrorType.WRONG_TOKEN, s);
	 }

	 // return a wrong token error with a message listing the expected ones
	 static Exc_Synt raiseExcFI ( Lex lexFI, Token[] first ) {
		 String s = lexFI.tokenFullDescr() +" - expected: ";
		 for ( int i = 0; i < first.length; i++) {
			 s += lexFI.tokenDescr(first[i]);
			 if ( i < first.length-1) s += " or ";
		 }
		 return new Exc_Synt (ErrorType.WRONG_TOKEN, s);
	 }
	// output error and skip input until ";"
	 static void skipStatement ( Exc e, boolean errCursor ) {
		if ( !( e instanceof Exc_Exec ) && e.errorType() != ErrorType.WRONG_FILE ) {
			if ( errCursor )
				ioh.errorCursor();
			ioh.error(e);
			lex.skipStatement();
		}
		else // error during run-time execution
			ioh.errorNoSkip(e);
	} 

	 static boolean isIntegerOrChar( int type ) {
		 return type==Exec.GenT || type==Exec.GnumT || type==Exec.IntT || type==Exec.CharT;
	 }

	 static boolean isFunction( int type ) {
		 return type==Exec.FuncT;
	 }

	 static boolean isLabel( int type ) {
		 return type==Exec.LabT;
	 }

		static boolean areEQNEQComparable ( int type1, int type2 ) {
			if ( isFunction(type1) || isFunction(type2) )
				return false;
			if ( isLabel(type1) || isLabel(type2) )
				return false;
			if (type1==type2 )
				return true;
			if ( type1 == Exec.GenT || type2 == Exec.GenT )
				return true;
			if ( type1 == Exec.NullT || type2 == Exec.NullT )
				return true;
			if ( isNumber(type1) && isNumber(type2) )
				return true;
			if ( isStringListJson(type1) && isStringListJson(type2) &&
					!areDistinctStringListJson(type1,type2) )
				return  true;
			return false; 
		}
		

		static boolean areRelComparable ( int type1, int type2 ) {
			if ( isFunction(type1) || isFunction(type2) )
				return false;
			if ( isLabel(type1) || isLabel(type2) )
				return false;
			if ( type1==Exec.ListT || type2==Exec.ListT )
				return false;
			if ( type1==Exec.JsonT || type2==Exec.JsonT )
				return false;
			if ( type1==Exec.NullT || type2==Exec.NullT )
				return false;
			if ( isNumber(type1) && isNumber(type2) )
				return true;
			if ( isStringList(type1)  && isStringList(type2) )
				// two cases: (1) only two strings or (2) a string and a generic GslT
				return true;
			if ( isBool(type1) && isBool(type2) )
				return true;
			return false;
		}
	
		static boolean isBool( int type ) {
			return type==Exec.BoolT || type==Exec.GenT;
		}
		
		static boolean isNumber( int type ) {
			return type==Exec.DoubleT || type==Exec.LongT || type==Exec.CharT 
					|| type==Exec.IntT || type==Exec.GnumT || type==Exec.GenT;
		}

		static int mostGeneralNumberType(int t1, int t2) {
			if ( t1==Exec.GenT || t2==Exec.GenT )
				return Exec.GnumT;
			if ( t1==Exec.GnumT || t2==Exec.GnumT )
				return Exec.GnumT;
			if ( t1==Exec.DoubleT || t2==Exec.DoubleT )
				return Exec.DoubleT;
			if ( t1==Exec.LongT || t2==Exec.LongT )
				return Exec.LongT;
			if ( t1==Exec.IntT || t2==Exec.IntT )
				return Exec.IntT;
			return Exec.CharT; 
		}

		static boolean isString(int t) {
			return  t==Exec.GenT || t==Exec.GsljT || t==Exec.GslT || 
					t==Exec.StringT;
		}

		static boolean isList(int t) {
			return  t==Exec.GenT || t==Exec.GsljT || t==Exec.GslT || 
					t==Exec.ListT;
		}

		static boolean isStringList(int t) {
			return  t==Exec.GenT || t==Exec.GsljT || t==Exec.GslT || 
					t==Exec.StringT || t==Exec.ListT;
		}

		static boolean isStringListJson(int t) {
			return  t==Exec.GenT || t==Exec.GsljT || t==Exec.GslT || 
					t==Exec.StringT || t==Exec.ListT || t==Exec.JsonT;
		}
		
		static boolean areDistinctStringListJson(int t1, int t2) {
			boolean t1Strict= t1==Exec.StringT || t1==Exec.ListT || t1==Exec.JsonT;
			boolean t2Strict= t2==Exec.StringT || t2==Exec.ListT || t2==Exec.JsonT;
			return  t1Strict && t2Strict && t1 != t2;
		}

		static int mostGeneralStringList(int t1, int t2) {
			if ( t1 == t2 )
				return t1;
			if ( isNumber(t1)  && isNumber(t2) )
				return mostGeneralNumberType(t1,t2);
			 if ( isStringList(t1)  && isStringList(t2) )
				 return Exec.GslT;
			return Exec.GenT;
		}	

		static int mostGeneralType(int t1, int t2) {
			if ( t1 == t2 )
				return t1;
			if ( t1 == Exec.GenT  || t2 == Exec.GenT )
				return Exec.GenT;
			if ( isNumber(t1)  && isNumber(t2) )
				return mostGeneralNumberType(t1,t2);
			 if ( isStringList(t1)  && isStringList(t2) )
				 return Exec.GslT;
			 if ( isStringListJson(t1)  && isStringListJson(t2) )
				 return Exec.GsljT;
			return Exec.GenT;
		}	


	// execution of a command service
	 static void serviceCommandExec () throws Exc {
		 switch ( typeService ) {
		 	case isServiceList: OutputH.printServiceList(typeServiceList==isDefaultList); break;
		 	case isLambdaList: OutputH.printLambdas(); break;
		 	case isVarService: OutputH.printVars(); break;
		 	case isFunctService: OutputH.printFuns(ioh.nUtil()); break;
		 	case isReleaseService: OutputH.printRelease(); break;
		 	case isDebugOnService: isDebug = true; OutputH.printDebug(isDebug); break;
		 	case isDebugOffService: isDebug = false; OutputH.printDebug(isDebug); break;
//		 	case isTailOptOnService: isTailOpt = true; OutputH.printTailOpt(isTailOpt); break;
//		 	case isTailOptOffService: isTailOpt = false; OutputH.printTailOpt(isTailOpt); break;
		 	case isHistoryService: OutputH.printHist(0,true); break;
		 	case isHistoryNumService: OutputH.printHist(numHistory,false); break;
		 	case isLabelService: OutputH.printLabels(setLabel,setLabelID); break;
		 	case isAboutService: if ( typeAbout == isAboutBuiltIn) OutputH.printOneBuiltIn(builtInF); else
		 						 if ( typeAbout == isAboutLABEL) OutputH.printOneLabel(idAbout); else
			 				     if ( typeAbout == isAboutFUNCT) OutputH.printOneFunct(idAbout,ioh.nUtil()); else
			 					 if ( typeAbout == isAboutVAR) OutputH.printOneVar(idAbout);
		 						break;
		 	case isSaveService: ioh.saveFile(); break;
		 	case isMemoryService: OutputH.printMemory(numMemoryService); break;
		 	case isImportEchoService: ioh.importFile(true); break;
		 	case isImportNoEchoService: ioh.importFile(false); break;
		 	case isClops: ioh.print(Long.toString(Exec.nExecMicroInstrs())+"\n"); break;
		 	case isExecService: ioh.setExecLine(History.command(numHistory)); break;
		 	default: throw new Exc_Synt(ErrorType.FATAL_ERROR,": unexpected behavior while executing service commands"); 

		 }
	 }
} // end class Parser

class FunctCalled {
	static  final int paramsInitCapacity = 20;
	boolean hasSideEffect; // true if the called function has side effects
	int numParams; // number of parameters of the called function
	boolean hasFunctParams; // true if the called function includes function parameters 
	String ID;
//	int functionType;
//	static int isParameter=1; // the called function is denoted by a parameter of a function definition
//	static int isIdentifier=2; // the called function is denoted by its identifier
//	static int isRetFunct=3; // the called function has been returned during a computation
	int functIndex;
	// case isParameter: 
	//		in general functIndex is the parameter index within a function definition  
	//      [ex1] given the function parameter X_i in the definition f(X_0..,X_i/n_i,...,X_k)-> ...X_i(...)..., functIndex is i
    //      in case of lambda definition inside a (external) function definition, the are two parameter types in the order: 
    //      (1) external parameters and (2) proper lambda parameters and the called function is denoted by an external parameter 
	//      [ex2] f(X_0,X_1/2,X_2)/1 -> \ (X_3) -> X_1(X_0,X3) where X3 is proper while X_0 - X_2 are external
	//            X_1 is the parameter denoting the called function and its functIndex is 1
	// case isIdentifier: functCode is the identifier function index
	// case isRetFunct: functCode is 0 (not applicable)
	int numParamsDefFunct; 
	// case isIdentifier: case isRetFunct: numParamsDefFunct is 0 (not applicable)
	// case isParameter: numParamsDefFunct is the total number of parameters of the function for which the called function is a parameter 
	// k for X_i in [ex1] and 4 (number of external and proper parameters) for X_1 in [ex2]
	ArrayList<Integer> pTs; // parameters types (varType, functType)
	ArrayList<Integer> pFn; // arity for function parameters
	int retT; // return value type (varType, functType)
	int retFn; // arity for return functType

	FunctCalled (String ID, int fCode, FunctInfo fi ) {
		this.ID = ID;
		this.functIndex = fCode;
		this.hasSideEffect = fi.hasSideEffect;
		this.numParams = fi.pTs.size();
		this.numParamsDefFunct = 0;
		this.hasFunctParams =fi.hasFunctParams;
		pTs = new ArrayList<Integer> (fi.pTs);
		pFn = new ArrayList<Integer> (fi.pFn);
		retT = fi.retT;
		retFn = fi.retFn;
//		print(this);
	}


	FunctCalled (String idN, boolean se, int fCode, int fArity, int defFuncArity ) {
		this.ID = idN;
		this.functIndex = fCode;
		this.hasSideEffect = se;
		this.numParams = fArity;
		this.numParamsDefFunct = defFuncArity;
		this.hasFunctParams =false;
		pTs = new ArrayList<Integer> (fArity);
		pFn = new ArrayList<Integer> (fArity);
		for ( int i=0; i < fArity; i++ ) {
			pTs.add(SymbH.varType);
			pFn.add(0);
		}
		retT = SymbH.varType;
		retFn = 0;
//		print(this);
	}

	FunctCalled ( FunctCalled fc ) {
		this.ID = fc.ID;
		this.functIndex = fc.functIndex;
		this.hasSideEffect = fc.hasSideEffect;
		this.numParams = fc.numParams;
		this.numParamsDefFunct = fc.numParamsDefFunct;
		this.hasFunctParams =fc.hasFunctParams;
		pTs = new ArrayList<Integer>(fc.pTs);
		pFn = new ArrayList<Integer>(fc.pFn);
		retT=fc.retT;
		retFn=fc.retFn;
//		print(this);
	}
	static void print(FunctCalled fc) {
		System.out.println("\nIDNAME: "+fc.ID);
		System.out.println("fCode: "+fc.functIndex);
		System.out.println("sideEffect: "+fc.hasSideEffect);
		System.out.println("nParams: "+fc.numParams);
		System.out.println("nParamsTot: "+fc.numParamsDefFunct);
		System.out.println("hasFunctParams: "+fc.hasFunctParams);
		System.out.println("retT: "+fc.retT);
		System.out.println("retFn: "+fc.retFn);
		System.out.println("pTs - pFn");
		for (int i=0; i<fc.numParams; i++)
			System.out.println("i="+i +" pTn="+fc.pTs.get(i)+ " pFn="+fc.pFn.get(i));
		
	}
	static boolean equiv ( FunctCalled fc1, FunctCalled fc2) {
		if ( 	fc1.hasSideEffect != fc2.hasSideEffect || 
				fc1.numParams != fc2.numParams ||
//				fc1.numParamsTot != fc2.numParamsTot ||
				fc1.hasFunctParams != fc2.hasFunctParams ||
				fc1.retT != fc2.retT ||
				fc1.retFn != fc2.retFn 
			)
			return false;
		for ( int i=0; i < fc1.numParams; i++ )
				if ( fc1.pTs.get(i) != fc2.pTs.get(i) || 
						fc1.pFn.get(i) != fc2.pFn.get(i) )
					return false;		
		return true;
	}
} // end class FunctCalled

class GuardRet {
	int exprt;
	FunctCalled fc;
	GuardRet( int e,FunctCalled f) {
		exprt=e; fc=f;
	}
}
