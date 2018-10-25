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


