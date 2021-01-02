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

package error;

public class Exc extends Exception {

	/**
	 * default serial number
	 */
	private static final long serialVersionUID = 1L;
	public enum ErrorType {
		// exceptions for Exc_Exec
		WRONG_IP("Wrong IP"),
		STACK_HEAP_OVERFLOW("Stack Heap Overflow in the instruction: "),
		ZERO_DIVIDE("Zero Divide"),
		OUTPUT_OVERFLOW("Output Overflow"),
		WRONG_MACHINE_OPERATOR("Wrong Machine Operator"),
		WRONG_ADDRESS("Wrong Memory Address"),
		EXEC_FAILURE("Exec Failure"),
		WRONG_PRINT_FORMAT("Wrong Printing Format"),
		EMPTY_LIST("Empty List"),
		NULL_FIELD("Null Field"),
		LIST_OUT_BOUND("List index Out of Range"),
		STRING_OUT_BOUND("String index Out of Range"),
		NEGATIVE_LIST_INDEX("Negative list index"),
		NEGATIVE_STRING_INDEX("Negative string index"),
		THROWE_NOT_SUPPORTED ("operation 'throw' not supported"),
		ADD_NOT_SUPPORTED ("operator '+' not supported"),
		SUB_NOT_SUPPORTED ("operator '-' not supported"),
		MULT_NOT_SUPPORTED ("operator '*' not supported"),
		DIV_NOT_SUPPORTED ("operator '/' not supported"),
		TOTUPLE_NOT_SUPPORTED ("cast to tuple not supported"),
		TOTYPE_NOT_SUPPORTED ("cast to type not supported"),
		TOLONG_NOT_SUPPORTED ("cast to long not supported"),
		TOINT_NOT_SUPPORTED ("cast to int not supported"),
		TOINTL_NOT_SUPPORTED ("cast to int/long not supported"),
		TOCHAR_NOT_SUPPORTED ("cast to char not supported"),
		TOSTRING_NOT_SUPPORTED ("cast to string not supported"),
		TOJSON_NOT_SUPPORTED ("cast to json not supported"),
		TOLIST_NOT_SUPPORTED ("cast to list not supported"),
		//JSON_DELETED ("the json has been deleted"),
		LIST_DELETED ("the list has been deleted"),
		NULLIFY_NOT_SUPPORTED ("'nullify' not supported"),
		LEN_NOT_SUPPORTED ("function 'len' not supported"),
		EXP_NOT_SUPPORTED ("function 'exp' not supported"),
		LOG_NOT_SUPPORTED ("function 'log' not supported"),
		POW_NOT_SUPPORTED ("function 'pow' not supported"),
		ISKEY_NOT_SUPPORTED ("function 'isKey' not supported"),
		REMINDER_NOT_SUPPORTED ("operator '%' not supported"),
		EQ_NOT_SUPPORTED ("operator '==' not supported"),
		NEQ_NOT_SUPPORTED ("operator '!=' not supported"),
		LT_NOT_SUPPORTED ("operator '<' not supported"),
		LTE_NOT_SUPPORTED ("operator '<=' not supported"),
		GT_NOT_SUPPORTED ("operator '>' not supported"),
		GTE_NOT_SUPPORTED ("operator '>=' not supported"),
		JUMPZ_NOT_SUPPORTED ("conditional jump not supported"),
		PLUS_MINUS_NOT_SUPPORTED ("operators '+' and '-' not supported"),
		NOT_NOT_SUPPORTED ("operator '!' not supported"),
		NEG_NOT_SUPPORTED ("change sign not supported"),
		CALLS_NOT_SUPPORTED ("argument type for CALLS not supported"),
		BUILDS_NOT_SUPPORTED ("argument type for BUILDS not supported"),
		LISTEL_NOT_SUPPORTED ("wrong type for list element"),
		DUPLICATED_KEY ("duplicated json field key"),
		DEREF_NOT_SUPPORTED ("deref operator not supported"),
		NEWFLD_NOT_SUPPORTED ("json field not supported"),
		WRONG_DATE ("wrong date format"),
		JSON_EXPECTED ("json type expected"),
		LIST_FIELD_EXPECTED ("list or field type expected"),
		FIELD_EXPECTED ("field type expected"),
		LONG_EXPECTED ("long type expected"),
		LIST_EXPECTED ("list type expected"),
		LIST_STRING_EXPECTED ("list or string type expected"),
		LIST_STRING_JSON_EXPECTED ("list or string or json type expected"),
		LIST_JSON_EXPECTED ("list or json type expected"),
		STRING_EXPECTED ("string type expected"),
		CHAR_EXPECTED ("char type expected"),
		INT_EXPECTED ("int or char type expected"),
		PUSHT_NOT_SUPPORTED ("PUSHT not supported"),
		HLIST_NOT_SUPPORTED ("list head not supported"),
		CLIST_NOT_SUPPORTED ("list element not supported"),
		CJSON_NOT_SUPPORTED ("list element not supported"),
		MODVEL_NOT_SUPPORTED ("list head update not supported"),
		MODV_NOT_SUPPORTED ("MODV not supported"),
		REF_EXPECTED ("ref type expected"),
		MISSING_EXEC_FILE ("missing exec file"),
		WRONG_EXEC_FILE ("wrong exec file"),
		PRINT_NOT_SUPPORTED ("print not supported"),
		WRONG_PRINT_FILE ("Print File must be the first entry in OUTPUT"),
		// exceptions for launching the program
		WRONG_ARGS_NUM ("Odd Number of Arguments Passed to the Executable Java Program"),
		WRONG_ARG_NAME ("Wrong Name for an Argument Passed to the Executable Java Program"),
		WRONG_ARG_INT ("Integer Format Required for an Argument Passed to the Executable Java Program"),
		WRONG_ARG_VAL ("Non-Positive Value for an Argument Passed to the Executable Java Program"),
		// exceptions for Exc_Ass
		LARGE_CODE("Too Large Code"),
		MISSING_END("Missing End Instruction"),
		MISSING_BEGIN("Missing Begin Instruction"),
		EMPTY_FILE("Empty File"),
		WRONG_FILE("Wrong File"),
		UNSOLVED_LABEL("Unsolved Label"),
		TOO_MANY_LABELS("Too Many Labels"),
		WRONG_INSTRUCTION("Wrong Assembler Instruction"),
		// exceptions for Exc_Synt
		FATAL_ERROR("Fatal Error"),
		WRONG_DOUBLE("Wrong Double Number"),
		WRONG_MAX_VAL_LONG("Max absolute value for long: 9223372036854775807"),
		WRONG_INT_LONG("Wrong Int or Long Number"),
		WRONG_ESCAPE("Wrong Escape Sequence"),
		MISSING_QUOTE("Missing quote"),
		WRONG_OPTION("Wrong Option"),
		INVALID_SYM("Invalid Symbol"),
		WRONG_TOKEN("Wrong Token"),
		WRONG_ID("Wrong ID "),
		WRONG_EXP_TYPE("Type Mismatch"),
		UNDEF_ID("Undefined Identifier"),
		EXPECTED_ID("Exepected Identifier"),
		EXPECTED_ID_OR_DUMMY("Exepected Identifier or '#='"),
		EXPECTED_ID_OR_HASH("Exepected Identifier or '#'"),
		WRONG_ACT_PARAM("Actual Parameter Type Mismatch"),
		WRONG_ACT_PAR_NUM("Actual Parameter Number Mismatch"),
		MISSING_PRINT("Missing '^' at the beginning"),
		SIDE_EFFECT("Side Effect Function cannot be called here"),
		GLOB_VAR_IN_FUNCT("Global Variable(s) in a Function"),
		WRONG_FUN_SET("Setting of a function is not allowed"),
		NO_PAR_SET("Parameter Setting is not allowed"),
		NAME_TOO_LONG("ID name longer than "),
//		TAIL_SET("No Tail Setting is allowed in a function without side effect"),
		WRONG_HISTRORY_INDEX("Wrong History Index"),
		WRONG_EXEC_INDEX("Wrong Exec Index"),
		LAB_NOT_ALLOWED("Labeled Variable not allowed"),
		// exceptions for Exc_Sem
		WRONG_FILE_IN("Wrong Data File Input"),
		UNDEF_FUNCT("Undefined Function"),
		WRONG_LAMBDA("Wrong Lambda Function Definition"),
		UNDEF_LABEL("Undefined Label"),
		UNDEF_VAR("Undefined Global Variable"),
		INV_VAR("Variable Not Visible"),
		UNDEF_INV_LABEL("Undefined or Invisible Label"),
		UNDEF_LABVAR("Undefined Labeled Variable"),
		WRONG_FUNCT_DEF("Wrong Function Definition"),
		WRONG_VAR_DEF("Wrong Variable Definition"),
		WRONG_DEF_TYPE("Wrong Definition Type"),
		WRONG_LIST_USE("Wrong List Usage"),
		WRONG_LIST_JSON_USE("Wrong List or Json Usage"),
		WRONG_FORM_PAR_NUM("Wrong Number of Formal Parameters"),
		DUPL_ID("Duplicated Formal Parameter or Labeled Variable Name"),
		DUPL_LABEL("Duplicated Label"),
		DUPL_LAB_VAR_FUNCT("Duplicated Labeled Variable or Function"),
		WRONG_LAB_VAR("Labeled Variable Previously Defined as Function"),
		WRONG_LAB_FUNCT("Labeled Function Previously Defined as Variable"),
		WRONG_PAR_TYPE("Wrong Parameter Type"),
		WRONG_RET_TYPE("Wrong Return Type");
		final String text;
		ErrorType(String s) {
			this.text=s;
		}	
	}
	protected ErrorType errorType;
	protected String errMessage;
	public Exc ( ErrorType errorType ) {
		this.errorType = errorType; this.errMessage = "";
	}
	public Exc ( ErrorType errorType, String errMessage ) {
		this.errorType = errorType; this.errMessage = errMessage;
	}

	public ErrorType errorType() { return errorType;}

	public String errorMessage() { return errMessage;}
	
	public void addMessage ( String s ) {
		errMessage += s;
	}

	public String print() { return "** ERROR "+errorType.name()
			+" ** " + errorType.text+ ": "+ errMessage;}


}
