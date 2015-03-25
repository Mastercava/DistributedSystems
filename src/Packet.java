import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;

public class Packet {
	
	private int senderId;
	private String message;
	private int type;
	private byte checkCode;
	private InetAddress senderIp;
	
	public Packet(DatagramPacket packet) {
		senderIp = packet.getAddress();
		byte[] data = packet.getData();
		senderId = (int) data[0];
		type = (int) data[1];
		checkCode = data[2];
		byte[] msgBytes = Arrays.copyOfRange(data,3,data.length);
		message = new String(msgBytes, 0, msgBytes.length);
	}
	
	
	
	public int getSenderId() {
		return -1;
	}
	
	
	

	public InetAddress getSenderIp(){
		return null;
	}
	
	
	public boolean isValid() {
		return false;
	}
	
	public int getType(){
		return -1;
	}
}


