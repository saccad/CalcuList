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

public class FunctInfo {
	static  final int paramsInitCapacity = 20;
	static  final int locIdsInitCapacity = 20;
	static  final int labelsInitCapacity = 20;
	static ArrayList<String> currParmsIds = new ArrayList<String>(paramsInitCapacity);
	static ArrayList<String> currLocIds = new ArrayList<String>(locIdsInitCapacity);
	static ArrayList<String> currUsedLabels = new ArrayList<String>(labelsInitCapacity);
	String source;
	String comment;
	boolean hasSideEffect;
	boolean hasFunctParams;
	CodeUnit code;
	ArrayList<Integer> pTs; // parameters types (varType, functType)
	ArrayList<Integer> pFn; // arity for function parameters
	int retT; // return value type (varType, functType)
	int retFn; // arity for return functType

	static int searchCurrParId ( String idN ){
		return currParmsIds.indexOf(idN);
	}

	static int searchCurrLocId ( String idN ){
		return currLocIds.indexOf(idN);
	}

	static int searchCurrUsedLabId ( String idN ){
		return currUsedLabels.indexOf(idN);
	}

	FunctInfo (boolean sideEffect ) {
		currParmsIds.clear(); 
		currLocIds.clear();
		currUsedLabels.clear();
		this.hasSideEffect = sideEffect;
		source=null; comment=null;
		hasFunctParams =false;
		pTs = new ArrayList<Integer> (paramsInitCapacity);
		pFn = new ArrayList<Integer> (paramsInitCapacity);
		retT = SymbH.varType;
		retFn = 0;
	}
	FunctInfo ( FunctInfo fi ) {
		hasSideEffect = fi.hasSideEffect;
		hasFunctParams = fi.hasFunctParams;
		code = fi.code; 
		source = fi.source; comment=fi.comment;
		pTs = new ArrayList<Integer>(fi.pTs);
		pFn = new ArrayList<Integer>(fi.pFn);
		retT=fi.retT;
		retFn=fi.retFn;
	}
	void mod ( FunctInfo fi ) {
		hasSideEffect = fi.hasSideEffect;
		hasFunctParams = fi.hasFunctParams;
		comment=fi.comment;
		code = fi.code; 
		pTs = new ArrayList<Integer>(fi.pTs);
		pFn = new ArrayList<Integer>(fi.pFn);
		retT=fi.retT;
		retFn=fi.retFn;
	}
	void addVarParam( String idN ) throws Exc {
		pTs.add(SymbH.varType); 
		currParmsIds.add(idN);
		pFn.add(0);
	}
	
	String functSource() {
		return source;
	}
	
	void modFunctSource( String s ) {
		source=s;
	}
	
	String functComment() {
		return comment;
	}
	
	void modFunctComment( String c ) {
		comment=c;
	}
	
	void addFunctParam( String idN, int functArity ) throws Exc {
		pTs.add(SymbH.functType); 
		currParmsIds.add(idN); 
		pFn.add(functArity);
		hasFunctParams=true;
	}

	void addLoc( String idN ) throws Exc {
		currLocIds.add(idN);
	}

	void addLabel( String label ) throws Exc {
		currUsedLabels.add(label);
	}

	int typeRet ( ) {
		return retT;
	}
	
	int typePar (int i ) {
		return pTs.get(i);
	}

	int arityRet ( ) {
		return retFn;
	}
	
	void setRet ( int type, int arity ) {
		retT = type;
		retFn = arity;
	}

	int arityFunctPar (int i ) {
		return pFn.get(i);
	}
	
	boolean hasFunctParam () {
		return hasFunctParams;
	}
	
	int nP() {
		return pTs.size();
	}
	
	static int nCurrUsedLabels() {
		return currUsedLabels.size();
	}

} // end class FuncInfo

