package ztek.bitcoin.miner.btcminer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StratumClient {
	// Connection streams
	private BufferedReader in;
	private PrintWriter out;
	private Socket socket;
	// credentials
	String username;
	String password;
	// logger
	Logger logger = LoggerFactory.getLogger(StratumClient.class);

	/**
	 * Connect to the Stratum pool
	 * 
	 * @param server
	 * @param port
	 * @param user
	 * @param password
	 * @throws IOException
	 */
	public StratumClient(String server, int port, String user, String password) throws IOException {
		InetAddress address = InetAddress.getByName(server);
		logger.info("Atempting to connect to " + address.toString() + " on port " + port + ".");
		// connect
		socket = new Socket();
		socket.connect(new InetSocketAddress(server, port));
		in = new BufferedReader(new InputStreamReader(new DataInputStream(socket.getInputStream())));
		out = new PrintWriter(new DataOutputStream(socket.getOutputStream()));
		this.username = user;
		this.password = password;

	}

	/**
	 * send and RPC JSON request
	 * 
	 * @param jsonMSG
	 */
	public void sendRequest(String jsonMSG) {
		out.println(jsonMSG);
		out.flush();
	}

	/**
	 * Read JSON-RPC response
	 * 
	 * @return
	 * @throws IOException
	 */
	public JSONObject readResponse() throws IOException {

		JSONObject json = null;
		String message = in.readLine();
		if (message != null) {
			json = new JSONObject(message);
			if (!json.has("result")) {
				logger.debug("JSON-RPC Notification : " + json.toString());
			} else {
				logger.debug("JSON-RPC response: " + json.toString());
			}
		} else {
			logger.debug("[null] input stream");
		}
		return json;
	}

	/**
	 * Initialise and authorise
	 */
	public void initialize() {
		try {
			String miningSubscribe = "{\"id\": 1,\"method\":\"mining.subscribe\",\"params\":[]}";
			sendRequest(miningSubscribe);
			readResponse();
			String authorizeMessage = "{\"params\": [\"" + username + "\", \"" + password + "\"], \"id\": \"2\", \"method\": \"mining.authorize\"}";
			sendRequest(authorizeMessage);
		} catch (IOException e) {
			logger.error("Cannot read the response: " + e.getMessage());
		}
	}

	/**
	 * Close the connection ...
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException {
		out.close();
		in.close();
		socket.close();
	}

}
