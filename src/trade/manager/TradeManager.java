package trade.manager;

import org.json.JSONObject;
import trade.coin.PARAM_KEY;

import java.util.*;

/**
 * Manage current order and completed transaction.
 */
public class TradeManager {

	private static TradeManager tradeManager = null;
	Map<String, TradeEntity> orderIDMap = new HashMap<>();
	List<String> tradeCompletedList = new ArrayList<>();

	private TradeManager(){}

	public static synchronized TradeManager getInstance(){
		if (tradeManager == null){
			tradeManager = new TradeManager();
		}
		return tradeManager;
	}

	public void addOrder(JSONObject orderJSON){
		String id = String.valueOf(orderJSON.getLong(PARAM_KEY.id.name()));
		TradeEntity entity = new TradeEntity(String.valueOf(orderJSON.getString(PARAM_KEY.rate.name())), orderJSON.getString(PARAM_KEY.amount.name()));
		orderIDMap.put(id, entity);
	}

	public void deleteOrder(String id){
		if (orderIDMap.remove(id) == null){
			System.out.println("Already order canceled. ID : " + id);
		}
	}

	public TradeEntity getOrder(String id){
		return orderIDMap.get(id);
	}

	public boolean isCompletedOrder(String id){
		if (orderIDMap.get(id) == null){
			return true;
		}
		return false;
	}

	public Map<String, TradeEntity> getAllOrder(){
		return orderIDMap;
	}

	/**
	 * This message must be the following format(csv)<br>
	 * Date, Order Id, Trade Id, Rate, Amount
	 * @param message
	 */
	public void completeTrade(String message){
		tradeCompletedList.add(message);
	}
	public List<String> getCompletedTradeList(){
		return tradeCompletedList;
	}


	public class TradeEntity{
		private double rate;
		private double amount;

		public TradeEntity(double rate, double amount){
			this.rate = rate;
			this.amount = amount;
		}

		public TradeEntity(String rate, String amount){
			this.rate = Double.valueOf(rate);
			this.amount = Double.valueOf(amount);
		}

		public void setAmount(double amount) {
			this.amount = amount;
		}

		public void setRate(double rate) {
			this.rate = rate;
		}

		public double getAmount() {
			return amount;
		}

		public double getRate() {
			return rate;
		}

		/**
		 * Reduce Settlement amount. This order is all executed return true.
		 * @param reduction
		 * @return
		 */
		public boolean execSettlement(double reduction){
			amount -= reduction;
			if (amount <= 0){
				return true;
			}
			return false;
		}
	}

}
