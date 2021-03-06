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
	
	private final int serverId = -1; 
	
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
			if(incomingPacket.isValid() && incomingPacket.getSenderId() != serverId) {
				switch (incomingPacket.getType()) {
				
					case 0:	//Normal message
						//System.out.println("Client #" + incomingPacket.getSenderId() + ": " + incomingPacket.getMessage());
						break;
						
					case 1: //Join request from client
						byte[] encodedKey = incomingPacket.getData();

											
						PublicKey clientPublicKey = null;
						try {
							clientPublicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encodedKey));
						} catch (Exception e) {
							e.printStackTrace();
							
						}
						//System.out.println("#########  " + byteToString(clientPublicKey.getEncoded()));
						join(incomingPacket.getSenderId(), clientPublicKey);
						break;
						
					case 4: //Leave request from client
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
				boolean[] binId = Utilities.idToBitArray(clientId);
				ArrayList<Key> keys = flatTable.getStableKeys(Utilities.getBinaryNeg(binId));
				for (Key k : keys) {
					multicast.sendMessage(2, Base64.getEncoder().encode(dek.getEncoded()),k);
				}
				
			}
			
			//generate new keks
			Key[][] oldKeks = flatTable.getOldTable();
			
			flatTable.changeKeys(Utilities.idToBitArray(clientId));
			Key[][] newKeks= flatTable.getNewTable(); 
			byte[] newMessage;
			
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < flatTable.getBitsNeeded(); j++) {
					String oldKey = Base64.getEncoder().encodeToString(oldKeks[i][j].getEncoded());
					System.out.println("OLD KEY " + oldKey );
					
					String newKey = Base64.getEncoder().encodeToString(newKeks[i][j].getEncoded());
					System.out.println("NEW KEY " + newKey ); 
					
					if (!oldKey.equals(newKey)) {
						newMessage = new byte[newKeks[i][j].getEncoded().length + 1];
						byte[] keyToSend = newKeks[i][j].getEncoded();
						newMessage[0] = Settings.CHECK_CODE;
						System.arraycopy(keyToSend, 0, newMessage, 1, keyToSend.length);
						newMessage = Messaging.encryptSymmetric(Base64.getEncoder().encode(newMessage), oldKeks[i][j]);
						multicast.sendMessage(Utilities.NEW_KEK_MSG, Base64.getEncoder().encode(newMessage), dek);
					} else {
						System.out.println("KEY " + i + "," + j + "not sent");
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
		System.out.println("Keys long: " + keypair.getPrivate().getEncoded().length + " , " + keypair.getPublic().getEncoded().length);
		return true;
	}
	
	private String byteToString(byte[] arg) {
		return Base64.getEncoder().encodeToString(arg);
		//return new String(arg);
	}
	
}
