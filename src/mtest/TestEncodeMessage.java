package mtest;

import java.io.ByteArrayOutputStream;

import javax.xml.bind.DatatypeConverter;

import chatapi.Funcs;
import chatapi.ProtocolNode;
import base.WhatsAppBase;
import settings.Constants;
import helper.BinTreeNodeWriter;
import helper.KeyStream;

public class TestEncodeMessage {
	public static void RunTect() throws Exception {
		
//		int[] challengeDataSrc = {117, 178, 236, 241, 12, 49, 178, 254, 189, 46, 195, 27, 75, 16, 62, 67, 62, 245, 232, 119};
//		int[] challengeDataSrc = {0xe9, 0x85, 0x75, 0x43, 0x07, 0xf1, 0x6a, 0x84, 0xf2, 0x01, 0xeb, 0x3b, 0xcc, 0xe9, 0xbc, 0xfa, 0x98, 0x88, 0x5d, 0x2b};
//		int[] challengeDataSrc = {0xf1, 0x28, 0x8b, 0x84, 0xcf, 0xca, 0xda, 0xbb, 0x72, 0xd9, 0xde, 0x95, 0x03, 0xad, 0x04, 0x71, 0x59, 0x1a, 0x89, 0xbd};
		int[] challengeDataSrc = {0xf6, 0x73, 0x55, 0x3e, 0x6e, 0xca, 0x56, 0x5f, 0xab, 0x2c, 0x13, 0xf5, 0xe4, 0x76, 0xce, 0x44, 0xda, 0xa1, 0x35, 0xc3};
	
		String pass = "+01ILzqNnN36tm5+1OvxAc3WkoM=";
		byte[] challengeData = new byte[challengeDataSrc.length]; 
//		byte[] challengeData = "a".getBytes(); 

		for (int i = 0; i < challengeData.length; i++) 
			challengeData[i] = (byte)challengeDataSrc[i];
		System.out.println(Funcs.GetHexArray(challengeData));

		/*StringBuffer buff = new StringBuffer();
		for (int i = 0; i < challengeData.length; i++) {
			int x = challengeDataSrc[i];
			if (challengeDataSrc[i] < 0) x = x + 256;
			buff.append(x + ", ");
		}	
		System.out.println(buff.toString());*/

		byte[] password64dec = TestFuncs.encryptPassword(pass);
		System.out.println(Funcs.GetHexArray(password64dec));
		System.out.println(new String(TestFuncs.encryptPassword(pass), WhatsAppBase.SYSEncoding));

		byte[][] keys = KeyStream.GenerateKeys(password64dec, challengeData);
		System.out.println(Funcs.GetHexArray(keys[0]));
		System.out.println(Funcs.GetHexArray(keys[1]));
		System.out.println(Funcs.GetHexArray(keys[2]));
		System.out.println(Funcs.GetHexArray(keys[3]));
		
//		byte[] key = "a".getBytes();
//		byte[] mackey = "a".getBytes();
		KeyStream ks = new KeyStream(keys[0], keys[1]);

		
		byte[] empbytes = DatatypeConverter.parseHexBinary("00");

		ByteArrayOutputStream babuffer = new ByteArrayOutputStream();
		babuffer.write("\0\0\0\0".getBytes(WhatsAppBase.SYSEncoding));
		babuffer.write("79250069542".getBytes(WhatsAppBase.SYSEncoding));
		for (int i = 0; i < challengeData.length; i++) 
			babuffer.write(challengeData[i]);

//		String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
//		String timestamp = "1460264129";
//		String timestamp = "1460340975";
		String timestamp = "1460432318";
		babuffer.write(timestamp.getBytes(WhatsAppBase.SYSEncoding));
		babuffer.write("000".getBytes(WhatsAppBase.SYSEncoding));
		babuffer.write(empbytes);
		babuffer.write("000".getBytes(WhatsAppBase.SYSEncoding));
		babuffer.write(empbytes);
		babuffer.write(Constants.OS_VERSION.getBytes(WhatsAppBase.SYSEncoding));
		babuffer.write(empbytes);
		babuffer.write(Constants.MANUFACTURER.getBytes(WhatsAppBase.SYSEncoding));
		babuffer.write(empbytes);
		babuffer.write(Constants.DEVICE.getBytes(WhatsAppBase.SYSEncoding));
		babuffer.write(empbytes);
		babuffer.write(Constants.BUILD_VERSION.getBytes(WhatsAppBase.SYSEncoding));
		
		byte[] data = babuffer.toByteArray();

		TestFuncs.PrintHex(data);
		data = ks.EncodeMessage(data, 0, 4, data.length - 4);
//		System.out.println(new String(data));
		TestFuncs.PrintHex(data);

		BinTreeNodeWriter writer = new BinTreeNodeWriter();
		ProtocolNode pn =  new ProtocolNode("response", null, null, data);

		byte[] encdata = writer.write(pn, true);
		TestFuncs.PrintHex(encdata);

	}

}
