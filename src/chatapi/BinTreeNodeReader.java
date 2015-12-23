package chatapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BinTreeNodeReader {

	private String input;
	/** @var $key KeyStream */
	private String key;

	public void resetKey() {
		this.key = null;
	}

	public void setKey(String key) {
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

	public ProtocolNode nextTree(String input) throws Exception {
		if (input != null)
			this.input = input;

		int firstByte = this.peekInt8();
		int stanzaFlag = (firstByte & 0xF0) >> 4;
		int stanzaSize = this.peekInt16(1) | ((firstByte & 0x0F) << 16);

		if (stanzaSize > this.input.length()) {
			throw new Exception("Incomplete message stanzaSize != "
					+ this.input.length());
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
			return new ProtocolNode("start", attributes, null, "");
		} else if (token == 2) {
			return null;
		}

		String tag = this.readString(token);
		attributes.clear();
		this.readAttributes(attributes, size);
		if ((size % 2) == 1)
			return new ProtocolNode(tag, attributes, null, "");

		token = this.readInt8();
		if (this.isListTag(token))
			return new ProtocolNode(tag, attributes, this.readList(token), "");

		return new ProtocolNode(tag, attributes, null, this.readString(token));
	}

    protected boolean isListTag(int token)
    {
        return (token == 248 || token == 0 || token == 249);
    }
    
    protected ArrayList<ProtocolNode> readList(int token) throws Exception
    {
        int size = this.readListSize(token);
		ArrayList<ProtocolNode> ret = new ArrayList<ProtocolNode>();
		for (int i = 0; i < size; i++) 
			ret.add(this.nextTreeInternal());
        return ret;
    }

    protected void readAttributes(Map<String, String> attributes, int size) {
		int attribCount = (size - 2 + size % 2) / 2;
		for (int i = 0; i < attribCount; i++) {
			String key;
			try {
				key = this.readString(this.readInt8());
				String value = this.readString(this.readInt8());
				attributes.put(key, value);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	protected String readString(int token) throws Exception
    {
        String ret = "";
 
        if (token == -1) {
            throw new Exception("BinTreeNodeReader->readString: Invalid token " + token);
        }

        if ((token > 2) && (token < 0xf5)) {
            ret = this.getToken(token);
        } else
        	
        	switch(token) {
            case 0: 
            	ret = "";
        		break;
        	case 0xfc: 
        	{
                int size = this.readInt8();
                ret  = this.fillArray(size);
        		break;
        	}
        	case 0xfd: 
        	{
                int size = this.readInt24();
                ret  = this.fillArray(size);
        		break;
        	}
        	case 0xfa: 
        	{
                String user = this.readString(this.readInt8());
                String server = this.readString(this.readInt8());
                if ((user.length() > 0) && (server.length() > 0)) {
                    ret = user + "@" + server;
                } else if (server.length() > 0) {
                    ret = server;
                }
            }        
        	case 0xff: 
                ret = this.readNibble();
        		break;        	
        }
        	
        return ret;
    }

protected String readNibble() throws Exception {
    int sbyte = this.readInt8();

    boolean ignoreLastNibble = (sbyte & 0x80) != 0;
    int size = (sbyte & 0x7f);
    int nrOfNibbles = size * 2 - (ignoreLastNibble ? 1 : 0);

    String data = this.fillArray(size);
    String retstring = "";

    for (int i = 0; i < nrOfNibbles; i++) {
        sbyte = data.charAt((int) Math.floor(i / 2));
        //TODO kkk int ord = ord(sbyte);

        int shift = 4 * (1 - i % 2);
        int decimal = (sbyte & (15 << shift)) >> shift;

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
            case 11:
            	retstring += (char)(decimal - 10 + 45);
                break;
            default:
                throw new Exception("Bad nibble: " + decimal);
        }
    }

    return retstring;
}

protected String fillArray(int len)
{
	String ret = "";
    if (this.input.length() >= len) {
        ret = this.input.substring(0, len);
        this.input = this.input.substring(len);
    }
    return ret;
}

protected String getToken(int token) throws Exception
    {
    	String ret = "";
        boolean subdict = false;
        // TODO kkk TokenMap::GetToken($token, $subdict, $ret);
        if (ret.isEmpty()) {
            token = this.readInt8();
         // TODO kkk TokenMap::GetToken($token, $subdict, $ret);
            if (ret.isEmpty()) {
                throw new Exception("BinTreeNodeReader->getToken: Invalid token " + token);
            }
        }
        return ret;
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
		if (this.input.length() >= (1 + offset)) {
			char sbstr = this.input.substring(offset, offset + 1).charAt(0);
			ret = (int) sbstr;
		}
		return ret;
	}

	protected int peekInt16() {
		return peekInt16(0);
	}

	protected int peekInt16(int offset) {
		int ret = 0;
		if (this.input.length() >= (2 + offset)) {
			char sbstr = this.input.substring(offset, offset + 1).charAt(0);
			ret = ((int) sbstr) << 8;
			sbstr = this.input.substring(offset + 1, offset + 2).charAt(0);
			ret |= ((int) sbstr) << 0;
		}
		return ret;
	}

	protected int peekInt24() {
		return peekInt24(0);
	}

	protected int peekInt24(int offset) {
		int ret = 0;
		if (this.input.length() >= (3 + offset)) {
			char sbstr = this.input.substring(offset, offset + 1).charAt(0);
			ret = ((int) sbstr) << 16;
			sbstr = this.input.substring(offset + 1, offset + 2).charAt(0);
			ret |= ((int) sbstr) << 8;
			sbstr = this.input.substring(offset + 2, offset + 3).charAt(0);
			ret |= ((int) sbstr) << 0;
		}
		return ret;
	}

	protected int readInt8() {
		int ret = this.peekInt8();
		if (this.input.length() >= 1)
			this.input = this.input.substring(1, 1);
		return ret;
	}

	protected int readInt16() {
		int ret = this.peekInt16();
		if (ret > 0) {
			this.input = this.input.substring(1, 2);
		}
		return ret;
	}

	protected int readInt24() {
		int ret = this.peekInt24();
		if (this.input.length() >= 3)
			this.input = this.input.substring(1, 3);
		return ret;
	}

}
