package mtest;

import helper.BinTreeNodeWriter;
import helper.KeyStream;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import base.ApiBase;
import base.WhatsAppBase;

public class TestMain {

	public static void main(String[] args) throws Exception {

	    //System.out.println(encrypt("1234567"));
	    //System.out.println(ApiBase.md5Custom("String").toLowerCase().replace("-", ""));

		
		
		String hello = "from";
		
/*/		testBinWriter();
		int v = 0x2;
		System.out.println(v);
		System.out.println(v & 0xff);
*/
		
	   // byte[] helloBytes = hello.getBytes("UTF-8");
	    byte[] helloBytes = {-8, 3};
	    System.out.println(helloBytes);
	 
/*	    String encoded = Base64.getEncoder().encodeToString(helloBytes);
	    System.out.println(hello + " encoded=> " +encoded);
	    
	    //System.out.println(helloBytes);
	    
		String signature = "MIIDMjCCAvCgAwIBAgIETCU2pDALBgcqhkjOOAQDBQAwfDELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFDASBgNVBAcTC1NhbnRhIENsYXJhMRYwFAYDVQQKEw1XaGF0c0FwcCBJbmMuMRQwEgYDVQQLEwtFbmdpbmVlcmluZzEUMBIGA1UEAxMLQnJpYW4gQWN0b24wHhcNMTAwNjI1MjMwNzE2WhcNNDQwMjE1MjMwNzE2WjB8MQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEUMBIGA1UEBxMLU2FudGEgQ2xhcmExFjAUBgNVBAoTDVdoYXRzQXBwIEluYy4xFDASBgNVBAsTC0VuZ2luZWVyaW5nMRQwEgYDVQQDEwtCcmlhbiBBY3RvbjCCAbgwggEsBgcqhkjOOAQBMIIBHwKBgQD9f1OBHXUSKVLfSpwu7OTn9hG3UjzvRADDHj+AtlEmaUVdQCJR+1k9jVj6v8X1ujD2y5tVbNeBO4AdNG/yZmC3a5lQpaSfn+gEexAiwk+7qdf+t8Yb+DtX58aophUPBPuD9tPFHsMCNVQTWhaRMvZ1864rYdcq7/IiAxmd0UgBxwIVAJdgUI8VIwvMspK5gqLrhAvwWBz1AoGBAPfhoIXWmz3ey7yrXDa4V7l5lK+7+jrqgvlXTAs9B4JnUVlXjrrUWU/mcQcQgYC0SRZxI+hMKBYTt88JMozIpuE8FnqLVHyNKOCjrh4rs6Z1kW6jfwv6ITVi8ftiegEkO8yk8b6oUZCJqIPf4VrlnwaSi2ZegHtVJWQBTDv+z0kqA4GFAAKBgQDRGYtLgWh7zyRtQainJfCpiaUbzjJuhMgo4fVWZIvXHaSHBU1t5w//S0lDK2hiqkj8KpMWGywVov9eZxZy37V26dEqr/c2m5qZ0E+ynSu7sqUD7kGx/zeIcGT0H+KAVgkGNQCo5Uc0koLRWYHNtYoIvt5R3X6YZylbPftF/8ayWTALBgcqhkjOOAQDBQADLwAwLAIUAKYCp0d6z4QQdyN74JDfQ2WCyi8CFDUM4CaNB+ceVXdKtOrNTQcc0e+t";
		String classesMd5 = "U+w9L64wtuyknyhiDVdM/w==";
	    String st = "/UIGKU1FVQa+ATM2A0za7G2KI9S/CwPYjgAbc67v7ep42eO/WeTLx1lb1cHwxpsEgF4+PmYpLd2YpGUdX/A2JQitsHzDwgcdBpUf7psX1BU=";
//		byte[] decoded = Base64.getDecoder().decode(encoded);
//		byte[] decoded = Base64.getDecoder().decode(classesMd5.getBytes("UTF-8"));
		byte[] decoded = Base64.getDecoder().decode(signature.getBytes());
		String helloAgain = new String(decoded, "UTF-8");
//		String helloAgain = new String(decoded);
//		System.out.println(helloAgain);

		String repeated = new String(new char[3]).replace("\0", ""+(char)(0x5C));
		System.out.println(repeated);
		
		gethash(); */
	}

    public static byte[] encrypt(String x) throws Exception {
	    java.security.MessageDigest d = null;
	    d = java.security.MessageDigest.getInstance("SHA-1");
	    d.reset();
	    d.update(x.getBytes());
	    return d.digest();
	  }
    
    public static void gethash() throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException
    {
		Mac mac = Mac.getInstance("HmacSHA1");
		SecretKeySpec keySpec = new SecretKeySpec("qnscAdgRlkIhAUPY44oiexBKtQbGY0orf7OV1I50".getBytes(),  "HmacSHA1");
		mac.init(keySpec);
		byte[] result = mac.doFinal("foo".getBytes());   
		byte[] st = Base64.getEncoder().encode(result);
	    System.out.println(new String(st, "UTF-8"));
    }
  
	public static byte[] encryptPassword()
    {
		String password = "123";
        try {
			return Base64.getDecoder().decode(password.getBytes(WhatsAppBase.SYSEncoding));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }

    public static void testBinWriter() throws IOException 
    {
		String encpass = new String(encryptPassword());
		System.out.println(encpass);

		char[] buffer = encpass.toCharArray();
		byte[][] keys = KeyStream.GenerateKeys(buffer, "salt".getBytes());
		KeyStream outputKey = new KeyStream(keys[0], keys[1]);
		
		String hello = "Hello World";
		BinTreeNodeWriter wr = new BinTreeNodeWriter();
		wr.setKey(outputKey);
		wr.buffer.write(hello.getBytes());
    	
    	byte[] mess = wr.flushBuffer(true);
		System.out.println(new String(mess));
    }
}
