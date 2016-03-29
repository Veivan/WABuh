package mtest;

public class TestFuncs {
	public static void PrintHex(byte[] data) {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			String hexStr = Integer.toString(data[i] & 0xFF, 16);
			buff.append(hexStr + " ");
			//buff.append(data[i] + " ");
		}
		System.out.println(buff.toString());
	}
}
