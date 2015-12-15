package chatapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProtocolNode {
	private String tag;
	private HashMap<String, String> attributeHash;
	private ArrayList<ProtocolNode> children;
	private String data;

	public ProtocolNode(String tag, Map<String, String> attributeHash,
			List<ProtocolNode> children, String data) {
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
	public String nodeString(String indent) {
		return nodeString(indent, false);
	}

	public String nodeString(String indent, boolean isChild) {

		String ret = "";
		/*
		 * TODO kkk //formatters $lt = "<"; $gt = ">"; $nl = "\n"; if ( !
		 * self::isCli()) { $lt = "&lt;"; $gt = "&gt;"; $nl = "<br />"; $indent
		 * = str_replace(" ", "&nbsp;", $indent); }
		 * 
		 * $ret = $indent . $lt . $this->tag; if ($this->attributeHash != null)
		 * { foreach ($this->attributeHash as $key => $value) { $ret .= " " .
		 * $key . "=\"" . $value . "\""; } } $ret .= $gt; if
		 * (strlen($this->data) > 0) { if (strlen($this->data) <= 1024) {
		 * //message $ret .= $this->data; } else { //raw data $ret .= " " .
		 * strlen($this->data) . " byte data"; } } if ($this->children) { $ret
		 * .= $nl; $foo = array(); foreach ($this->children as $child) { $foo[]
		 * = $child->nodeString($indent . "  ", true); } $ret .= implode($nl,
		 * $foo); $ret .= $nl . $indent; } $ret .= $lt . "/" . $this->tag . $gt;
		 * 
		 * if ( ! $isChild) { $ret .= $nl; if ( ! self::isCli()) { $ret .= $nl;
		 * } }
		 */
		return ret;
	}

	/**
	 * @return String
	 */
	public String getData() {
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
	public HashMap<String, String> getAttributeHash() {
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
    * @param ProtocolNode node
    */
    public void addChild(ProtocolNode node){
      this.children.add(node);
    }
    
    /**
    * @param ProtocolNode node
    */
    public void removeChild(String tag, String attrs)
    {
    	/*
		 * TODO kkk        if($this->children){
          if(is_int($tag)){
              if(isset($this->childen[$tag])){
                array_slice($this->children,$tag,1);
              }
          }
          else{
            foreach($this->childen as $i=>$child){
              $index = -1;
              if(strcmp($child->tag, $tag) == 0){
                $index = $i;
                foreach($attrs as $key=>$val){
                  if(strcmp($child->getAttribute($key),$val) != 0){
                    $index = -1; // attrs not equal
                    break;
                  }
                }
              }
              if($index != -1){
                array_slice($this->children,$index,1);
                return;
              }
            }
          } 
        }*/
    }

    /**
     * @param String needle
     * @return boolean
     */
    public boolean nodeIdContains(String needle)
    {
        return this.getAttribute("id").contains(needle);
    }


    //get children supports string tag or int index
    /**
     * @param String tag
     * @param array $attrs
     * @return ProtocolNode
     */
    public ProtocolNode getChild(String tag)
    {
    	return getChild(tag, null);
    }
        public ProtocolNode getChild(String tag, String attrs)
        {
    	ProtocolNode ret = null;
    	/* TODO kkk
        if ($this->children) {
            if (is_int($tag)) {
                if (isset($this->children[$tag])) {
                    return $this->children[$tag];
                } else {
                    return null;
                }
            }
            foreach ($this->children as $child) {
                if (strcmp($child->tag, $tag) == 0) {
                    $found = true;
                    foreach($attrs as $key=>$value){
                      if(strcmp($child->getAttribute($key),$value) != 0)
                      {
                        $found = false;
                        break;
                      }
                    }
                    if($found)
                      return $child;
                }
                $ret = $child->getChild($tag, $attrs);
                if ($ret) {
                    return $ret;
                }
            }
        } */

        return null;
    }

    /**
     * @param String tag
     * @return boolean
     */
    public boolean hasChild(String tag)
    {
        return (this.getChild(tag) != null);
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
         	parts[0] = Long.toString(System.currentTimeMillis() + offset);
         	this.attributeHash.put(id, parts[0] + "=" + parts[1]);
    	}
    	if (this.attributeHash.containsKey("t"))
    		attributeHash.put( "t", Long.toString(System.currentTimeMillis()) );
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
}
