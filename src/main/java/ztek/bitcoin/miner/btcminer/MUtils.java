package ztek.bitcoin.miner.btcminer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.UnsafeByteArrayOutputStream;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.ScriptBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Bytes;

public class MUtils {
	static Logger logger = LoggerFactory.getLogger(MUtils.class);

	/**
	 * @throws AuthenticationException
	 * @throws JSONException
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static Block createBlockToMine(NetworkParameters netParams, BtcRpcClient client)
			throws AuthenticationException, JSONException, ClientProtocolException, IOException {
		byte[] pubKey = Utils.HEX.decode("021aef7f21d5721cf5256461c9bad9179d1677de87cc7a2e9cb5154e2c818b1c33");

		// --->>> Create coinbase tx
		// CoinBaseTX cbtx = new CoinBaseTX();
		// logger.info("Original - " + cbtx.getHex().length() + " " + cbtx.getHex());

		JSONObject bTemplate = client.getBlockTemplate();
		Coin coin = Coin.valueOf(Long.parseLong(bTemplate.get("coinbasevalue").toString()) / 100000);
		int height = Integer.parseInt(bTemplate.get("height").toString());
		Transaction coinBase = createCoinbase(pubKey, coin, height);
		logger.info(coinBase.toString());
		Sha256Hash merkle = coinBase.getHash();
		JSONArray transactions = bTemplate.getJSONArray("transactions");
		// calculate Merkle root
		for (int i = 0; i < transactions.length(); i++) {
			byte[] transactionHash = calculateHash(((JSONObject) transactions.get(i)).get("data").toString()).getBytes();
			byte[] conc = Bytes.concat(merkle.getBytes(), transactionHash);
			merkle = Sha256Hash.wrapReversed(Sha256Hash.hashTwice(conc));
		}
		// Build block to mine
		long version = (new BigInteger(bTemplate.get("version").toString().toUpperCase(), 16)).longValue();
		Sha256Hash prevBlockHash = new Sha256Hash(bTemplate.get("previousblockhash").toString());
		Sha256Hash merkleRoot = new Sha256Hash(merkle.toString());
		long time = bTemplate.getLong("curtime");
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

	/**
	 * Serialize the block header
	 * 
	 * @param version
	 * @param prevHash
	 * @param merkle
	 * @param time
	 * @param nBits
	 * @param nonce
	 * @return
	 * @throws IOException
	 */
	public static String serializeHeaderString(long version, String prevHash, String merkle, long time, long nBits, long nonce) throws IOException {
		ByteArrayOutputStream stream = new UnsafeByteArrayOutputStream(Block.HEADER_SIZE);
		// fall back to manual write
		Utils.uint32ToByteStreamLE(version, stream);
		stream.write(new Sha256Hash(prevHash).getReversedBytes());
		stream.write(new Sha256Hash(merkle).getReversedBytes());
		Utils.uint32ToByteStreamLE(time, stream);
		Utils.uint32ToByteStreamLE(nBits, stream);
		Utils.uint32ToByteStreamLE(nonce, stream);
		return Utils.HEX.encode(stream.toByteArray());
	}
    /**
     * 
     * @param block
     * @return the serialized block
     * @throws IOException
     */
	public static String serializeHeaderString(Block block) throws IOException {
		return serializeHeaderString(block.getVersion(), block.getPrevBlockHash().toString(), block.getMerkleRoot().toString(), block.getTimeSeconds(),
				block.getDifficultyTarget(), block.getNonce());
	}
	
    /**
     * Create coun base transaction
     * @param pubKeyTo
     * @param value
     * @param height
     * @return
     */
	public static Transaction createCoinbase(byte[] pubKeyTo, Coin value, final int height) {
		MainNetParams params = MainNetParams.get();
		Transaction coinbase = new Transaction(params);
		final ScriptBuilder inputBuilder = new ScriptBuilder();
		if (height >= Block.BLOCK_HEIGHT_GENESIS) {
			inputBuilder.number(height);
		}
		int txCounter = 0;
		inputBuilder.data(new byte[] { (byte) txCounter, (byte) (txCounter++ >> 8) });
		// A real coinbase transaction has some stuff in the scriptSig like the
		// extraNonce and difficulty. The
		// transactions are distinguished by every TX output going to a different key.
		//
		// Here we will do things a bit differently so a new address isn't needed every
		// time. We'll put a simple
		// counter in the scriptSig so every transaction has a different hash.
		coinbase.addInput(new TransactionInput(params, coinbase, inputBuilder.build().getProgram()));
		coinbase.addOutput(new TransactionOutput(params, coinbase, value, ScriptBuilder.createOutputScript(ECKey.fromPublicOnly(pubKeyTo)).getProgram()));
		return coinbase;
		// coinbase.setParent(this);
		// coinbase.length = coinbase.unsafeBitcoinSerialize().length;
		// adjustLength(transactions.size(), coinbase.length);
	}

}
