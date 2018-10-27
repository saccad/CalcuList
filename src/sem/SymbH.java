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

package sem;

import error.Exc;
import error.Exc_Sem;
import error.Exc.ErrorType;
//** import synt.Parser; 
import exec.Exec;


public class SymbH {

	public static final int undefID= 0; 
	public static final int varType= 1; 
	public static final int functType= 2; 

	static boolean onGoingDef = false;
	static boolean newDef = false;
	static boolean currFactIdIsFormalParam = false;
	static boolean currFactIdIsLocVar = false;
	static boolean currFactIdIsLabGlob = false;
	static boolean isLambda = false;
	static int iCurrFactID; // relative index of the current identified symbol
	static int iCurrFactIDtot; // absolute index of the current identified symbol
	static int arityCurrFunct; // arity of the current identified function
	static int retTypeCurrFunct; // returned type of the current identified function
	static int retArityCurrFunct; // arity of the function returned by the current identified function
	static int iPrecDef;
	static Symbol currDef;
	static LambdaFunctInfo currLambdaFunct; 
	static String currLabel;
	static String currLabelComment;
	static boolean isVarDef, isFunctDef;

	public static boolean is_FunctDef() {
		return isFunctDef;
	}
	
	public static boolean is_Lambda() {
		return isLambda;
	}
	
	public static void startFunctDef ( String idN ) throws Exc {
		onGoingDef = true; 
		isLambda = false;
		isVarDef=false;
		isFunctDef=true;
		SymbolTableH.startLambdaFuncts();
		currDef = new Symbol(idN,false,"",functType);
		iPrecDef = SymbolTableH.searchID(idN);
		newDef = iPrecDef < 0 ; 
		if ( !newDef && SymbolTableH.tSymb(iPrecDef)!=functType )
			throw new Exc_Sem(ErrorType.WRONG_FUNCT_DEF, " "+idN+
					" has previously defined as variable");
	}

	public static void startVarDef ( String idN, boolean hasLabel, String label ) throws Exc {
		isVarDef=true;
		isFunctDef=false;
		onGoingDef = true; 
		isLambda = false;
		SymbolTableH.startLambdaFuncts();
		currDef = new Symbol(idN,hasLabel,label,varType);
		iPrecDef = SymbolTableH.searchID(idN);
		newDef = iPrecDef < 0 ; 
		if ( !newDef && SymbolTableH.tSymb(iPrecDef)!=varType )
			throw new Exc_Sem(ErrorType.WRONG_VAR_DEF, ": "+idN+
					" has previously defined as function");
		if ( newDef && hasLabel )
			throw new Exc_Sem(ErrorType.UNDEF_LABEL, " "+label);
	}

	public static void startLabelDef ( String label ) throws Exc {
		currLabel = label; currLabelComment=null;
		newDef = SymbolTableH.indLabel(label)>=0? false: true;
		SymbolTableH.initLabVars();
		onGoingDef=true;
	}
	
	public static void startLambda ( ) throws Exc {
		if ( isLambda )
			throw new Exc_Sem(ErrorType.WRONG_LAMBDA, 
					" - a lambda function cannot be defined inside another lambda function");
		isLambda=true;
		if (isFunctDef)
			currLambdaFunct = new LambdaFunctInfo(FunctInfo.currParmsIds,
					currDef.finfo.pTs, currDef.finfo.pFn);
		else {
			currLambdaFunct = new LambdaFunctInfo();
		}
	}
	
	public static int nLambda() {
		return SymbolTableH.nLambdaFuncts;
	}
	
	public static void addLabelComment (String comment ) {
		currLabelComment=comment;
	}
	
	public static void addCurrLabel ( String label ) throws Exc {
		if ( !isLabel(label ))
			throw new Exc_Sem(ErrorType.UNDEF_LABEL, " "+label);
		if ( FunctInfo.searchCurrUsedLabId(label) >= 0 )
			throw new Exc_Sem(ErrorType.DUPL_LABEL, 
			"-- duplicated label "+label);		
		currDef.finfo.addLabel(label);

	}
	
	public static int nFuncts () {
		return SymbolTableH.nFuncts();
	}
	
	public static int addLabVar ( String idN ) throws Exc {
		String idN_full = currLabel+"."+idN;
		// check whether the labeled variable is duplicated	in the current label definition
		if ( SymbolTableH.searchCurrLabID(idN) >= 0 ) 
			throw new Exc_Sem(ErrorType.DUPL_LAB_VAR, " "+idN_full);
		int indVar=-1;
		if ( !newDef ) {
			indVar= SymbolTableH.searchID(idN_full);
			if ( indVar >= 0 ) {
				indVar=SymbolTableH.iVarFunc(indVar);
			}
		}
		SymbolTableH.addPrevLabVar(idN);
		if ( indVar < 0 )
			SymbolTableH.addLabVar(idN_full, currLabel);
		return indVar;
	}	

	public static void noDef(){
		onGoingDef=false;
	}
	
	public static boolean isLabel ( String label ) {
		return SymbolTableH.indLabel(label)>=0;
	}

	public static int tSymb ( String idN ) {
		int j = SymbolTableH.searchID(idN);
		if ( j < 0 )
			return -1;
		return SymbolTableH.tSymb(j);
	}
	
	
	public static void commitLabDef ( ) throws Exc {
		SymbolTableH.endLabVars(currLabel,newDef,currLabelComment);
		onGoingDef = false;
		newDef = false; 
		//hasCurrLabel = false;
	}
	
	public static int indCurrDef () {
		if ( newDef )
			return SymbolTableH.nFuncts();
		else
			return SymbolTableH.iVarFunc(iPrecDef);
	}

	public static void commitVarFunc ( String s ) throws Exc {
		if ( onGoingDef && currDef.tSymb == functType ) {
			if ( newDef ) {
				currDef.finfo.modFunctSource(s);
				SymbolTableH.addFunc(currDef);
			}
			else {
				SymbolTableH.symbols.get(iPrecDef).finfo.code=currDef.finfo.code;
				SymbolTableH.symbols.get(iPrecDef).finfo.modFunctSource(s);
				SymbolTableH.symbols.get(iPrecDef).finfo.modFunctComment(currDef.finfo.functComment());
			}
		}		
		if ( onGoingDef && newDef && currDef.tSymb != functType ) 
				SymbolTableH.addVar(currDef); 
		onGoingDef = false;
		newDef = false;
		if ( currDef.tSymb == functType )
			SymbolTableH.commitLambdaFuncts();
		//hasCurrLabel = false;
	}
	
	public static void commitAnsGV() throws Exc {
		SymbolTableH.addVar(currDef); 	
		onGoingDef = false;
		newDef = false;		
	}
	
	public static void checkGV () throws Exc {
		int nGV=nVar();
		if ( nGV>0 && typeGV(nGV-1)==Exec.RefT )
			SymbolTableH.removeVar();
	}
	
	static CodeUnit fCode ( int iF ) { // called by Linker
		if ( iF >= 0 ) {
			int i = SymbolTableH.searchF(iF);
			if ( i >= 0 )
				return SymbolTableH.symbols.get(i).finfo.code;
		}
		else {
			int k = -iF-1;
			if (k < SymbolTableH.nLambdaFuncts ) 
				return SymbolTableH.lambdaFuncts.get(k).code;
		}
		return null;
	}
		
	public static int nVar() {
		if ( isVarDef && newDef )
			return SymbolTableH.nVar+1;
		else 
			return SymbolTableH.nVar;
	}
	public static int iVar() {
		if ( !newDef )
			return SymbolTableH.symbols.get(iPrecDef).iVarFunc;
		else 
			return SymbolTableH.nVar;
	}
	public static boolean isParam () {
		return currFactIdIsFormalParam;
	}
	public static boolean isLocVar () {
		return currFactIdIsLocVar;
	}
	public static boolean isLabGlobVar () {
		return currFactIdIsLabGlob;
	}
	public static int iCurrVarFunc() {
		return iCurrFactID;
	}
	public static int iCurrVarFuncTot() {
		return iCurrFactIDtot;
	}
	public static int nParFdef () {
		return currDef.finfo.nP();
	}
	public static int nParLambdaFdef () {
		return currLambdaFunct.nP();
	}
	public static void endFdef ( CodeUnit uc ) {
		currDef.finfo.code = uc;
	}
	public static void endLambaFdef ( CodeUnit uc ) throws Exc {
		currLambdaFunct.code = uc;
		SymbolTableH.addLambdaFunc(currLambdaFunct);
		isLambda = false;
	}
	public static void startComp ( ) {
		onGoingDef = false; 
		isLambda = false;
		isVarDef=false;
		isFunctDef=false;
		SymbolTableH.startLambdaFuncts();
	}
	public static void setDefType (int type ) throws Exc {
		currDef.tSymb = type;
		if ( !newDef ) 
			SymbolTableH.setTypeSymb(iPrecDef,type);
	}

	public static int typeDef ( ) throws Exc {
		//** currDef.tSymb = Parser.NoType;
		if ( !newDef ) {
			int pt = SymbolTableH.tSymb(iPrecDef);
			if ( pt == functType )
				throw new Exc_Sem(ErrorType.WRONG_DEF_TYPE, 
						"-- previously defined as a function");
			if ( SymbolTableH.hasLabelSymb(iPrecDef)  )
				currDef.tSymb = pt;
		}
		return currDef.tSymb;
	}

	public static void funcDef ( boolean sideEffect, int nUtil ) throws Exc {
		if ( !newDef ) {
			if ( SymbolTableH.iVarFunc(iPrecDef) < nUtil ) 
				throw new Exc_Sem(ErrorType.WRONG_FUNCT_DEF, ": "+
						" - a utility function cannot be redefined");
				
			if ( SymbolTableH.finfo_sideEffect(iPrecDef) != sideEffect )
				throw new Exc_Sem(ErrorType.WRONG_FUNCT_DEF, ": "+
						" - it has previously defined with different side effect option");
		}
		currDef.finfo = new FunctInfo(sideEffect); 
	}
	
	public static void addFunctComment (String comment ) {
		currDef.finfo.modFunctComment(comment);
	}

	public static int prevListJsonDef ( ) throws Exc {
		if ( !newDef ) {
			int j = SymbolTableH.iVarFunc(iPrecDef);
			int precT = Exec.typeGV(j);
			if ( precT != Exec.ListT && precT != Exec.JsonT )
				throw new Exc_Sem(ErrorType.WRONG_LIST_JSON_USE, 
						"-- the identifier has been previously defined as "+Exec.types[precT]);
			return precT;
		}
		else
			throw new Exc_Sem(ErrorType.WRONG_LIST_JSON_USE, 
					"-- the new identifier cannot be updated");			
	}
	
	public static boolean isNewDef( ) {
		return newDef;
	}
	
	public static int typeGV ( int j ) throws Exc {
		return Exec.typeGV(j);
	}

	public static void endPar () throws Exc {
		if ( !newDef ) {
			if ( SymbolTableH.symbols.get(iPrecDef).finfo.nP() != currDef.finfo.nP() )
				throw new Exc_Sem(ErrorType.WRONG_FORM_PAR_NUM, 
						"-- the number of parameters in the previous definition of the same function is different");
		}
	}
	
	public static void setRetType( int type, int arity ) throws Exc {
		if ( !newDef ) { 
			if ( SymbolTableH.symbols.get(iPrecDef).finfo.typeRet() != type )
				if ( type == functType )
					throw new Exc_Sem(ErrorType.WRONG_RET_TYPE, 
					"-- the function return type was a variable in the previous definition of "+currDef.idSymb);
				else
					throw new Exc_Sem(ErrorType.WRONG_RET_TYPE, 
					"-- the function return type was a function in the previous definition of "+currDef.idSymb);						
			if ( type == functType  ) {
				if ( arity != SymbolTableH.symbols.get(iPrecDef).finfo.arityRet() )
					throw new Exc_Sem(ErrorType.WRONG_RET_TYPE, 
					"-- different arity of the function return type in the previous definition of "+currDef.idSymb);
			}
		}
		currDef.finfo.setRet(type, arity);
	}

	public static void checkRetType( int type, int arity ) throws Exc {
		if ( currDef.finfo.typeRet() != type )
			if ( type == functType )
				throw new Exc_Sem(ErrorType.WRONG_RET_TYPE, 
				"-- the type for the returned function value must be a variable");
			else
				throw new Exc_Sem(ErrorType.WRONG_RET_TYPE, 
				"-- the type for the returned function value must be a function");						
		if ( type == functType  ) {
			if ( arity != currDef.finfo.arityRet() )
				throw new Exc_Sem(ErrorType.WRONG_RET_TYPE, 
				"-- the arity of the returned function must be "+currDef.finfo.arityRet());
		}
	}
	
	public static void addPar ( String idN, int type, int arity ) throws Exc {
		if ( !idN.equals("_") ) {
			if ( idN.equals(currDef.idSymb) )
				throw new Exc_Sem(ErrorType.DUPL_ID, 
						"-- parameter name "+idN+" equal to function name");
			if ( FunctInfo.searchCurrParId(idN) >= 0 )
				throw new Exc_Sem(ErrorType.DUPL_ID, 
				"-- parameter name "+idN+" equal to the name of a previous parameter");
		}
		if ( type == functType ) 
			currDef.finfo.hasFunctParams=true;
		if ( !newDef ) 
			if ( SymbolTableH.symbols.get(iPrecDef).finfo.nP() <= currDef.finfo.nP() )
				throw new Exc_Sem(ErrorType.WRONG_FORM_PAR_NUM, 
				"-- smaller number of parameters in the previous definition of "+currDef.idSymb);
			else
				if ( SymbolTableH.symbols.get(iPrecDef).finfo.typePar(currDef.finfo.nP()) != type )
					if ( type == functType )
						throw new Exc_Sem(ErrorType.WRONG_PAR_TYPE, 
						"-- the function parameter "+idN+" was a variable in the previous definition of "+currDef.idSymb);
					else
						throw new Exc_Sem(ErrorType.WRONG_PAR_TYPE, 
						"-- the variable parameter "+idN+" was a function in the previous definition of "+currDef.idSymb);						
				else
					if ( type == functType  ) {
						if ( arity != SymbolTableH.symbols.get(iPrecDef).finfo.arityFunctPar(currDef.finfo.nP()))
							throw new Exc_Sem(ErrorType.WRONG_PAR_TYPE, 
							"-- different arity of the function parameter "+idN+" in the previous definition of "+currDef.idSymb);
					}
		if ( type == functType ) 
			currDef.finfo.addFunctParam(idN,arity);
		else
			currDef.finfo.addVarParam(idN);
	}
	
	public static void addParLambda ( String idN ) throws Exc {
		if ( !currLambdaFunct.addVarParam(idN) )
			throw new Exc_Sem(ErrorType.DUPL_ID, 
			"-- parameter name "+idN+" equal to the name of a previous parameter");
	}

	public static void addLoc ( String idN ) throws Exc {
		if ( idN.equals(currDef.idSymb) )
			throw new Exc_Sem(ErrorType.DUPL_ID, 
					"-- local variable name "+idN+" equal to function name");
		if ( FunctInfo.searchCurrParId(idN) >= 0 )
			throw new Exc_Sem(ErrorType.DUPL_ID, 
			"-- local variable name "+idN+" equal to the name of a parameter");		
		if ( FunctInfo.searchCurrLocId(idN) >= 0 )
			throw new Exc_Sem(ErrorType.DUPL_ID, 
			"-- local variable name "+idN+" equal to the name of a previous local variable");		
		currDef.finfo.addLoc(idN);
	}
	
	public static boolean useFunctParams ( ) {
		return currLambdaFunct.useFunctParms;
	}
	public static int setCurrID ( String idN, boolean hasLabel, String label ) throws Exc {
		currFactIdIsFormalParam = false;
		currFactIdIsLocVar = false; currFactIdIsLabGlob = false;
		if ( isLambda ) { // case of an on-going lambda function definition
			// check whether the ID is an extended lambda parameter
			// i.e., a lambda parameter or a parameter of the function hosting the lamda definition
			int i = currLambdaFunct.searchCurrParId(idN);
			if ( i >= 0 ) { // is an extended lambda parameter
				currFactIdIsFormalParam = true;
				iCurrFactID = i;
				if ( i >= currLambdaFunct.nFunctParms ) // it is a lambda parameter
					return varType; // it cannot be a function
				else { // it is hosting function parameter
					currLambdaFunct.setUseFunctParms();
					int typePar = currLambdaFunct.typePar(i);
					if ( typePar == functType ) {
						arityCurrFunct = currLambdaFunct.arityFunctPar(i);
						retTypeCurrFunct = varType;
					}
					return typePar;					
				}
			}
			int j = SymbolTableH.searchID(idN);
			if ( j >= 0 ) // the symbol is a function (ok) or a global variable (nok)
				if ( SymbolTableH.symbols.get(j).tSymb == functType ){
					// the symbol is a function
					iCurrFactID = SymbolTableH.symbols.get(j).iVarFunc;
					iCurrFactIDtot = j; 
					retTypeCurrFunct=SymbolTableH.finfo_retType(iCurrFactID);
					retArityCurrFunct=SymbolTableH.finfo_retArity(iCurrFactID);
					return functType;
				}
				else // global variable are not visible inside a lambda definition
					throw new Exc_Sem(ErrorType.INV_VAR, ": "+idN);
			return undefID;			
		}	// end lambda	
		if ( onGoingDef && currDef.tSymb == functType ) {
			// on-going function definition
			if ( !hasLabel ) {
				if ( currDef.idSymb.equals(idN) ) { 
					// recursive call of the function that is being defined
					if ( newDef ) {
						iCurrFactID = SymbolTableH.nFunct;
						arityCurrFunct=currDef.finfo.nP();
						retTypeCurrFunct=currDef.finfo.retT;
						retArityCurrFunct=currDef.finfo.retFn;
					}
					else {
						iCurrFactID = SymbolTableH.symbols.get(iPrecDef).iVarFunc;
						arityCurrFunct=SymbolTableH.symbols.get(iPrecDef).finfo.nP();
						retTypeCurrFunct=SymbolTableH.symbols.get(iPrecDef).finfo.retT;
						retArityCurrFunct=SymbolTableH.symbols.get(iPrecDef).finfo.retFn;						
					}
					iCurrFactIDtot = -1; // it states it is the currently defined function
					return functType;
				}
				int i = FunctInfo.searchCurrParId(idN);
				if ( i >= 0 ) { // is a parameter
					currFactIdIsFormalParam = true;
					iCurrFactID = i;
					int typePar = currDef.finfo.typePar(i);
					if ( typePar == functType ) {
						arityCurrFunct = currDef.finfo.arityFunctPar(i);
						retTypeCurrFunct=varType; // a function parameter cannot return a function
					}
					return typePar;
				}
				i = FunctInfo.searchCurrLocId(idN);
				if ( i >= 0 ) { // is a local variable
					currFactIdIsLocVar = true;
					iCurrFactID = i;
					return varType;
				}
				for ( int j=0; j < FunctInfo.nCurrUsedLabels(); j++ ) {
					// search for labeled global variable with implicit label
					String currLab = FunctInfo.currUsedLabels.get(j);
					i = SymbolTableH.searchID((currLab+"."+idN));
					if ( i >= 0 ) { // it is a labeled global variable with implicit label
						iCurrFactID = SymbolTableH.symbols.get(i).iVarFunc;
						iCurrFactIDtot = i; currFactIdIsLabGlob = true;
						return varType;
					}		
				}
				// idN must be a function different from the function being defined 
				int j = SymbolTableH.searchID(idN);
				if ( j >= 0 ) 
					if ( SymbolTableH.symbols.get(j).tSymb == functType ){
						iCurrFactID = SymbolTableH.symbols.get(j).iVarFunc;
						iCurrFactIDtot = j;
						arityCurrFunct=SymbolTableH.symbols.get(j).finfo.nP();
						retTypeCurrFunct=SymbolTableH.symbols.get(j).finfo.retT;
						retArityCurrFunct=SymbolTableH.symbols.get(j).finfo.retFn;						
						return functType;
					}
					else
						throw new Exc_Sem(ErrorType.INV_VAR, ": "+idN);
					return undefID;
			}
			else { // case of labeled global variable
				int k = FunctInfo.searchCurrUsedLabId(label);
				if ( k < 0 ) 
					throw new Exc_Sem(ErrorType.UNDEF_INV_LABEL, ": "+label);
				int j = SymbolTableH.searchID(idN);
				if ( j >= 0 ) {
					iCurrFactID = SymbolTableH.symbols.get(j).iVarFunc;
					iCurrFactIDtot = j; currFactIdIsLabGlob = true;
					return varType;
				}
				else
					throw new Exc_Sem(ErrorType.UNDEF_LABVAR, ": "+idN);
			}
		}
		// case of global variable definition or of query definition
		if ( onGoingDef && !newDef && currDef.idSymb.equals(idN) ) { 
			// is a global variable that is being redefined 
			iCurrFactID = SymbolTableH.symbols.get(iPrecDef).iVarFunc;
			return varType;			
		}
		// idN is either a function or a global variable
		int j = SymbolTableH.searchID(idN);
		if ( j >= 0 ) {
			iCurrFactID = SymbolTableH.symbols.get(j).iVarFunc;
			iCurrFactIDtot = j; 
			int typeSymb=SymbolTableH.symbols.get(j).tSymb;
			if ( typeSymb ==functType ) {
				arityCurrFunct=SymbolTableH.symbols.get(j).finfo.nP();
				retTypeCurrFunct=SymbolTableH.symbols.get(j).finfo.retT;
				retArityCurrFunct=SymbolTableH.symbols.get(j).finfo.retFn;										
			}
			return typeSymb;
		}
		return undefID; 
	}
	
	public static int iSymbFunc  ( String idN ) throws Exc {
		if ( onGoingDef && currDef.tSymb == functType && currDef.idSymb.equals(idN) )
			return -1;
		else {
			int j = SymbolTableH.searchID(idN);
			if ( j >= 0 && SymbolTableH.symbols.get(j).tSymb==functType)
				return j;
			else
				throw new Exc_Sem(ErrorType.FATAL_ERROR, "-- unexpected parser behavior in calling SymbH");
		}
	}
	
	public static boolean hasSideEffect( int j ) {
		FunctInfo calledFuncInfo = (j == -1 )? currDef.finfo: SymbolTableH.symbols.get(j).finfo;
		return calledFuncInfo.hasSideEffect;
		
	}
	
	public static int parT ( int j, int i ) {
		FunctInfo calledFuncInfo = (j == -1 )? currDef.finfo: SymbolTableH.symbols.get(j).finfo;
		if ( i < calledFuncInfo.nP() )
			return calledFuncInfo.typePar(i);
		else
			return -1;
			//** return Parser.NoType;
	}
	
	public static int nParCurrFunDef (  ) {
		if ( isLambda )
			return currLambdaFunct.nPall();
		else
			return currDef.finfo.nP();
	}
	
	public static int nPar ( int j ) {
		FunctInfo calledFuncInfo = (j == -1 )? currDef.finfo: SymbolTableH.symbols.get(j).finfo;
		return calledFuncInfo.nP();
	}

	public static boolean hasFunctParam ( int j ) {
		FunctInfo calledFuncInfo = (j == -1 )? currDef.finfo: SymbolTableH.symbols.get(j).finfo;
		return calledFuncInfo.hasFunctParam();
	}

	public static int currFunctParam_NP () {
		return arityCurrFunct;
	}
	
	public static int currFunctRetType () {
		return retTypeCurrFunct;
	}

	public static int currFunctRetArity () {
		return retArityCurrFunct;
	}

	public static boolean equivSignature(int i, int iFormFPar, int iActFPar_NP ) {
		//if (isLambda ) {
//			System.out.println("**lambda - arity="+currLambdaFunct.arityFunctPar(iFormFPar));
//			return currLambdaFunct.arityFunctPar(iFormFPar)==iActFPar_NP;
//		}
//		else {
		{
			FunctInfo calledFuncInfo = (i == -1 )? currDef.finfo: SymbolTableH.symbols.get(i).finfo;
			return calledFuncInfo.arityFunctPar(iFormFPar) == iActFPar_NP;
		}
	}
	
	public static boolean equivLambdaSignature(int i, int iFormFPar) {
		FunctInfo calledFuncInfo = (i == -1 )? currDef.finfo: SymbolTableH.symbols.get(i).finfo;
		return calledFuncInfo.arityFunctPar(iFormFPar) == currLambdaFunct.nP();
	}

	public static boolean checkFunctRet( ) {
		return currDef.finfo.typeRet()==functType;
	}

	public static boolean checkLambdaRetArity(  ) {
		return currDef.finfo.arityRet()==currLambdaFunct.nP();
	}

	public static boolean equalArity(int i, int iFormFPar, int iActFPar_NP ) {
		FunctInfo calledFuncInfo = (i == -1 )? currDef.finfo: SymbolTableH.symbols.get(i).finfo;
		return calledFuncInfo.arityFunctPar(iFormFPar) == iActFPar_NP;
	}	
	
	public static int functPar_nP( int i ) {
		return  currDef.finfo.arityFunctPar(i);
	}
	
	public static int iSymb(String ID) {
		return SymbolTableH.searchID(ID);
	}
	
} // end class SymbH

