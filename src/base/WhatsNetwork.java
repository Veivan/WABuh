package base;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

import settings.Constants;
import chatapi.Funcs;

public class WhatsNetwork {

	// The time between sending and recieving
	private int recvTimeout;
	// The hostname of the whatsapp server
	private String whatsHost;
	// The port of the whatsapp server
	private int whatsPort;

	protected Socket socket; // A socket to connect to the WhatsApp network.

	public WhatsNetwork(int port, int timeout) {
		this.whatsHost = String
				.format("e%d.whatsapp.net", Funcs.randInt(1, 16));
		this.whatsPort = port;
		this.recvTimeout = timeout;
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
			Socket lsocket = new Socket(whatsHost, this.whatsPort);
			lsocket.setSoTimeout(this.recvTimeout);

			if (lsocket.isConnected()) {
				this.socket = lsocket;
				/*
				 * TODO kkk $this->eventManager()->fire("onConnect", array(
				 * $this->phoneNumber, $this->socket ) );
				 */
				// TODO kkk logFile("info", "Connected to WA server");
				return true;
			} else {
				// TODO kkk logFile("error", "Failed to connect WA server");
				/*
				 * TODO kkk $this->eventManager()->fire("onConnectError", array(
				 * $this->phoneNumber, $this->socket ) );
				 */
				return false;
			}
		} catch (Exception e) {
			System.out.println("Create socket error: " + e.getMessage());
			// TODO kkk logFile("error", "Failed to connect WA server" /*
			// ,e.getMessage() */);
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
			System.out.println("Failed to close socket: " + e.getMessage());
			// TODO kkk logFile("error", "Failed to close socket" /*
			// ,e.getMessage() */);
		} finally {
			socket = null;
			System.out.println("Disconnected from WA server ");
			// TODO kkk logFile("info", "Disconnected from WA server");
			/*
			 * TODO kkk $this->eventManager()->fire("onDisconnect", array(
			 * $this->phoneNumber, $this->socket ) );
			 */}
	}

	// Read 1024 bytes
	public byte[] ReadData() throws IOException {
		return ReadData(1024);
	}

	public byte[] ReadData(int length) throws IOException {
		return Socket_read(length);
	}

	private byte[] Socket_read(int length) throws IOException {
		byte buf[] = new byte[length];
		int r = this.socket.getInputStream().read(buf);
		if (r > 0)
			return buf;
		else
			return null;
	}
	
	/**
	 * Read 1024 bytes from the whatsapp server.
	 * @throws Exception 
	 */
	public String readStanza() throws Exception {
        byte[] nodeHeader = this.ReadData(3);

        if (nodeHeader == null || nodeHeader.length == 0)
        {
            //empty response
            return null;
        }

        if (nodeHeader.length != 3)
        {
            throw new Exception("Failed to read node header");
        }

        String buff = "";
        
        String header = ""; // TODO kkk header == nodeHeader

		int treeLength = (header.charAt(0) & 0x0F) << 16;
		treeLength |= header.charAt(1) << 8;
		treeLength |= header.charAt(2) << 0;

		// read full length
		int btcnt = 0;
		byte buf[] = new byte[1024];
		while (btcnt < treeLength) {
			int r = this.socket.getInputStream().read(buf);
			if (r == -1)
				break;
			buff += new String(buf, 0, r);
			btcnt += r;
		}

		if (buff.length() != treeLength) {
			throw new ConnectException(
					"Tree length did not match received length (buff = "
							+ buff.length() + " & treeLength = " + treeLength
							+ ")");
		}
		buff = header + buff;
		return buff;
	}

    // Send data to the whatsapp server
    // <param name="data">Data to be send as a byte array</param>
    public void SendData(byte[] data) throws IOException
    {
        Socket_send(data);
    }

    // Send data to the server
    // <param name="data">The data that needs to be send as a byte array</param>
    private void Socket_send(byte[] data) throws IOException
    {
        if (this.socket != null && this.socket.isConnected())
        {
			this.socket.getOutputStream().write(data);
        }
        else
        {
            throw new ConnectException("Socket not connected");
        }
    }

}