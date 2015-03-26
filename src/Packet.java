import java.net.DatagramPacket;
import java.net.InetAddress;
import java.security.Key;
import java.util.Arrays;
import java.util.List;


public class Packet {

	private int length;
	private byte[] rawData;
	private byte[] data;
	private int senderId;
	private String message;
	private int type;
	private byte checkCode;
	private InetAddress senderIp;
	
	public Packet(DatagramPacket packet) {
		length = packet.getLength();
		senderIp = packet.getAddress();
		//rawData = packet.getData();
		rawData = Arrays.copyOfRange(packet.getData(),0,length);
		//System.out.println("RECEIVED MESSAGE FORMALLY LONG " + rawData.length + ", ACTUALLY LONG " + length);
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
		System.out.println("TRYING TO DECRYPT MESSAGE OF LENGHT " + rawData.length + "...");
		
		for(Key k : keys) {
			
			byte[] decryptedData = rawData;
			if (k.getAlgorithm().equals("RSA")) {
				try {
					decryptedData = Messaging.decryptAsymmetric(rawData, k);
				} catch (Exception e) {
					System.out.println("Message cannot be decrypted by this key " + k.toString());
				}
				
			} else {
				try {
					decryptedData = Messaging.decryptSymmetric(rawData, k);
				} catch (Exception e) {
					System.out.println("Message cannot be decrypted by this key " + k.toString());
				}
			}
			
			//System.out.println("ATTEMPT DECRYPTED MESSAGE OF LENGHT " + decryptedData.length + "...");
			
			//Decryption with key k successful
			if(decryptedData[2] == Settings.CHECK_CODE && decryptedData != null) {
				System.out.println("DECRYPTION OK!!!!");
				rawData = decryptedData;
				decodeRawData();
				break;
			}
			//Otherwise try with another key

		}
	}

	public byte[] getData() {
		return data;
	}
}
