import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;



public class Server {
	
	private final int serverId = 0; 
	
	private Messaging multicast;
	private FlatTable flatTable;
	private ArrayList<Integer> clientsConnected;
	private KeyPair keypair;
	
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
		
		while(true) {
			
			incomingPacket = multicast.receiveMessage();
			if(incomingPacket.isValid()) {
				if(incomingPacket.getType() == 0) {
					System.out.println("Client #" + incomingPacket.getSenderId() + ": " + incomingPacket.getMessage());
				}
				else {
					//Client tries to join the group
					if(incomingPacket.getType() == 1) {
						byte[] encodedKey = incomingPacket.getMessage().getBytes();
						System.out.println("key received");
						SecretKey originalKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "RSA");
						join(incomingPacket.getSenderId(), originalKey);
					}
					else if(incomingPacket.getType() == 2) {
						leave(incomingPacket.getSenderId());
					}
				}
			}	
		}
		
	}
	
	public void join(int clientId, SecretKey key) {
		
		if(!clientsConnected.contains(clientId)) {
			/*
			Messaging msg = new Messaging(serverId);
			String str = " ";
			if (!clientsConnected.isEmpty()) {
				if (clientsConnected.get(clientId) == null) {
					str = "Client not connected: Trying a Connection";
					
				} else {
					str = "Client already connected";
				}
			}
			
			msg.sendMessage(0, str.getBytes());
			HashMap<Integer[], SecretKey> newKeys;
			newKeys = flatTable.joinGroup(clientId);
			
	
			//add method to send valures to hashmap
			byte[] encryptedMsg = null;
	
			
			
			Key newDek = flatTable.changeDek();
			newDek = flatTable.changeDek();
			
			initialSendDek(key, Utilities.keyToString(newDek).getBytes());
			
			*/
		
			//add the client to the list of clients connected
			clientsConnected.add(clientId);
			
			String s = "Client #" + clientId + " joined the group";
			System.out.println(s);
			multicast.sendInitialMessage(0, s.getBytes(), key);
			
			printConnectedClients();
			
			
		}
		
	}
	
	public void leave(int clientId) {
		
		if(clientsConnected.contains(clientId)) {
			/*
			HashMap<Integer[], SecretKey> newKeys;
			newKeys = flatTable.leaveGroup(clientId);
			
			*/
			
			clientsConnected.remove(clientsConnected.indexOf(clientId));
			String s = "Client #" + clientId + " left the group";
			System.out.println(s);
			multicast.sendMessage(0, s.getBytes(),null);
			printConnectedClients();
			
		}
		
	}

	
	/*
	private void initialSendDek(Key key, byte[] message) {
		
		/*
		try {
			//encryptedMsg = asymmetricEncrypt(newKeys.toString().getBytes(), key);
			encrMessage = encryptAsymmetric(Base64.getEncoder().encode(message), key);
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Messaging msg = new Messaging(serverId);
		
		//marco controlla qua la send se i parametri sono ok
		msg.sendMessage(1, encrMessage, null);
<<<<<<< HEAD
=======
		*/
		
>>>>>>> origin/master
	}
	*/
	
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
			keygen.initialize(2048);
			keypair = keygen.generateKeyPair();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to generate keys!!");
			return false;
		}
		System.out.println("Keypair generated succesfully");
		Settings.setServerPublicKey(keypair.getPublic());
		return true;
	}
	
}
