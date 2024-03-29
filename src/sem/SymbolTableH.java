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
import java.util.ArrayList;


public class SymbolTableH {
	static final int lambdaFunctsInitCapacity=100;
	static final int labelsInitCapacity=100;
	static final int symbolsInitCapacity=1000;
	static int nSymb = 0;
	static int nVar = 0;
	static int nFunct=0;
	static int nLambdaFuncts=0;
	static int nCommLambdaFuncts = 0; // number of committed lambda functions
	static int nLabels=0;
	static int nCurrLabVarFuncts;
	static int nPrevLabFuncts;
	static int nVarTmp; // including on-going defined labeled variables
	static int nFunctTmp; // including on-going defined labeled functions
	static int nSymbTmp;
	public static ArrayList<Symbol> symbols = new ArrayList<Symbol>(symbolsInitCapacity);
	public static ArrayList<LambdaFunctInfo> lambdaFuncts = new ArrayList<LambdaFunctInfo>(lambdaFunctsInitCapacity);
	static ArrayList<String> labels= new ArrayList<String>(labelsInitCapacity); // labels
	static ArrayList<String> labComments= new ArrayList<String>(labelsInitCapacity); // labels
	static ArrayList<String> currLabVarFuncts= new ArrayList<String>(labelsInitCapacity); // labeled variables that are currently defined
	static ArrayList<Integer> currPrevLabFuncts= new ArrayList<Integer>(labelsInitCapacity); // labeled variables that are currently defined
	static ArrayList<String> currPrevLabFunctNames= new ArrayList<String>(labelsInitCapacity); // labeled variables that are currently defined
	static ArrayList<Integer> currPrevLabFunctArities= new ArrayList<Integer>(labelsInitCapacity); // labeled variables that are currently defined

//	public static void printLambda() {
//		System.out.println("** nL="+nLambdaFuncts+"  nCommLambdaFuncts="+nCommLambdaFuncts);
//	}
	
	static void commitLambdaFuncts () {
		nCommLambdaFuncts = nLambdaFuncts;
	}

	public static int nLambdaFuncts () {
		return nCommLambdaFuncts;
	}

	static void startLambdaFuncts () {
		nLambdaFuncts = nCommLambdaFuncts;
	}
	
	static void initLabVarFuncts () {
		nSymbTmp=nSymb; nVarTmp=nVar; nFunctTmp=nFunct;
		nCurrLabVarFuncts=0; nPrevLabFuncts=0;
	}

	static void addPrevLabFunct ( int iFunct, String idN_full, int arity ) throws Exc {
		currPrevLabFuncts.add(nPrevLabFuncts, iFunct); 
		currPrevLabFunctNames.add(nPrevLabFuncts, idN_full); 
		currPrevLabFunctArities.add(nPrevLabFuncts, arity); 
		nPrevLabFuncts++;	
	}

	static void addCurrLabVarFuncts ( String idN ) throws Exc {
		currLabVarFuncts.add(nCurrLabVarFuncts, idN); nCurrLabVarFuncts++;	
	}

	static void addLabFunct ( String idN, String label, String idN_full, boolean se, int arity ) throws Exc {
		symbols.add(nSymbTmp, new Symbol(idN,true,label,SymbH.functType));
		symbols.get(nSymbTmp).iVarFunc= nFunctTmp;
		symbols.get(nSymbTmp).finfo = new FunctInfo(se); 
		symbols.get(nSymbTmp).finfo.code = Transl.nullBodyFunct (idN_full, nFunctTmp,arity); 
		String source = idN_full+"("; 
		for (int i=0; i<arity; i++) {
			symbols.get(nSymbTmp).finfo.addVarParam("_");
			source += i==0? "_": ", _";
		}
		symbols.get(nSymbTmp).finfo.source = source+") -> null"; 
		nSymbTmp++; nFunctTmp++;
	}
	
	static void addLabVar ( String idN, String label ) throws Exc {
		symbols.add(nSymbTmp, new Symbol(idN,true,label,SymbH.varType));
		symbols.get(nSymbTmp).iVarFunc= nVarTmp;
		nSymbTmp++; nVarTmp++;
	}
	
	static void endLabVarFuncts(String label, boolean isNewLabel, String comment ) throws Exc {
		nVar= nVarTmp;
		nFunct=nFunctTmp;
		nSymb= nSymbTmp;
		for ( int i=0; i<nPrevLabFuncts; i++ ) {
			int k= currPrevLabFuncts.get(i);
			symbols.get(k).finfo.code = 
					Transl.nullBodyFunct (currPrevLabFunctNames.get(i), currPrevLabFuncts.get(i),currPrevLabFunctArities.get(i)); 
			String source = currPrevLabFunctNames.get(i)+"("; 
			int n= symbols.get(k).finfo.nP();
			for (int j=0; j<n; j++) {
				symbols.get(k).finfo.addVarParam("_");
				source += j==0? "_": ", _";
			}
			symbols.get(k).finfo.source = source+") -> null"; 
		}		
		if ( isNewLabel ) {
			labels.add(nLabels,label); labComments.add(nLabels,comment);
			nLabels++;
		}
		else 
			labComments.add(indLabel(label),comment);			
	}
	
	public static String labelComment( int i ) {
		return labComments.get(i);
	}
 
	public static int nSymb() {
		return nSymb;
	}

	public static int iVarFunc ( int i ) {
		return symbols.get(i).iVarFunc;
	}
	
	public static void setTypeSymb(int i, int type ) {
		symbols.get(i).tSymb=type;
	}
	
	public static int tSymb(int i ) {
		return symbols.get(i).tSymb;
	}
	
	public static String idSymb(int i ) {
		return symbols.get(i).idSymb;
	}

	public static String idGV(int i ) {
		for ( int j = 0; j < nSymb; j++ )
			if ( symbols.get(j).tSymb == SymbH.varType &&  symbols.get(j).iVarFunc==i)
				return symbols.get(j).idSymb;
		return null;
	}

	public static boolean hasLabelSymb(int i ) {
		return symbols.get(i).hasLabel;
	}

	public static String labelSymb(int i ) {
		return symbols.get(i).label;
	}

	public static int finfo_nP ( int i ) {
		return symbols.get(i).finfo.nP();
	}

	public static String finfo_source ( int i ) {
		return symbols.get(i).finfo.functSource();
	}
	
	public static String lambda_source ( int i ) {
		return lambdaFuncts.get(i).source;
	}
	
	public static String finfo_comment ( int i ) {
		return symbols.get(i).finfo.comment;
	}
	
	public static int nFuncts() {
		return nFunct;
	}

	public static CodeUnit finfo_code ( int i ) {
		return symbols.get(i).finfo.code;
	}
	
	public static int finfo_typePar ( int i, int j  ) {
		return symbols.get(i).finfo.typePar(j);
	}

	public static int finfo_functParNP ( int i, int j  ) {
		return symbols.get(i).finfo.arityFunctPar(j);
	}
	
	public static int finfo_retType ( int i  ) {
		return symbols.get(i).finfo.typeRet();
	}

	public static int finfo_retArity ( int i  ) {
		return symbols.get(i).finfo.arityRet();
	}

	public static boolean finfo_sideEffect ( int i  ) {
		return symbols.get(i).finfo.hasSideEffect;
	}

	public static boolean finfo_functParams ( int i  ) {
		return symbols.get(i).finfo.hasFunctParams;
	}
	static void addVar ( Symbol s ) throws Exc {
		symbols.add(nSymb, new Symbol(s));
		symbols.get(nSymb).iVarFunc= nVar;
		nSymb++; nVar++;		
	}

	static void removeVar ( ) throws Exc {
		nSymb--; nVar--;		
	}

	static void addFunc ( Symbol s ) throws Exc {
		symbols.add(nSymb, new Symbol(s));
		symbols.get(nSymb).iVarFunc= nFunct;
		symbols.get(nSymb).finfo = new FunctInfo(s.finfo);
		nSymb++; nFunct++;
		
	}


	static void addLambdaFunc ( LambdaFunctInfo f ) throws Exc {
		lambdaFuncts.add(nLambdaFuncts, new LambdaFunctInfo(f));
//		System.out.println("i="+nLambdaFuncts);
//		System.out.println(lambdaFuncts.get(nLambdaFuncts).source);
		nLambdaFuncts++;
		
	}

	static int searchID ( String idN ){
		for ( int i = 0; i < nSymb; i++ )
			if ( symbols.get(i).idSymb.equals(idN) )
				return i;
		return -1;
	}
	
	static int searchCurrLabID ( String idN ){
		for ( int i = 0; i < nCurrLabVarFuncts; i++ )
			if ( currLabVarFuncts.get(i).equals(idN) )
				return i;
		return -1;
	}
	
	public static int indLabel ( String lab ){
		for ( int i = 0; i < nLabels; i++ )
			if ( labels.get(i).equals(lab) )
				return i;
		return -1;
	}

	public static String label ( int i ){
		return labels.get(i);
	}

	public static int nLabels () {
		return nLabels;
	}

	public static int searchF ( int iF ){
		for ( int i = 0; i < nSymb; i++ )
			if ( symbols.get(i).tSymb == SymbH.functType &&  symbols.get(i).iVarFunc==iF)
				return i;
		return -1;
	}
	
} // end class SymbolTable

class Symbol {
	String idSymb;
	String label;
	boolean hasLabel;
	int tSymb; // 0 = no type, 1 = variable, 2 = function
	int iVarFunc;
	FunctInfo finfo;
	Symbol ( String idS ) {
		idSymb = idS;
		label = ""; hasLabel=false; 
	}
	Symbol ( String idS, boolean hasLab, String lab, int tSy ) {
		idSymb = idS;  tSymb = tSy;
		label = lab; hasLabel=hasLab;
		
	}
	Symbol ( String idS, boolean hasLab, String lab ) {
		idSymb = idS; 
		label = lab; hasLabel=hasLab;
		
	}
	Symbol ( Symbol s ) {
		idSymb = s.idSymb;
		tSymb = s.tSymb;
		label = s.label;
		hasLabel = s.hasLabel;
	}
} // end class Symbol
