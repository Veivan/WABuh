package chatapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ProtocolNode {
	private String tag;
	private HashMap<String, String> attributeHash;
	private ArrayList<ProtocolNode> children;
	private byte[] data;

	public ProtocolNode(String tag, Map<String, String> attributeHash,
			List<ProtocolNode> children, byte[] data) {
		this.children = new ArrayList<ProtocolNode>();
		this.attributeHash = new HashMap<String, String>();

		this.tag = tag;
		this.attributeHash.putAll(attributeHash);
		this.children.addAll(children);
		this.data = data;
	}

	/**
	 * @param String
	 *            indent
	 * @param boolean isChild
	 * @return String
	 */
	public String nodeString() {
		return nodeString("", false);
	}

	public String nodeString(String indent) {
		return nodeString(indent, false);
	}

	public String nodeString(String indent, boolean isChild) {
		String ret = "\n" + indent + "<" + this.tag;
        if (this.attributeHash != null)
        {
        	for(Entry<String, String> entry : attributeHash.entrySet()) {
        	    String key = entry.getKey();
        	    String value = entry.getValue();
                ret += String.format(" %s=\"%s\"", key, value);
            }
        }
        ret += ">";
        if (this.data.length > 0)
        {
            if (this.data.length <= 1024)
            {
                ret += new String(this.data);
            }
            else
            {
                ret += String.format("--%d byte--", this.data.length);
            }
        }
        
        if (this.children != null && this.children.size() > 0)
        {
    		for(ProtocolNode item: this.children)
            {
                ret += item.nodeString(indent + "  ");
            }    		
    		ret += "\n" + indent;
        }
        ret += "</" + this.tag + ">";
        return ret;
	}

	/**
	 * @return byte[]
	 */
	public byte[] getData() {
		return this.data;
	}

	/**
	 * @return String
	 */
	public String getTag() {
		return this.tag;
	}

	/**
	 * @return the attributeHash
	 */
	public HashMap<String, String> getAttributes() {
		return attributeHash;
	}

    /**
     * @param String attribute
     * @return String
     */
    public String getAttribute(String attribute)
    {
    	String ret = "";
    	if (this.attributeHash.containsKey(attribute))
    		ret = this.attributeHash.get(attribute);
        return ret;
    }

    /**
	 * @return ArrayList<ProtocolNode>
	 */
	public ArrayList<ProtocolNode> getChildren() {
		return this.children;
	}
	
    /**
     * @param String tag
     * @return ProtocolNode
     */
    public ProtocolNode getChild(String tag)
    {
        if (this.children != null && this.children.size() > 0)
        {
    		for(ProtocolNode item: this.children)
            {
                if (ProtocolNode.TagEquals(item, tag))
                {
                    return item;
                }
                ProtocolNode ret = item.getChild(tag);
                if (ret != null)
                {
                    return ret;
                }
            }
        }
        return null;
    }
    
    /**
     * @param int $offset
     */
    public void refreshTimes()
    {
    	refreshTimes(0);
    }
    public void refreshTimes(int offset)
    {
        String id = this.attributeHash.get("id");
        if (id != null)
    	{  		
         	String[] parts = id.split("-");
         	parts[0] = Long.toString(System.currentTimeMillis() / 1000L + offset);
         	this.attributeHash.put(id, parts[0] + "=" + parts[1]);
    	}
    	if (this.attributeHash.containsKey("t"))
    		attributeHash.put( "t", Long.toString(System.currentTimeMillis() / 1000L) );
    }

    /**
     * Print human readable ProtocolNode object
     *
     * @return string
     */
    public void toReadableString()
    {
        String sep = System.lineSeparator();
        String readableNode = this.tag + sep +
        		this.attributeHash.toString() + sep +
        		Arrays.deepToString( this.children.toArray() ) + sep +
        		this.data+ sep;
        System.out.println(readableNode);
    }
    
    public static boolean TagEquals(ProtocolNode node, String _string)
    {
        return (((node != null) && (node.tag != null)) && node.tag.toLowerCase().equals(_string.toLowerCase()));
    }
}
