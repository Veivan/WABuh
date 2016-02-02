package helper;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class KeyStream {

	public static final String AuthMethod = "WAUTH-2";
	public static final int DROP = 768;
	private RC4 rc4;
	private long seq;
	private byte[] macKey;

	public KeyStream(byte[] key, byte[] macKey) {
		this.rc4 = new RC4(key, this.DROP);
		this.macKey = macKey;
	}

	public static byte[][] GenerateKeys(char[] password, byte[] nonce) {
		byte[][] array = new byte[4][];
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
			// PBEKeySpec keySpec = new PBEKeySpec(password, nonce, 2, 20 * 8);
			PBEKeySpec keySpec = new PBEKeySpec(password, nonce, 2, 20);
			SecretKeyFactory factory;
			try {
				factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
				byte[] secretKey = factory.generateSecret(keySpec).getEncoded();
				System.arraycopy(secretKey, 0, array2[j], 0, 20);
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return array2;
	}

	public void DecodeMessage(byte[] buffer, int macOffset, int offset,
			int length) {
		/**
		 * TODO kkk $mac = $this->computeMac($buffer, $offset, $length);
		 * //validate mac for ($i = 0; $i < 4; $i++) { $foo =
		 * ord($buffer[$macOffset + $i]); $bar = ord($mac[$i]); if ($foo !==
		 * $bar) { throw new Exception("MAC mismatch: $foo != $bar"); } } return
		 * $this->rc4->cipher($buffer, $offset, $length);
		 */
	}

	public void EncodeMessage(byte[] buffer, int macOffset, int offset,
			int length) {
		/**
		 * TODO kkk $data = $this->rc4->cipher($buffer, $offset, $length); $mac
		 * = $this->computeMac($data, $offset, $length); return substr($data, 0,
		 * $macOffset) . substr($mac, 0, 4) . substr($data, $macOffset + 4);
		 */
	}

	private String computeMac(String buffer, String offset, String length) {
		/**
		 * TODO kkk $hmac = hash_init("sha1", HASH_HMAC, $this->macKey);
		 * hash_update($hmac, substr($buffer, $offset, $length)); $array =
		 * chr($this->seq >> 24) . chr($this->seq >> 16) . chr($this->seq >> 8)
		 * . chr($this->seq); hash_update($hmac, $array); $this->seq++; return
		 * hash_final($hmac, true);
		 */
		return "";
	}

/*	kkk - чтоли сам придумал? 
 * private static String encryptPassword(String password)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {

		MessageDigest crypt = MessageDigest.getInstance("SHA-1");
		crypt.reset();
		crypt.update(password.getBytes(WhatsAppBase.SYSEncoding));

		return new BigInteger(1, crypt.digest()).toString(16);
	} */
}
