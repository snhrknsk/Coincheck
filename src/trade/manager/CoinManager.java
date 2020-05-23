package trade.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Singleton class<br>
 * Manage the coin price.
 */
public class CoinManager {

	private static CoinManager coinManager = null;

	private double currentRate = 0;
	private List<PriceEntity> rateList = new LinkedList<>();

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
		PriceEntity entity = new PriceEntity();
		entity.date = new Date(System.currentTimeMillis()).toString();
		entity.rate = String.valueOf(currentRate);
		rateList.add(entity);
	}

	public double getCurrentRate() {
		return currentRate;
	}

	public List<PriceEntity> getPriceHistory(){
		return rateList;
	}

	public static class PriceEntity{
		public String date = "";
		public String rate = "";
	}

}
