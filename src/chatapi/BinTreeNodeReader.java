package chatapi;

import helper.KeyStream;
import helper.TokenMap;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
			e.printStackTrace();
			return null;
		}
	}

	public ProtocolNode nextTree(byte[] input) throws Exception {
		if (input != null)
			this.input.write(input);

		int firstByte = this.peekInt8();
		int stanzaFlag = (firstByte & 0xF0) >> 4;
		int stanzaSize = this.peekInt16(1) | ((firstByte & 0x0F) << 16);

		if (stanzaSize > this.input.size()) {
			throw new Exception("Incomplete message stanzaSize != "
					+ this.input.size());
		}
		this.readInt24();

		/*
		 * TODO kkk if ($stanzaFlag & 8) { if (isset($this->key)) { $realSize =
		 * $stanzaSize - 4; $this->input =
		 * $this->key->DecodeMessage($this->input, $realSize, 0, $realSize);// .
		 * $remainingData; } else { throw new
		 * Exception("Encountered encrypted message, missing key"); } }
		 */

		if (stanzaSize > 0)
			return this.nextTreeInternal();
		else
			return null;
	}

	protected ProtocolNode nextTreeInternal() throws Exception {
		Map<String, String> attributes = new HashMap<String, String>();

		int token = this.readInt8();
		int size = this.readListSize(token);
		token = this.readInt8();
		if (token == 1) {
			this.readAttributes(attributes, size);
			return new ProtocolNode("start", attributes, null, null);
		} else if (token == 2) {
			return null;
		}

		String tag = new String(this.readBytes(token));
		attributes.clear();
		this.readAttributes(attributes, size);
		if ((size % 2) == 1)
			return new ProtocolNode(tag, attributes, null, null);

		token = this.readInt8();
		if (this.isListTag(token))
			return new ProtocolNode(tag, attributes, this.readList(token), null);

		return new ProtocolNode(tag, attributes, null, this.readBytes(token));
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

	protected void readAttributes(Map<String, String> attributes, int size) {
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
	}

	protected byte[] readBytes(int token) throws Exception {
		byte[] ret = null;

		if (token == -1) {
			throw new Exception("BinTreeNodeReader->readString: Invalid token "
					+ token);
		}

		if ((token > 2) && (token < 245)) {
			ret = this.getToken(token);
		} else

			switch (token) {
			case 0:
				ret = new byte[0];
				break;
			case 252: {
				int size = this.readInt8();
				ret = this.fillArray(size);
				break;
			}
			case 253: {
				int size = this.readInt24();
				ret = this.fillArray(size);
				break;
			}
			case 250: {
				String user = new String(this.readBytes(this.readInt8()));
				String server = new String(this.readBytes(this.readInt8()));
				if ((user.length() > 0) && (server.length() > 0)) {
					ret = (user + "@" + server).getBytes(WhatsAppBase.SYSEncoding);
				} else if (server.length() > 0) {
					ret = server.getBytes(WhatsAppBase.SYSEncoding);
				}
			}
			case 254: {
				int tmpToken = this.readInt8();
				ret = this.getToken(tmpToken + 0xf5);
			}
			case 255:
				ret = this.readNibble().getBytes(WhatsAppBase.SYSEncoding);
				break;
			}

		return ret;
	}

	protected String readNibble() throws Exception {
		int nextbyte = this.readInt8();

		boolean ignoreLastNibble = (nextbyte & 0x80) != 0;
		int size = (nextbyte & 0x7f);
		int nrOfNibbles = size * 2 - (ignoreLastNibble ? 1 : 0);

		byte[] data = this.fillArray(size);
		String retstring = "";

		for (int i = 0; i < nrOfNibbles; i++) {
			nextbyte = data[(int) Math.floor(i / 2.0)];

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
				retstring += "-";
			case 11:
				retstring += ".";
				break;
			default:
				throw new Exception("Bad nibble: " + decimal);
			}
		}

		return retstring;
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

	protected String getToken(int token) throws Exception {
		TokenMap tMap = new TokenMap();
		tMap.GetToken(token);	
		if (tMap.str == null){
			token = this.readInt8();
			tMap.GetToken(token);
		}		
		return tMap.str;
	}

	protected int readListSize(int token) throws Exception {
		if (token == 0xf8) {
			return this.readInt8();
		} else if (token == 0xf9) {
			return this.readInt16();
		}

		throw new Exception("BinTreeNodeReader->readListSize: Invalid token "
				+ token);
	}

	protected int peekInt8() {
		return peekInt8(0);
	}

	protected int peekInt8(int offset) {
		int ret = 0;
		if (this.input.size() >= (1 + offset)) {
			ret = (int) this.input.toByteArray()[offset];
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
			ret = (int) buffer[0 + offset] << 8;
			ret |= (int) buffer[1 + offset] << 0;
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
			ret = (buffer[0 + offset] << 16) + (buffer[1 + offset] << 8)
					+ buffer[2 + offset];
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

}
