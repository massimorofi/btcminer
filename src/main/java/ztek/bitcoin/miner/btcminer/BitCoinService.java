/**
 * 
 */
package ztek.bitcoin.miner.btcminer;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author massimo
 *
 */
public class BitCoinService extends Thread {
	static Logger logger = LoggerFactory.getLogger(BitCoinService.class);
	private String lastHash = "";
	private LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();
	private BtcRpcClient client = new BtcRpcClient();

	public LinkedBlockingQueue<String> getQueue() {
		return queue;
	}

	public void setQueue(LinkedBlockingQueue<String> queue) {
		this.queue = queue;
	}

	@Override
	public void run() {
		while (true) {
			try {

				String currentHash = this.client.getBestBlockHash();
				logger.debug("Current bestBlockHash" + currentHash + " Last Block Hash=" + lastHash);
				if (!lastHash.equalsIgnoreCase(currentHash)) {
					lastHash = currentHash;
					queue.add(lastHash);
				}
				Thread.sleep(10000);

			} catch (Exception e) {
				logger.error("[CANNOT EXECUTE BtcRpcClient.getBestBlockHash()]", e);
			}
		}
	}

	/**
	 * @return the client
	 */
	public BtcRpcClient getClient() {
		return client;
	}

	/**
	 * @param client
	 *            the client to set
	 */
	public void setClient(BtcRpcClient client) {
		this.client = client;
	}

}
