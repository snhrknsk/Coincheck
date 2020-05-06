package trade.exception;

public class TradeException extends Exception {
	private String message="";

	public TradeException(Exception e, String message){
		super(e);
		this.message = message;
	}

	public TradeException(String message){
		super(message);
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}
}
