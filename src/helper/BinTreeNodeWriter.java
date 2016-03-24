package helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import chatapi.ProtocolNode;
import base.WhatsAppBase;

public class BinTreeNodeWriter {
	public ByteArrayOutputStream buffer;
	// private ByteArrayOutputStream buffer;
	private KeyStream key;

	public BinTreeNodeWriter() {
		this.buffer = new ByteArrayOutputStream();
	}

	public void resetKey() {
		this.key = null;
	}

	public void setKey(KeyStream key) {
		this.key = key;
	}

	public byte[] StartStream(String domain, String resource) throws Exception {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("to", domain);
		attributes.put("resource", resource);
		this.writeListStart(attributes.size() * 2 + 1);

		this.buffer.write(0x1);
		this.writeAttributes(attributes);

		byte[] ret = this.flushBuffer();

		this.buffer.write((byte) 'W');
		this.buffer.write((byte) 'A');
		this.buffer.write(0x1);
		this.buffer.write(0x6);
		buffer.write(ret, 0, ret.length);
		ret = buffer.toByteArray();
		this.buffer.reset();
		return ret;
	}

	/**
	 * @param ProtocolNode
	 *            node
	 * @param boolean encrypt
	 *
	 * @return byte[]
	 * @throws Exception 
	 */
	public byte[] write(ProtocolNode node) throws Exception {
		return write(node, true);
	}

	public byte[] write(ProtocolNode node, boolean encrypt) throws Exception {
		if (node == null)
			this.buffer.write(0);
		else
			this.writeInternal(node);

		return this.flushBuffer(encrypt);
	}

	/**
	 * @param ProtocolNode
	 *            node
	 * @throws Exception
	 */
	protected void writeInternal(ProtocolNode node) throws Exception {
		int len = 1;
		if (node.getAttributes() != null)
			len += node.getAttributes().size() * 2;
		ArrayList<ProtocolNode> children = node.getChildren();
		if (children != null && children.size() > 0)
			len += 1;
		byte[] data = node.getData();
		if (data != null && data.length > 0)
			len += 1;

		this.writeListStart(len);
		this.writeString(node.getTag());
		this.writeAttributes(node.getAttributes());
		if (data != null && data.length > 0)
			this.writeBytes(data);

		if (children != null && children.size() > 0) {
			this.writeListStart(children.size());
			for (ProtocolNode child : children)
				this.writeInternal(child);
		}
	}

	protected byte[] flushBuffer() {
		return this.flushBuffer(true);
	}

	public byte[] flushBuffer(boolean encrypt) {
		byte[] data = this.buffer.toByteArray();

		byte[] size = this.getInt24(data.length);
		if (this.key != null && encrypt) {
			byte[] paddedData = new byte[data.length + 4];
			System.arraycopy(data, 0, paddedData, 0, data.length);

			// encrypt
			this.key.EncodeMessage(paddedData, paddedData.length - 4, 0,
					paddedData.length - 4);
			data = paddedData;

			// add encryption signature
			long encryptedBit = 8L;
			long dataLength = data.length;
			size[0] = (byte) ((long) (encryptedBit << 4) | (long) ((dataLength & 16711680L) >> 16));
			size[1] = (byte) ((dataLength & 65280L) >> 8);
			size[2] = (byte) (dataLength & 255L);
		}

		byte[] ret = new byte[data.length + 3];
		System.arraycopy(size, 0, ret, 0, 3);
		System.arraycopy(data, 0, ret, 3, data.length);
		this.buffer.reset();
		return ret;
	}

	protected void writeAttributes(Map<String, String> attributes)
			throws Exception {
		if (attributes != null) {
			for (Map.Entry<String, String> entry : attributes.entrySet()) {
				this.writeString(entry.getKey());
				this.writeString(entry.getValue(), true);
			}
		}
	}

	protected byte[] getInt24(int len) {
		byte[] ret = new byte[3];
		ret[0] = (byte) ((len & 0xf0000) >> 16);
		ret[1] = (byte) ((len & 0xff00) >> 8);
		ret[2] = (byte) (len & 0xff);
		return ret;
	}

	protected void writeBytes(byte[] bytes) throws IOException {
		writeBytes(bytes, false);
	}

	protected void writeBytes(byte[] bytes, boolean packed) throws IOException {
		int len = bytes.length;

		byte[] toWrite = bytes;
		if (len >= 0x100000) {
			this.buffer.write(0xfe);
			this.writeInt31(len);
		} else if (len >= 0x100) {
			this.buffer.write(0xfd);
			this.writeInt20(len);
		} else {
			String r = "";
			if (packed && len < 128) {
				r = this.tryPackAndWriteHeader(255, bytes);
				if (r.isEmpty())
					r = this.tryPackAndWriteHeader(251, bytes);
				if (r.isEmpty()) {
					this.buffer.write(0xfc);
					this.writeInt8(len);
				} else
					toWrite = r.getBytes(WhatsAppBase.SYSEncoding);
			}
		}
		this.buffer.write(toWrite);
	}

	private int packHex(int n) {
		switch (n) {
		case 48:
		case 49:
		case 50:
		case 51:
		case 52:
		case 53:
		case 54:
		case 55:
		case 56:
		case 57:
			return n - 48;
		case 65:
		case 66:
		case 67:
		case 68:
		case 69:
		case 70:
			return 10 + (n - 65);
		default:
			return -1;
		}
	}

	private int packNibble(int n) {
		switch (n) {
		case 45:
		case 46:
			return 10 + (n - 45);
		case 48:
		case 49:
		case 50:
		case 51:
		case 52:
		case 53:
		case 54:
		case 55:
		case 56:
		case 57:
			return n - 48;
		default:
			return -1;
		}
	}

	private int packByte(int v, int n2) {
		switch (v) {
		case 251:
			return this.packHex(n2);
		case 255:
			return this.packNibble(n2);
		default:
			return -1;
		}
	}

	private String tryPackAndWriteHeader(int v, byte[] data) {
		int length = data.length;
		if (length >= 128)
			return "";

		ArrayList<Integer> array2 = new ArrayList<Integer>();
		for (int i = 0; i < (int) Math.floor(length + 1 / 2.0); i++) {
			array2.add(0);
		}

		for (int i = 0; i < length; i++) {
			int packByte = this.packByte(v, (int) data[i]);
			if (packByte == -1) {
				array2.clear();
				break;
			}
			int n2 = (int) Math.floor(i / 2.0);
			int prev = array2.get(n2);
			prev |= (packByte << 4 * (1 - i % 2));
			array2.set(n2, prev);
		}
		if (array2.size() > 0) {
			if (length % 2 == 1) {
				int prev = array2.get(array2.size() - 1);
				prev |= 0xF;
				array2.set(array2.size() - 1, prev);
			}
			char[] charArray = new char[array2.size()];
			for (int i = 0; i < array2.size(); i++) {
				int t = array2.get(i);
				charArray[i] = (char) t;
			}
			String str = new String(charArray);
			this.buffer.write((char) v);
			this.writeInt8(length % 2 << 7 | charArray.length);
			return str;
		}
		return "";
	}

	protected void writeInt8(int v) {
		this.buffer.write((byte) (v & 0xff));
	}

	protected void writeInt16(int v) {
		this.buffer.write((byte) ((v & 0xff00) >> 8));
		this.buffer.write((byte) (v & 0x00ff));
	}

	protected void writeInt24(int v) {
		this.buffer.write((byte) ((v & 0xff0000) >> 16));
		this.buffer.write((byte) ((v & 0x00ff00) >> 8));
		this.buffer.write((byte) (v & 0x0000ff));
	}

	protected void writeInt20(int v) {
		this.buffer.write((byte) ((v & 0xf0000) >> 16));
		this.buffer.write((byte) ((v & 0xff00) >> 8));
		this.buffer.write((byte) (v & 0xff));
	}

	private void writeInt31(int v) {
		this.buffer.write((byte) ((v & 0x7F000000) >> 24));
		this.buffer.write((byte) ((v & 0xff0000) >> 16));
		this.buffer.write((byte) ((v & 0xff00) >> 8));
		this.buffer.write((byte) (v & 0xff));
	}

	protected void writeJid(String user, String server) throws Exception {
		this.buffer.write(0xfa); // 250
		if (user.length() > 0)
			this.writeString(user, true);
		else
			this.writeToken(0);
		this.writeString(server);
	}

	protected void writeListStart(int len) {
		if (len == 0) {
			this.buffer.write(0x00);
		} else if (len < 256) {
			this.buffer.write(0xf8);
			this.writeInt8(len);
		} else {
			this.buffer.write(0xf9);
			this.writeInt16(len);
		}
	}

	protected void writeString(String tag) throws Exception {
		writeString(tag, false);
	}

	protected void writeString(String tag, boolean packed) throws Exception {
		TokenMap tMap = new TokenMap();
		if (tMap.TryGetToken(tag)) {
			if (tMap.subdict)
				this.writeToken(236);
			this.writeToken(tMap.token);
			return;
		}
		int index = tag.indexOf('@');
		if (index > -1) {
			String server = tag.substring(index + 1);
			String user = tag.substring(0, index);
			this.writeJid(user, server);
		} else
			this.writeBytes(tag.getBytes(WhatsAppBase.SYSEncoding), packed);
	}

	protected void writeToken(int token) throws Exception {
		if (token < 255 && token >= 0) {
			this.buffer.write(token);
		} else {
			throw new Exception("Invalid token.");
		}
	}

}
