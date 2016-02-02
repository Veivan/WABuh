package chatapi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import base.WhatsAppBase;
import settings.Constants;
import helper.KeyStream;

public class Login {

	private String phoneNumber;
	private String password;
	private KeyStream inputKey;
	private KeyStream outputKey;
	private WhatsProt parent;

	public Login(WhatsProt parent, String password) {
		this.parent = parent;
		this.password = password;
		this.phoneNumber = this.parent.getMyNumber();
	}

	/**
	 * Send the nodes to the WhatsApp server to log in.
	 * 
	 * @throws Exception
	 */
	public boolean doLogin() throws Exception {
		if (this.parent.isLoggedIn()) {
			return true;
		}

		this.parent.writer.resetKey();
		this.parent.reader.resetKey();
		String resource = Constants.WHATSAPP_DEVICE + '-'
				+ Constants.WHATSAPP_VER + '-' + Constants.PORT;
		byte[] data = this.parent.writer.StartStream(Constants.WHATSAPP_SERVER,
				resource);

		ProtocolNode feat = this.createFeaturesNode();
		ProtocolNode auth = this.createAuthNode();

		this.parent.sendData(data);
		this.parent.sendNode(feat, false); // kkk - was true
		this.parent.sendNode(auth, false); // kkk - was true

		this.parent.pollMessage();// stream start
		this.parent.pollMessage();// features
		this.parent.pollMessage();// challenge or success

		if (this.parent.getChallengeData() != null) {
			ProtocolNode AuthResponseNode = this.createAuthResponseNode();
			this.parent.sendNode(AuthResponseNode);
			this.parent.reader.setKey(this.inputKey);
			this.parent.writer.setKey(this.outputKey);
			while (!this.parent.pollMessage()) {
			}
			;
		}

		if (this.parent.getLoginStatus() == settings.Constants.DISCONNECTED_STATUS) {
			throw new Exception("Login failure!");
		}

		/*
		 * TODO kkk $this->parent->logFile('info', '{number} successfully logged
		 * in', array('number' => $this->phoneNumber));
		 */

		this.parent.sendAvailableForChat();

		/*
		 * TODO kkk encription
		 * $this->parent->setMessageId(substr(base64_encode(mcrypt_create_iv(64,
		 * MCRYPT_DEV_URANDOM)), 0, 12));
		 * 
		 * if (extension_loaded('curve25519') || extension_loaded('protobuf')) {
		 * if (file_exists(__DIR__ . DIRECTORY_SEPARATOR .
		 * Constants::DATA_FOLDER . DIRECTORY_SEPARATOR . "axolotl-" .
		 * $this->phoneNumber . ".db")) { if
		 * (empty($this->parent->getAxolotlStore()->loadPreKeys())) {
		 * $this->parent->sendSetPreKeys(); $this->parent->logFile('info',
		 * 'Sending prekeys to WA server'); } } }
		 */
		return true;
	}

	/**
	 * Add stream features.
	 *
	 * @return ProtocolNode Return itself.
	 */
	private ProtocolNode createFeaturesNode() {
		ProtocolNode readreceipts = new ProtocolNode("readreceipts", null,
				null, null);
		ProtocolNode groupsv2 = new ProtocolNode("groups_v2", null, null, null);
		ProtocolNode privacy = new ProtocolNode("privacy", null, null, null);
		ProtocolNode presencev2 = new ProtocolNode("presence", null, null, null);
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		children.add(readreceipts);
		children.add(groupsv2);
		children.add(privacy);
		children.add(presencev2);
		ProtocolNode parent = new ProtocolNode("stream:features", null,
				children, null);
		return parent;
	}

	/**
	 * Add the authentication nodes.
	 *
	 * @return ProtocolNode Returns an authentication node.
	 * @throws IOException 
	 */
	private ProtocolNode createAuthNode() throws IOException {
		byte[] data = this.createAuthBlob();
		Map<String, String> attributeHash = new HashMap<String, String>();
		attributeHash.put("mechanism", helper.KeyStream.AuthMethod);
		attributeHash.put("user", this.phoneNumber);
		if (this.parent.hidden) {
			attributeHash.put("passive", "true");
		}
		ProtocolNode node = new ProtocolNode("auth", attributeHash, null, data);
		return node;
	}

	private byte[] createAuthBlob() throws IOException {
		byte[] data = null;
		if (this.parent.getChallengeData() != null) {
			String encpass = new String(this.parent.encryptPassword());
			char[] buffer = encpass.toCharArray();

			byte[][] keys = KeyStream.GenerateKeys(buffer,
					this.parent.getChallengeData());

			this.parent.reader.setKey(new KeyStream(keys[2], keys[3]));
			this.outputKey = new KeyStream(keys[0], keys[1]);

			ByteArrayOutputStream b = new ByteArrayOutputStream();
			b.write(new byte[] { 0, 0, 0, 0 });
			b.write(this.phoneNumber.getBytes(WhatsAppBase.SYSEncoding));
			b.write(this.parent.getChallengeData());		
			b.write(Long.toString(System.currentTimeMillis() / 1000L).getBytes(WhatsAppBase.SYSEncoding));

			data = b.toByteArray();

            this.parent.setChallengeData(null);
            this.outputKey.EncodeMessage(data, 0, 4, data.length - 4);
			this.parent.writer.setKey(this.outputKey);
					}
		return data;
	}

	/**
	 * Add the auth response to protocoltreenode.
	 *
	 * @return ProtocolNode Returns a response node.
	 * @throws Exception
	 */
	protected ProtocolNode createAuthResponseNode() throws Exception {
		return new ProtocolNode("response", null, null, this.authenticate());
	}

	/**
	 * Authenticate with the WhatsApp Server.
	 *
	 * @return string Returns binary string
	 * @throws Exception
	 */
	protected byte[] authenticate() throws Exception {
		byte[] data = null;
		if (this.parent.getChallengeData() != null) {
			String encpass = new String(this.parent.encryptPassword());
			char[] buffer = encpass.toCharArray();

			byte[][] keys = KeyStream.GenerateKeys(buffer,
					this.parent.getChallengeData());

			this.parent.reader.setKey(new KeyStream(keys[2], keys[3]));
			this.outputKey = new KeyStream(keys[0], keys[1]);

			ByteArrayOutputStream b = new ByteArrayOutputStream();
			b.write(new byte[] { 0, 0, 0, 0 });
			b.write(this.phoneNumber.getBytes(WhatsAppBase.SYSEncoding));
			b.write(this.parent.getChallengeData());

			data = b.toByteArray();
			this.outputKey.EncodeMessage(data, 0, 4, data.length - 4);
			this.parent.writer.setKey(this.outputKey);
			return data;
		}
		throw new Exception("Auth response error");
	}
}
