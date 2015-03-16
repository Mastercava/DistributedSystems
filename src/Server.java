import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;


public class Server {

	final static String OWN_ADDR = "192.168.1.1";
	final static String GROUP_ADDR = "228.5.6.7";
	final static int PORT = 6789;
	final static int BUFFER_SIZE = 256;
	
	public static void main(String[] args) {
		
		InetAddress group;
		MulticastSocket socket;
		
		try {
			
			group = InetAddress.getByName(GROUP_ADDR);
			socket = new MulticastSocket(PORT);
			socket.joinGroup(group);
			
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}
		
		try {
			System.out.println("Server " + InetAddress.getLocalHost().getHostAddress() + " connected succesfully to group " + GROUP_ADDR);
		} catch (Exception e1) {
			System.out.println("Server locally connected to group " + GROUP_ADDR);
		}
		
		byte[] buf = new byte[BUFFER_SIZE];
		
		while (true) {
			
			try {
				
				DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
				socket.receive(msgPacket);
				InetAddress senderIp = msgPacket.getAddress();
				String msg = (new String(buf, 0, buf.length)).trim();
				System.out.println(senderIp + ": " + msg);
				
				//Message analisys
				if(msg.equals("join")) {
					
					String tmp = senderIp + " joined the group";
					DatagramPacket packet = new DatagramPacket(tmp.getBytes(), tmp.length(), group, PORT);
		    		try {
						socket.send(packet);
					} catch (Exception e) {
						e.printStackTrace();
					}
		    		
		    	}
				
				buf = new byte[BUFFER_SIZE];
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

	}

}
