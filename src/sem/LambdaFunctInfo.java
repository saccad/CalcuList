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

public class LambdaFunctInfo {
	static final int paramsInitCapacity = 20;
	
	// the lambda definition can be included in a (external) function definition 
	// (extended) lambda parameters are proper or external (i.e., of the external function)
	// external parameters precede proper ones and only external parameters can be of type function
	public boolean useFunctParms; // true if the lambda function uses an external parameter of type function
	public boolean seHostFunct; // true if the external function has side effect
	public int nFunctParms; // number of parameters of the hosting function
	// properties of all extended lambda parameters
	ArrayList<String> currParmsIds; // IDs of extended lambda parameters (proper and external ones)
	ArrayList<Integer> fParmsTs; // lambda parameters types (0=varType, 1=functType)
	ArrayList<Integer> fParmsFn; // arity for lambda parameters
	CodeUnit code; // lambda executable code 
	String source; // lambda source code
	
	int nP() { // number of proper lambda parameters
		return currParmsIds.size()-nFunctParms;
	}

	int nPall() { // number of extended lambda parameters (proper and external ones)
		return currParmsIds.size();
	}

	int searchCurrParId ( String idN ){ // index of the parameter idN
		return currParmsIds.indexOf(idN);
	}
	
	int typePar ( int i ) { // varType or functType
		return fParmsTs.get(i);
	}
	
	int arityFunctPar ( int i ) {
		return fParmsFn.get(i);
	}	
	
	public String parmID( int i ) { // name of the parameter with index i
		return currParmsIds.get(i);
	}

	public int parmArity( int i ) {
		return fParmsFn.get(i);
	}

	public String source() {
		return source;
	}

	public boolean seFunctDef() {
		return seHostFunct;
	}

	LambdaFunctInfo ( ) {
		currParmsIds= new ArrayList<String>(paramsInitCapacity);
		fParmsTs=null;
		fParmsFn=null;
		nFunctParms = 0;
		useFunctParms = false;
		seHostFunct=false;
	}

	LambdaFunctInfo ( ArrayList<String> fpars, boolean se,ArrayList<Integer> fTs,
			ArrayList<Integer> fFn) {
		nFunctParms = fpars.size();
		useFunctParms=false;
		seHostFunct=se;
		currParmsIds= new ArrayList<String>(
				Math.max(paramsInitCapacity,nFunctParms));
		fParmsTs= new ArrayList<Integer>(
				Math.max(paramsInitCapacity,nFunctParms));
		fParmsFn= new ArrayList<Integer>(
				Math.max(paramsInitCapacity,nFunctParms));
		for (int i=0; i<nFunctParms; i++ ) {
			currParmsIds.add(fpars.get(i));
			fParmsTs.add(fTs.get(i));
			fParmsFn.add(fFn.get(i));
		}
	}
	
	LambdaFunctInfo ( LambdaFunctInfo fi ) {
		code = fi.code; 
		source = fi.source;
		currParmsIds=new ArrayList<String>(fi.currParmsIds);
		fParmsTs= fi.fParmsTs==null? null: new ArrayList<Integer>(fi.fParmsTs);
		fParmsFn= fi.fParmsTs==null? null: new ArrayList<Integer>(fi.fParmsFn);
		nFunctParms = fi.nFunctParms;
		useFunctParms = fi.useFunctParms; 
		seHostFunct = fi.seHostFunct;
	}

	void setUseFunctParms() {
		useFunctParms=true;
	}
	
	boolean addVarParam( String idN ) throws Exc {
		int i=searchCurrParId(idN);
		if ( i >= nFunctParms ) // duplicated lambda parameter ID
			return false;
		if ( i >=0 ) // lambda parameter ID equal to function parameter ID
			currParmsIds.set(i, " "); // the function parameter ID is obscured
		currParmsIds.add(idN);
		return true;
	}
	
} // end class LambdaFunctInfo
