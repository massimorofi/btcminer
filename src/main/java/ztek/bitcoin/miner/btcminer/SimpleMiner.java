package ztek.bitcoin.miner.btcminer;

import java.io.IOException;
import java.math.BigInteger;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.VerificationException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;;

/**
 * Hello world!
 *
 */
public class SimpleMiner {
	static Logger logger = LoggerFactory.getLogger(SimpleMiner.class);

	public static void main(String[] args) {

		try {
			StratumClient client = new StratumClient("massimo-Latitude-E6520", 3256, "1JazPKvRfGvdavcUAd1k4ZwzhxBAdVJnjR", "x");
			client.initialize();
			String gbt = " {\"id\": 0, \"method\": \"getblocktemplate\", \"params\": [{\"mode\":\"template\" , \"capabilities\": [\"support\" ,\"coinbasetxn\", \"workid\", \"coinbase/append\"],  \"rules\":[ \"support\"  ]}]}\n";
			client.sendRequest(gbt);
			JSONObject res= client.readResponse();
			logger.info(res.toString());
			while (true) {
				JSONObject response = client.readResponse();
				if (response != null) {
					logger.info(response.toString());
				} else {
					logger.info("Received null response");
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}

	}

	public static void solve(Block b, long startNonce) {
		while (true) {
			try {
				// Is our proof of work valid yet?
				if (checkProofOfWork(b, false))
					return;
				// No, so increment the nonce and try again.
				b.setNonce(startNonce);
				startNonce++;
				System.out.println("nonce=" + b.getNonce() + ", hash=" + b.getHashAsString());
			} catch (VerificationException e) {
				throw new RuntimeException(e); // Cannot happen.
			}
		}
	}

	/**
	 * Returns true if the hash of the block is OK (lower than difficulty target).
	 */
	protected static boolean checkProofOfWork(Block b, boolean throwException) throws VerificationException {
		// This part is key - it is what proves the block was as difficult to make as it
		// claims
		// to be. Note however that in the context of this function, the block can claim
		// to be
		// as difficult as it wants to be .... if somebody was able to take control of
		// our network
		// connection and fork us onto a different chain, they could send us valid
		// blocks with
		// ridiculously easy difficulty and this function would accept them.
		//
		// To prevent this attack from being possible, elsewhere we check that the
		// difficultyTarget
		// field is of the right value. This requires us to have the preceeding blocks.
		BigInteger target = b.getDifficultyTargetAsInteger();

		BigInteger h = b.getHash().toBigInteger();
		if (h.compareTo(target) > 0) {
			// Proof of work check failed!
			if (throwException)
				throw new VerificationException("Hash is higher than target: " + b.getHashAsString() + " vs " + target.toString(16));
			else
				return false;
		}
		return true;
	}

}
