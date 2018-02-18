/**
 * 
 */
package ztek.bitcoin.miner.btcminer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.UnsafeByteArrayOutputStream;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.VerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author massimo
 *
 */
public class Solver extends Thread {
	static Logger logger = LoggerFactory.getLogger(Solver.class);
	private Block block;
	private AtomicBoolean running = new AtomicBoolean(true);
	private AtomicLong nonce = new AtomicLong(0);
	private BtcRpcClient client = new BtcRpcClient();
	private long nonceRange = 0;

	/**
	 * @param block
	 *            to solve
	 * @param startN
	 *            starting nonce
	 * 
	 */
	public Solver(Block block, long startNonce, long nonceRange) {
		super();
		this.block = block.cloneAsHeader();
		this.nonce.set(startNonce);
		this.nonceRange = nonceRange;
		this.block.setNonce(this.nonce.get());
		logger.info("Start mining with nonce " + nonce.get());
	}

	/**
	 * run the solver
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		long time = System.currentTimeMillis();
		long initialNonce = nonce.get();
		long nonceLimit = initialNonce + nonceRange;
		try {
			String header = getHeaderString();
			logger.info("Initial Block Header HEX [" + header + "]");
		} catch (IOException e1) {
			logger.error("Solver ERROR!! -- ", e1);
		}
		while (running.get()) {
			try {
				// Is our proof of work valid yet?
				if (checkProofOfWork(block, false)) {
					logger.info("Nonce found ! " + block.toString());
					String blockHeader = getHeaderString();
					this.client.submitBlock(blockHeader);
					running.set(false);
					return;
				}
				// No, so increment the nonce and try again.
				long currNonce = nonce.incrementAndGet();
				block.setNonce(currNonce);
				if ((currNonce % 10000000) == 0) {
					long velocity = (currNonce - initialNonce) / ((System.currentTimeMillis() - time) / 1000);
					long expected= nonceRange/velocity;
					logger.info("Thread-[" + this.getName() + "] current velocity " + velocity + " hashes per second " + "current nonce = " + currNonce + " %"
							+ (100 * (currNonce - initialNonce) / nonceRange)+" expected completion in sec "+expected);
				}
				if (currNonce > nonceLimit) {
					logger.info("Nonce not found...final nonce = " + currNonce);
					return;
				}
			} catch (Exception e) {
				logger.error("Solver ERROR!! -- ", e);
				return;
			}
		}
	}

	/**
	 * Returns true if the hash of the block is OK (lower than difficulty target).
	 */
	protected boolean checkProofOfWork(Block b, boolean throwException) throws VerificationException {
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

	// default for testing
	public String getHeaderString() throws IOException {
		ByteArrayOutputStream stream = new UnsafeByteArrayOutputStream(Block.HEADER_SIZE);
		// fall back to manual write
		Utils.uint32ToByteStreamLE(this.block.getVersion(), stream);
		stream.write(this.block.getPrevBlockHash().getReversedBytes());
		stream.write(this.block.getMerkleRoot().getReversedBytes());
		Utils.uint32ToByteStreamLE(this.block.getTime().getTime(), stream);
		Utils.uint32ToByteStreamLE(this.block.getDifficultyTarget(), stream);
		Utils.uint32ToByteStreamLE(this.block.getNonce(), stream);
		return Utils.HEX.encode(stream.toByteArray());
	}

	/**
	 * @return the block
	 */
	public Block getBlock() {
		return block;
	}

	/**
	 * @param block
	 *            the block to set
	 */
	public void setBlock(Block block) {
		this.block = block;
	}

	/**
	 * @return the running
	 */
	public boolean getRunning() {
		return running.get();
	}

	/**
	 * @param running
	 *            the running to set
	 */
	public void setRunning(boolean running) {
		this.running.set(running);
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
