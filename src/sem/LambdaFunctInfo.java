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
	
	public boolean useFunctParms;
	public int nFunctParms; // number of parameters of the hosting function
	ArrayList<String> currParmsIds; // possible function parameters + lambda parameters
	ArrayList<Integer> fParmsTs; // function parameters types (0=varType, 1=functType)
	ArrayList<Integer> fParmsFn; // arity for function parameters
	CodeUnit code;
	String source;
	
	int nP() {
		return currParmsIds.size()-nFunctParms;
	}

	int nPall() {
		return currParmsIds.size();
	}

	int searchCurrParId ( String idN ){
		return currParmsIds.indexOf(idN);
	}
	
	int typePar ( int i ) {
		return fParmsTs.get(i);
	}
	
	int arityFunctPar ( int i ) {
		return fParmsFn.get(i);
	}	
	
	public String parmID( int i ) {
		return currParmsIds.get(i);
	}

	public int parmArity( int i ) {
		return fParmsFn.get(i);
	}

	public String source() {
		return source;
	}

	LambdaFunctInfo ( ) {
		currParmsIds= new ArrayList<String>(paramsInitCapacity);
		fParmsTs=null;
		fParmsFn=null;
		nFunctParms = 0;
		useFunctParms = false;
	}

	LambdaFunctInfo ( ArrayList<String> fpars, ArrayList<Integer> fTs,
			ArrayList<Integer> fFn) {
		nFunctParms = fpars.size();
		useFunctParms=false;
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
	}

	void setUseFunctParms() {
		useFunctParms=true;
	}
	
	boolean addVarParam( String idN ) throws Exc {
		int i=searchCurrParId(idN);
		if ( i >= nFunctParms )
			return false;
		if ( i >=0 )
			currParmsIds.set(i, " ");
		currParmsIds.add(idN);
		return true;
	}
	
} // end class LambdaFunctInfo
