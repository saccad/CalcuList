package sem;

import error.Exc;

import java.util.ArrayList;

public class LambdaFunctInfo {
	static final int paramsInitCapacity = 20;
	
	boolean useFunctParms;
	int nFunctParms; // number of parameters of the hosting function
	ArrayList<String> currParmsIds; // possible function parameters + lambda parameters
	ArrayList<Integer> fParmsTs; // function parameters types (varType, functType)
	ArrayList<Integer> fParmsFn; // arity for function parameters
	CodeUnit code;
	
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
