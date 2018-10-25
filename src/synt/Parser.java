package synt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
		 	Token.HALT
		 };
	static Token[] first_statement = { 
		 	Token.ID,
		 	Token.IDL,
		 	Token.PRINT,
		 	Token.EMARK
		 };
	static Token[] first_varDef = { 
			Token.OSPAR,
			Token.ASSIGN,
			Token.OPASSIGN
		 };
	static Token[] first_varGlobSet = first_varDef;


	static Token[] first_varFunctLabDef = { 
		Token.OSPAR,
		Token.ASSIGN,
		Token.OPASSIGN,
		Token.OPAR, 
		Token.STAR,
		Token.COLON
	 };

	static Token[] first_expr = { 
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
			Token.TRUE,
			Token.FALSE,
			Token.NULL,
			Token.BUILTIN,
			Token.TYPE
		 };
	static Token [] first_eqExpr = first_expr;

	static Token[] first_term = first_expr;
	
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
		Token.TRUE,
		Token.FALSE,
		Token.NULL,
		Token.BUILTIN,
		Token.TYPE
	 };
	
	static Token[] first_simpleTerm = first_fact;

	static Token[] first_orExpr = first_expr;
	static Token[] follows_orExpr_next = {
		Token.QMARK, 
		Token.COLON, 
		Token.COMMA, 
		Token.SCOLON, 
		Token.PRINTOPT, 
		Token.PRINTADD, 
		Token.CPAR, 
		Token.CSPAR, 
		Token.CBRACE, 
		Token.BAR,
		Token.OBRACESET,
		Token.OBRACEPRINT,
		Token.CBRACESET,
		Token.CBRACEPRINT
	};
	static Token[] follows_andExpr_next = {
		Token.OR,
		Token.QMARK, 
		Token.COLON, 
		Token.COMMA, 
		Token.SCOLON, 
		Token.PRINTOPT, 
		Token.PRINTADD, 
		Token.CPAR, 
		Token.CSPAR, 
		Token.CBRACE, 
		Token.BAR, 
		Token.OBRACESET,
		Token.OBRACEPRINT,
		Token.CBRACESET,
		Token.CBRACEPRINT
	};
	static Token [] first_andExpr = first_expr;
	static Token [] first_ifExpr =  first_expr;
	static Token [] first_constListElems = first_expr;
	static Token[] first_callFunct1 = first_expr;
	static Token[] first_callFunct = { //FIRST(<expr>) U {  LAMBDA }
		// include also epsilon - FOLLOWS = {CPAR}
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
		Token.TRUE,
		Token.FALSE,
		Token.NULL,
		Token.BUILTIN,
		Token.TYPE
	};
	static Token[] first_fields = first_expr; 

	static Token[] first_listElems = { //FIRST(<expr>) U {  DOT, GTLT(GT) , COLON }
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
			Token.TRUE,
			Token.FALSE,
			Token.NULL,
			Token.BUILTIN,
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
		Token.TRUE,
		Token.FALSE,
		Token.NULL,
		Token.TYPE
	 };

	 static int typeDef; // it will take one of the following values
	 static int isFunctDef = 0;
	 static int isVarDef = 1;
	 static int isComp = 2;
	 static int isService = 3;
	 static int isLabDef = 4;
	 
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
	 static final int isMemorySizeService = 15;
	 static final int isTailOptOnService = 16;
	 static final int isTailOptOffService = 17;
	 
	 static int typeAbout; // associated to isAboutService - it will take one of the following values
	 static final int isAboutBuiltIn = 0;
	 static final int isAboutLABEL = 1;
	 static final int isAboutVAR = 2;
	 static final int isAboutFUNCT = 3;

	 static String idAbout; // associated to isAboutLABEL,isAboutVAR, isAboutFUNCT

	 static int numHistory; // associated to isHistoryNumService
	 
	 static int nLocVars=0; // used within funcDef
	 
 
	 static boolean isDebug = false; 
	 static boolean isTailOpt = true;
	 
	 static boolean cannotBeJson;
	 
	 static BIFType builtInF;
	 
	 static boolean isNewVarDef; // true if the current statement is the definition of a new global variable

 	 static boolean correctFileIn=true; // reading data from file by <<(<nameFileIn>)
 	 static boolean isFileIn = false;
 	 static String nameFileIn = null;
 	 static int iJumpFileIn = 0;
 	 static Vector<String> excludedKeys; 
 	 static BufferedWriter outputExecF;
	 
	 // <program> -> { [ <statement> ] SCOLON } HALT
	 public static void program (  ) {
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
		 					if ( isDebug && typeDef != isService ) 
		 						PrintCode.printListing(Transl.end());
				 			if ( typeDef == isVarDef || typeDef == isFunctDef ) 
				 				SymbH.commitVarFunc(History.command(History.nCommand()-1));
				 			else 
				 				if ( typeDef == isLabDef  )
				 					SymbH.commitLabDef();
					 			else
					 				if ( typeDef == isService )
					 					serviceCommandExec();
				 			if (typeDef == isVarDef || typeDef == isLabDef || typeDef == isComp ) {
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
				 			if ( typeDef == isComp )
				 				PrintOutput.printOutput(true);
				 			else
					 			if ( typeDef == isVarDef ) {
					 				PrintOutput.setStandardOutStream();
					 				PrintOutput.printOutput(false);
					 			}
		 				}
		 				ioh.setPrompt(true);
			 			lex.accept(Token.SCOLON);		 					
		 				ioh.setPrompt(false);
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
									 if (execDone && e.errorType() != ErrorType.LIST_DELETED)
										 // run time error: the OUTPUT is flushed out unless there are deleted lists 
										 PrintOutput.printOutput(false);
								 }
								 catch( Exc et ) {
									 
								 }
								 ioh.setImportError();
					 			 History.histOff();
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
							 }
			     	}	
		 		}
		 		else {	 			
		 			try {
		 				ioh.setPrompt(true);
		 				if ( lex.currToken() != Token.SCOLON  ) {
		 				    ioh.setImportError();
			 				skipStatement(raiseExc(first_statement),true);
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
	 
	 // <statement> -> IDL  <varDef> | ID <varFunctLabDef> | 
	 //					PRINT <printCommand> | EMARK <serviceCommand>
	 static void statement ( ) throws Exc {
		 if ( lex.currToken() == Token.IDL ) {
		 	 Transl.start(); 
			 String idN = lex.IDName();
			 boolean hasLabel = true; String label = lex.currLabel();
		 	 History.startCommand(); History.addSubStr(idN);
			 lex.nextToken();
			 typeDef=isVarDef;
			 SymbH.startVarDef(idN,hasLabel,label);
 			 varDef();
 			 return;
		 }
		 if ( lex.currToken() == Token.ID ) {
			 	 Transl.start(); 
				 String idN = lex.IDName();
			 	 History.startCommand(); History.addSubStr(idN);
				 lex.nextToken();
				 varFunctLabDef(idN);
				 return;
		 }
		 if ( lex.currToken() == Token.PRINT ) {
			 History.startCommand(); 
			 History.addSubStr("^ ");
			 lex.nextToken();
			 printCommand();
			 return;
		 }
		 if ( lex.currToken() == Token.EMARK ) {
			 lex.nextToken();
			 serviceCommand();
		 }
		 else
			 throw raiseExc(first_statement);
	 }
	 
	 // <varFunctLabDef> -> <varDef> | STAR <funcDef(SE)> |  <funcDef(noSE)> | COLON <labDef>  
	 static void varFunctLabDef ( String idN ) throws Exc {
		 if (lex.currToken() == Token.COLON ) {
 			 lex.setComment(true); 
			 lex.printH_Colon(true); // print spaced COLON on history
 			 lex.nextToken();
 			 labDef(idN);
		 }
		 else
			 if ( inFirst(first_varDef)  ) {
				typeDef=isVarDef;
				boolean hasLabel=false; String label="";
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
					 SymbH.startFunctDef(idN); 
					 funcDef(idN,se);
				 }
				 else
		 			 if( lex.currToken() == Token.SCOLON )
		 					throw new Exc_Synt(ErrorType.MISSING_PRINT);
		 				 else
		 					 throw raiseExc(first_varFunctLabDef);
			 }	 
	 }
	 
	 // <labDef> -> [COMMENT] ID1 { COMMA ID1 } 
	 static void labDef ( String idN ) throws Exc {
 			 Transl.ins(Operator.INIT,SymbH.nVar());
 			 typeDef=isLabDef;
 			 SymbH.startLabelDef(idN);
 			 if ( lex.currToken()==Token.COMMENT ) {
 				 SymbH.addLabelComment(lex.commentString());
 				 if ( lex.isCommentOverflow() )
 					 ioh.print("\n**Warning: comment string truncated to max length "+Lex.maxLenComment+"\n");
 				 lex.nextToken();
 			 }
 			 lex.setComment(false); 
 			 labVar();
 			 while (lex.currToken() == Token.COMMA) {
				 lex.printH_Comma(true); // print space after comma in history
 				 lex.nextToken(); 
 				 labVar();		 	 				 
 			 };
 		 	 Transl.ins(Operator.HALT,0);
 			 return;
	 }
	 
	 // method called by <labDef>
	 static void labVar ( ) throws Exc {
		 String idN; 
		 if ( lex.currToken() == Token.ID ) 
			 idN = lex.IDName();
		 else
			 throw new Exc_Synt(ErrorType.EXPECTED_ID,
					 " - found "+lex.tokenFullDescr() );		 
		 lex.accept(Token.ID );
		 int indLabVar = SymbH.addLabVar(idN); 
		 if ( indLabVar < 0 ) {
			 Transl.ins(Operator.PUSHN,0); // default null value for labeled variables
		 }
		 else {
			 Transl.ins(Operator.LOADGV,indLabVar);
			 Transl.ins(Operator.PUSHN,0); // default null value for labeled variables
			 Transl.ins(Operator.MODV,0);
		 }
	 }
	 
	 // <printCommand> -> [FILEOUT OPAR (STRING | ID | IDL) CPAR] <ifExpr(SE)>
	 //					 [PRINTOPT] { PRINTADD  <ifExpr(SE)> [PRINTOPT] }
	 static void printCommand ( ) throws Exc {
	 	    Transl.start();
	 		SymbH.startComp();
	 		Transl.ins(Operator.INIT,SymbH.nVar());
	 		boolean startPrint = true;
			typeDef=isComp;
			if (lex.currToken()==Token.FILEOUT ) {
				lex.nextToken();
				lex.setTruncatableString(false); // a file name string cannot be truncated
				lex.accept(Token.OPAR);
				if ( lex.currToken() == Token.STRING ) 
					PrintOutput.setFileOutStream(lex.stringVal());
				else
					if ( lex.currToken() == Token.ID || lex.currToken() == Token.IDL ) {
						int iVar = getStringID();
						PrintOutput.setFileOutStream(PrintOutput.getStringGV(iVar));
					}
					else
						 throw new Exc_Synt(ErrorType.WRONG_TOKEN,lex.tokenFullDescr()+
								 " - expected: a string or 'ID'");
				lex.setTruncatableString(true); // by default string are truncatable
				lex.nextToken();
				lex.accept(Token.CPAR);
			}
			else
				PrintOutput.setStandardOutStream();
	 		do {
			 	if ( ! startPrint ) 
			 		lex.nextToken ();
			 	else {
			 		SymbH.startVarDef("ans",false,"");			 		
				 	Transl.ins(Operator.LOADGV,SymbH.iVar()); 
			 	}
		 		int retType=ifExpr(true); // side effect is enabled
			 	if ( retType>=Exec.FuncT )
		        	 	throw new Exc_Synt(ErrorType.WRONG_RET_TYPE, ": a function cannot be printed");
		 		if ( startPrint ) 
				 	Transl.ins(Operator.DUPL,0); 
		 		if ( lex.currToken() == Token.PRINTOPT) {
					Transl.ins(Operator.PRINT,lex.indPrint());
					lex.nextToken();
		 		}
		 		else
		 			Transl.ins(Operator.PRINT,0);
		 		if ( startPrint ) 
				 	Transl.ins(Operator.MODV,0); 		 			
	 			startPrint = false;
	 		} while ( lex.currToken() == Token.PRINTADD );
	 		Transl.ins(Operator.HALT,0);
	 		return;		 
	 }

	 // method called by printCommand and by varDef
	 static int getStringID() throws Exc {
		 String idName = lex.IDName();
		 boolean hasLabel= lex.currToken()==Token.IDL;
		 String label =   hasLabel? lex.currLabel(): null;
		 int idType = SymbH.setCurrID(idName,hasLabel,label);
		 if ( idType == SymbH.undefID )
				throw new Exc_Synt(ErrorType.UNDEF_ID, " " + idName);
		 if ( idType == SymbH.functType )
				throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
						"found a function - expected a global variable");
		 int iVar = SymbH.iCurrVarFunc();
		 int tSymb = SymbH.typeGV(iVar);
		 if ( tSymb != Exec.StringT )
				throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
						"- expected a string global variable");
		 return iVar;						
	 }

 
	 // <serviceCommand> -> ID("variables") | ID("functions") | ID("release") | ID("labels") |
	 //						ID("about") (BUILTIN ID | ID | IDL) | ID("debug") [ID("off") | ID("on")] | 
	 //						ID("history") [ [MINUS] INT] ) | ID("exec") [ [MINUS] NUM ] | ID("clops") | 
	 //						ID("save") | ID("memory") [ ID("size") ] | 
	 //						ID("import") [ID("echo") | ID("noecho")] |
	 static void serviceCommand() throws Exc {
	 	 typeDef=isService;
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
				 if ( lex.currToken() == Token.BUILTIN ) {
					 typeService = isAboutService; typeAbout = isAboutBuiltIn;
					 lex.nextToken();
					 if ( lex.currToken() == Token.ID ) {
						 builtInF = lex.getBuiltIn();
						 lex.nextToken();
						 return;
					 }
					 else
						 throw new Exc_Synt(ErrorType.WRONG_OPTION,lex.currTokenDescr()+
								 " - expected: ID of a built-in function" );
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
			 if ( checkCommand(name,"tailOpt")  ) {
				 lex.nextToken();
				 if ( lex.currToken() == Token.ID ) {
					 if ( lex.IDName().equals("on")  )
						 isTailOpt = true;
					 else
						 if ( lex.IDName().equals("off")  )
							 isTailOpt = false;
						 else
							 throw new Exc_Synt(ErrorType.WRONG_OPTION,lex.IDName()+
									 " - expected: 'on' or 'off' or no argument" );
					 lex.nextToken();
				 }
				 else
					 isTailOpt= true;
				 typeService = isTailOpt? isTailOptOnService: isTailOptOffService;
				 return;
			 }
			 if ( checkCommand(name,"history")  ) {
				 lex.nextToken();
				 int sign = 1;
				 if ( lex.currToken() == Token.MINUS ) {
					 sign =-1; lex.nextToken();
				 }
				 if ( lex.currToken() == Token.INT  ) {
					    if ( History.nCommand() == 0 )
					    	throw new Exc_Synt(ErrorType.WRONG_HISTRORY_INDEX," - empty history ");
					    int iH = lex.intVal()*sign;
						if ( iH < -History.nCommand()+1 || iH > History.nCommand() ) 
							 throw new Exc_Synt(ErrorType.WRONG_HISTRORY_INDEX,iH+
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
						 throw new Exc_Synt(ErrorType.WRONG_HISTRORY_INDEX," - spurious \'-\' ");
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
				 if ( lex.currToken() == Token.ID ) {
					 if ( lex.IDName().equals("size")  )
						 typeService=isMemorySizeService;
					 else
						 throw new Exc_Synt(ErrorType.WRONG_OPTION,lex.IDName()+
									 " - expected: 'size' or no argument" );
					 lex.nextToken();
				 }
				 else
					 typeService=isMemoryService;
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
								" - expected: 'variables' or 'functions' or 'debug' or 'history'"+
								"or 'labels' or 'import' or 'exec' or 'clops' or 'release' or 'save' or 'about' or 'tail'"+ " or any prefix of them");
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
	 //				ASSIGN ( HASH NULL | 
	 //				<fileIn>
	 //				| <ifExpr> ) )
	 static void varDef ( ) throws Exc {
		 int typeVar=SymbH.isNewDef()?Exec.NoType: SymbH.typeGV(SymbH.iVar()); 
		 boolean listElOrField=false;
	 	 boolean se = false;
		 Transl.ins(Operator.INIT,SymbH.nVar()); 
	 	 Transl.ins(Operator.LOADGV,SymbH.iVar()); 
 		 if ( lex.currToken() == Token.OSPAR ) {
 			 typeVar=Exec.NoType;
			 lex.nextToken(); listElOrField=true;
			 if ( inFirst(first_expr) ) {
				 int precType = SymbH.prevListJsonDef();
			 	 Transl.ins(Operator.DEREF,0);
			 	 SymbH.noDef();
				 int type=ifExpr(se);
				 if ( precType == Exec.ListT ) {
					 if ( type != Exec.NoType && !isInteger(type) )
							throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
									Exec.types[type]+"- expected types: int or char");
					 Transl.ins(Operator.LISTEL,0); 
				 }
				 else {
					 if ( type != Exec.NoType && type != Exec.StringT )
							throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
									Exec.types[type]+"- expected type: string");
					 Transl.ins(Operator.FLDFIND,1); 			 
				 }					 
				 lex.accept(Token.CSPAR);
		 		 while ( lex.currToken() == Token.OSPAR ) {
		 			 lex.nextToken();
					 if ( inFirst(first_expr) ) {
					 	 Transl.ins(Operator.HDFLDV,0);
						 type=ifExpr(se);
						 if ( type != Exec.NoType && !isInteger(type) && type != Exec.StringT )
								throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
										Exec.types[type]+"- expected types: int or char or string");
						 Transl.ins(Operator.LJEL,0); 
						 lex.accept(Token.CSPAR);
					 }				 
					 else 
						 throw raiseExc(first_expr);
				 }
			 }
			 else 
				 throw raiseExc(first_expr);	
 		 }
	 	 isNewVarDef = SymbH.isNewDef();
 		 if ( lex.currToken()==Token.OPASSIGN ) {
  			 Token assOpType = lex.currAssOp();
		 	 lex.printH_OpAssign(assOpType,true); // print op-assign with spaces on history
		 	 if ( isNewVarDef )
					throw new Exc_Synt(ErrorType.UNDEF_VAR, 
							"- the compound assignment cannot be used");		 		 
		 	 if ( typeVar != Exec.NoType && !isNumber(typeVar) && 
		 			 !( (typeVar==Exec.ListT || typeVar==Exec.StringT) && assOpType==Token.PLUS) ) 
		 		if ( assOpType==Token.PLUS )
					throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
							Exec.types[typeVar]+"- expected types: double or long or int or char or list or string");
		 		else
					throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
							Exec.types[typeVar]+"- expected types: double or long or int or char");		 		 
 			 opAssign(typeVar, listElOrField, assOpType,true); // se = true
 		 }
 		 else {
		 	 lex.printH_Assign(true); // print assign with spaces on history
		 	 lex.accept(Token.ASSIGN);
		 	 isFileIn = lex.currToken() == Token.FILEIN;
			 if( isFileIn ) 
				 fileIn();
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
					 int retType=ifExpr(se);
						if ( retType >= Exec.FuncT )
							throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
									Exec.types[Exec.FuncT]+"- a function cannot be assigned to a variable");		 		 		
				 }
 		 }
		 if ( listElOrField )
			 Transl.ins(Operator.MODVEL,0);
		 else
			 Transl.ins(Operator.MODV,0);	
	 	 Transl.ins(Operator.HALT,0);
	 	 if ( isFileIn && correctFileIn ) {
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
	         //Transl.ins(Operator.EXECF, 0);
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

	 // <fileIn> -> 	FILEIN OPAR (STRING | ID | IDL) 
	 //					[ 	COMMA  ( STRING | ID | IDL )
	 //						{COMMA  ( STRING | ID | IDL ) }  ] CPAR  
	 static void fileIn ( ) throws Exc {
	 	 	correctFileIn=true; 
	 	 	isFileIn = true;
	 	 	nameFileIn = null;
			lex.nextToken();
			lex.setTruncatableString(false); // a file name string cannot be truncated
			lex.accept(Token.OPAR);
			Transl.ins(Operator.EXECF, 0); // next instructions: MOD, HALT
			// int iJumpT = Transl.ins(Operator.JUMP,0);
			// Transl.modOperand(iJumpT, iJumpT+4); // skip NEXT, MOD, HALT
			// iJumpFileIn = Transl.ins(Operator.NEXT,0);
			nameFileIn = getString();
			lex.nextToken();
			excludedKeys = new Vector<String>(10,5);
			if ( lex.currToken() == Token.COMMA ) {
				lex.nextToken();
				excludedKeys.add(getString());
				lex.nextToken();
				while ( lex.currToken()==Token.COMMA ) {
					lex.nextToken();
					excludedKeys.add(getString());
					lex.nextToken();
				}
			}		
			lex.accept(Token.CPAR);
			if ( lex.currToken() != Token.SCOLON )
				correctFileIn=false;
	 }

	 // called by fileIn
	 private static String getString () throws Exc {
		 	String st;
			if ( lex.currToken() == Token.STRING ) 
				st= lex.stringVal();
			else
				if ( lex.currToken() == Token.ID || lex.currToken() == Token.IDL ) {
					int iVar=getStringID();
					st = PrintOutput.getStringGV(iVar);
				}
				else
					 throw new Exc_Synt(ErrorType.WRONG_TOKEN,lex.tokenFullDescr()+
							 " - expected: a string or 'ID' or 'IDL'");
			lex.setTruncatableString(true); // by default string are truncatable
			return st;
	 }
	 
	 // called by varDef and varGlobSet
	private static void opAssign (int type, boolean listElorField, Token opAssType, boolean se ) throws Exc {
		Transl.ins(Operator.DUPL, 0);
		if ( listElorField )
			Transl.ins(Operator.HDFLDV, 0);
		else
			Transl.ins(Operator.DEREF, 0);
		lex.nextToken();
		int typeExp = ifExpr(se); 
		if ( typeExp >= Exec.FuncT )
			throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
					Exec.types[Exec.FuncT]+"- a function cannot be assigned to a variable");		 		 		
		if ( type!=Exec.NoType && typeExp != Exec.NoType )
			if ( isNumber(type) && !isNumber(typeExp) )
				throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
						Exec.types[typeExp]+"- expected types: double or long or int or char");		 		 
			else
				if ( type == Exec.ListT && typeExp != Exec.ListT )
					throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
							Exec.types[typeExp]+"- expected type: list");
				else
					if ( type == Exec.StringT && !(typeExp == Exec.StringT || typeExp == Exec.CharT) )
						throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
								Exec.types[typeExp]+"- expected type: string or char");
		if ( opAssType==Token.PLUS) {
			Transl.ins(Operator.ADD, 0);
			return;
		}
		if ( opAssType==Token.MINUS ) {
			Transl.ins(Operator.SUB, 0);
			return;
		}
		if ( opAssType==Token.STAR) {
			Transl.ins(Operator.MULT, 0);
			return;
		}
		if ( opAssType==Token.DIV) {
			Transl.ins(Operator.DIV, 0);
			return;
		}
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
		// 			    [ PLUS | MINUS ] ( INT | LONG | DOUBLE ) | CHAR | STRING | NULL | TRUE | FALSE | TYPE
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
		 if ( lexFI.currToken() == Token.TRUE ) {
		 	 	 if( copyElem )
					 writeFI(opStr(Operator.PUSHB)+" "+1);
		 	 		 // Transl.ins(Operator.PUSHB,1);
				 lexFI.nextToken();
				 return;
		 }
		 if ( lexFI.currToken() == Token.FALSE ) {
		 	 	 if( copyElem )
					 writeFI(opStr(Operator.PUSHB)+" "+0);
		 	 		 // Transl.ins(Operator.PUSHB,0);
				 lexFI.nextToken();
				 return;
	 	 }
		 if ( lexFI.currToken() == Token.TYPE ) {
			 if ( lexFI.currType() == Exec.TypeT ) {
			 	 if( copyElem )
					 writeFI(opStr(Operator.PUSHMT)+" "+0);
			 		 // Transl.ins(Operator.PUSHMT,0);
				 lexFI.nextToken();
				 return;				 
			 }
			 else { 
			 	 if( copyElem )
					 writeFI(opStr(Operator.PUSHT)+" "+lexFI.currType());
			 		 // Transl.ins(Operator.PUSHT,lexFI.currType());
				 lexFI.nextToken();
				 return;
			 }
		 }		
		 throw raiseExcFI(lexFI,first_factFI); 
	}

	 // method called by factFI 
	 static void getStringFI(String s ) throws Exc {
		 writeFI(opStr(Operator.SSTRING)+" "+0);
		 // Transl.ins(Operator.SSTRING,0);
		 for ( int i =0; i <s.length(); i++ ) {
			 writeFI(opStr(Operator.PUSHC)+" "+(int)s.charAt(i));
			 // Transl.ins(Operator.PUSHC, s.charAt(i));
			 writeFI(opStr(Operator.CSTRING)+" "+0);
			 // Transl.ins(Operator.CSTRING, 0 );
		 }
		 writeFI(opStr(Operator.ESTRING)+" "+0);
		 // Transl.ins(Operator.ESTRING,0);
	 }


	// <funcDef> -> OPAR [<formalPar> { COMMA <formalPar> } ] CPAR  
	// 					[ DIV INT ] COLON [COMMENT]
	//					[LT ID1 [STAR] { COMMA ID1 [STAR] } GT] <ifExprSet>
	 static void funcDef ( String idN, boolean se ) throws Exc {
		 lex.accept(Token.OPAR );
		 SymbH.funcDef(se,ioh.nUtil());
		 if ( lex.currToken() == Token.ID || lex.currToken() == Token.BUILTIN) {
			 formalPar( se );
			 while ( lex.currToken() == Token.COMMA ) {
				 lex.printH_Comma(true); // print space after comma in history
				 lex.nextToken();
				 formalPar( se );
			 }
		 }
		 lex.accept(Token.CPAR );
		 if ( lex.currToken()==Token.DIV ) {
			 lex.nextToken();
			 if ( lex.currToken()== Token.CHAR || lex.currToken()== Token.INT ) {
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
	 	 lex.printH_Colon(true); // print colon with spaces on history
		 if ( lex.currToken() == Token.COLON ) {
			 lex.setComment(true); lex.nextToken();
		 }
		 else
			 throw new Exc_Synt(ErrorType.WRONG_TOKEN,lex.currTokenDescr()+
					 " - expected: ':'");
		 if ( lex.currToken()==Token.COMMENT ) {
			 SymbH.addFunctComment(lex.commentString());
			 if ( lex.isCommentOverflow() )
				 ioh.print("\n**Warning: comment string truncated to max length "+Lex.maxLenComment);
			 lex.setComment(false); 
			 lex.nextToken();
		 }
		 lex.setComment(false); 
		 nLocVars=0;
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
	 	 					 throw new Exc_Synt(ErrorType.GLOB_VAR_IN_FUNCT, " without side effects");	 					 
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
	 	 Transl.ins(Operator.START,nLocVars,"* "+idN);
	 	 int retType=ifExprSet(se,idN);
	 	 if ( retType>=Exec.FuncT )
	 		 SymbH.checkRetType(SymbH.functType,retType-Exec.FuncT);
	 	 else
	 		SymbH.checkRetType(SymbH.varType,0);
	 	 Transl.ins(Operator.RETURN,SymbH.nParFdef());
	 	 if ( isTailOpt )
	 		 Transl.tailRecOpt(SymbH.indCurrDef(),SymbH.nParFdef());
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
 					 throw new Exc_Synt(ErrorType.GLOB_VAR_IN_FUNCT, " without side effects");	 					 
				 SymbH.addCurrLabel(idN);
		 }
		 else {
			 SymbH.addLoc(idN); 
			 nLocVars++;
		 }
	 }

		// <formalPar> ->  ID1 [ DIV INT ]
	 static void formalPar ( boolean se ) throws Exc {
		 if ( lex.currToken() == Token.ID || lex.currToken() == Token.BUILTIN ) {
			 String idN = "_";
			 if ( lex.currToken() == Token.ID ) {
				 idN = lex.IDName();
			 }
			 lex.nextToken();
			 if ( lex.currToken()==Token.DIV ) {
				 lex.nextToken();
				 if ( lex.currToken()== Token.CHAR || lex.currToken()== Token.INT ) {
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

	 //<ifExprSet> ->  { <globSet> } ( EXC OPAR  <ifExpr> CPAR | <lambda> |
	 // 									 <orExpr> ( { <globSet> } |  
	//										QMARK <ifExprSet> COLON <ifExprSet> ) )
	 static int ifExprSet ( boolean se, String idN ) throws Exc {
		 while ( lex.currToken() == Token.OBRACESET || 
				 lex.currToken() == Token.OBRACEPRINT ) 
			 globSet(se); 
		 if ( lex.currToken() == Token.EXC ) {
			 lex.nextToken();
			 lex.setTruncatableString(false); // exc string cannot be truncated
			 lex.accept(Token.OPAR);
			 int exc_t=ifExpr(se);
			 if ( exc_t != Exec.NoType && exc_t != Exec.StringT )
					throw new Exc_Synt(ErrorType.STRING_EXPECTED, 
							 Exec.types[exc_t]+"- expected type: string");
			 printExc(idN);
			 lex.setTruncatableString(true); // by default string are truncatable
			 lex.accept(Token.CPAR);
			 Transl.ins(Operator.THROWE,0);
			 return Exec.StringExcF; // type exception as a string
		 }
		 if ( lex.currToken() == Token.LAMBDA) {
			 if ( !SymbH.checkFunctRet() )
					throw new Exc_Synt(ErrorType.WRONG_LAMBDA, 
							"- it must be defined inside a function returning a function");
			 int retType=lambda(false,0,0,0); // false since lambda is not a function parameter 
			 if ( !SymbH.checkLambdaRetArity() )
					throw new Exc_Synt(ErrorType.WRONG_LAMBDA, 
							"- the lambda function arity is different from the defining function arity return");			 
			 if ( lex.currToken() != Token.COLON &&  lex.currToken() != Token.SCOLON ) 
				 throw new Exc_Sem(ErrorType.WRONG_LAMBDA, 
						"-- expected ':' or ';' to terminate lambda function definition - found " + lex.tokenFullDescr());					 			 
			 return retType;
		 }

		 boolean  isSE=se;
		 int cexpr_t = orExpr(isSE); 
		 if ( lex.currToken() == Token.QMARK ) {
			 if ( cexpr_t != Exec.BoolT &&  cexpr_t != Exec.NoType ) 
					throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
								Exec.types[cexpr_t]+"- expected type: bool");
			 lex.nextToken();
			 int iJumpE = Transl.ins(Operator.JUMPZ,0);
			 int ifExpr1_t = ifExprSet(se,idN);
			 int iJumpT = Transl.ins(Operator.JUMP,0);
			 lex.printH_Colon(true); // print spaced COLON on history
			 lex.accept(Token.COLON );
			 int labelE = Transl.ins(Operator.NEXT,0);
			 Transl.modOperand(iJumpE, labelE);
			 int ifExpr2_t = ifExprSet(se,idN);
			 int labelT = Transl.ins(Operator.NEXT,0);
			 Transl.modOperand(iJumpT, labelT);
			 if ( ifExpr1_t == ifExpr2_t )
				 return ifExpr1_t;
			 else
				 return Exec.NoType;
		 }
		 else {
			 while ( lex.currToken() == Token.OBRACESET || 
					 lex.currToken() == Token.OBRACEPRINT ) 
					 globSet(se); 
			 return cexpr_t;
		 }
	}
	 
	 	// called by ifExprSet
	 static void printExc(String idN) throws Exc {
		 Transl.ins(Operator.SSTRING,0);
		 for ( int i =0; i <idN.length(); i++ ) {
			 Transl.ins(Operator.PUSHC, idN.charAt(i));
			 Transl.ins(Operator.CSTRING, 0 );
		 }
		 Transl.ins(Operator.ESTRING,0);
		 
	 }

	 // <globSet> -> OBRACESET  (ID <varGlobSet> | DUMMY <ifExpr> ) CBRACESET  | OBRACEPRINT <printGlobSet> CBRACEPRINT 
		 static void globSet ( boolean se ) throws Exc {
			 	 if ( lex.currToken() == Token.OBRACESET ) {
			 		lex.nextToken();
					 if ( lex.currToken() == Token.ID || lex.currToken() == Token.IDL ) {
						 varGlobSet(se);
						 lex.accept(Token.CBRACESET);
					 }
					 else 
						 if ( lex.currToken() == Token.DUMMY) {
							 lex.nextToken();
							 int retType=ifExpr(se);
							 if ( retType >= Exec.FuncT )
									throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
											Exec.types[Exec.FuncT]+"- a function cannot be used into a Global Setting");		 		 									 
							 Transl.ins(Operator.POP,0);							 
							 lex.accept(Token.CBRACESET);
						 }
						 else 
							 throw new Exc_Synt(ErrorType.EXPECTED_ID," -- found "+
									 		lex.tokenFullDescr());
			 	 }
			 	 else {
			 		 //lex.nextToken();
					 printGlobSet(se);
					 lex.accept(Token.CBRACEPRINT );
			 	 }
	 }
	 
	 // <printGlobSet> -> <ifExpr> [ PRINTOPT ] { PRINTADD <ifExpr> [ PRINTOPT ] }
	 static void printGlobSet ( boolean se ) throws Exc {
	 		do {
	 			lex.nextToken();
		 		int typeExp=ifExpr(se); 
				if ( typeExp >= Exec.FuncT )
					throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
							Exec.types[Exec.FuncT]+"- a function cannot be printed");		 		 		
		 		if ( lex.currToken() == Token.PRINTOPT) {
					Transl.ins(Operator.PRINT,lex.indPrint());
					lex.nextToken();
		 		}
		 		else
		 			Transl.ins(Operator.PRINT,0);
	 		} while ( lex.currToken() == Token.PRINTADD );
	 }
	 
	 // <varGlobSet> ->  [ OSPAR <ifExpr> CSPAR ]* ( OPASSIGN <ifExpr> |
	 //				     ASSIGN ( HASH NULL | <ifExpr>  ) 
	 static void varGlobSet ( boolean se ) throws Exc {
		 boolean listElOrField=false;		
		 String idN = lex.IDName(); 
		 boolean hasLabel=lex.currToken()==Token.IDL;
		 String label =   hasLabel? lex.currLabel(): null;
		 int type = SymbH.setCurrID(idN,hasLabel,label);
		 if ( type == SymbH.undefID )
				throw new Exc_Synt(ErrorType.UNDEF_ID, " " + idN);
		 if ( type == SymbH.functType )
				throw new Exc_Synt(ErrorType.WRONG_FUN_SET, " " + idN);
		 if ( SymbH.isParam() ) 
			 if ( se ) {
				 int nPar = SymbH.nParCurrFunDef();
				 Transl.ins(Operator.LOADARG,nPar-SymbH.iCurrVarFunc());
			 }
			 else
				 throw new Exc_Synt(ErrorType.NO_PAR_SET);
		 else
			 if ( SymbH.isLocVar() )
				 Transl.ins(Operator.LOADLV,SymbH.iCurrVarFunc());
			 else // SymbH.isLabGlobVar() is true
					 Transl.ins(Operator.LOADGV,SymbH.iCurrVarFunc()); 
		 lex.nextToken(); listElOrField=false;
 		 if ( lex.currToken() == Token.OSPAR ) {
			 lex.nextToken(); listElOrField=true;
			 if ( inFirst(first_expr) ) {
			 	 Transl.ins(Operator.DEREF,0);
				 type=ifExpr(se);
				 if ( type != Exec.NoType && !isInteger(type) && type != Exec.StringT )
						throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
								Exec.types[type]+"- expected types: int or char or string");
				 Transl.ins(Operator.LJEL,0); 
				 lex.accept(Token.CSPAR);
		 		 while ( lex.currToken() == Token.OSPAR ) {
		 			 lex.nextToken();
					 if ( inFirst(first_expr) ) {
					 	 Transl.ins(Operator.HDFLDV,0);
						 type=ifExpr(se);
						 if ( type != Exec.NoType && !isInteger(type) && type != Exec.StringT )
								throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
										Exec.types[type]+"- expected types: int or char or string");
						 Transl.ins(Operator.LJEL,0); 
						 lex.accept(Token.CSPAR);
					 }				 
					 else 
						 throw raiseExc(first_expr);
				 }
			 }
			 else 
				 throw raiseExc(first_expr);	
 		 }
 		 if ( lex.currToken()==Token.OPASSIGN ) {
 			 Token assOpType = lex.currAssOp();
		 	 lex.printH_OpAssign(assOpType,false); // print op-assign without spaces on history
 			 opAssign(Exec.NoType, listElOrField, assOpType,se); 
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
				 int typeExp=ifExpr(se);
				 if ( typeExp >= Exec.FuncT )
						throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
								Exec.types[Exec.FuncT]+"- a function cannot be assigned to a variable");		 		 		
			 }
 		 }
		 if ( listElOrField )
			 Transl.ins(Operator.MODVEL,0);
		 else
			 Transl.ins(Operator.MODV,0);	
	 }


	//**<ifExpr> ->  <orExpr(se)> [ QMARK <ifExpr> COLON <ifExpr> ]
	 static int ifExpr ( boolean se ) throws Exc {
		 int cexpr_t = orExpr(se);
		 if ( lex.currToken() == Token.QMARK ) {
			 if ( cexpr_t != Exec.BoolT && cexpr_t != Exec.NoType ) //**
					throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
								Exec.types[cexpr_t]+"- expected type: boolean");
			 lex.nextToken();
			 int iJumpE = Transl.ins(Operator.JUMPZ,0);
			 int ifExpr1_t = ifExpr(se);
			 int iJumpT = Transl.ins(Operator.JUMP,0);
			 lex.printH_Colon(true); // print spaced COLON on history
			 lex.accept(Token.COLON );
			 int labelE = Transl.ins(Operator.NEXT,0);
			 Transl.modOperand(iJumpE, labelE);
			 int ifExpr2_t = ifExpr(se);
			 int labelT = Transl.ins(Operator.NEXT,0);
			 Transl.modOperand(iJumpT, labelT);
			 if ( ifExpr2_t != ifExpr1_t ) 
				 return ifExpr1_t;
			 else
				 return Exec.NoType;
		 }
		 else
			 return cexpr_t;
	}

	// <orExpr(se)> -> <andExpr> <orExpr_next>
	 static int orExpr ( boolean se ) throws Exc {
		 if ( inFirst(first_orExpr) ) {
			 int cterm_t = andExpr(se);
			 if ( lex.currToken() == Token.OR ) { 
				 if ( cterm_t != Exec.BoolT && cterm_t != Exec.NoType ) 
						throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
									Exec.types[cterm_t]+"- expected type: boolean");
				 int iJumpE = Transl.ins(Operator.JUMPNZ,0);
				 int labelE = orExpr_next(se);
				 Transl.modOperand(iJumpE, labelE);
				 return Exec.BoolT;
			 }
			 else
				 if ( inFirst(follows_orExpr_next) )
					 return cterm_t;
				 else 
					 throw raiseExc(follows_orExpr_next);
		 }
		 else 
			 throw raiseExc(first_orExpr);
	}
	 
	 // <orExpr_next> -> OR <andExpr> <orExpr_next> | epsilon
	 static int orExpr_next(boolean se ) throws Exc {
		 lex.nextToken(); // skip token OR
		 int cterm_t = andExpr(se);
		 if ( cterm_t != Exec.BoolT && cterm_t != Exec.NoType )
				throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
						Exec.types[cterm_t]+"- expected type: boolean");
		 int labelE;
		 if ( lex.currToken() == Token.OR ) { 
			 int iJumpE = Transl.ins(Operator.JUMPNZ,0);
			 labelE = orExpr_next(se);
			 Transl.modOperand(iJumpE, labelE);
		 }
		 else 
			 if (inFirst(follows_orExpr_next)  ){
				 int iJ1 = Transl.ins(Operator.JUMP,0);
				 labelE =  Transl.ins(Operator.PUSHB,1);
				 Transl.ins(Operator.NEXT,0);
				 Transl.modOperand(iJ1, labelE+1);
			 }
			 else
				 throw raiseExc(follows_orExpr_next);
		 return labelE;
	 }



	// <andExpr> ->  <eqExpr> <endExpr_next> 
	 static int andExpr ( boolean se ) throws Exc {
		 if ( inFirst(first_andExpr) )  {  
			 int cfact_t = eqExpr(se);
			 if ( lex.currToken() == Token.AND ) { 
				 if ( cfact_t != Exec.BoolT && cfact_t != Exec.NoType )
						throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
								Exec.types[cfact_t]+"- expected type: boolean");
				 int iJumpE = Transl.ins(Operator.JUMPZ,0);
				 int labelE = andExpr_next(se);
				 Transl.modOperand(iJumpE, labelE);
				 return Exec.BoolT;
			 }
			 else 
				 if ( inFirst(follows_andExpr_next) )
						 return cfact_t;
				 else
					 throw raiseExc(follows_andExpr_next);
		 }
		 else 
			 throw raiseExc(first_andExpr);
	}
	 
	 // <andExpr_next> -> AND <eqExpr> <andExpr_next> | epsilon
	 static int andExpr_next(boolean se ) throws Exc {
		 lex.nextToken();
		 int cfact_t = eqExpr(se);
		 if ( cfact_t != Exec.BoolT && cfact_t != Exec.NoType )
				throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
						Exec.types[cfact_t]+"- expected type: boolean");
		 int labelE;
		 if ( lex.currToken() == Token.AND ) { 
			 int iJumpE = Transl.ins(Operator.JUMPZ,0);
			 labelE = andExpr_next(se);
			 Transl.modOperand(iJumpE, labelE);
		 }
		 else 
			 if ( inFirst(follows_andExpr_next) ) {
				 int iJ1 = Transl.ins(Operator.JUMP,0);
				 labelE =  Transl.ins(Operator.PUSHB,0);
				 Transl.ins(Operator.NEXT,0);
				 Transl.modOperand(iJ1, labelE+1);
			 }
			 else
				 throw raiseExc(follows_andExpr_next);
		 return labelE;
	 }

	//<eqExpr> -> <relExpr> { EQNEQ <relExpr> }
	 static int eqExpr ( boolean se ) throws Exc {
		 if ( inFirst(first_eqExpr) ) { 
			 int expr1_t = relExpr(se);
			 while ( lex.currToken() == Token.EQNEQ ) {
				 EQNEQOpType relOP =lex.EQNEQ_Op(); 
				 String relOpDescr = lex.tokenFullDescr();
				 lex.nextToken();
				 int expr2_t = relExpr(se);
				 if ( !areComparable(expr1_t, expr2_t,relOP) )
					 throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE,
							 Exec.types[expr2_t]+"- it cannot be compared with "+ Exec.types[expr1_t]+
							 " by operator "+relOpDescr);
				 Transl.ins(relOP.relOp,0);
				 expr1_t = Exec.BoolT;
			 }
			return expr1_t;
		 }
		 else
			 throw raiseExc(first_eqExpr);
	}
	 
		//<relExpr> -> <expr> { GTLT <expr> }
	 static int relExpr ( boolean se ) throws Exc {
		 if ( inFirst(first_eqExpr) ) { 
			 int expr1_t = expr(se);
			 while ( lex.currToken() == Token.GTLT ) {
					 GTLTOpType relOP =lex.GTLT_Op(); String relOpDescr = lex.tokenFullDescr();
					 lex.nextToken();
					 if ( !isComparable(expr1_t,relOP) )
						throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, " operator "+relOpDescr+" for type '"+
								Exec.types[expr1_t]+"'");
					 int expr2_t = expr(se);
					 if ( !isComparable(expr2_t,relOP) )
							throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, " operator "+relOpDescr+" for type '"+
									Exec.types[expr2_t]+"'");
					 if ( !areComparable(expr1_t, expr2_t,relOP) )
						 throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE,
								 Exec.types[expr2_t]+"- it cannot be compared with '"+ Exec.types[expr1_t]+
								 "' by operator "+relOpDescr);
					 Transl.ins(relOP.relOp,0);
					 expr1_t= Exec.BoolT;
			 }
			return expr1_t;
		 }
		 else
			 throw raiseExc(first_eqExpr);
	}
	 
	//<expr> -> <term> { ( PLUS | MINUS ) <term> }
	 static int expr ( boolean se ) throws Exc {
		 if ( inFirst(first_expr) ) { 
			 final int notype = 0;
			 final int number = 1;
			 final int string = 2;
			 final int list = 3;
			 int currType = notype;
			 boolean cannotBeList = false;
			 boolean firstWhile = true;
			 int term1_t = term(se);
			 if ( currType== number && term1_t != Exec.NoType && !isNumber(term1_t) )
					throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
							Exec.types[term1_t]+"- expected type: double or long or int or char");
			 while ( lex.currToken() == Token.PLUS ||  lex.currToken() == Token.MINUS  ) {
				 boolean isMINUS = lex.currToken() == Token.MINUS;
				 if ( firstWhile ) {
					 if ( !isNumber(term1_t) && term1_t != Exec.ListT && term1_t != Exec.StringT && term1_t != Exec.NoType)
						throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
								Exec.types[term1_t]+"- expected types: double or long or int or char or string or list");
					 if ( isNumber(term1_t) )
						 currType = number; 
					 else
						 if ( term1_t==Exec.StringT  )
							 currType = string; 	
						 else
							 if ( term1_t==Exec.ListT  )
								 currType = list; 								 
					 firstWhile=false;
				 }
				 if ( isMINUS ) {
					 if ( currType==list )
							throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
									"operator '-' is not supported for list");
					 else
						 if ( currType==string )
								throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
										"operator '-' is not supported for string");
					 currType = number;
				 }
				 lex.nextToken();
				 int term2_t = term(se);
				 switch ( currType ) {
				 	case number: 
						 if ( term2_t != Exec.NoType && !isNumber(term2_t) )
								throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
										Exec.types[term2_t]+"- expected type: double or long or int or char");
						 term1_t = mostGenericNumber(term1_t,term2_t);
				 		 break;
				 	case list: 
						 if ( term2_t != Exec.NoType && term2_t != Exec.ListT )
								throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
										Exec.types[term2_t]+"- expected type: list");					 
				 		 break;
				 	case string: 
						 if ( term2_t != Exec.NoType && term2_t != Exec.StringT && term2_t != Exec.CharT )
								throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
										Exec.types[term2_t]+"- expected type: string or char");					 
				 		 break;
				 	case notype:
				 		if ( cannotBeList && term2_t == Exec.ListT )
							throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
									Exec.types[term2_t]+"- expected type: double or long or int or char or string");
						 if ( term2_t==Exec.DoubleT ||  term2_t==Exec.IntT ) {
							 currType = number; term1_t = term2_t;
						 }
						 else
							 if ( term2_t==Exec.StringT  ) {
								 currType = string; term1_t = term2_t;
							 }
							 else
								 if ( term2_t==Exec.ListT  ) {
									 currType = list; term1_t = term2_t;
								 }
								 else
									 if (term2_t == Exec.CharT ) 
										 cannotBeList=true;
									 else
										 if ( term2_t != Exec.NoType )
											throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
													Exec.types[term2_t]+"- expected types: double or long or int or char or string or list");										 
				 }
				 if ( isMINUS )
					 Transl.ins(Operator.SUB,0);
				 else
					 Transl.ins(Operator.ADD,0);
			 }
			 return term1_t;
		 }
		 else
			 throw raiseExc(first_expr);
	}
	 
	//<term(se)> -> <uniTerm(se)> [ ( STAR | DIV | DIVTR | REMINDER) <uniTerm(se)> ]*
	 static int term (  boolean se ) throws Exc {
		 if ( inFirst(first_term) ) { 
			 int unTerm1_t = uniTerm(se);
			 boolean firstWhile = true;
			 while ( lex.currToken() == Token.STAR ||  lex.currToken() == Token.DIV  
					 			||  lex.currToken() == Token.DIVTR ||  lex.currToken() == Token.REMAINDER) {
				 if ( firstWhile  ) {
					 if ( unTerm1_t!=Exec.NoType && !isNumber(unTerm1_t) )
							throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
									Exec.types[unTerm1_t]+"- expected type: double or long or int or char");
				 }
				 else
					 firstWhile=false;
				 int opType = lex.currToken() == Token.STAR ? 1: lex.currToken() == Token.REMAINDER ? 2: 
					 lex.currToken() == Token.DIV ? 3 : 4;	 
				 lex.nextToken();
				 int unTerm2_t = uniTerm(se);
				 if ( unTerm2_t!=Exec.NoType && !isNumber(unTerm2_t) )
						throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
								Exec.types[unTerm2_t]+"- expected type: double or long or int or char");
				 if ( opType == 1 ) {
					 Transl.ins(Operator.MULT,0);
					 unTerm1_t=mostGenericNumber(unTerm1_t,unTerm2_t);
				 }
				 else
					 if ( opType == 2 ) {
						 Transl.ins(Operator.REMAIN,0);
						 unTerm1_t=mostGenericNumber(unTerm1_t,unTerm2_t);
					 }
				 else {
						 Transl.ins(Operator.DIV,0);
						 unTerm1_t=Exec.DoubleT;
						 if ( opType == 4 ) { // operator '//'
							 Transl.ins(Operator.TOINTL,0);
							 unTerm1_t=Exec.IntT;
						 }
				 }
			 }
			 return unTerm1_t;
		 }
		 else
			 throw raiseExc(first_term);
	}

		//<uniTerm> -> { PLUS | MINUS | NOT } <simpleTerm> 
	 static int uniTerm ( boolean se ) throws Exc {
		 boolean isMINUS = false;
		 boolean isNumber = false;
		 boolean isBool = false; 
		 boolean isNOT = false;
		 while ( lex.currToken() == Token.PLUS || lex.currToken() == Token.MINUS
				 || lex.currToken() == Token.EMARK ) {
			 if ( lex.currToken() == Token.EMARK && isNumber )
					throw new Exc_Synt(ErrorType.NOT_NOT_SUPPORTED, 
							" after operator '+' or '-'");
			 if ( (lex.currToken() == Token.PLUS || lex.currToken() == Token.MINUS) 
					 && isBool )
					throw new Exc_Synt(ErrorType.PLUS_MINUS_NOT_SUPPORTED, 
							" after operator '!'");
			 if ( lex.currToken() == Token.PLUS || lex.currToken() == Token.MINUS ) {
				 isNumber = true;
				 if ( lex.currToken() == Token.MINUS )
					 isMINUS = !isMINUS; 
				 lex.nextToken(); 
			 }
			 else
				 if ( lex.currToken() == Token.EMARK ) {
					 isBool=true;
					 isNOT = !isNOT;
					 lex.nextToken();
				 }
		 }
 		 int currT = simpleTerm(se);
		 if ( isNumber && currT != Exec.NoType && !isNumber(currT) )
				throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
						Exec.types[currT]+"- expected type: double or long or int or char");
		 if ( isMINUS )
			 Transl.ins(Operator.NEG,0);
		 if ( isBool ) {
			 if ( currT != Exec.BoolT && currT != Exec.NoType )
					throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
							Exec.types[currT]+"- expected type: boolean");
			 if ( isNOT )
				 Transl.ins(Operator.NOT, 0);
		 }	
		 return currT;
	 }

	//<simpleTerm> -> <fact> { OSPAR <listStringJsonElems> CSPAR } [ CAST TYPEC ] 
	 static int simpleTerm ( boolean se ) throws Exc {
	 	 if ( inFirst(first_simpleTerm) ) { 
	 		 int currT = fact(se);
	 		 while ( lex.currToken() == Token.OSPAR  ) {
		 			 if ( currT !=Exec.NoType && currT != Exec.ListT && 
		 					 currT != Exec.StringT && currT != Exec.JsonT)
							throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
									Exec.types[currT]+"- expected types: list or string or json");
					 lex.nextToken();
					 cannotBeJson=false;
					 currT =listStringJsonElems(se,currT);
					 lex.accept(Token.CSPAR );
				 }
	 		 if ( lex.currToken() == Token.CAST ) {
					 lex.nextToken();
					 if ( lex.currToken() == Token.TYPE ) {
						 int castT = lex.currType();
						 switch(castT) {
						 case Exec.DoubleT:
							 if ( currT==Exec.NoType || currT==Exec.IntT || currT==Exec.LongT || currT==Exec.DoubleT ||
							 	currT==Exec.CharT  ) {
								 	Transl.ins(Operator.TODOUBLE,0);
								 	currT = Exec.DoubleT;
							 }
							 else 
								 if ( currT==Exec.DoubleT )
									 currT=Exec.DoubleT;
								 else
									 throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
										 Exec.types[currT]+"- expected types: int or long or double or char");
							 break;
						 case Exec.LongT:
							 if ( currT==Exec.NoType || currT==Exec.IntT || currT==Exec.DoubleT ||
							 	currT==Exec.CharT  ) {
								 	Transl.ins(Operator.TOLONG,0);
								 	currT = Exec.LongT;
							 }
							 else 
								 if ( currT==Exec.LongT )
									 currT=Exec.LongT;
								 else
									 throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
										 Exec.types[currT]+"- expected types: int or long or double or char");
							 break;
						 case Exec.IntT:
							 if ( currT==Exec.NoType || currT==Exec.LongT || currT==Exec.DoubleT ||
							 	currT==Exec.CharT || currT==Exec.BoolT ) {
								 	Transl.ins(Operator.TOINT,0);
								 	currT = Exec.IntT;
							 }
							 else 
								 if ( currT==Exec.IntT )
									 currT=Exec.IntT;
								 else
									 throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
										 Exec.types[currT]+"- expected types: int or long or double or char or bool");
							 break;
						 case Exec.CharT:
							 if ( currT==Exec.NoType || currT==Exec.IntT || currT==Exec.DoubleT ||
							 				currT==Exec.LongT ) {
								 Transl.ins(Operator.TOCHAR,0);
								 currT = Exec.CharT;
							 }
							 else 
								 if ( currT==Exec.CharT )
									 currT=Exec.CharT;
								 else
									 throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
										 Exec.types[currT]+"- expected types: int or long or double or char");
							 break;
						 case Exec.StringT:
							 if ( currT==Exec.NoType || currT==Exec.ListT  ||
							 		currT==Exec.CharT ) {
								 Transl.ins(Operator.TOSTRING,0);
								 currT = Exec.StringT;
							 }
							 else
								 if ( currT==Exec.StringT )
									 currT = Exec.StringT;
								 else 
									 throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
											 Exec.types[currT]+"- expected types: char or list of chars or string");
							 break;
						 case Exec.ListT:
							 if ( currT==Exec.NoType || currT==Exec.StringT || currT==Exec.JsonT ) {
								 Transl.ins(Operator.TOLIST,0);
								 currT = Exec.ListT;										 
							 }
							 else
								 if ( currT==Exec.ListT )
									 currT = Exec.ListT;
								 else 
									 throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
											Exec.types[currT]+"- expected types: string or json or list");											 
							 break;
						 case Exec.JsonT:
							 if ( currT==Exec.NoType || currT==Exec.ListT ) {
								 Transl.ins(Operator.TOJSON,0);
								 currT = Exec.JsonT;										 
							 }
							 else
								 if ( currT==Exec.JsonT )
									 currT = Exec.JsonT;
								 else 
									 throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
											Exec.types[currT]+"- expected types: list or json");											 
							break;
						 case Exec.TypeT:
							 if ( currT != Exec.MetaTypeT ) {
								 Transl.ins(Operator.TOTYPE,0);
								 currT = Exec.TypeT;
							 }
							 else 
									throw new Exc_Synt(ErrorType.TOTYPE_NOT_SUPPORTED,
											" for metatype 'type'");
							 break;
						default:
							throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
									"- cast to "+Exec.types[castT]+" not supported");
						 }
											 
						 lex.nextToken();					 
					 }
					 else
							throw new Exc_Synt(ErrorType.WRONG_TOKEN, lex.tokenFullDescr()+
									"- expected types: long or int or char or string or list or json or type");						 
				 }
			 return currT;
	 	 }
	 	 else
	 		 throw raiseExc(first_simpleTerm);
	 }

	 // method called by by fact
	 static void getString(String s ) throws Exc {
		 Transl.ins(Operator.SSTRING,0);
		 for ( int i =0; i <s.length(); i++ ) {
			 Transl.ins(Operator.PUSHC, s.charAt(i));
			 Transl.ins(Operator.CSTRING, 0 );
		 }
		 Transl.ins(Operator.ESTRING,0);
	 }

	//<fact> -> ( IDL | ID [ OPAR <callFunct> CPAR [ OPAR <callFunct1> CPAR ]  ] ) | 
	// 				OSPAR [ <constListElems> ] CSPAR  | 
	//               OBRACE [ <fields> ] CBRACE | BUILTIN <factBuiltIn> | 
	//               OPAR  ( <ifExpr(se)> CPAR | <lambda> CPAR [ OPAR <callFunct1> CPAR ] ) 
	// 				 INT | LONG | DOUBLE | CHAR | STRING | NULL | TRUE | FALSE | TYPE
	 static int fact (  boolean se ) throws Exc {
		 if ( lex.currToken() == Token.ID || lex.currToken() == Token.IDL ) {
			 String idName = lex.IDName();
			 boolean hasLabel= lex.currToken()==Token.IDL;
			 String label =   hasLabel? lex.currLabel(): null;
			 int idType = SymbH.setCurrID(idName,hasLabel,label);
			 if ( idType == SymbH.undefID )
					throw new Exc_Synt(ErrorType.UNDEF_ID, " " + idName);
			 int retTypeF=SymbH.currFunctRetType();
			 int retArityF=SymbH.currFunctRetArity();
			 lex.nextToken();
			 if ( lex.currToken() == Token.OPAR ) {
				 if ( idType != SymbH.functType )
						throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
								"found a variable - expected a function");
				 lex.nextToken();
				 callFunct(se,idName);
				 lex.accept(Token.CPAR );
				 if ( lex.currToken()==Token.OPAR ) {
					 if ( retTypeF!=SymbH.functType )
							throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
									"found a function returning a variable - expected a function returning a function");
					 callFunct1(se,retArityF);
					 return Exec.NoType;
				 }
				 else
					 if ( retTypeF==SymbH.functType )
						 return Exec.FuncT+retArityF;
					 else
						 return Exec.NoType;			 
			 }
			 if ( !SymbH.isParam() && idType == SymbH.functType ) { 
				 // case of an external function passed as a argument of a function				 
				 Transl.ins(Operator.PUSHF,SymbH.iCurrVarFunc(), "*-> "+idName);
				 return Exec.FuncT+SymbH.currFunctParam_NP();
			 }
			 if (SymbH.isParam() ) {
				 int nPar = SymbH.nParCurrFunDef();
				 Transl.ins(Operator.LOADARG,nPar-SymbH.iCurrVarFunc());
				 Transl.ins(Operator.DEREF,0);
				 if ( idType == SymbH.functType ) {
					 return Exec.FuncT+SymbH.currFunctParam_NP();
				 }
				 else
					 return Exec.NoType;
			 }
			 int retType=Exec.NoType;
			 if ( SymbH.isLocVar() ) 
				 Transl.ins(Operator.LOADLV,SymbH.iCurrVarFunc());
			 else { // global variable
				 if ( typeDef == isVarDef || typeDef == isComp  ) 
					 if ( !hasLabel )
						 retType = SymbH.typeGV(SymbH.iCurrVarFunc());
				 Transl.ins(Operator.LOADGV,SymbH.iCurrVarFunc());
			 }
			 Transl.ins(Operator.DEREF,0);
			 return retType; 
		 }
		 if ( lex.currToken() == Token.BUILTIN ) { 
			 lex.nextToken();
			 return factBuiltIn(se);
		 }
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
		 if ( lex.currToken() == Token.OBRACE ) {
			 Transl.ins(Operator.SJSON,0);
			 lex.nextToken();
			 if ( lex.currToken() != Token.CBRACE  ) 
				 fields(se);
			 Transl.ins(Operator.EJSON,0);
			 lex.accept(Token.CBRACE );
			 return Exec.JsonT;
	 }
		 if ( lex.currToken() == Token.OPAR ) {
			 lex.nextToken();
			 if ( lex.currToken() == Token.LAMBDA ) {
				 int retType=lambda(false,0,0,0); // false since lambda is not a function parameter 
				 lex.accept(Token.CPAR);
				 if ( lex.currToken() == Token.OPAR ) {
					 callFunct1(se,SymbH.nParLambdaFdef());
					 return Exec.NoType;
				 }
				 else 
					 return retType;	
			 } 
			 else {
				 int ifExpr1_t = ifExpr(se);
				 lex.accept(Token.CPAR );
				 if ( ifExpr1_t >= Exec.FuncT && lex.currToken() == Token.OPAR ) {
					 callFunct1(se,ifExpr1_t-Exec.FuncT);
					 return Exec.NoType;					 
				 }
				 else
					 return ifExpr1_t;
			 }
		 }				 
		 if ( lex.currToken() == Token.DOUBLE ) {
				 Transl.ins(Operator.PUSHD,lex.doubleVal());
				 lex.nextToken();
				 return Exec.DoubleT;
		 }
		 if ( lex.currToken() == Token.INT ) {
			 Transl.ins(Operator.PUSHI,lex.intVal());
			 lex.nextToken();
			 return Exec.IntT;
		 }
		 if ( lex.currToken() == Token.LONG ) {
			 Transl.ins(Operator.PUSHL,Double.longBitsToDouble(lex.longVal()));
			 lex.nextToken();
			 return Exec.LongT;
		 }
		 if ( lex.currToken() == Token.CHAR ) {
				 Transl.ins(Operator.PUSHC,lex.CHARvalue());
				 lex.nextToken();
				 return Exec.CharT;
		 }
		 if ( lex.currToken() == Token.STRING ) {
				 getString(lex.stringVal());
				 if ( lex.isStringOverflow() )
					 ioh.print("\n**Warning: string truncated to max length "+Lex.maxLenString+"\n");
				 lex.nextToken();
				 return Exec.StringT;
		 }
		 if ( lex.currToken() == Token.NULL ) {
			 Transl.ins(Operator.PUSHN,0);
			 lex.nextToken();
			 return Exec.NullT;
		 }
		 if ( lex.currToken() == Token.TRUE ) {
				 Transl.ins(Operator.PUSHB,1);
				 lex.nextToken();
				 return Exec.BoolT;
		 }
		 if ( lex.currToken() == Token.FALSE ) {
				 Transl.ins(Operator.PUSHB,0);
				 lex.nextToken();
				 return Exec.BoolT;
	 	 }
		 if ( lex.currToken() == Token.TYPE ) {
			 if ( lex.currType() == Exec.TypeT ) {
				 Transl.ins(Operator.PUSHMT,0);
				 lex.nextToken();
				 return Exec.MetaTypeT;				 
			 }
			 else { 
				 Transl.ins(Operator.PUSHT,lex.currType());
				 lex.nextToken();
				 return Exec.TypeT;
			 }
		 }
		throw raiseExc(first_fact); 
	}
	 
	 // <fields> -> <ifExpr> COLON <ifExpr> { COMMA <ifExpr> COLON <ifExpr> }
	 static void fields(boolean se) throws Exc {
			if ( inFirst(first_fields) ) { 
				int type = ifExpr(se);
				if ( type != Exec.NoType && type != Exec.StringT )
					throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
							Exec.types[type]+"- expected type: string");
				lex.printH_Colon(true); // print spaced COLON on history
				lex.accept(Token.COLON);
				ifExpr(se);
				Transl.ins(Operator.NEWFLD,0);
				Transl.ins(Operator.CJSON,0);
				while ( lex.currToken() == Token.COMMA || lex.currToken() != Token.CBRACE ) {
					if ( lex.currToken() == Token.COMMA )  {
						lex.printH_Comma(true); // print space after comma in history
						lex.nextToken();
					}
					type = ifExpr(se);
					if ( type != Exec.NoType && type != Exec.StringT )
						throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
								Exec.types[type]+"- expected type: string");
					lex.printH_Colon(true); // print spaced COLON on history
					lex.accept(Token.COLON);
					ifExpr(se);
					Transl.ins(Operator.NEWFLD,0);
					Transl.ins(Operator.CJSON,0);
				}
			}
			 else
				 throw raiseExc(first_fields); 
	 
	 }
	 
		//<factBuiltIn> -> ( ID(EXP | LOG | LEN | TUPLE) OPAR <ifExpr> CPAR | ID1(RAND) OPAR CPAR |
		//               ID(POW) OPAR <ifExpr>  COMMA <ifExpr> CPAR | 
		//				 ID(IND) OPAR <ifExpr>  COMMA <ifExpr> [COMMA <ifExpr>] CPAR |
	 	//				 ID(GTIME) OPAR [ <ifExpr> ] CPAR
	 static int factBuiltIn ( boolean se ) throws Exc{
		 if ( lex.currToken() != Token.ID )
				throw new Exc_Synt(ErrorType.WRONG_TOKEN, lex.tokenFullDescr()+
						"- expected token: 'ID'");						 
		 BIFType builtinF = lex.getBuiltIn();
		 int operand=0;
		 lex.nextToken();
		 lex.accept(Token.OPAR);
		 if ( builtinF != BIFType.RAND && builtinF != BIFType.GDATE 
				 && builtinF != BIFType.PDATE ) { 
			 int ifExpr_t = ifExpr(se);
			 if ( builtinF == BIFType.IND ) {
				 if ( ifExpr_t!=Exec.StringT && ifExpr_t!=Exec.NoType )
						throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
								Exec.types[ifExpr_t]+"- expected type: string");
 				 lex.printH_Comma(false); // no space after comma in history
				 lex.accept(Token.COMMA);
				 ifExpr_t = ifExpr(se);
				 if ( ifExpr_t!=Exec.StringT && ifExpr_t!=Exec.NoType )
							throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
									Exec.types[ifExpr_t]+"- expected type: string");
				 if (lex.currToken()==Token.COMMA ) {
	 				 lex.printH_Comma(false); // no space after comma in history
					 lex.nextToken();
					 ifExpr_t = ifExpr(se);
					 if ( !isInteger(ifExpr_t) && ifExpr_t!=Exec.NoType )
								throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
										Exec.types[ifExpr_t]+"- expected type: int or char");						 
				 }
				 else 
					 Transl.ins(Operator.PUSHI,0);			 
			 }
			 else
				 if ( builtinF == BIFType.ISKEY ) {
					 if ( ifExpr_t!=Exec.JsonT && ifExpr_t!=Exec.NoType )
							throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
									Exec.types[ifExpr_t]+"- expected type: json");
	 				 lex.printH_Comma(false); // no space after comma in history
					 lex.accept(Token.COMMA);
					 ifExpr_t = ifExpr(se);
					 if ( ifExpr_t!=Exec.StringT && ifExpr_t!=Exec.NoType )
								throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
										Exec.types[ifExpr_t]+"- expected type: string");
				 }
				 else
					 if ( builtinF == BIFType.LEN ) { 
						 if ( ifExpr_t!=Exec.ListT && ifExpr_t!=Exec.StringT && ifExpr_t!=Exec.JsonT && ifExpr_t!=Exec.NoType )
								throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
										Exec.types[ifExpr_t]+"- expected type: string or list or json");
					 }
					 else
						 if ( builtinF == BIFType.TUPLE ) { 
							 if ( ifExpr_t!=Exec.JsonT && ifExpr_t!=Exec.NoType && ifExpr_t!=Exec.LongT )
									throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
											Exec.types[ifExpr_t]+"- expected type: json or long");						 
						 }
						 else {
				 			 if ( !isNumber(ifExpr_t) && ifExpr_t!=Exec.NoType )
									throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
											Exec.types[ifExpr_t]+"- expected type: double or long or int or char");
				 			 if ( builtinF == BIFType.POW ) {
				 				 lex.printH_Comma(false); // no space after comma in history
				 				 lex.accept(Token.COMMA);
				 				 ifExpr_t = ifExpr(se);
				 	 			 if ( !isNumber(ifExpr_t) && ifExpr_t!=Exec.NoType )
				 					throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
											Exec.types[ifExpr_t]+"- expected type: double or long or int or char");
				 			 }
						 }
		 }
		 else
//				 if (builtinF == BIFType.GDATE ) {
// 					 if ( lex.currToken()==Token.CPAR )
// 						 operand=0;
// 				 }	
			 if ( builtinF == BIFType.PDATE ) {
				 int ifExpr_t = ifExpr(se);
				 if ( ifExpr_t!=Exec.LongT && ifExpr_t!=Exec.NoType )
							throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
									Exec.types[ifExpr_t]+"- expected type: long");
				 if ( lex.currToken()==Token.CPAR )
					 operand=0;
				 else {
					 lex.accept(Token.COMMA);
					 ifExpr_t = ifExpr(se);
					 if ( ifExpr_t!=Exec.StringT && ifExpr_t!=Exec.NoType )
							throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
									Exec.types[ifExpr_t]+"- expected type: string");
					 operand=1;
				 }			 
			 }
			 else {
				 if (builtinF == BIFType.GDATE ) 
					 if ( lex.currToken()==Token.CPAR )
						 operand=0;
					 else {
						 int ifExpr_t = ifExpr(se);
						 if ( ifExpr_t!=Exec.StringT && ifExpr_t!=Exec.NoType )
									throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
											Exec.types[ifExpr_t]+"- expected type: string");
						 lex.accept(Token.COMMA);
						 ifExpr_t = ifExpr(se);
						 if ( ifExpr_t!=Exec.StringT && ifExpr_t!=Exec.NoType )
								throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
										Exec.types[ifExpr_t]+"- expected type: string");
						 operand=1;
					 }			 						 
			 }
			lex.accept(Token.CPAR);
			Transl.ins(builtinF.biOp, operand);
			if ( builtinF == BIFType.LEN || builtinF == BIFType.IND )
				return Exec.IntT;
			else
				if ( builtinF == BIFType.ISKEY )
					return Exec.BoolT;
				else
					if ( builtinF == BIFType.GDATE )
						return Exec.LongT;
					else
						if ( builtinF == BIFType.PDATE )
							return Exec.StringT;
						else
							if ( builtinF == BIFType.TUPLE )
								return Exec.ListT;
							else						
								return Exec.DoubleT;		 
	 }


	// <callFunct> -> [ ( <ifExpr> | <lambda> ) { COMMA ( <ifExpr> | <lambda> ) } ]
	 static void callFunct (  boolean se, String idN ) throws Exc {
		 int codeFunct=-1, iFunctCalled=-1, iFunctParam=-1;
		 boolean isFunctParameter = SymbH.isParam();
		 if ( isFunctParameter ) {
			 iFunctParam = SymbH.iCurrVarFunc(); // equals to the parameter index
			 //iFunctCalled=-1; // it is the the function currently defined
		 }
		 else {
			 codeFunct = SymbH.iCurrVarFunc(); // equals to the relative function index
			 iFunctCalled = SymbH.iCurrVarFuncTot(); // equals to the absolute function index
			 if ( !se && SymbH.hasSideEffect(iFunctCalled) )
				 throw new Exc_Synt(ErrorType.SIDE_EFFECT, ": " + idN);			 
		 }
		 int nFormPar = isFunctParameter? SymbH.currFunctParam_NP():
			 								SymbH.nPar(iFunctCalled); 
		 int iPar = 0;
		 if ( inFirst(first_callFunct) ) {
			 boolean first = true;
			 do {
				 if ( ! first ) {
					 lex.printH_Comma(false); // no space after comma in history
					 lex.nextToken();
				 }
			 	 if ( iPar == nFormPar )
			 		 throw new Exc_Synt(ErrorType.WRONG_ACT_PAR_NUM, 
							"- expected number: "+nFormPar);
				 int formPar_t = isFunctParameter? SymbH.varType:
					 								SymbH.parT(iFunctCalled,iPar);
				 if ( lex.currToken() == Token.LAMBDA ) {
					 if ( formPar_t != SymbH.functType )
							throw new Exc_Synt(ErrorType.WRONG_ACT_PARAM, "- expected variable type");
					 lambda(true,formPar_t,iFunctCalled,iPar);
					 if ( lex.currToken() != Token.COMMA &&  lex.currToken() != Token.CPAR ) 
						 throw new Exc_Sem(ErrorType.WRONG_LAMBDA, 
								"-- expected ',' or ')' to terminate lambda function definition - found " + lex.tokenFullDescr());					 
				 }
				 else {
					 int ifExpr1_t = ifExpr(se);
					 if ( ifExpr1_t >= Exec.FuncT && formPar_t != SymbH.functType )
							throw new Exc_Synt(ErrorType.WRONG_ACT_PARAM, 
									"- expected variable type");
					 if ( ifExpr1_t < Exec.FuncT && formPar_t == SymbH.functType )
							throw new Exc_Synt(ErrorType.WRONG_ACT_PARAM, 
									"- expected function type");
					 int iActFPar_NP; // arity of the i-th function parameter
					 if ( formPar_t == SymbH.functType  ) { //
						 if ( SymbH.isParam() ) 
							 iActFPar_NP = SymbH.currFunctParam_NP(); 
						 else {
							 int iFP = SymbH.iCurrVarFuncTot();
							 if ( SymbH.hasFunctParam(iFP) )
									throw new Exc_Synt(ErrorType.WRONG_ACT_PARAM, 
											" an actual parameter function cannot have function parameters");
							 if ( SymbH.hasSideEffect(iFP) )
									throw new Exc_Synt(ErrorType.WRONG_ACT_PARAM, 
											" an actual parameter function cannot have side effects");
							 if ( SymbH.currFunctRetType() == SymbH.functType  )
									throw new Exc_Synt(ErrorType.WRONG_ACT_PARAM, 
											" an actual parameter function cannot return a function");
							 iActFPar_NP = SymbH.nPar(iFP);
						 }
						 if ( !SymbH.equivSignature(iFunctCalled,iPar, iActFPar_NP) ) 
								throw new Exc_Synt(ErrorType.WRONG_ACT_PARAM, 
										"- actual and formal function parameters have different arity");
						 }
				 }
				 iPar++; 
				 first = false;
			 } while ( lex.currToken() == Token.COMMA );
		 }
		 if ( iPar != nFormPar )
				throw new Exc_Synt(ErrorType.WRONG_ACT_PAR_NUM, 
						iPar+"- expected number: "+nFormPar);
		 if ( isFunctParameter ) {
			 int nPar = SymbH.nParCurrFunDef();
			 Transl.ins(Operator.LOADARG,nPar-iFunctParam);
			 Transl.ins(Operator.DEREF,0);
			 Transl.ins(Operator.CALLS,0,"-> ("+idN+")");
		 }
		 else
			 Transl.ins(Operator.CALL,codeFunct,"-> "+idN);
		 return;
	}

		// <callFunct1> -> [ <ifExpr>  { COMMA <ifExpr> } ]
	 static void callFunct1 (  boolean se, int nFormPar ) throws Exc {
		 Transl.ins(Operator.STOREF, 0);
		 lex.nextToken();
		 int iPar = 0;
		 if ( inFirst(first_callFunct1) ) {
			 boolean first = true;
			 do {
				 if ( ! first ) {
					 lex.printH_Comma(false); // no space after comma in history
					 lex.nextToken();
				 }
			 	 if ( iPar == nFormPar )
			 		 throw new Exc_Synt(ErrorType.WRONG_ACT_PAR_NUM, 
							"- expected number: "+nFormPar);
				 int ifExpr1_t = ifExpr(se);
				 if ( ifExpr1_t >= Exec.FuncT )
						throw new Exc_Synt(ErrorType.WRONG_ACT_PARAM, 
								"- expected variable type");
				 iPar++; 
				 first = false;
			 } while ( lex.currToken() == Token.COMMA );
		 }
		 if ( iPar != nFormPar )
				throw new Exc_Synt(ErrorType.WRONG_ACT_PAR_NUM, 
						iPar+"- expected number: "+nFormPar);
		 Transl.ins(Operator.GETF,0);
		 Transl.ins(Operator.CALLS,0,"*-> returned function ");
		 lex.accept(Token.CPAR);
		 return;
	}
	 
	// <lambda> -> LAMBDA [ <formalParLambda> ] COLON <ifExpr>
	static int lambda (boolean isParam, int formPar_t, int iFunctCalled, int iPar ) throws Exc {
		 History.addChar(' ');
		 lex.nextToken(); // skip LAMBDA
		 SymbH.startLambda();
		 int k=SymbH.nLambda()+1;
		 Transl.ins(Operator.PUSHF,-k, "*-> Lambda "+k);		 
	 	 int indSL=Transl.ins(Operator.START,0,"* Lambda "+k);
	 	 int nLP = 0;
		 if ( lex.currToken() != Token.COLON ) {
			 formalParLambda( ); nLP++;
			 while ( lex.currToken() == Token.COMMA ) {
				 lex.printH_Comma(false); // print space after comma in history
				 lex.nextToken();
				 formalParLambda( ); nLP++;
			 }
		 } 	
		 lex.printH_Colon(false); // no space after COLON on history
		 lex.accept(Token.COLON);
		 // check equivalence with formal parameter
		 if ( isParam ) {
			 if ( !SymbH.equivLambdaSignature(iFunctCalled,iPar) )
				 throw new Exc_Synt(ErrorType.WRONG_ACT_PARAM, 
						"- actual and formal function parameters have different arity");	
		 }
//		 else
//			 if ( !SymbH.checkLambdaRetArity(nLP))
//				 throw new Exc_Synt(ErrorType.WRONG_LAMBDA, 
//							"- actual and formal function parameters have different arity");					 
		 int retType = ifExpr(false); // no side effects
	 	 if ( retType>=Exec.FuncT )
     	 	throw new Exc_Synt(ErrorType.WRONG_RET_TYPE, ": a lambda function cannot return a function");
	 	 Transl.ins(Operator.RETURN,SymbH.nParLambdaFdef());
	 	 SymbH.endLambaFdef(Transl.extractLambda(indSL));
	 	 if ( SymbH.useFunctParams() ) {
	 		Transl.ins(Operator.PUSHI, SymbH.nParFdef());
	 		Transl.ins(Operator.BUILDS,SymbH.nParLambdaFdef(), "*->*-> Lambda "+k);
	 	 }
	 	 return Exec.FuncT+nLP;
	}
	
	// <formalParLambda> -> ID [ COMMA ID]* 
	static void formalParLambda () throws Exc {
		 if ( lex.currToken() == Token.ID ) {
			 String idN = lex.IDName();
			 lex.nextToken();
			 SymbH.addParLambda(idN); 
		 }
		 else
				throw new Exc_Sem(ErrorType.WRONG_FUNCT_DEF, 
						"-- expected ID for lambda function parameter - found " + lex.tokenFullDescr());					 
	}

	// <constListElems> ->  <ifExpr> [ [COMMA] <ifExpr> ]* [ BAR <ifExpr> ]
	 static boolean constListElems ( boolean se ) throws Exc {
		if ( inFirst(first_constListElems) ) { 
			boolean isApp = false;
			ifExpr(se);
			Transl.ins(Operator.HLIST,0);
			while ( lex.currToken() == Token.COMMA || ( lex.currToken() != Token.BAR && 
					lex.currToken() != Token.CSPAR ) ) {
				if ( lex.currToken() == Token.COMMA ) {
					lex.printH_Comma(true); // print space after comma in history
					lex.nextToken();
				}
				ifExpr(se);
				Transl.ins(Operator.CLIST,0);
			}
			if ( lex.currToken() == Token.BAR ) {
				isApp = true;
				lex.nextToken();
				int ifExpr3_t = ifExpr(se);
				if ( ifExpr3_t != Exec.ListT && ifExpr3_t != Exec.NoType )
					throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
							Exec.types[ifExpr3_t]+"- expected type: list");
				Transl.ins(Operator.ALIST,0);
			}
			return isApp;
		}
		 else
			 throw raiseExc(first_constListElems); 
	}

	// <listStringJsonElems> ->  DOT | GTLT(GT) | <ifExpr> [ COLON [ <ifExpr> ] ] | 
	// 									COLON [ <ifExpr> ]
	 static int listStringJsonElems ( boolean se, int currT) throws Exc {
		 if ( currT != Exec.ListT && currT != Exec.StringT && currT != Exec.JsonT && currT != Exec.NoType )
				throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
						Exec.types[currT]+"- expected types: list or string or json");
		 if ( lex.currToken() == Token.DOT ) { // A[.] - head of a list
				if ( currT != Exec.ListT && currT != Exec.NoType )
					throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
							Exec.types[currT]+" - expected type: list");
				 lex.nextToken();
				 Transl.ins(Operator.HEAD,0);
				 cannotBeJson=false;
				 return Exec.NoType;
		 }
		 if ( lex.currToken() == Token.GTLT ) // A[>] or A[>i] - tail or subtail of a list
			 if ( lex.GTLT_Op() != GTLTOpType.GT )
				throw new Exc_Synt(ErrorType.WRONG_TOKEN, 
						lex.currTokenDescr() +"- expected '>'");
			 else {
				 if ( currT != Exec.ListT && currT != Exec.NoType )
						throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
								Exec.types[currT]+" - expected type: list");
				 lex.nextToken();
				 if ( lex.currToken() == Token.CSPAR )
					 Transl.ins(Operator.TAIL,0); // case A[>]
				 else {
					 int ifExpr_t = ifExpr(se);
					 if ( !isInteger(ifExpr_t) && ifExpr_t != Exec.NoType )
							throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
									Exec.types[ifExpr_t]+" - expected type: integer or char");
					 Transl.ins(Operator.LISTEL,0); // case A[>i]		
					 Transl.ins(Operator.TAIL,0); //
				 }				 
				 return Exec.ListT;
			 }
		 if ( lex.currToken() == Token.COLON ) { // A[:] or A[:i]
		 	 lex.printH_Colon(false); // print non-spaced COLON on history
			 lex.nextToken();
			 if ( lex.currToken() == Token.CSPAR ) { // A[:]
				 if ( currT == Exec.ListT ) {
					 Transl.ins(Operator.PUSHI,0);
					 Transl.ins(Operator.LCLONE,0);
					 return Exec.ListT;
				 }
				 else 
					 if ( currT == Exec.StringT )
						 return Exec.StringT;
					 else 
						 if ( currT == Exec.JsonT ) {
							 Transl.ins(Operator.JCLONE,0);
							 return Exec.JsonT;
						 }
						 else {
							 Transl.ins(Operator.LSJCLONE,0);
							 cannotBeJson=false;
							 return Exec.NoType;
						 }					 
			 }
			 else { // A[:i]
				 if ( currT == Exec.JsonT )
						throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
								"json - expected type: list or string");
				 cannotBeJson=true;
				 Transl.ins(Operator.PUSHI,0);
				 int ifExpr1_t = ifExpr(se);
				 return checkElement3(currT,Exec.IntT,ifExpr1_t);
			 }
	     }
		 if ( inFirst(first_ifExpr) ) { // A[i] or A[i:] or A[i:j]
					 int ifExpr2_t = ifExpr(se);
					 if ( lex.currToken() == Token.CSPAR ) // A[i]
						 return checkElement1 (currT,ifExpr2_t );
					 else {
						 if ( currT == Exec.JsonT )
								throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
										"json - expected type: list or string");
						 lex.printH_Colon(false); // print non-spaced COLON on history
						 cannotBeJson=true;
						 lex.accept(Token.COLON);
						 if ( lex.currToken() == Token.CSPAR ) // A[i:]
							 return checkElement2 (currT,ifExpr2_t );
						 else { // A[i:j]
							 int ifExpr3_t = ifExpr(se);
							 return checkElement3(currT,ifExpr2_t,ifExpr3_t);
						 }
					 }
				 }
		 throw raiseExc(first_listElems); 
	}
	 
	 // method called by <listStringElems>
	 static int checkElement1 ( int currT, int exprT ) throws Exc  { // A[i]
		 if ( currT == Exec.JsonT ) 
			 if ( exprT==Exec.StringT || exprT==Exec.NoType ) {
				 Transl.ins(Operator.FLDFIND,0);
				 Transl.ins(Operator.FLDVAL,0);
				 return Exec.NoType;
			 }
			 else
				throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
							Exec.types[exprT]+"- expected type: string");
		 if ( currT == Exec.ListT ) 
			 if ( isInteger(exprT) || exprT==Exec.NoType ) {
				 Transl.ins(Operator.LISTEL,0);
				 Transl.ins(Operator.HEAD,0);
				 cannotBeJson=false;
				 return Exec.NoType;
			 }
			 else
				throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
							Exec.types[exprT]+"- expected types: int or char");
		 if ( currT == Exec.StringT ) {
			 if ( isInteger(exprT) || exprT == Exec.NoType ) {
				 Transl.ins(Operator.STRINGEL,0);
				 return Exec.CharT;
			 }
			 else
				throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
							 Exec.types[exprT]+"- expected types: int or char or string");
		 }
		 // currT is now equal to NullT
		 if ( cannotBeJson && exprT==Exec.StringT )
			 throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
					 Exec.types[exprT]+"list or string - expected type: json");	 			 
		 if ( isInteger(exprT) ) {
			 Transl.ins(Operator.LSELV,0); 
			 return Exec.NoType;
		 }
		 else
			 if ( exprT==Exec.StringT ) {
				 Transl.ins(Operator.FLDFIND,0);
				 Transl.ins(Operator.FLDVAL,0);
				 return Exec.NoType;
			 }
			 else			 
				 if ( exprT == Exec.NoType ) {
					 Transl.ins(Operator.LSJELV,0);
					 return Exec.NoType;
				 }
				 else
					 throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
									 Exec.types[exprT]+"- expected types: int or char or string");	 
	 }

	 // method called by <listStringElems>
	 static int checkElement2 ( int currT, int exprT ) throws Exc  { // A[i:]
		 if ( currT == Exec.ListT ) 
			 if ( isInteger(exprT) || exprT==Exec.NoType ) {
				 Transl.ins(Operator.LCLONE,0);
				 return Exec.ListT;
			 }
			 else
				throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
							Exec.types[exprT]+"- expected types: int or char");
		 if ( currT == Exec.StringT ) {
			 if ( isInteger(exprT) || exprT == Exec.NoType ) {
				 Transl.ins(Operator.SUBSTR,0);
				 return Exec.StringT;
			 }
			 else
				 throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
								 Exec.types[exprT]+"- expected types: int or char");
		 }
		 // currT is now equal to NullT
		 if ( isInteger(exprT) || exprT == Exec.NoType ) {
			 Transl.ins(Operator.LSCLONE,0);
			 cannotBeJson=true;
			 return Exec.NoType;
		 }
		 else
			 throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
							 Exec.types[exprT]+"- expected types: int or char");	 
	 }

	 // method called by <listStringElems>
	 static int checkElement3 ( int currT, int exprT1, int exprT2 ) throws Exc  { // A[i:j]
		 if ( currT == Exec.ListT ) 
			 if ( isInteger(exprT1) || exprT1==Exec.NoType ) 
				 if ( isInteger(exprT2) || exprT2==Exec.NoType ){
					 Transl.ins(Operator.LCLONE,1);
					 return Exec.ListT;
				 }
				 else
					throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
								Exec.types[exprT2]+"- expected types: int or char");
			 else
					throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
							Exec.types[exprT1]+"- expected types: int or char");
		 if ( currT == Exec.StringT ) {
			 if ( isInteger(exprT1) || exprT1 == Exec.NoType ) 
				 if ( isInteger(exprT2) ){
					 Transl.ins(Operator.SUBSTR,1);
					 return Exec.StringT;
				 }
				 else
					 if ( exprT2 == Exec.NoType  ) {
						 Transl.ins(Operator.LSCLONE,1);
						 cannotBeJson=true;
						 return Exec.NoType;				 
					 }
					 else
						 throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
								 Exec.types[exprT2]+"- expected types: int or char");
		 }
		 // currT is now equal to NullT
		 if ( isInteger(exprT1) || exprT1 == Exec.NoType ) 
			 if ( isInteger(exprT2) || exprT2 == Exec.NoType || exprT2 == Exec.StringT ){
				 Transl.ins(Operator.LSCLONE,1);
				 cannotBeJson=true;
				 return Exec.NoType;
			 }
			 else
				 throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
						 Exec.types[exprT2]+"- expected types: int or char");
		 else			 
			 throw new Exc_Synt(ErrorType.WRONG_EXP_TYPE, 
							 Exec.types[exprT1]+"- expected type: int or char");	 
	 }
	 
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
		else
			ioh.errorNoSkip(e);
	} 

	 static boolean isInteger( int type ) {
		 return type==Exec.IntT || type==Exec.CharT;
	 }

	 static boolean isLongInteger( int type ) {
		 return type==Exec.LongT || type==Exec.IntT || type==Exec.CharT;
	 }

	 static boolean isComparable ( int type, GTLTOpType relOp ) {
		return type==Exec.NoType || isNumber(type) || type==Exec.StringT || 
						type==Exec.BoolT || type==Exec.NullT;
	 }

		static boolean areComparable ( int type1, int type2, EQNEQOpType relOp ) {
			if ( type1 == Exec.NoType || type2 == Exec.NoType )
				return true;
			if ( isNumber(type1) && isNumber(type2) )
				return true;
			if ( type1 == Exec.NullT || type2 == Exec.NullT )
				return true;
			return type1==type2 || (type1==Exec.TypeT && type2==Exec.MetaTypeT) ||
						(type2==Exec.TypeT && type1==Exec.MetaTypeT);
		}
		

		static boolean areComparable ( int type1, int type2, GTLTOpType relOp ) {
			if ( type1 == Exec.NoType || type2 == Exec.NoType )
				return true;
			if ( isNumber(type1) && isNumber(type2) )
				return true;
			if ( type1 == Exec.NullT || type2 == Exec.NullT )
				return true;
			return type1==type2;
		}
		
		static boolean isNumber( int type ) {
			return type==Exec.DoubleT || type==Exec.LongT || type==Exec.CharT || type==Exec.IntT;
		}

		static int mostGenericNumber(int t1, int t2) {
			if ( t1==Exec.NoType || t2==Exec.NoType )
				return Exec.NoType;
			if ( t1==Exec.DoubleT || t2==Exec.DoubleT )
				return Exec.DoubleT;
			if ( t1==Exec.LongT || t2==Exec.LongT )
				return Exec.LongT;
			return Exec.IntT;
		}


	// execution of a command service
	 static void serviceCommandExec () throws Exc {
		 switch ( typeService ) {
		 	case isVarService: OutputH.printVars(); break;
		 	case isFunctService: OutputH.printFuns(ioh.nUtil()); break;
		 	case isReleaseService: OutputH.printRelease(); break;
		 	case isDebugOnService: isDebug = true; OutputH.printDebug(isDebug); break;
		 	case isDebugOffService: isDebug = false; OutputH.printDebug(isDebug); break;
		 	case isTailOptOnService: isTailOpt = true; OutputH.printTailOpt(isTailOpt); break;
		 	case isTailOptOffService: isTailOpt = false; OutputH.printTailOpt(isTailOpt); break;
		 	case isHistoryService: OutputH.printHist(0,true); break;
		 	case isHistoryNumService: OutputH.printHist(numHistory,false); break;
		 	case isLabelService: OutputH.printLabels(); break;
		 	case isAboutService: if ( typeAbout == isAboutBuiltIn) OutputH.printOneBuiltIn(builtInF); else
		 						 if ( typeAbout == isAboutLABEL) OutputH.printOneLabel(idAbout); else
			 				     if ( typeAbout == isAboutFUNCT) OutputH.printOneFunct(idAbout,ioh.nUtil()); else
			 					 if ( typeAbout == isAboutVAR) OutputH.printOneVar(idAbout);
		 						break;
		 	case isSaveService: ioh.saveFile(); break;
		 	case isMemoryService: OutputH.printMemory(); break;
		 	case isMemorySizeService: OutputH.printSize(); break;
		 	case isImportEchoService: ioh.importFile(true); break;
		 	case isImportNoEchoService: ioh.importFile(false); break;
		 	case isClops: ioh.print(Integer.toString(Exec.nExecMicroInstrs())+"\n"); break;
		 	case isExecService: ioh.setExecLine(History.command(numHistory)); break;
		 	default: throw new Exc_Synt(ErrorType.FATAL_ERROR,": unexpected behavior while executing service commands"); 

		 }
	 }
} // end class Parser

