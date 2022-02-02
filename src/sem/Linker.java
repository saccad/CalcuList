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
import exec.Exec;
import exec.Instruction;
import exec.Instruction.Operator;

import java.util.ArrayList;


public class Linker {

	private static final int entriesInitCapacity = 100;
	static ArrayList<FunctTable> entries = new ArrayList<FunctTable>(entriesInitCapacity);
	static int nUnsFTentries;
	static int nPr;
	static int lastInsPoint;
	public static int link ( CodeUnit uc ) throws Exc {
		entries.clear(); 
		nUnsFTentries = 0;
		nPr = 0; 
		boolean isInitCode = true;
		lastInsPoint = uc.numInstr();
		do {
			if ( isInitCode )
				copy(uc,0);
			else {
				int iF = entries.size()-nUnsFTentries; 
				copy(entries.get(iF).fCode,entries.get(iF).insPoint);
				nUnsFTentries--;		
			}
			isInitCode = false;
		} while (nUnsFTentries > 0);
		return nPr;
	}
    
	static void copy (CodeUnit uc, int insP) throws Exc {
		for ( int i = 0; i < uc.numInstr(); i++ ){
			if ( nPr >= Exec.getCS() )
				throw new Exc_Sem(ErrorType.LARGE_CODE, " Internal: increase the size of CODE in Exec > "
						+Exec.getCS() + " for Argument EXECCS");
			Instruction uc_i= uc.code.get(i);
			if (    uc_i.getOperator() == Operator.CALL || 
					uc_i.getOperator() == Operator.PUSHF ) 
//					(uc_i.getOperator() == Operator.PUSHF && uc.code.get(i+1).getOperator()!=Operator.FUNCF) ) 
				modCall(uc_i, uc_i.getOperator()); 
			else
				if ( uc_i.getOperator() == Operator.START ||
					 (uc_i.getOperator() == Operator.NEXT && 
						uc_i.getComment()!=null && !uc_i.getComment().isEmpty() &&
						uc_i.getComment().startsWith("*")) ) {
					Exec.modInstruction(nPr, new Instruction(
							uc_i.getOperator(), uc_i.getOperand(), uc_i.getComment()));
					nPr++; 
				}
				else
					if ( Instruction.isJumpOperator(uc_i.getOperator() ) &&
							uc_i.getOperator() != Operator.CALL ) {
						if ( uc_i.getOperator() == Operator.JUMP && 
								uc_i.getComment()!=null &&!uc_i.getComment().isEmpty() &&
										uc_i.getComment().startsWith("->")	)
							Exec.modInstruction(nPr, new Instruction(uc_i.getOperator(), 
									uc_i.getOperand()+insP,uc_i.getComment()));
						else
							Exec.modInstruction(nPr, new Instruction(uc_i.getOperator(), 
									uc_i.getOperand()+insP));							
							nPr++;
					}
					else {
						Exec.modInstruction(nPr, new Instruction(uc_i.getOperator(), 
								uc_i.getOperand()));
						nPr++;		
					}
		}
	}

	static void modCall ( Instruction instr, Operator op ) throws Exc {
		int iF = (int) instr.getOperand();
		int iEntry = -1;
		for ( int j = 0; j < entries.size() && iEntry < 0 ; j++)
			if ( entries.get(j).functNum == iF ) 
				iEntry = j; // function found in entries
		if ( iEntry >= 0 ) {
			Exec.modInstruction(nPr, new Instruction(op, 
					entries.get(iEntry).insPoint, instr.getComment()));
			nPr++;
		}
		else {
			CodeUnit fCode = SymbH.fCode(iF);
			if ( fCode == null )
				throw new Exc_Sem(ErrorType.UNDEF_FUNCT, "-- Unexpected Error");
			entries.add(new FunctTable(iF,lastInsPoint, fCode));
			Exec.modInstruction(nPr,new Instruction(op, lastInsPoint, instr.getComment()));
			nPr++;
			lastInsPoint += fCode.numInstr();
			nUnsFTentries++;
		}	
	}

} // end class Linker

class FunctTable {
	int functNum;
	int insPoint;
	CodeUnit fCode;
	FunctTable (int fN, int insP, CodeUnit fC) {
		functNum = fN;
		insPoint = insP;
		fCode = fC;
	}	
} // end class FunctTable

