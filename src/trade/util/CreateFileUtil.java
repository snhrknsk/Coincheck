package trade.util;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import trade.coin.CoinCheckClient;
import trade.coin.PARAM_KEY;
import trade.manager.AccountInfo;
import trade.manager.CoinManager;
import trade.manager.TradeManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;

public class CreateFileUtil {

	private static final Logger log = Logger.getLogger(CreateFileUtil.class);
	public static void createPriceHistoryCSV() {

		String fileName = "/PriceHistory" + "_" + LocalDateTime.now().toString();
		try (FileWriter f = new FileWriter(AccountInfo.getInstance().getOutputFilePath() + fileName, false)){
			try(PrintWriter p = new PrintWriter(new BufferedWriter(f))) {
				// set header
				String header = "Data,Price,UP";
				p.print(header);
				p.println();
				// set contents
				List<CoinManager.PriceEntity> priceHistory = CoinManager.getInstance().getPriceHistory();
				double preVal = 0;
				for (CoinManager.PriceEntity element : priceHistory) {
					p.print(element.date + ",");
					p.print(element.rate);
					if (Double.parseDouble(element.rate) > preVal) {
						p.print(",↑");
					} else if (Double.parseDouble(element.rate) < preVal) {
						p.print(",↓");
					} else {
						p.print(",=");
					}
					preVal = Double.parseDouble(element.rate);
					p.println();
				}
			}
			log.info("End Output Price History CSV File");
		} catch (IOException e) {
			log.error(e);
		}
	}

	public static void createTradeHistory(){

		String fileName = "/TradeHistory" + "_" + LocalDateTime.now().toString();
		try (FileWriter f = new FileWriter(AccountInfo.getInstance().getOutputFilePath() +fileName, false)){
			try(PrintWriter p = new PrintWriter(new BufferedWriter(f))) {
				// set header
				String header = "Date,Order Id,Trade Id,Buy or Sell,Rate,Amount";
				p.print(header);
				p.println();
				// set contents
				List<TradeManager.TradedOrderEntity> priceHistory = TradeManager.getInstance().getCompletedTradeList();
				for (TradeManager.TradedOrderEntity element : priceHistory) {
					String buy = element.isBuyOrder() ? "買" : "売";
					String result = String.format("%s,%s,%s,%s,%s.%s",
							element.getDate(), element.getOrderId(), element.getTradeId(), buy, element.getRate(), element.getAmount());
					p.print(result);
					p.println();
				}
			}
			log.info("End Output Price History CSV File");
		} catch (IOException e) {
			log.error(e);
		}
	}

	public static void createOpenOrder(){
		String result = CoinCheckClient.getOpenOrder();
		if (result.isEmpty()){
			System.out.println("All order is settled. No rest order.");
			return;
		}
		JSONObject resultJSON = new JSONObject(result);
		JSONArray resultArray = resultJSON.getJSONArray(PARAM_KEY.orders.name());

		String fileName = "/RestOrder" + "_" + LocalDateTime.now().toString();
		try (FileWriter f = new FileWriter(AccountInfo.getInstance().getOutputFilePath() +fileName, false)){
			try(PrintWriter p = new PrintWriter(new BufferedWriter(f))) {
				// set header
				String header = "Date,Id,Order Type,Rate,Amount";
				p.print(header);
				p.println();
				// set contents
				for (int i = 0; i < resultArray.length(); i++) {
					JSONObject target = resultArray.getJSONObject(i);
					String element = String.format("%s,%s,%s,%s,%s"
							, target.getString(PARAM_KEY.created_at.name()), target.getLong(PARAM_KEY.id.name()), target.getString(PARAM_KEY.order_type.name())
							,target.getString(PARAM_KEY.rate.name()),target.getString(PARAM_KEY.pending_amount.name()));
					p.print(element);
					p.println();
				}
			}
			log.info("End Output Rest Order CSV File");
		} catch (IOException e) {
			log.error(e);
		}
	}
}
