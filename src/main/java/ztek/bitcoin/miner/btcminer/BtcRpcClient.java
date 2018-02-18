package ztek.bitcoin.miner.btcminer;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BtcRpcClient {
	static Logger logger = LoggerFactory.getLogger(BtcRpcClient.class);
	AtomicInteger counter = new AtomicInteger(0);
	String host = "http://localhost:8332";

	/**
	 * 
	 * @param command
	 * @return
	 * @throws AuthenticationException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public JSONObject call(String command) throws AuthenticationException, ClientProtocolException, IOException {
		logger.debug("Executing RPC call: " + command);
		CloseableHttpClient client = HttpClients.createDefault();
		// setup post
		HttpPost httpPost = new HttpPost(this.host);
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials("user", "password");
		httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));
		httpPost.setEntity(new StringEntity(command));
		httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));
		httpPost.setHeader("Content-type", "application/json");
		// execute post and get response
		ResponseHandler<String> responseHandler = new RpcResponseHandler();
		String response = client.execute(httpPost, responseHandler);
		client.close();
		return new JSONObject(response);
	}

	public String getBestBlockHash() throws AuthenticationException, JSONException, ClientProtocolException, IOException {
		// get latest hash
		int id = counter.incrementAndGet();
		String command = "{\"id\": " + id + ", \"method\": \"getbestblockhash\"}";
		return (String) call(command).get("result");
	}

	public JSONObject getBestBlock(String hash) throws AuthenticationException, JSONException, ClientProtocolException, IOException {
		// get latest hash
		int id = counter.incrementAndGet();
		String command = "{\"jsonrpc\": \"1.0\",\"id\": " + id + ", \"method\": \"getblock\", \"params\": [\"" + hash + "\" , 1]}";
		return (JSONObject) call(command).get("result");
	}

	public JSONObject getBlockTemplate() throws AuthenticationException, JSONException, ClientProtocolException, IOException {
		int id = counter.incrementAndGet();
		String command = "{\"id\": " + id
				+ ", \"method\": \"getblocktemplate\", \"params\": [{\"mode\":\"template\" , \"capabilities\": [\"support\" ,\"coinbasetxn\", \"workid\", \"coinbase/append\"],  \"rules\":[ \"support\"  ]}]}\n";
		return (JSONObject) call(command).get("result");
	}

	public JSONObject submitBlock(String blockHeader) throws AuthenticationException, JSONException, ClientProtocolException, IOException {
		int id = counter.incrementAndGet();
		String command = "{\"id\": " + id + ", \"method\": \"submitblock\", \"params\": [\"" + blockHeader + "\"]}";
		return (JSONObject) call(command);
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

}
