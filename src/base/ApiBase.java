package base;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import settings.Constants;

public class ApiBase {

	public static String md5Custom(String st) {
	    MessageDigest messageDigest = null;
	    byte[] digest = new byte[0];
	 
	    try {
	        messageDigest = MessageDigest.getInstance("MD5");
	        messageDigest.reset();
	        messageDigest.update(st.getBytes());
	        digest = messageDigest.digest();
	    } catch (NoSuchAlgorithmException e) {
	        // тут можно обработать ошибку
	        // возникает она если в передаваемый алгоритм в getInstance(,,,) не существует
	        e.printStackTrace();
	    }
	 
	    BigInteger bigInt = new BigInteger(1, digest);
	    String md5Hex = bigInt.toString(16);
	 
	    while( md5Hex.length() < 32 ){
	        md5Hex = "0" + md5Hex;
	    }
	 
	    return md5Hex;
	}
	
	/**
	 * Process number/jid and turn it into a JID if necessary
	 *
	 * @param String
	 *            $number Number to process
	 * @return string
	 */
	public String getJID(String number) {
		if (!number.contains("@")) {
			// check if group message
			if (number.contains("-"))
				// to group
				number += "@" + Constants.WHATSAPP_GROUP_SERVER;
			else
				// to normal user
				number += "@" + Constants.WHATSAPP_SERVER;
		}
		return number;
	}

}
