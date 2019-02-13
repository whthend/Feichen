package fchen;

/**
 * It encapsulates the data returned by the cloud.
 * @author Chen, Fei
 */
public class ProofData {

	private int result = 0;
	private String filename = null;
	private byte[] mac = null;

	public ProofData(int result, String filename, byte[] mac) {
		super();
		this.result = result;
		this.filename = filename;
		this.mac = mac;
	}

	public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public byte[] getMac() {
		return mac;
	}

	public void setMac(byte[] mac) {
		this.mac = mac;
	}
}
