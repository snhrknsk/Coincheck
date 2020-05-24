package trade.exec;

import org.json.JSONObject;
import trade.coin.CoinCheckClient;
import trade.coin.PARAM_KEY;
import trade.manager.TradeManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface ITradeLogic {

	/**
	 * Logic name
	 */
	default public String getLogicName(){
		return this.getClass().getName();
	}
	/**
	 * Your trade logic<br>
	 * Call this method from Task Worker at constants interval
	 */
	public void exec();

	/**
	 * Argument "Params" is the defined parameters list(Defined Order in each logic).
	 * If restart this logic, call this method.
	 */
	default public boolean setParams(Map<String, String> params){
		return true;
	};

	/**
	 * Get current settings.
	 */
	default public Map<String, String> getParams(){
		return new HashMap<>();
	}

	/**
	 * Post process to stop task.
	 */
	default public boolean stopTask(){
		return true;
	}

	/**
	 * Cancel the current order to stop or restart the execution.
	 */
	default boolean resetTrade(String orderId){
		String result =CoinCheckClient.postOrderCancel(orderId);
		JSONObject resultObject = new JSONObject(result);
		if (!resultObject.getBoolean(PARAM_KEY.success.name())){
			System.out.println(result);
			return false;
		}
		TradeManager.getInstance().deleteOrder(orderId);
		System.out.println("The order is canceled. ID = " + resultObject.getString(PARAM_KEY.id.name()));
		return true;
	}

}
