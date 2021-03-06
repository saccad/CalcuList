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

public class FrameDebugHandler {
	static int [] fpEntries;
	static int nFpEntries;
	
	public static void startArray() {
		fpEntries = new int [Exec.MS / 2];
	}
	
	static int isFpEntry( int ind ) {
		for ( int i = nFpEntries-1; i >= 0 ; i-- )
			if ( ind == fpEntries[i] )
				return i;
		return -1;
	}
	static void removeFrame() {
		nFpEntries--;
	}
	static void addFrame( int ind ) {
		fpEntries[nFpEntries]=ind; 
		nFpEntries++; // overflow cannot arise!
	}
	
	static void initFrame (  ) {
		fpEntries[0]=0; 
		nFpEntries = 1;
	}
		
} // end class FrameDebugHandler


