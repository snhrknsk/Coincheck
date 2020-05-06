package trade.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Singleton class<br>
 * Manage the coin price.
 */
public class CoinManager {

	private static CoinManager coinManager = null;

	private double currentRate = 0;
	private List<String> rateList = new ArrayList<>();

	private CoinManager(){
	}

	public static synchronized CoinManager getInstance(){
		if (coinManager == null){
			coinManager = new CoinManager();
		}
		return coinManager;
	}

	public void setCurrentRate(String currentRate) {
		this.currentRate = Double.valueOf(currentRate);
		rateList.add(new Date(System.currentTimeMillis()) + "," + String.valueOf(currentRate));
	}

	public double getCurrentRate() {
		return currentRate;
	}

	public List<String> getPriceHistory(){
		return rateList;
	}
}
