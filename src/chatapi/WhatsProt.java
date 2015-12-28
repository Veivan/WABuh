package chatapi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chatapi.Constants;
import chatapi.Funcs;
import chatapi.Logger;

public class WhatsProt {

	private String accountInfo; // The AccountInfo object.
	private String challengeFilename; // Path to nextChallenge.dat.
	private String challengeData; //
	protected boolean debug; // Determines whether debug mode is on or off.
	protected String eventManager; // An instance of the WhatsApiEvent Manager.
	protected List<String> groupList; // array(); // An array with all the
										// groups a user belongs in.
	protected String outputKey; // Instances of the KeyStream class.
	protected int groupId = -1; // Id of the group created.
	protected String lastId = "-1"; // Id to the last message sent.
	protected String loginStatus; // Holds the login status.
	protected List<String> mediaFileInfo; // = array(); // Media File
											// Information

	protected HashMap<String, Object> mediaQueue; // Queue for media message
													// nodes

	protected int messageCounter = 0; // Message counter for auto-id.
	protected int iqCounter = 1;
	protected List<ProtocolNode> messageQueue; // = array(); // Queue for
												// received
												// messages.
	protected String name; // The user name.
	protected Object newMsgBind = false; //

	protected List<ProtocolNode> outQueue; // = array(); // Queue for outgoing
											// messages.
	protected String password; // The user password.
	protected String phoneNumber; // The user phone number including the country
									// code without '+' or '00'.
	protected String serverReceivedId; // Confirm that the *server* has received
										// your command.
	protected Socket socket; // A socket to connect to the WhatsApp network.
	protected MessageStoreInterface messageStore = null;
	protected HashMap<String, String> nodeId; // = array();

	protected String messageId;
	protected boolean voice;
	protected long timeout = 0;
	protected HashMap<String, String> sessionCiphers; // = array();

	protected List<String> v2Jids; // = array();

	protected HashMap<String, String> groupCiphers; // = array();

	protected List<String> pending_nodes; // = array();

	protected boolean replaceKey;
	protected int retryCounter = 1;
	protected boolean readReceipts = true;

	public AxolotlInterface axolotlStore;

	public BinTreeNodeWriter writer; // An instance of the BinTreeNodeWriter class.
	public BinTreeNodeReader reader; // An instance of the BinTreeNodeReader class.
	public Logger logger;
	public boolean log;
	public String dataFolder; //

	/**
	 * Default class constructor.
	 *
	 * @param String
	 *            number The user phone number including the country code
	 *            without '+' or '00'.
	 * @param String
	 *            nickname The user name.
	 * @param boolean debug Debug on or off, false by default.
	 * @param $log
	 *            Enable log, false by default.
	 * @param String
	 *            datafolder The folder for whatsapp data like MEDIA, PICTURES
	 *            etc.. By default that is wadata in src folder
	 * @throws IOException
	 */
	public WhatsProt(String number, String nickname, boolean debug,
			boolean log, String datafolder) throws IOException {

		this.writer = new BinTreeNodeWriter();
		this.reader = new BinTreeNodeReader();

		this.debug = debug;
		this.phoneNumber = number;

		if (datafolder == null)
			datafolder = ""; // To prevent exception
		else
			datafolder = datafolder.trim();
		File f = new File(datafolder);

		if (f.exists()) {
			if (datafolder.endsWith(File.separator))
				datafolder += File.separator;
			this.dataFolder = datafolder;

			File fmedia = new File(datafolder + Constants.MEDIA_FOLDER);
			fmedia.createNewFile();
			File fpict = new File(datafolder + Constants.PICTURES_FOLDER);
			fpict.createNewFile();
		} else
			this.dataFolder = System.getProperty("user.dir") + File.separator
					+ Constants.DATA_FOLDER + File.separator;

		// wadata/nextChallenge.12125557788.dat
		this.challengeFilename = String.format("%snextChallenge.%s.dat",
				this.dataFolder, number);
		this.log = log;
		if (log)
			this.logger = new Logger(this.dataFolder + "logs" + File.separator
					+ number + ".log");

		this.name = nickname;
		this.loginStatus = Constants.DISCONNECTED_STATUS;
		this.mediaQueue = new HashMap<String, Object>();
		this.nodeId = new HashMap<String, String>();

		this.messageQueue = new ArrayList<ProtocolNode>();
		this.outQueue = new ArrayList<ProtocolNode>();

		this.sessionCiphers = new HashMap<String, String>();
		this.groupCiphers = new HashMap<String, String>();

		/*
		 * TODO kkk this.eventManager = new WhatsApiEventsManager();
		 * 
		 * this.setAxolotlStore(new axolotlSqliteStore(number));
		 */
	}

	/**
	 * If you need use different challenge fileName you can use this
	 * 
	 * @param filename
	 *            the challengeFilename to set
	 */
	public void setChallengeFilename(String filename) {
		this.challengeFilename = filename;
	}

	/**
	 * Add message to the outgoing queue.
	 *
	 * @param ProtocolNode
	 *            node
	 */
	public void addMsgOutQueue(ProtocolNode node) {
		outQueue.add(node);
	}

	/**
	 * Connect (create a socket) to the WhatsApp network.
	 *
	 * @return boolean
	 */
	public boolean connect() {
		if (isConnected()) {
			return true;
		}

		try {
			/* Create a TCP/IP socket. */
			String waserver = String.format("e%d.whatsapp.net",
					Funcs.randInt(1, 16));
			Socket lsocket = new Socket(waserver, Constants.PORT);
			lsocket.setSoTimeout(Constants.TIMEOUT_SEC);

			if (lsocket.isConnected()) {
				this.socket = lsocket;
				/*
				 * TODO kkk $this->eventManager()->fire("onConnect", array(
				 * $this->phoneNumber, $this->socket ) );
				 */
				logFile("info", "Connected to WA server");
				return true;
			} else {
				logFile("error", "Failed to connect WA server");
				/*
				 * TODO kkk $this->eventManager()->fire("onConnectError", array(
				 * $this->phoneNumber, $this->socket ) );
				 */
				return false;
			}
		} catch (Exception e) {
			System.out.println("Create socket error: " + e.getMessage());
			logFile("error", "Failed to connect WA server" /* ,e.getMessage() */);
			return false;
		}
	}

	/**
	 * Do we have an active socket connection to WhatsApp?
	 *
	 * @return boolean
	 */
	public boolean isConnected() {
		return (this.socket.isConnected());
	}

	/**
	 * Disconnect from the WhatsApp network.
	 */
	public void disconnect() {
		try {
			socket.close();
		} catch (Exception e) {
			logFile("error", "Failed to close socket" /* ,e.getMessage() */);
		} finally {
			socket = null;
			loginStatus = Constants.DISCONNECTED_STATUS;
			logFile("info", "Disconnected from WA server");
			/*
			 * TODO kkk $this->eventManager()->fire("onDisconnect", array(
			 * $this->phoneNumber, $this->socket ) );
			 */}
	}

	/**
	 * TODO kkk
	 * 
	 * @return WhatsApiEventsManager
	 * 
	 *         public function eventManager() { return $this->eventManager; }
	 */

	/**
	 * Enable / Disable automatic read receipt This is enabled by default
	 */
	public void enableReadReceipt(boolean enable) {
		this.readReceipts = enable;
	}

	/**
	 * Drain the message queue for application processing.
	 *
	 * @return ProtocolNode[] Return the message queue list.
	 */
	public List<ProtocolNode> getMessages() {
		List<ProtocolNode> ret = new ArrayList<ProtocolNode>();
		ret.addAll(this.messageQueue);
		this.messageQueue.clear();
		return ret;
	}

	/**
	 * Login to the WhatsApp server with your password
	 *
	 * If you already know your password you can log into the Whatsapp server
	 * using this method.
	 *
	 * @param string
	 *            password Your whatsapp password. You must already know this!
	 */
	public void loginWithPassword(String password) {
		this.password = password;

		try (BufferedReader br = new BufferedReader(new FileReader(
				challengeFilename))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			challengeData = sb.toString();
		} catch (IOException e) {
			logFile("error", "Error reading challengeFile" /* ,e.getMessage() */);
		}

		Login login = new Login(this, this.password);
		login.doLogin();
	}

	public String getMyNumber() {
		return this.phoneNumber;
	}

	public void logFile(String tag, String message /* , $context = array() */) {
		if (this.log) {
			this.logger.log(tag, message /* , $context */);
		}
	}

	/**
	 * Fetch a single message node
	 *
	 * @throws Exception
	 */
	public boolean pollMessage() throws Exception {
		if (!this.isConnected()) {
			throw new Exception("Connection Closed!");
		}

		/*
		 * TODO kkk $r = array($this->socket); $w = array(); $e = array(); $s =
		 * socket_select($r, $w, $e, Constants::TIMEOUT_SEC,
		 * Constants::TIMEOUT_USEC);
		 * 
		 * if ($s) { // Something to read if ($stanza = $this->readStanza()) {
		 * $this->processInboundData($stanza); return true; } }
		 */
		if (System.currentTimeMillis() - this.timeout * 1000 > 60) {
			this.sendPing();
		}
		return false;
	}

	/**
	 * Have we an active connection with WhatsAPP AND a valid login already?
	 */
	public boolean isLoggedIn() {
		// If you aren't connected you can't be logged in!
		// ($this->isConnected())
		// We are connected - but are we logged in? (the rest)
		return (this.isConnected() && !this.loginStatus.isEmpty() && this.loginStatus == Constants.CONNECTED_STATUS);
	}

	/**
	 * Read 1024 bytes from the whatsapp server.
	 *
	 * @throws Exception
	 */
	public String readStanza() {
		/*
		 * TODO kkk $buff = ''; if ($this->isConnected()) { $header =
		 * 
		 * @socket_read($this->socket, 3);//read stanza header if ($header ===
		 * false) { $this->eventManager()->fire("onClose", array(
		 * $this->phoneNumber, 'Socket EOF' ) ); }
		 * 
		 * if (strlen($header) == 0) { //no data received return; } if
		 * (strlen($header) != 3) { throw new
		 * ConnectionException("Failed to read stanza header"); } $treeLength =
		 * (ord($header[0]) & 0x0F) << 16; $treeLength |= ord($header[1]) << 8;
		 * $treeLength |= ord($header[2]) << 0;
		 * 
		 * //read full length $buff = socket_read($this->socket, $treeLength);
		 * //$trlen = $treeLength; $len = strlen($buff); //$prev = 0; while
		 * (strlen($buff) < $treeLength) { $toRead = $treeLength -
		 * strlen($buff); $buff .= socket_read($this->socket, $toRead); if ($len
		 * == strlen($buff)) { //no new data read, fuck it break; } $len =
		 * strlen($buff); }
		 * 
		 * if (strlen($buff) != $treeLength) { throw new
		 * ConnectionException("Tree length did not match received length (buff = "
		 * . strlen($buff) . " & treeLength = $treeLength)"); } $buff = $header
		 * . $buff; }
		 * 
		 * return $buff;
		 */

		return "";
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
	 * Send node to the WhatsApp server.
	 * 
	 * @param ProtocolNode
	 *            node
	 * @param boolean encrypt
	 */
	public void sendNode(ProtocolNode node, boolean encrypt) {
		this.timeout = System.currentTimeMillis() / 1000;
		this.debugPrint(node.nodeString("tx  ") + "\n");
		this.sendData(this.writer.write(node, encrypt));
	}

	public void sendNode(ProtocolNode node) {
		sendNode(node, true);
	}

	public String getChallengeData() {
		return this.challengeData;
	}

	/**
	 * Send data to the WhatsApp server.
	 * 
	 * @param String
	 *            $data
	 *
	 * @throws Exception
	 */
	public void sendData(String data) {
		if (this.isConnected()) {
			try {
				this.socket.getOutputStream().write(data.getBytes());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logFile("error", "Failed to connect WA server" /*
																 * ,e.getMessage(
																 * )
																 */);
			}

			/*
			 * TODO kkk if (socket_write($this->socket, $data, strlen($data))
			 * === false) { $this->eventManager()->fire("onClose", array(
			 * $this->phoneNumber, "Connection closed!" ) ); }
			 */
		}
	}

	/**
	 * Print a message to the debug console.
	 *
	 * @param mixed
	 *            debugMsg The debug message.
	 * @return boolean
	 */
	public boolean debugPrint(String debugMsg) {
		/*
		 * TODO kkk if ($this->debug) { if (is_array($debugMsg) ||
		 * is_object($debugMsg)) { print_r($debugMsg);
		 * 
		 * } else { echo $debugMsg; } return true; }
		 */
		if (this.debug)
			System.out.println(debugMsg);

		return false;
	}

	/**
	 * Send the active status. User will show up as "Online" (as long as socket
	 * is connected).
	 */
	public void sendActiveStatus() {
		Map<String, String> attributeHash = new HashMap<String, String>();
		attributeHash.put("type", "active");

		ProtocolNode messageNode = new ProtocolNode("presence", attributeHash,
				null, null);
		this.sendNode(messageNode);
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
		long time = System.currentTimeMillis();
		if (System.currentTimeMillis() - this.timeout * 1000 > 60) {

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
					&& System.currentTimeMillis() - time < timeout * 1000);
		}
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

	public void sendRetry(String to, String id, String t) {
		sendRetry(to, id, t, null);
	}

	public void sendRetry(String to, String id, String t, String participant) {
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		Map<String, String> attributeHash = new HashMap<String, String>();

		attributeHash.put("id", id);
		attributeHash.put("v", "1");
		attributeHash.put("count", Integer.toString(this.retryCounter));
		attributeHash.put("t", t);

		ProtocolNode retryNode = new ProtocolNode("retry", attributeHash, null,
				null);
		// TODO kkk ProtocolNode registrationNode = new
		// ProtocolNode("registration", null, null,
		// adjustId($this->axolotlStore->getLocalRegistrationId()));
		ProtocolNode registrationNode = new ProtocolNode("registration", null,
				null, null);

		attributeHash.clear();
		attributeHash.put("id", id);
		attributeHash.put("to", "to");
		attributeHash.put("type", "retry");
		attributeHash.put("t", t);

		if (participant != null) // isgroups group retry
			attributeHash.put("participant", participant);
		else
			this.retryCounter++;

		children.add(retryNode);
		children.add(registrationNode);

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
		this.waitForServer(id);
	}

	/**
	 * Send a Broadcast Message with audio.
	 *
	 * The recipients MUST have your number (synced) and in their contact list
	 * otherwise the message will not deliver to that person.
	 *
	 * Approx 20 (unverified) is the maximum number of targets
	 *
	 * @param ArrayList
	 *            <String> targets An array of numbers to send to.
	 * @param String
	 *            path URL or local path to the audio file to send
	 * @param boolean storeURLmedia Keep a copy of the audio file on your server
	 * @param int fsize
	 * @param String
	 *            fhash
	 * @return string|null Message ID if successfully, null if not.
	 */
	public String sendBroadcastAudio(ArrayList<String> targets, String path) {
		return sendBroadcastAudio(targets, path, false, 0, "");
	}

	public String sendBroadcastAudio(ArrayList<String> targets, String path,
			boolean storeURLmedia, int fsize, String fhash) {
		// Return message ID. Make pull request for this.
		return this.sendMessageAudio(targets, path, storeURLmedia, fsize,
				fhash, storeURLmedia);
	}

	/**
	 * Send a Broadcast Message with a video.
	 *
	 * The recipients MUST have your number (synced) and in their contact list
	 * otherwise the message will not deliver to that person.
	 *
	 * Approx 20 (unverified) is the maximum number of targets
	 *
	 * @param ArrayList
	 *            <String> targets An array of numbers to send to.
	 * @param String
	 *            path URL or local path to the video file to send
	 * @param boolean storeURLmedia Keep a copy of the audio file on your server
	 * @param int fsize
	 * @param String
	 *            fhash
	 * @param String
	 *            caption
	 * @return String|null Message ID if successfully, null if not.
	 */
	public String sendBroadcastVideo(ArrayList<String> targets, String path) {
		return sendBroadcastVideo(targets, path, false, 0, "", "");
	}

	public String sendBroadcastVideo(ArrayList<String> targets, String path,
			boolean storeURLmedia, int fsize, String fhash, String caption) {
		// Return message ID. Make pull request for this.
		return this.sendMessageVideo(targets, path, storeURLmedia, fsize,
				fhash, caption);
	}

	/**
	 * Send a Broadcast Message with an image.
	 *
	 * The recipients MUST have your number (synced) and in their contact list
	 * otherwise the message will not deliver to that person.
	 *
	 * Approx 20 (unverified) is the maximum number of targets
	 *
	 * @param ArrayList
	 *            <String> targets An array of numbers to send to.
	 * @param String
	 *            path URL or local path to the image file to send
	 * @param boolean storeURLmedia Keep a copy of the audio file on your server
	 * @param int fsize
	 * @param String
	 *            fhash
	 * @param String
	 *            caption
	 * @return String|null Message ID if successfully, null if not.
	 */
	public String sendBroadcastImage(ArrayList<String> targets, String path) {
		return sendBroadcastImage(targets, path, false, 0, "", "");
	}

	public String sendBroadcastImage(ArrayList<String> targets, String path,
			boolean storeURLmedia, int fsize, String fhash, String caption) {
		// Return message ID. Make pull request for this.
		return this.sendMessageImage(targets, path, storeURLmedia, fsize,
				fhash, caption);
	}

	/**
	 * Send a Broadcast Message with location data.
	 *
	 * The recipients MUST have your number (synced) and in their contact list
	 * otherwise the message will not deliver to that person.
	 *
	 * If no name is supplied , receiver will see large sized google map
	 * thumbnail of entered Lat/Long but NO name/url for location.
	 *
	 * With name supplied, a combined map thumbnail/name box is displayed Approx
	 * 20 (unverified) is the maximum number of targets
	 *
	 * @param ArrayList
	 *            <String>targets An array of numbers to send to.
	 * @param float lng The longitude of the location eg 54.31652
	 * @param float lat The latitude if the location eg -6.833496
	 * @param String
	 *            name (Optional) A name to describe the location
	 * @param String
	 *            url (Optional) A URL to link location to web resource
	 * @return String Message ID
	 */
	public String sendBroadcastLocation(ArrayList<String> targets, float lng,
			float lat) {
		return sendBroadcastLocation(targets, lng, lat, null, null);
	}

	public String sendBroadcastLocation(ArrayList<String> targets, float lng,
			float lat, String name, String url) {
		// Return message ID. Make pull request for this.
		return this.sendMessageLocation(targets, lng, lat, name, url);
	}

	/**
	 * Send a broadcast
	 * 
	 * @param ArrayList
	 *            <String> $targets Array of numbers to send to
	 * @param Object
	 *            node
	 * @param String
	 *            type
	 * @return String
	 */
	protected String sendBroadcast(ArrayList<String> targets,
			ProtocolNode node, String type) {
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		Map<String, String> attributeHash = new HashMap<String, String>();

		for (String target : targets) {
			String jid = this.getJID(target);
			attributeHash.put("jid", jid);
			ProtocolNode toNode = new ProtocolNode("to", attributeHash, null,
					null);
			children.add(toNode);
			attributeHash.clear();
		}
		ProtocolNode broadcastNode = new ProtocolNode("broadcast", null,
				children, null);

		String msgId = this.createMsgId();

		attributeHash.clear();
		attributeHash.put("id", msgId);
		attributeHash.put("type", type);
		attributeHash.put("to", Long.toString(System.currentTimeMillis())
				+ "@broadcast");

		children.clear();
		children.add(node);
		children.add(broadcastNode);

		ProtocolNode messageNode = new ProtocolNode("message", attributeHash,
				children, null);
		this.sendNode(messageNode);
		this.waitForServer(msgId);

		/*
		 * TODO kkk /listen for response
		 * 
		 * $this->eventManager()->fire("onSendMessage", array(
		 * $this->phoneNumber, $targets, $msgId, $node ));
		 */

		return msgId;
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
		return sendMessageNode(to, node, null);
	}

	protected String sendMessageNode(String to, ProtocolNode node, String id) {
		String msgId = (id == null) ? this.createMsgId() : id;
		to = this.getJID(to);

		String type = (node.getTag() == "body" || node.getTag() == "enc") ? "text"
				: "media";

		Map<String, String> attributeHash = new HashMap<String, String>();
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();

		attributeHash.put("to", to);
		attributeHash.put("type", type);
		attributeHash.put("id", msgId);
		attributeHash.put("t", Long.toString(System.currentTimeMillis()));
		attributeHash.put("notify", this.name);

		children.add(node);

		ProtocolNode messageNode = new ProtocolNode("message", attributeHash,
				children, null);
		this.sendNode(messageNode);

		/*
		 * TODO kkk $this->logFile('info', '{type} message with id {id} sent to
		 * {to}', array('type' => $type, 'id' => $msgId, 'to' =>
		 * ExtractNumber($to))); $this->eventManager()->fire("onSendMessage",
		 * array( $this->phoneNumber, $to, $msgId, $node ));
		 */

		this.waitForServer(msgId);
		return msgId;
	}

	/**
	 * Send a location to the user/group.
	 *
	 * If no name is supplied, the receiver will see a large google maps
	 * thumbnail of the lat/long, but NO name or url of the location.
	 *
	 * When a name supplied, a combined map thumbnail/name box is displayed.
	 *
	 * @param ArrayList
	 *            <String> to The recipient(s) to send the location to.
	 * @param float lng The longitude of the location, e.g. 54.31652.
	 * @param float lat The latitude of the location, e.g. -6.833496.
	 * @param String
	 *            name (Optional) A custom name for the specified location.
	 * @param String
	 *            url (Optional) A URL to attach to the specified location.
	 * @return String Message ID
	 */
	public String sendMessageLocation(ArrayList<String> targets, float lng,
			float lat) {
		return sendMessageLocation(targets, lng, lat, null, null);
	}

	public String sendMessageLocation(ArrayList<String> to, float lng,
			float lat, String name, String url) {
		Map<String, String> attributeHash = new HashMap<String, String>();

		attributeHash.put("type", "location");
		attributeHash.put("encoding", "raw");
		attributeHash.put("latitude", Float.toString(lat));
		attributeHash.put("longitude", Float.toString(lng));
		attributeHash.put("name", name);
		attributeHash.put("url", url);

		ProtocolNode mediaNode = new ProtocolNode("media", attributeHash, null,
				null);
		String id = ((to.size() > 1) ? this.sendBroadcast(to, mediaNode,
				"media") : this.sendMessageNode(to.get(0), mediaNode));
		this.waitForServer(id);

		// Return message ID. Make pull request for this.
		return id;
	}

	/**
	 * Send a Broadcast Message
	 *
	 * The recipients MUST have your number (synced) and in their contact list
	 * otherwise the message will not deliver to that person.
	 *
	 * Approx 20 (unverified) is the maximum number of targets
	 *
	 * @param ArrayList
	 *            <String>targets An array of numbers to send to.
	 * @param String
	 *            message Your message
	 * @return String Message ID
	 */
	public String sendBroadcastMessage(ArrayList<String> targets, String message) {
		ProtocolNode bodyNode = new ProtocolNode("body", null, null, message);
		// Return message ID. Make pull request for this.
		return this.sendBroadcast(targets, bodyNode, "text");
	}

	/**
	 * Send an image file to group/user.
	 *
	 * @param ArrayList
	 *            <String> to Recipient number
	 * @param String
	 *            filepath The url/uri to the image file.
	 * @param boolean storeURLmedia Keep copy of file
	 * @param int fsize size of the media file
	 * @param String
	 *            fhash base64 hash of the media file
	 * @param String
	 *            caption
	 * @return String|null Message ID if successfully, null if not.
	 */
	public String sendMessageImage(ArrayList<String> to, String filepath) {
		return sendMessageImage(to, filepath, false, 0, "", "");
	}

	public String sendMessageImage(ArrayList<String> to, String filepath,
			boolean storeURLmedia, int fsize, String fhash, String caption) {
		if (fsize == 0 || fhash == "") {
			List<String> allowedExtensions = Arrays.asList("jpg", "jpeg",
					"gif", "png");
			int size = 5 * 1024 * 1024; // Easy way to set maximum file size
										// for this media type.
			// Return message ID. Make pull request for this.
			return this.sendCheckAndSendMedia(filepath, size, to, "image",
					allowedExtensions, storeURLmedia, caption);
		} else {
			// Return message ID. Make pull request for this.
			return this.sendRequestFileUpload(fhash, "image", fsize, filepath,
					to, caption);
		}
	}

	/**
	 * Send audio to the user/group.
	 *
	 * @param ArrayList
	 *            <String> to The recipient.
	 * @param String
	 *            filepath The url/uri to the audio file.
	 * @param boolean storeURLmedia Keep copy of file
	 * @param int fsize
	 * @param String
	 *            fhash
	 * @param boolean voice
	 * @return String|null Message ID if successfully, null if not.
	 */
	public String sendMessageAudio(ArrayList<String> to, String filepath) {
		return sendMessageAudio(to, filepath, false, 0, "", false);
	}

	public String sendMessageAudio(ArrayList<String> to, String filepath,
			boolean storeURLmedia, int fsize, String fhash) {
		return sendMessageAudio(to, filepath, storeURLmedia, fsize, fhash,
				false);
	}

	public String sendMessageAudio(ArrayList<String> to, String filepath,
			boolean storeURLmedia, int fsize, String fhash, boolean voice) {
		this.voice = voice;

		if (fsize == 0 || fhash == "") {
			List<String> allowedExtensions = Arrays.asList("3gp", "caf", "wav",
					"mp3", "wma", "ogg", "aif", "aac", "m4a");
			int size = 10 * 1024 * 1024; // Easy way to set maximum file size
											// for this media type.
			// Return message ID. Make pull request for this.
			return this.sendCheckAndSendMedia(filepath, size, to, "audio",
					allowedExtensions, storeURLmedia);
		} else {
			// Return message ID. Make pull request for this.
			return this.sendRequestFileUpload(fhash, "audio", fsize, filepath,
					to);
		}
	}

	/**
	 * Send a video to the user/group.
	 *
	 * @param ArrayList
	 *            <String> to The recipient.
	 * @param String
	 *            filepath A URL/URI to the MP4/MOV video.
	 * @param boolean storeURLmedia Keep a copy of media file.
	 * @param int fsize Size of the media file
	 * @param String
	 *            fhash base64 hash of the media file
	 * @param String
	 *            caption *
	 * @return String|null Message ID if successfully, null if not.
	 */
	public String sendMessageVideo(ArrayList<String> to, String filepath) {
		return sendMessageVideo(to, filepath, false, 0, "", "");
	}

	public String sendMessageVideo(ArrayList<String> to, String filepath,
			boolean storeURLmedia, int fsize, String fhash, String caption) {
		if (fsize == 0 || fhash == "") {
			List<String> allowedExtensions = Arrays.asList("3gp", "mp4", "mov",
					"avi");
			int size = 20 * 1024 * 1024; // Easy way to set maximum file size
											// for this media type.

			// Return message ID. Make pull request for this.
			return this.sendCheckAndSendMedia(filepath, size, to, "video",
					allowedExtensions, storeURLmedia, caption);
		} else {
			// Return message ID. Make pull request for this.
			return this.sendRequestFileUpload(fhash, "video", fsize, filepath,
					to, caption);
		}
	}

	/**
	 * Checks that the media file to send is of allowable filetype and within
	 * size limits.
	 *
	 * @param String
	 *            filepath The URL/URI to the media file
	 * @param int maxSize Maximum filesize allowed for media type
	 * @param ArrayList
	 *            <String> to Recipient ID/number
	 * @param String
	 *            type media filetype. 'audio', 'video', 'image'
	 * @param array
	 *            allowedExtensions An array of allowable file types for the
	 *            media file
	 * @param boolean storeURLmedia Keep a copy of the media file
	 * @param String
	 *            caption
	 * @return String|null Message ID if successfully, null if not.
	 */
	private String sendCheckAndSendMedia(String filepath, int maxSize,
			ArrayList<String> to, String type, List<String> allowedExtensions,
			boolean storeURLmedia) {
		return sendCheckAndSendMedia(filepath, maxSize, to, type,
				allowedExtensions, storeURLmedia, "");
	}

	private String sendCheckAndSendMedia(String filepath, int maxSize,
			ArrayList<String> to, String type, List<String> allowedExtensions,
			boolean storeURLmedia, String caption) {
		if (this.getMediaFile(filepath, maxSize) == true) {

			/*
			 * TODO kkk if (in_array($this->mediaFileInfo['fileextension'],
			 * $allowedExtensions)) { $b64hash =
			 * base64_encode(hash_file("sha256",
			 * $this->mediaFileInfo['filepath'], true)); //request upload and
			 * get Message ID $id = $this->sendRequestFileUpload($b64hash,
			 * $type, $this->mediaFileInfo['filesize'],
			 * $this->mediaFileInfo['filepath'], $to, $caption);
			 * $this->processTempMediaFile($storeURLmedia); // Return message
			 * ID. Make pull request for this. return $id; } else { //Not
			 * allowed file type. $this->processTempMediaFile($storeURLmedia);
			 * return null; }
			 */

			return null;
		} else {
			// Didn't get media file details.
			return null;
		}
	}

	/**
	 * Send request to upload file
	 *
	 * @param String
	 *            b64hash A base64 hash of file
	 * @param String
	 *            type File type
	 * @param int size File size
	 * @param String
	 *            filepath Path to image file
	 * @param ArrayList
	 *            <String> to Recipient(s)
	 * @param String
	 *            caption
	 * @return String Message ID
	 */
	private String sendRequestFileUpload(String b64hash, String type, int size,
			String filepath, ArrayList<String> to) {
		return sendRequestFileUpload(b64hash, type, size, filepath, to, "");
	}

	private String sendRequestFileUpload(String b64hash, String type, int size,
			String filepath, ArrayList<String> to, String caption) {
		String id = this.createIqId();

		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		Map<String, String> attributeHash = new HashMap<String, String>();

		attributeHash.put("hash", b64hash);
		attributeHash.put("type", type);
		attributeHash.put("size", Integer.toString(size));

		ProtocolNode mediaNode = new ProtocolNode("media", attributeHash, null,
				null);

		attributeHash.clear();
		attributeHash.put("id", id);
		attributeHash.put("to", Constants.WHATSAPP_SERVER);
		attributeHash.put("type", "set");
		attributeHash.put("xmlns", "w:m");

		children.add(mediaNode);

		ProtocolNode node = new ProtocolNode("iq", attributeHash, children,
				null);

		// add to queue
		String messageId = this.createMsgId();
		Map<String, Object> hashmap = new HashMap<String, Object>();
		hashmap.put("messageNode", node);
		hashmap.put("filePath", filepath);
		hashmap.put("to", to);
		hashmap.put("message_id", messageId);
		hashmap.put("caption", caption);
		this.mediaQueue.put(id, hashmap);

		this.sendNode(node);
		this.waitForServer(id);

		// Return message ID. Make pull request for this.
		return messageId;
	}

	/**
	 * Retrieves media file and info from either a URL or localpath
	 *
	 * @param String
	 *            filepath The URL or path to the mediafile you wish to send
	 * @param integer
	 *            maxsizebytes The maximum size in bytes the media file can be.
	 *            Default 5MB
	 *
	 * @return boolean false if file information can not be obtained.
	 */
	private boolean getMediaFile(String filepath) {
		return getMediaFile(filepath, 5242880);
	}

	private boolean getMediaFile(String filepath, int maxsizebytes) {
		/*
		 * TODO kkk if (filter_var($filepath, FILTER_VALIDATE_URL) !== false) {
		 * $this->mediaFileInfo = array(); $this->mediaFileInfo['url'] =
		 * $filepath;
		 * 
		 * $media = file_get_contents($filepath);
		 * $this->mediaFileInfo['filesize'] = strlen($media);
		 * 
		 * if ($this->mediaFileInfo['filesize'] < $maxsizebytes) {
		 * $this->mediaFileInfo['filepath'] = tempnam($this->dataFolder .
		 * Constants::MEDIA_FOLDER, 'WHA');
		 * file_put_contents($this->mediaFileInfo['filepath'], $media);
		 * $this->mediaFileInfo['filemimetype'] =
		 * get_mime($this->mediaFileInfo['filepath']);
		 * $this->mediaFileInfo['fileextension'] =
		 * getExtensionFromMime($this->mediaFileInfo['filemimetype']); return
		 * true; } else { return false; } } else if (file_exists($filepath)) {
		 * //Local file $this->mediaFileInfo['filesize'] = filesize($filepath);
		 * if ($this->mediaFileInfo['filesize'] < $maxsizebytes) {
		 * $this->mediaFileInfo['filepath'] = $filepath;
		 * $this->mediaFileInfo['fileextension'] = pathinfo($filepath,
		 * PATHINFO_EXTENSION); $this->mediaFileInfo['filemimetype'] =
		 * get_mime($filepath); return true; } else { return false; } }
		 */
		return false;
	}

	/**
	 * If the media file was originally from a URL, this function either deletes
	 * it or renames it depending on the user option.
	 *
	 * @param boolean storeURLmedia Save or delete the media file from local
	 *        server
	 */
	protected void processTempMediaFile(boolean storeURLmedia) {
		/*
		 * TODO kkk if (isset($this->mediaFileInfo['url'])) { if (storeURLmedia)
		 * { if (is_file($this->mediaFileInfo['filepath'])) {
		 * rename($this->mediaFileInfo['filepath'], $this->mediaFileInfo[
		 * 'filepath'].'.'.$this->mediaFileInfo['fileextension']); } } else { if
		 * (is_file($this->mediaFileInfo['filepath'])) {
		 * unlink($this->mediaFileInfo['filepath']); } } }
		 */
	}

	/**
	 * Create a unique msg id.
	 *
	 * @return string A message id string.
	 */
	protected String createMsgId() {
		// TODO kkk return $this->messageId . dechex($this->messageCounter++);
		return Integer.toHexString(this.messageCounter++);
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
	 * Process number/jid and turn it into a JID if necessary
	 *
	 * @param String
	 *            $number Number to process
	 * @return string
	 */
	public String getJID(String number) {
		if (!number.contains("@")) {
			// check if group message
			if (number.contains("-"))
				// to group
				number += "@" + Constants.WHATSAPP_GROUP_SERVER;
			else
				// to normal user
				number += "@" + Constants.WHATSAPP_SERVER;
		}
		return number;
	}

	/**
	 * Delete Broadcast lists
	 *
	 * @param ArrayList
	 *            <String> lists Contains the broadcast-id list
	 */
	public void sendDeleteBroadcastLists(ArrayList<String> lists) {
		String msgId = this.createIqId();

		if (lists == null || lists.size() == 0)
			return;

		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		Map<String, String> attributeHash = new HashMap<String, String>();

		for (String list : lists) {
			attributeHash.put("id", list);
			children.add(new ProtocolNode("list", attributeHash, null, null));
			attributeHash.clear();
		}

		ProtocolNode deleteNode = new ProtocolNode("delete", null, children,
				null);

		attributeHash.clear();
		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "w:b");
		attributeHash.put("type", "set");
		attributeHash.put("to", Constants.WHATSAPP_SERVER);

		children.clear();
		children.add(deleteNode);

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
	}

	/**
	 * Clears the "dirty" status on your account
	 *
	 * @param ArrayList
	 *            <String> categories
	 */
	protected void sendClearDirty(ArrayList<String> categories) {
		String msgId = this.createIqId();

		if (categories == null || categories.size() == 0)
			return;

		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		Map<String, String> attributeHash = new HashMap<String, String>();

		for (String category : categories) {
			attributeHash.put("type", category);
			children.add(new ProtocolNode("clean", attributeHash, null, null));
			attributeHash.clear();
		}

		attributeHash.clear();
		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "urn:xmpp:whatsapp:dirty");
		attributeHash.put("type", "set");
		attributeHash.put("to", Constants.WHATSAPP_SERVER);

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
	}

	public void sendClientConfig() {
		Map<String, String> attributeHash = new HashMap<String, String>();
		attributeHash.put("platform", Constants.WHATSAPP_DEVICE);
		attributeHash.put("version", Constants.WHATSAPP_VER);
		ProtocolNode child = new ProtocolNode("config", attributeHash, null, "");

		attributeHash.clear();
		attributeHash.put("id", this.createIqId());
		attributeHash.put("xmlns", "urn:xmpp:whatsapp:push");
		attributeHash.put("type", "set");
		attributeHash.put("to", Constants.WHATSAPP_SERVER);

		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		children.add(child);

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
	}

	public void sendGetClientConfig() {
		String msgId = this.createIqId();
		ProtocolNode child = new ProtocolNode("config", null, null, null);

		Map<String, String> attributeHash = new HashMap<String, String>();
		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "urn:xmpp:whatsapp:push");
		attributeHash.put("type", "get");
		attributeHash.put("to", Constants.WHATSAPP_SERVER);

		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		children.add(child);

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
		this.waitForServer(msgId);
	}

	/**
	 * Transfer your number to new one
	 *
	 * @param String
	 *            number
	 * @param String
	 *            identity
	 * @throws UnsupportedEncodingException
	 */
	public void sendChangeNumber(String number, String identity)
			throws UnsupportedEncodingException {
		String msgId = this.createIqId();

		ProtocolNode usernameNode = new ProtocolNode("username", null, null,
				number);
		ProtocolNode passwordNode = new ProtocolNode("password", null, null,
				URLDecoder.decode(identity, "UTF-8"));

		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		children.add(usernameNode);
		children.add(passwordNode);

		ProtocolNode modifyNode = new ProtocolNode("modify", null, children,
				null);

		children.clear();
		children.add(modifyNode);

		Map<String, String> attributeHash = new HashMap<String, String>();
		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "urn:xmpp:whatsapp:account");
		attributeHash.put("type", "get");
		attributeHash.put("to", "c.us");

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
	}

	/**
	 * Send a request to return a list of groups user is currently participating
	 * in.
	 *
	 * To capture this list you will need to bind the "onGetGroups" event.
	 */
	public void sendGetGroups() {
		this.sendGetGroupsFiltered("participating");
	}

	/**
	 * Send the getGroupList request to WhatsApp
	 * 
	 * @param String
	 *            type Type of list of groups to retrieve. "owning" or
	 *            "participating"
	 */
	protected void sendGetGroupsFiltered(String type) {
		String msgId = this.createIqId();
		this.nodeId.put("getgroups", msgId);
		ProtocolNode child = new ProtocolNode(type, null, null, null);
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		children.add(child);

		Map<String, String> attributeHash = new HashMap<String, String>();
		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "w:g2");
		attributeHash.put("type", "get");
		attributeHash.put("to", Constants.WHATSAPP_GROUP_SERVER);

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
		this.waitForServer(msgId);
	}

	/**
	 * Send a request to get new Groups V2 info.
	 *
	 * @param String
	 *            groupID The group JID
	 */
	public void sendGetGroupV2Info(String groupID) {
		String msgId = this.createIqId();
		this.nodeId.put("get_groupv2_info", msgId);

		Map<String, String> attributeHash = new HashMap<String, String>();
		attributeHash.put("request", "interactive");

		ProtocolNode queryNode = new ProtocolNode("query", attributeHash, null,
				null);
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		children.add(queryNode);

		attributeHash.clear();
		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "w:g2");
		attributeHash.put("type", "get");
		attributeHash.put("to", this.getJID(groupID));

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
	}

	/**
	 * Send a request to get a list of people you have currently blocked.
	 */
	public void sendGetPrivacyBlockedList() {
		String msgId = this.createIqId();
		this.nodeId.put("privacy", msgId);

		Map<String, String> attributeHash = new HashMap<String, String>();
		Map<String, String> attributeHash2 = new HashMap<String, String>();
		attributeHash.put("name", "default");

		ProtocolNode child = new ProtocolNode("list", attributeHash, null, null);
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		children.add(child);

		ProtocolNode child2 = new ProtocolNode("query", attributeHash2,
				children, null);

		children.clear();
		children.add(child2);

		attributeHash.clear();
		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "jabber:iq:privacy");
		attributeHash.put("type", "get");

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
		this.waitForServer(msgId);
	}

	/**
	 * Send a request to get privacy settings.
	 */
	public void sendGetPrivacySettings() {
		String msgId = this.createIqId();
		this.nodeId.put("privacy_settings", msgId);

		ProtocolNode privacyNode = new ProtocolNode("privacy", null, null, null);
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		children.add(privacyNode);

		Map<String, String> attributeHash = new HashMap<String, String>();
		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "privacy");
		attributeHash.put("type", "get");
		attributeHash.put("to", Constants.WHATSAPP_SERVER);

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
		this.waitForServer(msgId);
	}

	/**
	 * Set privacy of 'last seen', status or profile picture to all, contacts or
	 * none.
	 *
	 * @param String
	 *            category Options: 'last', 'status' or 'profile'
	 * @param String
	 *            value Options: 'all', 'contacts' or 'none'
	 */
	public void sendSetPrivacySettings(String category, String value) {
		String msgId = this.createIqId();

		Map<String, String> attributeHash = new HashMap<String, String>();
		attributeHash.put("name", category);
		attributeHash.put("value", value);

		ProtocolNode categoryNode = new ProtocolNode("category", attributeHash,
				null, null);

		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		children.add(categoryNode);

		ProtocolNode privacyNode = new ProtocolNode("privacy", null, children,
				null);
		children.clear();
		children.add(privacyNode);

		attributeHash.clear();
		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "privacy");
		attributeHash.put("type", "set");
		attributeHash.put("to", Constants.WHATSAPP_SERVER);

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
		this.waitForServer(msgId);
	}

	/**
	 * Get profile picture of specified user.
	 *
	 * @param String
	 *            number Number or JID of user
	 * @param boolean $large Request large picture
	 */
	public void sendGetProfilePicture(String number) {
		sendGetProfilePicture(number, false);
	}

	public void sendGetProfilePicture(String number, boolean large) {
		String msgId = this.createIqId();
		this.nodeId.put("getprofilepic", msgId);

		Map<String, String> attributeHash = new HashMap<String, String>();
		String type = (large ? "image" : "preview");
		attributeHash.put("type", type);

		ProtocolNode picture = new ProtocolNode("picture", attributeHash, null,
				null);

		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		children.add(picture);

		attributeHash.clear();
		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "w:profile:picture");
		attributeHash.put("type", "get");
		attributeHash.put("to", this.getJID(number));

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
		this.waitForServer(msgId);
	}

	/**
	 * @param ArrayList
	 *            <String> numbers Numbers to get profile profile photos of.
	 * @return boolean
	 */
	public boolean sendGetProfilePhotoIds(ArrayList<String> numbers) {
		String msgId = this.createIqId();

		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		Map<String, String> attributeHash = new HashMap<String, String>();
		for (String number : numbers) {
			attributeHash.put("jid", this.getJID(number));
			children.add(new ProtocolNode("user", attributeHash, null, null));
			attributeHash.clear();
		}

		if (children.size() == 0)
			return false;

		ProtocolNode listNode = new ProtocolNode("list", null, children, null);
		children.clear();
		children.add(listNode);

		attributeHash.clear();
		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "w:profile:picture");
		attributeHash.put("type", "get");

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
		this.waitForServer(msgId);

		return true;
	}

	/**
	 * Send a request to get the current server properties.
	 */
	public void sendGetServerProperties() {
		String msgId = this.createIqId();

		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		children.add(new ProtocolNode("user", null, null, null));

		Map<String, String> attributeHash = new HashMap<String, String>();
		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "w");
		attributeHash.put("type", "get");
		attributeHash.put("to", Constants.WHATSAPP_SERVER);

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
	}

	/**
	 * Send a request to get the current service pricing.
	 *
	 * @param String
	 *            lg Language
	 * @param String
	 *            lc Country
	 */
	public void sendGetServicePricing(String lg, String lc) {
		String msgId = this.createIqId();

		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		Map<String, String> attributeHash = new HashMap<String, String>();

		attributeHash.put("lg", lg);
		attributeHash.put("lc", "lc");

		ProtocolNode pricingNode = new ProtocolNode("pricing", attributeHash,
				null, null);
		children.add(pricingNode);

		attributeHash.clear();
		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "urn:xmpp:whatsapp:account");
		attributeHash.put("type", "get");
		attributeHash.put("to", Constants.WHATSAPP_SERVER);

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
	}

	/**
	 * Send a request to extend the account.
	 */
	public void sendExtendAccount() {
		String msgId = this.createIqId();

		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		Map<String, String> attributeHash = new HashMap<String, String>();

		ProtocolNode extendingNode = new ProtocolNode("extend", null, null,
				null);
		children.add(extendingNode);

		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "urn:xmpp:whatsapp:account");
		attributeHash.put("type", "set");
		attributeHash.put("to", Constants.WHATSAPP_SERVER);

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
	}

	/**
	 * Gets all the broadcast lists for an account.
	 */
	public void sendGetBroadcastLists() {
		String msgId = this.createIqId();
		this.nodeId.put("get_lists", msgId);

		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		Map<String, String> attributeHash = new HashMap<String, String>();

		ProtocolNode listsNode = new ProtocolNode("lists", null, null, null);
		children.add(listsNode);

		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "w:b");
		attributeHash.put("type", "get");
		attributeHash.put("to", Constants.WHATSAPP_SERVER);

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
	}

	/**
	 * Send a request to get the normalized mobile number representing the JID.
	 *
	 * @param String
	 *            countryCode Country Code
	 * @param String
	 *            number Mobile Number
	 */
	public void sendGetNormalizedJid(String countryCode, String number) {
		String msgId = this.createIqId();

		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		Map<String, String> attributeHash = new HashMap<String, String>();

		ProtocolNode ccNode = new ProtocolNode("cc", null, null, countryCode);
		ProtocolNode inNode = new ProtocolNode("in", null, null, number);
		children.add(ccNode);
		children.add(inNode);
		ProtocolNode normalizeNode = new ProtocolNode("normalize", null,
				children, null);
		children.clear();
		children.add(normalizeNode);

		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "urn:xmpp:whatsapp:account");
		attributeHash.put("type", "get");
		attributeHash.put("to", Constants.WHATSAPP_SERVER);

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
	}

	/**
	 * Removes an account from WhatsApp.
	 *
	 * @param String
	 *            lg Language
	 * @param String
	 *            lc Country
	 * @param String
	 *            feedback User Feedback
	 */
	public void sendRemoveAccount() {
		sendRemoveAccount(null, null, null);
	}

	public void sendRemoveAccount(String lg, String lc, String feedback) {
		String msgId = this.createIqId();

		List<ProtocolNode> children = new ArrayList<ProtocolNode>();
		Map<String, String> attributeHash = new HashMap<String, String>();

		if (feedback != null && feedback.length() > 0) {
			if (lg == null)
				lg = "";
			if (lc == null)
				lc = "";

			attributeHash.put("lg", lg);
			attributeHash.put("lc", lc);

			ProtocolNode child = new ProtocolNode("body", attributeHash, null,
					feedback);
			children.add(child);
		}

		ProtocolNode removeNode = null;
		if (children.size() == 0)
			removeNode = new ProtocolNode("remove", null, null, null);
		else
			removeNode = new ProtocolNode("remove", null, children, null);

		children.clear();
		children.add(removeNode);

		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "urn:xmpp:whatsapp:account");
		attributeHash.put("type", "get");
		attributeHash.put("to", Constants.WHATSAPP_SERVER);

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
		this.waitForServer(msgId);
	}

	/**
	 * Get the current status message of a specific user.
	 *
	 * @param List
	 *            <String> jids The users' JIDs
	 */
	public void sendGetStatuses(List<String> jids) {
		String msgId = this.createIqId();
		this.nodeId.put("getstatuses", msgId);

		Map<String, String> attributeHash = new HashMap<String, String>();
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();

		for (String jid : jids) {
			attributeHash.put("jid", this.getJID(jid));
			children.add(new ProtocolNode("user", attributeHash, null, null));
			attributeHash.clear();
		}
		ProtocolNode statusnode = new ProtocolNode("status", null, children,
				null);
		children.clear();
		children.add(statusnode);

		attributeHash.clear();
		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "status");
		attributeHash.put("type", "get");
		attributeHash.put("to", Constants.WHATSAPP_SERVER);

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
		this.waitForServer(msgId);
	}

	/**
	 * Create a group chat.
	 *
	 * @param String
	 *            subject The group Subject
	 * @param List
	 *            <String> participants An array with the participants numbers.
	 *
	 * @return String The group ID.
	 */
	public int sendGroupsChatCreate(String subject, List<String> participants) {
		String msgId = this.createIqId();
		this.nodeId.put("groupcreate", msgId);

		Map<String, String> attributeHash = new HashMap<String, String>();
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();

		for (String participant : participants) {
			attributeHash.put("jid", this.getJID(participant));
			children.add(new ProtocolNode("participant", attributeHash, null,
					null));
			attributeHash.clear();
		}

		attributeHash.put("subject", subject);

		ProtocolNode createNode = new ProtocolNode("create", attributeHash,
				children, null);

		children.clear();
		children.add(createNode);

		attributeHash.clear();
		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "w:g2");
		attributeHash.put("type", "set");
		attributeHash.put("to", Constants.WHATSAPP_GROUP_SERVER);

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
		this.waitForServer(msgId);

		int groupId = this.groupId;

		/*
		 * TODO kkk $this->eventManager()->fire("onGroupCreate", array(
		 * $this->phoneNumber, $groupId ));
		 */

		return groupId;
	}

	/**
	 * Change group's subject.
	 *
	 * @param String
	 *            gjid The group id
	 * @param String
	 *            subject The subject
	 */
	public void sendSetGroupSubject(String gjid, String subject) {
		String msgId = this.createIqId();

		Map<String, String> attributeHash = new HashMap<String, String>();
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();

		ProtocolNode child = new ProtocolNode("subject", null, null, subject);
		children.add(child);

		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "w:g2");
		attributeHash.put("type", "set");
		attributeHash.put("to", this.getJID(gjid));

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
	}

	/**
	 * Leave a group chat.
	 *
	 * @param List
	 *            <String> gjids Group or group's JIDs
	 */
	public void sendGroupsLeave(List<String> gjids) {
		String msgId = this.createIqId();
		nodeId.put("leavegroup", msgId);

		Map<String, String> attributeHash = new HashMap<String, String>();
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();

		for (String gjid : gjids) {
			attributeHash.put("id", this.getJID(gjid));
			children.add(new ProtocolNode("group", attributeHash, null, null));
			attributeHash.clear();
		}

		attributeHash.put("action", "delete");

		ProtocolNode leave = new ProtocolNode("leave", attributeHash, children,
				null);

		children.clear();
		children.add(leave);

		attributeHash.clear();
		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "w:g2");
		attributeHash.put("type", "set");
		attributeHash.put("to", Constants.WHATSAPP_GROUP_SERVER);

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
		this.waitForServer(msgId);
	}

	/**
	 * Change participants of a group.
	 *
	 * @param String
	 *            groupId The group ID.
	 * @param List
	 *            <String> participant The array of participants.
	 * @param String
	 *            tag The tag action. 'add', 'remove', 'promote' or 'demote'
	 * @param String
	 *            id
	 */
	protected void sendGroupsChangeParticipants(String groupId,
			List<String> participants, String tag, String id) {
		Map<String, String> attributeHash = new HashMap<String, String>();
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();

		/* IMPROVE kkk - The cycle made */

		for (String participant : participants) {
			attributeHash.put("jid", this.getJID(participant));
			children.add(new ProtocolNode("participant", attributeHash, null,
					null));
			attributeHash.clear();
		}

		ProtocolNode child = new ProtocolNode(tag, null, children, null);

		children.clear();
		children.add(child);

		attributeHash.clear();
		attributeHash.put("id", id);
		attributeHash.put("xmlns", "w:g2");
		attributeHash.put("type", "set");
		attributeHash.put("to", this.getJID(groupId));

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqNode);
		this.waitForServer(id);
	}

	/**
	 * Add participant(s) to a group.
	 *
	 * @param String
	 *            groupId The group ID.
	 * @param List
	 *            <String> participants An array with the participants numbers
	 *            to add
	 */
	public void sendGroupsParticipantsAdd(String groupId,
			List<String> participants) {
		String msgId = this.createMsgId();
		this.sendGroupsChangeParticipants(groupId, participants, "add", msgId);
	}

	/**
	 * Remove participant from a group.
	 *
	 * @param String
	 *            groupId The group ID.
	 * @param String
	 *            participant The number of the participant you want to remove
	 */
	public void sendGroupsParticipantsRemove(String groupId, String participant) {
		String msgId = this.createMsgId();
		List<String> participants = new ArrayList<String>();
		participants.add(participant);
		this.sendGroupsChangeParticipants(groupId, participants, "remove",
				msgId);
	}

	/**
	 * Promote participant of a group; Make a participant an admin of a group.
	 *
	 * @param String
	 *            gId The group ID.
	 * @param String
	 *            participant The number of the participant you want to promote
	 */
	public void sendPromoteParticipants(String groupId, String participant) {
		String msgId = this.createMsgId();
		List<String> participants = new ArrayList<String>();
		participants.add(participant);
		this.sendGroupsChangeParticipants(groupId, participants, "promote",
				msgId);
	}

	/**
	 * Demote participant of a group; remove participant of being admin of a
	 * group.
	 *
	 * @param String
	 *            gId The group ID.
	 * @param String
	 *            participant The number of the participant you want to demote
	 */
	public void sendDemoteParticipants(String groupId, String participant) {
		String msgId = this.createMsgId();
		List<String> participants = new ArrayList<String>();
		participants.add(participant);
		this.sendGroupsChangeParticipants(groupId, participants, "demote",
				msgId);
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
	 */
	public String sendMessage(String to, String plaintext) {
		return sendMessage(to, plaintext, false);
	}

	public String sendMessage(String to, String plaintext, boolean force_plain) {
		/*
		 * TODO kkk if (extension_loaded('curve25519') &&
		 * extension_loaded('protobuf') && !$force_plain) { $to_num =
		 * ExtractNumber($to); if (!(strpos($to,'-') !== false)) {
		 * 
		 * if(!$this->axolotlStore->containsSession($to_num, 1))
		 * $this->sendGetCipherKeysFromUser($to_num);
		 * 
		 * $sessionCipher = $this->getSessionCipher($to_num);
		 * 
		 * if (in_array($to_num, $this->v2Jids)) { $version = "2"; $plaintext =
		 * padMessage($plaintext); } else $version = "1"; $cipherText =
		 * $sessionCipher->encrypt($plaintext);
		 * 
		 * if ($cipherText instanceof WhisperMessage) $type = "msg"; else $type
		 * = "pkmsg"; $message = $cipherText->serialize(); $msgNode = new
		 * ProtocolNode("enc", array( "v" => $version, "type" => $type ), null,
		 * $message); } else {
		 */
		/*
		 * $type = "skmsg"; if (in_array($to, $this->v2Jids)) { $version = "2";
		 * $plaintext = padMessage($plaintext); } else $version = "1";
		 * die("NOT IMPLEMENTED\n"); //$message =
		 * "\x03".openssl_random_pseudo_bytes(30);
		 */
		// NOT IMPLEMENTED GROUP ENCRIPTED MESSAGE";
		/*
		 * TODO kkk $msgNode = new ProtocolNode("body", null, null, $plaintext);
		 * }
		 * 
		 * } else $msgNode = new ProtocolNode("body", null, null, $plaintext);
		 */

		ProtocolNode msgNode = new ProtocolNode("body", null, null, plaintext);
		String id = this.sendMessageNode(to, msgNode, null);
		if (this.messageStore != null)
			this.messageStore.saveMessage(this.phoneNumber, to, plaintext, id,
					Long.toString(System.currentTimeMillis()));
		return id;
	}

	/**
	 * Send the composing message status. When typing a message.
	 *
	 * @param String
	 *            to The recipient to send status to.
	 */
	public void sendMessageComposing(String to) {
		this.sendChatState(to, "composing");
	}

	/**
	 * Send the 'paused composing message' status.
	 *
	 * @param String
	 *            $to The recipient number or ID.
	 */
	public void sendMessagePaused(String to) {
		this.sendChatState(to, "paused");
	}

	protected void sendChatState(String to, String state) {
		Map<String, String> attributeHash = new HashMap<String, String>();
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();

		attributeHash.put("to", this.getJID(to));
		children.add(new ProtocolNode(state, null, null, null));

		ProtocolNode node = new ProtocolNode("chatstate", attributeHash,
				children, null);
		this.sendNode(node);
	}

	/**
	 * Send the next message.
	 */
	public void sendNextMessage() {
		if (this.outQueue.size() > 0) {
			ProtocolNode msgnode = this.outQueue.get(0);
			this.outQueue.remove(0);
			msgnode.refreshTimes();
			this.lastId = msgnode.getAttribute("id");
			this.sendNode(msgnode);
		} else {
			this.lastId = "-1";
		}
	}

	/**
	 * Send the offline status. User will show up as "Offline".
	 */
	public void sendOfflineStatus() {
		Map<String, String> attributeHash = new HashMap<String, String>();
		attributeHash.put("type", "inactive");
		ProtocolNode node = new ProtocolNode("presence", attributeHash, null,
				null);
		this.sendNode(node);
	}

	/**
	 * Send a ping to the WhatsApp server. I'm alive!
	 *
	 * @param String
	 *            msgid The id of the message.
	 */
	public void sendPong(String msgid) {
		Map<String, String> attributeHash = new HashMap<String, String>();
		attributeHash.put("to", Constants.WHATSAPP_SERVER);
		attributeHash.put("id", msgid);
		attributeHash.put("type", "result");

		ProtocolNode iqNode = new ProtocolNode("iq", attributeHash, null, null);
		this.sendNode(iqNode);

		/*
		 * TODO kkk $this->eventManager()->fire("onSendPong", array(
		 * $this->phoneNumber, $msgid ));
		 */
	}

	public void sendAvailableForChat() {
		sendAvailableForChat(null);
	}

	public void sendAvailableForChat(String nickname) {
		Map<String, String> attributeHash = new HashMap<String, String>();

		if (nickname != null) {
			// update nickname
			this.name = nickname;
		}
		attributeHash.put("name", this.name);
		ProtocolNode iqNode = new ProtocolNode("presence", attributeHash, null,
				null);
		this.sendNode(iqNode);
	}

	/**
	 * Send presence status.
	 *
	 * @param String
	 *            type The presence status.
	 */
	public void sendPresence() {
		sendPresence("active");
	}

	public void sendPresence(String type) {
		Map<String, String> attributeHash = new HashMap<String, String>();
		attributeHash.put("type", type);
		ProtocolNode node = new ProtocolNode("presence", attributeHash, null,
				null);
		this.sendNode(node);

		/*
		 * TODO kkk $this->eventManager()->fire("onSendPresence", array(
		 * $this->phoneNumber, $type, $this->name ));
		 */
	}

	/**
	 * Send presence subscription, automatically receive presence updates as
	 * long as the socket is open.
	 *
	 * @param String
	 *            to Phone number.
	 */
	public void sendPresenceSubscription(String to) {
		Map<String, String> attributeHash = new HashMap<String, String>();
		attributeHash.put("type", "subscribe");
		attributeHash.put("to", this.getJID(to));
		ProtocolNode node = new ProtocolNode("presence", attributeHash, null,
				null);
		this.sendNode(node);
	}

	/**
	 * Unsubscribe, will stop subscription.
	 *
	 * @param String
	 *            to Phone number.
	 */
	public void sendPresenceUnsubscription(String to) {
		Map<String, String> attributeHash = new HashMap<String, String>();
		attributeHash.put("type", "unsubscribe");
		attributeHash.put("to", this.getJID(to));
		ProtocolNode node = new ProtocolNode("presence", attributeHash, null,
				null);
		this.sendNode(node);
	}

	/**
	 * Set the picture for the group.
	 *
	 * @param String
	 *            gjid The groupID
	 * @param String
	 *            path The URL/URI of the image to use
	 */
	public void sendSetGroupPicture(String gjid, String path) {
		this.sendSetPicture(gjid, path);
	}

	/**
	 * Set your profile picture.
	 *
	 * @param String
	 *            path The URL/URI of the image to use
	 */
	public void sendSetProfilePicture(String path) {
		this.sendSetPicture(this.phoneNumber, path);
	}

	/**
	 * Set your profile picture
	 *
	 * @param String
	 *            jid
	 * @param String
	 *            filepath URL or localpath to image file
	 */
	protected void sendSetPicture(String jid, String filepath) {
		String msgId = this.createIqId();

		Map<String, String> attributeHash = new HashMap<String, String>();
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();

		/*
		 * TODO kkk $data = preprocessProfilePicture($filepath); $preview =
		 * createIconGD($filepath, 96, true);
		 */
		String data = "";
		String preview = "";

		attributeHash.put("type", "image");
		ProtocolNode pictureNode = new ProtocolNode("picture", attributeHash,
				null, data);
		attributeHash.clear();
		attributeHash.put("type", "preview");
		ProtocolNode previewNode = new ProtocolNode("picture", attributeHash,
				null, preview);
		children.add(pictureNode);
		children.add(previewNode);

		attributeHash.clear();
		attributeHash.put("id", "msgId");
		attributeHash.put("to", this.getJID(jid));
		attributeHash.put("type", "set");
		attributeHash.put("xmlns", "w:profile:picture");

		ProtocolNode iqnode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqnode);
		this.waitForServer(msgId);
	}

	/**
	 * Set the list of numbers you wish to block receiving from.
	 *
	 * @param ArrayList
	 *            <String> blockedJids One or more numbers to block messages
	 *            from.
	 */
	public void sendSetPrivacyBlockedList() {
		List<String> blockedJids = new ArrayList<String>();
		sendSetPrivacyBlockedList(blockedJids);
	}

	public void sendSetPrivacyBlockedList(List<String> blockedJids) {
		Map<String, String> attributeHash = new HashMap<String, String>();
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();

		attributeHash.put("type", "jid");
		attributeHash.put("action", "deny");

		int index = 1;
		for (String jid : blockedJids) {
			attributeHash.put("value", this.getJID(jid));
			attributeHash.put("order", Integer.toString(index++)); // WhatsApp
																	// stream
																	// crashes
																	// on zero
																	// index
			children.add(new ProtocolNode("iq", attributeHash, null, null));
		}

		attributeHash.clear();
		attributeHash.put("name", "default");
		ProtocolNode child = new ProtocolNode("list", attributeHash, children,
				null);

		children.clear();
		children.add(child);
		ProtocolNode child2 = new ProtocolNode("query", null, children, null);
		children.clear();
		children.add(child2);

		attributeHash.clear();
		attributeHash.put("id", this.createIqId());
		attributeHash.put("xmlns", "jabber:iq:privacy");
		attributeHash.put("type", "set");

		ProtocolNode iqnode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqnode);
	}

	/*
	 * Removes the profile photo.
	 */
	public void sendRemoveProfilePicture() {
		String msgId = this.createIqId();

		Map<String, String> attributeHash = new HashMap<String, String>();
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();

		ProtocolNode picture = new ProtocolNode("picture", null, null, null);
		attributeHash.put("type", "preview");
		ProtocolNode thumb = new ProtocolNode("picture", attributeHash, null,
				null);
		children.add(picture);
		children.add(thumb);

		attributeHash.clear();
		attributeHash.put("id", msgId);
		attributeHash.put("to", this.getJID(this.phoneNumber));
		attributeHash.put("xmlns", "w:profile:picture");
		attributeHash.put("type", "set");

		ProtocolNode iqnode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqnode);
	}

	/**
	 * Set the recovery token for your account to allow you to retrieve your
	 * password at a later stage.
	 *
	 * @param String
	 *            token A user generated token.
	 */
	public void sendSetRecoveryToken(String token) {
		String msgId = this.createIqId();

		Map<String, String> attributeHash = new HashMap<String, String>();
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();

		attributeHash.put("xmlns", "w:ch:p");
		ProtocolNode child = new ProtocolNode("pin", attributeHash, null, token);
		children.add(child);

		attributeHash.clear();
		attributeHash.put("id", msgId);
		attributeHash.put("to", Constants.WHATSAPP_SERVER);
		attributeHash.put("type", "set");

		ProtocolNode iqnode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqnode);
	}

	/**
	 * Update the user status.
	 *
	 * @param String
	 *            txt The text of the message status to send.
	 */
	public void sendStatusUpdate(String txt) {
		String msgId = this.createIqId();

		Map<String, String> attributeHash = new HashMap<String, String>();
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();

		ProtocolNode child = new ProtocolNode("status", null, null, txt);
		children.add(child);

		attributeHash.put("id", msgId);
		attributeHash.put("to", Constants.WHATSAPP_SERVER);
		attributeHash.put("type", "set");
		attributeHash.put("xmlns", "status");

		ProtocolNode iqnode = new ProtocolNode("iq", attributeHash, children,
				null);
		this.sendNode(iqnode);
		this.waitForServer(msgId);

		/*
		 * TODO kkk $this->eventManager()->fire("onSendStatusUpdate", array(
		 * $this->phoneNumber, $txt ));
		 */
	}

	/**
	 * Send a vCard to the user/group.
	 *
	 * @param String
	 *            to The recipient to send.
	 * @param String
	 *            name The contact name.
	 * @param Object
	 *            vCard The contact vCard to send.
	 * @return String Message ID
	 */
	public String sendVcard(String to, String name, /* TODO kkk Object */
			String vCard) {
		Map<String, String> attributeHash = new HashMap<String, String>();
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();

		attributeHash.put("name", name);
		ProtocolNode vCardNode = new ProtocolNode("vcard", attributeHash, null,
				vCard);
		children.add(vCardNode);

		attributeHash.clear();
		attributeHash.put("type", "vcard");
		ProtocolNode mediaNode = new ProtocolNode("media", attributeHash,
				children, null);

		// Return message ID. Make pull request for this.
		return this.sendMessageNode(to, mediaNode);
	}

	/**
	 * Send a vCard to the user/group as Broadcast.
	 *
	 * @param ArrayList
	 *            <String> $targets An array of recipients to send to.
	 * @param String
	 *            $name The vCard contact name.
	 * @param object
	 *            $vCard The contact vCard to send.
	 * @return String Message ID
	 */
	public String sendBroadcastVcard(ArrayList<String> targets, String name, /*
																			 * TODO
																			 * kkk
																			 * Object
																			 */
			String vCard) {
		Map<String, String> attributeHash = new HashMap<String, String>();
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();

		attributeHash.put("name", name);
		ProtocolNode vCardNode = new ProtocolNode("vcard", attributeHash, null,
				vCard);
		children.add(vCardNode);

		attributeHash.clear();
		attributeHash.put("type", "vcard");
		ProtocolNode mediaNode = new ProtocolNode("media", attributeHash,
				children, null);

		// Return message ID. Make pull request for this.
		return this.sendBroadcast(targets, mediaNode, "media");
	}

	/**
	 * Rejects a call
	 *
	 * @param String
	 *            to Phone number.
	 * @param String
	 *            id The main node id
	 * @param String
	 *            callId The call-id
	 */
	public void rejectCall(String to, String id, String callId) {
		Map<String, String> attributeHash = new HashMap<String, String>();
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();

		attributeHash.put("call-id", callId);
		ProtocolNode rejectNode = new ProtocolNode("reject", attributeHash,
				null, null);
		children.add(rejectNode);

		attributeHash.clear();
		attributeHash.put("id", "id");
		attributeHash.put("to", this.getJID(to));
		ProtocolNode callNode = new ProtocolNode("call", attributeHash,
				children, null);
		this.sendNode(callNode);
	}

	/**
	 * Sets the bind of the new message.
	 *
	 * @param Object
	 *            $bind
	 */
	public void setNewMessageBind(Object bind) {
		this.newMsgBind = bind;
	}

	public String sendSync(ArrayList<String> numbers) {
		return sendSync(numbers, null, 3);
	}

	public String sendSync(ArrayList<String> numbers,
			ArrayList<String> deletedNumbers, int syncType) {
		String msgId = this.createIqId();

		Map<String, String> attributeHash = new HashMap<String, String>();
		List<ProtocolNode> children = new ArrayList<ProtocolNode>();

		// number must start with '+' if international contact
		for (String number : numbers) {
			if (number.substring(0, 1) != "+")
				number = "+" + number;
			children.add(new ProtocolNode("user", null, null, number));
		}
		attributeHash.put("type", "delete");
		if (deletedNumbers != null)
			for (String number : deletedNumbers) {
				if (number.substring(0, 1) != "+")
					number = "+" + number;
				attributeHash.put("jid", this.getJID(number));
				children.add(new ProtocolNode("user", attributeHash, null, null));
			}

		String mode = "";
		String context = "";

		switch (syncType) {
		case 0:
			mode = "full";
			context = "registration";
			break;
		case 1:
			mode = "full";
			context = "interactive";
			break;
		case 2:
			mode = "full";
			context = "background";
			break;
		case 3:
			mode = "delta";
			context = "interactive";
			break;
		case 4:
			mode = "delta";
			context = "background";
			break;
		case 5:
			mode = "query";
			context = "interactive";
			break;
		case 6:
			mode = "chunked";
			context = "registration";
			break;
		case 7:
			mode = "chunked";
			context = "interactive";
			break;
		case 8:
			mode = "chunked";
			context = "background";
			break;
		default:
			mode = "delta";
			context = "background";
		}

		attributeHash.put("mode", mode);
		attributeHash.put("context", context);
		attributeHash
				.put("sid",
						Long.toString((System.currentTimeMillis() + 11644477200L) * 10000000));
		attributeHash.put("index", "0");
		attributeHash.put("last", "true");
		ProtocolNode syncNode = new ProtocolNode("sync", attributeHash,
				children, null);

		children.clear();
		children.add(syncNode);

		attributeHash.clear();
		attributeHash.put("id", msgId);
		attributeHash.put("xmlns", "urn:xmpp:whatsapp:sync");
		attributeHash.put("type", "get");
		ProtocolNode iqnode = new ProtocolNode("iq", attributeHash, children,
				null);

		this.sendNode(iqnode);
		this.waitForServer(msgId);

		return msgId;
	}

	public void setMessageStore(MessageStoreInterface messageStore) {
		/*
		 * kkk Так нужно создавать параметр messageStore try { this.messageStore
		 * = new SqliteMessageStore(number); } catch (ClassNotFoundException e)
		 * { // TODO Auto-generated catch block e.printStackTrace(); }
		 */
		this.messageStore = messageStore;
	}

	public MessageStoreInterface getMessageStore() {
		return this.messageStore;
	}

	public AxolotlInterface getAxolotlStore() {
		return this.axolotlStore;
	}

	public void setAxolotlStore(AxolotlInterface axolotlStore) {
		this.axolotlStore = axolotlStore;
	}

	/**
	 * Process the challenge.
	 *
	 * @param ProtocolNode
	 *            node The node that contains the challenge.
	 */
	protected void processChallenge(ProtocolNode node) {
		this.challengeData = node.getData();
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

	public void addPendingNode(ProtocolNode node) {
		String from = node.getAttribute("from");

		/*
		 * TODO kkk if(strpos($from,Constants::WHATSAPP_SERVER) !== false)
		 * $number = ExtractNumber($node->getAttribute("from")); else $number =
		 * ExtractNumber($node->getAttribute("participant"));
		 * 
		 * if(!isset($this->pending_nodes[$number]))
		 * $this->pending_nodes[$number] = [];
		 * 
		 * $this->pending_nodes[$number][] = $node;
		 */
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
		attributes.put("to", from);
		attributes.put("class", sclass);
		attributes.put("id", id);
		if (id != "")
			attributes.put("t", node.getAttribute("t"));
		if (type != "")
			attributes.put("type", type);
		ProtocolNode ack = new ProtocolNode("ack", attributes, null, null);
		this.sendNode(ack);
	}

	/**
	 * Process and save media image.
	 *
	 * @param ProtocolNode
	 *            node ProtocolNode containing media
	 */
	protected void processMediaImage(ProtocolNode node) {
		ProtocolNode media = node.getChild("media");

		if (media != null) {
			String filename = media.getAttribute("file");
			String url = media.getAttribute("url");
			/*
			 * TODO kkk //save thumbnail file_put_contents($this->dataFolder .
			 * Constants::MEDIA_FOLDER . DIRECTORY_SEPARATOR . 'thumb_' .
			 * $filename, $media->getData()); //download and save original
			 * file_put_contents($this->dataFolder . Constants::MEDIA_FOLDER .
			 * DIRECTORY_SEPARATOR . $filename, file_get_contents($url));
			 */
		}
	}

	/**
	 * Processes received picture node.
	 *
	 * @param ProtocolNode
	 *            node ProtocolNode containing the picture
	 */
	protected void processProfilePicture(ProtocolNode node) {
		ProtocolNode pictureNode = node.getChild("picture");
		String filename = "";
		if (pictureNode != null) {
			if (pictureNode.getAttribute("type") == "preview")
				filename = this.dataFolder + Constants.PICTURES_FOLDER
						+ File.separator + "preview_"
						+ node.getAttribute("from") + "jpg";
			else
				filename = this.dataFolder + Constants.PICTURES_FOLDER
						+ File.separator + node.getAttribute("from") + "jpg";

			// TODO kkk file_put_contents($filename, $pictureNode->getData());
		}
	}

	/**
	 * Process media upload response
	 *
	 * @param ProtocolNode
	 *            node Message node
	 * @return boolean
	 */
	public boolean processUploadResponse(ProtocolNode node) {
		String id = node.getAttribute("id");
		HashMap<String, Object> messageNode = new HashMap<String, Object>();
		messageNode.putAll((HashMap<String, Object>) this.mediaQueue.get(id));

		if (messageNode == null) {
			// message not found, can't send!
			/*
			 * TODO kkk $this->eventManager()->fire("onMediaUploadFailed",
			 * array( $this->phoneNumber, $id, $node, $messageNode,
			 * "Message node not found in queue" ));
			 */
			return false;
		}

		String url = "";
		String filesize = "";
		String filehash = "";
		String filetype = "";
		String filename = "";

		ProtocolNode duplicate = node.getChild("duplicate");
		if (duplicate != null) {
			// file already on whatsapp servers
			url = duplicate.getAttribute("url");
			filesize = duplicate.getAttribute("size");
			// $mimetype = $duplicate->getAttribute("mimetype");
			filehash = duplicate.getAttribute("filehash");
			filetype = duplicate.getAttribute("type");
			// $width = $duplicate->getAttribute("width");
			// $height = $duplicate->getAttribute("height");
			filename = url.substring(url.lastIndexOf('/') + 1);
		}
		/*
		 * TODO kkk else { //upload new file json =
		 * WhatsMediaUploader::pushFile($node, $messageNode,
		 * $this->mediaFileInfo, $this->phoneNumber);
		 * 
		 * if (!$json) { //failed upload
		 * $this->eventManager()->fire("onMediaUploadFailed", array(
		 * $this->phoneNumber, $id, $node, $messageNode,
		 * "Failed to push file to server" )); return false; }
		 * 
		 * $url = $json->url; $filesize = $json->size; // $mimetype =
		 * $json->mimetype; $filehash = $json->filehash; $filetype =
		 * $json->type; // $width = $json->width; // $height = $json->height;
		 * $filename = $json->name; }
		 */

		Map<String, String> mediaAttribs = new HashMap<String, String>();
		mediaAttribs.put("type", filetype);
		mediaAttribs.put("url", url);
		mediaAttribs.put("encoding", "raw");
		mediaAttribs.put("file", filename);
		mediaAttribs.put("size", filesize);
		String caption = (String) messageNode.get("caption");
		if (caption != "") {
			mediaAttribs.put("caption", caption);
		}
		if (this.voice == true) {
			mediaAttribs.put("origin", "live");
			this.voice = false;
		}

		String filepath = (String) messageNode.get("filePath");
		ArrayList<String> to = new ArrayList<String>();
		to.addAll((ArrayList<String>) messageNode.get("to"));
		String icon = "";
		switch (filetype) {
		case "image":
			caption = (String) messageNode.get("caption");
			// TODO kkk $icon = createIcon($filepath);
			break;
		case "video":
			caption = (String) messageNode.get("caption");
			// TODO kkk $icon = createVideoIcon($filepath);
			break;
		default:
			caption = "";
			icon = "";
			break;
		}
		// Retrieve Message ID
		String message_id = (String) messageNode.get("message_id");

		ProtocolNode mediaNode = new ProtocolNode("media", mediaAttribs, null,
				icon);
		if (to.size() == 1)
			this.sendMessageNode(to.get(0), mediaNode, message_id);
		else
			this.sendBroadcast(to, mediaNode, "media");

		/*
		 * TODO kkk $this->eventManager()->fire("onMediaMessageSent", array(
		 * $this->phoneNumber, $to, $message_id, $filetype, $url, $filename,
		 * $filesize, $filehash, $caption, $icon ));
		 */
		return true;
	}

	/**
	 * Tell the server we received the message.
	 *
	 * @param ProtocolNode
	 *            node The ProtocolTreeNode that contains the message.
	 * @param String
	 *            type
	 * @param String
	 *            participant
	 * @param String
	 *            callId
	 */
	public void sendReceipt(ProtocolNode node) {
		sendReceipt(node, "read", null, null);
	}

	public void sendReceipt(ProtocolNode node, String type, String participant,
			String callId) {
		Map<String, String> messageHash = new HashMap<String, String>();
		if (type == "read")
			messageHash.put("type", type);
		if (participant != null)
			messageHash.put("participant", participant);

		messageHash.put("to", node.getAttribute("from"));
		messageHash.put("id", node.getAttribute("id"));
		messageHash.put("t", node.getAttribute("t"));

		ProtocolNode messageNode = null;

		if (callId != null) {
			Map<String, String> attributeHash = new HashMap<String, String>();
			attributeHash.put("call-id", callId);
			ProtocolNode offerNode = new ProtocolNode("offer", attributeHash,
					null, null);
			List<ProtocolNode> children = new ArrayList<ProtocolNode>();
			children.add(offerNode);
			messageNode = new ProtocolNode("receipt", messageHash, children,
					null);
		} else {
			messageNode = new ProtocolNode("receipt", messageHash, null, null);
		}
		this.sendNode(messageNode);
		/*
		 * TODO kkk $this->eventManager()->fire("onSendMessageReceived", array(
		 * $this->phoneNumber, $node->getAttribute("id"),
		 * $node->getAttribute("from"), $type ));
		 */
	}

	/**
	 * Send a read receipt to a message.
	 *
	 * @param String
	 *            to The recipient.
	 * @param String
	 *            id
	 */
	public void sendMessageRead(String to, String id) {
		Map<String, String> attributeHash = new HashMap<String, String>();
		attributeHash.put("type", "read");
		attributeHash.put("to", to);
		attributeHash.put("id", id);
		ProtocolNode messageNode = new ProtocolNode("receipt", attributeHash,
				null, null);
		this.sendNode(messageNode);
	}

	/**
	 * @param String
	 *            jid
	 * @return String
	 */
	private String parseJID(String jid) {
		String parts = jid.substring(0, jid.indexOf('@') + 1);
		return parts;
	}

	public String getSessionCipher(String number) {
		String sessionCipher = this.sessionCiphers.get(number);
		if (sessionCipher == null) {
			// TODO kkk sessionCipher = new
			// SessionCipher($this->axolotlStore,$this->axolotlStore,$this->axolotlStore,$this->axolotlStore,$number,1);
			this.sessionCiphers.put(number, sessionCipher);
		}
		return sessionCipher;
	}

	public String getGroupCipher(String groupId) {
		String groupCipher = this.groupCiphers.get(groupId);
		if (groupCipher == null) {
			// TODO kkk groupCipher = new
			// GroupCipher($this->axolotlStore,$groupId);
			this.groupCiphers.put(groupId, groupCipher);
		}
		return groupCipher;
	}

	/**
	 * @return the readReceipts
	 */
	public boolean getReadReceipt() {
		return readReceipts;
	}

	/**
	 * @return the nodeId
	 */
	public HashMap<String, String> getNodeId() {
		return nodeId;
	}

	/**
	 * @return the v2Jids
	 */
	public List<String> getV2Jids() {
		return v2Jids;
	}

	/**
	 * @param String
	 *            author
	 */
	public void setV2Jids(String author) {
		this.v2Jids.add(author);
	}

	/**
	 * @param retryCounter
	 *            the retryCounter to set
	 */
	public void setRetryCounter(int retryCounter) {
		this.retryCounter = retryCounter;
	}

	/**
	 * @param groupId
	 *            the groupId to set
	 */
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public void setMessageId(String id) {
		this.messageId = id;
	}

	public void setChallengeData(String data) {
		this.challengeData = data;
	}

	public void setOutputKey(String outputKey) {
		this.outputKey = outputKey;
	}

	public String getLoginStatus() {
		return this.loginStatus;
	}

	/**
	 * @return the pending_nodes
	 */
	public List<String> getPending_nodes() {
		return pending_nodes;
	}

	/**
	 * @return the newMsgBind
	 */
	public Object getNewMsgBind() {
		return newMsgBind;
	}

	public void pushMessageToQueue(ProtocolNode node) {
		this.messageQueue.add(node);
	}

}
