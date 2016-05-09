import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.simple.parser.ParseException;



public class Security {
	
	private static String TG_ID ="TG_01";
	private static Security security;
	private static String Secure_Key = "123456";					// Originally used by TG device serial number
	private static String SHA_key = null;
	protected static String IV = "01010101010101010101010101010101";
	private static String topic = null;
	private static MQTT mqtt = null;
	private MessageDigest md = null;
	public Security (){}
	
	public static Security getInstance()
	{
		if(security == null)
		{
			security = new Security();
			mqtt = MQTT.getInstance();
		}
		return security;
	}
	


	/**
	 * Pre		: Sharing Secure key and hash algorithm with SC
	 * Func		: Make a secret key sharing with SC using Secure_key and SHA-256 algorithm
	 * Post		: Decrypt message from SC
	 * @return
	 */
	public Boolean Generate_Shakey() {
		try {
		    md = MessageDigest.getInstance("SHA-256");
		    byte[] byteData = md.digest(Secure_Key.getBytes());
		    StringBuffer sb = new StringBuffer(); 
		    
		    // Execution for compatibility with javascript
			for(int i = 0 ; i < byteData.length ; i++){
				sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
			}
			
			SHA_key = sb.toString();
			System.out.println("SHA_key : " + SHA_key);
		    return true;
		    
		} catch (NoSuchAlgorithmException e) {
		    return false;
		}
	}


	/**
	 * 
	 * 	Pre		: Sharing encryption algorithm, IV and secret key(Sha)
	 *  Func	: Encrypt plain text
	 *  Post	: Send encrypted text to SC
	 *  
	 */
	public byte[] encrypt(String plainText, String encryptionKey)throws Exception {
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		byte[] b= hexToByteArray(encryptionKey);
		SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"),"AES");//b ,"AES");

		AlgorithmParameterSpec ivSpec = new IvParameterSpec(IV.getBytes("UTF-8"));//hexToByteArray(IV));
  		cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
  		
		return cipher.doFinal(plainText.getBytes("UTF-8"));
  		//return cipher.doFinal(hexToByteArray(stringToHex(plainText)));
	}

	/**
	 * 
	 * 	Pre		: Sharing encryption algorithm, IV and secret key(Sha)
	 *  Func	: Decrypt cipher text
	 *  Post	: Parsing the decrypted data
	 *  
	 */
	public String decrypt(byte[] cipherText, String encryptionKey) throws Exception {
		
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		System.out.println(bytArrayToHex(cipherText));
		System.out.println("EncryptionKey : "+ encryptionKey);
		
		System.out.println("hexToByte : " + hexToByteArray(encryptionKey));
		
        byte[] b= hexToByteArray(encryptionKey);
        SecretKeySpec keySpec = new SecretKeySpec(b, "AES");
        
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(hexToByteArray(IV));
        cipher.init(Cipher.DECRYPT_MODE,keySpec, ivSpec);

        byte[] results = cipher.doFinal(cipherText);
        
        return new String(results);
	}
	
	 // 문자열을 헥사 스트링으로 변환하는 메서드
	  public static String stringToHex(String s) throws UnsupportedEncodingException {
		  return String.format("%040x", new BigInteger(1, s.getBytes("UTF-8")));
	  }

	public String bytArrayToHex(byte[] a) {
	    StringBuilder sb = new StringBuilder();
	    for(final byte b: a)
	        sb.append(String.format("%02x", b&0xff));
	    return sb.toString();
	}
	
	public static byte[] hexToByteArray(String hex) {
	    if (hex == null || hex.length() == 0) {
	        return null;
	    }
	    byte[] ba = new byte[hex.length() / 2];
	    for (int i = 0; i < ba.length; i++) {
	        ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
	    }
	    return ba;
	}

	/**
	 *  Pre		: Subscribe first topic for receive encrypted message
	 *  Func	: Decrypt message from SC and set new topic category
	 *  Post	: Set pub/sub topic as new topic category 
	 */
	public String GetTopicFromSC(byte[] encrypted_msg) throws Exception
	{
		MsgParser parser = new MsgParser();
		System.out.println("# Making ShaKey....");
		String decrypted_Data=null;
		Generate_Shakey();
		try {
			decrypted_Data = decrypt(encrypted_msg, SHA_key);
			System.out.println("# First message : " + decrypted_Data);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("#! error- decrypt failed");
		}

		/*************** Get new topic from SC *******************/
		try {
			parser.SetMsg(decrypted_Data);
			TG_ID = parser.get_Seed();							// New topic category for pub/sub
			
			System.out.println("# Changed SHA_key : " + SHA_key);
			System.out.println("-------------------------------");
			System.out.println("-------------------------------");
			System.out.println();
		} catch (ParseException e) {
			System.out.println("#! error - parsing");
		}
		
		if(mqtt == null){
			System.out.println("#! mqtt is null");
			mqtt = MQTT.getInstance();
		}
		mqtt.SetKeyValue(TG_ID);
		
		return TG_ID;
	}
	
	public String GetDecryptedMsg(byte[] encrypted_msg)
	{
		String decryptedData = null;
		try {
			System.out.println("decrypting....");
			decryptedData = decrypt(encrypted_msg, SHA_key);
			System.out.println("decryted msg : "+decryptedData);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("error- decrypt failed");
		}
		return decryptedData;
	}
	
	/*public void SetTopicFromJson(String msg)
	{
		MsgParser msgparser = new MsgParser();
		try {
			msgparser.SetMsg(msg);
		} catch (ParseException e) {
			System.out.println("error - parsing");
		}
		topic=msgparser.getTopic();
		
		System.out.println("setting ..... new topic : "+topic+" new secureKey : "+Secure_Key);
		mqtt.SetKeyValue(topic);
		mqtt.setGotTopic();
	}*/
	
/*	public static String stringToHex(String s) {
	    String result = "";
	    for (int i = 0; i < s.length(); i++) {
	      result += String.format("%02X", (int) s.charAt(i));
	    }
	    System.out.println("****** string to hex : " + result);
	    return result;
	  }*/
	/*	public String Make_JSon(String data){
	StringBuilder sb = new StringBuilder();
	sb.append("{\"data\":\"");
	sb.append(data);
	sb.append("\",\"type\":\"03\"}");

	return sb.toString();
}*/

/*	public String Padding(String data) {
	int paddingCount = data.length() % 16;
	paddingCount = 16 - paddingCount;
	StringBuilder sb = new StringBuilder();
	sb.append(data);
	for (int i = 0; i < paddingCount; i++) {
		sb.append(" ");
	}
	return sb.toString();
}*/

/*	public byte[] bytePadding(byte[] data) {
	int num = 16 - data.length % 16;
	System.out.println("num : " + num);
	for (int i = 0; i < num; i++) {
		data[data.length] = '\0';
	}
	return data;
}*/
/*
  	public void Generate_Shakey(String key) {
		//SHA_key = SHA256(noun1, noun2);
		    byte[] byteData = hexToByteArray(stringToHex(key));
		    StringBuffer sb = new StringBuffer(); 
			for(int i = 0 ; i < byteData.length ; i++){
				sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
			}
			SHA_key = sb.toString();
	}*/
	
}
