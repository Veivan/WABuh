package chatapi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import static java.lang.Math.toIntExact;
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
		// TODO kkk - was: - if (this.parent.isLoggedIn()) return true;

		if (!this.parent.isLoggedIn())
			throw new Exception("Not connected!");

		this.parent.writer.resetKey();
		this.parent.reader.resetKey();
		String resource = Constants.PLATFORM + '-' + Constants.WHATSAPP_VER;
		byte[] data = this.parent.writer.StartStream(Constants.WHATSAPP_SERVER,
				resource);

		/**/
		ProtocolNode feat = this.createFeaturesNode();
		ProtocolNode auth = this.createAuthNode();

		this.parent.sendData(data);
		this.parent.sendNode(feat, false); // TODO kkk - was true
		this.parent.sendNode(auth, false); // TODO kkk - was true

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
		this.parent.sendGetPrivacyBlockedList();
		this.parent.sendGetClientConfig();
		// TODO kkk
		// $this->parent->setMessageId(substr(bin2hex(mcrypt_create_iv(64,
		// MCRYPT_DEV_URANDOM)), 0, 22)); // 11 char hex

		/*
		 * TODO kkk encription
		 * $this->parent->setMessageId(substr(bin2hex(mcrypt_create_iv(64,
		 * MCRYPT_DEV_URANDOM)), 0, 22)); // 11 char hex
		 * 
		 * if (extension_loaded('curve25519') || extension_loaded('protobuf')) {
		 * if (file_exists($this->parent->dataFolder.
		 * 'axolotl-'.$this->phoneNumber.'.db')) { $pre_keys =
		 * $this->parent->getAxolotlStore()->loadPreKeys(); if
		 * (empty($pre_keys)) {
		 * 
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
		/*
		 * kkk - Commented in PHP ProtocolNode readreceipts = new
		 * ProtocolNode("readreceipts", null, null, null); ProtocolNode groupsv2
		 * = new ProtocolNode("groups_v2", null, null, null); ProtocolNode
		 * privacy = new ProtocolNode("privacy", null, null, null); ProtocolNode
		 * presencev2 = new ProtocolNode("presence", null, null, null);
		 * List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		 * children.add(readreceipts); children.add(groupsv2);
		 * children.add(privacy); children.add(presencev2); ProtocolNode parent
		 * = new ProtocolNode("stream:features", null, children, null);
		 */
		ProtocolNode parent = new ProtocolNode("stream:features", null, null,
				null);
		return parent;
	}

	/**
	 * Add the authentication nodes.
	 *
	 * @return ProtocolNode Returns an authentication node.
	 * @throws IOException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	private ProtocolNode createAuthNode() throws IOException,
			NoSuchAlgorithmException, InvalidKeySpecException {
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
			byte[][] keys = KeyStream.GenerateKeys(this.parent.encryptPassword(),
					this.parent.getChallengeData());

			this.inputKey = new KeyStream(keys[2], keys[3]);
			this.outputKey = new KeyStream(keys[0], keys[1]);

			this.parent.reader.setKey(this.inputKey);
			// TODO kkk need rebuild
			ByteArrayOutputStream b = new ByteArrayOutputStream();
			b.write(new byte[] { 0, 0, 0, 0 });
			b.write(this.phoneNumber.getBytes(WhatsAppBase.SYSEncoding));
			b.write(this.parent.getChallengeData());
			b.write(Long.toString(System.currentTimeMillis() / 1000L).getBytes(
					WhatsAppBase.SYSEncoding));

			data = b.toByteArray();

			this.parent.setChallengeData(null);
			data = this.outputKey.EncodeMessage(data, 0, 4, data.length - 4);
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
		byte[] challengeData = this.parent.getChallengeData();
		if (challengeData != null) {

			byte[][] keys = KeyStream.GenerateKeys(this.parent.encryptPassword(), challengeData);

			System.out.println(Funcs.GetHexArray(challengeData));

			System.out.println(Funcs.GetHexArray(keys[0]));
			System.out.println(Funcs.GetHexArray(keys[1]));
			System.out.println(Funcs.GetHexArray(keys[2]));
			System.out.println(Funcs.GetHexArray(keys[3]));

			this.inputKey = new KeyStream(keys[2], keys[3]);
			this.outputKey = new KeyStream(keys[0], keys[1]);

			this.parent.reader.setKey(this.inputKey);

			byte[] empbytes = DatatypeConverter.parseHexBinary("00");
			
			ByteArrayOutputStream babuffer = new ByteArrayOutputStream();
			babuffer.write("\0\0\0\0".getBytes(WhatsAppBase.SYSEncoding));
			babuffer.write("79250069542".getBytes(WhatsAppBase.SYSEncoding));
			babuffer.write(challengeData);
			String timestamp = String.valueOf(System.currentTimeMillis() / 1000L);
			babuffer.write(timestamp.getBytes(WhatsAppBase.SYSEncoding));
			babuffer.write("000".getBytes(WhatsAppBase.SYSEncoding));
			babuffer.write(empbytes);
			babuffer.write("000".getBytes(WhatsAppBase.SYSEncoding));
			babuffer.write(empbytes);
			babuffer.write(Constants.OS_VERSION.getBytes(WhatsAppBase.SYSEncoding));
			babuffer.write(empbytes);
			babuffer.write(Constants.MANUFACTURER.getBytes(WhatsAppBase.SYSEncoding));
			babuffer.write(empbytes);
			babuffer.write(Constants.DEVICE.getBytes(WhatsAppBase.SYSEncoding));
			babuffer.write(empbytes);
			babuffer.write(Constants.BUILD_VERSION.getBytes(WhatsAppBase.SYSEncoding));
			
			byte[] data = babuffer.toByteArray();

			System.out.println("timestamp = " + timestamp);
			System.out.println(Funcs.GetHexArray(data));

			data = this.outputKey.EncodeMessage(data, 0, 4, data.length - 4);

			System.out.println(Funcs.GetHexArray(data));

			this.parent.writer.setKey(this.outputKey);
			this.parent.setOutputKey(this.outputKey);
			return data;
		}
		throw new Exception("Auth response error");
	}
}
