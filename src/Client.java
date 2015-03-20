import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


public class Client {
	
	private KeyPair keypair;
	private PublicKey publKey;
	private PrivateKey privKey;
	
	private Messaging multicast;
	
	private byte[] messageReceived;
	
	private int clientId;
	
	private boolean terminateFlag = false;
	
	public static void main(String[] args) {
		
		//Selects the id for the client 
		System.out.print("Insert an ID for this client: ");
		Scanner reader = new Scanner(System.in);
		int assignedId = reader.nextInt();
		
		//Creates a client instance
		Client client = new Client(assignedId);
		
		
		
	}
	
	
	public Client(int assignedId) {
		
		clientId = assignedId;

		KeyPairGenerator keygen;
		SecureRandom random;
		//generation of asymmetric key pairs
		try {
			keygen = KeyPairGenerator.getInstance("RSA");
			keygen.initialize(2048);
			keypair = keygen.generateKeyPair();
			privKey = keypair.getPrivate();
			publKey = keypair.getPublic();
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Key Pairs Generated");
		
		multicast = new Messaging(clientId);
		multicast.initialize();
		
		ReceiveThread recv = new ReceiveThread();
		recv.start();

		SendThread snd = new SendThread();
		snd.start();
		
		Server server = new Server();
		server.addClient(clientId, publKey);
		
		messageReceived = server.getEncrMessage();
		try {
			decryptMessageAsymmetric(messageReceived);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}
	
	/*
	 * Getter public key
	 */
	public Key getPublicKey() {
		return publKey;
	}
	
	private void decryptMessageAsymmetric(byte[] message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] msg;
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privKey);
		
		msg = cipher.doFinal(message);
		System.out.println("PLAIN MSG " + ByteToString(message));
		
	}
	
	private String ByteToString(byte[] arg) {
		return Base64.getEncoder().encodeToString(arg);
	}

	
	class ReceiveThread extends Thread {

		public void run() {
			
			Packet incomingPacket;
			
			while(!terminateFlag) {
				
				incomingPacket = multicast.receiveMessage();
				if(incomingPacket.getType() == 0) {
					System.out.println("Client #" + incomingPacket.getSenderId() + ": " + incomingPacket.getMessage());
				}
				else {
					//TODO
				}
				
			}

		}
	}

	
	class SendThread extends Thread {

		public void run() {
			
			Scanner reader = new Scanner(System.in);
			System.out.println("Write a message to send to the group and press enter");

			while(!terminateFlag) {
				String msg = reader.next();
				
	    		if(msg.equals("exit")) {
	    			terminateFlag = true;
	    			System.out.println("Closing client process");
	    		}
	    		else {
	    			multicast.sendMessage(0, msg.getBytes());
	    		}
			}

		}
	}	

	
	  

}