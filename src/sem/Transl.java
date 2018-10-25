package sem;

import error.Exc;
import error.Exc_Sem;
import error.Exc.ErrorType;
import exec.Instruction;
import exec.Instruction.Operator;


public class Transl {

	static CodeUnit unitCode;
	public static void start ( ) {
		unitCode=new CodeUnit();
	}
	public static int ins( Operator operator, double operand) throws Exc {
		return unitCode.ins (operator, operand, null);
		
	}
	public static int ins( Operator operator, double operand, String comment) throws Exc {
//		if ( unitCode.numInstr >= UnitCode.numMaxInstr )
//			throw new Exc_Sem(ErrorType.LARGE_CODE, "Internal: increase the maximal size of a unit code");
		return unitCode.ins (operator, operand, comment);
		
	}
	public static void modOperand (int i, double operand ) throws Exc {
		if ( i >= unitCode.numInstr() || i < 0 )
			throw new Exc_Sem(ErrorType.FATAL_ERROR, "-- unexpected parser behavior in calling Transl");			
		unitCode.mod(i,operand);
	}
	public static CodeUnit extractLambda(int ind) throws Exc {
		int n = unitCode.numInstr() - ind+1;
		CodeUnit lambdaCode = new CodeUnit(n);
		for ( int i = ind; i<unitCode.numInstr(); i++)
			if ( Instruction.isJumpOperator(unitCode.code.get(i).getOperator() ) &&
					unitCode.code.get(i).getOperator() != Operator.CALL )
				lambdaCode.ins(unitCode.code.get(i).getOperator(), 
						unitCode.code.get(i).getOperand()-ind, unitCode.code.get(i).getComment());
			else
				lambdaCode.ins(unitCode.code.get(i).getOperator(), 
						unitCode.code.get(i).getOperand(), unitCode.code.get(i).getComment());
		unitCode.setNumInstr(ind);
		return lambdaCode;
	}
	public static void tailRecOpt(int funcN, int nPar) throws Exc {
		int nInstr= unitCode.numInstr();
		int iTailR =1; boolean doneTailOpt=false; int iTailRec=-1;
		boolean justCalledTail=false;
		for ( int i=0; i<nInstr; i++) {
			Instruction cInstr = unitCode.code.get(i);
			if ( cInstr.getOperator() == Operator.JUMP ) {
				if ( justCalledTail ) {
					cInstr.modOperator(Operator.NEXT);
					cInstr.modComment("* dummy command after tail recursion ");				
				}
			}
			justCalledTail=false;	
			if ( cInstr.getOperator() == Operator.CALL &&
					cInstr.getOperand()== funcN)
				if ( isTailRec(i+1) ) {
					cInstr.modOperator(Operator.JUMP);
					if ( iTailRec==-1 )
						iTailRec=unitCode.numInstr();
					cInstr.modOperand(iTailRec);
					cInstr.modComment("-> tail recursion "+iTailR);
					justCalledTail=true;
					if ( !doneTailOpt ) {
						unitCode.ins(Operator.NEXT, 0, "* tail recursion ");
						unitCode.ins(Operator.MOVEARG, nPar);
						unitCode.ins(Operator.JUMP, 1, "-> launch tail execution ");
						doneTailOpt=true;
					}
					iTailR++; 
				}
		}			
	}
	static boolean isTailRec( int i ) {
		Instruction cInstr = unitCode.code.get(i);
		if ( cInstr.getOperator()==Operator.RETURN )
			return true;
		if ( cInstr.getOperator()==Operator.NEXT )
			return isTailRec(i+1);
		if ( cInstr.getOperator()==Operator.JUMP)
			return isTailRec((int)cInstr.getOperand());
		return false;
	}
	public static CodeUnit end() {
		return unitCode;
	}

} // end class Transl

