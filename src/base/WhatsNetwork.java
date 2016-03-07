package base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

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
	 * @throws Exception
	 */
	public void connect() throws Exception {
		if (isConnected())
			return;
		try {
			/* Create a TCP/IP socket. */
			Socket lsocket = new Socket(whatsHost, this.whatsPort);
			lsocket.setSoTimeout(this.recvTimeout);
			if (lsocket.isConnected())
				this.socket = lsocket;
		} catch (Exception e) {
			throw new Exception("Create socket error: " + e.getMessage());
		}
		if (!this.isConnected())
			throw new Exception("Cannot connect");
	}

	/**
	 * Do we have an active socket connection to WhatsApp?
	 *
	 * @return boolean
	 */
	public boolean isConnected() {
		if (this.socket == null)
			return false;
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
		} finally {
			socket = null;
			System.out.println("Disconnected from WA server ");
		}
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
	 * 
	 * @throws Exception
	 */
	public byte[] readStanza() throws Exception {
		byte[] nodeHeader = this.ReadData(3);

		if (nodeHeader == null || nodeHeader.length == 0) {
			// empty response
			return null;
		}

		if (nodeHeader.length != 3) {
			throw new Exception("Failed to read node header");
		}

		int treeLength = (nodeHeader[0] & 0x0F) << 16;
		treeLength |= nodeHeader[1] << 8;
		treeLength |= nodeHeader[2] << 0;

		// read full length
		ByteArrayOutputStream input = new ByteArrayOutputStream();
		input.write(nodeHeader);
		int btcnt = 0;
		byte buf[] = new byte[1024];
		while (btcnt < treeLength) {
			int r = this.socket.getInputStream().read(buf);
			if (r == -1)
				break;
			input.write(buf);
			btcnt += r;
		}

		if (input.size() != treeLength) {
			throw new ConnectException(
					"Tree length did not match received length (input = "
							+ input.size() + " & treeLength = " + treeLength
							+ ")");
		}
		return input.toByteArray();
	}

	// Send data to the whatsapp server
	// <param name="data">Data to be send as a byte array</param>
	public void SendData(byte[] data) throws IOException {
		Socket_send(data);
	}

	// Send data to the server
	// <param name="data">The data that needs to be send as a byte array</param>
	private void Socket_send(byte[] data) throws IOException {
		if (this.socket != null && this.socket.isConnected()) {
			this.socket.getOutputStream().write(data);
		} else {
			throw new ConnectException("Socket not connected");
		}
	}

}
