package helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.openssl.PasswordException;

public class KeyStream {

	public static final String AuthMethod = "WAUTH-2";
	public static final int DROP = 768;
	private RC4 rc4;
	private long seq = 0;
	private Mac mac;

	// key = password, mackey = challengedata
	public KeyStream(byte[] key, byte[] macKey) {
		this.rc4 = new RC4(key, KeyStream.DROP);
		try {
			this.mac = Mac.getInstance("HmacSHA1");
			SecretKeySpec keySpec = new SecretKeySpec(macKey, "HmacSHA1");
			mac.init(keySpec);
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static byte[][] GenerateKeys(byte[] password64dec, byte[] nonce)
			throws PasswordException {
		final int keylen = 20;

		char[] password = new char[password64dec.length];
		for(int i = 0; i < password.length; i++){
			password[i] = (char)(password64dec[i] & 0xFF);
		}

		byte[][] array = new byte[4][keylen];
		byte[][] array2 = array;
		byte[] array3 = new byte[] { 1, 2, 3, 4 };
		byte[] array4 = new byte[nonce.length + 1];
		for (int i = 0; i < nonce.length; i++) {
			array4[i] = nonce[i];
		}
		nonce = array4;
		for (int j = 0; j < array2.length; j++) {
			nonce[nonce.length - 1] = array3[j];
			// foo = wa_pbkdf2("sha1", $password, $nonce, 2, 20, true);
			/*
			 * This works only with ASCII symbols PBEKeySpec keySpec = new
			 * PBEKeySpec(password, nonce, 2, keylen * 8);
			 * 
			 * SecretKeyFactory factory =
			 * SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1"); byte[]
			 * secretKey = factory.generateSecret(keySpec).getEncoded();
			 * System.arraycopy(secretKey, 0, array2[j], 0, keylen);
			 */

//			byte[] secretKey = calculatePasswordDigest(password, nonce, 2);
			byte[] secretKey = calculatePasswordDigest(password64dec, nonce, 2);
			System.arraycopy(secretKey, 0, array2[j], 0, keylen);
		}
		return array2;
	}

	// PBKDF2 with bouncycastle in Java
	// http://stackoverflow.com/questions/8674018/pbkdf2-with-bouncycastle-in-java/14242371#14242371
	private static byte[] calculatePasswordDigest(byte[] pass, byte[] salt,
			int iterations) throws PasswordException {
		byte[] derivedKey = null;
		try {
			/*
			 * JCE Version (does not work as BC uses PKCS12 encoding)
			 * SecretKeyFactory kf =
			 * SecretKeyFactory.getInstance("PBEWITHHMACSHA1","BC"); PBEKeySpec
			 * ks = new PBEKeySpec(pass, salt, iterations,160); SecretKey digest
			 * = kf.generateSecret(ks); return digest.getEncoded();
			 */
			PKCS5S2ParametersGenerator gen = new PKCS5S2ParametersGenerator();
//			gen.init(PBEParametersGenerator.PKCS5PasswordToBytes(pass), salt, iterations);
			gen.init(pass, salt, iterations);
			derivedKey = ((KeyParameter) gen.generateDerivedParameters(160))
					.getKey();
		} catch (Exception e) {
			throw new PasswordException("PasswordDigest generation error.");
		}
		return derivedKey;
	}

	public void DecodeMessage(byte[] buffer, int macOffset, int offset,
			int length) throws Exception {
		byte[] array = this.ComputeMac(buffer, offset, length);
		for (int i = 0; i < 4; i++) {
			if (buffer[macOffset + i] != array[i]) {
				throw new Exception(String.format(
						"MAC mismatch on index %d! %d != %d", i,
						buffer[macOffset + i], array[i]));
			}
		}
		// TODO kkk - need return data
		this.rc4.Cipher(buffer, offset, length);
	}

	public byte[] EncodeMessage(byte[] buffer, int macOffset, int offset,
			int length) throws IOException {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		byte[] data = this.rc4.Cipher(buffer, offset, length);
		byte[] mac = this.ComputeMac(data, offset, length);

		byte[] tbuf0 = new byte[macOffset];
		System.arraycopy(data, 0, tbuf0, 0, macOffset);
		stream.write(tbuf0);

		byte[] tbuf1 = new byte[4];
		System.arraycopy(mac, 0, tbuf1, 0, 4);
		stream.write(tbuf1);

		int nlen = data.length - macOffset - 4;
		byte[] tbuf2 = new byte[nlen];
		System.arraycopy(data, data.length - nlen, tbuf2, 0, nlen);
		stream.write(tbuf2);

		return stream.toByteArray();
	}

	private byte[] ComputeMac(byte[] buffer, int offset, int length) {
		byte[] array = new byte[] { (byte) (this.seq >> 24),
				(byte) (this.seq >> 16), (byte) (this.seq >> 8),
				(byte) this.seq };
		this.seq++;
		this.mac.update(buffer, offset, length);
		return this.mac.doFinal(array);
	}
}
