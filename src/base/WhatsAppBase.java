package base;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import settings.Constants;
import chatapi.BinTreeNodeReader;
import chatapi.BinTreeNodeWriter;
import chatapi.Logger;
import chatapi.ProtocolNode;

public class WhatsAppBase extends ApiBase {

	private String accountInfo; // The AccountInfo object.
	protected boolean debug; // Determines whether debug mode is on or off.
	protected String challengeFilename; // Path to nextChallenge.dat.
	protected byte[] challengeData; //

	protected String password; // The user password.
	protected String loginStatus; // Holds the login status.

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

		// wadata/nextChallenge.12125557788.dat
		this.challengeFilename = String.format("%snextChallenge.%s.dat",
				this.dataFolder, number);
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
			// TODO kkk this.fireOnConnectSuccess();
		} catch (Exception e) {
			// TODO kkk this.fireOnConnectFailed(e);
		}
	}

	public void Disconnect() {
		this.whatsNetwork.disconnect();
		this.loginStatus = Constants.DISCONNECTED_STATUS;
		// TODO kkk this.fireOnDisconnect(ex);
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
		this.timeout = System.currentTimeMillis() / 1000;
		this.debugPrint(node.nodeString("tx  ") + "\n");
		this.sendData(this.writer.write(node, encrypt));
	}

	protected void SendData(byte[] data) {
		try {
			this.whatsNetwork.SendData(data);
		} catch (IOException e) {
			this.Disconnect();
		}
	}

	/**
	 * Send data to the WhatsApp server.
	 * 
	 * @param String
	 *            $data
	 *
	 * @throws Exception
	 */
	public void sendData(String data) { // TODO kkk rewrite param to byte[]
		try {
			this.whatsNetwork.SendData(data.getBytes(WhatsAppBase.SYSEncoding));
		} catch (IOException e) {
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

}
