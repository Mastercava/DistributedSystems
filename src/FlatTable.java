import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


public class FlatTable {
	
	private final int SUPPORTED_HOSTS = 8;
	private final String ENCRYPTION_ALGORITHM = "DES";
	private int bitsNeeded;
	
	private SecretKey[][] flatTable;
	private SecretKey dek;
	KeyGenerator keygen;
	
	public FlatTable() {
		
		bitsNeeded = (int) (Math.log(SUPPORTED_HOSTS) / Math.log(2));
		flatTable = new SecretKey[2][bitsNeeded];
		
		System.out.println("Flat table with " + bitsNeeded + " bits created, supporting " + SUPPORTED_HOSTS + " hosts");
		try {
			keygen = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		System.out.println("Using encryption algorithm \"" + ENCRYPTION_ALGORITHM + "\"");
		dek = generateKey();
		System.out.println("DEK generated: " + keyToString(dek));
		
		initializeTable();
		
	}
	
	//Fills flat table with initial keys
	public void initializeTable() {
		int i, j;
		for (i = 0; i<2; i++) {
			for (j=0; j<bitsNeeded; j++) {
				flatTable[i][j] = generateKey();
				System.out.print(keyToString(flatTable[i][j]) + "   ");
			}
			System.out.println("");
		}
		
	}
	
	public void joinGroup(int clientId) {
		
	}
	
	public void leaveGroup(int clientId) {
		
	}
	
	private SecretKey generateKey() {
		SecretKey key = keygen.generateKey();
		return key;
	}
	
	private String keyToString(SecretKey key) {
		return Base64.getEncoder().encodeToString(key.getEncoded());
	}
	
	private boolean[] idToBitArray(int clientId) {
		
		boolean[] bits = new boolean[7];
	    for (int i = 6; i >= 0; i--) {
	        bits[i] = (clientId & (1 << i)) != 0;
	    }
	    return bits;
	    
	}

}
