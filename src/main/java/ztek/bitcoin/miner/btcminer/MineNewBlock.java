/*
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ztek.bitcoin.miner.btcminer;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.BaseEncoding;

/**
 * Try to mine a block each time the getBlockTemplate returns a new mining
 * proposal. The BitCoinService query every 10 sec Bitcoind. The main loop run
 * solver threads (number of cores -1) each time a new block template is
 * received (kill the running ones first)
 */
public class MineNewBlock {
	static Logger logger = LoggerFactory.getLogger(MineNewBlock.class);
	public static final BaseEncoding HEX = BaseEncoding.base16().lowerCase();
	static ArrayList<Solver> solvers = new ArrayList<Solver>();
	static int workerNumber = 0;
	static int workersTotal = 0;
	static long MAX_NONCE = (BigInteger.valueOf(2).pow(32)).longValue();
	static NetworkParameters netParams = MainNetParams.get();

	public static void main(String[] args) throws Exception {

		if (args.length > 1) {
			workerNumber = Integer.parseInt(args[0]);
			workersTotal = Integer.parseInt(args[1]);
		} else {
			logger.info(
					"Params must be 2 <worker number> <total workers> \n A worker is a separate node/server, then internally each server run parallel solver therads.\n those numbers are used to divide the nonce range in multple parts to work in parallel ");
		}
		if (args.length > 2) {
			if (args[2].equalsIgnoreCase("TEST")) {
				netParams = TestNet3Params.get();
			}
		}
		logger.info(" MAX Nonce = " + MAX_NONCE);
		// run the service to get the blocktemplate
		BtcRpcClient client = new BtcRpcClient();
		// properties
		ClassLoader classLoader = MineNewBlock.class.getClassLoader();
		InputStream file = classLoader.getResourceAsStream("config.properties");
		Properties properties = new Properties();
		// load a properties file
		properties.load(file);
		client.setHost(properties.getProperty("host"));
		logger.info("Setting host for RPC " + client.getHost());
		BitCoinService service = new BitCoinService();
		service.setClient(client);
		service.start();
		BlockingQueue<String> queue = service.getQueue();
		while (true) {
			logger.info(" Waiting for new block ...");
			String lastHash = queue.take();
			logger.info("New block...best Block hash: " + lastHash);
			String bestBlock = client.getBestBlock(lastHash).toString();
			logger.info("Best block header: " + bestBlock.toString());
			Block block = MiningUtils.createBlockToMine(netParams,client);
			int cores = Runtime.getRuntime().availableProcessors();
			logger.info("Number of cores " + cores);
			runSolvers(client, block, cores - 1);
		}

		// peerGroup.stopAsync();
	}

	/**
	 * Starts solver threads for the
	 * 
	 * @param block
	 * @param numberOfSolvers
	 * @param noncerange
	 */
	public static void runSolvers(BtcRpcClient client, Block block, long numberOfSolvers) {
		logger.info("Removing solvers ....");
		long nonceRange = MAX_NONCE / numberOfSolvers;
		long startNonce = 0;
		if (workersTotal > 0) {
			nonceRange = MAX_NONCE / workersTotal;
			startNonce = workerNumber * nonceRange;
			nonceRange = nonceRange / numberOfSolvers;
			logger.info(" Total Workers=" + workersTotal + " Worker number=" + workerNumber + " || nonceRange=" + nonceRange + "  startNonce=" + startNonce);
		}
		if (!solvers.isEmpty()) {
			for (int i = 0; i < numberOfSolvers; i++) {
				solvers.get(i).setRunning(false);
			}
		}
		// close all solvers thread
		solvers.clear();
		logger.info("Creating new solvers .... for block " + block.toString());
		for (int i = 0; i < numberOfSolvers; i++) {
			Solver solver = new Solver(block, (startNonce + i * nonceRange), nonceRange);
			solver.setName("Solver-" + i);
			solver.setClient(client);
			solvers.add(solver);
			solver.start();
		}
	}


}
