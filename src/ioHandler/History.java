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
	public static int nCurrComm( ) {
			return currComm.length();
	}
	public static int nCommand() {
		return hist.size();
	}
	public static String currCommand( ){
		return currComm;
	}
	public static String command( int i ){
		return hist.get(i);
	}

}
