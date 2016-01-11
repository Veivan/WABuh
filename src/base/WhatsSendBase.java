package base;

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
	 * Fetch a single message node
	 *
	 * @throws Exception
	 */
	public boolean pollMessage() throws Exception {
		if (!this.whatsNetwork.isConnected())
			throw new Exception("Connection Closed!");

		// TODO kkk byte[] nodeData;
		String nodeData;
		try {
			nodeData = this.whatsNetwork.readStanza();
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
	 * @param $data
	 *
	 * @throws Exception
	 */
	protected void processInboundData(String data) {
		ProtocolNode node = null;
		try {
			node = this.reader.nextTree(data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

		/*
		 * TODO kkk $this->debugPrint($node->nodeString("rx  ") . "\n");
		 * $this->serverReceivedId = $node->getAttribute('id');
		 * 
		 * if ($node->getTag() == "challenge") { $this->processChallenge($node);
		 * } elseif ($node->getTag() == "failure") { $this->loginStatus =
		 * Constants::DISCONNECTED_STATUS;
		 * $this->eventManager()->fire("onLoginFailed", array(
		 * $this->phoneNumber, $node->getChild(0)->getTag() )); if
		 * ($node->getChild(0)->getTag() == 'not-authorized')
		 * $this->logFile('error', 'Blocked number or wrong password. Use
		 * blockChecker.php'); } elseif ($node->getTag() == "success") { if
		 * ($node->getAttribute("status") == "active") { $this->loginStatus =
		 * Constants::CONNECTED_STATUS; $challengeData = $node->getData();
		 * file_put_contents($this->challengeFilename, $challengeData);
		 * $this->writer->setKey($this->outputKey);
		 * 
		 * $this->eventManager()->fire("onLoginSuccess", array(
		 * $this->phoneNumber, $node->getAttribute("kind"),
		 * $node->getAttribute("status"), $node->getAttribute("creation"),
		 * $node->getAttribute("expiration") )); } elseif
		 * ($node->getAttribute("status") == "expired") {
		 * $this->eventManager()->fire("onAccountExpired", array(
		 * $this->phoneNumber, $node->getAttribute("kind"),
		 * $node->getAttribute("status"), $node->getAttribute("creation"),
		 * $node->getAttribute("expiration") )); } } elseif ($node->getTag() ==
		 * 'ack') { if ($node->getAttribute("class") == "message") {
		 * $this->eventManager()->fire("onMessageReceivedServer", array(
		 * $this->phoneNumber, $node->getAttribute('from'),
		 * $node->getAttribute('id'), $node->getAttribute('class'),
		 * $node->getAttribute('t') )); } } elseif ($node->getTag() ==
		 * 'receipt') { if ($node->hasChild("list")) { foreach
		 * ($node->getChild("list")->getChildren() as $child) {
		 * $this->eventManager()->fire("onMessageReceivedClient", array(
		 * $this->phoneNumber, $node->getAttribute('from'),
		 * $child->getAttribute('id'), $node->getAttribute('type'),
		 * $node->getAttribute('t'), $node->getAttribute('participant') )); } }
		 * if ($node->hasChild("retry")) {
		 * $this->sendGetCipherKeysFromUser(ExtractNumber
		 * ($node->getAttribute('from')), true); }
		 * 
		 * $this->eventManager()->fire("onMessageReceivedClient", array(
		 * $this->phoneNumber, $node->getAttribute('from'),
		 * $node->getAttribute('id'), $node->getAttribute('type'),
		 * $node->getAttribute('t'), $node->getAttribute('participant') ));
		 * 
		 * $this->sendAck($node, 'receipt'); } if ($node->getTag() == "message")
		 * { $handler = new MessageHandler($this, $node); } if ($node->getTag()
		 * == "presence" && $node->getAttribute("status") == "dirty") { //clear
		 * dirty $categories = array(); if (count($node->getChildren()) > 0) {
		 * foreach ($node->getChildren() as $child) { if ($child->getTag() ==
		 * "category") { $categories[] = $child->getAttribute("name"); } } }
		 * $this->sendClearDirty($categories); } if (strcmp($node->getTag(),
		 * "presence") == 0 && strncmp($node->getAttribute('from'),
		 * $this->phoneNumber, strlen($this->phoneNumber)) != 0 &&
		 * strpos($node->getAttribute('from'), "-") === false) { $presence =
		 * array(); if ($node->getAttribute('type') == null) {
		 * $this->eventManager()->fire("onPresenceAvailable", array(
		 * $this->phoneNumber, $node->getAttribute('from'), )); } else {
		 * $this->eventManager()->fire("onPresenceUnavailable", array(
		 * $this->phoneNumber, $node->getAttribute('from'),
		 * $node->getAttribute('last') )); } } if ($node->getTag() == "presence"
		 * && strncmp($node->getAttribute('from'), $this->phoneNumber,
		 * strlen($this->phoneNumber)) != 0 &&
		 * strpos($node->getAttribute('from'), "-") !== false &&
		 * $node->getAttribute('type') != null) { $groupId =
		 * $this->parseJID($node->getAttribute('from')); if
		 * ($node->getAttribute('add') != null) {
		 * $this->eventManager()->fire("onGroupsParticipantsAdd", array(
		 * $this->phoneNumber, $groupId,
		 * $this->parseJID($node->getAttribute('add')) )); } elseif
		 * ($node->getAttribute('remove') != null) {
		 * $this->eventManager()->fire("onGroupsParticipantsRemove", array(
		 * $this->phoneNumber, $groupId,
		 * $this->parseJID($node->getAttribute('remove')) )); } } if
		 * (strcmp($node->getTag(), "chatstate") == 0 &&
		 * strncmp($node->getAttribute('from'), $this->phoneNumber,
		 * strlen($this->phoneNumber)) != 0 &&
		 * strpos($node->getAttribute('from'), "-") === false) {
		 * if($node->getChild(0)->getTag() == "composing"){
		 * $this->eventManager()->fire("onMessageComposing", array(
		 * $this->phoneNumber, $node->getAttribute('from'),
		 * $node->getAttribute('id'), "composing", $node->getAttribute('t') ));
		 * } else { $this->eventManager()->fire("onMessagePaused", array(
		 * $this->phoneNumber, $node->getAttribute('from'),
		 * $node->getAttribute('id'), "paused", $node->getAttribute('t') )); } }
		 * if ($node->getTag() == "receipt") {
		 * $this->eventManager()->fire("onGetReceipt", array(
		 * $node->getAttribute('from'), $node->getAttribute('id'),
		 * $node->getAttribute('offline'), $node->getAttribute('retry') )); } if
		 * ($node->getTag() == "iq") { $handler = new IqHandler($this, $node); }
		 * 
		 * if ($node->getTag() == "notification") { $handler = new
		 * NotificationHandler($this, $node); } if ($node->getTag() == "call") {
		 * if ($node->getChild(0)->getTag() == "offer") { $callId =
		 * $node->getChild(0)->getAttribute("call-id");
		 * $this->sendReceipt($node, null, null, $callId);
		 * 
		 * $this->eventManager()->fire("onCallReceived", array(
		 * $this->phoneNumber, $node->getAttribute("from"),
		 * $node->getAttribute("id"), $node->getAttribute("notify"),
		 * $node->getAttribute("t"), $node->getChild(0)->getAttribute("call-id")
		 * )); } else { $this->sendAck($node, 'call'); }
		 * 
		 * } if ($node->getTag() == "ib") { foreach($node->getChildren() as
		 * $child) { switch($child->getTag()) { case "dirty":
		 * $this->sendClearDirty(array($child->getAttribute("type"))); break;
		 * case "account": $this->eventManager()->fire("onPaymentRecieved",
		 * array( $this->phoneNumber, $child->getAttribute("kind"),
		 * $child->getAttribute("status"), $child->getAttribute("creation"),
		 * $child->getAttribute("expiration") )); break; case "offline":
		 * 
		 * break; default: throw new Exception("ib handler for " .
		 * $child->getTag() . " not implemented"); } } }
		 * 
		 * // Disconnect socket on stream error. if ($node->getTag() ==
		 * "stream:error") {
		 * 
		 * $this->eventManager()->fire("onStreamError", array(
		 * $node->getChild(0)->getTag() ));
		 * 
		 * $this->logFile('error', 'Stream error {error}', array('error' =>
		 * $node->getChild(0)->getTag())); $this->disconnect(); }
		 * if(isset($handler)) { $handler->Process(); unset($handler); }
		 */
	}

}
