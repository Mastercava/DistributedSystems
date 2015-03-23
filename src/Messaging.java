import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.Key;


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
			return new Packet(msgPacket);

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
			

	}
	
}
