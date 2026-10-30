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
import exec.Instruction;
import exec.Instruction.Operator;


public class Transl {

	static CodeUnit unitCode;
	static CodeUnit nullUnitCode;
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
			if ( Instruction.isJumpOperator(unitCode.code.get(i).getOperator() ) )
				lambdaCode.ins(unitCode.code.get(i).getOperator(), 
						unitCode.code.get(i).getOperand()-ind, unitCode.code.get(i).getComment());
			else
				lambdaCode.ins(unitCode.code.get(i).getOperator(), 
						unitCode.code.get(i).getOperand(), unitCode.code.get(i).getComment());
		unitCode.setNumInstr(ind);
		return lambdaCode;
	}

	
	public static CodeUnit end() {
		return unitCode;
	}
	
	public static CodeUnit nullBodyFunct (String fName, int fCode, int arity, boolean hasFuncRet) throws Exc {
		nullUnitCode=new CodeUnit();
		nullUnitCode.ins(Operator.START,fCode,"* "+fName);
		if (hasFuncRet)
			nullUnitCode.ins(Operator.PUSHN,0,"* <<incomplete>>"); // error labeled function incomplete
		else
			nullUnitCode.ins(Operator.PUSHN,0);
		nullUnitCode.ins(Operator.RETURN,arity);
		return nullUnitCode;
	}

} // end class Transl

