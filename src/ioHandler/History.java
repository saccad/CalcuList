package ioHandler;

import error.Exc;
import java.util.ArrayList;

public class History {
	private static boolean histOn = false;
	private static int histInitCapacity = 200;
	private static ArrayList<String> hist = new ArrayList<String>(histInitCapacity);
	private static String currComm;
	public static void startCommand () {
		currComm=""; histOn = true;
	}

	public static void endCommand() throws Exc {
		if ( histOn ) {
			hist.add(currComm);
			histOn=false;
		}
	}

	public static void histOff() {
			histOn=false;
		}

	public static void histOn() {
		histOn=true;
	}
	public static void addSubStr( String ss ) {
		if ( histOn )
			currComm += ss;
	}
	public static void addChar( char c ) {
		if ( histOn )
			currComm += c;
	}
	public static int nCommand() {
		return hist.size();
	}
	public static String command( int i ){
		return hist.get(i);
	}

}
