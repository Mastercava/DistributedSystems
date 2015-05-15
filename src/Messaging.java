import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.Key;
import java.util.List;

import javax.crypto.Cipher;


public class Messaging {

	final static String GROUP_ADDR = "228.5.6.7";
	final static int PORT = 6789;
	final static int BUFFER_SIZE = 256;
	
	private static InetAddress group;
	private static MulticastSocket socket;
	
	private int senderId;
	
	public Messaging(int assignedId) {
		senderId = assignedId;
	}
	
	public boolean initialize() {
		try {
			
			group = InetAddress.getByName(GROUP_ADDR);

			socket = new MulticastSocket(PORT);
			socket.joinGroup(group);

		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
		try {
			System.out.println("Host " + InetAddress.getLocalHost().getHostAddress() + " connected succesfully to group " + GROUP_ADDR);
		} catch (Exception e) {
			System.out.println("Client connected locally to group " + GROUP_ADDR);
		}
		
		return true;
	}
	
	public boolean sendMessage(int type, byte[] data, Key encryptionKey) {
		
		byte[] newData = new byte[data.length+3];
		newData[0] = (byte) senderId;
		newData[1] = (byte) type;
		newData[2] = Settings.CHECK_CODE;
		System.arraycopy(data, 0, newData, 3, data.length);
		
		//If key provided, encrypt message
		if(encryptionKey != null) {
			if (encryptionKey.getAlgorithm().equals(Settings.ENCRYPTION_ALGORITHM)) {
				newData = encryptSymmetric(newData, encryptionKey);
			} else {
				newData = encryptAsymmetric(newData, encryptionKey);
			}
			
			System.out.println("HOST #" + senderId + " SENDING ENCRYPTED DATA!!");
		}
		
		DatagramPacket packet = new DatagramPacket(newData, newData.length, group, PORT);

		try {
			socket.send(packet);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	public static byte[] encryptSymmetric(byte[] data, Key key) {
		System.out.println("ORIGINAL DATA LENGHT: " + data.length);
		
		byte[] encryptedData = null;
		
		try {
			Cipher cipher = Cipher.getInstance(Settings.ENCRYPTION_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			encryptedData = cipher.doFinal(data);
			
		} catch(Exception e) {
			e.printStackTrace();
			encryptedData = data;
		}
		
		System.out.println("ENCRYPTED DATA LENGHT: " + encryptedData.length);
		
		return encryptedData;
	}

	public Packet receiveMessage(List<Key> keys) {
		
		byte[] buf = new byte[BUFFER_SIZE];

		try {

			DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
			socket.receive(msgPacket);
			Packet pkt = new Packet(msgPacket);
			
			//Decrypts if necessary
			if(!pkt.isValid() && keys != null) {
				pkt.tryDecryption(keys);
			}
			
			return pkt;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		
			

	}
	
	
	
	static public byte[] encryptAsymmetric(byte[] data, Key key) {
		
		System.out.println("ORIGINAL DATA LENGHT: " + data.length);
		
		byte[] encryptedData = null;
		
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			encryptedData = cipher.doFinal(data);
			
		} catch(Exception e) {
			e.printStackTrace();
			encryptedData = data;
		}
		
		System.out.println("ENCRYPTED DATA LENGHT: " + encryptedData.length);
		
		return encryptedData;
	}
	

	static public byte[] decryptAsymmetric(byte[] encryptedData, Key key) throws Exception {
		
		byte[] data = null;
		
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, key);
		data = cipher.doFinal(encryptedData);
		
		
		return data;
	}
	
	public boolean sendInitialMessage(int type, byte[] data, Key publicKey) {
		byte[] encryptedMessage = new byte[publicKey.getEncoded().length];
		
		System.out.println("########  " + publicKey.getAlgorithm());
		
		try {
			
			Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE,publicKey);
			encryptedMessage = cipher.doFinal(data);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 
		}
		
		System.out.println("Encryption with RSA done");
		byte[] newData = new byte[encryptedMessage.length+3];
		newData[0] = (byte) senderId;
		newData[1] = (byte) type;
		newData[2] = Settings.CHECK_CODE;
		
		System.arraycopy(encryptedMessage, 0, newData, 3, data.length);
		
		DatagramPacket packet = new DatagramPacket(newData, newData.length, group, PORT);

		try {
			socket.send(packet);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		
		
		
		
		
		
		
		return true;
	}

	public static  byte[] decryptSymmetric(byte[] encryptedData, Key k) throws Exception {
		byte[] data = null;


		Cipher cipher = Cipher.getInstance(Settings.ENCRYPTION_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, k);
		data = cipher.doFinal(encryptedData);


		return data;
		
	}
	
}
