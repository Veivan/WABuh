package helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import chatapi.Funcs;
import chatapi.ProtocolNode;
import base.WhatsAppBase;

public class BinTreeNodeReader {

	private ByteArrayOutputStream input;

	private KeyStream key;

	public BinTreeNodeReader() {
		this.input = new ByteArrayOutputStream();
	}

	public void resetKey() {
		this.key = null;
	}

	public void setKey(KeyStream key) {
		this.key = key;
	}

	public ProtocolNode nextTree() {
		try {
			return nextTree(null);
		} catch (Exception e) {
			// TODO
			e.printStackTrace();
			return null;
		}
	}

	public ProtocolNode nextTree(byte[] input) throws Exception {
		if (input != null)
			this.input.write(input);

		int firstByte = this.peekInt8();
		int stanzaFlag = (firstByte & 0xF0) >> 4; // ENCRYPTED
		/*
		 * $isCompressed = (0x400000 & $firstByte) > 0; $isEncrypted = (0x800000
		 * & $firstByte) > 0;
		 */
		int stanzaSize = this.peekInt16(1) | ((firstByte & 0x0F) << 16);

		if (stanzaSize > this.input.size()) {
			throw new Exception("Incomplete message stanzaSize != "
					+ this.input.size());
		}
		this.readInt24();

		boolean isEncrypted = (stanzaFlag & 8) != 0;
		if (isEncrypted) {
			if (this.key != null) {
				int realStanzaSize = stanzaSize - 4;
				int macOffset = stanzaSize - 4;
				byte[] treeData = this.input.toByteArray();
				this.key.DecodeMessage(treeData, macOffset, 0, realStanzaSize);
				if ((stanzaFlag & 4) != 0) { // compressed
					treeData = inflateBuffer(treeData); // done
				}

				this.input.reset();
				byte[] subArray = Arrays.copyOfRange(treeData, 0,
						realStanzaSize);
				this.input.write(subArray);
			} else {
				throw new Exception(
						"Received encrypted message, encryption key not set");
			}
		}

		if (stanzaSize > 0)
			return this.nextTreeInternal();
		else
			return null;
	}

	// TODO kkk NOT USED ANYWHERE
	protected String readNibble() throws Exception {
		int nextbyte = this.readInt8();

		boolean ignoreLastNibble = (nextbyte & 0x80) != 0;
		int size = (nextbyte & 0xFF);
		int nrOfNibbles = size * 2 - (ignoreLastNibble ? 1 : 0);

		byte[] data = this.fillArray(size);
		String retstring = "";

		for (int i = 0; i < nrOfNibbles; i++) {
			nextbyte = data[(int) Math.floor(i / 2.0)] & 0xFF;

			int shift = 4 * (1 - i % 2);
			byte decimal = (byte) ((nextbyte & (15 << shift)) >> shift);

			switch (decimal) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
				retstring += decimal;
				break;
			case 10:
				// retstring += "-";
			case 11:
				// retstring += ".";
				retstring += (char) (decimal - 10 + 45);
				break;
			default:
				throw new Exception("Bad nibble: " + decimal);
			}
		}

		return retstring;
	}

	protected String getToken(int token) throws Exception {
		TokenMap tMap = new TokenMap();
		tMap.GetToken(token);
		if (tMap.str == null) {
			token = this.readInt8();
			tMap.GetToken(token);
		}
		return tMap.str;
	}

	protected String getTokenDouble(int n, int n2) throws Exception {
		int pos = n2 + n * 256;
		TokenMap tMap = new TokenMap();
		tMap.GetToken(pos);
		return tMap.str;
	}

	protected byte[] readBytes(int token) throws Exception {
		byte[] ret = null;

		if (token == -1) {
			throw new Exception("BinTreeNodeReader->readString: Invalid token "
					+ token);
		}

		if ((token > 2) && (token < 236)) {
			ret = this.getToken(token).getBytes(WhatsAppBase.SYSEncoding);
		} else

			switch (token) {
			case 0:
				ret = new byte[0];
				break;
			case 236:
			case 237:
			case 238:
			case 239:
				int token2 = this.readInt8();
				ret = this.getTokenDouble(token - 236, token2).getBytes(
						WhatsAppBase.SYSEncoding);
				break;
			case 250: {
				String user = new String(this.readBytes(this.readInt8()));
				String server = new String(this.readBytes(this.readInt8()));
				if ((user.length() > 0) && (server.length() > 0)) {
					ret = (user + "@" + server)
							.getBytes(WhatsAppBase.SYSEncoding);
				} else if (server.length() > 0) {
					ret = server.getBytes(WhatsAppBase.SYSEncoding);
				}
				break;
			}
			case 251:
			case 255:
				// ret = this.readNibble().getBytes(WhatsAppBase.SYSEncoding);
				ret = this.readPacked8(token); // maybe utf8 decode
				break;
			case 252: {
				int size = this.readInt8();
				ret = this.fillArray(size);
				break;
			}
			case 253: {
				int size = this.readInt20();
				ret = this.fillArray(size);
				break;
			}
			case 254: {
				int tmpToken = this.readInt31();
				ret = this.getToken(tmpToken + 0xf5).getBytes(
						WhatsAppBase.SYSEncoding);
				break;
			}
			}

		return ret;
	}

	protected byte[] readPacked8(int n) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		int len = this.readInt8();
		int remove = 0;
		if ((len & 0x80) != 0 && n == 251)
			remove = 1;

		len = len & 0xFF;
		byte[] text = Arrays.copyOfRange(this.input.toByteArray(), 0, len);
		this.input.reset();
		this.input.write(text);

		byte[] data = Funcs.bin2hex(new String(text)).getBytes();
		len = data.length;
		for (int i = 0; i < len; i++) {
			String sval = Funcs.hex2bin("0" + (data[i] & 0xFF));
			int val = Integer.parseInt(sval, 10);
			if (i == (len - 1) && val > 11 && n != 251) {
				continue;
			}
			out.write(this.unpackByte(n, val));
		}

		byte[] tempdata = out.toByteArray();
		return Arrays.copyOfRange(tempdata, 0, tempdata.length - remove);
	}

	protected int unpackByte(int n, int n2) throws Exception {
		switch (n) {
		case 251:
			return this.unpackHex(n2);
		case 255:
			return this.unpackNibble(n2);
		default:
			throw new Exception("bad packed type " + n);
		}
	}

	protected int unpackHex(int n) throws Exception {
		switch (n) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
			return n + 48;
		case 10:
		case 11:
		case 12:
		case 13:
		case 14:
		case 15:
			return 65 + (n - 10);
		default:
			throw new Exception("bad hex " + n);
		}
	}

	protected int unpackNibble(int n) throws Exception {
		switch (n) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
			return n + 48;
		case 10:
		case 11:
			return 45 + (n - 10);
		default:
			throw new Exception("bad nibble " + n);
		}
	}

	protected Map<String, String> readAttributes(int size) {
		Map<String, String> attributes = new HashMap<String, String>();
		int attribCount = (size - 2 + size % 2) / 2;
		for (int i = 0; i < attribCount; i++) {
			try {
				String key = new String(this.readBytes(this.readInt8()));
				String value = new String(this.readBytes(this.readInt8()));
				attributes.put(key, value);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return attributes;
	}

	private byte[] inflateBuffer(byte[] data) throws DataFormatException,
			IOException {
		Inflater inflater = new Inflater();
		inflater.setInput(data);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
				data.length);
		byte[] buffer = new byte[1024];
		while (!inflater.finished()) {
			int count = inflater.inflate(buffer);
			outputStream.write(buffer, 0, count);
		}
		outputStream.close();
		byte[] output = outputStream.toByteArray();
		return output;
	}

	protected ProtocolNode nextTreeInternal() throws Exception {
		Map<String, String> attributes = new HashMap<String, String>();

		int token = this.readInt8();
		int size = this.readListSize(token);
		if (size == 0)
			throw new Exception("nextTree sees 0 list or null tag");

		token = this.readInt8();

		if (token == 1) {
			token = this.readInt8();
		}
		if (token == 2) {
			return null;
		}

		String tag = new String(this.readBytes(token));
		attributes = this.readAttributes(size);
		if ((size % 2) == 1)
			return new ProtocolNode(tag, attributes, null, null);

		token = this.readInt8();
		if (this.isListTag(token))
			return new ProtocolNode(tag, attributes, this.readList(token), null);

		int len = 0;
		byte[] data = null;
		switch (token) {
		case 252:
			len = this.readInt8();
			data = this.fillArray(len);
			return new ProtocolNode(tag, attributes, null, data);
		case 253:
			len = this.readInt20();
			data = this.fillArray(len);
			return new ProtocolNode(tag, attributes, null, data);
		case 254:
			len = this.readInt31();
			data = this.fillArray(len);
			return new ProtocolNode(tag, attributes, null, data);
		case 255:
		case 251:
			return new ProtocolNode(tag, attributes, null,
					this.readPacked8(token));
		default:
			return new ProtocolNode(tag, attributes, null,
					this.readBytes(token));
		}

	}

	protected boolean isListTag(int token) {
		return (token == 248 || token == 0 || token == 249);
	}

	protected ArrayList<ProtocolNode> readList(int token) throws Exception {
		int size = this.readListSize(token);
		ArrayList<ProtocolNode> ret = new ArrayList<ProtocolNode>();
		for (int i = 0; i < size; i++)
			ret.add(this.nextTreeInternal());
		return ret;
	}

	protected int readListSize(int token) throws Exception {
		if (token == 0)
			return 0;

		if (token == 0xf8) {
			return this.readInt8();
		} else if (token == 0xf9) {
			return this.readInt16();
		}

		throw new Exception(
				"BinTreeNodeReader->readListSize: invalid list size in readListSize: token "
						+ token);
	}

	protected int peekInt8() {
		return peekInt8(0);
	}

	protected int peekInt8(int offset) {
		int ret = 0;
		if (this.input.size() >= (1 + offset)) {
			ret = this.input.toByteArray()[offset] & 0xFF;
		}
		return ret;
	}

	protected int peekInt16() {
		return peekInt16(0);
	}

	protected int peekInt16(int offset) {
		int ret = 0;
		if (this.input.size() >= (2 + offset)) {
			byte[] buffer = input.toByteArray();
			ret = (buffer[offset] & 0xFF) << 8; 
			ret |= buffer[offset + 1] & 0xFF;
		}
		return ret;
	}

	protected int peekInt24() {
		return peekInt24(0);
	}

	protected int peekInt24(int offset) {
		int ret = 0;
		if (this.input.size() >= (3 + offset)) {
			byte[] buffer = input.toByteArray();
			ret = ((buffer[offset] & 0xFF) << 16) + ((buffer[offset + 1] & 0xFF) << 8)
					+ (buffer[offset + 2] & 0xFF);
		}
		return ret;
	}

	protected int peekInt20() {
		return peekInt20(0);
	}

	protected int peekInt20(int offset) {
		int ret = 0;
		if (this.input.size() >= (3 + offset)) {
			byte[] buffer = input.toByteArray();
			ret = ((buffer[offset] & 0xFF) << 16) + ((buffer[offset + 1] & 0xFF) << 8)
					+ (buffer[offset + 2] & 0xFF);
		}
		return ret;
	}

	protected int peekInt31() {
		return peekInt31(0);
	}

	protected int peekInt31(int offset) {
		int ret = 0;
		if (this.input.size() >= (4 + offset)) {
			byte[] buffer = input.toByteArray();
			ret = ((buffer[offset] & 0xFF) << 24) + ((buffer[offset + 1] & 0xFF) << 16)
					+ ((buffer[offset + 2] & 0xFF) << 8) + (buffer[offset + 3] & 0xFF);
		}
		return ret;
	}

	public int readHeader() {
		return readHeader(0);
	}

	public int readHeader(int offset) {
		int ret = 0;
		if (this.input.size() >= (3 + offset)) {
			byte[] buffer = input.toByteArray();
			ret = (buffer[offset] & 0xFF) + ((buffer[offset + 1] & 0xFF) << 16)
					+ ((buffer[offset + 2] & 0xFF) << 8);
		}
		return ret;
	}

	protected int readInt8() {
		int ret = this.peekInt8();
		if (this.input.size() >= 1) {
			byte[] buffer = input.toByteArray();
			input.reset();
			input.write(buffer, 1, buffer.length - 1);
		}
		return ret;
	}

	protected int readInt16() {
		int ret = this.peekInt16();
		if (this.input.size() >= 2) {
			byte[] buffer = input.toByteArray();
			input.reset();
			input.write(buffer, 2, buffer.length - 2);
		}
		return ret;
	}

	protected int readInt24() {
		int ret = this.peekInt24();
		if (this.input.size() >= 3) {
			byte[] buffer = input.toByteArray();
			input.reset();
			input.write(buffer, 3, buffer.length - 3);
		}
		return ret;
	}

	protected int readInt20() {
		int ret = this.peekInt20();
		if (this.input.size() >= 3) {
			byte[] buffer = input.toByteArray();
			input.reset();
			input.write(buffer, 3, buffer.length - 3);
		}
		return ret;
	}

	protected int readInt31() {
		int ret = this.peekInt31();
		if (this.input.size() >= 4) {
			byte[] buffer = input.toByteArray();
			input.reset();
			input.write(buffer, 4, buffer.length - 4);
		}
		return ret;
	}

	protected byte[] fillArray(int len) throws Exception {
		byte[] ret = new byte[len];
		byte[] buffer = input.toByteArray();
		if (buffer.length >= len) {
			System.arraycopy(buffer, 0, ret, 0, len);
			input.reset();
			input.write(buffer, len, buffer.length - len);
		} else {
			throw new Exception();
		}
		return ret;
	}
}
