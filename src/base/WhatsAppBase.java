package base;

import helper.AccountInfo;
import helper.BinTreeNodeReader;
import helper.BinTreeNodeWriter;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import settings.Constants;
import chatapi.AxolotlInterface;
import chatapi.Logger;
import chatapi.MessageStoreInterface;
import chatapi.ProtocolNode;
import chatapi.SqliteMessageStore;

public class WhatsAppBase extends WhatsEventBase {

	protected AccountInfo accountInfo; // The AccountInfo object.
	protected boolean debug; // Determines whether debug mode is on or off.
	protected String challengeFilename; // Path to nextChallenge.dat.
	protected byte[] challengeData; //

	protected String password; // The user password.
	protected String loginStatus; // Holds the login status.
    
	public boolean hidden;

	protected String outputKey; // Instances of the KeyStream class.

	protected List<ProtocolNode> messageQueue; // Queue for received messages.

	protected String name; // The user name.
	protected String phoneNumber; // The user phone number including the country
	// code without '+' or '00'.

	public BinTreeNodeWriter writer; // An instance of the BinTreeNodeWriter
										// class.
	public BinTreeNodeReader reader; // An instance of the BinTreeNodeReader
										// class.
	public Logger logger;
	public boolean log;
	public String dataFolder; //

	protected long timeout = 0;

	protected WhatsNetwork whatsNetwork;

	public static final String SYSEncoding = "UTF-8";

	protected SqliteMessageStore messageStore = null;
	
	public void setMessageStore(SqliteMessageStore messageStore) {
		/*
		 * TODO kkk Так нужно создавать параметр messageStore try { this.messageStore
		 * = new SqliteMessageStore(number); } catch (ClassNotFoundException e)
		 * { // TODO Auto-generated catch block e.printStackTrace(); }
		 */
		this.messageStore = messageStore;
	}

	public MessageStoreInterface getMessageStore() {
		return this.messageStore;
	}

	public AxolotlInterface axolotlStore;
	public AxolotlInterface getAxolotlStore() {
		return this.axolotlStore;
	}

	public void setAxolotlStore(AxolotlInterface axolotlStore) {
		this.axolotlStore = axolotlStore;
	}

	
   public void constructBase(String number, String nickname, boolean debug,
			boolean log, String datafolder) throws IOException {

		this.debug = debug;
		this.phoneNumber = number;
		this.name = nickname;
		this.loginStatus = Constants.DISCONNECTED_STATUS;

		this.writer = new BinTreeNodeWriter();
		this.reader = new BinTreeNodeReader();

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
		
      /* TODO kkk  if (!file_exists($this->dataFolder.'logs')) {
            mkdir($this->dataFolder.'logs', 0777, true);
        } */


		// wadata/nextChallenge.12125557788.dat
		this.challengeFilename = String.format("%snextChallenge.%s.dat",
				this.dataFolder, number);
		
		setMessageStore(null); // number
		//TODO kkk    $this->messageStore = new SqliteMessageStore($number);
		// TODO kkk this.setAxolotlStore(new axolotlSqliteStore(number, , $this->dataFolder));

		this.log = log;
		if (log)
			this.logger = new Logger(this.dataFolder + "logs" + File.separator
					+ number + ".log");

		this.messageQueue = new ArrayList<ProtocolNode>();

		this.whatsNetwork = new WhatsNetwork(Constants.PORT,
				Constants.TIMEOUT_SEC);
	}

	public void Connect() {
		try {
			this.whatsNetwork.connect();
			this.loginStatus = Constants.CONNECTED_STATUS;
			// success
			this.fireOnConnectSuccess();
		} catch (Exception e) {
			this.fireOnConnectFailed(e);
		}
	}

	public void Disconnect() {
		Disconnect(null);
	}
	
	public void Disconnect(Exception ex) {
		this.whatsNetwork.disconnect();
		this.loginStatus = Constants.DISCONNECTED_STATUS;
		this.fireOnDisconnect(ex);
	}

	/**
	 * Send node to the WhatsApp server.
	 * 
	 * @param ProtocolNode
	 *            node
	 * @param boolean encrypt
	 */
	public void sendNode(ProtocolNode node) {
		sendNode(node, true);
	}

	public void sendNode(ProtocolNode node, boolean encrypt) {
		this.timeout = System.currentTimeMillis() / 1000L;
		this.debugPrint(node.nodeString("tx  ") + "\n");
		this.sendData(this.writer.write(node, encrypt));
	}

	/**
	 * Send data to the WhatsApp server.
	 * @param byte[]  $data
	 */
	public void sendData(byte[] data) { 
		try {
			this.whatsNetwork.SendData(data);
		} catch (Exception e) {
            this.Disconnect();
			/*
			 * TODO kkk $this->eventManager()->fire("onClose", array(
			 * $this->phoneNumber, "Connection closed!" ) );
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

	public byte[] encryptPassword()
    {
        try {
			return Base64.getDecoder().decode(this.password.getBytes(WhatsAppBase.SYSEncoding));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }
	/*
	 * TODO kkk - так делать не нужно. Оставил как пример шифровки 
	 * private static String encryptPassword(String
	 * password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
	 * 
	 * MessageDigest crypt = MessageDigest.getInstance("SHA-1"); crypt.reset();
	 * crypt.update(password.getBytes(WhatsAppBase.SYSEncoding));
	 * 
	 * return new BigInteger(1, crypt.digest()).toString(16); }
	 */

	public byte[] getChallengeData() {
		return challengeData;
	}

	public void setChallengeData(byte[] challengeData) {
		this.challengeData = challengeData;
	}

}
