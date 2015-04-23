import java.security.Key;


public class Settings {
	
	public static byte CHECK_CODE = (byte) 123456;
	
	public static Key SERVER_PUBLIC_KEY = null; //ACTUALLY USELESS!!
	public static int RSA_KEYSIZE = 512;
	public static String ENCRYPTION_ALGORITHM = "AES";
	public static int MAX_USERS = 8;
	public static int SERVER_ID = -1;
	public static int BIT_NUMBER = 3;
	
}
