import java.security.Key;
import java.util.Base64;

public class Utilities {
	
	public static int LEAVE_MSG = 4;
	public static int NEW_KEK_MSG = 5;
	
	
	public static String keyToString(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
	
	public static boolean[] idToBitArray(int clientId) {
			
		boolean[] bits = new boolean[Settings.BIT_NUMBER];
	    for (int i = Settings.BIT_NUMBER - 1; i >= 0; i--) {
	        bits[i] = (clientId & (1 << i)) != 0;
	    }
	    System.out.println("NEW BIT ARRAY");
	    for (int i = Settings.BIT_NUMBER - 1; i >= 0; i--) {
	    	if (bits[i]) {
	    		System.out.print("1");
	    	} else {
	    		System.out.print("0");
	    	}
	    	
	    }
	    System.out.print("\n");
	    
	    return bits;
		    
	}
	
	public static boolean[] getBinaryNeg(boolean[] bitId) {
		for (int i = 0; i < bitId.length; i++) {
			bitId[i] = !bitId[i];
		}
		 for (int i = Settings.BIT_NUMBER - 1; i >= 0; i--) {
		    	if (bitId[i]) {
		    		System.out.print("1");
		    	} else {
		    		System.out.print("0");
		    	}
		    	
		    }
		    System.out.print("\n");
		return bitId;
	}
	

}
