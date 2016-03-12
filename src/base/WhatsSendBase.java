package base;

import helper.AccountInfo;
import helper.MessageWA;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import settings.Constants;
import chatapi.Funcs;
import chatapi.MessageStoreInterface;
import chatapi.ProtocolNode;
import chatapi.SqliteMessageStore;

public class WhatsSendBase extends WhatsAppBase {

	protected String messageId;
	protected int messageCounter = 0; // Message counter for auto-id.
	protected int iqCounter = 1;
	
	protected int retryCounter = 1;
    /*/ TODO kkk public $retryCounters = [];
    public $retryNodes = []; */
	
	protected boolean replaceKey;

	protected HashMap<String, String> nodeId; // = array();
	
	protected String serverReceivedId; // Confirm that the *server* has received
	// your command.

	protected List<String> v1Only; // = array();
    public List<String> retryNodes;


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
		String sid = Integer.toHexString(iqId);
		if (sid.length() % 2 == 1)
			sid += "0";
		return sid;
	}

	/**
	 * Send a request to get cipher keys from an user
	 *
	 * @param String
	 *            numbers Phone number of the user you want to get the cipher
	 *            keys.
	 */
	public void sendGetCipherKeysFromUser(ArrayList<String> numbers) {
		sendGetCipherKeysFromUser(numbers, false);
	}

	public void sendGetCipherKeysFromUser(ArrayList<String> numbers,
			boolean replaceKey) {
		this.replaceKey = replaceKey;
		String msgId = this.createIqId();
		this.nodeId.put("cipherKeys", msgId);

		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		Map<String, String> attributeHash = new HashMap<String, String>();

		for (String number : numbers) {
			attributeHash.put("jid", this.getJID(number));
			children.add(new ProtocolNode("user", attributeHash, null, null));
			attributeHash.clear();
		}

		ProtocolNode keyNode = new ProtocolNode("key", null, children, null);
		children.clear();
		children.add(keyNode);

		attributeHash.clear();
		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "encrypt");
		attributeHash.put("type", "get");
		attributeHash.put("to", Constants.WHATSAPP_SERVER);

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
		this.waitForServer(msgId);
	}
	
	public void sendSetPreKeys() {
		sendSetPreKeys(false);
	}

	public void sendSetPreKeys(boolean bnew) {
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		Map<String, String> attributeHash = new HashMap<String, String>();

		/*
		 * TODO kkk $axolotl = new KeyHelper();
		 * 
		 * $identityKeyPair = $axolotl->generateIdentityKeyPair(); $privateKey =
		 * $identityKeyPair->getPrivateKey()->serialize(); $publicKey =
		 * $identityKeyPair->getPublicKey()->serialize(); $keys =
		 * $axolotl->generatePreKeys(mt_rand(), 200);
		 * $this->axolotlStore->storePreKeys($keys);
		 * 
		 * for ($i = 0; $i < 200; $i++) { $prekeyId =
		 * adjustId($keys[$i]->getId()); $prekey =
		 * substr($keys[$i]->getKeyPair()->getPublicKey()->serialize(),1); $id =
		 * new ProtocolNode('id', null, null, $prekeyId); $value = new
		 * ProtocolNode('value', null, null, $prekey); $prekeys[] = new
		 * ProtocolNode('key', null, array($id, $value), null); // 200 PreKeys
		 * 
		 * } if (bnew) $registrationId =
		 * $this->axolotlStore->getLocalRegistrationId(); else $registrationId =
		 * $axolotl->generateRegistrationId(); $registration = new
		 * ProtocolNode('registration', null, null, adjustId($registrationId));
		 * $identity = new ProtocolNode('identity', null, null,
		 * substr($publicKey, 1)); $type = new ProtocolNode('type', null, null,
		 * chr(Curve::DJB_TYPE));
		 * 
		 * $this->axolotlStore->storeLocalData($registrationId,
		 * $identityKeyPair);
		 * 
		 * $list = new ProtocolNode('list', null, $prekeys, null);
		 * 
		 * $signedRecord = $axolotl->generateSignedPreKey($identityKeyPair,
		 * $axolotl->getRandomSequence(65536));
		 * $this->axolotlStore->storeSignedPreKey
		 * ($signedRecord->getId(),$signedRecord);
		 * 
		 * $sid = new ProtocolNode('id', null, null,
		 * adjustId($signedRecord->getId())); $value = new ProtocolNode('value',
		 * null, null,
		 * substr($signedRecord->getKeyPair()->getPublicKey()->serialize(), 1));
		 * $signature = new ProtocolNode('signature', null, null,
		 * $signedRecord->getSignature());

        $iqId = $this->nodeId['sendcipherKeys'] = $this->createIqId();
        $iqNode = new ProtocolNode('iq',
        [
          'id'    => $iqId,
          'to'    => Constants::WHATSAPP_SERVER,
          'type'  => 'set',
          'xmlns' => 'encrypt',
        ], [$identity, $registration, $type, $list, $secretKey], null);

		 */

		/* TODO kkk Temp nodes */
		ProtocolNode identity = new ProtocolNode("skey", null, null, null);
		ProtocolNode registration = new ProtocolNode("skey", null, null, null);
		ProtocolNode type = new ProtocolNode("skey", null, null, null);
		ProtocolNode list = new ProtocolNode("skey", null, null, null);

		ProtocolNode sid = new ProtocolNode("skey", null, null, null);
		ProtocolNode value = new ProtocolNode("skey", null, null, null);
		ProtocolNode signature = new ProtocolNode("skey", null, null, null);

		children.clear();
		children.add(sid);
		children.add(value);
		children.add(signature);

		ProtocolNode secretKey = new ProtocolNode("skey", null, children, null);

		children.clear();
		children.add(identity);
		children.add(registration);
		children.add(type);
		children.add(list);
		children.add(secretKey);

		String msgId = this.createIqId();

		attributeHash.clear();
		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "encrypt");
		attributeHash.put("type", "set");
		attributeHash.put("to", Constants.WHATSAPP_SERVER);

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
		this.waitForServer(msgId);
	}


	/**
	 * Wait for WhatsApp server to acknowledge *it* has received message.
	 * 
	 * @param String
	 *            id The id of the node sent that we are awaiting
	 *            acknowledgement of.
	 * @param int timeout sec
	 */
	public void waitForServer(String id) {
		waitForServer(id, 5);
	}

	public void waitForServer(String id, int timeout) {
		long time = System.currentTimeMillis() / 1000L;
		if (System.currentTimeMillis() / 1000L - this.timeout > 60) {

			this.serverReceivedId = "";
			do {
				try {
					this.pollMessage();
				} catch (Exception e) {
					logFile("error", "Failed to poll message" /*
															 * ,e.getMessage( )
															 */);
				}
			} while (this.serverReceivedId != id
					&& System.currentTimeMillis() / 1000L - time < timeout);
		}
	}

	public void logFile(String tag, String message /* , $context = array() */) {
		if (this.log) {
			this.logger.log(tag, message /* , $context */);
		}
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

		if (ProtocolNode.TagEquals(node, "challenge")) {
			this.processChallenge(node);
		} 
		else if (ProtocolNode.TagEquals(node, "success")) 
		{
			this.accountInfo = new AccountInfo(node.getAttribute("status"),
					node.getAttribute("kind"), node.getAttribute("creation"),
					node.getAttribute("expiration"));

			if (node.getAttribute("status") == "active") {
				this.loginStatus = Constants.CONNECTED_STATUS;
				byte[] challengeData = node.getData();

				FileWriter fw;
				try {
					fw = new FileWriter(this.challengeFilename);
					fw.write(new String(challengeData));
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// TODO kkk Keystream this.writer.setKey(this.outputKey);

				this.fireOnLoginSuccess(this.phoneNumber, node.getData());

			} else if (node.getAttribute("status") == "expired") {
				// TODO kkk events this.onAccountExpired(this.phoneNumber,
				// node.getData());
			}
		} 
		else if (ProtocolNode.TagEquals(node, "failure")) 
		{
			this.loginStatus = Constants.DISCONNECTED_STATUS;
			this.fireOnLoginFailed(node.getChildren().get(0).getTag());
		}

		else if (ProtocolNode.TagEquals(node, "ack")) 
		{
			if (node.getAttribute("class") == "message") //server receipt
				this.fireOnGetMessageReceivedServer(node.getAttribute("from"), node.getAttribute("id")); 
        }

		else if (ProtocolNode.TagEquals(node, "receipt")) 
		{ 
			String from = node.getAttribute("from"); 
			String id = node.getAttribute("id");
			String type = (node.getAttribute("type") != null ? node.getAttribute("type") : "delivery"); 
			switch (type) { 
			case "delivery": //delivered to target
				this.fireOnGetMessageReceivedClient(from, id); 
				break; 
			case "read":
				this.fireOnGetMessageReadedClient(from, id); //read by target 
				break; 
			case "played": //played by target //todo 
				break; 
			}
			ProtocolNode list = node.getChild("list"); 
			if (list != null) 
				for(ProtocolNode receipt : list.getChildren())
				{
					this.fireOnGetMessageReceivedClient(from, receipt.getAttribute("id")); 
				}
	        if (node.hasChild("retry")) 
	        {
	        	this.sendGetCipherKeysFromUser(Funcs.ExtractNumber(from), true);
	            this.messageStore.setPending(id, from);
	        }
	        /* TODO kkk
	        if (node.hasChild("error") && node.getChild("error").getAttribute("type") == "enc-v1") 
	        {
	                this->v1Only[ExtractNumber($node->getAttribute('from'))] = true;
	                this.messageStore.setPending(id, from);
	                $this->sendPendingMessages($node->getAttribute('from'));
	            }
	           */
				
	        //send ack 
	        this.sendAck(node, "receipt");
		 }
		
		 if (ProtocolNode.TagEquals(node, "message")) 
		 {
			//TODO kkk this.handleMessage(node, autoReceipt); 
		 }
		  		  
		 if (ProtocolNode.TagEquals(node, "iq")) 
		 { 
			 //TODO kkk this.handleIq(node); 
		 }
		  
		 if (node.getTag() == "notification") 
		 { 
			//TODO kkk this.handleNotification(node); 
		 }
			 
		 /* TODO kkk if (ProtocolNode.TagEquals(node, "stream:error")) { var textNode =
		 * node.GetChild("text"); if (textNode != null) { string content =
		 * WhatsApp.SYSEncoding.GetString(textNode.GetData());
		 * Helper.DebugAdapter.Instance.fireOnPrintDebug("Error : " + content);
		 * } this.Disconnect(); }
		 * 
		 * if (ProtocolNode.TagEquals(node, "presence")) { //presence node
		 * this.fireOnGetPresence(node.GetAttribute("from"),
		 * node.GetAttribute("type")); }
		 * 
		 * if (node.tag == "ib") { foreach (ProtocolNode child in node.children)
		 * { switch (child.tag) { case "dirty":
		 * this.SendClearDirty(child.GetAttribute("type")); break; case
		 * "offline": //this.SendQrSync(null); break; default: throw new
		 * NotImplementedException(node.NodeString()); } } }
		 * 
		 * if (node.tag == "chatstate") { string state =
		 * node.children.FirstOrDefault().tag; switch (state) { case
		 * "composing": this.fireOnGetTyping(node.GetAttribute("from")); break;
		 * case "paused": this.fireOnGetPaused(node.GetAttribute("from"));
		 * break; default: throw new NotImplementedException(node.NodeString());
		 * } }
		 */   
	}
	
	/**
	 * @param ProtocolNode
	 *            node
	 * @param String
	 *            class
	 */
	public void sendAck(ProtocolNode node, String sclass) {
		sendAck(node, sclass, false);
	}

	public void sendAck(ProtocolNode node, String sclass, boolean isGroup) {
		String from = node.getAttribute("from");
		String to = node.getAttribute("to");
		String id = node.getAttribute("id");

		String participant = "";
		String type = "";
		if (!isGroup) {
			type = node.getAttribute("type");
			participant = node.getAttribute("participant");
		}

		Map<String, String> attributes = new HashMap<String, String>();
		if (to != "")
			attributes.put("from", to);
		if (!participant.isEmpty())
			attributes.put("participant", participant);
		if (isGroup)
			attributes.put("count", Integer.toString(this.retryCounter));

		// TODO kkk             $attributes['count'] = $this->retryCounters[$id];

		attributes.put("to", from);
		attributes.put("class", sclass);
		attributes.put("id", id);
		//if (id != "")	attributes.put("t", node.getAttribute("t"));
		if (type != "")
			attributes.put("type", type);
		ProtocolNode ack = new ProtocolNode("ack", attributes, null, null);
		this.sendNode(ack);
	}

	/**
	 * Have we an active connection with WhatsAPP AND a valid login already?
	 */
	public boolean isLoggedIn() {
		// If you aren't connected you can't be logged in!
		// ($this->isConnected())
		// We are connected - but are we logged in? (the rest)
		return (this.whatsNetwork.isConnected() && !this.loginStatus.isEmpty() && this.loginStatus == Constants.CONNECTED_STATUS);
	}

	public byte[] getChallengeData() {
		return this.challengeData;
	}

    public void sendPendingMessages(String jid)
    {
        if (this.messageStore != null && this.isLoggedIn()) 
        {
        	ArrayList<MessageWA> messages = new ArrayList<MessageWA>();
            messages = this.messageStore.getPending(jid);
            
            for(MessageWA message : messages)
				try {
					this.sendMessage(message.getTo(), message.getMessage());
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        }
    }

	/**
	 * Send a text message to the user/group.
	 *
	 * @param String
	 *            to The recipient.
	 * @param String
	 *            txt The text message.
	 * @param boolean enc
	 *
	 * @return String Message ID.
	 * @throws UnsupportedEncodingException 
	 */
	public String sendMessage(String to, String plaintext) throws UnsupportedEncodingException {
		return sendMessage(to, plaintext, false);
	}

	public String sendMessage(String to, String plaintext, boolean force_plain) throws UnsupportedEncodingException {
		/* TODO kkk
        if (extension_loaded('curve25519') && extension_loaded('protobuf') && !$force_plain) {
            $to_num = ExtractNumber($to);
            if (!(strpos($to, '-') !== false)) {
                if (!$this->axolotlStore->containsSession($to_num, 1)) {
                    $this->sendGetCipherKeysFromUser($to_num);
                }

                $sessionCipher = $this->getSessionCipher($to_num);

                if (in_array($to_num, $this->v2Jids) && !isset($this->v1Only[$to_num])) {
                    $version = '2';
                    $alteredText = padMessage($plaintext);
                } else {
                    $version = '1';
                    $alteredText = $plaintext;
                }
                $cipherText = $sessionCipher->encrypt($alteredText);

                if ($cipherText instanceof WhisperMessage) {
                    $type = 'msg';
                } else {
                    $type = 'pkmsg';
                }
                $message = $cipherText->serialize();
                $msgNode = new ProtocolNode('enc',
              [
                'v'     => $version,
                'type'  => $type,
              ], null, $message);
            } else {
                
          $msgNode = new ProtocolNode('body', null, null, $plaintext);
            }
        } else {
            $msgNode = new ProtocolNode('body', null, null, $plaintext);
        }
        $plaintextNode = new ProtocolNode('body', null, null, $plaintext);
        $id = $this->sendMessageNode($to, $msgNode, null, $plaintextNode);

        if ($this->messageStore !== null) {
            $this->messageStore->saveMessage($this->phoneNumber, $to, $plaintext, $id, time());
        }

        return $id; 		 */

		ProtocolNode msgNode = new ProtocolNode("body", null, null, plaintext.getBytes(WhatsAppBase.SYSEncoding));
		String id = this.sendMessageNode(to, msgNode, null, null);
		if (this.messageStore != null)
			this.messageStore.saveMessage(this.phoneNumber, to, plaintext, id,
					Long.toString(System.currentTimeMillis() / 1000L));
		return id;
	}

	/**
	 * Send node to the servers.
	 *
	 * @param String
	 *            to - Single recipient
	 * @param ProtocolNode
	 *            node
	 * @param String
	 *            id
	 *
	 * @return String Message ID.
	 */
	protected String sendMessageNode(String to, ProtocolNode node) {
		return sendMessageNode(to, node, null, null);
	}

	protected String sendMessageNode(String to, ProtocolNode node, String id) {
		return sendMessageNode(to, node, id, null);
	}

	protected String sendMessageNode(String to, ProtocolNode node, String id, ProtocolNode plaintextNode) {
		String msgId = (id == null) ? this.createMsgId() : id;
		to = this.getJID(to);

		String type = (node.getTag() == "body" || node.getTag() == "enc") ? "text"
				: "media";

		Map<String, String> attributeHash = new HashMap<String, String>();
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();

		attributeHash.put("to", to);
		attributeHash.put("type", type);
		attributeHash.put("id", msgId);
		attributeHash.put("t", Long.toString(System.currentTimeMillis() / 1000L));
		attributeHash.put("notify", this.name);

		children.add(node);

		ProtocolNode messageNode = new ProtocolNode("message", attributeHash,
				children, null);
		this.sendNode(messageNode);

        if (node.getTag() == "enc") node = plaintextNode;

        /*
		 * TODO kkk event 
		 * $this->logFile('info', '{type} message with id {id} sent to
		 * {to}', array('type' => $type, 'id' => $msgId, 'to' =>
		 * ExtractNumber($to))); $this->eventManager()->fire("onSendMessage",
		 * array( $this->phoneNumber, $to, $msgId, $node ));
		 */

		//this.waitForServer(msgId);
		return msgId;
	}

	/**
	 * Create a unique msg id.
	 *
	 * @return string A message id string.
	 */
	protected String createMsgId() {
		return this.messageId + Integer.toHexString(this.messageCounter++);

	/* TODO kkk
	 * 
	 *         $msg = hex2bin($this->messageId);
        $chars = str_split($msg);
        $chars_val = array_map('ord', $chars);
        $pos = count($chars_val) - 1;
        while (true) {
            if ($chars_val[$pos] < 255) {
                $chars_val[$pos]++;
                break;
            } else {
                $chars_val[$pos] = 0;
                $pos--;
            }
        }
        $chars = array_map('chr', $chars_val);
        $msg = bin2hex(implode($chars));
        $this->messageId = $msg;

        return $this->messageId;

	 * 
	 */
	
	}


}
