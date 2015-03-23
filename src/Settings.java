import java.security.Key;


public class Settings {
	
	public static byte CHECK_CODE = (byte) 123456;
	
	public static Key SERVER_PUBLIC_KEY = null;

	/*
	private Settings() {
		
	}
	
	public static Settings getInstance() {
		if(instance == null) instance = new Settings();
		return instance;
	}
	*/
	
	public static void setServerPublicKey(Key key) {
		SERVER_PUBLIC_KEY = key;
	}
	
	public static boolean asymmetric() {
		return false;
	}
	
	public static boolean symmetric() {
		return true;
	}
	
	
	
}
