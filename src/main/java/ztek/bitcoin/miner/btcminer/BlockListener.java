package ztek.bitcoin.miner.btcminer;

import java.util.concurrent.LinkedBlockingQueue;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.VerificationException;
import org.bitcoinj.core.listeners.NewBestBlockListener;

public class BlockListener implements NewBestBlockListener {
	private LinkedBlockingQueue<Block> queue = new LinkedBlockingQueue<Block>();

	@Override
	public void notifyNewBestBlock(StoredBlock block) throws VerificationException {
		queue.add(block.getHeader());

	}

	/**
	 * @return the queue
	 */
	public LinkedBlockingQueue<Block> getQueue() {
		return queue;
	}

	/**
	 * @param queue
	 *            the queue to set
	 */
	public void setQueue(LinkedBlockingQueue<Block> queue) {
		this.queue = queue;
	}

}
