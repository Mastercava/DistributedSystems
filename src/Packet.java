import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;


public class Packet {

	private int senderId;
	private String message;
	private int type;
	private InetAddress senderIp;
	
	public Packet(DatagramPacket packet) {
		senderIp = packet.getAddress();
		byte[] data = packet.getData();
		senderId = (int) data[0];
		type = (int) data[1];
		byte[] msgBytes = Arrays.copyOfRange(data,2,data.length);
		message = new String(msgBytes, 0, msgBytes.length);
	}
	
	public int getSenderId() {
		return senderId;
	}
	
	public String getMessage() {
		return message;
	}

	public InetAddress getSenderIp() {
		return senderIp;
	}
	
	public int getType() {
		return type;
	}

}
