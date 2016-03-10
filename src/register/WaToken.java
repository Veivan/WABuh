package register;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

import base.ApiBase;
import base.WhatsAppBase;

public class WaToken {

	public static String GenerateRequestToken(String number, String platform)
			throws UnsupportedEncodingException {
		String result = "";
		switch (platform) {
		case "Nokia":
			String releaseTime = "1452554789539"; // 2.13.30
			String token = "PdA2DJyKoUrwLw1Bg6EIhzh502dF9noR9uFCllGk1447796090073"
					+ releaseTime + number;
			result = ApiBase.md5Custom(token).toLowerCase().replace("-", "");
			break;

		case "Android":
			String st = "eQV5aq/Cg63Gsq1sshN9T3gh+UUp0wIw0xgHYT1bnCjEqOJQKCRrWxdAe2yvsDeCJL+Y4G3PRD2HUF7oUgiGo8vGlNJOaux26k+A2F3hj8A=";
			String signature = "MIIDMjCCAvCgAwIBAgIETCU2pDALBgcqhkjOOAQDBQAwfDELMAkGA1UEBhMCVVMxEzARBgNVBAgTCkNhbGlmb3JuaWExFDASBgNVBAcTC1NhbnRhIENsYXJhMRYwFAYDVQQKEw1XaGF0c0FwcCBJbmMuMRQwEgYDVQQLEwtFbmdpbmVlcmluZzEUMBIGA1UEAxMLQnJpYW4gQWN0b24wHhcNMTAwNjI1MjMwNzE2WhcNNDQwMjE1MjMwNzE2WjB8MQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEUMBIGA1UEBxMLU2FudGEgQ2xhcmExFjAUBgNVBAoTDVdoYXRzQXBwIEluYy4xFDASBgNVBAsTC0VuZ2luZWVyaW5nMRQwEgYDVQQDEwtCcmlhbiBBY3RvbjCCAbgwggEsBgcqhkjOOAQBMIIBHwKBgQD9f1OBHXUSKVLfSpwu7OTn9hG3UjzvRADDHj+AtlEmaUVdQCJR+1k9jVj6v8X1ujD2y5tVbNeBO4AdNG/yZmC3a5lQpaSfn+gEexAiwk+7qdf+t8Yb+DtX58aophUPBPuD9tPFHsMCNVQTWhaRMvZ1864rYdcq7/IiAxmd0UgBxwIVAJdgUI8VIwvMspK5gqLrhAvwWBz1AoGBAPfhoIXWmz3ey7yrXDa4V7l5lK+7+jrqgvlXTAs9B4JnUVlXjrrUWU/mcQcQgYC0SRZxI+hMKBYTt88JMozIpuE8FnqLVHyNKOCjrh4rs6Z1kW6jfwv6ITVi8ftiegEkO8yk8b6oUZCJqIPf4VrlnwaSi2ZegHtVJWQBTDv+z0kqA4GFAAKBgQDRGYtLgWh7zyRtQainJfCpiaUbzjJuhMgo4fVWZIvXHaSHBU1t5w//S0lDK2hiqkj8KpMWGywVov9eZxZy37V26dEqr/c2m5qZ0E+ynSu7sqUD7kGx/zeIcGT0H+KAVgkGNQCo5Uc0koLRWYHNtYoIvt5R3X6YZylbPftF/8ayWTALBgcqhkjOOAQDBQADLwAwLAIUAKYCp0d6z4QQdyN74JDfQ2WCyi8CFDUM4CaNB+ceVXdKtOrNTQcc0e+t";
			String classesMd5 = "7UDPOXwpiLBvEjT8uNwsuA=="; // 2.12.440

			byte[] key0 = Base64.getDecoder().decode(signature.getBytes(WhatsAppBase.SYSEncoding));
			byte[] key1 = Base64.getDecoder().decode(classesMd5.getBytes(WhatsAppBase.SYSEncoding));
			byte[] key2 = Base64.getDecoder().decode(st.getBytes(WhatsAppBase.SYSEncoding));

			String data = new String(key0) + new String(key1) + number;
			// str_repeat(char, 64)
			String opadst = new String(new char[64]).replace("\0", ""
					+ (char) (0x5C));
			String ipadst = new String(new char[64]).replace("\0", ""
					+ (char) (0x36));

			byte[] opad = opadst.getBytes(WhatsAppBase.SYSEncoding);
			byte[] ipad = ipadst.getBytes(WhatsAppBase.SYSEncoding);

			for (int i = 0; i < 128; i++) {
				opad[i] = (byte) (opad[i] ^ key2[i]);
				ipad[i] = (byte) (ipad[i] ^ key2[i]);
			}

			String opadxor = new String(opad);
			String ipadxor = new String(ipad);

			String hash1 = ApiBase.toSHA1((ipadxor + data).getBytes(WhatsAppBase.SYSEncoding));
			String hash2 = ApiBase.toSHA1((opadxor + hash1).getBytes(WhatsAppBase.SYSEncoding));

			result = Base64.getEncoder().encodeToString(hash2.getBytes(WhatsAppBase.SYSEncoding));
		}

		return result;
	}

}
