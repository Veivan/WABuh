package mtest;

import java.util.Base64;

public class TestFuncs {
	public static void PrintHex(byte[] data) {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			String hexStr = Integer.toString(data[i] & 0xFF, 16);
			if (hexStr.length() == 1)
				buff.append("0" + hexStr + " ");
			else
				buff.append(hexStr + " ");
			// buff.append(data[i] + " ");
		}
		System.out.println(buff.toString());
	}

	public static byte[] encryptPassword(String password)
    {
//		String password = "123";

		try {
			return Base64.getDecoder().decode(password.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			return new byte[1];
		} 
    }

}
