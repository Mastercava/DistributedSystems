import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.HashMap;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;



public class Server {

	final static String OWN_ADDR = "192.168.1.1";
	final static String GROUP_ADDR = "228.5.6.7";
	final static int PORT = 6789;
	final static int BUFFER_SIZE = 256;
	
	private HashMap<String, SecretKey> table;
	private SecretKey[][] hashTable;
	private SecretKey dek;
	
	private static InetAddress group;
	private static MulticastSocket socket;
	
	public Server() {
		
		table = new HashMap<String, SecretKey>();
		KeyGenerator keygen = null;
		
		hashTable = new SecretKey[2][5];
		
		try {
			keygen = KeyGenerator.getInstance("DES");
			keygen.init(56);
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		dek = keygen.generateKey();
		
		
		try {
			
			group = InetAddress.getByName(GROUP_ADDR);
			socket = new MulticastSocket(PORT);
			socket.joinGroup(group);
			
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}
		
		try {
			System.out.println("Server " + InetAddress.getLocalHost().getHostAddress() + " connected succesfully to group " + GROUP_ADDR);
		} catch (Exception e1) {
			System.out.println("Server locally connected to group " + GROUP_ADDR);
		}
		
		generateKeys();
	}
	
	private void generateKeys() {
		SecretKey key;
		KeyGenerator keygen = null;
		String index;
		try {
			keygen = KeyGenerator.getInstance("DES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		/*for (int i=0; i<2;i++) {
			for (int j=0; j<3;j++) {
				key = keygen.generateKey();
				index = "" + i + "" + j;
				table.put(index, key);
				
			}
		}*/
		
		for (int i = 0; i<2;i++) {
			for (int j=0; j<5; j++) {
				hashTable[i][j] = keygen.generateKey();
				
			}
		}
		
		
	}
	
	public void initialCommunication(int id, PublicKey key) {
		
		
		
	}
	
	
	public static void main(String[] args) {
		
		

		
		byte[] buf = new byte[BUFFER_SIZE];
		
		Server server = new Server();
		
		while (true) {
			
			try {
				
				DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
				socket.receive(msgPacket);
				InetAddress senderIp = msgPacket.getAddress();
				String msg = (new String(buf, 0, buf.length)).trim();
				System.out.println(senderIp + ": " + msg);
				
				//Message analisys
				if(msg.equals("join")) {
					
					String tmp = senderIp + " joined the group";
					DatagramPacket packet = new DatagramPacket(tmp.getBytes(), tmp.length(), group, PORT);
		    		try {
						socket.send(packet);
					} catch (Exception e) {
						e.printStackTrace();
					}
		    		
		    	}
				
				buf = new byte[BUFFER_SIZE];
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

	}

}
