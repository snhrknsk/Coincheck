package trade.manager;

public class AccountInfo {

	private static AccountInfo accountInfo = null;

	private String accessKey = "";
	private String secretKey = "";
	private String outputFilePath = "/Users/shinohara/Documents/CoinCheckHistory";

	private AccountInfo() {
	}

	public static synchronized AccountInfo getInstance() {
		if (accountInfo == null) {
			accountInfo = new AccountInfo();
		}
		return accountInfo;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public void setOutputFilePath(String outputFilePath) {
		this.outputFilePath = outputFilePath;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public String getOutputFilePath() {
		return outputFilePath;
	}

}
