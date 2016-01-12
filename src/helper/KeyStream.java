package helper;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

	public static void GenerateKeys(String password, String nonce) {
		/**
		 * TODO kkk $array = array( "key", //placeholders "key", "key", "key" );
		 * $array2 = array(1, 2, 3, 4); $nonce .= '0'; for ($j = 0; $j <
		 * count($array); $j++) { $nonce[(strlen($nonce) - 1)] =
		 * chr($array2[$j]); $foo = wa_pbkdf2("sha1", $password, $nonce, 2, 20,
		 * true); $array[$j] = $foo; } return $array;
		 */
	}

	public void DecodeMessage(String buffer, String macOffset, String offset,
			String length) {
		/**
		 * TODO kkk $mac = $this->computeMac($buffer, $offset, $length);
		 * //validate mac for ($i = 0; $i < 4; $i++) { $foo =
		 * ord($buffer[$macOffset + $i]); $bar = ord($mac[$i]); if ($foo !==
		 * $bar) { throw new Exception("MAC mismatch: $foo != $bar"); } } return
		 * $this->rc4->cipher($buffer, $offset, $length);
		 */
	}

	public String EncodeMessage(String buffer, String macOffset, String offset,
			String length) {
		/**
		 * TODO kkk $data = $this->rc4->cipher($buffer, $offset, $length); $mac
		 * = $this->computeMac($data, $offset, $length); return substr($data, 0,
		 * $macOffset) . substr($mac, 0, 4) . substr($data, $macOffset + 4);
		 */
		return "";
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

	private static String encryptPassword(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {

	    MessageDigest crypt = MessageDigest.getInstance("SHA-1");
	    crypt.reset();
	    crypt.update(password.getBytes("UTF-8"));

	    return new BigInteger(1, crypt.digest()).toString(16);
	}	
}
