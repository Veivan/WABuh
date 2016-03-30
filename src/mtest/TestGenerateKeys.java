package mtest;

import chatapi.Funcs;
import helper.KeyStream;

public class TestGenerateKeys {
	public static void RunTect() throws Exception {
/*		char[] decodedpassword = { '˚', 'M', 'H', '/', ':', 'ç', 'ú', '›', '˙', '∂',
				'n', '~', '‘', 'Î', 'Ò', '', 'Õ', '÷', 'í', 'É' };
		int[] challengeHex = {0xf7, 0xe2, 0x86, 0xd2, 0x1c, 0xda, 0x51, 0x2c, 0xef, 0x9b, 
				0x65, 0xb8, 0xd2, 0x69, 0x20, 0xf9, 0x3c, 0x5d, 0x64, 0x87};
		
		byte[] challengeData = new byte[20];
		for (int i = 0; i < 20; i++){			
			if (challengeHex[i] > 127) challengeData[i] = (byte)(challengeHex[i] - 255);
			else challengeData[i] = (byte)challengeHex[i];				
		}
		*/
		
		//char[] password = { 'a', 'b'};
		//byte[] challengeData = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
		
		String pass = "+01ILzqNnN36tm5+1OvxAc3WkoM=";
		//pass = "ab";
		
		byte[] decoded = TestFuncs.encryptPassword(pass);
		System.out.println(Funcs.GetHexArray(decoded));			

		byte[] challengeData = "b".getBytes();
		
		String encpass = new String(TestFuncs.encryptPassword(pass) );
		
//		char[] buffer = encpass.toCharArray();
		char[] buffer = pass.toCharArray();
		System.out.println(encpass);			
		
		byte[][] keys = KeyStream.GenerateKeys(buffer, challengeData);
		
		System.out.println(Funcs.GetHexArray(keys[0]));			
		System.out.println(Funcs.GetHexArray(keys[1]));			
		System.out.println(Funcs.GetHexArray(keys[2]));			
		System.out.println(Funcs.GetHexArray(keys[3]));			
	}

}
