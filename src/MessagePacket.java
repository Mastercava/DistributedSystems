import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Arrays;


public class MessagePacket extends Packet{

	private int senderId;
	private String message;
	private int type;
	private byte checkCode;
	private InetAddress senderIp;
	
	public MessagePacket(DatagramPacket packet) {
		super(packet);
		senderIp = packet.getAddress();
		byte[] data = packet.getData();
		senderId = (int) data[0];
		type = (int) data[1];
		checkCode = data[2];
		byte[] msgBytes = Arrays.copyOfRange(data,3,data.length);
		message = new String(msgBytes, 0, msgBytes.length);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
	

}
