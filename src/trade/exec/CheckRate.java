package trade.exec;

import org.json.JSONObject;
import trade.coin.CoinCheckClient;
import trade.manager.CoinManager;

/**
 * Check current price.<br>
 * Save latest price in {@link CoinManager}.
 */
public class CheckRate implements ITradeLogic{
	@Override
	public void exec() {
		String result = CoinCheckClient.getCurrentPrice();
		JSONObject jsonObject = new JSONObject(result);
		String rate = jsonObject.getString("rate");
		CoinManager.getInstance().setCurrentRate(rate);
	}
}
