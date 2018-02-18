/**
 * 
 */
package ztek.bitcoin.miner.btcminer;

import java.io.IOException;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.VerificationException;
import org.bitcoinj.params.MainNetParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

import junit.framework.TestCase;

/**
 * @author massimo
 *
 */
public class BtcRpcClientTest extends TestCase {
	static Logger logger = LoggerFactory.getLogger(BtcRpcClientTest.class);

	/**
	 * @param name
	 */
	public BtcRpcClientTest(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@VisibleForTesting
	public void testSubmit() {
		BtcRpcClient client = new BtcRpcClient();
		String header = "020000003c48a294584f90e58325c60ca82896d071826b45680a661cec4d424d00000000de6433d46c0c7f50d84a05aec77be0199176cdd47f77e344b6f50c84380fddba66dc47501d00ffff000001000101000000010000000000000000000000000000000000000000000000000000000000000000ffffffff1302955d0f00456c6967697573005047dc66085fffffffff02fff1052a010000001976a9144ebeb1cd26d6227635828d60d3e0ed7d0da248fb88ac01000000000000001976a9147c866aee1fa2f3b3d5effad576df3dbf1f07475588ac00000000";
		try {
			JSONObject result = client.submitBlock(header);
			logger.info(result.toString());
			assertEquals("null", result.get("error").toString());
			assertEquals("high-hash".toUpperCase(), result.get("result").toString().toUpperCase());
			logger.info("----------- START HEADER MANIPULATION -------------");
			// manipulate string
			// Short
			header = "00000fffff1111199999";
			result = client.submitBlock(header);
			logger.info(result.toString());
			if (!result.get("error").toString().equalsIgnoreCase("null")) {
				logger.info("Error asserted");
				assertEquals(((JSONObject) result.get("error")).get("message").toString(), "Block decode failed");
			}
			// test block header creation and submit
			String blockHash="00000000000000000001a3a73858b76b5b024c230047ec346c294adee6b10190";
			String bestBlock = client.getBestBlock(blockHash).toString();
			logger.info("Best block header: " + bestBlock.toString());
			//Block block = MiningUtils.createBlockToMine(MainNetParams.get(), client);
			//block.setDifficultyTarget(0xffffff);
			//try {
			//	block.verifyHeader();		
			//} catch (VerificationException ve) {
			//	logger.info("VERIFICATION EXCEPTION !", ve);
			//}
			//Solver sol = new Solver(block, 1, 100);
			//header = sol.getHeaderString();
			result = client.submitBlock(bestBlock);
			logger.info(result.toString());
			assertEquals("null", result.get("error").toString());
			assertEquals("high-hash".toUpperCase(), result.get("result").toString().toUpperCase());

		} catch (Exception e) {
			logger.error(" Submit Failed ! :::", e);
			fail("Submit Block Failed " + e.getMessage());
		}

	}

}
