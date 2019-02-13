package fchen;

/**
 * It encapsulates the data returned by the cloud.
 * Because of the privacy concern, this data structure is modified in this case.
 * Now the query file name is a series MACs of all possible file names. 
 * @author Chen, Fei
 */
public class ProofData {

	private int    result        = 0;
	private String[] filename    = null;
	private String[] mac         = null;

	public ProofData(int result, String[] filename, String[] mac) {
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

	public String[] getFilename() {
		return filename;
	}

	public void setFilename(String[] filename) {
		this.filename = filename;
	}

	public String[] getMac() {
		return mac;
	}

	public void setMac(String[] mac) {
		this.mac = mac;
	}
}
