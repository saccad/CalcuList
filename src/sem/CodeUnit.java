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

import exec.Instruction;
import exec.Instruction.Operator;
import error.Exc;
import java.util.ArrayList;

public class CodeUnit  {

//	public static int numMaxInstr = 3200;
	static final int codeInitCapacity = 3200;
	ArrayList<Instruction> code;
//	int numInstr;
	
//	public static void modSizeUC( int size ) {
//		numMaxInstr=Math.max(numMaxInstr,size);
//	}
//
	CodeUnit () {
		code = new ArrayList<Instruction> (codeInitCapacity);
//		numInstr=0;
	}
	CodeUnit (int n) {
		code = new ArrayList<Instruction> (n);
//		numInstr=0;
	}
	
	int ins (Operator operator, double operand) throws Exc {
		code.add(new Instruction(operator,operand));
//		numInstr++;
//		if ( numInstr >= numMaxInstr )
//			throw new Exc_Sem(ErrorType.LARGE_CODE, " Internal: increase the size of code in UnitCode > "
//					+numMaxInstr + " for Argument UNITCODESIZE");
		return numInstr()-1;
	}
	int ins (Operator operator, double operand, String comment) throws Exc {
		code.add(new Instruction(operator,operand, comment));
//		numInstr++;
//		if ( numInstr >= numMaxInstr )
//			throw new Exc_Sem(ErrorType.LARGE_CODE, " Internal: increase the size of code in UnitCode > "
//					+numMaxInstr + " for Argument UNITCODESIZE");
		return numInstr()-1;
	}
	public int numInstr() {
		return code.size();
	}
	void setNumInstr(int n) {
		code.subList(n,numInstr()).clear();
	}
	
	void mod (int i, double operand ) {
		code.get(i).modOperand(operand);
	}
	Instruction instr(int i) {
		return code.get(i);
	}
}

