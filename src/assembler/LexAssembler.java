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

package assembler;

import java.util.ArrayList;

import exec.Instruction;
import exec.Instruction.OperandType;
import error.Exc;
import error.Exc_Assembler;
import error.Exc.ErrorType;

public class LexAssembler {
	static int state, iLine, nLine;
	//static int label;
	static Instruction.OperandType operandType; 
	static Instruction.Operator operatorCode;
	static String operator, slabel, soperand;
	static String line;
	static double operand; 
	static char prefixBeginEnd ='$';
	static int BEGIN = 1;
	static int END = 2;
	static int beginEnd;
	
	static AsmLine parseInstruction ( String asmLine, int instrInd, 
			ArrayList<LabelInstr> labels, ArrayList<LabelInstr> calledLabels) throws Exc {	
			iLine=0; 
			line=asmLine;
			nLine= line.length();
			if ( nLine == 0 )
				return null;
			state= 0;
			boolean terminate = false;
			while ( iLine < nLine && !terminate ) 
				switch (state) {
					case 0:	
						state0 (); break; 
					case 1:	
						state1 (labels, instrInd); break; 
					case 2:	
						state2 (); break; 
					case 3:	
						state3 (); break; 
					case 4:	
						state4 (); break;
					case 5:	
						state5 (calledLabels, instrInd); break;
					case 6: 
						state6 (calledLabels, instrInd); break;
					case 7:
						state7(); break;
					case 8:
						state8(); break;
					case 9: case 10: case 11:
						terminate = true; break;
					default:
						throw new Exc_Assembler(ErrorType.EXEC_FAILURE, "Unexpected Error in LexAss");
				}
			switch ( state ) {
				case 9:
					return new AsmBlank();
				case 10: 
					return new AsmInstr (operatorCode,operand);
				case 11:
					return new AsmBeginEnd(beginEnd);
				default:
					System.out.println("\nstate="+state);
					throw new Exc_Assembler(ErrorType.WRONG_INSTRUCTION, "incomplete instruction");					
			}
		}
	
	static void state0 (  ) throws Exc { // skip intial blanks
		if ( Character.isWhitespace(line.charAt(iLine)) ) {			
			iLine++;
			if ( iLine==nLine)
				state = 9;
		}
		else
			if ( line.charAt(iLine) =='/' ) 
				state = 9; 
			else
				if ( Character.isDigit(line.charAt(iLine)) ) { // there is label
					state = 1; slabel = Character.toString(line.charAt(iLine)); iLine++; 
				}
				else // no label
					if ( Character.isLetter(line.charAt(iLine)) ) {
						state = 3; operator = Character.toString(line.charAt(iLine)); iLine++; 
					}
					else
						if ( line.charAt(iLine) == prefixBeginEnd ) {
							state = 4; operator = ""; iLine++;
						}
						else
							throw new Exc_Assembler(ErrorType.WRONG_INSTRUCTION, "wrong operator");
	}

	/**
	 * Parse the numeric label
	 */
	static void state1 (  ArrayList<LabelInstr> labels, int instrInd ) throws Exc {
		if ( Character.isDigit(line.charAt(iLine)) ) {
			slabel += Character.toString(line.charAt(iLine)); iLine++; 
		}
		else
			if ( Character.isWhitespace(line.charAt(iLine)) ) {
				checkLabel(slabel,labels,instrInd);
				state = 2; 
			}
			else
				throw new Exc_Assembler(ErrorType.WRONG_INSTRUCTION, "wrong label");
	}
	
	static void state2 ( ) throws Exc { // start the recognition of the operator
		if ( Character.isWhitespace(line.charAt(iLine)) )
			iLine++;
		else
			if ( Character.isLetter(line.charAt(iLine)) ) {
				state = 3; operator = Character.toString(line.charAt(iLine)); iLine++; 
			}
			else
				throw new Exc_Assembler(ErrorType.WRONG_INSTRUCTION, "wrong operator");
	}

	static void state3 ( ) throws Exc { // recognition of the operator
		if ( Character.isLetterOrDigit(line.charAt(iLine)) ) {
			operator += Character.toString(line.charAt(iLine)); 
			iLine++; 
			if ( iLine == nLine || line.charAt(iLine)=='/' ) {
				checkOp();								
				if ( operandType != OperandType.NOP )
					throw new Exc_Assembler(ErrorType.WRONG_INSTRUCTION, "missing operand");								
				state = 10;	
			}
		}
		else
			if ( Character.isWhitespace(line.charAt(iLine)) ) {
				checkOp();								
				iLine++; 
				if (  operandType==OperandType.NOP ) 
					state = iLine<nLine? 7: 10;
				else
					if ( iLine<nLine )
						state = 5;
					else 
						throw new Exc_Assembler(ErrorType.WRONG_INSTRUCTION, "missing operand");
			}
			else
				throw new Exc_Assembler(ErrorType.WRONG_INSTRUCTION, "wrong operator");
	}
		
	static void state4 ( ) throws Exc { // recognition of begin / end
		if ( Character.isLetter(line.charAt(iLine)) ) {
			operator += Character.toString(line.charAt(iLine)); iLine++; 
			if ( iLine == nLine || line.charAt(iLine) =='/') {
				beginEnd = checkOpBeginEnd();
				state = 11;
			}
		}
		else
			if ( Character.isWhitespace(line.charAt(iLine)) ) {
				beginEnd = checkOpBeginEnd();	
				state = 8;
			}
	}


	/**
	 * start the recognition of the operand
	 */
	static void state5 ( ArrayList<LabelInstr> calledLabels, int instrInd ) throws Exc {
		if ( Character.isWhitespace(line.charAt(iLine)) )
			iLine++;
		else {
			if ( line.charAt(iLine) =='/' ) 
				throw new Exc_Assembler(ErrorType.WRONG_INSTRUCTION, "missing operand");
			checkOperandSymb();
			soperand = Character.toString(line.charAt(iLine)); 
			iLine++; 
			if ( iLine == nLine ) {
				checkOperand(calledLabels, instrInd);
				state=10;
			}
			else 
				state = 6;
		}
	}

	static void state6 ( ArrayList<LabelInstr> calledLabels, int instrInd  ) throws Exc {
		if ( Character.isWhitespace(line.charAt(iLine)) || line.charAt(iLine) =='/' ) {
			checkOperand(calledLabels, instrInd);
			if ( Character.isWhitespace(line.charAt(iLine)) ) {
				state = 7; iLine++;
				if ( iLine == nLine ) 
					state = 10; 
			}
			else  // case line.charAt(iLine) =='/'
				state = 10;
		}
		else {
			checkOperandSymb();
			soperand += Character.toString(line.charAt(iLine)); 
			iLine++;
			if ( iLine >= nLine ) {
				checkOperand(calledLabels, instrInd);
				state = 10;
			}
		}
	}

	static void state7 ( ) throws Exc { // check for spurious characters at the end of the line
		if ( Character.isWhitespace(line.charAt(iLine)) ) {
			iLine++;
			if ( iLine == nLine ) 
				state = 10; 
		}
		else
			if ( line.charAt(iLine) =='/' ) 
				state = 10; 
			else
				throw new Exc_Assembler(ErrorType.WRONG_INSTRUCTION, "spurious characters at the end");
	}

	static void state8 ( ) throws Exc { // check for spurious characters at the end of the line
		if ( Character.isWhitespace(line.charAt(iLine)) ) {
			iLine++;
			if ( iLine == nLine ) 
				state = 11; 
		}
		else
			if ( line.charAt(iLine) =='/' ) 
				state = 11; 
			else
				throw new Exc_Assembler(ErrorType.WRONG_INSTRUCTION, "spurious characters at the end");
	}

	static void checkLabel(String slabel, ArrayList<LabelInstr> labels, int instrInd) throws Exc {
		int label = Integer.parseInt(slabel);
		int k = labels.indexOf(new LabelInstr(label,0)); 
		if ( k >= 0 )
			throw new Exc_Assembler(ErrorType.WRONG_INSTRUCTION, "duplicated label");
		else
			labels.add(new LabelInstr(label, instrInd));	
	}

	static void checkOperandSymb( ) throws Exc{
		if ( line.charAt(iLine) != '+' && line.charAt(iLine) != '-'
			&& line.charAt(iLine) != '.' && line.charAt(iLine) != 'e'
				&& line.charAt(iLine) != 'E' && !Character.isDigit(line.charAt(iLine)))
			throw new Exc_Assembler(ErrorType.WRONG_INSTRUCTION, "wrong operand");
	}


	static void checkOperand ( ArrayList<LabelInstr> calledLabels, int instrInd  ) throws Exc {
		if ( operandType == OperandType.IOP || operandType == OperandType.POP ) {
			try {
				operand = Integer.parseInt(soperand);
			} 	catch (Exception e) {
					throw new Exc_Assembler(ErrorType.WRONG_INSTRUCTION, "expected integer operand");
				}
			if ( operandType == OperandType.POP && (operand < 0 || soperand.charAt(0) == '+') ) 
				throw new Exc_Assembler(ErrorType.WRONG_INSTRUCTION, "expected unsigned integer operand"); 
			if ( Instruction.isJumpOperator(operatorCode) )
				calledLabels.add(new LabelInstr((int)operand,instrInd));
		}
		else 
			if ( operandType == OperandType.LOP ) {
				try {
					operand = Double.longBitsToDouble(Long.parseLong(soperand));
				} 	catch (Exception e) {
						throw new Exc_Assembler(ErrorType.WRONG_INSTRUCTION, "expected long operand");
					}				
			}
			else
				try {
					operand = Double.parseDouble(soperand);
				} 	catch (Exception e) {
						throw new Exc_Assembler(ErrorType.WRONG_INSTRUCTION, "expected double operand");
					}
	}
	
	static int checkOpBeginEnd( ) throws Exc{
		if ( operator.equalsIgnoreCase("begin") ) 
			return BEGIN;
		else 
			if ( operator.equalsIgnoreCase("end") ) 
				return END;	
			else
				throw new Exc_Assembler(ErrorType.WRONG_INSTRUCTION, "expected '$begin' or '$end'");				
	}

	static void checkOp() throws Exc {
		operatorCode = Instruction.getOperatorCode(operator);
		if ( operatorCode == null )
			throw new Exc_Assembler(ErrorType.WRONG_INSTRUCTION, "wrong operator");								
		operandType = Instruction.getOperandType(operatorCode);
	
	}

} // end class LexAss

class LabelInstr {
	int label;
	int instrNo;
	LabelInstr (int l, int i ) {
		label = l; instrNo = i;
	}
	@Override public boolean equals ( Object ob ) {
		 return this == ob || this.label== ((LabelInstr) ob).label;
	}
} // end class LabelInstr

class AsmLine {
	
} // end class AsmLine

class AsmInstr extends AsmLine {
	Instruction instr; 
	AsmInstr ( Instruction.Operator operator, double operand ) {
		instr = new Instruction(operator,operand);
	}
} // end class AsmInstr

class AsmBlank extends AsmLine {
} // end class AsmBlank

class AsmBeginEnd extends AsmLine {
	int type;
	AsmBeginEnd ( int beginEnd ) {
		type=beginEnd;
	}
} // end class AsmBeginEnd
