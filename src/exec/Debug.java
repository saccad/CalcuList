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


import java.util.Scanner;

import sem.SymbolTableH;
import exec.Instruction.Operator;

public class Debug {
	static  Scanner debugReader = new Scanner(System.in);
	static int debugSteps, iDebugStep;
	static boolean standardBP; // true for standard breakpoints
	static int iState;
	static int initCodeN; // it is modified when a run time function is built
	public static int nGlobVar;
	public static boolean runCalcuList = true;
	static String callsID;

   public static void set_runAssembler () {
	   runCalcuList = false;
   }
   
   static void debugStart() {
	  iDebugStep = 1; debugSteps = 1; iState=0;
	  standardBP=false;
	  initCodeN=Exec.CODEN;
	  for ( int i = 0; i <Exec.CS; i++ )
		  Exec.BREAKPOINTS[i]=false;
	  System.out.println("Breakpoints on START and RETURN instructions? (Y/N)");
	  char replay = debugReader.next().charAt(0);
	  if ( replay == 'Y' || replay=='y')
		  standardBP=true;
	  else {
		  System.out.println("Other Breakpoints? (Y/N)");
		  replay = debugReader.next().charAt(0);
		  if ( replay == 'Y' || replay=='y') {	  
			  System.out.println("Type the instruction labels or a negative number to finish");
			  int iBP;
			  do {
				  iBP = debugReader.nextInt();
				  if ( iBP >= 0 && iBP < Exec.CS )
					  Exec.BREAKPOINTS[iBP]=true;
			  } while ( iBP >=0 );
		  }
	  }
	  
   }
 
	static void initFrame ( int nGV ) {
		FrameDebugHandler.initFrame();
		nGlobVar=nGV;
	}

	static void removeFrame (  ) {
		FrameDebugHandler.removeFrame();
	}

	static void addFrame( int ind ) {
		FrameDebugHandler.addFrame(ind);
	}
	
	public static String printType(int t){
		if ( t < 0 || t > Exec.StringQF)
			 return "?";
		else
			 return Exec.types[t];
	}
	
    static String printPointer( int X ) {
    	if ( X==Exec.nullValue ) 
    		return "null";
    	else
    		return Integer.toString(X);
    }

	public static void printHeap() {
		printHeap(Exec.MS-Exec.HP);
	}

	public static void printHeap( int nElem ) {
	      int i=Exec.MS-1;
	      if ( i == Exec.HP ) {
	    	  System.out.println("EMPTY HEAP");
		      System.out.println("======================");
	    	  return;
	      }
	      if ( nElem == 0 ) {
	    	  System.out.println("Skipping all heap elements");
		      System.out.println("======================");
	    	  return;	    	  
	      }
	      System.out.println("======================");
	      System.out.print("++ HEAP");
	      System.out.println(" (1^ Garbage Element *-> "+printPointer(Exec.GP)+
	    			  ", 1^ string *-> "+printPointer(Exec.FSPP)+")");
	      int nStAdd=Exec.FSPP;
	      int iStr=0;
	      int iListEl=0;
	      int iFieldVal = 0;
	      int iJsonEl = 0;
	      int iGarbEl = 0;
	      int iDelEl = 0;
	      int iDelField = 0;
	      int iElem = 0;
	      boolean isGarbageEl;
		  System.out.print("---------------");
	      while ( i > Exec.HP && iElem <= nElem ) {
	    	  if ( iElem == nElem ) 
	    		  System.out.print("\n...skipping next heap elements...\n");
	    	  else {
			      isGarbageEl = false;
		    	  if ( i==nStAdd || i==Exec.NSPP ) {
		    		  boolean completeString = i!=Exec.NSPP;
		    		  if ( completeString )
		    			  System.out.println(" [String "+iStr+"]");
		    		  else
		    			  System.out.println(" [String "+iStr+"] - incomplete");
		    		  int n=(int)Exec.MEM[i];
			    	  System.out.format("%05d",i);
			    	  System.out.print(": ");
			    	  System.out.format("%-6d",n);
		    		  System.out.println("\tlength");
		    		  for ( int k =1; k<=n; k++) {
		    			  i--;
				    	  System.out.format("%05d",i);
				    	  System.out.print(": ");
				    	  System.out.format("%-6d",(int)Exec.MEM[i]);
		    			  System.out.println("\t'"+(char)Exec.MEM[i]+"'");
		    		  }
		    		  i--;
		    		  if ( completeString ) {
				    	  System.out.format("%05d",i);
				    	  System.out.print(": ");
				    	  System.out.format("%-6d",(int)Exec.MEM[i]);
			    		  if ( Exec.MEM[i]==Exec.nullValue ) {
			    			  System.out.println("\t* last string");
			    			  nStAdd=Exec.nullValue; 
			    		  }
			    		  else {
			    			  System.out.println("\t*-> next string");
			    			  nStAdd=(int)Exec.MEM[i];
			    			  iStr++;
			    		  }
			    		  i--; 
		    		  }
		    	  }
		    	  else 
			    	  if ( Exec.MEM[i-2] < 0 ) { // field <value,key> (the link is negative)
			    		  if ( Exec.MEM[i]==Exec.deletedNullValue && Exec.MEM[i-1]==Exec.NullT ) {
			    			  System.out.println(" [Deleted Field-Value "+iDelField+"]");
			    			  iDelField++;		    			  
			    		  }
			    		  else {
			    			  System.out.println(" [Key-Value "+iFieldVal+"]");
				        	  iFieldVal++;		    			  
			    		  }
				    	  System.out.format("%05d",i);
				    	  System.out.print(": ");
			    		  printValue(Exec.MEM[i], (int)Exec.MEM[i-1]);
			    		  i--;
				    	  System.out.format("%05d",i);
				    	  System.out.print(": ");
				    	  System.out.format("%-6d",(int)Exec.MEM[i]);
			        	  System.out.println("\t" +printType((int)Exec.MEM[i]));
			    		  i--;
				    	  System.out.format("%05d",i);
				    	  System.out.print(": ");
				    	  System.out.format("%-6d",-(int)Exec.MEM[i]);
		        		  System.out.println("\t*->HEAP (key string)");
			        	  i--;
			    	  }
			    	  else {
			    		  int type = (int)Exec.MEM[i-1];
			    		  if ( type == Exec.FieldT ) {
			    			  System.out.println(" [Json Element "+iJsonEl+"]");
			    			  iJsonEl++;
			    		  }
			    		  else 
			    			  if( type==Exec.NoType ) {
				    			  System.out.println(" [Garbage Element "+iGarbEl+"]");
				    			  iGarbEl++;
				    			  isGarbageEl=true;
			    			  }
			    			  else
			    				  if ( type==Exec.NullT && Exec.MEM[i] == Exec.deletedNullValue ) {
					    			  System.out.println(" [Deleted List Element "+iDelEl+"]");
					    			  iDelEl++;
			    				  }
				    			  else {
				    				  System.out.println(" [List Element "+iListEl+"]");
				    				  iListEl++;		    			  
				    			  }
				    	  System.out.format("%05d",i);
				    	  System.out.print(": ");
			    		  printValue(Exec.MEM[i], type);
			    		  i--;
				    	  System.out.format("%05d",i);
				    	  System.out.print(": ");
				    	  System.out.format("%-6d",(int)Exec.MEM[i]);
			        	  System.out.println("\t" +printType(type));
			        	  i--;
				    	  System.out.format("%05d",i);
				    	  System.out.print(": ");
				    	  System.out.format("%-6d",(int)Exec.MEM[i]);
			        	  if ( type == Exec.FieldT ) {
				        	  if ( Exec.MEM[i]==Exec.nullValue )
				        		  System.out.println("\tend json");
				        	  else
				        		  System.out.println("\t*->next json element");
			        	  }
			    		  else 
			    			  if ( isGarbageEl )
					        	  if ( Exec.MEM[i]==Exec.nullValue )
			    					  System.out.println("\tend garbage list");
					        	  else
			    					  System.out.println("\t*->next garbage element");
			    			  else		    				  
					        	  if ( Exec.MEM[i]==Exec.nullValue )
					        		  System.out.println("\tend list");
					        	  else
					        		  System.out.println("\t*->next list element");
			        	  i--;
			    	  }
		    	  }
	    	  iElem++;
    		  System.out.print("---------------");
	      }
	      System.out.println();
	      System.out.println("======================");
	}
	
	static void printOutput() {
	      System.out.println("---------------");
	      System.out.println("++ OUTPUT");
	      int j0=0;
	      for ( int j = j0; j < Exec.OP; j++ ) {
	    	  if ( j % 2 == 0) {
	    		  System.out.println("---------------");
	    		  int t = (int)Exec.OUTPUT[j+1];
	    		  if ( t == Exec.CharQF )
	    			  t=Exec.CharT;
	    		  else
	    			  if( t == Exec.StringQF || t== Exec.StringExcF )
	    				  t=Exec.StringT;
		    	  System.out.format("%05d",j);
		    	  System.out.print(": ");
	    		  printValue(Exec.OUTPUT[j],t);
	    	  }
	    	  else {
		    	  System.out.format("%05d",j);
		    	  System.out.print(": ");
		    	  System.out.format("%-6d",(int)Exec.OUTPUT[j]);
	    		  System.out.println("\t" +printType((int)Exec.OUTPUT[j]));
	    	  }
	       }
	      System.out.println("---------------");				
	}

	static void printAddedCode( int startC, int endC ) {
	      System.out.println("---------------");
	      System.out.println("++ CODE ADDED AT RUN TIME");
	      for ( int i = startC; i<endC; i++ ) 
	    	  		System.out.print(i+"\t"+Exec.CODE[i].decode()+"\n" );
	      System.out.println("---------------");				
	}

	static boolean isStandardBP(int IPC ) {
    	if ( standardBP && ( Exec.CODE[IPC].getOperator() == Operator.START || 
    			Exec.CODE[IPC].getOperator() == Operator.RETURN ))
    		return true;
    	else
    		return false;
    }
    
    static boolean isPrintable ( int c ) {
    	return c>=32 && c <=127;
    }
    
   public static void printValue(double v, int t ) {
    	if ( t== Exec.DoubleT ) {
    		System.out.println(v);
    		return;
    	}
    	if ( t== Exec.IntT ) {
    		System.out.println((int)v);
    		return;
    	}
    	if ( t== Exec.LongT ) {
    		System.out.println(Double.doubleToRawLongBits(v));
    		return;
    	}
    	System.out.format("%-6d",(int)v);
		if ( t==Exec.BoolT ) 
			if ( v== 0 )
				System.out.println("\tfalse");
			else
				System.out.println("\ttrue");
		else
			if ( ( v!= Exec.nullValue && t==Exec.ListT) ||  t==Exec.StringT ||  
			       t==Exec.JsonT || ( v!= Exec.nullValue && t==Exec.FieldT ) ) {
				System.out.println("\t*->HEAP");
			}
			else
   				if ( v== Exec.nullValue && t==Exec.FieldT )
	   				System.out.println("\tnull");
				else
    				if ( v== Exec.nullValue && t==Exec.ListT )
    	   				System.out.println("\t[]");
    				else
    					if ( t==Exec.CharT ) {
    						if ( isPrintable((int)v) )
    							System.out.println("\t'"+(char)v+"'");
    						else
    							System.out.println("\t'\\"+(int)v+"'");
    					}
    					else
    						if ( t==Exec.TypeT || t==Exec.MetaTypeT)
    							System.out.println("\t"+printType((int)v));
    						else
    							if ( t==Exec.RefT )
    								System.out.println("\t*->STACK");
    							else
    								System.out.println("");
    }

	static boolean printState ( int IPC ) {
	  boolean debugAct = true;
	  System.out.println("\n************");
	  System.out.println(  " STATE "+iState);
	  System.out.println("************");
	  System.out.println(" FP = "+Exec.FP + " SP = " + 
			  Exec.SP+ " HP = " + Exec.HP+ " OP = "+Exec.OP+ " CODEN = "+Exec.CODEN);
	  System.out.println(" GP = "+Exec.GP+" FSPP = "+Exec.FSPP+" LSPP = "+Exec.LSPP+" NSPP = "+Exec.NSPP);
	  Instruction instrC = Exec.CODE[IPC];
	  if (instrC.getOperator() == Operator.CALLS ) 
		  instrC.modComment(callsID);
	  System.out.println(" Current Instruction: ["+IPC+"] "+instrC.decode());
	  if ( Exec.IP >= 0 ) {
		  instrC = Exec.CODE[Exec.IP];
		  if (instrC.getOperator() == Operator.CALLS ) {
			  callsID = Exec.CODE[(int)Exec.MEM[Exec.SP-1]].getComment().replaceFirst("\\*", "->()");
			  instrC.modComment(callsID);
		  }
		  System.out.println(" Next Instruction:    ["+Exec.IP+"] "+instrC.decode() );
	  }
	  else
		  System.out.println(" Next Instruction: End of Execution" );
      System.out.println("\n++ STACK");
      System.out.println("====================== MAIN UNIT");
      int iStack = 0;
      for ( int i=0; i < nGlobVar; i++ ) {
	      System.out.print("---------------");
	      System.out.print(" [GV "+i+"]");
	      if ( runCalcuList ) 
	    	  	System.out.print(": \""+SymbolTableH.idGV(i)+"\"");
	      System.out.println();
    	  	  System.out.format("%05d",iStack);
    	  	  System.out.print(": ");
    	  	  printValue(Exec.MEM[iStack], (int) Exec.MEM[iStack+1]);
    	  	  iStack++;
    	  	  System.out.format("%05d: ",iStack);
    	  	  System.out.format("%-6d",(int)Exec.MEM[iStack]);
    	  	  System.out.println("\t"+printType((int)Exec.MEM[iStack]));
    	  	  iStack++;   	  
      }
      int iUnit =1;
      while ( iStack <= Exec.SP ) {
    	  int iFP = FrameDebugHandler.isFpEntry(iStack) ; 
    	  if ( iFP > 0 ) {
      		  System.out.println("====================== UNIT "+iUnit);
    		  iUnit++;
        	  System.out.format("%05d: ",iStack);
        	  System.out.format("%-6d",(int)Exec.MEM[iStack]);
        	  System.out.println("\tRA");
    	   	  iStack++;  	  
           	  System.out.format("%05d: ",iStack);
        	  System.out.format("%-6d",(int)Exec.MEM[iStack]);
    		  System.out.println("\tDL");
    	   	  iStack++;  	  
    	  }
    	  else {
    	      System.out.println("---------------");
        	  System.out.format("%05d",iStack);
        	  System.out.print(": ");
    		  if ( iStack < Exec.SP ) {
    	       	  printValue(Exec.MEM[iStack], (int) Exec.MEM[iStack+1]);
            	  iStack++;
            	  System.out.format("%05d: ",iStack);
            	  System.out.format("%-6d",(int)Exec.MEM[iStack]);
            	  System.out.println("\t"+printType((int)Exec.MEM[iStack]));
	    	   	  iStack++;  	  			  
    		  }
    		  else {
            	  System.out.format("%-6d",(int)Exec.MEM[iStack]);
            	  System.out.println("\tRA");
    			  iStack++;
    		  }
    	  }
      }
	 System.out.println("====================");
      
      if ( debugSteps > 0 || Exec.BREAKPOINTS[IPC] || isStandardBP(IPC)) {
          iDebugStep++;
	      if ( iDebugStep > debugSteps) {
		      if (  Exec.MS-1 > Exec.HP  ) {
			    	  System.out.println("\nPrint Heap? (Y/N)");
			    	  char replay = debugReader.next().charAt(0);
			    	  if ( replay == 'Y' || replay=='y')
			    		  printHeap();
		    	  }
		    	  else
		    		  System.out.println("\n++ EMPTY HEAP ++");
			  if (  Exec.OP > 0  ) {
				    	  System.out.println("\nPrint Output? (Y/N)");
				    	  char replay = debugReader.next().charAt(0);
				    	  if ( replay == 'Y' || replay=='y')
				    		  printOutput();
		    	  }
		    	  else
		    		  System.out.println("\n++ EMPTY OUTPUT ++");
			  if (  Exec.CODEN > initCodeN  ) {
		    	  		System.out.println("\nPrint the "+(Exec.CODEN - initCodeN)+" Lines of Code Added at Run Time? (Y/N)");
		    	  		char replay = debugReader.next().charAt(0);
		    	  		if ( replay == 'Y' || replay=='y')
		    	  			printAddedCode(initCodeN,Exec.CODEN);
			  }
			  else
				  System.out.println("\n++ NO CODE ADDED AT RUN TIME ++");
			  
		      System.out.println("\nNumber of steps before next pause? --enter 0 to go to next breakpoint, -1 to stop debugging");
		      try {
		    	  	debugSteps = debugReader.nextInt(); 
		      }
		      catch ( Exception e ) {
			      System.out.println("Wrong input -- converted to 1");		    	  
		    	  	  debugSteps = 1;
		      }
			      
			  if ( debugSteps >= 0 )
				  iDebugStep = 1;
			  else 
				   debugAct = false;
	      }
      }
      iState++;
	return debugAct;  
  }

} // end class Debug

