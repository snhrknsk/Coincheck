package trade.exec;

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

	private Set<Long> orderSet = new HashSet<>();
	@Override
	public void exec() {

		String result = CoinCheckClient.getTradeHistory();
		JSONObject histories = new JSONObject(result);
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
						System.out.println("All order is settled. ID : " + id);
						TradeManager.getInstance().deleteOrder(id);
					} else {
						System.out.println("A part of order is remained. Order ID = " + id + " Remain : " + entity.getAmount());
					}
					TradeManager.TradedOrderEntity tradedEntity = new TradeManager.TradedOrderEntity.Builder(entity.getRate(), entity.getAmount(), entity.isBuyOrder())
							.date(target.getString(PARAM_KEY.created_at.name())).orderId(String.valueOf(target.getLong(PARAM_KEY.id.name()))).tradeId(id).build();
					TradeManager.getInstance().completeTrade(tradedEntity);
					orderSet.add(target.getLong(PARAM_KEY.id.name()));
				}
			}
		}
	}
}
