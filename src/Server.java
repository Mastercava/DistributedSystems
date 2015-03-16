import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


public class Server {

	final static String INET_ADDR = "228.5.6.7";
	final static int PORT = 6789;
	final static int BUFFER_SIZE = 256;
	
	public static void main(String[] args) {
		
		InetAddress group;
		MulticastSocket socket;
		
		try {
			
			group = InetAddress.getByName(INET_ADDR);
			socket = new MulticastSocket(PORT);
			socket.joinGroup(group);
			
		} catch(Exception e) {
			e.printStackTrace();
			return;
		}
		
		System.out.println("Server connected succesfully to group " + INET_ADDR);
		
		byte[] buf = new byte[BUFFER_SIZE];
		
		while (true) {
			
			try {
				
				DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
				socket.receive(msgPacket);
				String msg = new String(buf, 0, buf.length);
				System.out.println("Received message: " + msg);
				buf = new byte[BUFFER_SIZE];
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

	}

}
