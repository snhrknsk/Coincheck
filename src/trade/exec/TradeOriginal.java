package trade.exec;

import org.json.JSONObject;
import trade.coin.CoinCheckClient;
import trade.coin.PARAM_KEY;
import trade.manager.CoinManager;
import trade.manager.TradeManager;

/**
 * Loop the following entry and sell<br>
 * Entry<br>
 * -> UP UP_SELL%     -> sell (Profitability) -> UP PROFIT_UP_BUY%     -> Entry<br>
 *                                            -> DOWN PROFIT_DOWN_BUY% -> Entry<br>
 * -> DOWN DOWN_SELL% -> sell (loss cut)      -> UP LOSS_UP_BUY%       -> Entry<br>
 *                                            -> DOWN LOSS_DOWN_BUY%   -> Entry
 */
public class TradeOriginal implements ITradeLogic{

	private final double INITIAL_FUND = 8000;

	private boolean isLastTradeUP = true;
	private boolean isLastTradeBuy = true;
	private final double UP_SELL = 1;
	private final double DOWN_SELL = 0.4f;
	private final double PROFIT_UP_BUY = 0.2f;
	private final double PROFIT_DOWN_BUY = 0.4f;
	private final double LOSS_UP_BUY = 0.2f;
	private final double LOSS_DOWN_BUY = 1;
	private final double SEPARATE_RATE = 0.05f;

	private double lastTradePrice = 0;
	private double lastTradeAmount = 0;
	private double nextBuyPrice = 0;
	private double nextSellPrice = 0;

	private String prevOrderId = "0";

	@Override
	public void exec() {

		double current = CoinManager.getInstance().getCurrentRate();
		if (lastTradePrice == 0){
			double orderRate = current * ((100 - PROFIT_UP_BUY) / 100);
			double amount = INITIAL_FUND / orderRate;
			String result = CoinCheckClient.postBuyRequest(String.valueOf(orderRate), String.valueOf(amount));
			postTrade(result);
			return;
		}
		if (checkPreviousTrade()){
			if (isLastTradeBuy){
				sell();
			} else {
				buy();
			}
		}
	}

	private void sell(){


	}

	private void buy(){

	}

	private boolean checkPreviousTrade(){
		return TradeManager.getInstance().isCompletedOrder(prevOrderId);
	}

	private void postTrade(String result){
		if (result.isEmpty()){
			System.out.println("TradeSimple.class Fail to post trade request.");
			return;
		}
		JSONObject resultJSON = new JSONObject(result);
		String id = String.valueOf(resultJSON.getLong(PARAM_KEY.id.name()));
		String rate = resultJSON.getString(PARAM_KEY.rate.name());
		String date = resultJSON.getString(PARAM_KEY.created_at.name());
		String amount = resultJSON.getString(PARAM_KEY.amount.name());
		if(resultJSON.getString(PARAM_KEY.order_type.name()).equals("buy")){
			isLastTradeBuy = true;
		} else {
			isLastTradeBuy = false;
		}
		TradeManager.getInstance().addOrder(resultJSON, toString());
		prevOrderId = id;
		lastTradePrice = Double.valueOf(rate);
		lastTradeAmount = Double.valueOf(amount);

		System.out.println(" Exec Post Order ID : " + id + " RATE : " + rate + " DATE : " + date);
	}
}
