package ioHandler;

import java.io.*;
import java.util.Date;

import error.Exc;
import error.Exc_Assembler;
import error.Exc_IOH;
import error.Exc.ErrorType;
import exec.Exec;
import exec.FrameDebugHandler;
import synt.Parser;
import sem.SymbH;


public class IOH {
	  protected char look; // single lookahed character (current character)
	  protected  String line; // single current input line
	  protected int iLine; // line current index
	  private  BufferedReader reader; //  standard input stream
	  private  boolean isStandardInput; //  true standard input stream - false input from file
	  private  boolean isUtilityImport; //  true if the utility file is being imported
	  private  boolean importError; //  true if error has been detected on imported file
	  private  boolean echo; //  true if the input file command must be echoed
	  private int previous_nHist; // number of commands in History before importing a file
	  private boolean isFileIn; // true if data input from a file
	  private static String defaultFileExt = ".cl";
	  private int nUtilFuncts;
	  private int nUtilHist;
	  protected FileH fileH;

	  /** Builds up an IOH object to read data from the standard input
	   * and write data to the standard output. */
	  
	  public static void main(String [] args) {
			boolean error = false;
			try {
				checkArgs(args);
			} catch (Exc e ) {
				System.out.println("\n**ERROR ON LAUNCHING ARGUMENTS**");
				printError (e); error = true;
			}
			if (error)
				return;
			// initialization of arrays
			Exec.startArray(); FrameDebugHandler.startArray();
		    OutputH.printStart();
		    Parser.program(); // call Calculist	    		
		 }

	  public IOH(){
		//OutputH.setPrompt(true);
		reader = new BufferedReader(new InputStreamReader(System.in));
	  // use default buffer size
	    line = "";
	    iLine = 1;
	    look = ' ';
	    isStandardInput=true;
	    importError=false;
	    echo= true;
	    isFileIn = false; 
	    nUtilFuncts=0; nUtilHist=0;
	    isUtilityImport = true;
	    String fileUtilities = "Utilities"+defaultFileExt;
	    try {
	    		fileH= new FileH(fileUtilities,false);
	    } catch (Exc e) {
	    	isUtilityImport = false;
	    	System.out.println("** Import of Utilities File failed\n"+e.print()+"\n");
		    System.out.println("You may continue the session without using utilities.");
	    }
	    try {
	    	nextChar();
	    }
		catch (Exception e) {
			// no exception expected!!
		}
	  }
	  
	  public IOH(String fileInName) throws Exc {
		  try {
			  reader = new BufferedReader(new FileReader(fileInName));
		  }
		  catch (Exception e) {
			  throw new Exc_Assembler(ErrorType.WRONG_FILE, e.getMessage()+
        			  " - reading data from FileIn failed");			  
		  }
	  // use default buffer size
	    line = "";
	    iLine = 1;
	    look = ' ';
	    isStandardInput=true;
	    importError=false;
	    echo= true;
	    isFileIn=true; 
	    nUtilFuncts=0; nUtilHist=0;
	    isUtilityImport = false;
	    try {
	    	nextChar();
	    }
		catch (Exception e) {
			// no exception expected!!
		}
	  }
	  
	  public void setPrompt(boolean p ) {
		  if ( isStandardInput )
			  OutputH.setPrompt(p);
	  }
	  
	  public int nUtil () {
		  return nUtilFuncts;
	  }
	  
	  public void importFile(boolean isEcho ) throws Exc {
			System.out.print("Enter file name (path w.r.t the current working directory or full path):\n.. ");
			try {
				String fileName = reader.readLine();
				fileH= new FileH(fileName,isEcho);
			}
			catch (Exc e ) {
				throw e;
			}
			catch (Exception e) {
				// no exception should arise
			}
	  }

	  public void saveFile() throws Exc {
			System.out.print("Enter file name (path w.r.t the current working directory or full path):\n.. ");
	        try {
				String fileName = reader.readLine();
	            File file = new File(fileName);
	            BufferedWriter output = new BufferedWriter(new FileWriter(file));
	            output.write("/* CalcuList Session of "+(new Date()).toString()+" */\n");
				for ( int i=nUtilHist; i < History.nCommand(); i++ ) 
					output.write(History.command(i)+"\n");
	            output.close();
	          } catch ( Exception e ) {
	        	  throw new Exc_Assembler(ErrorType.WRONG_FILE, e.getMessage()+
	        			  " - session not saved!");
	          }
			System.out.println("** Session saved **");
	        
	  }

	 public  void setImportError() {
		  importError=true;
	  }
	 
	 public void setExecLine( String command ) {
		 System.out.println(command);
		 look=' ';
		 line = command+line.substring(iLine-1);
		 iLine=0;
	 }
  
	  /**
	   * @return the current lookahead character
	   */
	  public char currChar() {
	   return look;
	  }

	  /** Check whether the command halt has been issued inside an imported file */
	  public void checkHalt() {
		  if ( !isStandardInput ) {
			  System.out.println("** Import ended by 'halt' **");
			  int k = History.nCommand()-previous_nHist;
			  System.out.println("-- "+k+" imported commands\n");
			  if ( importError )
					System.out.println("** Errors in the imported file ** \n");
		  }
			  
	  }
	  
	  /** Read the next character in the stream. */
	  public void nextChar() throws Exc {
		  if ( isFileIn ) {
			  if ( line==null ) 
				  look=(char)0;
			  else
				  if ( iLine == line.length() ) {
					  	look = ' ';
					  	iLine++;				  
				  }
				  else
					  if ( iLine < line.length() ) {
						  look = line.charAt(iLine);
						  iLine++;
					  }
					  else { // iLine > line.length()
							try {
								line = reader.readLine();
							}
							catch (Exception e) {
								System.out.println(e.getMessage());
								// no exception expected!!
							}
							if (line==null) 
								look=(char)0; // end file
						   iLine=0;
					  }					  
			  return;
		  }
	  	  if ( iLine == line.length() ) {
			  	look = ' ';
			  	iLine++;
		  }
		  else
			  if ( iLine > line.length() ) {
				if ( !isStandardInput && !isFileIn && fileH.end() ) {
					isStandardInput=true;
					System.out.println("** Import ended **");
					setPrompt(true);
					fileH.close();
					if ( importError )
						System.out.println("** Errors in the imported file ** \n");
					if ( isUtilityImport ) {
						nUtilFuncts = SymbH.nFuncts();
						nUtilHist = History.nCommand();
						if (importError )
							System.out.println("** It may be convenient to restart the session with an empty utility file, "
									+ "to import the utility file with wrong definitions and to correct them ** ");
					}
					if (!echo) {
						int k = History.nCommand()-previous_nHist;
						if ( k == 0 )
							  System.out.println("-- 0 imported commands\n");
						else 
							  System.out.println("-- to list the "+k+
									  " imported commands, type the service command \"!history;\"\n");
					}
				    isUtilityImport = false;						
				}
				if ( isStandardInput ) {
					OutputH.prompt();
					try {
						line = reader.readLine();
					}
					catch (Exception e) {
						// no exception expected!!
					}
				}
				else { // import file
						line=fileH.currLineF(); 
						if ( echo )
							System.out.println(line);
						fileH.nextLineF();;
					}
		  		iLine = 0;
	 		}
			  else {
				  look = line.charAt(iLine);
				  iLine++;
			  }
	  }


	  /** Skip over leading white space.
	   */
	  public void skipWhite() {
		  while ( Character.isWhitespace(look) ) 
			  try{
	      		 nextChar();
			  }
			catch (Exception e) {
				// no exception expected!!
			}
	  }


	  /** Recognize whether look is an alpha character.*/
	  public boolean isLetter() {
	   	return ( 'A' <= look && look <= 'Z' ) || ('a' <= look && look <= 'z');
	  }

	  /** Recognize whether look is a decimal digit.*/
	  public boolean isDigit() {
	   	return Character.isDigit(look);
	  }

	    /** Recognize whether look is an alphanumeric character.*/
	  public boolean isLetterOrDigitOrUnderscore() {
	   	return isLetter() || isDigit() || look == '_';
	  }
	  
	  public void errorCursor(  ) {
		  OutputH.errorCursor(iLine);
	}

	  public void error( Exc e ) {
		  OutputH.error(e);
	  }

	  public void errorNoSkip( Exc e ) {
		  OutputH.errorNoSkip(e);
	  }

	  public void error( Exc e, String s ) {
		  OutputH.error(e,s);
	  }

	  public void print( String s ) { 	
		  System.out.print(s);
	  }
	  
	  public void bye() {
		  OutputH.bye();
	  }

	 static void printError (Exc e ) {
			System.out.println(">>***Error Type: "+ e.print() );
		}

		static void checkArgs(String[] args ) throws Exc {
			if ( args.length > 0  ) {
				int i = 0;
				do {
					if ( i+1 >= args.length)
						throw new Exc_IOH(ErrorType.WRONG_ARGS_NUM, 
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
					  case EXECCS: Exec.modSizeCS(setInt(k,b.name()));
					              return;
					  case EXECMS: Exec.modSizeMS(setInt(k,b.name())); return;
					  case EXECOS: Exec.modSizeOS(setInt(k,b.name())); return;
					  }
			  throw new Exc_IOH(ErrorType.WRONG_ARG_NAME, 
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
		
		class FileH {
			DataInputStream in;
			BufferedReader br;
			String lineF;
			FileH ( String fileName,boolean isEcho ) throws Exc {
				try {
					previous_nHist=History.nCommand();
					File file = null;
					FileInputStream fstream=null;
					try {
						file = new File(fileName);
						fstream = new FileInputStream(file);
					} 
					catch ( Exception e1 ) {
						int n = fileName.length();
						if ( n > 3 && defaultFileExt.equals(fileName.substring(n-defaultFileExt.length())))
								throw e1;
						else
								try {
									System.out.println("No file \""+fileName+
											"\" found - searching for such file with default extension \""+
											defaultFileExt+"\"");
									fileName +=defaultFileExt;
									file = new File(fileName);
									fstream = new FileInputStream(file);							
								} catch ( Exception e ) {
									System.out.println("No file \""+fileName+
											"\" found ");
									throw new Exc_Assembler(ErrorType.WRONG_FILE, e.getMessage());
								}					
					}
					in = new DataInputStream(fstream);
					br = new BufferedReader(new InputStreamReader(in));
					System.out.println("** Importing file \""+file.getCanonicalPath()+"\"  **");
					lineF = br.readLine();
					isStandardInput=false; importError=false; echo=isEcho;
					line = ";"; iLine = 0; look = ' '; 
				}	catch (Exception e) {
						isStandardInput= true; importError=true;
						throw new Exc_Assembler(ErrorType.WRONG_FILE, e.getMessage());
					}						
			}
			boolean end() {
				return lineF==null;
			}
			void close() throws Exc {
				try {
					in.close();
				}
				catch (Exception e) {
					isStandardInput= true; importError=true;
					throw new Exc_Assembler(ErrorType.WRONG_FILE, e.getMessage());
				}
			}
			String currLineF() {
				return lineF;
			}
			void nextLineF () throws Exc {
				try {
					lineF = br.readLine();
				}
				catch (Exception e) {
					isStandardInput= true; importError=true;
					throw new Exc_Assembler(ErrorType.WRONG_FILE, e.getMessage());
				}
			}
		}

	} // end class IOH