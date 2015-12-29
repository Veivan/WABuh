package mtest;

import java.io.UnsupportedEncodingException;

import javax.xml.bind.DatatypeConverter;

public class TestMain {

	public static void main(String[] args) throws UnsupportedEncodingException {
		/*/ String hexString =
		// "fd00000aa8660b5b010006acdc0100000101000100010000";
		String hexString = "f8";
		byte[] bytes = DatatypeConverter.parseHexBinary(hexString);
		System.out.println(new String(bytes, "UTF-8"));
		System.out.println((char) 0xf8); */
		
		byte[] buf = new byte[5];
		buf[0] = 1;
		buf[4] = 1;
		String data = new String(buf, 0, 5);
		System.out.println(data);
		System.out.println(data.length());
	}

}
