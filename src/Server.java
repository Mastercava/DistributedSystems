import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;



public class Server {
	
	private final int serverId = 0; 
	
	private Messaging multicast;
	private FlatTable flatTable;
	private ArrayList<Integer> clientsConnected;
	private KeyPair keypair;
	private Key dek;
	
	private byte[] encrMessage; //for test
	
	
	public byte[] getEncrMessage() {
		return encrMessage;
	}

	public Server() {
		
		multicast = new Messaging(serverId);
		multicast.initialize();
		
		generateKeys();
		
		flatTable = new FlatTable();
		clientsConnected = new ArrayList<Integer>();
		
	}
	
	public static void main(String[] args) {
				
		Server server = new Server();
		
		server.startReceiving();

	}
	
	public void startReceiving() {
		
		Packet incomingPacket;
		
		List<Key> keys = new ArrayList<Key>();
		keys.add(keypair.getPrivate());
		
		while(true) {
			
			incomingPacket = multicast.receiveMessage(keys);
			if(incomingPacket.isValid() && incomingPacket.getSenderId() != 0) {
				switch (incomingPacket.getType()) {
				
					case 0:
						System.out.println("Client #" + incomingPacket.getSenderId() + ": " + incomingPacket.getMessage());
						break;
					case 1:
						byte[] encodedKey = incomingPacket.getData();

											
						PublicKey clientPublicKey = null;
						try {
							clientPublicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encodedKey));
						} catch (Exception e) {
							e.printStackTrace();
							
						}
						System.out.println("#########  " + byteToString(clientPublicKey.getEncoded()));
						
						join(incomingPacket.getSenderId(), clientPublicKey);
						break;
					case 4:
						int senderId = incomingPacket.getSenderId();
						if (clientsConnected.contains(senderId)) {
							System.out.println("Client # " + senderId + ": Leave request accepted "  );
							leave(senderId);
						}
						
						
					
					
				}
			}	
		}
		
	}
	
	public synchronized void join(int clientId, Key clientPublicKey) {
		
		if(!clientsConnected.contains(clientId)) {
			
			//Add the client to the list of clients connected
			boolean connClients = clientsConnected.isEmpty();
			clientsConnected.add(clientId);
			
			String s = "Client #" + clientId + " joined the group";
			System.out.println(s);
			Key newDek = flatTable.changeDek();
			multicast.sendMessage(2, Base64.getEncoder().encode(newDek.getEncoded()), clientPublicKey);
			System.out.println("DEKKKKK     "  + Base64.getEncoder().encodeToString(newDek.getEncoded()));
			for (Key k: flatTable.joinGroup(clientId)) {
				multicast.sendMessage(3, Base64.getEncoder().encode(k.getEncoded()), clientPublicKey);
				
			}
			
			System.out.println("KEYS SENT");
			if (!connClients)
				sendNewDek(clientId,newDek);
			
			
			printConnectedClients();
			
			
		} else {
			multicast.sendMessage(0, "Host already connected".getBytes() , clientPublicKey);
		}
		
	}
	
	private void sendNewDek(int clientId, Key newDek) {
		boolean[] bitId = Utilities.idToBitArray(clientId); 
		List<Key> keys;
		
		//invert the value of bits of clientId
		for (int i = 0; i < bitId.length; i++) {
			bitId[i] = !bitId[i];
		}
		
		keys = flatTable.getStableKeys(bitId);
		for (Key k : keys) {
			System.out.println("Trying to send new DEK");
			multicast.sendMessage(2,  Base64.getEncoder().encode(newDek.getEncoded()),k );
		}
	}

	public synchronized void leave(int clientId) {
		
		
		if(clientsConnected.contains(clientId)) {
			/*
			HashMap<Integer[], SecretKey> newKeys;
			newKeys = flatTable.leaveGroup(clientId);
			
			*/
			
			clientsConnected.remove(clientsConnected.indexOf(clientId));
			String s = "Client #" + clientId + " left the group";
			System.out.println(s);
			dek = flatTable.changeDek();
			//message for new dek
			for (int client : clientsConnected) {
				boolean[] binId = Utilities.idToBitArray(client);
				ArrayList<Key> keys = flatTable.getStableKeys(Utilities.getBinaryNeg(binId));
				for (Key k : keys) {
					multicast.sendMessage(2, Base64.getEncoder().encode(dek.getEncoded()),k);
					System.out.println("NEW DEK SENT");
				}
				
			}
			
			//generate new keks
			Key[][] oldKeks = flatTable.getTable();
			flatTable.changeKeys(Utilities.idToBitArray(clientId));
			System.out.println("New keks generated");
			Key[][] newKeks= flatTable.getTable(); 
			byte[] newKeyEncrypted;
			
			
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < flatTable.getBitsNeeded(); j++) {
					if (!(oldKeks[i][j].equals(newKeks[i][j]))) {
						System.out.println("TRY TO SEND A NEW KEK");
						byte[] keyToSend = newKeks[i][j].getEncoded();
						newKeyEncrypted = Messaging.encryptSymmetric(Base64.getEncoder().encode(keyToSend), oldKeks[i][j]);
						byte[] newMessage = new byte[newKeyEncrypted.length + 1];
						newMessage[0] = Settings.CHECK_CODE;
						newMessage = Arrays.copyOfRange(newKeyEncrypted,1,newKeyEncrypted.length);
						multicast.sendMessage(Utilities.NEW_KEK_MSG, newMessage, dek);
						System.out.println("New KEK SENT");
						
							
					} else { 
						System.out.println("THE TWO KEYS ARE THE SAME");
					}
				}
			}
			
			
			
			
			printConnectedClients();
			
		}
		
	}

	
	private void printConnectedClients() {
		String msg;
		if(clientsConnected.size() == 0) msg = "Currently there are no clients connected";
		else {
			msg = "Currently connected clients: ";
			for(int id : clientsConnected) msg += id + "  ";
		}
		System.out.println(msg);
	
	}
	
	private boolean generateKeys() {
		KeyPairGenerator keygen;
		//Generation of asymmetric key pairs
		try {
			keygen = KeyPairGenerator.getInstance("RSA");
			keygen.initialize(Settings.RSA_KEYSIZE);
			keypair = keygen.generateKeyPair();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to generate keys!!");
			return false;
		}
		System.out.println("Keypair generated succesfully");
		Settings.setServerPublicKey(keypair.getPublic()); //USELESS!!!
		System.out.println("Keys long: " + keypair.getPrivate().getEncoded().length + " , " + keypair.getPublic().getEncoded().length);
		return true;
	}
	
	private String byteToString(byte[] arg) {
		return Base64.getEncoder().encodeToString(arg);
		//return new String(arg);
	}
	
}
