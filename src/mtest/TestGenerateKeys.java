package mtest;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import chatapi.Funcs;
import helper.KeyStream;

public class TestGenerateKeys {
	public static void RunTect() throws Exception {
		/*
		 * char[] decodedpassword = { 'ы', 'M', 'H', '/', ':', 'Ќ', 'њ', 'Э',
		 * 'ъ', '¶', 'n', '~', 'Ф', 'л', 'с', '', 'Н', 'Ц', '’', 'ѓ' }; int[]
		 * challengeHex = {0xf7, 0xe2, 0x86, 0xd2, 0x1c, 0xda, 0x51, 0x2c, 0xef,
		 * 0x9b, 0x65, 0xb8, 0xd2, 0x69, 0x20, 0xf9, 0x3c, 0x5d, 0x64, 0x87};
		 * 
		 * byte[] challengeData = new byte[20]; for (int i = 0; i < 20; i++){ if
		 * (challengeHex[i] > 127) challengeData[i] = (byte)(challengeHex[i] -
		 * 255); else challengeData[i] = (byte)challengeHex[i]; }
		 */

		// char[] password = { 'a', 'b'};
		// byte[] challengeData = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		// 1, 1, 1, 1, 1};

		// System.out.println(System.getProperty("sun.jnu.encoding"));
		// System.out.println(System.getProperty("file.encoding"));
		System.out.println(Charset.defaultCharset());

		// System.setProperty("file.encoding", "UTF-8");
		// System.out.println(Charset.defaultCharset());

		String pass = "+01ILzqNnN36tm5+1OvxAc3WkoM=";
		// pass = "ab";

		byte[] decoded = TestFuncs.encryptPassword(pass);
		System.out.println(Funcs.GetHexArray(decoded));

		byte[] challengeData = "b".getBytes();

		String encpass = new String(TestFuncs.encryptPassword(pass)); // StandardCharsets.UTF_8

		// fb 4d 48 2f 3a 8d 9c dd fa b6 6e 7e d4 eb f1 01 cd d6 92 83

		// char[] buffer = encpass.toCharArray();
		// char[] buffer = pass.toCharArray();
		// char[] buffer = "ыMH/:ЌњЭъ¶n~ФлсНЦ’ѓ".toCharArray();
		// char[] buffer = {'M'};

		for (int i = 1; i < 1000000; i++) {
			byte[] zx = { (byte) -4 };
			int[] xx = { i };
			char[] buffer = (new String(xx, 0, 1)).toCharArray();
			// System.out.println(buffer);

			byte[][] keys = KeyStream.GenerateKeys(buffer, challengeData);

			if (keys[0][0] == 30 && keys[0][1] == 53) {
				System.out.println(buffer);
				System.out.println(Funcs.GetHexArray(keys[0]));
				System.out.println(Funcs.GetHexArray(keys[1]));
				System.out.println(Funcs.GetHexArray(keys[2]));
				System.out.println(Funcs.GetHexArray(keys[3]));
			}
		}
	}

}
