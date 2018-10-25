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
		INIT(OperandType.POP), HALT(OperandType.NOP), 
		START(OperandType.POP), 
		RETURN(OperandType.POP),
		CALL(OperandType.POP), CALLS(OperandType.NOP),
		THROWE(OperandType.NOP), BUILDS(OperandType.POP),
		DUPL(OperandType.NOP), POP(OperandType.POP), 
		PUSHD(OperandType.DOP), PUSHI(OperandType.IOP),
		PUSHL(OperandType.LOP),
		PUSHF(OperandType.IOP), PUSHV(OperandType.DOP),
		PUSHC(OperandType.POP), PUSHB(OperandType.POP),
		PUSHT(OperandType.POP), PUSHMT(OperandType.NOP),
		PUSHN(OperandType.NOP), PUSHARG(OperandType.POP),
	    NULLIFY(OperandType.POP),
	    GETF(OperandType.NOP), STOREF(OperandType.NOP),
		LOADGV(OperandType.POP), LOADLV(OperandType.POP),
		LOADARG(OperandType.POP), MOVEARG (OperandType.POP),
		DEREF(OperandType.NOP), MODV(OperandType.NOP), 
	    ADD(OperandType.NOP), SUB(OperandType.NOP), MULT(OperandType.NOP),
	    DIV(OperandType.NOP),NEG(OperandType.NOP), 
	    TODOUBLE(OperandType.NOP), TOLONG(OperandType.NOP), TOINTL(OperandType.NOP),
	    TOINT(OperandType.NOP), TOCHAR(OperandType.NOP),TOTYPE(OperandType.NOP), 
	    TOSTRING(OperandType.NOP),TOLIST (OperandType.NOP),TOJSON(OperandType.NOP),
	    TUPLE(OperandType.NOP),
	    RAND(OperandType.NOP), EXP(OperandType.NOP),LOG(OperandType.NOP),
	    GDATE(OperandType.POP), PDATE(OperandType.POP),
	    POW(OperandType.NOP), REMAIN(OperandType.NOP), ISKEY(OperandType.NOP),
	    EQ(OperandType.NOP), NEQ(OperandType.NOP), 
	    LT(OperandType.NOP), LTE(OperandType.NOP), GT(OperandType.NOP), 
	    GTE(OperandType.NOP),NOT(OperandType.NOP), 
	    JUMP(OperandType.POP), JUMPZ(OperandType.POP), 
	    JUMPNZ(OperandType.POP), NEXT(OperandType.NOP),
	    SLIST(OperandType.NOP), HLIST(OperandType.NOP), CLIST(OperandType.NOP), 
	    ELIST(OperandType.NOP),ALIST(OperandType.NOP), 
	    HEAD(OperandType.NOP), TAIL(OperandType.NOP), 
	    //SUBTAIL(OperandType.NOP),
	    LISTEL(OperandType.NOP), MODVEL(OperandType.NOP),
	    LCLONE(OperandType.POP),
	    SSTRING(OperandType.NOP), CSTRING(OperandType.NOP), ESTRING(OperandType.NOP), 
	    STRINGEL(OperandType.NOP), SUBSTR(OperandType.POP),
	    STRIND(OperandType.NOP), 
	    NEWFLD(OperandType.NOP),SJSON(OperandType.NOP), CJSON(OperandType.NOP), EJSON(OperandType.NOP), 
	    FLDVAL(OperandType.NOP), FLDFIND(OperandType.POP),
	    JCLONE(OperandType.NOP), 
	    LEN(OperandType.NOP), HDFLDV(OperandType.NOP),
	    LJEL (OperandType.NOP), LSJCLONE(OperandType.NOP), LSJELV(OperandType.NOP), 
	    LSELV(OperandType.NOP), LSCLONE(OperandType.POP), 
	    EXECF(OperandType.NOP), 
	    PRINT(OperandType.POP); 
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
		  return operator==	Operator.JUMP || operator==Operator.JUMPZ || operator==Operator.JUMPNZ 
				  || operator==Operator.CALL;
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
		  if ( comment != null ) {
			  if ( getOperandType(getOperator()) == OperandType.NOP)
				  str +="\t";
			  str +="\t/ "+ comment;
		  }
		  return str;
	  }

}
