package ztek.bitcoin.miner.btcminer;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.MainNetParams;

public class CoinBaseTX {
	/**
	 * 01000000 - version 01 - input count
	 * 0000000000000000000000000000000000000000000000000000000000000000 - prev tx
	 * ffffffff - prev out n 53 - length of coinbase script
	 * 038349040d00456c69676975730052d8f72ffabe6d6dd991088decd13e658bbecc0b2b4c87306f637828917838c02a5d95d0e1bdff9b0400000000000000002f73733331312f00906b570400000000e4050000
	 * - coinbase script ffffffff - sequence 01 - output count bf20879500000000 -
	 * 2508660927, satoshi count, or 25.08660927 BTC 19 - script length
	 * 76a9145399c3093d31e4b0af4be1215d59b857b861ad5d88ac - script 00000000 -
	 * locktime
	 */
	private String version = "01000000";
	private String input = "01";
	private String prevHash = "0000000000000000000000000000000000000000000000000000000000000000";
	private String prevOut = "ffffffff";
	private String csLenght = "53";
	private String cscript = "038349040d00456c69676975730052d8f72ffabe6d6dd991088decd13e658bbecc0b2b4c87306f637828917838c02a5d95d0e1bdff9b0400000000000000002f73733331312f00906b570400000000e4050000";
	private String sequence = "ffffffff";
	private String outCount = "01";
	private String value = "14A71B6600000000";
	private String sLenght = "19";
	private String script = "76a9145399c3093d31e4b0af4be1215d59b857b861ad5d88ac";
	private String time = "0" + Long.toHexString(System.currentTimeMillis());

	public Transaction buildTx() {
		Transaction t = new Transaction(MainNetParams.get(), Utils.parseAsHexOrBase58(this.getHex().toLowerCase()));
		return t;
	}

	@Override
	public String toString() {
		return "CoinBaseTX [version=" + version + ", input=" + input + ", prevHash=" + prevHash + ", prevOut=" + prevOut + ", csLenght=" + csLenght
				+ ", cscript=" + cscript + ", sequence=" + sequence + ", outCount=" + outCount + ", value=" + value + ", sLenght=" + sLenght + ", script="
				+ script + ", time=" + time + "]";
	}

	public String getHex() {
		return (version + input + prevHash + prevOut + csLenght + cscript + sequence + outCount + value + sLenght + script + time).toLowerCase();
	}

	public String getCscript() {
		return cscript;
	}

	public void setCscript(String cscript) {
		this.cscript = cscript;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

}
