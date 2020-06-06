package trade.exec;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import trade.coin.CoinCheckClient;
import trade.coin.PARAM_KEY;
import trade.manager.CoinManager;
import trade.manager.TradeManager;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Loop the following entry and sell<br>
 * Entry and Sell the order if up UP_SELL%
 */
public class TradeSimple implements  ITradeLogic{

	private static long TASK_ID = 0;
	private Logger log = Logger.getLogger(this.getClass().getName() + "#" + TASK_ID);

	private long taskID = 0;
	private double INITIAL_FUND = 8000;
	private double currentFund = INITIAL_FUND;

	private boolean isLastTradeBuy = true;
	private double SELL = 0.3f;
	private double BUY = 0.3f;
	private double FIRST_BUY = 0.05f;

	private double initialTradePrice = 0;
	private double lastTradePrice = 0;
	private double lastTradeAmount = 0;
	private String prevOrderId = "0";

	public TradeSimple(){
		taskID = TASK_ID++;
	}

	@Override
	public void exec() {

		double current = CoinManager.getInstance().getCurrentRate();
		if (lastTradePrice == 0){
			int orderRate = (int)(current * ((100 - FIRST_BUY) / 100));
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

	/**
	 * Set params as the following map key.
	 * INITIAL_FUND, FIRST_BUY, SELL, BUY
	 * @param params
	 * @return
	 */
	@Override
	public synchronized boolean setParams(Map<String, String> params) {

		log.info("Set params to TradeSimple logic : " + params);
		try {
			INITIAL_FUND = Double.valueOf(params.get("INITIAL_FUND"));
			FIRST_BUY = Double.valueOf(params.get("FIRST_BUY"));
			SELL = Double.valueOf(params.get("SELL"));
			BUY = Double.valueOf(params.get("BUY"));
			isLastTradeBuy = true;
			initialTradePrice = 0;
			lastTradePrice = 0;
			lastTradeAmount = 0;
			if (!prevOrderId.equals("0")) {
				resetTrade(prevOrderId);
			}
		} catch (NumberFormatException e){
			log.error(e);
			return false;
		}
		return true;
	}

	@Override
	public Map<String, String> getParams(){
		Map<String, String> param = new LinkedHashMap<>();
		param.put("INITIAL_FUND", String.valueOf(INITIAL_FUND));
		param.put("FIRST_BUY", String.valueOf(FIRST_BUY));
		param.put("SELL", String.valueOf(SELL));
		param.put("BUY", String.valueOf(BUY));
		param.put("CURRENT_FUNDS", String.valueOf(currentFund));
		return param;
	}

	@Override
	public boolean stopTask() {
		if (!prevOrderId.equals("0") && TradeManager.getInstance().getOrder(prevOrderId) != null) {
			resetTrade(prevOrderId);
		} else {
			log.warn("There is no active order. Previous order ID is " + prevOrderId);
		}
		return true;
	}

	@Override
	public boolean resetTrade(String orderId){
		if (!isLastTradeBuy){
			String result =CoinCheckClient.postOrderCancel(orderId);
			JSONObject resultObject = new JSONObject(result);
			if (!resultObject.getBoolean(PARAM_KEY.success.name())){
				log.warn(result);
				return false;
			}
			TradeManager.getInstance().deleteOrder(orderId);
			log.info("The order is canceled. ID = " + resultObject.getLong(PARAM_KEY.id.name()));
			return true;
		}
		log.warn("Last order is BUY. So, current sell order is not cancel");
		return true;
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
		BigDecimal buyAmount = new BigDecimal((currentFund) / buyRate).setScale(5, BigDecimal.ROUND_HALF_UP);
		String result = CoinCheckClient.postBuyRequest(String.valueOf(buyRate), buyAmount.toString());
		postTrade(result);
	}

	private boolean checkPreviousTrade(){
		return TradeManager.getInstance().isCompletedOrder(prevOrderId);
	}

	private synchronized void postTrade(String result){
		if (result.isEmpty()){
			log.warn("Fail to post trade request.");
			return;
		}
		JSONObject resultJSON = new JSONObject(result);
		if (!resultJSON.getBoolean(PARAM_KEY.success.name())){
			log.warn(result);
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
			log.error("Order is not correct. Order type is not buy or sell");
			return;
		}
		TradeManager.getInstance().addOrder(resultJSON, toString());
		prevOrderId = id;
		lastTradePrice = Double.valueOf(rate);
		lastTradeAmount = Double.valueOf(amount);
		log.info(" Exec Post Order ID : " + id + " Order Type : " + orderType + " RATE : " + rate + " Amount" + amount + " DATE : " + date);
	}

	@Override
	public String toString(){
		return getLogicName() + "#" + taskID;
	}

}
