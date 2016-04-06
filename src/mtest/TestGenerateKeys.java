package mtest;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

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

		byte[] challengeData = "bbbbbbbb".getBytes(StandardCharsets.UTF_8);

		String encpass = new String(TestFuncs.encryptPassword(pass), "cp866"); // StandardCharsets.UTF_8

		// fb 4d 48 2f 3a 8d 9c dd fa b6 6e 7e d4 eb f1 01 cd d6 92 83

		Charset cCharset = Charset.forName("cp866"); // "cp1251" "cp866"
														// "ISO8859_5" "KOI8_R"
		//Charset cCharset = StandardCharsets.UTF_8;
		CharBuffer charBuffer = cCharset.decode(ByteBuffer.wrap(decoded)); // also
																			// decode
																			// to
																			// String
		// CharBuffer cBuffer = ByteBuffer.wrap(decoded).asCharBuffer();

		System.out.println(charBuffer.toString());

		// char[] buffer = encpass.toCharArray();
		// char[] buffer = pass.toCharArray();
		// char[] buffer = "ыMH/:ЌњЭъ¶n~ФлсНЦ’ѓ".toCharArray();
//		char[] buffer = { charBuffer.array()[0] };
		 char[] buffer = {251};
		 //char[] buffer = {'ы'};

		for(int i=0; i<buffer.length;i++){
		    System.out.println(Integer.toBinaryString(0x100 + buffer[i]).substring(1));
		}
		/*
		 * Charset latin1Charset = Charset.forName("ISO-8859-1"); CharBuffer
		 * charBuffer = latin1Charset.decode(ByteBuffer.wrap(byteArray)); //
		 * also decode to String byteBuffer = latin1Charset.encode(charbuffer);
		 * // also decode from String
		 * 
		 * byte[] zx = { (byte) 0xFB }; char[] buffer = (new String(zx,
		 * "cp866")).toCharArray();
		 */

		byte[][] keys = KeyStream.GenerateKeys(buffer, challengeData);

		System.out.println(buffer);
		System.out.println(Funcs.GetHexArray(keys[0]));
		System.out.println(Funcs.GetHexArray(keys[1]));
		System.out.println(Funcs.GetHexArray(keys[2]));
		System.out.println(Funcs.GetHexArray(keys[3])); 
		
//		testPBE();
	}

	private static void testPBE() throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] challengeData = "b".getBytes();
		char[] pass = { 0x4d };
		
		PBEKeySpec keySpec = new PBEKeySpec(pass, challengeData, 2, 20 * 8);
		SecretKeyFactory factory = SecretKeyFactory
				.getInstance("PBKDF2WithHmacSHA1");
		byte[] secretKey = factory.generateSecret(keySpec).getEncoded();
		//System.arraycopy(secretKey, 0, array2[j], 0, keylen);
		System.out.println(pass);
		System.out.println(Funcs.GetHexArray(secretKey));
	}
}
