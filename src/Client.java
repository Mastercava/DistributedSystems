import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import java.net.UnknownHostException;

import java.util.Scanner;


public class Client {
	
	private KeyPair keypair;
	private PublicKey publKey;
	private PrivateKey privKey;
	
	private Messaging multicast;
	
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
			keygen = KeyPairGenerator.getInstance("DSA","SUN");
			random = SecureRandom.getInstance("SHA1PRNG", "SUN");
			keygen.initialize(1024,random);
			keypair = keygen.generateKeyPair();
			privKey = keypair.getPrivate();
			publKey = keypair.getPublic();
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchProviderException e1) {
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

	}
	
	/*
	 * Getter public key
	 */
	public Key getPublicKey() {
		return publKey;
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