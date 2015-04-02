import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


public class FlatTable {
	
	private final int SUPPORTED_HOSTS = 8;
	private int bitsNeeded;
	
	private SecretKey[][] flatTable;
	private SecretKey dek;
	KeyGenerator keygen;
	
	public FlatTable() {
		
		bitsNeeded = (int) (Math.log(SUPPORTED_HOSTS) / Math.log(2));
		flatTable = new SecretKey[2][bitsNeeded];
		System.out.println("Flat table with " + bitsNeeded + " bits created, supporting " + SUPPORTED_HOSTS + " hosts");
		try {
			keygen = KeyGenerator.getInstance(Settings.ENCRYPTION_ALGORITHM);
			keygen.init(128);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		System.out.println("Using encryption algorithm \"" + Settings.ENCRYPTION_ALGORITHM + "\"");
		dek = generateKey();
		System.out.println("DEK generated: " + Utilities.keyToString(dek));
		
		initializeTable();
		
	}
	
	//Fills flat table with initial keys
	public void initializeTable() {
		int i, j;
		for (i = 0; i<2; i++) {
			for (j=0; j<bitsNeeded; j++) {
				flatTable[i][j] = generateKey();
				System.out.print(Utilities.keyToString(flatTable[i][j]) + "   ");
			}
			System.out.println("");
		}
		
	}
	
	public ArrayList<Key> joinGroup(int clientId) {
		boolean[] binValues = Utilities.idToBitArray(clientId);
		ArrayList<Key> keys;
		//seleziona le keks da inviare al client
		keys = selectCorrectKeks(binValues);
			
		return keys;
		
		
	}
	
	private ArrayList<Key> selectCorrectKeks( boolean[] values ) {
		ArrayList<Key> keys = new ArrayList<Key>();
		
		for (int i= 0; i< bitsNeeded; i++) {
			if (!values[i]) {
				keys.add(flatTable[0][i]);
			} else {
				keys.add(flatTable[1][i]);
			}
			
		}
		
		return keys;
	}
	
	public Key changeDek() {
		dek = keygen.generateKey();
		//for the dek used values of keyId -1 ,-1
		return dek;
	}
	
	
	
	public void changeKeys(boolean[] value) {
		Integer[] keyId = new Integer[2]; 
		HashMap<Integer[],SecretKey> keyGenerated;
		keyGenerated = new HashMap<Integer[],SecretKey>();
		ArrayList<Key >keysGenerated = new ArrayList<Key>(); 
		for (int i = 0; i < bitsNeeded; i++) {
			if (!value[i]) {
				flatTable[0][i] = keygen.generateKey();
				keyId[0] = i;
				keyId[1] = 0;
				keyGenerated.put(keyId, flatTable[0][i]);
				keysGenerated.add(flatTable[0][i]);
				System.out.println("Key " + i + "," + 0 +" updated!");

			} else {
				flatTable[0][i] = keygen.generateKey();
				keyId[0] = i;
				keyId[1] = 1;
				keyGenerated.put(keyId, flatTable[1][i]);
				keysGenerated.add(flatTable[1][i]);
				System.out.println("Key " + i + "," + 1 +" updated!");
				
			}
		}
		
		
		
		
	}
	
	public ArrayList<Key> getStableKeys(boolean[] value) {
		ArrayList<Key> toReturn = new ArrayList<Key>();
		for (int i= 0; i<bitsNeeded; i++) {
			if (!value[i]) {
				toReturn.add(flatTable[0][i]);
			} else {
				toReturn.add(flatTable[1][i]);
			}
		}
		
		System.out.println("STABLE KEYS DIMENSION   "  + toReturn.size());
		
		return toReturn;
	}
	
	
	
	
	
	private SecretKey generateKey() {
		SecretKey key = keygen.generateKey();
		return key;
	}
	
	public Key[][] getTable() {
		return flatTable;
	}

	public int getBitsNeeded() {
		
		return bitsNeeded;
	}
	
	
	
	
	

}
