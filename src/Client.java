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
import javax.crypto.spec.SecretKeySpec;


public class Client {
	
	private KeyPair keypair;
	
	private Messaging multicast;
	
	private byte[] messageReceived;
	
	private int clientId;
	
	private boolean terminateFlag = false;
	
	private SecretKey dek;
	
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

		generateKeys();
		
		multicast = new Messaging(clientId);
		multicast.initialize();
		
		ReceiveThread recv = new ReceiveThread();
		recv.start();

		SendThread snd = new SendThread();
		snd.start();
		
		/*
		
		messageReceived = server.getEncrMessage();
		try {
			decryptMessageAsymmetric(messageReceived);
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
		

	}
	
	
	private void decryptMessageAsymmetric(byte[] message) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] msg;
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, keypair.getPrivate());
		
		msg = cipher.doFinal(message);
		
		System.out.println("PLAIN MSG " + byteToString(msg));
		
	}
	
	
	private boolean generateKeys() {
		KeyPairGenerator keygen;
		//Generation of asymmetric key pairs
		try {
			keygen = KeyPairGenerator.getInstance("RSA");
			keygen.initialize(512);
			keypair = keygen.generateKeyPair();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to generate keys!!");
			return false;
		}
		System.out.println("Generated public key: " + byteToString(keypair.getPrivate().getEncoded()));
		System.out.println("Generated private key: " + byteToString(keypair.getPublic().getEncoded()));
		return true;
	}
	
	
	private String byteToString(byte[] arg) {
		return Base64.getEncoder().encodeToString(arg);
	}

	
	class ReceiveThread extends Thread {

		public void run() {
			
			Packet incomingPacket;
			
			while(!terminateFlag) {
				
				incomingPacket = multicast.receiveMessage();
				//Valid and clear/decryptable message 
				if(incomingPacket.isValid()) {
					if(incomingPacket.getType() == 0) {
						if (incomingPacket.getSenderId() == 0) {
							System.out.println("Server ##" + ": " + incomingPacket.getMessage());
						} else {
							System.out.println("Client #" + incomingPacket.getSenderId() + ": " + incomingPacket.getMessage());
						}
						
						
					}
					else {
						if (incomingPacket.getSenderId() == 0) {
							byte[] dekIncomed;
							byte[] dekEncrypted;
							System.out.println("Something Received from server");
							dekIncomed = incomingPacket.getMessage().getBytes();
							dekEncrypted = multicast.decryptAsymmetric(dekIncomed, keypair.getPrivate());
							dek = new SecretKeySpec(dekIncomed, "AES");
						}
					}
				}
				//Cannot decrypt
				else {
					System.out.println("Unable to decrypt: " + incomingPacket.getMessage());
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
				
	    		if(msg.equals("EXIT")) {
	    			terminateFlag = true;
	    			System.out.println("Closing client process");
	    		}
	    		else if(msg.equals("JOIN")) {
	    			System.out.println("Trying to join the group...");
	    			multicast.sendMessage(1, keypair.getPublic().getEncoded(), null);
	    			System.out.println("###########  "+byteToString(keypair.getPublic().getEncoded()));
	    		}
	    		else if(msg.equals("LEAVE")) {
	    			multicast.sendMessage(2, msg.getBytes(), null);
	    			System.out.println("Trying to leave the group...");
	    		}
	    		else {
	    			multicast.sendMessage(0, msg.getBytes(), null);
	    		}
			}

		}
	}	

	
	  

}