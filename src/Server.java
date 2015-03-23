import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
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
						join(incomingPacket.getSenderId(), null);
					}
					else if(incomingPacket.getType() == 2) {
						leave(incomingPacket.getSenderId());
					}
				}
			}	
		}
		
	}
	
	public void join(int clientId, Key key) {
		
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
			
			System.out.println("Client #" + clientId + " joined the group");
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
			System.out.println("Client #" + clientId + " left the group");
			printConnectedClients();
		}
		
	}

	
	/*
	private void initialSendDek(Key key, byte[] message) {
		
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
}
