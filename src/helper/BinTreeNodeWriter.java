package helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import chatapi.ProtocolNode;
import base.WhatsAppBase;

public class BinTreeNodeWriter {
	private ByteArrayOutputStream buffer;
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

	public byte[] StartStream(String domain, String resource) throws IOException {
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
		this.buffer.write(0x5);
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
	 */
	public byte[] write(ProtocolNode node) {
		return write(node, true);
	}

	public byte[] write(ProtocolNode node, boolean encrypt) {
		if (node == null)
			this.buffer.write(0);
		else
			try {
				this.writeInternal(node);
			} catch (IOException e) {
				e.printStackTrace();
			}

		return this.flushBuffer(encrypt);
	}

	protected byte[] flushBuffer() {
		return this.flushBuffer(true);
	}

	protected byte[] flushBuffer(boolean encrypt) {
		byte[] data = this.buffer.toByteArray();
		byte[] data2 = new byte[data.length + 4];
		System.arraycopy(data, 0, data2, 0, data.length);

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
		System.arraycopy(data, 0, ret, 0, data.length);
		this.buffer.reset();
		return ret;
	}

	protected void writeAttributes(Map<String, String> attributes) throws IOException {
		if (attributes != null) {
			for (Map.Entry<String, String> entry : attributes.entrySet()) {
				this.writeString(entry.getKey());
				this.writeString(entry.getValue());
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

	protected void writeBytes(String bytes) throws IOException {
		writeBytes(bytes.getBytes(WhatsAppBase.SYSEncoding));
	}

	protected void writeBytes(byte[] bytes) throws IOException {
		int len = bytes.length;
		if (len >= 0x100) {
			this.buffer.write(0xfd);
			this.writeInt24(len);
		} else {
			this.buffer.write(0xfc);
			this.writeInt8(len);
		}
		this.buffer.write(bytes);
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

	/**
	 * @param ProtocolNode
	 *            node
	 * @throws IOException
	 */
	protected void writeInternal(ProtocolNode node) throws IOException {
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

	protected void writeJid(String user, String server) throws IOException {
        this.buffer.write(0xfa);
        if (user.length() > 0)
            this.writeString(user);
        else
            this.writeToken(0);
        this.writeString(server);
	}

	protected void writeListStart(int len) {
        if (len == 0)
        {
            this.buffer.write(0x00);
        }
        else if (len < 256)
        {
            this.buffer.write(0xf8);
            this.writeInt8(len);
        }
        else
        {
            this.buffer.write(0xf9);
            this.writeInt16(len);
        }
	}

	protected void writeString(String tag) throws IOException {       
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
			this.writeBytes(tag);
	}

	protected void writeToken(int token) {
        if (token < 0xf5)
        {
            this.buffer.write((byte)token);
        }
        else if (token <= 0x1f4)
        {
            this.buffer.write(0xfe);
            this.buffer.write((byte)(token - 0xf5));
        }
	}

}
