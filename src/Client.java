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
import java.util.Scanner;


public class Client {
	
	final static String INET_ADDR = "228.5.6.7";
	final static int PORT = 6789;
	final static int BUFFER_SIZE = 256;
	
	private InetAddress group;
	private MulticastSocket socket;
	
	
	private KeyPair keypair;
	private PublicKey publKey;
	private PrivateKey privKey;
	
	private boolean terminateFlag = false;
	
	public static void main(String[] args) {
		
		Client client = new Client();
		
		
	}
	
	
	public Client() {

		KeyPairGenerator keygen;
		SecureRandom random;

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
		try {

			group = InetAddress.getByName(INET_ADDR);
			socket = new MulticastSocket(PORT);
			socket.joinGroup(group);

		} catch(Exception e) {
			e.printStackTrace();
		}

		System.out.println("Client connected succesfully to group " + INET_ADDR);

		ReceiveThread recv = new ReceiveThread();
		recv.start();

		SendThread snd = new SendThread();
		snd.start();

	}
	
	public Key getPublicKey() {
		return publKey;
	}

	class ReceiveThread extends Thread {

		public void run() {

			byte[] buf = new byte[BUFFER_SIZE];

			while(!terminateFlag) {

				try {

					DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
					socket.receive(msgPacket);
					String msg = new String(buf, 0, buf.length);
					System.out.println("Received message: " + msg);
					buf = new byte[BUFFER_SIZE];

				} catch (IOException e) {
					e.printStackTrace();
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

				System.out.println(msg);
				/*
	    		if(msg == "exit") {
	    			terminateFlag = true;
	    			System.out.println("Closing client process");
	    		}
				 */

				DatagramPacket packet = new DatagramPacket(msg.getBytes(), msg.length(), group, PORT);

				try {
					socket.send(packet);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		}
	}

}