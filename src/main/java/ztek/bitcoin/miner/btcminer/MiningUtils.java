package ztek.bitcoin.miner.btcminer;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Bytes;

public class MiningUtils {
	static Logger logger = LoggerFactory.getLogger(MiningUtils.class);
	/**
	 * @throws AuthenticationException
	 * @throws JSONException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static Block createBlockToMine(NetworkParameters netParams, BtcRpcClient client) throws AuthenticationException, JSONException, ClientProtocolException, IOException {
		// Create coinbase tx
		CoinBaseTX cbtx = new CoinBaseTX();
		logger.info("Original - " + cbtx.getHex().length() + " " + cbtx.getHex());
		Sha256Hash merkle = calculateHash(cbtx.getHex());
		JSONObject bTemplate = client.getBlockTemplate();
		JSONArray transactions = bTemplate.getJSONArray("transactions");
		// calculate Merkle root
		for (int i = 0; i < transactions.length(); i++) {
			byte[] conc = Bytes.concat(merkle.getBytes(), calculateHash(((JSONObject) transactions.get(i)).get("data").toString()).getBytes());
			merkle = Sha256Hash.wrapReversed(Sha256Hash.hashTwice(conc));
		}
		// Build block to mine
		long version = (new BigInteger(bTemplate.get("version").toString().toUpperCase(), 16)).longValue();
		Sha256Hash prevBlockHash = new Sha256Hash(bTemplate.get("previousblockhash").toString());
		Sha256Hash merkleRoot = new Sha256Hash(merkle.toString());
		long time = System.currentTimeMillis();
		long difficultyTarget = (new BigInteger(bTemplate.get("bits").toString().toUpperCase(), 16)).longValue();
		long nonce = 0;
		Block b = new Block(netParams, version, prevBlockHash, merkleRoot, time, difficultyTarget, nonce, new ArrayList<Transaction>());
		logger.info("Merkle root " + merkle.toString());
		logger.info("Block to mine " + b.toString());
		return b;
	}

	/**
	 * Calculates the block hash by serializing and hashing the resulting bytes.
	 */
	public static Sha256Hash calculateHash(String data) {
		byte[] bytes = Utils.parseAsHexOrBase58(data);
		logger.debug("Data size:" + data.length());
		return Sha256Hash.wrapReversed(Sha256Hash.hashTwice(bytes));
	}


}
