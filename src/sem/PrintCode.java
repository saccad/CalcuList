package sem;

import error.Exc;
import exec.Exec;
import sem.CodeUnit;

public class PrintCode {

	public static void printListing (CodeUnit uc ) {
		System.out.print("\n------CODE UNIT------\n");
		for (int i = 0; i < uc.numInstr(); i++) {
			System.out.print(i+"\t"+uc.instr(i).decode()+"\n" );
		}
		System.out.print("---End of CODE UNIT---\n");		
		
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
