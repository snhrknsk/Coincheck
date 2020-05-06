package trade.exec;

import org.json.JSONObject;
import trade.coin.CoinCheckClient;
import trade.coin.PARAM_KEY;
import trade.manager.CoinManager;
import trade.manager.TradeManager;

import java.math.BigDecimal;

/**
 * Loop the following entry and sell<br>
 * Entry and Sell the order if up UP_SELL%
 */
public class TradeSimple implements  ITradeLogic{

	private final double INITIAL_FUND = 8000;
	private double currentFund = 0;

	private boolean isLastTradeBuy = true;
	private final double SELL = 0.2f;
	private final double BUY = 0.2f;

	private double initialTradePrice = 0;
	private double lastTradePrice = 0;
	private double lastTradeAmount = 0;

	private String prevOrderId = "0";

	@Override
	public void exec() {

		double current = CoinManager.getInstance().getCurrentRate();
		if (lastTradePrice == 0){
			int orderRate = (int)(current * ((100 - BUY) / 100));
			double amount = INITIAL_FUND / orderRate;
			String result = CoinCheckClient.postBuyRequest(String.valueOf(orderRate), String.valueOf(amount));
			postTrade(result);
			initialTradePrice = orderRate;
			return;
		}
		if (checkPreviousTrade()){
			if (isLastTradeBuy){
				sell(current);
			} else {
				buy(current);
			}
		}
	}

	private void sell(double current){

		int sellRate = (int)current;
		if (current > lastTradePrice * ((100 + SELL) / 100)){
			sellRate = (int)(current * ((100 + SELL) / 100));
		} else {
			sellRate = (int)(lastTradePrice * ((100 + SELL) / 100));
		}
		double sellAmount = lastTradeAmount;
		String result = CoinCheckClient.postSellRequest(String.valueOf(sellRate), String.valueOf(sellAmount));
		postTrade(result);
	}

	private void buy(double current){

		int buyRate = (int)initialTradePrice;
		if (current < initialTradePrice){
			buyRate = (int)(current * ((100 - BUY) / 100));
		}
		currentFund = lastTradeAmount * lastTradePrice;
//		double buyAmount = (currentFund) / buyRate;
		BigDecimal buyAmount = new BigDecimal((currentFund) / buyRate).setScale(5);
		String result = CoinCheckClient.postBuyRequest(String.valueOf(buyRate), buyAmount.toString());
		postTrade(result);
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
		if (!resultJSON.getBoolean(PARAM_KEY.success.name())){
			System.out.println(result);
			return;
		}
		String id = String.valueOf(resultJSON.getLong(PARAM_KEY.id.name()));
		String rate = resultJSON.getString(PARAM_KEY.rate.name());
		String date = resultJSON.getString(PARAM_KEY.created_at.name());
		String amount = resultJSON.getString(PARAM_KEY.amount.name());
		String orderType = resultJSON.getString(PARAM_KEY.order_type.name());
		if("buy".equals(orderType)){
			isLastTradeBuy = true;
		} else if ("sell".equals(orderType)) {
			isLastTradeBuy = false;
		} else {
			System.out.println("Order is not correct. Order type is not buy or sell");
			return;
		}

		TradeManager.getInstance().addOrder(resultJSON);
		prevOrderId = id;
		lastTradePrice = Double.valueOf(rate);
		lastTradeAmount = Double.valueOf(amount);
		System.out.println(" Exec Post Order ID : " + id + " RATE : " + rate + " Amount" + amount + " DATE : " + date);
	}

}
