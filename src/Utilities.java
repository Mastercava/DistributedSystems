import java.security.Key;
import java.util.Base64;

public class Utilities {
	
	
	public static String keyToString(Key key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
	
	public static boolean[] idToBitArray(int clientId) {
			
		boolean[] bits = new boolean[7];
	    for (int i = 6; i >= 0; i--) {
	        bits[i] = (clientId & (1 << i)) != 0;
	    }
	    return bits;
		    
	}
	

}
