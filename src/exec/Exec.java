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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;
import java.util.Random;

import error.Exc;
import error.Exc_Assembler;
import error.Exc_Exec;
import error.Exc.ErrorType;
import exec.Instruction.Operator;

import java.util.Date;


/**
 * Interpreter for CalcuList Virtual Machine
 * @author	Domenico Sacca'
 * @version	4.2.0
 * @date	 July 2018
 */
public class Exec {
	
	static  Scanner reader = new Scanner(System.in);

	static private Instruction IR;
	static final int nullValue = 0; // it must be equal to the value of null
	static final int deletedNullValue = -1; // used to mark a deleted list element (<-1, NullT>) or json element (<-1,FielddT>)
	static int 	nClops; // number of instructions executed by the current exec
	static  int CS = 32000; // size of Code Array
	static  int MS = 64000; // size of Memory Array
	static  int OS = 16000; // size of Output Array
    static  double [] MEM; // Memory Array
    static  double [] OUTPUT; // Output Array
    static Instruction [] CODE; // Code Array
    static boolean [] BREAKPOINTS;
    static int cexp = 200; // cost of exp in clops
    static int clog = 200; // cost of log in clops
    static int cpow = 200; // cost of pow in clops
    static int crand = 200; // cost of rand in clops
    static int crem = 100; // cost of reminder in clops
	static int IP; 		// Instruction Pointer (Address of Next Instruction) 
	static int FP; 		// Frame Pointer (Start Address of the current frame)
	static int SP; 		// Stack Pointer (Address of the element on top in the stack)
	static int OP;  		// Output Pointer (Address of the first free element in the output)
	static int HP;		// Heap Pointer (Address of the first free element in the heap)
	static int FFPP; 	// Pointer to the First Returned Function in the stored list 
	static int FSPP; 	// Pointer to the First String in the String Pool 
	static int LSPP; 	// Pointer to the Last String in the String Pool
	static double WRD1; 	// an internal double register for computations 
	static double WRD2; 	// a second internal double register for computations 
	static int WRI1; 		// an internal integer register for computations
	static int WRI2; 		// a second internal integer register for computations
	static int WRI3; 		// a third internal integer register for computations
	static int WRI4; 		// a fourth internal integer register reserved for deleting json/list elements
	static long WRL1; 	// an internal long register for computations
	static long WRL2; 	// a second long integer register for computations
	static String WRS1; 	// a string register for computations (a bit unrealistic)
	static String WRS2; 	// a second string register for computations (a bit unrealistic)
	static int HPC;		// Pointer to the first available heap triplet (json element, list element, field value)
	static int SPP; 		// current String Pool Pointer  
	static int GP; 		// current pointer to the garbage list of triplets
	static int NSPP; 	// Pointer to a New String being added to the String Pool  
	static int CODEN;	// Number of instructions in CODE
	static String ov_MEM =" - check the program or eventually increase the memory size > "
											+MS + " for Argument EXECMS";
	// types
	public static final int NoType=0;
	public static final int DoubleT=1;
	public static final int LongT=2;
	public static final int IntT=3;
	public static final int CharT=4;
	public static final int BoolT=5;
	public static final int NullT=6;
	public static final int StringT=7;
	public static final int ListT=8;
	public static final int JsonT=9;
	public static final int TypeT=10;
	public static final int MetaTypeT=11;
	public static final int RefT=12;
	static final int FieldT=13;
	public static final int FuncT=14;	
	// print format for exc output (int or quoted string) and for quoting chars and string
	// static final int IntExcF=13;
	public static final int StringExcF=15;
	static final int CharQF = 16; 
	static final int StringQF=17;
	static final int NullQF=18;
	static final int ListIndentAllF=19;
	static final int JsonIndentAllF=20;
	static final int ListIndent1F=21;
	static final int JsonIndent1F=22;
	
	static final int maxChar = Character.MAX_VALUE;
	static final int maxInt = Integer.MAX_VALUE;
	static final int minInt = Integer.MIN_VALUE+1;
	static final long maxLong = Long.MAX_VALUE;
	static final long minLong = Long.MIN_VALUE+1;
	
	static final int maxLenString = 1024; // max length for a string
	
	private static Random random = new Random();
	
	public static String fileExecName = "_CalcuList_fileExecF_";
	
	public static final String[] types = {
		"no type" ,		// types[0]
		"double" ,		// types[1]
		"long" ,			// types[2]
		"int" ,			// types[3]
		"char" ,		// types[4]
		"bool" ,		// types[5]
		"null", 		// types[6]	
		"string" ,		// types[7]
		"list",			// types[8]	
		"json",			// types[9]	
		"type", 		// types[10]	
		"metatype", 	// types[11]	
		"ref" ,			// types[12]
		"field",		// types[13]	
		"funct", 		// types[14]	
		"string exc",	// types[15]	
		"quoted char", 	// types[16]	
		"quoted string", // types[17]	
		"printable null", 	// types[18]	
		"formatted list", 	// types[19]	
		"formatted json", 	// types[20]	
		"1-level formatted list", 	// types[21]	
		"1-level formatted json" 	// types[22]	
	};
	
	public static int nExecMicroInstrs(  ) {
		return nClops;
	}

	public static void modSizeCS( int size ) {
		CS=Math.max(CS,size);
	}

	public static void modSizeMS( int size ) {
		MS=Math.max(MS,size);
	}

	public static void modSizeOS( int size ) {
		OS=Math.max(OS,size);
	}
	
	public static void startArray() {
		MEM = new double [MS]; 
		HP = MS-1;
		FSPP  = LSPP = NSPP = nullValue; // list of strings empty at the beginning
		GP=nullValue; // garbage list of triplets empty at the beginning
		OUTPUT = new double [OS];
		CODE = new Instruction [CS];
		BREAKPOINTS = new boolean [CS];
	}

	public static int getSP () { // return SP (equal to the size of the Stack)
		return SP;
	}

	public static int getHP () { // return HP
		return HP;
	}

	public static int getMS () { // return the size of Memory Array
		return MS;
	}

	public static int getCS () { // return the size of Code Array
		return CS;
	}
	
	public static int getOS () { // return the size of Output Array
		return OS;
	}
	
	public static int getOP () { // return the Output Pointer
		return OP;
	}
	
	public static Instruction getInstruction ( int i ) throws Exc{
		if ( i >= 0 && i < CS )
			return CODE[i];
		else
			throw new Exc_Exec(ErrorType.WRONG_ADDRESS, " to CODE");
	}

	public static void modInstruction ( int i, Instruction instr ) throws Exc{
		if ( i >= 0 && i < CS )
			CODE[i]=instr;
		else
			throw new Exc_Exec(ErrorType.WRONG_ADDRESS, " to CODE");
	}
	
	public static double valGV( int i ) {
		return MEM[i*2];
	}

	public static int typeGV( int i ) {
		return (int) MEM[i*2+1];
	}

	public static int addrGV( int i ) {
		return i*2;
	}

	static String typeName( int i ) {
		if ( i >= DoubleT && i <= NullQF )
			return types[i];
		else
			return "? (Code = "+i+")";
	}
	
	static void concatenateList() throws Exc {
		try {
	    	nClops+=3; // next check
	    	if ( MEM[SP-1] == nullValue ) { // 3 clops
	    		MEM[SP-1] = MEM[SP]; // 3 clops
	        	SP--; // 1 clop
	    		nClops +=4;
	    	}
	    	else {
	        	nClops+=2; // next check
	    		if ( MEM[SP] != nullValue ){ // 2 clops
	    			WRI1 = (int) MEM[SP-1]; // 2 clops
	    			nClops +=2;
	    			do {
	    				WRD1 = MEM[WRI1-2]; // 2 clops
	    				nClops +=3; // including next check
	    				if ( WRD1 == nullValue ) { // 1 clop
	    					MEM[WRI1-2]=MEM[SP]; // 3 clops
	    					nClops +=3;
	    				}
	    				else {
	    					WRI1=(int)WRD1; // 1 clop
	    					nClops++;
	    				}
	    				nClops ++; //  next check
	    			} while (WRD1!=nullValue); // 1 clop
	    		}
	        	SP--; // 1 clop
	        	nClops++;
	    	}	
			} catch ( Exception e ) {
				throw new Exc_Exec(ErrorType.EXEC_FAILURE, e.getMessage()+" - IP = "+IP);
			}
	}
	
	static void compareString () throws Exc {
		WRI2= (int)MEM[SP-1]; // 2 clops
		WRI3=(int) MEM[SP]; // 1 clop
		WRD1 = MEM[WRI2]; // 1 clops
		WRI1 = (int) MEM[WRI3]; // 1 clop
		SP--; // 1 clop
		nClops+=6;
		do {
			nClops+=2; // next check
			if (WRD1 == 0 && WRI1 == 0 ) { // 2 clops
				MEM[SP] = 0; // 1 clop
				nClops++;
				return;
			}
			nClops++; // next check
			if ( WRD1 == 0 ) { // 1 clop
				MEM[SP] = -1; // 1 clop
				nClops++;
				return;
			}
			nClops++; // next check
			if ( WRI1 == 0 ) { // 1 clop
				MEM[SP] = 1; // 1 clop
				nClops++;
				return;
			}
			WRI2--; WRI3--; WRD1--; WRI1--; // 4 clops
			nClops +=7; // including next check
			if ( MEM[WRI2] < MEM[WRI3] )	{ // 3 clops
				MEM[SP] = -1; // 1 clop
				nClops++;
				return;
			}
			nClops += 3; // next check
			if ( MEM[WRI2] > MEM[WRI3] )	{ // 3 clops
				MEM[SP] = 1; // 1 clop
				nClops++;
				return;
			}
		} while (true);
	}
	
	
	static void equalS () throws Exc {
		WRI2 = WRI1-1; // 2 clops
		WRD1 = NSPP-1; // 2 clops
		WRI3 = (int)MEM[NSPP]; // 1 clop 
		nClops+=6; // including next check
		while ( WRI3 >0 ) { // 1 clop
			nClops+=3; // next check
			if ( MEM[WRI2] != MEM[(int)WRD1] ) { // 3 clops
				WRI3=0; // 1 clop
				nClops++;
				return;
			}
			WRD1--; WRI2--; WRI3--; // 3 clops
			nClops+=3;
		}
		WRI3=1; // 1 clop
		nClops++;
		return;
	}
	
	static void searchString() throws Exc {
		WRI1 = FSPP; // 1 clop
		nClops +=2; // including next check
		if (WRI1 == nullValue ) { // no strings, 1 clop
			WRI1 = -1; // 1 clop
			nClops++;
			return;
		}
		do {
			nClops+=3; // next check
			if ( MEM[WRI1] == MEM[NSPP] ) { // 3 clops
				equalS();
				nClops++; // next check
				if ( WRI3 == 1 ) // 1 clop
					return;
			}
			WRI1 = (int)MEM[WRI1-(int)MEM[WRI1]-1]; // 4 clops
			nClops += 5; // including next check
		} while (WRI1 != nullValue); // 1 clop
		WRI1--; // 1 clop
		nClops++;
		return;
	}

	static void endAddString (String Instr) throws Exc {
		try {
	    	if ( SP >= HP   ) // 1 clop
        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,Instr+ov_MEM);
    		MEM[NSPP] = NSPP-HP-1; // 3 clops
	    	searchString();
	    	SP--; // 1 clop
	    	nClops+=5; // including next check
	    	if ( WRI1 < 0) { // 1 clop
	    		MEM[SP] = NSPP; // 1 clop
	    		nClops+=2; // including next check
	    		if ( FSPP == nullValue ) { // 1 clop
	    			FSPP=NSPP; // 1 clop
	    			nClops++;
	    		}
	    		else {
	    			MEM[LSPP-(int)MEM[LSPP]-1]=NSPP; // 4 clops
	    			nClops+=4;
	    		}
    			MEM[HP]= nullValue; // 1 clop
	    		LSPP=NSPP; // 1 clop
	    		NSPP=nullValue; // 1 clop
		    	HP--; // 1 clop
		    	nClops+=4;
	    	}
	    	else { // the string already exists
	    		HP=NSPP; // 1 clop
	    		NSPP=nullValue; // 1 clop
	    		MEM[SP] = WRI1;   // 1 clop
	    		nClops+=3;
	    	}			
		} catch ( Exc e ) {
			throw e; 
		}
		catch ( Exception e ) {
			throw new Exc_Exec(ErrorType.EXEC_FAILURE, e.getMessage()+" - IP = "+IP);
		}
	}
	
	static void addString1(String Instr) throws Exc {
		try {
	    	if ( SP >= HP   ) // 1 clop
        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,Instr+ov_MEM);
			NSPP=HP; HP--; // 2 clops
	    	WRD1 = MEM[SP-1]; // 2 clops
	    	WRI1 = (int) WRD1-1; // 2 clops
	    	WRD1 = MEM[(int)WRD1]; // 1 clop
	    	nClops+=9; // including last while check
	    	while ( WRD1 > 0 ) { // 1 clop
		    	if ( SP >= HP   ) // 1 clop
	        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,Instr+ov_MEM);
	    		 MEM[HP]=MEM[WRI1]; // 2 clops
	    		 HP--; WRI1--; WRD1--; // 3 clops
	    		 nClops+=7;
	    	}			
		}   catch ( Exc e ) {
			throw e; 
		}
		catch ( Exception e ) {
			throw new Exc_Exec(ErrorType.EXEC_FAILURE, e.getMessage()+" - IP = "+IP);
		}
}	
	
	static void addString2 (String Inst) throws Exc {
		try {
	    	WRD1 = MEM[SP]; // 1 clop
	    	WRI1 = (int) WRD1-1; // 2 clops
	    	WRD1 = MEM[(int)WRD1]; // 1 clop
	    	nClops +=5; // including last while check
	    	while ( WRD1 > 0 ) { // 1 clop
		    	if ( SP >= HP   ) // 1 clop
	        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,Inst + ov_MEM);
	    		 MEM[HP]=MEM[WRI1]; // 2 clops
	    		 HP--; WRI1--; WRD1--; // 3 clops
	    		 nClops+=7;
	    	}
		} catch ( Exc e ) {
			throw e; 
		}
		catch ( Exception e ) {
			throw new Exc_Exec(ErrorType.EXEC_FAILURE, e.getMessage()+" - IP = "+IP);
		}
	}
	
	
	static void appendString(String Instr) throws Exc {
		try {
			addString1(Instr);
	    	addString2(Instr);
	    	endAddString(Instr);
		}   catch ( Exc e ) {
				throw e; 
			}
			catch ( Exception e ) {
				throw new Exc_Exec(ErrorType.EXEC_FAILURE, e.getMessage()+" - IP = "+IP);
			}
	}

	static void appendCharToString(String Instr) throws Exc {
		try {
			addString1(Instr);
	    	if ( SP >= HP   ) // 1 clop
        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,Instr+ov_MEM);
			MEM[HP]= MEM[SP]; // 2 clops
			HP--; // 1 clop
	    	nClops +=4;
	    	endAddString(Instr);
		}   catch ( Exc e ) {
				throw e; 
			}
			catch ( Exception e ) {
				throw new Exc_Exec(ErrorType.EXEC_FAILURE, e.getMessage()+" - IP = "+IP);
			}
	}
	
	static void callSTRINGEL() throws Exc{
		SP--; // 1 clop
		if ( MEM[SP] < 0 ) // 2 clops
			throw new Exc_Exec(ErrorType.NEGATIVE_STRING_INDEX);
		nClops +=3; 
		WRI1 = (int) MEM[SP-2]; // 2 clops
		WRI2 = (int) MEM[WRI1]; // 1 clop g
		if (  MEM[SP]>=WRI2 ) // 2 clops
			throw new Exc_Exec(ErrorType.STRING_OUT_BOUND," - index = "+(int)MEM[SP]);
		SP--; // 1 clop
		MEM[SP-1]=MEM[WRI1-(int)MEM[SP+1]-1]; // 6 clops
		MEM[SP]=CharT; // 1 clop
		nClops+=13;
	}

	static void callSUBSTRIND() throws Exc{
		nClops+=3; // next check
		if ( MEM[SP+1] < 0 ) // 3 clops
			throw new Exc_Exec(ErrorType.NEGATIVE_STRING_INDEX);
		do {
			WRI2= (int)MEM[SP-1]; // 2 clops
			WRI3=(int) MEM[SP]; // 1 clop
			WRD1 = MEM[WRI2]-MEM[SP+1]; // 4 clops
			WRI2 -= MEM[SP+1]; // 2 clops
			WRI1 = (int) MEM[WRI3]; // 1 clops
			MEM[SP+2]=0; // 2 clops
			nClops+=12;
			do {
				nClops++; // next check
				if ( WRI1 == 0 ) { // 1 clop
					MEM[SP+2] = 1; // 2 clops
					nClops+=2; // found
				}
				else {
	    			nClops++; // next check
	    			if ( WRD1 == 0  ) { // 1 clop
	    				MEM[SP+2] = -2;  // 2 clops
	    				nClops+=2; // not foundable
	    			}
	    			else {
		    			WRI2--; WRI3--; WRD1--; WRI1--; // 4 clops
		    			nClops +=8; // including next check
		    			if ( MEM[WRI2] < MEM[WRI3] || MEM[WRI2] > MEM[WRI3])	{ // 4 clops
		    				MEM[SP+2] = -1; // 2 clops
		    				nClops+=2; // not found so far 
		    			}
	    			}
				}
				nClops+=2; // next while check
			} while (MEM[SP+2]==0); // 2 clops
			nClops+=2; // next check
			if ( MEM[SP+2]==-1) {// 2 clops
				MEM[SP+1]++; // 4 clops
				nClops+=4;
			}
			nClops+=2; // next while check
		} while ( MEM[SP+2]==-1 ); // 2 clops
		nClops+=2; // next check
		if ( MEM[SP+2]==1 ) {// 2 clops
			MEM[SP-1]=MEM[SP+1]; // 4 clops
			nClops+=4;
		}
		else {
			MEM[SP-1]=-1; // 2 clops
			nClops+=2;
		}
		MEM[SP]=IntT; // 1 clop
		nClops++;
	}

	static void callSUBSTRING0(String Instr) throws Exc {
		SP--; // 1 clop
		if ( MEM[SP] < 0 ) // 2 clops
			throw new Exc_Exec(ErrorType.NEGATIVE_STRING_INDEX);
		nClops+=3; // including next check
		if ( SP  >= HP   ) // 1 clop
			throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,Instr+ov_MEM);
		NSPP=HP; HP--; // 2 clops
		WRI1 = (int) MEM[SP-2]; // 2 clops 
		WRI2 = (int) MEM[WRI1] - (int) MEM[SP]; // 3 clops
		nClops+=8; // including next check
		if ( WRI2 > 0 ){ // 1 clop 
			WRI1 = WRI1-1- (int)MEM[SP]; // 3 clops
			nClops +=3;
			do {
		    	if ( SP  >= HP   ) // 1 clop
	        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,Instr+ov_MEM);
				MEM[HP]= MEM[WRI1]; // 2 clops
				HP--; // 1 clop		    	    				
				WRI1--; WRI2--; // 2 clops
				nClops +=7; // including next check
			} while (WRI2 > 0); // 1 clop
		}
		SP --; // 1 clop
		endAddString(Instr);   	    	
		if (SP  >= HP) // 1 clop
			throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,Instr+ov_MEM);
		SP++; MEM[SP]=StringT; // 2 clops
		nClops+=4;			        		
	}
	
	static void callSUBSTRING1(String Instr) throws Exc {
		SP-=2;// 1 clop
		MEM[SP]= MEM[SP+1]; // 3 clops
		nClops+=9; // including next check
		if ( MEM[SP] < 0 || MEM[SP-1] < 0 ) // 5 clops
			throw new Exc_Exec(ErrorType.NEGATIVE_STRING_INDEX);
		if ( SP  >= HP   ) // 1 clop
			throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,Instr+ov_MEM);
		NSPP=HP; HP--; // 2 clops
		WRI2 = (int)MEM[SP]; // 1 clop
		WRI1 = (int) MEM[SP-3]; // 2 clops 
		WRD1 = MEM[WRI1]; // 2 clops
		WRI2= WRI2>WRD1? (int) WRD1: WRI2; // 2 clops
		WRI2 = WRI2-(int)MEM[SP-1]; // 3 clops
		WRD1 = WRD1 - MEM[SP-1]; // 3 clops
		nClops+=18; // including next check
		if ( WRI2 > 0 && WRD1 > 0 ){ // 2 clops
			WRI1 = WRI1 -1- (int)MEM[SP-1]; // 3 clops
			nClops +=3;
			do {
				if ( SP  >= HP   ) // 1 clop
	        			throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,Instr+ov_MEM);
				MEM[HP]= MEM[WRI1]; // 2 clops
				HP--; // 1 clop		    	    				
				WRI1--; WRI2--; // 2 clops
				nClops +=7; // including next check
			} while (WRI2 > 0 ); // 1 clop
		}
		SP--; SP--;// 2 clops
		endAddString(Instr);   	    	
		if (SP  >= HP) // 1 clop
			throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,Instr+ov_MEM);
		SP++; MEM[SP]=StringT; // 2 clops
		nClops+=5;
	}

	static void callGetHeapTriplet(String Instr) throws Exc {
		nClops++; // next check
		if ( GP == nullValue ) { // 1 clop
			HPC = HP; // 1 clop
	        HP-=2; // 2 clops
	    	if (SP  >= HP) // 1 clop
	    		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,Instr+ov_MEM);
	        MEM[HP] = nullValue; // 1 clop
	        HP--;// 1 clop
	        nClops+=6;
		}
		else {
			HPC=GP; // 1 clop
			GP = (int) MEM[GP-2]; // 3 clops
			MEM[HPC-2]=nullValue; // 3 clops
			nClops+=7;
		}
	}
	
	static void callDelHeapTriplet() throws Exc {
		MEM[WRI2]=nullValue; // 1 clop
		MEM[WRI2-1]= NoType; // 2 clops
		MEM[WRI2-2]=GP; // 2 clops
		GP=WRI2; // 1 clop
		nClops+=6; // including while check
	}
	
	static void callNullify() throws Exc {
	    	if ( MEM[SP] < DoubleT || MEM[SP] > MetaTypeT ) // 3 clops
	    		throw new Exc_Exec(ErrorType.NULLIFY_NOT_SUPPORTED," for type "+typeName((int)MEM[SP]));
	    	nClops+=6; // including next check
	    	if ( MEM[SP] == JsonT || MEM[SP] == ListT ) { // 3 clops
	    			WRI1=(int)MEM[SP-1]; // 2 clops
	    			nClops+=7; // including next if check
	    			if ( MEM[WRI1-1] == FieldT && MEM[WRI1]==nullValue )  { // 5 clops
	    				// initial null field for a json
	    				WRI2=(int)MEM[WRI1-2]; // 2 clops
	    				MEM[WRI1-2]= nullValue; // 2 clops
	    				WRI1=WRI2; // 1 clop
	    				nClops+=5;
	    			}
	    			nClops++; // last while check
	    			while ( WRI1 != nullValue ) { // 1 clop
	    				if ( MEM[WRI1-1] == FieldT && MEM[WRI1]!=nullValue )  { // 5 clops
	    					WRI2= (int) MEM[WRI1]; // 1 clop
	        				callDelHeapTriplet(); // delete key-value triplet
	        				nClops++;
	    				}
	    				WRI2=WRI1; // 1 clop
	    				WRI1 = (int) MEM[WRI2-2]; // 2 clops
	    				callDelHeapTriplet();
	    				nClops+=9; // including while check
	    			}
	    	}	
	}
	
	static void callSkipDeletedNulls() throws Exc {		
		while (WRI4!=nullValue && (MEM[WRI4]==deletedNullValue && MEM[WRI4-1]==NullT || // 6 clops
				MEM[WRI4-1]==FieldT && MEM[(int)MEM[WRI4]]==deletedNullValue && MEM[(int)MEM[WRI4]-1]== NullT) ) { // 10 clops
			WRI4=(int)MEM[WRI4-2]; // 2 clops
			nClops+=18; // including while check 
		}
		nClops+=16; // last while check
	}
	
	static void callLISTEL() throws Exc {
	    	if ( MEM[SP] != IntT && MEM[SP] != CharT ) // 3 clops (assuming M[SP] is kept into a register)
	    		throw new Exc_Exec(ErrorType.INT_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");
	    	SP--; // 1 clop
	    	if ( MEM[SP] < 0 ) // 2 clops
	    		throw new Exc_Exec(ErrorType.NEGATIVE_LIST_INDEX);
	    	WRI4=(int)MEM[SP-2]; // 2 clops
	    	callSkipDeletedNulls();
	     	if ( WRI4 == nullValue ) // 1 clop
	    		throw new Exc_Exec(ErrorType.LIST_OUT_BOUND);
	    	nClops += 9;
	    	while (MEM[SP]>0) { // 2 clops
	 				WRI4=(int)MEM[WRI4-2]; // 2 clops 
					MEM[SP]--; // 3 clops
			    	callSkipDeletedNulls();
		        	if ( WRI4 == nullValue ) // 1 clop
		        		throw new Exc_Exec(ErrorType.LIST_OUT_BOUND);
		        	nClops +=8; // including while check
			};
		nClops+=2; // 2 clops for last while check
	    	SP--; // 1 clop
	    	MEM[SP-1]=WRI4; // 2 clops
	    	nClops+=3;	
	}
	
	static void callNEWFLD (String Instr) throws Exc {
        callGetHeapTriplet(Instr);		
      	SP-=2; // 2 clops
      	MEM[HPC]=MEM[SP+1]; // the field value - 3 clops
       	MEM[HPC-1]=MEM[SP+2]; // the field type - 4 clops
       	MEM[HPC-2]=-MEM[SP-1]; // negative link for the key - 5 clops
       	MEM[SP-1]=HPC; // 4 clops
       	MEM[SP]=FieldT; // 1 clop
       	HPC--; // 1 clop
      	nClops+=20;	
	}
	
	static void callFIELDVAL() throws Exc {
	    	nClops+=2; // next check
	    	if ( MEM[SP-1]==nullValue ) { // 2 clops
	    		MEM[SP] = NullT; // 1 clop
	    		nClops++;
	    	}
	    	else {
	    		WRI1 = (int) MEM[SP-1]; // 2 clops
	    		MEM[SP-1] = MEM[WRI1]; // 3 clops
	    		MEM[SP] = MEM[WRI1-1]; // 3 clops
	    		nClops+=8;
	    	}		
	}
	
	static void callFLDFIND0() throws Exc {
    	if ( MEM[SP] != StringT ) // 2 clops
    		throw new Exc_Exec(ErrorType.STRING_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");
		SP--; // 1 clop
		MEM[SP-1]=MEM[SP]; // 3 clops
		SP--; // 1 clop
		WRI1 = (int) MEM[SP-1]; // 2 clops
		WRI4 = (int) MEM[WRI1-2]; // skip the initial null field - 2 clops
		callSkipDeletedNulls();
		MEM[SP-1] = nullValue; // null value - 2 clops
		nClops += 14; // including last while check
		while ( WRI4 != nullValue ) { // 1 clop
			WRI2 = (int) MEM[WRI4]; // 1 clop
			nClops += 7; // including while check and next check
			if ( -MEM[WRI2-2] == MEM[SP] ) { // 5 clops
				MEM[SP-1]=WRI2; // 2 clops
				WRI4=nullValue; // 1 clop
				nClops+=3;
			}
			else {
				WRI4 = (int) MEM[WRI4-2]; // 2 clops
				callSkipDeletedNulls();
				nClops+=2;
			}
		}
		MEM[SP] = FieldT; // 1 clop
		nClops++;
	}
	
	static void callFLDFIND1(String Instr) throws Exc {
	    	if ( MEM[SP] != StringT ) // 2 clops
	    		throw new Exc_Exec(ErrorType.STRING_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");
		WRI1 = (int) MEM[SP-3]; // 2 clops
		WRI3=WRI1;
		WRI4 = (int) MEM[WRI1-2]; // skip the initial null field - 2 clops
		callSkipDeletedNulls();
		nClops += 7; // including last while check
		while ( WRI4 != nullValue ) { // 1 clop
			WRI2 = (int) MEM[WRI4]; // 1 clop
			nClops += 8; // including while check and next check
			if ( -MEM[WRI2-2] == MEM[SP-1] ) { // 6 clops
				SP-=2; // 2 clops
				MEM[SP-1]=WRI2; // 2 clops
				MEM[SP] = FieldT; // 1 clop
				nClops+=5;
				return;
			}
			else {
				WRI3 = WRI4; // 1 clop
				WRI4 = (int) MEM[WRI4-2]; // 2 clops
				callSkipDeletedNulls();
				nClops+=3;
			}
		}
		// new field
		MEM[SP-2]=WRI3; // 3 clops
	    	if (SP +1 >= HP ) // 2 clops
	    		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,Instr+ov_MEM);
	    	SP++; // 1 clop
	    	MEM[SP] = nullValue; // 1 clop
	    	SP++; // 1 clop
	    	MEM[SP] = NullT; // 1 clop
	    	callNEWFLD(Instr);
	    	WRI1 = (int) MEM[SP-1]; // 2 clops
	    	callCLIST(Instr);
	    	MEM[SP-1]=WRI1; // 2 clops
		MEM[SP] = FieldT; // 1 clop
	    	nClops+=16;
	}
	
	static void callSJLIST(String Instr) throws Exc {
	    	if (SP + 3 >= HP) // 2 clops
	    		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,Instr+ov_MEM);
	    	SP++; MEM[SP] = nullValue; // 2 clops
	    	SP++; MEM[SP] = nullValue; // 2 clops
	    	SP++; // 1 clop
	    	MEM[SP]=nullValue; // null field - 1 clop
	    	SP++; // 1 clop
	    	MEM[SP]=FieldT; // 1 clop
	    	nClops+=10;
	    	callHLIST(Instr);		
	}
	
	static void callHEAD() throws Exc {
		WRI4 = (int) MEM[SP-1]; // 2 clops
		callSkipDeletedNulls();
	    	if ( WRI4 == nullValue ) // 1 clop
	    		throw new Exc_Exec(ErrorType.EMPTY_LIST," - head element is missing");
	    	WRI1 = WRI4; // 1 clop
	    	MEM[SP-1] = MEM[WRI1]; // 3 clops
	    	MEM[SP] = MEM[WRI1-1]; // 3 clops
		nClops +=10;		
	}
	
	static void callHLIST (String Instr) throws Exc {
        callGetHeapTriplet(Instr);		
        MEM[HPC-1] = MEM[SP]; // 3 clops
        SP--; // 1 clop
        MEM[HPC] = MEM[SP]; // 2 clops
        SP--; // 1 clop
        MEM[SP]= HPC; // 1 clop
        MEM[SP-1]= HPC; // 2 clops
		nClops +=10;		
	}
	
	static void callCLIST(String Instr) throws Exc {
        callGetHeapTriplet(Instr);		
        MEM[HPC-1] = MEM[SP]; // 3 clops
        SP--; // 1 clop
        MEM[HPC] = MEM[SP]; // 2 clops
        SP--; // 1 clop
        MEM[(int)MEM[SP]-2]= HPC; // 3 clops
        MEM[SP]= HPC; // 1 clops
		nClops +=11;	
	}
	
	static void callJSTOLIST1(String Instr) throws Exc {
		if ( SP +3 >= HP ) // 2 clops
			throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,Instr+ov_MEM);
		SP++; // 1 clop
    	MEM[SP] = nullValue; // 1 clop
		SP++; // 1 clop
    	MEM[SP] = nullValue; // 1 clop
		WRI2 = (int) MEM[WRI4]; // 1 clop 
		SP++; // 1 clop
		MEM[SP] = -MEM[WRI2-2]; // 3 clops
		SP++; // 1 clop
		MEM[SP] = StringT; // 1 clop
		callHLIST(Instr);
		if ( SP +1 >= HP ) // 2 clops
			throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,Instr+ov_MEM);
		SP++; // 1 clop
		MEM[SP] = MEM[WRI2]; // 2 clops
		WRI2--; // 1 clop 
		SP++; // 1 clop
		MEM[SP] = MEM[WRI2]; // 2 clops
		callCLIST(Instr);
		MEM[SP]=ListT; // 1 clop	
		nClops+=23;
	}

	static void callJSTOLIST(String Instr) throws Exc {
		WRI1 = (int) MEM[SP-1]; // 1 clop
		WRI4 = (int) MEM[WRI1-2]; // skip the initial null field - 2 clops
		callSkipDeletedNulls();
    	MEM[SP-1] = nullValue; // 2 clops
    	MEM[SP] = nullValue; // 1 clop
		nClops +=7; // including next check
		if ( WRI4 != nullValue ) { // 1 clop
   			callJSTOLIST1(Instr); 
   			callHLIST(Instr);
			WRI4 = (int) MEM[WRI4-2]; // 2 clops
			callSkipDeletedNulls();
	        nClops +=2;		
		}
		while ( WRI4 != nullValue ) { // 1 clop
  			callJSTOLIST1(Instr); 
  			callCLIST(Instr);
			WRI4 = (int) MEM[WRI4-2]; // 2 clops
	        nClops +=2;		
			callSkipDeletedNulls();
	    }
		nClops++; // last while check
		MEM[SP]= ListT; // 2 clops
		nClops+=2;
	}
	
	static void callSJSON(String Instr) throws Exc {
    	SP++; // 1 clop
    	MEM[SP]=nullValue; // null field - 1 clop
    	SP++; // 1 clop
    	MEM[SP]=FieldT; // 1 clop
    	nClops+=3;
    	callHLIST(Instr);		
	}
	
	static void callEQ() throws Exc {
    	nClops+=5; // next check
    	if ( MEM[SP] <= CharT && MEM[SP+2] <= CharT  ) { // 5 clops
    		nClops+=2; // next check
    		if ( MEM[SP]==LongT ) { // 2 clops
    			if ( MEM[SP+2] == LongT) { // 3 clops
    				WRI1 = Double.doubleToRawLongBits(MEM[SP-1]) == 
    						Double.doubleToRawLongBits(MEM[SP+1])? 1: 0; // 6 clops
    			}
    			else 
    				WRI1 = Double.doubleToRawLongBits(MEM[SP-1]) == 
					MEM[SP+1]? 1: 0; // 6 clops;
    			nClops+=	9;		
    		}
    		else {
    			if ( MEM[SP+2] == LongT) { // 3 clops
    				WRI1 = MEM[SP-1] == 
    						Double.doubleToRawLongBits(MEM[SP+1])? 1: 0; // 6 clops
    			}
    			else 
    				WRI1 = MEM[SP-1] == MEM[SP+1]? 1: 0; // 6 clops;
    			nClops+=	9;		
    		}
    	}
    	else {
    		if ( MEM[SP]==MEM[SP+2] || MEM[SP] == NullT  || MEM[SP+2] == NullT 
    				|| (MEM[SP]==TypeT&&MEM[SP+2]==MetaTypeT) 
    				|| (MEM[SP+2]==TypeT&&MEM[SP]==MetaTypeT) ) // 9 clops
    			WRI1= MEM[SP-1]==MEM[SP+1] && MEM[SP] == MEM[SP+2]? 1: 0; // 8 clops
    		else
        		throw new Exc_Exec(ErrorType.EQ_NOT_SUPPORTED,
        				" for types '"+typeName((int)MEM[SP])+
        				"' and '"+typeName((int)MEM[SP+2])+"'");		        			
    		nClops+=17;
    	}
        MEM[SP]=BoolT; // 1 clop
		nClops ++;
	}
	
	static void callLT() throws Exc {
		nClops+=5; // next check
    	if ( MEM[SP] == NullT || MEM[SP+2] == NullT ) { // 5 clops
    		WRI1= 0; // 1 clop
    		nClops++;
    	}
    	else {
        	nClops+=6; // next check
        	if ( MEM[SP+2]==StringT ) { // 3 clops
            	MEM[SP]=MEM[SP+1]; // 3 clops
        		compareString();
        		if ( MEM[SP] == -1 ) // 2 clops
        			WRI1=1; // 1 clop
        		else
        			WRI1=0; // 1 clop
        		SP++; // 1 clop
        		nClops+=6;
        	}
        	else {
        		nClops+=2;
	    		if ( MEM[SP]==LongT ) { // 2 clops
	    			if ( MEM[SP+2] == LongT) { // 3 clops
	    				WRI1 = Double.doubleToRawLongBits(MEM[SP-1]) < 
	    						Double.doubleToRawLongBits(MEM[SP+1])? 1: 0; // 6 clops
	    			}
	    			else 
	    				WRI1 = Double.doubleToRawLongBits(MEM[SP-1]) < 
						MEM[SP+1]? 1: 0; // 6 clops;
	    			nClops+=	9;		
	    		}
	    		else {
	    			if ( MEM[SP+2] == LongT) { // 3 clops
	    				WRI1 = MEM[SP-1] < 
	    						Double.doubleToRawLongBits(MEM[SP+1])? 1: 0; // 6 clops
	    			}
	    			else 
	    				WRI1 = MEM[SP-1] < MEM[SP+1]? 1: 0; // 6 clops;
	    			nClops+=	9;		
	    		}
        	}
    	}
        MEM[SP]=BoolT; // 1 clop
		nClops ++;
	}

	static void callLTE() throws Exc {
		nClops+=5; // next check
    	if ( MEM[SP] == NullT || MEM[SP+2] == NullT ) { // 5 clops
    		WRI1= MEM[SP] == MEM[SP+2]?1: 0; // 5 clops
    		nClops+=5;
    	}
    	else {
        	nClops+=6; // next check
        	if ( MEM[SP+2]==StringT ) { // 3 clops
            	MEM[SP]=MEM[SP+1]; // 3 clops
        		compareString();
        		if ( MEM[SP] == 1 ) // 2 clops
        			WRI1=0; // 1 clop
        		else
        			WRI1=1; // 1 clop
        		SP++; // 1 clop
        		nClops+=6;
        	}
        	else {
        		nClops+=2;
	    		if ( MEM[SP]==LongT ) { // 2 clops
	    			if ( MEM[SP+2] == LongT) { // 3 clops
	    				WRI1 = Double.doubleToRawLongBits(MEM[SP-1]) <= 
	    						Double.doubleToRawLongBits(MEM[SP+1])? 1: 0; // 6 clops
	    			}
	    			else 
	    				WRI1 = Double.doubleToRawLongBits(MEM[SP-1]) <= 
						MEM[SP+1]? 1: 0; // 6 clops;
	    			nClops+=	9;		
	    		}
	    		else {
	    			if ( MEM[SP+2] == LongT) { // 3 clops
	    				WRI1 = MEM[SP-1] <=
	    						Double.doubleToRawLongBits(MEM[SP+1])? 1: 0; // 6 clops
	    			}
	    			else 
	    				WRI1 = MEM[SP-1] <= MEM[SP+1]? 1: 0; // 6 clops;
	    			nClops+=	9;		
	    		}
        	}
    	}
        MEM[SP]=BoolT; // 1 clop
		nClops ++;
	}
	
	static void callCJSON(String Instr) throws Exc {
    	WRI1 = (int) MEM[SP-3]; // json first element - 2 clops
    	WRI4 = (int) MEM[WRI1-2]; // skip the initial null field - 2 clops
		callSkipDeletedNulls();
    	WRI2 = (int) MEM[SP-1]; // field to be added - 2 clops
    	WRI2 = (int) MEM[WRI2-2]; // key of the field to be added - 2 clops
    	nClops+=11; // incuding last while check
    	while ( WRI4 > nullValue  && WRI1 != -1) { // 1 clop
    		nClops+=5; // next check
    		if ( WRI2 == MEM[(int) MEM[WRI4]-2] ) { // 5 clops
    			WRI1 =-1; // found duplicate key - 1 clop
    			nClops++;
    		}
    		else {
    			WRI4 = (int) MEM[WRI4-2]; // 2 clops
    			nClops+=2;
    			callSkipDeletedNulls();
    		}		        			
    	}
    	nClops++; // next check
     	if ( WRI4  > nullValue ) // 1 clop
    		throw new Exc_Exec(ErrorType.DUPLICATED_KEY,"'"+
    					PrintOutput.getStringHeap(-WRI2)+"'"); 
    					// negative address for the string (key) into a field	
     					// trick used for the debugger
    	callCLIST(Instr);		
	}

	static void callJSCLONE(String Instr) throws Exc {
		WRI1 = (int) MEM[SP-1]; // 1 clop
		WRI4 = (int) MEM[WRI1-2]; // skip the initial null field - 2 clops
		callSkipDeletedNulls();
		SP-=2; // 2 clops
		callSJLIST(Instr);
		nClops+=5;
		while ( WRI4 != nullValue ) { // 1 clop
			if ( SP +3 >= HP ) // 2 clops
				throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,Instr+ov_MEM);
			WRI2 = (int) MEM[WRI4]; // 1 clop 
			SP++; // 1 clop
			MEM[SP] = -MEM[WRI2-2]; // 4 clops
			SP++; // 1 clop
			MEM[SP] = StringT; // 1 clop
			SP++; // 1 clop
			MEM[SP] = MEM[WRI2]; // 2 clops
			WRI2--; // 1 clop 
			SP++; // 1 clop
			MEM[SP] = MEM[WRI2]; // 2 clops
			callNEWFLD(Instr);
			WRI4 = (int) MEM[WRI4-2]; // 2 clops
			callSkipDeletedNulls();
	        nClops +=20; // including while check
	        callCLIST(Instr);
		};
		nClops++; // 1 clop for last while check
		MEM[SP]=JsonT; // 1 clop
		nClops++;
	}
	
	static void callSLCLONE0(String Instr) throws Exc {
		SP--; // 1 clop
		WRI4=(int)MEM[SP-2]; // 2 clops
		callSkipDeletedNulls();
		if ( MEM[SP] < 0 ) // 2 clops
			throw new Exc_Exec(ErrorType.NEGATIVE_LIST_INDEX);
		nClops+=5; 
		while (MEM[SP]>0 && WRI4 != nullValue ) { // 3 clops
				WRI4=(int)MEM[WRI4-2]; // 2 clops
				callSkipDeletedNulls();
				MEM[SP]--; // 3 clops
	        	nClops +=8; // including while check
			};
		nClops+=3; // 4 clops for last while check
		SP--; // 1 clop
		nClops+=2; // including next check
		if ( WRI4 != nullValue ) { // 1 clop
			SP--; // 1 clop
			callGetHeapTriplet(Instr);
			MEM[SP]=HPC; // 1 clop
			nClops +=2;
			do {
				MEM[HPC] = MEM[WRI4]; // 2 clops
				MEM[HPC-1] = MEM[WRI4-1]; // 4 clops
				WRI4=(int)MEM[WRI4-2]; // 2 clops
				callSkipDeletedNulls();
				nClops +=9; // including next check
				if ( WRI4 == nullValue ) { // 1 clop
					MEM[HPC-2]=nullValue; // 2 clops
					nClops +=2;
				}
				else {
					WRI3=HPC-2; // 2 clops
					callGetHeapTriplet(Instr);
					MEM[WRI3]=HPC;  // 1 clop
					nClops +=3;
				}
		    	nClops++; // next while check
			} while (WRI4 != nullValue );
		 	if (SP  >= HP) // 1 clop
		 		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,Instr+ov_MEM);
			SP++; // 1 clop
			MEM[SP]=ListT; // 1 clop
			nClops+=3;
		}
		else {
			MEM[SP-1]=nullValue; // 2 clops
			MEM[SP]=ListT; // 1 clop
			nClops+=3;
		}
	}
	
	static void callSLCLONE1(String Instr) throws Exc {
		SP-=3;// 2 clops
		MEM[SP-1]= MEM[SP]; // 3 clops
		MEM[SP]= MEM[SP+2]; // 3 clops
		nClops+=12; // including next check
		if ( MEM[SP] < 0 || MEM[SP-1] < 0 ) // 4 clops
			throw new Exc_Exec(ErrorType.NEGATIVE_LIST_INDEX);
		MEM[SP] = MEM[SP]-MEM[SP-1]; // 4 clops
		nClops +=6; // including next check
	 	if ( MEM[SP] <= 0 ) { // 2 clops
			SP--; // 1 clop
			MEM[SP-1]=nullValue; // 2 clops
			MEM[SP]=ListT; // 1 clop
			nClops+=4;
		}
		else {
			SP--; // 1 clop
			WRI4=(int)MEM[SP-1]; // 2 clops
			callSkipDeletedNulls();
			nClops+=3;
		    	while (MEM[SP]>0 && WRI4 != nullValue ) { // 3 clops
		 				WRI4=(int)MEM[WRI4-2]; // 2 clops
						callSkipDeletedNulls();
						MEM[SP]--; // 3 clops
			        	nClops +=8; // including while check
		 	};
		    	nClops+=4; // last while check and  next check
	 		if ( WRI4 != nullValue ) { // 1 clop
	     			MEM[SP]=MEM[SP+1]; // 3 clops
	    			callGetHeapTriplet(Instr);
	    			MEM[SP-1]=HPC; // 2 clops
		        	nClops +=5; 
	     			do {
	     				MEM[HPC] = MEM[WRI4]; // 2 clops
	     				MEM[HPC-1] = MEM[WRI4-1]; // 4 clops
	     				WRI4=(int)MEM[WRI4-2]; // 2 clops
	     				callSkipDeletedNulls();
	     				MEM[SP]--; // 3 clops
	     				nClops +=15; // including next check
	     				if ( WRI4 == nullValue || MEM[SP] <= 0 ) { // 4 clops
	     					MEM[HPC-2]=nullValue; // 2 clops
	     					nClops +=2;
	     				}
	     				else {
	    					WRI3=HPC-2; // 1 clop
	    					callGetHeapTriplet(Instr);
	     					MEM[WRI3]=HPC; // 1 clop
	     					nClops +=2;
	      				}
	     				nClops +=4; // next while check
	     			} while (WRI4 != nullValue && MEM[SP]>0); // 4 clops
	 			}
			else {
				MEM[SP-1]=nullValue; // 2 clops
				nClops+=2;
			}
	     	MEM[SP]=ListT; // 1 clop
	     	nClops++;
		}
	}
	
	static void callBuilds() throws Exc {
		if (CODEN + 2*WRI1 +4 >= CS) // 4 clops
			throw new Exc_Exec(ErrorType.LARGE_CODE, " Internal: increase the size of CODE in Exec > "
					+CODEN + " for Argument EXECCS");
		CODE[CODEN]=new Instruction(Operator.START,0,"* Run Time Function calling Lambda -> "+(int)MEM[SP-1]); // 2 clops
		CODEN++; // 1 clop
		WRI4 = FP-2*WRI1; // 2 clops
		nClops += 8; // including last while check
		while ( WRI4 < FP ) { // 1 clop
			CODE[CODEN]=new Instruction(Operator.PUSHV,MEM[WRI4]); // 4 clops
			CODEN++; // 1 clop
			WRI4++; // 1 clop
			nClops += 7;
		}
		CODE[CODEN]=new Instruction(Operator.PUSHARG,WRI2); // 2 clops
		CODEN++; // 1 clop
		CODE[CODEN]=new Instruction(Operator.CALL,WRD1,"-> lambda function"); // 3 clops
		CODEN++; // 1 clop
		CODE[CODEN]=new Instruction(Operator.RETURN,WRI2); // 3 clops	
		CODEN++; // 1 clop
		nClops += 11;
	}
	
	static void execF (boolean debugAct, int IPC) throws Exc{ // execute code from text file
			File file;
			FileInputStream fstream;
			try {
				file = new File(fileExecName);
				fstream = new FileInputStream(file);
			} catch ( Exception e ) {
				throw new Exc_Assembler(ErrorType.MISSING_EXEC_FILE, fileExecName+": unexpected error");
			}
			try {
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine = br.readLine();
				while ( strLine!= null ) {
					String[] tokens = strLine.split("\\p{Space}");
					IR=new Instruction(Instruction.getOperatorCode(tokens[0]),
							Double.parseDouble(tokens[1]));
					exec1(debugAct,IR,IPC);
					strLine = br.readLine();
				}
				fstream.close();
				br.close();
			} 
			catch ( Exc e ) {
				try { fstream.close(); } catch( Exception e1 ) {};
				throw e;
			}
			catch ( Exception e ) {
				throw new Exc_Assembler(ErrorType.WRONG_EXEC_FILE, fileExecName+": unexpected error");
			} 
	}

	
	public static void exec( boolean debugAct, int nC) throws Exc {
		CODEN = nC;
		IP = 0; OP=0; 
		if (NSPP != nullValue) {
			HP = NSPP;
			NSPP=nullValue;
		}
		nClops = 0;
		if ( debugAct )  Debug.debugStart();
	
		try {
		    
			while ( IP >= 0) {
				  if ( IP >= CS ) // 1 clop
					  throw new Exc_Exec(ErrorType.WRONG_IP);
			      int IPC = IP; // current IP
				  // FETCH
				  IR = CODE[IP]; // 1 clop
				  IP++; // next IP, 1 clop
				  nClops +=3;
				  debugAct=exec1(debugAct,IR,IPC);
			}
		} catch (Exc e) {
			e.addMessage(" - IP ="+IP);
			throw e; 
		}
		catch ( Exception e ) {
			throw new Exc_Exec(ErrorType.EXEC_FAILURE, e.getClass().getName()+" ("+e.getMessage()+") - IP = "+IP);
		}
	}
		
		static boolean exec1(boolean debugAct, Instruction IR, int IPC) throws Exc{
			      		      
			try {
			      switch (IR.getOperator()) {
			        case INIT: // start an execution unit
			        	FP=0; // 1 clop
			        	WRI1= (int)IR.getOperand(); // 1 clop
			        	SP = 2*WRI1-1; // 2 clops
			        	if (SP >= HP ) // 1 clops
			        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"START "+ov_MEM);
			        	if (FFPP != nullValue ) { // 1 clop
			        		callNullify();
			        		FFPP = nullValue; // 1 clop
			        		nClops++;
			        	}
			        	if ( debugAct )  Debug.initFrame(WRI1);
					nClops +=6;
			        	break;
		        	
		        case HALT: // end an execution unit
		        	IP=-1; // 1 clop
					nClops ++;
		        	break;
		
		        case START: // start the execution of a function 
        			// by setting up its frame and adding the link to the previous pointer 
		        	SP++; // 1 clop
			        MEM[SP] = FP; // 1 clop
			        FP = SP-1; // 1 clop 
			        SP+=(int) IR.getOperand()*2; // 3 clops
		        	if (SP >= HP ) // 1 clops
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"START "+ov_MEM);
			        if ( debugAct )  Debug.addFrame(FP);
					nClops +=7;
		        	break;

		        case RETURN: // end the execution of the current function 
		        			 // by removing its frame and its n arguments,
		        			 // where n is the command operand,
		        			// and by returning the control to the calling function
		        	WRI1 = (int) IR.getOperand(); // 1 clop
		        	WRD1 = MEM[FP+1]; // 2 clops
		        	IP = (int) MEM[FP]; // 1 clop
		        	WRI1 = FP-2*WRI1; // 3 clops
		        	MEM[WRI1] = MEM[SP-1]; // 3 clops
		        	MEM[WRI1+1] = MEM[SP]; // 3 clops
		        	SP = WRI1+1; // 1 clop
		        	FP = (int) WRD1; // 1 clops
		        	if ( debugAct )  Debug.removeFrame();
		        	nClops +=15; 
		        	break;

		        case THROWE: // stop the execution by throwing an exception reported in two strings
	       			 // that are written on OUTPUT
		        	if ( MEM[SP] != StringT ) // 2 clops
		        		throw new Exc_Exec(ErrorType.THROWE_NOT_SUPPORTED," for type "+typeName((int)MEM[SP]));
		        	if ( MEM[SP-2] != StringT ) // 3 clops
		        		throw new Exc_Exec(ErrorType.THROWE_NOT_SUPPORTED," for type "+typeName((int)MEM[SP-2]));
				    if (OP+3 > OS ) // 2 clops
				       	throw new Exc_Exec(ErrorType.OUTPUT_OVERFLOW, "Internal: increase the output size > "
									+OS + " for Argument EXECOS");
				    OUTPUT[OP]= MEM[SP-3]; // 3 clops 
				    OP++; // 1 clop
				    OUTPUT[OP]= StringExcF; // 1 clop 
				    OP++; // 1 clop
				    OUTPUT[OP]= MEM[SP-1]; // 3 clops 
				    OP++; // 1 clop
				    OUTPUT[OP]= StringT; // 1 clop
				    OP++; // 1 clop
			       	IP=-1; // 1 clop
					nClops +=20;
			       	break;

		        case CALL:	// call the function whose code starts at the address 
        			// denoted by the command operand
        			// the frame of the called function is started on the stack
        			// by storing the calling function return address
		           	if (SP >= HP ) // 1 clops
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"CALL "+ov_MEM);
		           	SP++; // 1 clop
		           	MEM[SP] = IP; // 1 clop
		           	IP = (int) IR.getOperand(); // 2 clops
					nClops +=4;
		           	break;
		
		        case CALLS:	 // call the function whose whose code starts at the address
		        			 // stored on top of the stack  
							 //  which is afterward removed from the stack
							// the frame of the called function is started on the stack
							// by storing the calling function return address
		        		if ( MEM[SP] != FuncT ) // 2 clops
		        			throw new Exc_Exec(ErrorType.CALLS_NOT_SUPPORTED," for type "+typeName((int)MEM[SP]));
		           	WRI1 = (int)MEM[SP-1];// 2 clops
		           	SP--; // 1 clop
		           	MEM[SP] = IP; // 1 clop
		           	IP = WRI1; // 1 clop
					nClops +=7;
		           	break;	
		           	
		        case BUILDS: // builds a run time function calling the lambda function 
		        				// whose code starts at the address stored on top of the stack
		        				// The frame top is then replaced by the starting code address of the built function
	        			if ( MEM[SP] != IntT ) // 2 clops
	        				throw new Exc_Exec(ErrorType.BUILDS_NOT_SUPPORTED," for type "+typeName((int)MEM[SP])+" - expected 'int'");
		        		SP--; // 1 clop
	        			WRI1 = (int) MEM[SP]; // 1 clop
		        		SP--; // 1 clop
		        		if ( MEM[SP] != FuncT ) // 2 clops
	        				throw new Exc_Exec(ErrorType.BUILDS_NOT_SUPPORTED," for type "+typeName((int)MEM[SP])+" - expected 'function'");
		        		WRI2 = (int) IR.getOperand(); // 2 clops
		        		WRI3 = CODEN; // 1 clop
		        		WRD1 = MEM[SP-1]; // 2 clop
		        		callBuilds();
		        		MEM[SP-1]=WRI3; // 2 clops
		        		nClops +=14;
		        		break;
		        		
		        case DUPL: // duplicate the stack head
		        	if (SP +1 >= HP ) // 2 clops
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"DUPL "+ov_MEM);
		        	SP++; // 1 clop
		        	MEM[SP] = MEM[SP-2]; // 3 clops
		        	SP++; // 1 clop
		        	MEM[SP] = MEM[SP-2]; // 3 clops
					nClops +=9;
		        	break;
		
		        case POP: // remove the stack head
		        	SP-=2; // 2 clops
					nClops +=2;
		        	break;		

		        case PUSHD: // push a double on top of the stack
		        	if (SP +1 >= HP ) // 2 clops
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"PUSHD "+ov_MEM);
		        	SP++; // 1 clop
		        	MEM[SP] = IR.getOperand(); // 2 clops
		        	SP++; // 1 clop
		        	MEM[SP] = DoubleT; // 1 clop
		        	// in actual implementation, 1 additional clop for an additional IP++
		        	// as PUSHD takes two 64 bit words
					nClops +=7;
		        	break;
		
		        case PUSHL: // push a long integer on top of the stack
		        	if (SP +1 >= HP ) // 2 clops
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"PUSHL "+ov_MEM);
		        	SP++; // 1 clop
		        	MEM[SP] = IR.getOperand(); // 2 clops
		        	SP++; // 1 clop
		        	MEM[SP] = LongT; // 1 clop
					nClops +=7;
		        	break;

		        case PUSHI: // push an integer on top of the stack
		        	if (SP +1 >= HP ) // 2 clops
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"PUSHI "+ov_MEM);
		        	SP++; // 1 clop
		        	MEM[SP] = IR.getOperand(); // 2 clops
		        	SP++; // 1 clop
		        	MEM[SP] = IntT; // 1 clop
					nClops +=7;
		        	break;

		        case PUSHC: // push a character on top of the stack
		        	if (SP +1 >= HP ) // 2 clops
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"PUSHC "+ov_MEM);
		        	SP++; // 1 clop
		        	MEM[SP] = IR.getOperand(); // 2 clops
		        	SP++; // 1 clop
		        	MEM[SP] = CharT; // 1 clop
					nClops +=7;
		        	break;
		        	
		        case PUSHB: // push a bool value on top of the stack
		        	if (SP +1 >= HP ) // 2 clops
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"PUSHB "+ov_MEM);
		        	SP++; // 1 clop
		        	if ( IR.getOperand() == 0 ) // 1 clop
		        		MEM[SP] = 0; // 1 clop
		        	else
		        		MEM[SP]=1; // 1 clop
		        	SP++; // 1 clop
		        	MEM[SP] = BoolT; // 1 clop
					nClops +=7;
		        	break;
		        	
		        case PUSHF: // push a function address on top of the stack
		        	if (SP +1 >= HP ) // 2 clops
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"PUSHF "+ov_MEM);
		        	SP++; // 1 clop
		        	MEM[SP] = IR.getOperand(); // 2 clops
		        	SP++; // 1 clop
		        	MEM[SP] = FuncT; // 1 clop
				nClops +=7;
		        	break;
		        	
		        case PUSHV: // push an untyped value on top of the stack
		        	if (SP >= HP ) // 2 clops
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"PUSHF "+ov_MEM);
		        	SP++; // 1 clop
		        	MEM[SP] = IR.getOperand(); // 2 clops
				nClops +=5;
		        	break;
		        	
		        case PUSHT: // push a listable type on top of the stack
		        	if (SP +1 >= HP ) // 2 clops
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"PUSHT "+ov_MEM);
		        	WRI1 = (int)IR.getOperand(); // 1 clop
		        	if ( WRI1 < DoubleT || WRI1 >= TypeT   ) // 2 clops
		        		throw new Exc_Exec(ErrorType.PUSHT_NOT_SUPPORTED," for type '"+typeName((int)MEM[SP])
	        				+"'");
		        	SP++; // 1 clop
		        	MEM[SP] = WRI1; // 2 clops
		        	SP++; // 1 clop
		        	MEM[SP] = TypeT; // 1 clop
					nClops +=10;
		        	break;
		        	
		        case PUSHMT: // push the metatype value "type" on top of the stack
		        	if (SP +1 >= HP ) // 2 clops
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"PUSHMT "+ov_MEM);
		        	SP++; // 1 clop
		        	MEM[SP] = TypeT; // 1 clop
		        	SP++; // 1 clop
		        	MEM[SP] = MetaTypeT; // 1 clop
					nClops +=6;
		        	break;
		        	
		        case PUSHN: // push the null value on top of the stack
		        	if (SP +1 >= HP ) // 2 clops
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"PUSHN "+ov_MEM);
		        	SP++; // 1 clop
		        	MEM[SP] = 0; // 1 clop
		        	SP++; // 1 clop
		        	MEM[SP] = NullT; // 1 clop
					nClops +=6;
		        	break;
		        	
		        case PUSHARG: // copy the k actual arguments of the current function unit 
	        		// on top of the stack
	        		// where k is the operand of the command
		        	WRI1= (int) IR.getOperand()*2; // 2 clops
		        	if (SP +WRI1 >= HP ) // 2 clops
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"PUSHARG "+ov_MEM);
		        	WRI1 = FP-WRI1; // 2 clops
		        	nClops += 6;  
		        	while ( WRI1 < FP ) { // 1 clop
		        		SP++; // 1 clop
		        		MEM[SP]= MEM[WRI1]; // 2 clops
		        		WRI1++; // 1 clop
		        		nClops += 5; // including while check
		        	}
				nClops++; // last while check	
		        	break;


		        case NULLIFY: // 1 integer instruction operand op: 0 or 1
		        	// replace the stack operand with a null value if op = 0 or a deleted null value if op = 1
		        	// in both cases, if the operand is a list or a json, remove its elements
		        	// (including the field values for a json) by adding them into the garbage list
		        	callNullify();
		        	WRI1 = (int) IR.getOperand(); // 1 clop
		        	if ( WRI1 == 0 ) // 1 clop
		        		MEM[SP-1]=nullValue; // 2 clops
		        	else
		        		MEM[SP-1]=deletedNullValue; // 2 clops
		        	MEM[SP]=NullT; // 1 clop
		        	nClops+=5;
		        	break;

		        case GETF: // get the head of FFPP list
		        	if ( FFPP == nullValue ) // 1 clop
		            		throw new Exc_Exec(ErrorType.EMPTY_LIST," - unexpected error: head element of FFPP list is missing");
		        	if (SP +1 >= HP ) // 2 clops
		        		    throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"GETF ");
		        	SP++; // 1 clop
		        	MEM[SP] = MEM[FFPP]; // 1 clop
		        	SP++; // 1 clop
		        	MEM[SP] = FuncT; // 1 clop
		        	WRI2 = FFPP; // 1 clop
		        	FFPP = (int) MEM[WRI2-2]; // 2 clops
		        	nClops +=10;
		        	callDelHeapTriplet();
		        	break;
		        	
		        case STOREF: // store a function pointer as the head of FFPP list
		        	callGetHeapTriplet("STOREF ");		
		            MEM[HPC-1] = MEM[SP]; // 3 clops
		            SP--; // 1 clop
		            MEM[HPC] = MEM[SP]; // 2 clops
		            SP--; // 1 clop
		        	if ( FFPP != nullValue ) { // 1 clop
		        		MEM[HPC-2]=FFPP; // 2 clops
		        		nClops +=2;
		        	}
		        	FFPP=HPC; // 1 clop
		        	nClops +=8;
		        	break;
		        	
		        case LOADGV: // push the address of the i-th global variable on top of the stack,
		        			// where i is the operand of the command
		        	if (SP  >= HP) // 1 clop
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"LOADGC "+ov_MEM);
		        	SP++; // 1 clop
		        	MEM[SP] = 2*IR.getOperand(); // 3 clops
		        	if (SP  >= HP) // 1 clop
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"LOADGV "+ov_MEM);
		        	SP++; // 1 clop
		        	MEM[SP] = RefT; // 1 clops
		        	nClops +=8;
		        	break;
		    		
		        case LOADLV: // push the address of the i-th function local variable on top of the stack,
							// where i is the operand of the command
        					// local variables are stored in the order they occur
        					// from the third position of the current function frame on
		        	if (SP  >= HP) // 1 clop
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"LOADLV "+ov_MEM);
		        	SP++; // 1 clop
		        	MEM[SP] = FP+2*(1+IR.getOperand()); // 5 clops
		        	if (SP  >= HP) // 1 clop
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"LOADLV "+ov_MEM);
		        	SP++; // 1 clop
		        	MEM[SP] = RefT; // 1 clops
					nClops +=10;
		        	break;

		        case LOADARG: // push the address of the i-th function argument on top of the stack,
					// where i is the operand of the command
        			// function arguments are stored in the reverse order they occur,
        			// at the end of the previous function frame 
		        	if (SP  >= HP) // 1 clop
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"LOADARG "+ov_MEM);
		        	SP++; // 1 clop
		        	MEM[SP] = FP-2*IR.getOperand(); // 4 clops
		        	if (SP  >= HP) // 1 clop
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"LOADARG "+ov_MEM);
		        	SP++; // 1 clop
		        	MEM[SP] = RefT; // 1 clops
					nClops +=7;
		        	break;

		        case MOVEARG: // move the k actual arguments on top of the stack 
		        		// into the arguments of the current function with tail recursion
		        		// where k is the operand of the command
		        	WRI2= (int) IR.getOperand(); // 1 clop
		        	WRI1 = FP-1; // 1 clop
		        	nClops += 2;  
		        	while ( WRI2 > 0 ) { // 1 clop
		        		MEM[WRI1] = MEM[SP]; // 2 clops
		        		SP--; WRI1--; // 2 clops
		        		MEM[WRI1] = MEM[SP]; // 2 clops
		        		SP--; WRI1--; // 2 clops
		        		WRI2--; // 1 clop 
		        		nClops += 10; // including while check
		        	}
				nClops++; // last while check	
		        	break;

		        case DEREF: // replace a reference value with the referenced value stored in the stack
		        	if ( MEM[SP] != RefT) // 2 clops
		        		throw new Exc_Exec(ErrorType.DEREF_NOT_SUPPORTED," for types "+typeName((int)MEM[SP-2])+" and "+types[(int)MEM[SP]]);
		        	WRI1 = (int)MEM[SP-1]; // 2 clops
		        	MEM[SP-1] = MEM[WRI1]; // 2 clops
		        	MEM[SP] = MEM[WRI1+1]; // 3 clops
					nClops +=9;
		        	break;
		
		        case MODV: // modify a referenced value in the stack
		        	if ( MEM[SP] < DoubleT || MEM[SP] > RefT  ) // 2 clops
		        		throw new Exc_Exec(ErrorType.MODV_NOT_SUPPORTED," - found type '"+typeName((int)MEM[SP-4])+"'");
		        	SP--; SP--;// 2 clops
		        	if ( MEM[SP] != RefT ) // 2 clops
		        		throw new Exc_Exec(ErrorType.REF_EXPECTED," - found type '"+typeName((int)MEM[SP-4])+"'");
		        	WRI1 = (int)MEM[SP-1]; // 2 clops
			        MEM[WRI1]=MEM[SP+1]; // 2 clops
			        MEM[WRI1+1]=MEM[SP+2]; // 4 clops
			        SP--; SP--; // 2 clops
					nClops +=14;
		        	break;

		        case ADD: // add the two values on top of the stack
		        		  // remove them from the stack and store the result on top of it
		        	nClops +=7; // next check
		        	if ( (MEM[SP] >= DoubleT && MEM[SP]<=CharT) && // 3 clops (assuming 2 clops for type checking)
		        			(MEM[SP-2] >= DoubleT && MEM[SP-2]<=CharT ) ) {
		    	        SP-=2; // 1 clop
		    	        nClops+=4; // including next check
		        			if ( MEM[SP+2]>DoubleT ) { // 3 clops
		            			if ( MEM[SP+2]>LongT ) // 3 clops
		            				WRL1=(int)MEM[SP+1]; // 2 clops
		            			else	 {	
		            				WRL1=Double.doubleToRawLongBits(MEM[SP+1]); // 2 clops
		            			}
		            			nClops+=7; // including next check
		    	        		if ( MEM[SP]>DoubleT ) { // 2 clops
		    	        			if ( MEM[SP]>LongT) // 2 clops
		    	        				WRL2=(int)MEM[SP-1]; // 2 clops
		    	        			else {
		    	        				WRL2=Double.doubleToRawLongBits(MEM[SP-1]); // 2 clops
		    	        			}
		    	        			nClops+=4;
		    	        			try {
		    	        				WRL1=Math.addExact(WRL1,WRL2); // 1 clop
		    	        				if ( WRL1 >= minInt && WRL1 <= maxInt ) { // 2 clops
		    	        					MEM[SP-1]= (double) WRL1; // 2 clops
		    	        					MEM[SP]=IntT; // 1 clop
		    	        				}
		    	        				else {
		    	        					MEM[SP-1]= Double.longBitsToDouble(WRL1); // 2 clops
		    	        					MEM[SP]=LongT; // 1 clop
		    	        				}
		    	        				nClops+=6;
		    	        			}
		    	        			catch(ArithmeticException e) {
		    	        				MEM[SP-1]=(double)WRL1+(double)WRL2; // 3 clops
		    	        				MEM[SP]=DoubleT; // 1 clop
		    	        				nClops+=4;
		    	        			}
		    	        		}
		    	        		else { 
		    	        			MEM[SP-1]=MEM[SP-1]+WRL1; // 5 clops
		    	        			MEM[SP]=DoubleT; // 1 clop
		    	        			nClops+=6;
		    	        		}
		    	        }
		    	        else {
		    	        		nClops+=2; // next check
		    	        		if (MEM[SP]==LongT) { // 2 clops
		    	        			WRL1=Double.doubleToRawLongBits(MEM[SP-1]); // 2 clops
		    	        			MEM[SP-1]=MEM[SP+1]+WRL1; // 5 clops
		    	        			nClops+=7;
		    	        		}
		    	        		else {
		    	        			MEM[SP-1]=MEM[SP+1]+MEM[SP-1]; // 6 clops
		    	        			nClops+=6;
		    	        		}
		    	        		MEM[SP]	= DoubleT; // 1 clop
		    	        		nClops++;
		    	        }
		        		break;
		        	}
		        	nClops+=4; // next check
		        	if ( MEM[SP]==ListT && MEM[SP-2]==ListT) { // 4 clops
		        		MEM[SP-2]=MEM[SP-1]; // 4 clops
		        		SP -=2; // 2 clops
		        		nClops +=6;
		        		concatenateList();
		        		SP++; // 1 clop
		        		MEM[SP]=ListT; // 1 clop
		        		nClops += 3; break;
		        	}
		        	nClops+=5; // next check
		        	if ( MEM[SP]==StringT && MEM[SP-2]==StringT) { // 5 clops
		        		MEM[SP-2]=MEM[SP-1]; // 4 clops
		        		SP -=2; // 2 clops
		        		nClops +=6;
		        		appendString("ADD ");
			        	if (SP  >= HP) // 1 clop
			        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"ADD "+ov_MEM);
		        		SP++; // 1 clop
		        		MEM[SP]=StringT; // 1 clop
		        		NSPP=nullValue; // 1 clop
		        		nClops += 4; break;
		        	}	
		        	nClops+=5; // next check
		        	if ( MEM[SP]==CharT && MEM[SP-2]==StringT) { // 5 clops
		        		MEM[SP-2]=MEM[SP-1]; // 4 clops
		        		SP -=2; // 2 clops
		        		nClops +=6;
		        		appendCharToString("ADD ");
			        	if (SP  >= HP) // 1 clop
			        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"ADD "+ov_MEM);
		        		SP++; // 1 clop
		        		MEM[SP]=StringT; // 1 clop
		        		NSPP=nullValue; // 1 clop
		        		nClops += 4; break;
		        	}			        	
	        		throw new Exc_Exec(ErrorType.ADD_NOT_SUPPORTED," for types '"+
		        		types[(int)MEM[SP-2]]+"' and '"+typeName((int)MEM[SP])+"'");
		
		        case SUB: // subtract the two values on top of the stack
	        		  	  // remove them from the stack and store the result on top of it
		        	nClops +=7; // next check
		        	if ( (MEM[SP] >= DoubleT && MEM[SP]<=CharT) && // 3 clops (assuming 2 clops for type checking)
		        			(MEM[SP-2] >= DoubleT && MEM[SP-2]<=CharT ) ) { // 4 clops (assuming 2 clops for type checking)
		    	        SP-=2; // 1 clop
		    	        nClops+=4; // including next check
		        			if ( MEM[SP+2]>DoubleT ) { // 3 clops
		            			if ( MEM[SP+2]>LongT ) // 3 clops
		            				WRL1=(int)MEM[SP+1]; // 2 clops
		            			else	 {	
		            				WRL1=Double.doubleToRawLongBits(MEM[SP+1]); // 2 clops
		            			}
		            			nClops+=7; // including next check
		    	        		if ( MEM[SP]>DoubleT ) { // 2 clops
		    	        			if ( MEM[SP]>LongT) // 2 clops
		    	        				WRL2=(int)MEM[SP-1]; // 2 clops
		    	        			else {
		    	        				WRL2=Double.doubleToRawLongBits(MEM[SP-1]); // 2 clops
		    	        			}
		    	        			nClops+=4;
		    	        			try {
		    	        				WRL1=Math.subtractExact(WRL2,WRL1); // 1 clop
		    	        				if ( WRL1 >= minInt && WRL1 <= maxInt ) { // 2 clops
		    	        					MEM[SP-1]= (double) WRL1; // 2 clops
		    	        					MEM[SP]=IntT; // 1 clop
		    	        				}
		    	        				else {
		    	        					MEM[SP-1]= Double.longBitsToDouble(WRL1); // 2 clops
		    	        					MEM[SP]=LongT; // 1 clop
		    	        				}
		    	        				nClops+=6;
		    	        			}
		    	        			catch(ArithmeticException e) {
		    	        				MEM[SP-1]=(double)WRL2-(double)WRL1; // 3 clops
		    	        				MEM[SP]=DoubleT; // 1 clop
		    	        				nClops+=4;
		    	        			}
		    	        		}
		    	        		else { 
		    	        			MEM[SP-1]=MEM[SP-1]-WRL1; // 5 clops
		    	        			MEM[SP]=DoubleT; // 1 clop
		    	        			nClops+=6;
		    	        		}
		    	        }
		    	        else {
		    	        		nClops+=2; // next check
		    	        		if (MEM[SP]==LongT) { // 2 clops
		    	        			WRL1=Double.doubleToRawLongBits(MEM[SP-1]); // 2 clops
		    	        			MEM[SP-1]=MEM[SP-1]-WRL1; // 5 clops
		    	        			nClops+=7;
		    	        		}
		    	        		else {
		    	        			MEM[SP-1]=MEM[SP-1]-MEM[SP+1]; // 6 clops
		    	        			nClops+=6;
		    	        		}
		    	        		MEM[SP]	= DoubleT; // 1 clop
		    	        		nClops++;
		    	        } 
		        		break;
		        	}
	        		throw new Exc_Exec(ErrorType.SUB_NOT_SUPPORTED," for types '"+
			        		types[(int)MEM[SP-2]]+"' and '"+typeName((int)MEM[SP])+"'");
		
		        case MULT: // multiply the two values on top of the stack
      		  	  		  // remove them from the stack and store the result on top of it
		        	nClops +=7; // next check
		        	if ( (MEM[SP] >= DoubleT && MEM[SP]<=CharT) && // 3 clops (assuming 2 clops for type checking)
		        			(MEM[SP-2] >= DoubleT && MEM[SP-2]<=CharT ) ) { // 4 clops (assuming 2 clops for type checking)
		    	        SP-=2; // 1 clop
		    	        nClops+=4; // including next check
		        			if ( MEM[SP+2]>DoubleT ) { // 3 clops
		            			if ( MEM[SP+2]>LongT ) // 3 clops
		            				WRL1=(int)MEM[SP+1]; // 2 clops
		            			else	 {	
		            				WRL1=Double.doubleToRawLongBits(MEM[SP+1]); // 2 clops
		            			}
		            			nClops+=7; // including next check
		    	        		if ( MEM[SP]>DoubleT ) { // 2 clops
		    	        			if ( MEM[SP]>LongT) // 2 clops
		    	        				WRL2=(int)MEM[SP-1]; // 2 clops
		    	        			else {
		    	        				WRL2=Double.doubleToRawLongBits(MEM[SP-1]); // 2 clops
		    	        			}
		    	        			nClops+=4;
		    	        			try {
		    	        				WRL1=Math.multiplyExact(WRL2,WRL1); // 1 clop
		    	        				if ( WRL1 >= minInt && WRL1 <= maxInt ) { // 2 clops
		    	        					MEM[SP-1]= (double) WRL1; // 2 clops
		    	        					MEM[SP]=IntT; // 1 clop
		    	        				}
		    	        				else {
		    	        					MEM[SP-1]= Double.longBitsToDouble(WRL1); // 2 clops
		    	        					MEM[SP]=LongT; // 1 clop
		    	        				}
		    	        				nClops+=6;
		    	        			}
		    	        			catch(ArithmeticException e) {
		    	        				MEM[SP-1]=(double)WRL2*(double)WRL1; // 3 clops
		    	        				MEM[SP]=DoubleT; // 1 clop
		    	        				nClops+=4;
		    	        			}
		    	        		}
		    	        		else { 
		    	        			MEM[SP-1]=MEM[SP-1]*WRL1; // 5 clops
		    	        			MEM[SP]=DoubleT; // 1 clop
		    	        			nClops+=6;
		    	        		}
		    	        }
		    	        else {
		    	        		nClops+=2; // next check
		    	        		if (MEM[SP]==LongT) { // 2 clops
		    	        			WRL1=Double.doubleToRawLongBits(MEM[SP-1]); // 2 clops
		    	        			MEM[SP-1]=MEM[SP-1]*WRL1; // 5 clops
		    	        			nClops+=7;
		    	        		}
		    	        		else {
		    	        			MEM[SP-1]=MEM[SP-1]*MEM[SP+1]; // 6 clops
		    	        			nClops+=6;
		    	        		}
		    	        		MEM[SP]	= DoubleT; // 1 clop
		    	        		nClops++;
		    	        } 
		        		break;		        	
		        	}
	        		throw new Exc_Exec(ErrorType.MULT_NOT_SUPPORTED," for types '"+
			        		types[(int)MEM[SP-2]]+"' and '"+typeName((int)MEM[SP])+"'");
		
		        case DIV: // divide the two values on top of the stack
		  	  		  // remove them from the stack and store the result on top of it
		        	nClops +=7; // next check
		        	if ( (MEM[SP] >= DoubleT && MEM[SP]<=CharT) && // 3 clops (assuming 2 clops for type checking)
		        			(MEM[SP-2] >= DoubleT && MEM[SP-2]<=CharT ) ) { // 4 clops (assuming 2 clops for type checking)
		        		SP-=2; // 1 clop
		        		WRD1= (MEM[SP+2] == LongT)? Double.doubleToRawLongBits(MEM[SP+1]):
		        								MEM[SP+1]; // 5 clops
		        		WRD2= (MEM[SP] == LongT)? Double.doubleToRawLongBits(MEM[SP-1]):
							MEM[SP-1]; // 5 clops
		        		if ( WRD1 == 0 ) // 2 clops
			          		throw new Exc_Exec(ErrorType.ZERO_DIVIDE);		        		
				    MEM[SP-1] = WRD2 / WRD1; // 3 clops
					MEM[SP] = DoubleT; // 1 clop
				    nClops +=16; 
					break;
		        	}
	        		throw new Exc_Exec(ErrorType.DIV_NOT_SUPPORTED," for types '"+
			        		types[(int)MEM[SP-2]]+"' and '"+typeName((int)MEM[SP])+"'");
		
		        case NEG: // change the sign to the typed value at the top of the stack
		        	nClops+=2; // next check
		        	if ( MEM[SP] > CharT   ) // 2 clops
		        		throw new Exc_Exec(ErrorType.NEG_NOT_SUPPORTED," for type '"+typeName((int)MEM[SP])
	        				+"'");
		        	nClops+=2; // next check
		        if ( MEM[SP] != LongT) { // 2 clops
			        	MEM[SP-1]=-MEM[SP-1]; // 4 clops
			        	nClops+=6; // including next check
			        	if ( MEM[SP] == CharT ) {// 2 clops
			        		MEM[SP]=IntT; // 1 clop
			        		nClops++;
			        	}
			        	else {
			        		nClops+=5; // next check
			        		if ( MEM[SP] == IntT && MEM[SP-1]==minInt ) { // 5 clops
			        			WRL1=-(long) minInt; // 2 clops
			        			MEM[SP-1]=Double.longBitsToDouble(WRL1); // 2 clops
			        			MEM[SP]=LongT; // 1 clop
			        			nClops+=5;
			        		}
			        	}
		        }
		        else {
		        		WRL1=Double.doubleToRawLongBits(MEM[SP-1]); // 1 clop
		        		nClops+=2; // including next check
		        		if ( WRL1 == minLong ) { // 1 clop
		        			MEM[SP-1]=-(double) WRL1; // 3 clops
		        			MEM[SP]=DoubleT; // 1 clop
		        			nClops+=4;
		        		}
		        		else {
		        			MEM[SP-1]=Double.longBitsToDouble(-WRL1); // 2 clops
		        			nClops+=2;
		        		}
		        }
		        	break;
		        	
		        case TODOUBLE: // convert the value on top of the stack into a double
		  	  		  // remove it from the stack and store the result on top of it
		        	if ( MEM[SP] < DoubleT || (MEM[SP] > CharT ) ) // 4 clops
		        		throw new Exc_Exec(ErrorType.TOLONG_NOT_SUPPORTED," for type '"+typeName((int)MEM[SP])+"'");
			    nClops+=6; // including next check
		        	if ( MEM[SP]!= DoubleT ) { // 2 clops
			        	WRI1=SP-1; // 1 clop
			        	nClops+=3; // including next check
			        	if ( MEM[SP] == LongT ) { // 2 clops
			        		WRL1= Double.doubleToRawLongBits(MEM[WRI1]); // 2 clops
			        		MEM[WRI1]=WRL1; // 1 clop
			        		nClops+=3;
			        	}
			        	MEM[SP] = DoubleT; // 1 clop
					nClops ++;
		        	}
		        	break;
		
		        case TOINTL: // convert the double value on top of the stack into a long or int
		        	if ( MEM[SP] != DoubleT )  // 2 clops
		        		throw new Exc_Exec(ErrorType.TOINTL_NOT_SUPPORTED," for type '"+typeName((int)MEM[SP])+"' -- double expected");
			    WRI1=SP-1; // 1 clop
			    nClops+=6; // including next check
			    if ( MEM[WRI1]>=minInt && MEM[WRI1] <= maxInt ) { // 3 clops
		        		MEM[SP] = IntT; // 1 clop
		        		nClops ++;		    
			    }
			    else 
			        	if ( MEM[WRI1]<minLong || MEM[WRI1] >maxLong ) // 3 clops
			        		throw new Exc_Exec(ErrorType.TOINTL_NOT_SUPPORTED," for value "+MEM[WRI1]);
			        	else {
			        		MEM[WRI1]=Double.longBitsToDouble((long)MEM[WRI1]); // 2 clops
				        	MEM[SP] = LongT; // 1 clop
			        	}
					nClops +=6;
		        	break;
		
		        case TOLONG: // convert the value on top of the stack into a long integer
		  	  		  // remove it from the stack and store the result on top of it
		        	if ( MEM[SP] < DoubleT || (MEM[SP] > CharT ) ) // 4 clops
		        		throw new Exc_Exec(ErrorType.TOLONG_NOT_SUPPORTED," for type '"+typeName((int)MEM[SP])+"'");
			    nClops+=6; // including next check
		        	if ( MEM[SP]!= LongT ) { // 2 clops
			        	WRI1=SP-1; // 1 clop
			        	if ( MEM[WRI1]<minLong || MEM[WRI1] >maxLong ) // 3 clops
			        		throw new Exc_Exec(ErrorType.TOLONG_NOT_SUPPORTED," for value "+MEM[WRI1]);
			        	MEM[WRI1]=Double.longBitsToDouble((long)MEM[WRI1]); // 2 clops
			        	MEM[SP] = LongT; // 1 clop
					nClops +=7;
		        	}
		        	break;
		
		        case TOINT: // convert the value on top of the stack into an integer
		  	  		  // remove it from the stack and store the result on top of it
		        	if ( MEM[SP] < DoubleT || (MEM[SP] > CharT && MEM[SP]!=BoolT) ) // 6 clops
		        		throw new Exc_Exec(ErrorType.TOINT_NOT_SUPPORTED," for type '"+typeName((int)MEM[SP])+"'");
		        nClops+=8; // including next check
		        	if ( MEM[SP]!= IntT ) { // 2 clops
			        	WRI1=SP-1; // 1 clop
			        nClops+=7; // including next check
			        	if ( MEM[SP]==LongT ) { // 2 clops
			        		WRL1 = Double.doubleToRawLongBits(MEM[WRI1]); // 1 clop
				        	if ( WRL1<minInt || WRL1 >maxInt ) // 2 clops
				        		throw new Exc_Exec(ErrorType.TOINT_NOT_SUPPORTED," for value "+WRL1);
				        	MEM[WRI1] = (int) WRL1;// 1 clop
				        	nClops+=4;
			        	}
			        	else {
				        	if ( MEM[WRI1]<minInt || MEM[WRI1] >maxInt ) // 2 clops
				        		throw new Exc_Exec(ErrorType.TOINT_NOT_SUPPORTED," for value "+MEM[WRI1]);
				        	MEM[WRI1] = (int) MEM[WRI1];// 2 clops 
				        	nClops+=4;
			        	}
			        	MEM[SP] = IntT; // 1 clop
					nClops++;
		        }
		        	break;
		
		        case TOCHAR:// convert the value on top of the stack into a character
		  	  		  // remove it from the stack and store the result on top of it
		        	if ( MEM[SP] < DoubleT || MEM[SP] > CharT) // 3 clops
		        		throw new Exc_Exec(ErrorType.TOCHAR_NOT_SUPPORTED," for type '"+typeName((int)MEM[SP])+"'");
			        nClops+=6; // including next check
		        	if ( MEM[SP]!= CharT ) { // 2 clops
			        	WRI1=SP-1; // 1 clop
			        nClops+=7; // including next check
			        	if ( MEM[SP]==LongT ) { // 2 clops
			        		WRL1 = Double.doubleToRawLongBits(MEM[WRI1]); // 1 clop
				        	if ( WRL1<0 || WRL1 >maxChar ) // 2 clops
				        		throw new Exc_Exec(ErrorType.TOCHAR_NOT_SUPPORTED," for value "+WRL1);
				        	MEM[WRI1] = (int) WRL1;// 1 clop
				        	nClops+=4;
			        	}
			        	else {
				        	if ( MEM[WRI1]<0 || MEM[WRI1] >maxChar ) // 2 clops
				        		throw new Exc_Exec(ErrorType.TOCHAR_NOT_SUPPORTED," for value "+MEM[WRI1]);
				        	MEM[WRI1] = (int) MEM[WRI1];// 2 clops 
				        	nClops+=4;
			        	}
			        	MEM[SP] = CharT; // 1 clop
					nClops++;
		        }
		        	break;

		        case TOTYPE: // convert the value on top of the stack into a type
		  	  		  // remove it from the stack and store the result on top of it 
		        	if ( MEM[SP] < DoubleT || MEM[SP]>=MetaTypeT )  // 3 clops (assuming 1 clop for storing MEM[SP])
		        		throw new Exc_Exec(ErrorType.TOTYPE_NOT_SUPPORTED," for type code '"+typeName((int)MEM[SP])+"'");
		        	MEM[SP-1] = (int) MEM[SP];// 3 clops 
		        	if ( MEM[SP] == TypeT ) // 2 clops
		        		MEM[SP] = MetaTypeT; // 1 clop
		        	else
		        		MEM[SP] = TypeT; // 1 clop
					nClops +=9;
		        	break;
		
		        case TOLIST: // convert the string on top of the stack into a list
		  	  		  // remove it from the stack and store the result on top of it 
		        	if ( MEM[SP] != StringT && MEM[SP] != ListT && MEM[SP] != JsonT) // 3 clops
		        		throw new Exc_Exec(ErrorType.TOLIST_NOT_SUPPORTED," for type '"+typeName((int)MEM[SP])+"'");
		        	nClops+=5; // including next check
		        	if ( MEM[SP] == ListT ) // 2 clops
		        		break;
		        	nClops+=2; // next check
		        	if ( MEM[SP] == JsonT ) { // 2 clops
		        		callJSTOLIST("TOLIST ");
		        		break;
		        	}
		        	WRI1 = (int) MEM[SP-1]; // 2 clops
		        	WRI2=(int) MEM[WRI1]; // 1 clop
		        	MEM[SP]=ListT; // 2 clops including next check  
		        	nClops+=5;
		        	if ( WRI2== 0 ) {
		        		MEM[SP-1]=nullValue; // 2 clops
		        		nClops+=2;
		        	}
		        	else { 
		        		callGetHeapTriplet("TOLIST ");
		        		MEM[SP-1]=HPC; // 2 clops
		        		nClops+= 3; // including last while
		        		while ( WRI2 > 0 ) { // 1 clop 
				        	WRI1--; // 1 clop
				        	MEM[HPC]=MEM[WRI1]; // 2 clops
				        	MEM[HPC-1]=CharT; // 2 clops
				        	WRI3=HPC-2; // 2 clops
				        	WRI2--; // 1 clop
				        	if ( WRI2 >0 ) { // 1 clop
				        		callGetHeapTriplet("TOLIST ");
					        	MEM[WRI3]=HPC; // 1 clop
					        	nClops++;
				        	}
				        	else {
				        		MEM[WRI3]=nullValue; // 1 clop
				        		nClops++;
				        	}			        		
				        	nClops+=10;
		        		}
		        		MEM[HP+1]=nullValue; // 2 clops 
		        		nClops+=2;
		        	}
		        	break;

		        case TOJSON: // convert the value on top of the stack into a JSON
		  	  		  // remove it from the stack and store the result on top of it 
		        	if ( MEM[SP] != ListT && MEM[SP] != JsonT) // 3 clops
		        		throw new Exc_Exec(ErrorType.TOJSON_NOT_SUPPORTED," for type '"+typeName((int)MEM[SP])+"'");
		        	if ( MEM[SP] == JsonT ) // 2 clops
		        		break;
		    		WRI4 =  (int) MEM[SP-1]; // 1 clop
		    		callSkipDeletedNulls();
		        	nClops+=6; 
		    		callSJSON("TOJSON ");
		    		while ( WRI4 != nullValue ) { // 1 clop
		    			if ( SP +3 >= HP ) // 2 clops
		    				throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"TOJSON "+ov_MEM);
		    			if ( MEM[WRI4-1] != ListT ) // 3 clops
			        		throw new Exc_Exec(ErrorType.TOJSON_NOT_SUPPORTED," for list element type '"+typeName((int)MEM[SP])+"'"+
			        				" - a list element must be a two element list (Key, value)");
		    			WRI2 = (int) MEM[WRI4]; // address of the list (key, value) - 1 clop
		    			SP++; // 1 clop
		    			MEM[SP]=MEM[WRI2]; // 2 clops
		    			if ( MEM[WRI2-1]!= StringT ) // 3 clops
			        		throw new Exc_Exec(ErrorType.TOJSON_NOT_SUPPORTED," for key type '"+typeName((int)MEM[SP])+"'"+
			        				" - it must be a string");
		    			SP++; // 1 clop
		    			MEM[SP]=StringT; // 1 clop
		    			WRI2 = (int) MEM[WRI2-2]; // 2 clops
		    			SP++; // 1 clop
		    			MEM[SP]=MEM[WRI2]; // 2 clops
		    			SP++; // 1 clop
		    			MEM[SP]=MEM[WRI2-1]; // 3 clops
		    			nClops+=22;
		    			callNEWFLD("TOJSON ");
		    			WRI3=WRI4; // 1 clop
		    			callCJSON("TOJSON ");
		    			WRI4 = (int)MEM[WRI3-2]; // 2 clops
			    		callSkipDeletedNulls();
		    			nClops+=3;
		    		}
		    		nClops++; // last while check
		    		MEM[SP]=JsonT; // 1 clop
		    		nClops++;
		        	break;

		        case TOSTRING: // convert the value on top of the stack into a string
		  	  		  // remove it from the stack and store the result on top of it 
		        	if ( MEM[SP] != CharT && MEM[SP] != ListT && MEM[SP] != StringT) // 4 clops
		        		throw new Exc_Exec(ErrorType.TOSTRING_NOT_SUPPORTED," for type '"+typeName((int)MEM[SP])+"'");
		        	nClops+=6; // including next check
		        	if ( MEM[SP] == CharT ) { // 2clops
		    	    	if ( SP +1 >= HP   ) // 2 clops
		            		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"TOSTRING "+ov_MEM);
		    			NSPP=HP; HP--; // 2 clops
		    			MEM[HP]= MEM[SP-1]; // 3 clops
		    			HP--; // 1clop
		    			MEM[SP] = StringT; // 1 clop
		    			nClops +=9;
		    			endAddString("TOSTRING ");   	    	
			        	if (SP  >= HP) // 1 clop
			        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"TOSTRING "+ov_MEM);
		        		SP++; MEM[SP]=StringT; // 2 clops
		        		nClops+=3;
		        	}
		        	else {
		        		nClops+=2; // next check
		        		if ( MEM[SP] == ListT ) { // 2 clops
			    	    	if ( SP  >= HP   ) // 1 clop
			            		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"TOSTRING "+ov_MEM);
			    			NSPP=HP; HP--; // 2 clops
			    			WRI4= (int) MEM[SP-1]; // 2 clops
				    		callSkipDeletedNulls();
		    	        	nClops+=6; // including next check
		    	    		if ( WRI4 != nullValue ) // 1 clop
		    	    			do {
		    			        	if ( MEM[WRI4-1] != CharT) // 3 clops
		    			        		throw new Exc_Exec(ErrorType.TOSTRING_NOT_SUPPORTED,
		    			        				" for list element with type '"+typeName((int)MEM[WRI1-1])+"'");
					    	    	if ( SP  >= HP   ) // 1 clop
					            		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"TOSTRING "+ov_MEM);
		    		    			MEM[HP]= MEM[WRI4]; // 2 clops
		    		    			HP--; // 1 clop		    	    				
		    	    				WRI4 = (int) MEM[WRI4-2]; // 2 clops
		    			    		callSkipDeletedNulls();
		    	    				nClops +=10; // including while check
		    	    			} while (WRI4!=nullValue); // 1 clop
			    	    	endAddString("TOSTRING ");   	    	
				        	if (SP  >= HP) // 1 clop
				        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"TOSTRING "+ov_MEM);
			        		SP++; MEM[SP]=StringT; // 2 clops
			        		nClops+=3;
		        		}
		        	}
		        	break;
		        	
		        case TUPLE:
		        	if ( MEM[SP] != JsonT && MEM[SP] != LongT) // 4 clops
		        		throw new Exc_Exec(ErrorType.TOTUPLE_NOT_SUPPORTED," for type '"+typeName((int)MEM[SP])+"'");
		        	nClops+=6; // including next check
		        	if ( MEM[SP] == LongT )  { // 2 clops
		        		WRL1= Double.doubleToRawLongBits(MEM[SP-1]); // 2 clops
		        		Calendar cal=Calendar.getInstance(); cal.setTimeInMillis(WRL1); // 50 clops?
		        		MEM[SP-1] = nullValue; // 2 clops
			        	MEM[SP] = nullValue; // 1 clop
		        		nClops+=55;
			        	SP++; // 1 clop
			        	MEM[SP]=cal.get(Calendar.YEAR); // 20 clops
			        	SP++; // 1 clop
			        	MEM[SP]=IntT; // 1 clop
		        		callHLIST("TUPLE "); 
		        		nClops+=23;
			        	SP++; // 1 clop
			        	MEM[SP]=cal.get(Calendar.MONTH)+1; // 20 clops
			        	SP++; // 1 clop
			        	MEM[SP]=IntT; // 1 clop
		        		callCLIST("TUPLE "); 
		        		nClops+=23;
			        	SP++; // 1 clop
			        	MEM[SP]=cal.get(Calendar.DAY_OF_MONTH); // 20 clops
			        	SP++; // 1 clop
			        	MEM[SP]=IntT; // 1 clop
		        		callCLIST("TUPLE "); 
		        		nClops+=23;
			        	SP++; // 1 clop
			        	MEM[SP]=cal.get(Calendar.HOUR_OF_DAY); // 20 clops
			        	SP++; // 1 clop
			        	MEM[SP]=IntT; // 1 clop
		        		callCLIST("TUPLE "); 
		        		nClops+=23;
			        	SP++; // 1 clop
			        	MEM[SP]=cal.get(Calendar.MINUTE); // 20 clops
			        	SP++; // 1 clop
			        	MEM[SP]=IntT; // 1 clop
		        		callCLIST("TUPLE "); 
		        		nClops+=23;
			        	SP++; // 1 clop
			        	MEM[SP]=cal.get(Calendar.SECOND); // 20 clops
			        	SP++; // 1 clop
			        	MEM[SP]=IntT; // 1 clop
		        		callCLIST("TUPLE "); 
		        		nClops+=23;
			        	SP++; // 1 clop
			        	MEM[SP]=cal.get(Calendar.MILLISECOND); // 20 clops
			        	SP++; // 1 clop
			        	MEM[SP]=IntT; // 1 clop
		        		callCLIST("TUPLE "); 
		        		nClops+=23;
			        	SP++; // 1 clop
			        	WRI1=cal.get(Calendar.DAY_OF_WEEK)-1; // 20 clops
			        	MEM[SP]=WRI1<1? 7: WRI1; // 2 clops
			        	SP++; // 1 clop
			        	MEM[SP]=IntT; // 1 clop
		        		callCLIST("TUPLE "); 
		        		nClops+=25;
			        	SP++; // 1 clop
			        	MEM[SP]=cal.get(Calendar.DAY_OF_YEAR); // 20 clops
			        	SP++; // 1 clop
			        	MEM[SP]=IntT; // 1 clop
		        		callCLIST("TUPLE "); 
		        		nClops+=23;
		        	}
		        	else {
			    		WRI1 = (int) MEM[SP-1]; // 1 clop
			    		WRI4 = (int) MEM[WRI1-2]; // skip the initial null field - 2 clops
			    		callSkipDeletedNulls();
			        	MEM[SP-1] = nullValue; // 2 clops
			        	MEM[SP] = nullValue; // 1 clop
			    		nClops +=7; // including next check
			    		if ( WRI4 != nullValue ) { // 1 clop
			    			if ( SP +1 >= HP ) // 2 clops
			    				throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"TUPLE "+ov_MEM);
			    			WRI2= (int) MEM[WRI4]; // 1 clop
			    			SP++; // 1 clop
			    			MEM[SP] = MEM[WRI2]; // 2 clops
			    			SP++; // 1 clop
			    			MEM[SP] = MEM[WRI2-1]; // 2 clops
			       		callHLIST("TUPLE ");
			    			WRI4 = (int) MEM[WRI4-2]; // 2 clops
			    			callSkipDeletedNulls();
			    	        nClops +=2;		
			    		}
			    		while ( WRI4 != nullValue ) { // 1 clop
			    			if ( SP +1 >= HP ) // 2 clops
			    				throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"TUPLE "+ov_MEM);
			    			WRI2= (int) MEM[WRI4]; // 1 clop
			    			SP++; // 1 clop
			    			MEM[SP] = MEM[WRI2]; // 2 clops
			    			SP++; // 1 clop
			    			MEM[SP] = MEM[WRI2-1]; // 2 clops
			      			callCLIST("TUPLE ");
			    			WRI4 = (int) MEM[WRI4-2]; // 2 clops
			    	        nClops +=2;		
			    			callSkipDeletedNulls();
			    	    }
			    		nClops++; // last while check
		        	}
		    		MEM[SP]= ListT; // 2 clops
		    		nClops+=2;
		        	break;

		        case GDATE: // returns the number of milliseconds since January 1, 1970, 00:00:00 GMT 
		        				// represented by a long
		        		if (SP +1 >= HP ) // 2 clops
		        			throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"GDATE "+ov_MEM);
		        		WRI1= (int)IR.getOperand(); // 1 clop 
			        	nClops+=4; // including next check
		        		if ( WRI1 == 0 ) { // 1 clop
				        	SP++; // 1 clop
		        			MEM[SP]=Double.longBitsToDouble((new Date()).getTime()); // 50 clops!
		        			nClops+=51;
		        		}
		        		else {
		        			nClops+=2; // next check
			    			if ( MEM[SP]!= StringT ) // 2 clops
				    			throw new Exc_Exec(ErrorType.STRING_EXPECTED,"GDATE ");			    						        			
			    			WRS1= PrintOutput.getStringHeap((int)MEM[SP-1]);
			    			nClops+=2+WRS1.length()*2; // linear cost for constracting the string
			    			SP-=2; // 2 clops
			    			if ( MEM[SP]!= StringT ) // 2 clops
				    			throw new Exc_Exec(ErrorType.STRING_EXPECTED,"GDATE ");
			    			nClops+=4;
			    			WRS2= PrintOutput.getStringHeap((int)MEM[SP-1]);
			    			nClops+=2+WRS2.length()*2; // linear cost for constracting the string
			    			SP--; // 1 clop
			    			try {
			    				MEM[SP]=Double.longBitsToDouble(((new SimpleDateFormat(WRS1)).parse(WRS2)).getTime()); 			
			    				nClops+=1+WRS1.length()*2+WRS2.length()*2;
			    			}
			    			catch ( IllegalArgumentException e ) {
			    				throw new Exc_Exec(ErrorType.WRONG_DATE,"illegal data pattern ");
			    			}
			    			catch ( NullPointerException  e ) {
			    				throw new Exc_Exec(ErrorType.WRONG_DATE,"null data pattern or data string ");
			    			}
			    			catch ( ParseException  e ) {
			    				throw new Exc_Exec(ErrorType.WRONG_DATE,"null data string ");
			    			}
		        		}
			        	SP++; // 1 clop
			        	MEM[SP] = LongT; // 1 clop
			        	nClops+=2;
		        	break;

		        case PDATE: // returns the date represented by a long
			    		WRI1= (int)IR.getOperand(); // 1 clop 
			    		nClops+=2; // including next check
			    		if ( WRI1 == 0 ) { // 1 clop
			    			nClops+=2; // next check
			    			if ( MEM[SP]!= LongT ) // 2 clops
				    			throw new Exc_Exec(ErrorType.LONG_EXPECTED,"PDATE ");			    				
			    			WRS1 =(new Date(Double.doubleToRawLongBits(MEM[SP-1]))).toString();
			    			nClops+=2+WRS1.length()*2; // linear cost for constractin the string
			    		}
			    		else {
			    			nClops+=2; // next check
			    			if ( MEM[SP]!= StringT ) // 2 clops
				    			throw new Exc_Exec(ErrorType.STRING_EXPECTED,"PDATE ");			    				
			    			WRS2= PrintOutput.getStringHeap((int)MEM[SP-1]);
			    			nClops+=2+WRS2.length()*2; // linear cost for constractin the string
			    			SP-=2; // 2 clops
			    			nClops+=4; // including next check
			    			if ( MEM[SP]!= LongT ) // 2 clops
				    			throw new Exc_Exec(ErrorType.LONG_EXPECTED,"PDATE ");
			    			try {
			    				WRS1=(new SimpleDateFormat(WRS2)).format(new Date(Double.doubleToRawLongBits(MEM[SP-1])));
			    				nClops+=2+WRS2.length()*2+WRS1.length();
			    			}
			    			catch ( IllegalArgumentException e ) {
			    				throw new Exc_Exec(ErrorType.WRONG_DATE,"illegal data pattern ");
			    			}
			    			catch ( NullPointerException  e ) {
			    				throw new Exc_Exec(ErrorType.WRONG_DATE,"null data pattern");
			    			}			    				
			    		}
			    		SP-=2; // 2 clops
			    		nClops+=6; // next check
			    		if (SP +WRS1.length()+3 >= HP ) // 4 clops
			    			throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"PDATE "+ov_MEM);
					NSPP=HP; MEM[HP]=0; // 2 clops
					HP--; // 1 clop
					nClops+=3;
					for ( int i=0; i<WRS1.length(); i++ ) { // 3 clops
	    	    				MEM[NSPP]++; // 3 clops
	    	    				MEM[HP]= WRS1.charAt(i); // 3 clops
	    	    				HP--; // 1 clop
					}
				    SP++; // 1 clop - in this cell the pointer to the string will be stored
				    SP++; // 1 clop - this cell will be next removed by endAddString 
					nClops+=2;
				    endAddString("ESTRING ");
					NSPP=nullValue; // 1 clop
					SP++; // 1 clop
					MEM[SP]=StringT; // 1 clop
					nClops+=3;
			    break;

		        	
		        	
		        case RAND: // a double number between 0 and 1 is randomly generated and 
		        	 // pushed into the stack
		        	if (SP +1 >= HP ) // 2 clops
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"RAND "+ov_MEM);
		        	SP++; // 1 clop
		        	MEM[SP] = random.nextDouble(); // crand clops
		        	SP++; // 1 clop
		        	MEM[SP] = DoubleT; // 1 clop
		        	nClops+=5+crand;
		        	break;

		        case EXP:// compute the natural exponential function of a number 
		        	if ( MEM[SP] < DoubleT || MEM[SP] > CharT ) // 3 clops
		        		throw new Exc_Exec(ErrorType.EXP_NOT_SUPPORTED," for type "+typeName((int)MEM[SP]));
		        	MEM[SP-1] = Math.exp(MEM[SP-1]); // (4+cexp) clops
		        	MEM[SP] = DoubleT; // 1 clop
				nClops +=8+cexp;
		        	break;
		
		        case LOG: // compute the natural logarithm of a number 
		        	if ( MEM[SP] < DoubleT || MEM[SP] > CharT ) // 3 clops
		        		throw new Exc_Exec(ErrorType.LOG_NOT_SUPPORTED," for type "+typeName((int)MEM[SP]));
		        	if ( MEM[SP-1] <= 0 ) // 3 clops
		        		throw new Exc_Exec(ErrorType.LOG_NOT_SUPPORTED," for value "+MEM[SP-1]);
		        	MEM[SP-1] = Math.log(MEM[SP-1]); // (4+clog) clops
		        	MEM[SP] = DoubleT; // 1 clop
					nClops +=11+clog;
		        	break;
		
		        case POW: // compute the power of a number raised to another number  
		        	if ( MEM[SP] < DoubleT || MEM[SP] > CharT || 
		        			MEM[SP-2] < DoubleT || MEM[SP-2] > CharT  ) // 7 clops
		        		throw new Exc_Exec(ErrorType.POW_NOT_SUPPORTED," for types "+typeName((int)MEM[SP-2])+" and "+types[(int)MEM[SP]]);
		        	MEM[SP-3] = Math.pow(MEM[SP-3],MEM[SP-1]); // (6+cpow) clops
		        	if ( MEM[SP] != DoubleT && MEM[SP-2] != DoubleT && MEM[SP-1] >= 0 && MEM[SP-3] <= maxInt ) // 11 clops
		        		MEM[SP-2] = IntT; // 2 clops
		        	else
		        		MEM[SP-2] = DoubleT; // 2 clops
		        	SP -=2; // 2 clops
					nClops +=27+cpow;
		        	break;

		        case ISKEY: // verify wether the json in n-1 position includes a given key 
		        	if ( MEM[SP] != StringT || MEM[SP-2] != JsonT  ) // 5 clops
		        		throw new Exc_Exec(ErrorType.ISKEY_NOT_SUPPORTED," for types "+typeName((int)MEM[SP-2])+" and "+types[(int)MEM[SP]]);
		        	callFLDFIND0();
		        	if ( MEM[SP-1]!=nullValue ) // 2 clops
		        		MEM[SP-1]=1; // 2clops
		        	MEM[SP] = BoolT; // 2 clops
				nClops +=11;
		        	break;

		        case REMAIN: // compute the reminder of two numbers 
		        	if ( MEM[SP] < DoubleT || MEM[SP] > CharT || 
		        			MEM[SP-2] < DoubleT || MEM[SP-2] > CharT  ) // 7 clops
		        		throw new Exc_Exec(ErrorType.REMINDER_NOT_SUPPORTED," for types "+typeName((int)MEM[SP-2])+" and "+types[(int)MEM[SP]]);
		        	MEM[SP-3] = MEM[SP-3] % MEM[SP-1]; // (6+crem) clops
		        	if ( MEM[SP] != DoubleT && MEM[SP-2] != DoubleT ) // 5 clops
		        		MEM[SP-2] = IntT; // 2 clops
		        	else
		        		MEM[SP-2] = DoubleT; // 2 clops
		        	SP -=2; // 2 clops
					nClops +=21+crem;
		        	break;

		        case EQ: // check whether two values are equal 
		        	// the two values must have the same type or must be both numeric
		        	// or one of them must be null (that is therefore comparable with any value)
		        	// types "metatype" and "type" are comparable
		        	SP--; SP--; // 2 clops
		        	if ( MEM[SP] < DoubleT || MEM[SP]>MetaTypeT )  // 3 clops (assuming 1 clop for storing MEM[SP])
		        		throw new Exc_Exec(ErrorType.EQ_NOT_SUPPORTED," for type '"+typeName((int)MEM[SP])+"'");
		        	if ( MEM[SP+2] < DoubleT || MEM[SP+2]>MetaTypeT ) // 4 clops (assuming 2 clops for storing MEM[SP+2])
		        		throw new Exc_Exec(ErrorType.EQ_NOT_SUPPORTED," for type '"+typeName((int)MEM[SP+2])+"'");
		        	nClops+=9; 
		        	callEQ();
		        	MEM[SP-1]=WRI1; // 2 clops
		        	nClops+=2;
		        	break;		

		        case NEQ: // check whether two values are not equal 
		        	// the two values must have the same type or must be both numeric
		        	// or one of them must be null (that is therefore comparable with any value)
		        	// types "metatype" "type" are comparable
		        	SP--; SP--; // 2 clops
		        	if ( MEM[SP] < DoubleT || MEM[SP]>MetaTypeT )  // 3 clops (assuming 1 clop for storing MEM[SP])
		        		throw new Exc_Exec(ErrorType.NEQ_NOT_SUPPORTED," for type '"+typeName((int)MEM[SP])+"'");
		        	if ( MEM[SP+2] < DoubleT || MEM[SP+2]>MetaTypeT ) // 4 clops (assuming 2 clops for storing MEM[SP+2])
		        		throw new Exc_Exec(ErrorType.NEQ_NOT_SUPPORTED," for type '"+typeName((int)MEM[SP+2])+"'");
		        	nClops+=9; // including next check
		        	callEQ();
		        	MEM[SP-1]=1-WRI1; // 3 clops
		        	nClops+=3;
		        	break;		

		        case LT:// check whether the first operand value is less than the second one
		        	// admissible types are: numeric, string and null
		        	// the two values must have the same type or must be both numeric
		        	// or one of them must be null (that is therefore comparable with any value)
		        	SP--; SP--; // 2 clops
		        	nClops+=14; // including next check
	        		if ( MEM[SP]<DoubleT || MEM[SP]>StringT || MEM[SP+2]>StringT || MEM[SP+2]<DoubleT ||  // 7 clops
	        				( (MEM[SP+2]>CharT || MEM[SP]>CharT) && MEM[SP] != NullT && MEM[SP+2] != NullT &&
	        						MEM[SP+2]!= MEM[SP] ) ) // 5 clops
		        		throw new Exc_Exec(ErrorType.LT_NOT_SUPPORTED," for types '"+typeName((int)MEM[SP])
		        				+"' and '"+typeName((int)MEM[SP+2])+"'");
	        		callLT();
	        		MEM[SP-1]=WRI1; // 2 clops
	        		nClops+=2; 
		        	break;	
	
		        case LTE: // check whether the first operand value is less than or equal to the second one
		        	// admissible types are: numeric, string and null
		        	// the two values must have the same type or must be both numeric
		        	// or one of them must be null (that is therefore comparable with any value)
		        	SP--; SP--; // 2 clops
		        	nClops+=14; // including next check
	        		if ( MEM[SP]<DoubleT || MEM[SP]>StringT || MEM[SP+2]>StringT || MEM[SP+2]<DoubleT ||  // 7 clops
	        				( (MEM[SP+2]>CharT || MEM[SP]>CharT) && MEM[SP] != NullT && MEM[SP+2] != NullT &&
	        						MEM[SP+2]!= MEM[SP] ) ) // 5 clops
		        		throw new Exc_Exec(ErrorType.LTE_NOT_SUPPORTED," for types '"+typeName((int)MEM[SP])
		        				+"' and '"+typeName((int)MEM[SP+2])+"'");
	        		callLTE();
	        		MEM[SP-1]=WRI1; // 2 clops
	        		nClops+=2; 
		        	break;	
	
		        case GT: // check whether the first operand value is greater than the second one
		        	// admissible types are: numeric, string and null
		        	// the two values must have the same type or must be both numeric
		        	// or one of them must be null (that is therefore comparable with any value)
		        	SP--; SP--; // 2 clops
		        	nClops+=14; // including next check
	        		if ( MEM[SP]<DoubleT || MEM[SP]>StringT || MEM[SP+2]>StringT || MEM[SP+2]<DoubleT ||  // 7 clops
	        				( (MEM[SP+2]>CharT || MEM[SP]>CharT) && MEM[SP] != NullT && MEM[SP+2] != NullT &&
	        						MEM[SP+2]!= MEM[SP] ) ) // 5 clops
		        		throw new Exc_Exec(ErrorType.GT_NOT_SUPPORTED," for types '"+typeName((int)MEM[SP])
		        				+"' and '"+typeName((int)MEM[SP+2])+"'");
	        		callLTE();
	        		MEM[SP-1]=1-WRI1; // 3 clops
	        		nClops+=3; 
		        	break;	
	
		        case GTE: // check whether the first operand value is greater than or equal to the second one
		        	// admissible types are: numeric, string and null
		        	// the two values must have the same type or must be both numeric
		        	// or one of them must be null (that is therefore comparable with any value)
		        	SP--; SP--; // 2 clops
		        	nClops+=14; // including next check
	        		if ( MEM[SP]<DoubleT || MEM[SP]>StringT || MEM[SP+2]>StringT || MEM[SP+2]<DoubleT ||  // 7 clops
	        				( (MEM[SP+2]>CharT || MEM[SP]>CharT) && MEM[SP] != NullT && MEM[SP+2] != NullT &&
	        						MEM[SP+2]!= MEM[SP] ) ) // 5 clops
		        		throw new Exc_Exec(ErrorType.GTE_NOT_SUPPORTED," for types '"+typeName((int)MEM[SP])
		        				+"' and '"+typeName((int)MEM[SP+2])+"'");
	        		callLT();
	        		MEM[SP-1]=1-WRI1; // 3 clops
	        		nClops+=3; 
		        	break;	
	
		        case NOT:// implement the not boolean operator 
		        	if ( MEM[SP] !=  BoolT   ) // 3 clops
		        		throw new Exc_Exec(ErrorType.NOT_NOT_SUPPORTED," for type '"+typeName((int)MEM[SP])
	        				+"'");
		        	if ( MEM[SP-1]==0 ) // 2 clops
		        		MEM[SP-1]=1; // 2 clops
		        	else
		        		MEM[SP-1]=0; // 2 clops
					nClops +=10;
		        	break;
		        	
		        case JUMP: // unconditional  jump to another instruction
		        	IP = (int) IR.getOperand();
					nClops +=1;
		        	break;
		
		        case JUMPZ: // if the stack operand is false (i.e., equal to 0)  jump to another instruction
		        	if ( MEM[SP] != BoolT) // 2 clops
		        		throw new Exc_Exec(ErrorType.JUMPZ_NOT_SUPPORTED," for type '"+typeName((int)MEM[SP])+"'");
		        	nClops+=5; // including next check
		        	if ( MEM[SP-1] == 0 ) { // 3 clops
		        		IP = (int) IR.getOperand();
		        		nClops++;
		        	}
		        	SP -=2; // 2 clops
					nClops +=2;
		        	break;
		
		        case JUMPNZ: // if the stack operand is true (i.e., equal to 1)  jump to another instruction
		        	if ( MEM[SP] != BoolT) // 2 clops
		        		throw new Exc_Exec(ErrorType.JUMPZ_NOT_SUPPORTED," for type '"+typeName((int)MEM[SP])+"'");
		        	nClops+=5; // including next check
		        	if ( MEM[SP-1] != 0 ) { // 3 clops
		        		IP = (int) IR.getOperand();
		        		nClops++;
		        	}
		        	SP -=2; // 2 clops
					nClops +=2;
		        	break;
		
		        case NEXT: // dummy instruction performing no operations
					nClops +=0;
		        	break;

		        case SLIST: // start the construction of a list whose elements will be next inserted 
		        	if (SP + 2 >= HP) // 2 clops
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"SLIST "+ov_MEM);
		        	SP++; MEM[SP] = nullValue; // 2 clops
		        	SP++; MEM[SP] = nullValue; // 2 clops
					nClops +=6;
		        	break;
		
		        case HLIST: // include a first element into the list under construction  
		        	if ( MEM[SP] < DoubleT || MEM[SP]>MetaTypeT )  // 3 clops (assuming 1 clop for storing MEM[SP])
		        		throw new Exc_Exec(ErrorType.HLIST_NOT_SUPPORTED,"  for type '"+typeName((int)MEM[SP])+"'");
		        	callHLIST("HLIST ");
					nClops +=3;
		        	break;
		
		        case CLIST: // continue the list construction by appending a new element 
		        	if ( MEM[SP] < DoubleT || MEM[SP]>MetaTypeT )  // 3 clops (assuming 1 clop for storing MEM[SP])
		        		throw new Exc_Exec(ErrorType.CLIST_NOT_SUPPORTED," for type '"+typeName((int)MEM[SP])+"'");
		        	callCLIST("CLIST ");
					nClops +=3;
		        	break;
		
		        case ELIST: // complete the construction of a list 
		        	MEM[SP]=ListT;
					nClops +=1;
		        	break;

		        case ALIST: // append an existing list to the list under the construction
		        	if ( MEM[SP] != ListT ) // 2 clop
		        		throw new Exc_Exec(ErrorType.LIST_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");
		        	SP--; // 1 clop
		        	MEM[(int)MEM[SP-1]-2] = MEM[SP];  // 5 clops      	
		        	SP--; // 1 clop
		        	MEM[SP]=ListT;  // 1 clop
					nClops +=10;
		        	break;
		
		        case HEAD: // get the head of a list
		        	if ( MEM[SP] != ListT ) // 2 clops
		        		throw new Exc_Exec(ErrorType.LIST_EXPECTED," found type '"+typeName((int)MEM[SP])+"'");
					callHEAD();
		        	nClops +=2;
		        	break;
		
		        case TAIL: // return the tail of a list
		        	nClops++; // next check
		        	if ( MEM[SP] != ListT ) // 2 clopS
		        		throw new Exc_Exec(ErrorType.LIST_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");
		        	WRI4 = (int)MEM[SP-1]; // 2 clops
		        	callSkipDeletedNulls();
		        	if ( WRI4 == nullValue ) // 3 clops
    	        			throw new Exc_Exec(ErrorType.EMPTY_LIST," - tail is missing");
		        	MEM[SP-1] = MEM[WRI4-2]; // 4 clops
				nClops +=11;
		        	break;
		
		        case LISTEL: // get the element of a list with a given index
		        	if ( MEM[SP-2] != ListT  ) // 3 clops
		        		throw new Exc_Exec(ErrorType.LIST_EXPECTED," - found type '"+typeName((int)MEM[SP-2])+"'");
		        	nClops+=3;
		        	callLISTEL();
					break;

		        case MODVEL: // modify the head element of a list or a field 
		        	if ( MEM[SP] < DoubleT || MEM[SP]>MetaTypeT )  // 3 clops (assuming 1 clop for storing MEM[SP])
		        		throw new Exc_Exec(ErrorType.MODVEL_NOT_SUPPORTED,"  for type '"+typeName((int)MEM[SP])+"'");
		        	SP--; SP--; // 2 clops
		        	if ( MEM[SP] != ListT && MEM[SP] != FieldT) // 2 clops
		        		throw new Exc_Exec(ErrorType.LIST_FIELD_EXPECTED," - found type '"+typeName((int)MEM[SP-4])+"'");
		        	if ( MEM[SP-1] == nullValue ) // 2 clops
		        		if ( MEM[SP] == ListT )
		        			throw new Exc_Exec(ErrorType.EMPTY_LIST," - head element is missing");
		        		else
		        			throw new Exc_Exec(ErrorType.NULL_FIELD," - unexspected behavior");
		        	WRI1 = (int)MEM[SP-1]; // 2 clops
			        MEM[WRI1]=MEM[SP+1]; // 3 clops
			        MEM[WRI1-1]=MEM[SP+2]; // 4 clops
			        SP--; SP--; // 2 clops
					nClops +=22;
		        	break;

		        case LCLONE: // instruction operand op= 0 (additional two stack operands) or 1 (additional three stack operands)  
		        	// if op = 0, clone the element of a list from a given index on
		        	// if op = 1, clone the elements of a list from a first index to a second index (excluded)
	        		nClops+=2; // next check
		        	if( IR.getOperand() == 0 ) { // 2 clops
			        	if ( MEM[SP-2] != ListT  ) // 3 clops
			        		throw new Exc_Exec(ErrorType.LIST_EXPECTED," - found type '"+typeName((int)MEM[SP-2])+"'");
			        	if ( MEM[SP] != IntT && MEM[SP] != CharT ) // 3 clops
			        		throw new Exc_Exec(ErrorType.INT_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");
			        	nClops+=6;
			        	callSLCLONE0("LCLONE "); 
		        	}
		        	else {
			        	if ( MEM[SP-4] != ListT ) // 3 clops
			        		throw new Exc_Exec(ErrorType.LIST_EXPECTED," - found type '"+typeName((int)MEM[SP-4])+"'");
			        	if ( MEM[SP-2] != IntT && MEM[SP-2] != CharT ) // 4 clops
			        		throw new Exc_Exec(ErrorType.INT_EXPECTED," - found type '"+typeName((int)MEM[SP-2])+"'");
			        	if ( MEM[SP] != IntT && MEM[SP] != CharT ) // 3 clops
			        		throw new Exc_Exec(ErrorType.INT_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");
			        	nClops+=10;
			        	callSLCLONE1("LCLONE ");
		        	}
		        	break;

		        case SSTRING: // start the construction of a string whose characters will be next inserted 
			    	if ( SP >= HP   ) // 1 clop
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"SSTRING "+ov_MEM);
					NSPP=HP; MEM[HP]=0; // 2 clops
					HP--; // 1 clop
					nClops+=4;
					break;

		        case CSTRING: // continue the string construction by appending a new character 
		        	if ( MEM[SP] != CharT ) // 2 clops
		        		throw new Exc_Exec(ErrorType.CHAR_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");
	    	    		MEM[NSPP]++; // 3 clops
	    			MEM[HP]= MEM[SP-1]; // 3 clops
	    			SP-=2;// 3 clops
	    			HP--; // 1 clop
	        		nClops +=12;
					break;

		        case ESTRING: // end the construction of a string
	    	    	if ( SP  >= HP   ) // 1 clop
	            		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"ESTRING "+ov_MEM);
		        	SP++; // 1 clop - in this cell the pointer to the string will be stored
		        	SP++; // 1 clop - this cell will be next removed by endAddString 
			    	endAddString("ESTRING ");
			    	NSPP=nullValue; // 1 clop
	    	    	if ( SP  >= HP   ) // 1 clop
	            		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"ESTRING "+ov_MEM);
			    	SP++; // 1 clop
			    	MEM[SP]=StringT; // 1 clop
			    	nClops+=7;
					break;

		        case STRINGEL: // get the character of a string with a given index
		        	if ( MEM[SP-2] != StringT ) // 3 clops
		        		throw new Exc_Exec(ErrorType.STRING_EXPECTED," - found type '"+typeName((int)MEM[SP-2])+"'");
		        	if ( MEM[SP] != IntT && MEM[SP] != CharT ) // 3 clops
		        		throw new Exc_Exec(ErrorType.INT_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");
		        	nClops+=6;
		        	callSTRINGEL();
					break;

		        case SUBSTR: // operand op= 0 (additional two stack operands) or 1 (additional three stack operands)  
		        	// construct a new string composed by the characters of a given string
		        	// from a given index on if op = 0, or 
		        	// from a first index to a second index (excluded) if op = 1 
	        		nClops+=2; // next check
		        	if( IR.getOperand() == 0 ) { // 2 clops
			        	if ( MEM[SP-2] != StringT ) // 3 clops
			        		throw new Exc_Exec(ErrorType.STRING_EXPECTED," - found type '"+typeName((int)MEM[SP-2])+"'");
			        	if ( MEM[SP] != IntT && MEM[SP] != CharT ) // 3 clops
			        		throw new Exc_Exec(ErrorType.INT_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");
			        	nClops+=6;
			        	callSUBSTRING0("SUBSTRING0 ");
		        	}
		        	else {
			        	if ( MEM[SP-4] != StringT ) // 3 clops
			        		throw new Exc_Exec(ErrorType.STRING_EXPECTED," - found type '"+typeName((int)MEM[SP-4])+"'");
			        	if ( MEM[SP-2] != IntT && MEM[SP-2] != CharT ) // 4 clops
			        		throw new Exc_Exec(ErrorType.INT_EXPECTED," - found type '"+typeName((int)MEM[SP-2])+"'");
			        	if ( MEM[SP] != IntT && MEM[SP] != CharT ) // 3 clops
			        		throw new Exc_Exec(ErrorType.INT_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");
			        	nClops+=10;
			        	callSUBSTRING1("SUBSTRING1 ");
		        	}
 		        	break;
 		        	
		        case STRIND: // given two strings S and T and an index i on top of the stack, 
		        	// return the index within the string S of the first occurrence of the string T, 
		        	// starting at the index i or -1 if T does not occur in S, starting at i
		        	if ( MEM[SP-4] != StringT ) // 3 clops
		        		throw new Exc_Exec(ErrorType.STRING_EXPECTED," - found type '"+typeName((int)MEM[SP-4])+"'");
		        	if ( MEM[SP-2] != StringT ) // 3 clops
		        		throw new Exc_Exec(ErrorType.STRING_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");
		        	if ( MEM[SP] != IntT && MEM[SP] != CharT ) // 3 clops
		        		throw new Exc_Exec(ErrorType.INT_EXPECTED," - found type '"+typeName((int)MEM[SP-2])+"'");
		    		SP-=4;// 2 clops
		    		MEM[SP]= MEM[SP+1]; // 3 clops
		    		MEM[SP+1]=MEM[SP+3]; // 4 clops
		    		nClops+=18; 
		        	callSUBSTRIND();
		        	break; 
		        	
		        case SJSON: // start the construction of a json 
		        	// and insert an initial null field
		        	if (SP + 3 >= HP) // 2 clops
		        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"SJSON "+ov_MEM);
		        	SP++; MEM[SP] = nullValue; // 2 clops
		        	SP++; MEM[SP] = nullValue; // 2 clops
		        	nClops+=6;
		        	callSJSON("SJSON ");
		        	break;
		        
		        case CJSON: // continue the json construction by appending a new field
		        	nClops+=2; // next check
		        	if ( MEM[SP] != FieldT )  // 2 clops 
		        		throw new Exc_Exec(ErrorType.CJSON_NOT_SUPPORTED,
		        				" for type '"+typeName((int)MEM[SP])+"'");
		        	callCJSON("CJSON ");
		        	break;
		
		        case EJSON: // given a pair (JF, JL) on top of the stack, 
		        	// where JF and JL are the links to the first and last field of a json
		        	// end the construction of the json being by replacing JL with JsonT
		        	MEM[SP] = JsonT; // 1 clop
		        	nClops++;
		        	break;

		        case NEWFLD: // given a string K and a value V on top of the stack,
		        	// construct a new field (K,V) in the heap and push its address on top of the stack
		        	if ( MEM[SP] < DoubleT || MEM[SP]>MetaTypeT )  // 3 clops (assuming 1 clop for storing MEM[SP])
		        		throw new Exc_Exec(ErrorType.NEWFLD_NOT_SUPPORTED," for type '"+typeName((int)MEM[SP])+"'");
		        	if ( MEM[SP-2] != StringT ) // 3 clops
		        		throw new Exc_Exec(ErrorType.STRING_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");
		           	nClops+=6;
		           	callNEWFLD("NEWFLD ");
		        	break;
		        	
		        case FLDVAL: // given a field on top of the stack
		        	// replace it with its value
		        	nClops+=2; // next check
		        	if ( MEM[SP] != FieldT ) // 2 clops
		        		throw new Exc_Exec(ErrorType.FIELD_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");
		        	callFIELDVAL();
		        	break;
		        	
		        case FLDFIND: // instruction operator op: 0 or 1
		        	// given a json J and a string S on top of the stack
		        	// replace them with the address to the field J[S] if it exists or otherwise 
		        	// if op = 0, with the null field 
		        	// if op = 1, with a new added field with value null
		        	nClops+=3; // next check
		        	if ( MEM[SP-2] != JsonT ) // 3 clops
		        		throw new Exc_Exec(ErrorType.JSON_EXPECTED," - found type '"+typeName((int)MEM[SP-2])+"'");
	        		nClops+=2; // next check
		        	if( IR.getOperand() == 0 )  // 2 clops
		        		callFLDFIND0();
		        	else
		        		callFLDFIND1("FLDFIND 1 ");
		        	break;

		        case JCLONE: // given a json J on top of the stack,
		        	// clone J and replace it on top of the stack
		        	nClops+=3; // next check
		        	if ( MEM[SP] != JsonT ) // 3 clops
		        		throw new Exc_Exec(ErrorType.JSON_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");
		        	callJSCLONE("JSCLONE ");
		        	break;
		        	
		        	
		        case LEN: // compute the length of a list or json or a string 
		        	if ( MEM[SP] != StringT && MEM[SP] != ListT && MEM[SP] != JsonT ) // 4 clops
		        		throw new Exc_Exec(ErrorType.LEN_NOT_SUPPORTED," for type "+typeName((int)MEM[SP]));
		        	nClops+=6; // including next check
		        	if( MEM[SP] == ListT  ) { // 2 clops
		        		 WRI4=(int)MEM[SP-1]; // 2 clops
				         callSkipDeletedNulls();
		        		 WRI1=0; // 1 clop
		        		 nClops+=4; // include last while check 
		        		 while ( WRI4 != nullValue ) { // 1 clop
		        			 WRI1++; // 1 clop
		        			 WRI4=(int)MEM[WRI4-2]; // 2 clops
		 		        	 callSkipDeletedNulls();
		        			 nClops+= 4; // including all while checks but the last one */
		        		 }
		        		 MEM[SP-1]=WRI1; // 2 clops
		        		 MEM[SP]=IntT; // 1 clop
		        		 nClops+=3;
		        		 break;
		        	}
		        	nClops+=2; // next check
		        	if( MEM[SP] == JsonT ) { // 2 clops
		        		 WRI2=(int)MEM[SP-1]; // 2 clops
		        		 WRI4 = (int) MEM[WRI2-2]; // skip the initial null field - 2 clops
	 		        	 callSkipDeletedNulls();
		        		 WRI1=0; // 1 clop
		        		 nClops+=6; // include last while check 
		        		 while ( WRI4 != nullValue ) { // 1 clop
			        		 WRI1++; // 1 clop
		        			 WRI4=(int)MEM[WRI4-2]; // 2 clops
		 		        	 callSkipDeletedNulls();
		        			 nClops+= 4; // including all while checks but the last one 
		        		 }
		        		 MEM[SP-1]=WRI1; // 2 clops
		        		 MEM[SP]=IntT; // 1 clop
		        		 nClops+=3;
		        		 break;
		        	}
		        	MEM[SP-1]=MEM[(int)MEM[SP-1]]; // 5 clops
		        	MEM[SP]=IntT; // 1 clop
		        	nClops+=6;
		        	break;

		        case HDFLDV: // given a list or a field
		        	// return the value of the list head or the field value
		        	nClops+=3; // next check
		        	if ( MEM[SP] != ListT && MEM[SP] != FieldT ) // 3 clops
		        		throw new Exc_Exec(ErrorType.LIST_FIELD_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");
		        	nClops+=2; // next check
		        	if ( MEM[SP] == FieldT ) // 2 clops
		        		callFIELDVAL();
		        	else
		        		callHEAD();
		        	break;
		        	
		        case LJEL: // 2 operands: the first is a json or a list and
		        	// the second respectively a string and an int
		        	// call respectively FLDLV or LISTEL
		        	if( MEM[SP-2] == ListT )  // 2 clops
		        		callLISTEL();
		        	else
			        	if( MEM[SP-2] == JsonT )  // 2 clops
			        		callFLDFIND1("LTJSEL ");
			        	else
			        		throw new Exc_Exec(ErrorType.LIST_JSON_EXPECTED," - found type '"+typeName((int)MEM[SP-2])+"'");
		        	break;

		        case LSJCLONE: // 1 operand: if it is a list or a json, it is cloned or if it is a string, it is kept
		        	nClops+=2; // next check
		        	if ( MEM[SP]==ListT ) {// 2 clops
			        	if (SP +1 >= HP ) // 2 clops
			        		throw new Exc_Exec(ErrorType.STACK_HEAP_OVERFLOW,"LTSTJSCLN "+ov_MEM);
			        	SP++; // 1 clop
			        	MEM[SP] = 0; // 1 clop
			        	SP++; // 1 clop
			        	MEM[SP] = IntT; // 1 clop
						nClops +=6;
		        		callSLCLONE0("LTSTJSCLN ");
		        	}
		        	else {
		        		nClops+=2; // next check
			        	if ( MEM[SP]==JsonT ) {// 2 clops
			        		callJSCLONE("LTSTJSCLN ");
			        	}
			        	else {
			        		nClops+=2; // next check
			        		if ( MEM[SP]==StringT ) { // 2 clops
			        			// the whole string is returned
			        		}
			        		else
			        			throw new Exc_Exec(ErrorType.LIST_STRING_JSON_EXPECTED," - found type '"+typeName((int)MEM[SP-2])+"'");
			        	}
			        }
		        	break;
		        		        	
		        case LSJELV: // 2 operands, the second an index, the first either a list or a string or a json
		        	// return an element
		        	nClops+=3; // next check
		        	if ( MEM[SP-2]==ListT ) { // 3 clops
		        		callLISTEL();
		        		callHEAD();
		        	}
		        	else {
		        		nClops+=3;
			        	if ( MEM[SP-2]==JsonT ) { // 3 clops
			        		callFLDFIND0();
			        		callFIELDVAL();
			        	}
			        	else {
			        		nClops+=3; // next check
			        		if ( MEM[SP-2]==StringT ) { // 3 clops
			        			nClops+=2; // next check
			        			if (MEM[SP]== IntT || MEM[SP]==CharT ) // 2 clops
			        				callSTRINGEL();
			        			else
			        				throw new Exc_Exec(ErrorType.INT_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");
			        		}
			        		else
			        			throw new Exc_Exec(ErrorType.LIST_STRING_JSON_EXPECTED," - found type '"+typeName((int)MEM[SP-2])+"'");
			        	}
			    }
		        	break;
		        	
		        case LSELV: // 2 operands, the second an index, the first either a list or a string
		        	// return an element
		        	nClops+=3; // next check
		        	if ( MEM[SP-2]==ListT ) { // 3 clops
		        		callLISTEL();
		        		callHEAD();
		        	}
		        	else {
			        	nClops+=3; // next check
			        	if ( MEM[SP-2]==StringT ) { // 3 clops
		        			nClops+=2; // next check
		        			if (MEM[SP]== IntT || MEM[SP]==CharT ) // 2 clops
		        				callSTRINGEL();
		        			else
		        				throw new Exc_Exec(ErrorType.INT_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");
		        		}
		        		else
		        			throw new Exc_Exec(ErrorType.LIST_STRING_EXPECTED," - found type '"+typeName((int)MEM[SP-2])+"'");
			    }
		        	break;
		        	
		        case LSCLONE: // 2 operands, the second an index, the first either a list or a string - 
		        				  // return the postifx sublist or substring
	        		nClops+=2; // next check
		        	if( IR.getOperand() == 0 ) { // 2 clops
			        	nClops+=3; // next check
			        	if ( MEM[SP-2]==ListT ) { // 3 clops
			        		nClops+=3; // next check
			        		if ( MEM[SP]==IntT || MEM[SP]== CharT )// 3 clops
			        			callSLCLONE0("LSCLONE ");
			        		else
	        					throw new Exc_Exec(ErrorType.INT_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");		        			
			        	}
			        	else {
			        		nClops+=3; // next check
			        		if ( MEM[SP-2]==StringT ) { // 3 clops
			        			nClops+=3; // next check
			        			if (MEM[SP]== IntT || MEM[SP]==CharT ) // 3 clops
			        				callSUBSTRING0("LSCLONE ");
			        			else 
			        				throw new Exc_Exec(ErrorType.INT_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");
			        		}
			        		else
			        			throw new Exc_Exec(ErrorType.LIST_STRING_EXPECTED," - found type '"+typeName((int)MEM[SP-2])+"'");
			        	}
		        	}
		        	else {
		        // construct a new string composed by the characters of a given string or 
		        	// a new list composed by the element of a given list, 
		        	// from a first index to a second index (excluded)
			        	nClops+=3; // next check
			        	if ( MEM[SP-4]==ListT ) {// 3 clops 
			        		nClops+=3; // next check
			        		if ( MEM[SP-2]==IntT || MEM[SP-2]==CharT ) { // 3 clops
			        			nClops+=3; // next check
			        			if ( MEM[SP]==IntT || MEM[SP]==CharT) // 3 clops
			        				callSLCLONE1("LSCLONE ");
			        			else
			        				throw new Exc_Exec(ErrorType.INT_EXPECTED," - found type '"+typeName((int)MEM[SP])+"'");
			        		}
			        		else
			        			throw new Exc_Exec(ErrorType.INT_EXPECTED," - found type '"+typeName((int)MEM[SP-2])+"'");		
			        	}
			        	else {
			        		nClops+=3; // next check
			        		if ( MEM[SP-4]==StringT ) { // 3 clops
			        			nClops+=4; // next check
			        			if (MEM[SP-2]!= IntT && MEM[SP-2]!=CharT )  // 4 clops
			        				throw new Exc_Exec(ErrorType.INT_EXPECTED," - found type '"+typeName((int)MEM[SP-2])+"'");
			        			else {
			        				nClops+=3; // next check
			        				if ( MEM[SP]== IntT || MEM[SP]==CharT ) { // 3 clops
			        					callSUBSTRING1("LSCLONE ");
			        				}
				        			else
				        				throw new Exc_Exec(ErrorType.INT_EXPECTED," - found type '"+types[(int)MEM[SP]]+"'");
			        			}
			        		}
			        		else
			        			throw new Exc_Exec(ErrorType.LIST_STRING_EXPECTED," - found type '"+types[(int)MEM[SP-2]]+"'");
			        	}
		        	}
		        	break;
		        	
		        case EXECF:
		        		execF(debugAct,IPC);
		        	break;
		        	
			    case PRINT: 
		    		WRI3 = (int) IR.getOperand(); // 1 clop
		    		WRI1 = (int) MEM[SP]; // 1 clop
		    		SP--; // 1 clop
    	        		nClops +=4; // including next check
    	        		if ( WRI1 >= DoubleT && WRI1 <= MetaTypeT ) { // 1 clop
				    if (OP+1 > OS ) // 1 clop
			        		throw new Exc_Exec(ErrorType.OUTPUT_OVERFLOW, "Internal: increase the output size > "
									+OS + " for Argument EXECOS");
			        	OUTPUT[OP]= MEM[SP]; // 2 clops 
	    	        		SP--; // 1 clop
			        	OP++; // 1 clops 
			        	nClops+=6; // including next check
			        	if ( WRI3 == 1 ) { // 1 clop
			        		nClops++; // next check
			        		if ( WRI1==StringT ) { // 1 clop
			        			OUTPUT[OP]= StringQF; // 1 clop
					        	OP++; // 1 clop
			        			nClops+=2;
			        			break;
			        		}
		        			nClops++; // next check
		        			if ( WRI1==CharT) { // 1 clop
		        				OUTPUT[OP]= CharQF; // 1 clop
					        	OP++; // 1 clop
			        			nClops+=2;
			        			break;
		        			}
		        			nClops++; // next check
		        			if ( WRI1==NullT ) { // 1 clop
		        				OUTPUT[OP]= NullQF; // 1 clop
					        	OP++; // 1 clop
			        			nClops+=2;
			        			break;
		        			}
			        	}
			        	nClops += 2; // next check
			        	if ( WRI3 == 3 ) { // 1 clop
			        		nClops++; // next check
			        		if ( WRI1==StringT ) { // 1 clop
			        			OUTPUT[OP]= StringQF; // 1 clop
					        	OP++; // 1 clop
			        			nClops+=2;
			        			break;
			        		}
		        			nClops++; // next check
		        			if ( WRI1==CharT) { // 1 clop
		        				OUTPUT[OP]= CharQF; // 1 clop
					        	OP++; // 1 clop
			        			nClops+=2;
			        			break;
		        			}
		        			nClops++; // next check
		        			if ( WRI1==NullT ) { // 1 clop
		        				OUTPUT[OP]= NullQF; // 1 clop
					        	OP++; // 1 clop
			        			nClops+=2;
			        			break;
		        			}
		        			nClops++; // next check
		        			if ( WRI1==JsonT ) { // 1 clop
		        				OUTPUT[OP]= JsonIndentAllF; // 1 clop
					        	OP++; // 1 clop
			        			nClops+=2;
			        			break;
		        			}
		        			nClops++; // next check
		        			if ( WRI1==ListT ) { // 1 clop
		        				OUTPUT[OP]= ListIndentAllF; // 1 clop
					        	OP++; // 1 clop
			        			nClops+=2;
			        			break;
		        			}
			        	}
			        	nClops += 2; // next check
		        		if ( WRI3 == 2 ) { // 2 clops
		        			nClops++; // next check
		        			if ( WRI1==JsonT ) { // 1 clop
		        				OUTPUT[OP]= JsonIndent1F; // 1 clop
					        	OP++; // 1 clop
			        			nClops+=2;
			        			break;
		        			}
		        			nClops++; // next check
		        			if ( WRI1==ListT ) { // 1 clop
		        				OUTPUT[OP]= ListIndent1F; // 1 clop
					        	OP++; // 1 clop
			        			nClops+=2;
			        			break;
		        			}
			        	}
			        	OUTPUT[OP]= WRI1; // 1 clop
			        	OP++; // 1 clop
			        	nClops+=2;
    	        	}
    	        	else 
			        		throw new Exc_Exec(ErrorType.PRINT_NOT_SUPPORTED, " for type '"+typeName((int)MEM[WRI1])+"'");
		        	break;
		        	
		        default:
		        	throw new Exc_Exec(ErrorType.WRONG_MACHINE_OPERATOR);
		
		      } // switch
		      if ( debugAct ) debugAct=Debug.printState(IPC); 
		      return debugAct;
		} 	catch ( Exc e ) {
				e.addMessage(" - IP ="+IP);
				throw e; 
			}
			catch ( Exception e ) {
				throw new Exc_Exec(ErrorType.EXEC_FAILURE, e.getClass().getName()+" ("+e.getMessage()+") - IP = "+IP);
			}
		    
	  }


}
