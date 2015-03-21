import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;



public class Server {
	
	private final int serverId = 1; 
	
	private Messaging multicast;
	private FlatTable flatTable;
	private ArrayList<Integer> clientsConnected;
	
	private byte[] encrMessage; //for test
	
	
	public byte[] getEncrMessage() {
		return encrMessage;
	}

	public Server() {
		
		multicast = new Messaging(serverId);
		multicast.initialize();
		
		flatTable = new FlatTable();
		clientsConnected = new ArrayList<Integer>();
		
	}
	
	public static void main(String[] args) {
				
		Server server = new Server();
		FlatTable flatTable = new FlatTable();
		
		server.startReceiving();

	}
	
	public void startReceiving() {
		
		Packet incomingPacket;
		
		while(true) {
			
			incomingPacket = multicast.receiveMessage();
			if(incomingPacket.getType() == 0) {
				System.out.println("Client #" + incomingPacket.getSenderId() + ": " + incomingPacket.getMessage());
			}
			else {
				//TODO
			}
			
		}
		
	}
	
	public void addClient(int clientId, Key key) {
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
		
		//add method to send values to hashmap
		byte[] encryptedMsg;
		try {
			//encryptedMsg = asymmetricEncrypt(newKeys.toString().getBytes(), key);
			encrMessage = asymmetricEncrypt(newKeys.toString().getBytes(), key);
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
		
		
		
		
		
		
		
		
		
		
		clientsConnected.add(clientId);
	}

	private byte[] asymmetricEncrypt(byte[] arg, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
		Cipher cipher = Cipher.getInstance("RSA");
		byte[] toReturn = null;
		cipher.init(Cipher.ENCRYPT_MODE, key);
		try {
			toReturn = cipher.doFinal(arg);
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Plain Message" + arg.toString());
		System.out.println("Encrypted message " + toReturn.toString());
		
		
		
		return toReturn;
	}
}
