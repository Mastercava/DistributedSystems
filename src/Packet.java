import java.net.DatagramPacket;
import java.net.InetAddress;
import java.security.Key;
import java.util.Arrays;
import java.util.List;


public class Packet {

	private byte[] rawData;
	private byte[] data;
	private int senderId;
	private String message;
	private int type;
	private byte checkCode;
	private InetAddress senderIp;
	
	public Packet(DatagramPacket packet) {
		senderIp = packet.getAddress();
		rawData = packet.getData();
		decodeRawData();
	}
	
	private void decodeRawData() {
		senderId = (int) rawData[0];
		type = (int) rawData[1];
		checkCode = rawData[2];
		data = Arrays.copyOfRange(rawData,3,rawData.length);
		message = new String(data, 0, data.length);
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
	
	public boolean isValid() {
		return checkCode == Settings.CHECK_CODE;
	}

	public void tryDecryption(List<Key> keys) {
		if(isValid() || keys == null) return;
		for(Key k : keys) {
			//decryptedData = ...
			/*
			//Decryption with key k successful
			if(decryptedData[2] == Settings.CHECK_CODE) {
				rawData = decryptedData;
				decodeRawData();
				break;
			}
			//Otherwise try with another key
			*/
		}
	}

	public byte[] getData() {
		return data;
	}
}
