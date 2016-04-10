package mtest;

import base.WhatsAppBase;
import chatapi.Funcs;
import helper.KeyStream;

public class TestGenerateKeys {
	public static void RunTect() throws Exception {
		/*		 
		 * byte[] challengeData = {0xf7, 0xe2, 0x86, 0xd2, 0x1c, 0xda, 0x51, 0x2c, 0xef,
		 * 0x9b, 0x65, 0xb8, 0xd2, 0x69, 0x20, 0xf9, 0x3c, 0x5d, 0x64, 0x87};
		 * 
		 * f3 39 03 3f 87 43 64 0f 4e 8c ff 64 5b 7e dd bf a7 44 10 62
		 * 
		 * byte[] challengeData = new byte[20]; for (int i = 0; i < 20; i++){ if
		 * (challengeHex[i] > 127) challengeData[i] = (byte)(challengeHex[i] -
		 * 255); else challengeData[i] = (byte)challengeHex[i]; }
		 */


		String pass = "+01ILzqNnN36tm5+1OvxAc3WkoM=";
		byte[] challengeData = "bbbbbbbb".getBytes(WhatsAppBase.SYSEncoding); 

		byte[] password64dec = TestFuncs.encryptPassword(pass);
		System.out.println(Funcs.GetHexArray(password64dec));
		System.out.println(new String(TestFuncs.encryptPassword(pass), WhatsAppBase.SYSEncoding));

		// Password must be like this
		char[] buffer2 = {251, 0x4d, 0x48, 0x2f, 0x3a, 0x8d, 0x9c, 0xdd, 0xfa, 
				 0xb6, 0x6e, 0x7e, 0xd4, 0xeb, 0xf1, 0x01, 0xcd, 0xd6, 0x92, 0x83};

		byte[][] keys = KeyStream.GenerateKeys(password64dec, challengeData);

		System.out.println(Funcs.GetHexArray(keys[0]));
		System.out.println(Funcs.GetHexArray(keys[1]));
		System.out.println(Funcs.GetHexArray(keys[2]));
		System.out.println(Funcs.GetHexArray(keys[3])); 
	}
}
