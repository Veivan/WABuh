package chatapi;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class BinTreeNodeWriter {
	private ByteArrayOutputStream output;
	/** @var $key KeyStream */
	private String key;

	public BinTreeNodeWriter() {
		this.output = new ByteArrayOutputStream();
	}

	public void resetKey() {
		this.key = null;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public byte[] StartStream(String domain, String resource) {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("to", domain);
		attributes.put("resource", resource);
		this.writeListStart(attributes.size() * 2 + 1);
		this.output.write(new byte[] {(byte)0x01}, this.output.size(), 1);
		this.writeAttributes(attributes);

        byte[] ret = this.flushBuffer();

        return "WA" + this.writeInt8(1) + this.writeInt8(5)
				+ this.flushBuffer();
	}

	/**
	 * @param ProtocolNode
	 *            node
	 * @param boolean encrypt
	 *
	 * @return String
	 */
	public String write(ProtocolNode node) {
		return write(node, true);
	}

	public String write(ProtocolNode node, boolean encrypt) {
		if (node == null)
			this.output += (char) 0x00;
		else
			this.writeInternal(node);

		return this.flushBuffer(encrypt);
	}

	/**
	 * @param ProtocolNode
	 *            node
	 */
	protected void writeInternal(ProtocolNode node) {
		int len = 1;
		if (node.getAttributes() != null)
			len += node.getAttributes().size() * 2;
		if (node.getChildren().size() > 0)
			len += 1;
		if (node.getData().length > 0)
			len += 1;

		this.writeListStart(len);
		this.writeString(node.getTag());
		this.writeAttributes(node.getAttributes());
		if (node.getData().length > 0)
			this.writeBytes(node.getData());

		if (node.getChildren() != null) {
			this.writeListStart(node.getChildren().size());
			for (ProtocolNode child : node.getChildren())
				this.writeInternal(child);
		}
	}

	protected String flushBuffer() {
		return this.flushBuffer(true);
	}

	protected String flushBuffer(boolean encrypt) {
		int size = this.output.size();
		String data = this.output;
		if (this.key != null && encrypt) {
			String bsize = this.getInt24(size);
			// encrypt
			// TODO kkk $data = $this->key->EncodeMessage($data, $size, 0,
			// $size);
			int len = data.length();

			char[] bsarr = bsize.toCharArray();

			bsarr[0] = (char) ((8 << 4) | ((len & 16711680) >> 16));
			bsarr[1] = (char) ((len & 65280) >> 8);
			bsarr[2] = (char) (len & 255);
			size = this.parseInt24(bsarr);
		}
		String ret = this.writeInt24(size) + data;
		this.output = "";
		return ret;
	}

	/*
	 * kkk private Character[] toCharacterArray( String s ) {
	 * 
	 * if ( s == null ) return null;
	 * 
	 * int len = s.length(); Character[] array = new Character[len]; for (int i
	 * = 0; i < len ; i++) { array[i] = new Character(s.charAt(i)); }
	 * 
	 * return array; }
	 */

	protected String getInt24(int length) {
		String ret = "";
		ret += (char) (((length & 0xf0000) >> 16));
		ret += (char) (((length & 0xff00) >> 8));
		ret += (char) (length & 0xff);
		return ret;
	}

	protected int parseInt24(char[] data) {
		int ret = (int) (data[0]) << 16;
		ret |= (int) (data[1]) << 8;
		ret |= (int) (data[2]) << 0;
		/*
		 * был разбор строки int ret = (int)(data.charAt(0)) << 16; ret |=
		 * (int)(data.charAt(1)) << 8; ret |= (int)(data.charAt(2)) << 0;
		 */
		return ret;
	}

	protected void writeListStart(int len) {
		if (len == 0) {
			this.output += (char) 0x00;
		} else if (len < 256) {
			this.output += (char) 0xf8 + len;
		} else
			this.output += (char) 0xf9 + this.writeInt16(len);
	}

	protected void writeAttributes(Map<String, String> attributes) {
		if (attributes != null) {
			for (Map.Entry<String, String> entry : attributes.entrySet()) {
				this.writeString(entry.getKey());
				this.writeString(entry.getValue());
			}
		}
	}

	protected void writeString(String tag) {
		int intVal = -1;
		boolean subdict = false;
		// TODO kkk if (TokenMap::TryGetToken($tag, $subdict, $intVal))
		if (true) {
			if (subdict)
				this.writeToken(236);
			this.writeToken(intVal);
			return;
		}
		int index = tag.indexOf('@');
		if (index > -1) {
			String server = tag.substring(index + 1);
			String user = tag.substring(0, index);
			this.writeJid(user, server);
		} else
			this.writeBytes(tag);
	}

	protected void writeBytes(byte[] bytes) {
		int len = bytes.length;
		if (len >= 0x100) {
			this.output += (char) 0xfd;
			this.output += this.writeInt24(len);
		} else {
			this.output += (char) 0xfc;
			this.output += this.writeInt8(len);
		}
		this.output += bytes;
	}

	protected void writeJid(String user, String server) {
		this.output += (char) 0xfa;
		if (user.length() > 0)
			this.writeString(user);
		else
			this.writeToken(0);
		this.writeString(server);
	}

	protected void writeToken(int token) {
		if (token < 0xf5) {
			this.output += (char) token;
		} else if (token <= 0x1f4) {
			this.output += (char) 0xfe + (char) (token - 0xf5);
		}
	}

	protected String writeInt8(int v) {
		String ret = "" + (char) (v & 0xff);
		return ret;
	}

	protected String writeInt16(int v) {
		String ret = "" + (char) ((v & 0xff00) >> 8);
		ret += (char) ((v & 0x00ff) >> 0);
		return ret;
	}

	protected String writeInt24(int v) {
		String ret = "" + (char) ((v & 0xff0000) >> 16);
		ret += (char) ((v & 0x00ff00) >> 8);
		ret += (char) ((v & 0x0000ff) >> 0);
		return ret;
	}

}
