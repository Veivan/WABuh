package chatapi;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import base.WhatsAppBase;

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
		this.attributeHash.clear();
		if (attributeHash != null)
			this.attributeHash.putAll(attributeHash);
		if (children != null)
			this.children.addAll(children);
		this.data = data;
	}

	/**
	 * @param String
	 *            indent
	 * @param boolean isChild
	 * @return String
	 */
	public String nodeString(String indent) {
		return nodeString(indent, false);
	}

	public String nodeString(String indent, boolean isChild) {
		String ret = "\n" + indent + "<" + this.tag;
		if (this.attributeHash != null) {
			for (Entry<String, String> entry : attributeHash.entrySet()) {
				ret += String.format(" %s=\"%s\"", entry.getKey(),
						entry.getValue());
			}
		}
		ret += ">";
		if (this.data != null) {
			if (this.data.length > 0) {
				if (this.data.length <= 1024) {
					//ret += new String(this.data, WhatsAppBase.SYSEncoding);
					ret += Funcs.GetHexArray(this.data);
				} else
					ret += String.format("--%d byte--", this.data.length);
			}
		}

		if (this.children != null && !this.children.isEmpty()) {
			for (ProtocolNode item : this.children)
				ret += item.nodeString(indent + "  ");
			ret += "\n" + indent;
		}

		ret += "</" + this.tag + ">";
		return ret;
	}

	/**
	 * @return String
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
	 * @param String
	 *            attribute
	 * @return String
	 */
	public String getAttribute(String attribute) {
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
	 * @param ProtocolNode
	 *            node
	 */
	public void addChild(ProtocolNode node) {
		this.children.add(node);
	}

	/**
	 * @param String
	 *            needle
	 * @return boolean
	 */
	public boolean nodeIdContains(String needle) {
		return this.getAttribute("id").contains(needle);
	}

	// get children supports string tag or int index
	/**
	 * @param String
	 *            tag
	 * @return ProtocolNode
	 */
	public ProtocolNode getChild(String tag) {
		if (this.children != null && !this.children.isEmpty()) {
			for (ProtocolNode item : this.children) {
				if (ProtocolNode.TagEquals(item, tag))
					return item;
				ProtocolNode ret = item.getChild(tag);
				if (ret != null)
					return ret;
			}
		}
		return null;
	}

	/**
	 * @param String
	 *            tag
	 * @return boolean
	 */
	public boolean hasChild(String tag) {
		return (this.getChild(tag) != null);
	}

	/**
	 * @param int $offset
	 */
	public void refreshTimes() {
		refreshTimes(0);
	}

	public void refreshTimes(int offset) {
		String id = this.attributeHash.get("id");
		if (id != null) {
			String[] parts = id.split("-");
			parts[0] = Long.toString(System.currentTimeMillis() / 1000L
					+ offset);
			this.attributeHash.put(id, parts[0] + "=" + parts[1]);
		}
		if (this.attributeHash.containsKey("t"))
			attributeHash.put("t",
					Long.toString(System.currentTimeMillis() / 1000L));
	}

	/**
	 * Print human readable ProtocolNode object
	 *
	 * @return string
	 */
	public void toReadableString() {
		String sep = System.lineSeparator();
		String readableNode = this.tag + sep + this.attributeHash.toString()
				+ sep + Arrays.deepToString(this.children.toArray()) + sep
				+ this.data + sep;
		System.out.println(readableNode);
	}

	public static boolean TagEquals(ProtocolNode node, String _string) {
		return (((node != null) && (node.tag != null)) && node.tag
				.toLowerCase().equals(_string.toLowerCase()));
	}
}
