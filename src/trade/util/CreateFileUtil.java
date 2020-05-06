package trade.util;

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

	public static void createPriceHistoryCSV() {

		String fileName = "/PriceHistory" + "_" + LocalDateTime.now().toString();
		try (FileWriter f = new FileWriter(AccountInfo.getInstance().getOutputFilePath() +fileName, false)){
			try(PrintWriter p = new PrintWriter(new BufferedWriter(f))) {
				// set header
				String header = "Data,Price,UP";
				p.print(header);
				p.println();
				// set contents
				List<String> priceHistory = CoinManager.getInstance().getPriceHistory();
				double preVal = 0;
				for (String element : priceHistory) {
					p.print(element);
					String[] temp = element.split(",");
					if (temp.length > 1) {
						if (Double.valueOf(temp[1]) > preVal) {
							p.print(",↑");
						} else if (Double.valueOf(temp[1]) < preVal) {
							p.print(",↓");
						} else {
							p.print(",=");
						}
						preVal = Double.valueOf(temp[1]);
					}
					p.println();
				}
			}
			System.out.println("End Output Price History CSV File");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void createTradeHistory(){

		String fileName = "/TradeHistory" + "_" + LocalDateTime.now().toString();
		try (FileWriter f = new FileWriter(AccountInfo.getInstance().getOutputFilePath() +fileName, false)){
			try(PrintWriter p = new PrintWriter(new BufferedWriter(f))) {
				// set header
				String header = "Date,Order Id,Trade Id,Rate,Amount";
				p.print(header);
				p.println();
				// set contents
				List<String> priceHistory = TradeManager.getInstance().getCompletedTradeList();
				for (String element : priceHistory) {
					p.print(element);
					p.println();
				}
			}
			System.out.println("End Output Price History CSV File");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void createOpenOrder(){
		String result = CoinCheckClient.postOpenOrder();
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
			System.out.println("End Output Rest Order CSV File");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
