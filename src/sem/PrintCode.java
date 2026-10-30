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

import java.util.ArrayList;

import error.Exc;
import exec.Exec;
import exec.Instruction.Operator;

public class PrintCode {

	public static void printListing (CodeUnit uc ) {
		int initCapacityLambdaInd=20;
		ArrayList<Integer> lambdaInd=new ArrayList<Integer>(initCapacityLambdaInd);
		System.out.print("\n------CODE UNIT------\n");
		for (int i = 0; i < uc.numInstr(); i++) {
			System.out.print(i+"\t"+uc.instr(i).decode()+"\n" );
			if (uc.instr(i).getOperator()== Operator.PUSHF
					&& uc.instr(i).getOperand() < 0 )
				lambdaInd.add((int)uc.instr(i).getOperand());
		}
		System.out.print("---End of CODE UNIT---\n");
		for ( int i=0; i<lambdaInd.size(); i++ ) {
			printLambda(lambdaInd.get(i));
		}	
	}
	
	public static void printLambda (int lambdaInd ) {
		CodeUnit uc = SymbH.fCode(lambdaInd);
		System.out.print("\n------LAMBDA UNIT------\n");
		for (int i = 0; i < uc.numInstr(); i++) {
			System.out.print(i+"\t"+uc.instr(i).decode()+"\n" );
		}
			System.out.print("---End of LAMBDA UNIT---\n");	
	}

	public static void printExecCode ( int nAbsoluteCode, int nInitCode ) throws Exc {
		System.out.print("\n------ABSOLUTE CODE------\n");
		if ( nAbsoluteCode == nInitCode )
			System.out.print(" ***It coincides with the Unit Code***\n");
		else
			for (int i = 0; i < nAbsoluteCode; i++) {
				System.out.print(i+"\t"+Exec.getInstruction(i).decode() +"\n");
			}
		System.out.print("---End of ABSOLUTE CODE---\n \n");				
	}

	
}
