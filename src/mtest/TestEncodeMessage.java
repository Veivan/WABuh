package mtest;

import java.io.ByteArrayOutputStream;

import settings.Constants;
import base.WhatsAppBase;
import helper.KeyStream;

public class TestEncodeMessage {
    public static void RunTect() throws Exception {
		byte[] key = "a".getBytes();
		byte[] mackey =  "a".getBytes();
		KeyStream ks = new KeyStream(key, mackey);
		//byte[] data = "hello".getBytes("UTF-8");
		
		byte[] data = null;

		StringBuffer buff = new StringBuffer();
		buff.append("\0\0\0\0");
		buff.append("79250069542");
		
		byte[] challengeData = {72, -37, 1, -69, 1, -79, -98, 69, -41, -6, 113, 27, -32, -61, -109, -113, -119, 5, -89, 88};	
		
		buff.append(new String(challengeData));
/*		buff.append(String.valueOf(System.currentTimeMillis()));
		buff.append("000");
		buff.append(empstr);
		buff.append("000");
		buff.append(empstr);
		buff.append(Constants.OS_VERSION);
		buff.append(empstr);
		buff.append(Constants.MANUFACTURER);
		buff.append(empstr);
		buff.append(Constants.DEVICE);
		buff.append(empstr);
		buff.append(Constants.BUILD_VERSION); */
		
		data = buff.toString().getBytes();
		TestFuncs.PrintHex(data);
		data = ks.EncodeMessage(data, 0, 4, data.length - 4);
		System.out.println(new String(data));
		TestFuncs.PrintHex(data);
		
		String x = "=?¡R{1kIY‚SÜG[¼";
	  }

}
