import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


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
	
	public boolean sendKeyMessage(int type, SecretKey key)  {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DatagramPacket dgp = new DatagramPacket(b.toByteArray(),b.toByteArray().length,group,PORT);
		KeyPacket keyPacket = new KeyPacket(dgp, type, key);
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(b);
			oos.writeObject(keyPacket);
			
			oos.flush();
			byte[] buf = b.toByteArray();
			DatagramPacket packet = new DatagramPacket(buf,buf.length,group,PORT);
			socket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return false;
		}
		
		return true;
		
	}
	
	public KeyPacket receiveKeyPacket()  {
		
		DatagramPacket packet;
		byte[] buffer = new byte[BUFFER_SIZE];
	    packet = new DatagramPacket(buffer, buffer.length );
	    KeyPacket keyPacket = null;
	    try {
			socket.receive(packet);
			ByteArrayInputStream b = new ByteArrayInputStream(buffer);
		    ObjectInputStream ois = new ObjectInputStream(b);
			keyPacket = (KeyPacket)ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
	    
		
		
		
		return keyPacket;
		
	}
	
	
	public boolean sendMessage(int type, byte[] data, Key encryptionKey) {
		
		byte[] newData = new byte[data.length+3];
		newData[0] = (byte) senderId;
		newData[1] = (byte) type;
		newData[2] = Settings.CHECK_CODE;
		System.arraycopy(data, 0, newData, 3, data.length);
		
		//If key provided, encrypt message
		if(encryptionKey != null) {
			
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
	
	public Packet receiveMessage() {
		
		byte[] buf = new byte[BUFFER_SIZE];

		try {

			DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
			socket.receive(msgPacket);
			ByteArrayInputStream b = new ByteArrayInputStream(buf);
		    ObjectInputStream ois = new ObjectInputStream(b);
		    Packet packet = (Packet)ois.readObject();
			
			
			return packet;
			

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} 		
		
			

	}
	
	
	
	public byte[] encryptAsymmetric(byte[] data, Key key) {
		
		byte[] encryptedData = null;
		
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			encryptedData = cipher.doFinal(data);
			
		} catch(Exception e) {
			e.printStackTrace();
			encryptedData = data;
		}
		
		return encryptedData;
	}
	

	public byte[] decryptAsymmetric(byte[] encryptedData, Key key) {
		
		byte[] data = null;
		
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.DECRYPT_MODE, key);
			data = cipher.doFinal(encryptedData);
		} catch(Exception e) {
			e.printStackTrace();
			data = encryptedData;
		}
		
		return data;
	}
	
	public boolean sendInitialMessage(int type, byte[] data, Key publicKey) {
		byte[] encryptedMessage = null;
		
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
		byte[] newData = new byte[data.length+3];
		newData[0] = (byte) senderId;
		newData[1] = (byte) type;
		newData[2] = Settings.CHECK_CODE;
		
		System.arraycopy(data, 0, newData, 3, data.length);
		
		DatagramPacket packet = new DatagramPacket(newData, newData.length, group, PORT);

		try {
			socket.send(packet);
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		}

		
		
		
		
		
		
		
		return true;
		}
		
	

	public boolean sendBootKeyMessage(int i, PublicKey public1) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DatagramPacket packetToSend = new DatagramPacket(b.toByteArray(),b.toByteArray().length,group,PORT);
		
		PublicKeyPacket pKeyPacket = new PublicKeyPacket(packetToSend,senderId,public1);
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(b);
			oos.writeObject(pKeyPacket);
			
			oos.flush();
			byte[] buf = b.toByteArray();
			packetToSend = new DatagramPacket(buf,buf.length,group,PORT);
			socket.send(packetToSend);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return false;
		}
		
		return true;
		
	}
	
	public PublicKeyPacket receiveBootKeyMessage() {
		
		DatagramPacket packet;
		byte[] buffer = new byte[BUFFER_SIZE];
	    packet = new DatagramPacket(buffer, buffer.length );
	    PublicKeyPacket keyPacket = null;
	    try {
			socket.receive(packet);
			ByteArrayInputStream b = new ByteArrayInputStream(buffer);
		    ObjectInputStream ois = new ObjectInputStream(b);
			keyPacket = (PublicKeyPacket)ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
	    return keyPacket;
		
	}
	
}
