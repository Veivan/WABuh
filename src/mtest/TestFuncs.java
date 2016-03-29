package mtest;

public class TestFuncs {
    public static void PrintHex(byte[] data) {
		StringBuffer buff = new StringBuffer();
  		for (int i = 0; i < data.length; i++)
		{
    		buff.append(data[i] + " ");
		}
		System.out.println(buff.toString());
    }
}
