package base;

import helper.AccountInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import settings.Constants;
import chatapi.ProtocolNode;

public class WhatsSendBase extends WhatsAppBase {

	protected int messageCounter = 0; // Message counter for auto-id.
	protected int iqCounter = 1;

	/**
	 * Process the challenge.
	 *
	 * @param ProtocolNode
	 *            node The node that contains the challenge.
	 */
	protected void processChallenge(ProtocolNode node) {
		this.setChallengeData(node.getData());
	}

	/**
	 * Fetch a single message node
	 *
	 * @throws Exception
	 */
	public boolean pollMessage() throws Exception {
		if (!this.whatsNetwork.isConnected())
			throw new Exception("Connection Closed!");

		try {
			byte[] nodeData = this.whatsNetwork.readStanza();
			if (nodeData != null) {
				this.processInboundData(nodeData);
				return true;
			}
		} catch (Exception e) {
			this.Disconnect();
		}

		/*
		 * TODO kkk //no data received else
		 * $this->eventManager()->fire("onClose", array( $this->phoneNumber,
		 * 'Socket EOF' )
		 */
		if (System.currentTimeMillis() - this.timeout * 1000 > 60) {
			this.sendPing();
		}
		return false;
	}

	/**
	 * Send a ping to the server.
	 */
	public void sendPing() {
		String msgId = this.createIqId();

		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		Map<String, String> attributeHash = new HashMap<String, String>();

		ProtocolNode pingNode = new ProtocolNode("ping", null, null, null);
		children.add(pingNode);

		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "w:p");
		attributeHash.put("type", "get");
		attributeHash.put("to", Constants.WHATSAPP_SERVER);

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
	}

	/**
	 * iq id
	 *
	 * @return string Iq id
	 */
	public String createIqId() {
		int iqId = this.iqCounter;
		this.iqCounter++;

		return Integer.toHexString(iqId);
	}

	/**
	 * Process inbound data.
	 *
	 * @param byte[] data
	 *
	 * @throws Exception
	 */
	protected void processInboundData(byte[] data) throws Exception {
		ProtocolNode node = null;
		node = this.reader.nextTree(data);
		if (node != null)
			this.processInboundDataNode(node);
	}

	/**
	 * Will process the data from the server after it's been decrypted and
	 * parsed.
	 *
	 * This also provides a convenient method to use to unit test the event
	 * framework.
	 * 
	 * @param ProtocolNode
	 *            node
	 * @param type
	 *
	 * @throws Exception
	 */
	protected void processInboundDataNode(ProtocolNode node) {
		this.timeout = System.currentTimeMillis();

        if (ProtocolNode.TagEquals(node, "challenge"))
        {
            this.processChallenge(node);
        }
        else if (ProtocolNode.TagEquals(node, "success"))
        {
            this.loginStatus = Constants.CONNECTED_STATUS;
            this.accountInfo = new AccountInfo(node.getAttribute("status"),
                                                node.getAttribute("kind"),
                                                node.getAttribute("creation"),
                                                node.getAttribute("expiration")); 
            this.fireOnLoginSuccess(this.phoneNumber, node.getData());
        }
        else if (ProtocolNode.TagEquals(node, "failure"))
        {
            this.loginStatus = Constants.UNAUTHORIZED_STATUS;
            this.fireOnLoginFailed(node.getChildren().get(0).getTag());
        }

/* TODO event kkk        
 		if (ProtocolNode.TagEquals(node, "receipt"))
        {
            String from = node.GetAttribute("from");
            String id = node.GetAttribute("id");
        
            String type = (node.GetAttribute("type") != null ? node.GetAttribute("type") : "delivery");
            switch (type)
            {
                case "delivery":
                    //delivered to target
                    this.fireOnGetMessageReceivedClient(from, id);
                    break;
                case "read":
                    this.fireOnGetMessageReadedClient(from, id);
                    //read by target
                    //todo
                    break;
                case "played":
                    //played by target
                    //todo
                    break;
            } 

            ProtocolNode list = node.GetChild("list");
            if (list != null)
                foreach (ProtocolNode receipt in list.GetAllChildren())
                {
                    this.fireOnGetMessageReceivedClient(from, receipt.GetAttribute("id"));
                }

            //send ack
            SendNotificationAck(node, type);
        }

        if (ProtocolNode.TagEquals(node, "message"))
        {
            this.handleMessage(node, autoReceipt);
        }


        if (ProtocolNode.TagEquals(node, "iq"))
        {
            this.handleIq(node);
        }

        if (ProtocolNode.TagEquals(node, "stream:error"))
        {
            var textNode = node.GetChild("text");
            if (textNode != null)
            {
                string content = WhatsApp.SYSEncoding.GetString(textNode.GetData());
                Helper.DebugAdapter.Instance.fireOnPrintDebug("Error : " + content);
            }
            this.Disconnect();
        }

        if (ProtocolNode.TagEquals(node, "presence"))
        {
            //presence node
            this.fireOnGetPresence(node.GetAttribute("from"), node.GetAttribute("type"));
        }

        if (node.tag == "ib")
        {
            foreach (ProtocolNode child in node.children)
            {
                switch (child.tag)
                {
                    case "dirty":
                        this.SendClearDirty(child.GetAttribute("type"));
                        break;
                    case "offline":
                        //this.SendQrSync(null);
                        break;
                    default:
                        throw new NotImplementedException(node.NodeString());
                }
            }
        }

        if (node.tag == "chatstate")
        {
            string state = node.children.FirstOrDefault().tag;
            switch (state)
            {
                case "composing":
                    this.fireOnGetTyping(node.GetAttribute("from"));
                    break;
                case "paused":
                    this.fireOnGetPaused(node.GetAttribute("from"));
                    break;
                default:
                    throw new NotImplementedException(node.NodeString());
            }
        }

        if (node.tag == "ack")
        {
            string cls = node.GetAttribute("class");
            if (cls == "message")
            {
                //server receipt
                this.fireOnGetMessageReceivedServer(node.GetAttribute("from"), node.GetAttribute("id"));
            }
        }

        if (node.tag == "notification")
        {
            this.handleNotification(node);
        }
        */
        
	}

}
