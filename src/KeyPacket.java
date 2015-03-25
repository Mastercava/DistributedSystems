import java.io.Serializable;
import java.net.DatagramPacket;

import javax.crypto.SecretKey;


public class KeyPacket extends Packet implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7602657523013138859L;
	
	
	private int sender;
	private SecretKey key;
	
	
	public KeyPacket(DatagramPacket packet, int sender, SecretKey key) {
		super(packet);
		this.sender = sender;
		this.key = key;
		
	}

	public int getSender() {
		return sender;
	}

	public void setSender(int sender) {
		this.sender = sender;
	}

	public SecretKey getKey() {
		return key;
	}

	public void setKey(SecretKey key) {
		this.key = key;
	}


	

}
