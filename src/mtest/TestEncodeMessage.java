package mtest;

import helper.KeyStream;

public class TestEncodeMessage {
    public static void RunTect() throws Exception {
		byte[] key = "a".getBytes();
		byte[] mackey = {0};
		KeyStream ks = new KeyStream(key, mackey);
		byte[] data = "hello".getBytes("UTF-8");
		data = ks.EncodeMessage(data, 0, 4, data.length - 4);
		System.out.println(new String(data));
	  }

}
