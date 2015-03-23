import java.security.Key;


public class Settings {
	
	public static Settings instance;
	
	public static byte CHECK_CODE = (byte) 123456;
	
	public Key SERVER_PUBLIC_KEY;

	private Settings() {
		
	}
	
	public static Settings getInstance() {
		if(instance == null) instance = new Settings();
		return instance;
	}
	
	public static boolean asymmetric() {
		return false;
	}
	
	public static boolean symmetric() {
		return true;
	}
	
	
	
}
