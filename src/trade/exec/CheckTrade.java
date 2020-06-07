package trade.exec;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import trade.coin.CoinCheckClient;
import trade.coin.PARAM_KEY;
import trade.manager.TradeManager;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Check trade info.
 * Order is settled in multiple time.
 * Manage whether all order is settled or not by order_id and id in response.
 */
public class CheckTrade implements ITradeLogic{

	private Logger log = Logger.getLogger(this.getClass());
	private Set<Long> orderSet = new HashSet<>();
	@Override
	public void exec() {

		//order history to check if order is done
		String result = CoinCheckClient.getTradeHistory();
		JSONObject histories = new JSONObject(result);
		//open order to check if open order exists
		String openOrderResult = CoinCheckClient.getOpenOrder();
		JSONObject openOrders = new JSONObject(openOrderResult);
		Map<String, TradeManager.TradeEntity> orderMap = TradeManager.getInstance().getAllOrder();
		for (Map.Entry<String, TradeManager.TradeEntity> entry: orderMap.entrySet()) {
			String id = entry.getKey();
			JSONArray tradeHistory = histories.getJSONArray(PARAM_KEY.data.name());
			for (int i = 0; i < tradeHistory.length(); i++) {
				JSONObject target = tradeHistory.getJSONObject(i);
				if (String.valueOf(target.getLong(PARAM_KEY.order_id.name())).equals(id) && !orderSet.contains(target.getLong(PARAM_KEY.id.name()))){
					// order amount - settlement amount. All order amount is settled, its order is deleted from TradeManager
					TradeManager.TradeEntity entity = entry.getValue();
					JSONObject funds = target.getJSONObject(PARAM_KEY.funds.name());
					double settlement = Double.valueOf(funds.getString(PARAM_KEY.btc.name()));
					if (settlement < 0) {
						settlement *= -1;
					}
					if (entity.execSettlement(settlement)){
						log.info("All order is settled. ID : " + id);
						TradeManager.getInstance().deleteOrder(id);
					} else {
						//double check if settle is not 0, but open order doesn't exist
						//sometimes a part of order is remained in spite of order completed.
						if (!checkOpenOrder(id, openOrders)){
							log.info("All order is settled. ID : " + id);
							log.warn("Order is completed, but remain settle amount in system. ID : " + id + " amount :" + entity.getAmount());
							TradeManager.getInstance().deleteOrder(id);
						} else {
							log.info("A part of order is remained. Order ID = " + id + " Remain : " + entity.getAmount());
						}
					}
					TradeManager.TradedOrderEntity tradedEntity = new TradeManager.TradedOrderEntity.Builder(entity.getRate(), entity.getOrderAmount(), entity.isBuyOrder())
							.date(target.getString(PARAM_KEY.created_at.name())).orderId(String.valueOf(target.getLong(PARAM_KEY.id.name()))).tradeId(id).logic(entry.getValue().getLogic()).build();
					TradeManager.getInstance().completeTrade(tradedEntity);
					orderSet.add(target.getLong(PARAM_KEY.id.name()));
				}
			}
		}
	}

	/**
	 * If specified id's order doesn't exist, return false.<br>
	 * If specified id's order was completed, return true
	 * @return
	 */
	private boolean checkOpenOrder(String targetId, JSONObject opens) {
		if (!opens.getBoolean(PARAM_KEY.success.name())){
			log.error("Get open order request fails.");
			return true;
		}
		JSONArray openArray = opens.getJSONArray(PARAM_KEY.orders.name());
		for (int i = 0; i < openArray.length(); i++){
			JSONObject openOrder = openArray.getJSONObject(i);
			if (targetId.equals(openOrder.getLong(PARAM_KEY.id.name()))){
				return true;
			}
		}
		return false;
	}
}
