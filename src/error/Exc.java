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
		EMPTY_FUNCSTACK("Empty FuncStack"),
		EMPTY_LIST("Empty List"),
		NULL_VALUE("Null Value"),
		NULL_FIELD("Null Field"),
		UNDEF_RETLABFUNC("Undefined Body for Labeled Function Returning a Function"),
		LIST_OUT_BOUND("List index Out of Range"),
		STRING_OUT_BOUND("String index Out of Range"),
		NEGATIVE_LIST_INDEX("Negative list index"),
		NEGATIVE_STRING_INDEX("Negative string index"),
		THROWE_NOT_SUPPORTED ("operation 'throw' not supported"),
		EQ_NEQ_UNSUPP ("operator '==' and '!=' not supported"),
		REL_OP_UNSUPP ("operators '<', '<=', '>', '>=' not supported"),
		ADD_UNSUPP ("operators '+' not supported"),
		ADDLS_UNSUPP ("operators '++' not supported"),
		SUB_UNSUPP ("operators '+' and '-' not supported"),
		MULT_UNSUPP ("operator '*' not supported"),
		DIV_UNSUPP ("operator '/' not supported"),
		REMAINDER_UNSUPP ("operator '%' not supported"),
		CAST_UNSUPP ("cast type not supported"),
		TOTYPE_UNSUPP ("cast to type not supported"),
		TODOUBLE_UNSUPP ("cast to double not supported"),
		TOLONG_UNSUPP ("cast to long not supported"),
		TOINT_UNSUPP ("cast to int not supported"),
		TOINTL_UNSUPP ("cast to int/long not supported"),
		TOCHAR_UNSUPP ("cast to char not supported"),
		TOSTRING_UNSUPP ("cast to string not supported"),
		TOJSON_UNSUPP ("cast to json not supported"),
		TOLIST_UNSUPP ("cast to list not supported"),
		JSON_DELETED ("the json has been deleted"),
		LIST_DELETED ("the list has been deleted"),
		NULLIFY_NOT_SUPPORTED ("'nullify' not supported"),
		STRING_LIST_JSON_EXPECTED ("function '_len' not supported"),
		ISKEY_NOT_SUPPORTED ("function '_isKey' not supported"),
		BOOL_EXPECTED ("expected a boolean value"),
		POS_NUM_EXPECTED("expected a positive numeric value"),
		NUM_EXPECTED("expected a numeric type"),
		CALL_NOT_SUPPORTED ("argument type for CALL not supported"),
		LWRAP_NOT_SUPPORTED ("argument type for LWARP not supported"),
		LISTEL_NOT_SUPPORTED ("wrong type for list element"),
		DUPLICATED_KEY ("duplicated json field key"),
		DEREF_NOT_SUPPORTED ("deref operator not supported"),
		DEREFX_NOT_SUPPORTED ("derefx operator not supported"),
		NEWFLD_NOT_SUPPORTED ("json field not supported"),
		WRONG_DATE ("wrong date format"),
		JSON_EXPECTED ("json type expected"),
		LIST_FIELD_EXPECTED ("list or field type expected"),
		FIELD_EXPECTED ("field type expected"),
		LONG_EXPECTED ("long type expected"),
		LIST_EXPECTED ("list type expected"),
		LONG_JSON_EXPECTED ("long or json type expected"),
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
		DISPLAY_NOT_SUPPORTED ("display not supported"),
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
		WRONG_BUILTIN("Wrong Built-In Function"),
		WRONG_DOUBLE("Wrong Double Number"),
		WRONG_MAX_VAL_LONG("Max absolute value for long: 9223372036854775807"),
		WRONG_LONG("Missing final 'l' or 'L' for a Long Number"),
		WRONG_INT_LONG("Wrong Int or Long Number"),
		WRONG_ESCAPE("Wrong Escape Sequence"),
		MISSING_QUOTE("Missing quote"),
		WRONG_OPTION("Wrong Option"),
		INVALID_SYM("Invalid Symbol"),
		WRONG_TOKEN("Wrong Token"),
		WRONG_ID("Wrong ID "),
		WRONG_TYPE("Type Mismatch"),
		WRONG_FTYPE("Function Type Mismatch"),
		WRONG_CALL("More than two nested function calls"),
		UNDEF_ID("Undefined Identifier"),
		NOT_EXPECTED_LAB("Expected an Identifier not a Label"),
		WRONG_UN_OP("Wrong Unary Operator"),
		EXPECTED_ID("Expected Identifier"),
		EXPECTED_SET_BRACE("Expected '{!'"),
		EXPECTED_ID_OR_DUMMY("Expected Identifier or '_='"),
		EXPECTED_ID_OR_HASH("Expected Identifier or '#'"),
		WRONG_ACT_PARAM("Actual Parameter Type Mismatch"),
		WRONG_ACT_PAR_NUM("Actual Parameter Number Mismatch"),
		MISSING_PRINT("Missing '^' at the beginning"),
		WRONG_SIDE_EFFECT("Side Effect Function cannot be called"),
		GLOB_VAR_NO_SE_FUNCT("Global Variable(s) in a Function Without Side Effects"),
		WRONG_FUN_SET("Setting of a function is not allowed"),
		WRONG_LAB_SET("Setting of a label is not allowed"),
		NO_SE_FUNC("Function with side effects required"),
		NO_PAR_SET("Parameter Setting is not allowed"),
		NAME_TOO_LONG("ID name longer than "),
		WRONG_HISTORY_INDEX("Wrong History Index"),
		WRONG_EXEC_INDEX("Wrong Exec Index"),
		LAB_NOT_ALLOWED("Labeled Variable not allowed"),
		// exceptions for Exc_Sem
		WRONG_FILE_IN("Wrong Data File Input"),
		EMPTY_STRING ("non-empty string expected"),
		UNDEF_FUNCT("Undefined Function"),
		SE_LAMBDA("Side Effects in Lambda Function Definition"),
		SE_DISPLAY("Display not allowed in the definion of a Function without side effects"),
		SE_READLINE("Line Read not allowed in the definion of a Function without side effects"),
		WRONG_LAMBDA("Wrong Lambda Function Definition"),
		UNDEF_LABEL("Undefined Label"),
		UNDEF_VAR("Undefined Global Variable"),
		INV_VAR("Variable Not Visible"),
		INV_FUNC("Function Not Visible"),
		INV_LABVAR("Invisible Labeled Variable"),
		UNDEF_LABFUNC("Undefined Labeled Function"),
		WRONG_LABEL_DEF("Wrong Label Definition"),
		WRONG_FUNCT_DEF("Wrong Function Definition"),
		WRONG_VAR_DEF("Wrong Variable Definition"),
		WRONG_DEF_TYPE("Wrong Definition Type"),
		WRONG_LIST_USE("Wrong List Usage"),
		VAR_NOT_LIST_JSON("Variable is not a list or a json"),
		VAR_NOT_DEF("New variable cannot be sliced"),
		WRONG_FORM_PAR_NUM("Wrong Number of Formal Parameters"),
		DUPL_ID("Duplicated Formal Parameter or Labeled Variable Name"),
		DUPL_LAMB_ID("Duplicated Lambda Formal Parameter Name"),
		DUPL_LABEL("Duplicated Label"),
		DUPL_LAB_VAR_FUNCT("Duplicated Labeled Variable or Function"),
		WRONG_LAB_VAR("Labeled Variable Previously Defined as Function"),
		WRONG_LAB_FUNCT("Labeled Function Previously Defined as Variable"),
		WRONG_FPARAM_LAB_FUNCT("Parametr of Labeled Function Previously Defined with different type"),
		WRONG_FRET_LAB_FUNCT("Labeled Function Previously Defined with a different returning function"),
		WRONG_ARITY_LAB_FUNCT("Labeled Function Previously Defined with Different Arity"),
		WRONG_SE_LAB_FUNCT("Labeled Function Previously Defined with Different Side Effect"),
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
