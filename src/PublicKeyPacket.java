import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.security.PublicKey;



public class PublicKeyPacket extends Packet implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7567762766654043137L;
	private PublicKey key;
	
	
	//stuff from the old class
	private int senderId;
	private byte checkCode;
	private InetAddress senderIp;
	
	
	public PublicKeyPacket(DatagramPacket packet, int senderId, PublicKey key) {
		super(packet);		
		this.senderId = senderId;
		this.key = key;
		checkCode = Settings.CHECK_CODE;
		
	}

	public int getSender() {
		return senderId;
	}

	public void setSender(int sender) {
		this.senderId = sender;
	}

	public PublicKey getKey() {
		return key;
	}

	public void setKey(PublicKey key) {
		this.key = key;
	}
	

	@Override
	public int getSenderId() {
		// TODO Auto-generated method stub
		return senderId;
	}

	@Override
	public InetAddress getSenderIp() {
		// TODO Auto-generated method stub
		return senderIp;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public int getType() {
		return 1;
	}


	

}
