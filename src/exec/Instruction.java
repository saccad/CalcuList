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

package exec;

/**
 * Definition of instruct operator codes for the CalcuList Machine 
 * @author 	Domenico Sacca'
 * @version 1.0
 * @date	May 2015
 */
public class Instruction {
	/*
	 * An operator may have no operand (NOP) or a positive integer operand (POP) or
	 * a positive/negative integer operand (IOP) or a double operand (DOP)
	 */
	public enum OperandType{
		NOP, // no operand
		IOP, // positive or negative integer operand
		POP, // non-negative integer operand
		LOP, // positive or negative long integer operand
		DOP; // double operand
	}
	public enum Operator {
		// Instructions for Starting and Ending Code Units
		INIT(OperandType.POP), // * start the main execution unit 
		HALT(OperandType.NOP), // * end the main execution unit 
		START(OperandType.IOP),  // start a function unit
		STARTX(OperandType.IOP), // start a lambda wrapper
		ALLOC(OperandType.POP),	 // * allocate function local variables
		RETURN(OperandType.POP), // * end a function unit and return the result
		THROWE(OperandType.NOP), // * raise and exception 
		// Function call and scheduling instructions
		CALL(OperandType.NOP), // * call a function
		LWRAP(OperandType.IOP), // generate a lambda wrapper
	    STOREF(OperandType.NOP), // * push a function onto FuncStack
	    GETF(OperandType.NOP), // * pop a function from FuncStack
	    // STACK manipulation instructions
		PUSHD(OperandType.DOP), // push a double onto STACK
		PUSHL(OperandType.LOP), // push a long onto STACK
		PUSHI(OperandType.IOP), // push an int onto STACK
		PUSHC(OperandType.POP), // * push a char onto STACK
		PUSHB(OperandType.POP), // * push a bool onto STACK
		PUSHN(OperandType.NOP), // * push null onto STACK 
		PUSHT(OperandType.POP), // * push a type onto STACK
		PUSHF(OperandType.IOP), // * push a function CODE address onto STACK
		PUSHLAB(OperandType.POP), // * push a label address onto STACK
		DUPL(OperandType.NOP), // * duplicate the top element of STACK 
		POP(OperandType.NOP), // * pop the the top element of STACK 
		// Loading and dereferencing instructions
		LOADGV(OperandType.POP), // * push address of a global variable into STACK
		LOADLV(OperandType.POP), // * push address of a local variable into STACK
		LOADPAR(OperandType.POP), // * push address of a function parameter into STACK
		DEREF(OperandType.NOP), // * replace a reference with the referenced element in the stack
		MODV(OperandType.NOP), // * modify the value of a referenced element in the stack 	
		NULLIFY(OperandType.POP), // * replace the top STACK element with a null value and, in the case of lists or JSON objects, delete all contained elements
		// External parameter handling Instructions 
		STOREXPAR(OperandType.POP), // * construct an external parameter array for a lambda function
		SETXPAR(OperandType.POP), // * update the register pointing to the current external parameter array
		GETXPAR(OperandType.NOP), // * store the reference to the current external parameter array into the activation frame of a lambda function
		LOADXPAR(OperandType.POP), // * push the address of an external parameter into the STACK
		DEREFX(OperandType.NOP), // * replace a reference to an external parameter with the referenced value
		// Branching Instructions
	    JUMP(OperandType.POP), // * perform an unconditional jump to another instruction
	    JUMPZ(OperandType.POP), // * jump to another instruction if the stack operand is false
	    JUMPNZ(OperandType.POP), // * jump to another instruction if the stack operand is true
	    NEXT(OperandType.NOP), // * perform no operation and continue with the next instruction
		// Comparison and Logical Instructions
	    EQ(OperandType.NOP), // * check whether two values are equal
	    NEQ(OperandType.NOP), // * check whether two values are not equal
	    LT(OperandType.NOP), // * check whether the first operand value is less than the second one
	    LTE(OperandType.NOP), // * check whether the first operand value is less than or equal to the second one
	    GT(OperandType.NOP), // * check whether the first operand value is greater than the second one
	    GTE(OperandType.NOP), // * check whether the first operand value is greater than or equal to the second one
	    NOT(OperandType.NOP), // * implement the Boolean negation operator
	    CHECKB(OperandType.NOP), // * check whether the stack operand is Boolean
		// Arithmetic Instructions
	    ADD(OperandType.NOP), // * add the two typed values at the top of the stack
	    SUB(OperandType.NOP), // * subtract the two typed values at the top of the stack
	    MULT(OperandType.NOP), // * multiply the two typed values at the top of the stack
	    DIV(OperandType.NOP), // * divide the two typed values at the top of the stack
	    REMAINDER(OperandType.NOP), // * compute the remainder of two numbers
	    NEG(OperandType.NOP), // * change the sign of the typed value at the top of the stack
	    CHECKN(OperandType.NOP), // * check whether the top value in the stack is numeric
	    // Instructions for list manipulation
	    SLIST(OperandType.NOP), // * start the construction of a list
	    HLIST(OperandType.NOP), // * insert the first element into a list under construction
	    CLIST(OperandType.NOP), // * append a new element to a list under construction 
	    ELIST(OperandType.NOP), // * complete the construction of a list
	    ALIST(OperandType.NOP), // * append an existing list to a list under construction
	    HEAD(OperandType.NOP), // * return the head element value of a list
	    TAIL(OperandType.NOP), // * return the tail of a list
	    LISTEL(OperandType.NOP), // * return the list element value with a given index
	    LCLONE(OperandType.POP), // * clone a portion of a list
	    // Instructions for string manipulation
	    SSTRING(OperandType.NOP), // * start the construction of a string
	    CSTRING(OperandType.NOP), // * append a new character to a string under construction
	    ESTRING(OperandType.NOP), // * complete the construction of a string
	    STRINGEL(OperandType.NOP), // * return the character of a string with a given index
	    SUBSTR(OperandType.POP), // * construct a substring from a string
	    // Instructions for JSON object manipulation
	    SJSON(OperandType.NOP), // * start the construction of a JSON object by inserting an initial dummy field
	    CJSON(OperandType.NOP), // * append a new field to a JSON object under construction
	    EJSON(OperandType.NOP), // * complete the construction of a JSON object
	    NEWFLD(OperandType.NOP), // * construct a new JSON key-value triplet
	    FLDVAL(OperandType.NOP), // * replace the field on top of the STACK with its value
	    FLDFIND(OperandType.POP), // * search a JSON field by key
	    JCLONE(OperandType.NOP), // * clone the JSON object on top of the STACK
	    // Generic Manipulation Instructions for Strings, Lists, and JSON Objects
	    HDFLDV(OperandType.NOP), // * return the head value of a list or the value of a field	    
	    LJEL (OperandType.NOP), // * return an element of a list with a given index or the field of a JSON object with a given key.
	    LSJCLONE(OperandType.NOP), // * return a copy of a list, JSON object, or string
	    LSCLONE(OperandType.POP), // * construct a sublist or a substring whose elements are within given indices
	    LSELV(OperandType.NOP), // * get the character of a string or an element of a list with a given index
	    LSJELV(OperandType.NOP), // * get the character of a string, an element of a list, or the value of a JSON field
	    MODVEL(OperandType.NOP), // * modify the head element of a list or the value of a field
	    ADDLS(OperandType.NOP), // * concatenate two lists or two strings
	    // casting instructions
		CAST(OperandType.POP), // * cast the typed value on top of the STACK to another type
		TOINT(OperandType.NOP), // * convert a double value into an integer
		// Instructions for built-in functions
		RAND(OperandType.NOP), // * push a randomly generated double value in the range from 0 to 1
		EXP(OperandType.NOP), // * compute the natural exponential function of a number
		LOG(OperandType.NOP), // * compute the natural logarithm of a number
		POW(OperandType.NOP), // * compute the power of a number raised to another number
		LEN(OperandType.NOP), // * compute the length of a list, string, or JSON object
	    STRIND(OperandType.NOP), // * return the first occurrence index of a string inside another string, starting from a given index
		ISKEY(OperandType.NOP), // * check whether a JSON object contains a field with a given key
		GDATE(OperandType.POP), // * generate the current date or convert a date string into Java's internal date representation
		PDATE(OperandType.POP), // * convert a long integer representing a date into a formatted string
		TUPLE(OperandType.NOP),	// * convert a JSON object or a date into a list   	    
	    // Input/Output and External Execution Instructions
	    PRINT(OperandType.POP), // * append a value to OUTPUT together with its type and printing option
	    DISPLAY(OperandType.NOP), // * interactively print a value on the console using the standard printing option
	    READL(OperandType.NOP), // * interactively read a line of text from the console
	    EXECF(OperandType.NOP); // * execute a portion of CLVM code stored in an external code file
		
		
		final OperandType operandType; 
		Operator(OperandType operandType) {
			this.operandType=operandType; 
		}
	}
	  private Operator operator;
	  private double operand;
	  private String comment;

	  public Instruction(Operator operator, double operand ) {
	    this(operator,operand,null);
	  }

	  public Instruction( Operator operator, double operand, String comment  ) {
			    //this.label = label;
			    this.operator = operator;
			    this.operand = operand;
			    this.comment= comment;
	  }

	  public Operator getOperator() { return operator; }
	  public void modOperator(Operator o ) {  operator=o; }
	  public double getOperand() { return operand; }
	  public void modOperand(double o ) {  operand=o; }
	  public String getComment() { return comment; }
	  public void modComment(String c ) {  comment=c; }
	  public static OperandType getOperandType ( Operator operator ) {
		  return operator.operandType;
	  }
	  
	  public static boolean isJumpOperator(Operator operator) {
		  return operator==	Operator.JUMP || operator==Operator.JUMPZ || operator==Operator.JUMPNZ;
//				  || operator==Operator.PUSHF;
	  }
	  
	  public static String getOperatorString ( Operator operator ) {
		  return operator.name();
	  }
	  
	  public static Operator getOperatorCode ( String op ) {
		  if ( op != null )
			  for ( Operator b : Operator.values() )
				  if ( op.equalsIgnoreCase(b.name()) )
					  return b;
		  return null;
	  }
	  
	  public  String decode ( ) {
		  String str = getOperatorString(getOperator());
		  if ( getOperandType(getOperator()) == OperandType.POP || getOperandType(getOperator()) == OperandType.IOP)
			  str += "\t"+Integer.toString((int) getOperand());
		  else
			  if ( getOperandType(getOperator()) == OperandType.LOP )
				  str+="\t"+Long.toString(Double.doubleToRawLongBits(getOperand()) );
			  else
				  if ( getOperandType(getOperator()) == OperandType.DOP )
					  str += "\t"+Double.toString(getOperand());
		  if ( getComment() != null ) {
			  if ( getOperandType(getOperator()) == OperandType.NOP)
				  str +="\t";
			  str +="\t/ "+ getComment();
		  }
		  return str;
	  }

}
