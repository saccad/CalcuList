package assembler;


import java.io.*;

import exec.*;
import error.Exc;
import error.Exc_Assembler;
import error.Exc.ErrorType;
import java.util.ArrayList;


public class Assembler {

	static  BufferedReader reader;
	static private int nMaxListing;
	static private String release = "4.2.0";
	static private String releaseDate = "July 5, 2018";
	static String defaultFileExt = ".clvm";
	private static final int labelInitCapacity = 200;
	private static final int calledLabelInitCapacity = 100;
	public static void main(String [] args){
		reader = new BufferedReader(new InputStreamReader(System.in));
		boolean error = false;
		try {
			checkArgs(args);
		} catch (Exc e ) {
			System.out.println("\n**ERROR ON LAUNCHING ARGUMENTS "+e.print());
			error = true;
		}
		if (error)
			return;
		// initialization of arrays
		Exec.startArray(); FrameDebugHandler.startArray();
		Debug.set_runAssembler();
		int maxCodeSize = Exec.getCS();
	    String E;
	    System.out.print (
				"     *************************************************\n" +
				"              ***       CLVM Assembler     ***        \n" +
				"     *************************************************\n"+
				"     ********* Release "+release+" of "+releaseDate+" *******    \n\n");

		boolean endSession = false;
		ArrayList<LabelInstr> labels=new ArrayList<LabelInstr>(labelInitCapacity);
		ArrayList<LabelInstr> calledLabels = new ArrayList<LabelInstr>(calledLabelInitCapacity);
		try {
			FileReader fr = new FileReader(reader);
			// int nListing=readListing(listing); 
			int iUnitCode = 1;
			int iL=0;
			String instrLine=null;
			do {
				boolean end = false; boolean begin = false;
				int nPr = 0; 
				endSession = false;
				while ( nPr < maxCodeSize && !end && !fr.endFile() ) {
					instrLine = fr.nextFileLine(); iL++;
					System.out.print(iL+":\t"+instrLine);
					AsmLine asmLine = LexAssembler.parseInstruction(instrLine,nPr,
							labels, calledLabels);
					if ( asmLine instanceof AsmBeginEnd && ((AsmBeginEnd) asmLine).type==LexAssembler.BEGIN) 
						if ( !begin ) {
							begin=true;
							System.out.println("\t-- PROGRAM #" + iUnitCode + " --");
							iUnitCode++;
							labels.clear(); calledLabels.clear();
						}
						else
							throw new Exc(ErrorType.MISSING_END);
					else 
						if ( asmLine instanceof AsmBeginEnd && ((AsmBeginEnd) asmLine).type==LexAssembler.END) 
							if ( begin ) {
								end =true;
								System.out.println("\t-- PROGRAM #" + (iUnitCode-1) + " --");
							}
							else
								throw new Exc(ErrorType.MISSING_BEGIN); //***
						else {
							System.out.println();
							if ( asmLine instanceof AsmInstr )
								if ( begin ) {
									Exec.modInstruction(nPr,((AsmInstr) asmLine).instr);
									nPr++;
								}
								else
									throw new Exc(ErrorType.MISSING_BEGIN); 
						}
				}
				if ( !end )
					if ( nPr == maxCodeSize )
						throw new Exc(ErrorType.LARGE_CODE, "Internal: increase the CODE size"
								+maxCodeSize + " for Argument EXECCS");
					else
						if ( begin )
							throw new Exc(ErrorType.MISSING_END);
				if ( nPr > 0 ) {
					solveLabels(labels, calledLabels);
					System.out.print("\nPrinting listing? (Y/N) \n>> ");
					E = reader.readLine();
					if ( E.charAt(0) == 'Y' || E.charAt(0) == 'y' )
						printProgram( nPr );
					System.out.print("Debug? (Y/N) \n>> ");
					E = reader.readLine();
					boolean debugReq = E.charAt(0)=='Y' || E.charAt(0)=='y';
					Exec.exec(debugReq,nPr);
					if (Exec.getOP() > 0 )
						PrintOutput.printOutput(true);
					System.out.print("\nQuit the session? (Y/N) \n>> ");
					E = reader.readLine();
					if ( E.charAt(0) == 'Y' || E.charAt(0) == 'y' )
						endSession=true;
				}
			} while ( !fr.endFile() && !endSession );
			fr.closeFile();
		} 	catch (Exc e ) {
				System.out.println("\n**Execuction Terminated "+e.print());
			}
			catch ( Exception e ) {
				System.out.println("\n** Unexpected error ** "+e.getClass().getName()+": "+e.getMessage());
			}
			
		bye();
	}

	static void solveLabels (ArrayList<LabelInstr> labels, ArrayList<LabelInstr> calledLabels) throws Exc {
		for ( int i=0; i < calledLabels.size(); i++ ) {
			LabelInstr li = calledLabels.get(i);
			int label = li.label;
			int iInst = li.instrNo;
			int k = labels.indexOf(li);
			if ( k < 0 )
				throw new Exc_Assembler(ErrorType.UNSOLVED_LABEL, " "+label);
			int kInstrNo = labels.get(k).instrNo;
			Exec.getInstruction(iInst).modOperand(kInstrNo);
		}	
	}

	
	static void printProgram( int k ) throws Exc {
		System.out.println("\n** Listing of the program unit **");
		for (int i = 0; i < k; i++)
			System.out.println(i+"\t"+Exec.getInstruction(i).decode() );
		System.out.println("** End of Listing **\n");		
	}
	
	static void bye() {
	 	System.out.println("\nBye");
    }

	static void checkArgs(String[] args ) throws Exc {
		if ( args.length > 0 ) {
			int i = 0;
			do {
				if ( i+1 == args.length)
					throw new Exc(ErrorType.WRONG_ARGS_NUM, 
							" - the number of arguments must be even");
				checkArg1(args[i],args[i+1]);
				i +=2;
			} while(i < args.length);
				
		}
	}
	

	static void checkArg1(String a, String k) throws Exc {
		  for ( ArgsH b : ArgsH.values() )
			  if ( a.equalsIgnoreCase(b.name()) )
				  switch(b) {
				  case EXECCS: nMaxListing=setInt(k,b.name()); Exec.modSizeCS(nMaxListing);
				              return;
				  case EXECMS: Exec.modSizeMS(setInt(k,b.name())); return;
				  case EXECOS: Exec.modSizeOS(setInt(k,b.name())); return;
				  }
		  throw new Exc(ErrorType.WRONG_ARG_NAME, 
					" - valid ones are EXECCS EXECMS EXECOS");
		
	}


	static int setInt(String k, String arg) throws Exc {
		int size;
		try {
			size = Integer.parseInt(k);
		} 
		catch ( NumberFormatException e ) {
			throw new Exc(ErrorType.WRONG_ARG_INT," - "+k+" for "+arg);
		}
		if ( size < 1 )
			throw new Exc(ErrorType.WRONG_ARG_VAL," - "+k+" for "+arg);			
		return size;
	}


	enum ArgsH {
		EXECCS, EXECMS, EXECOS;
	}


} // end class Ass

class FileReader {
	
	File file = null;
	FileInputStream fstream=null;
	String fileName;
	String strLine = null;
	DataInputStream in;
	BufferedReader br;
	
	FileReader( BufferedReader r ) throws Exc {
		BufferedReader reader=r; // text reader
		System.out.print("Enter file name (path w.r.t the current working directory or full path):\n>> ");
		try {
			fileName = reader.readLine();
			try {
				file = new File(fileName);
				fstream = new FileInputStream(file);
			} catch ( Exception e1 ) {
				int n = fileName.length();
				if ( n > 3 && Assembler.defaultFileExt.equals(fileName.substring(n-Assembler.defaultFileExt.length())))
					throw new Exc_Assembler(ErrorType.WRONG_FILE, e1.getClass().getName()+": "+e1.getMessage());
				else
					try {
						System.out.println("No file \""+fileName+
								"\" found - searching for such file with default extension \""+
								Assembler.defaultFileExt+"\"");
						fileName +=Assembler.defaultFileExt;
						file = new File(fileName);
						fstream = new FileInputStream(file);							
					} catch ( Exception e ) {
						throw new Exc_Assembler(ErrorType.WRONG_FILE, e.getClass().getName()+": "+e.getMessage());
					}					
			}
			System.out.println("** Importing file \""+file.getCanonicalPath()+"\"  **\n");
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			strLine = br.readLine();
			if ( strLine== null )
				throw new Exc_Assembler(ErrorType.EMPTY_FILE);
		} catch (Exception e) {
			throw new Exc_Assembler(ErrorType.WRONG_FILE, e.getClass().getName()+": "+e.getMessage());
		}
	}
	
	String nextFileLine () throws Exc {
		try {
			if ( strLine == null ) 
				return strLine;
			else {
				String str1 = strLine;
				strLine = br.readLine();
				return str1;
			}
		} catch (Exception e) {
			throw new Exc_Assembler(ErrorType.WRONG_FILE, e.getClass().getName()+": "+e.getMessage());
			}
	}
	
	boolean endFile () {
		return strLine == null;
	}
	
	void closeFile () throws Exc{
		try {
			in.close();
		} catch ( Exception e ) {
			throw new Exc_Assembler(ErrorType.WRONG_FILE, e.getClass().getName()+": "+e.getMessage());
		}
	}
}
