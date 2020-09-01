package trade.exec;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import trade.coin.CoinCheckClient;
import trade.coin.PARAM_KEY;
import trade.manager.CoinManager;
import trade.manager.TradeManager;
import trade.util.Util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * https://jp.investing.com/crypto/bitcoin/btc-jpy
 * のテクニカルサマリを参照
 * 複数インスタンス生成禁止
 */
public class TradeTechIndex implements  ITradeLogic{

	private static long TASK_ID = 0;
	private Logger log = Logger.getLogger(this.getClass().getName() + "#" + TASK_ID);
	private static TradeTechIndex instance = null;

	private TradeTechIndex(){
	}
	public static synchronized TradeTechIndex getInstance(){
		if (instance == null){
			instance = new TradeTechIndex();
		}
		return instance;
	}

	private enum SIGNAL{
		s_buy,buy,s_sell,sell,neutral
	}
	private long taskID = 0;
	/* Excecメソッド何回ごとに実行するかを決定
	 * HTML読み込みが重いので１回だけ実行する
	 * 実行するかどうかをuseSummaryで決定
	 */
	private enum SUMMARY_CONF{
		shortInterval(5, false,0, 8000),
		middleInterval(10, true,1, 8000),
		longInterval(15, false,2, 8000),
		dailyInterval(15, true,3, 8000),
		monthlyInterval(20, false,4, 8000),;
		private int intervalCount;
		private boolean useSummary;
		private int index;
		private double fund = 0;
		public boolean prevTradeBuy = false;
		public String prevOrderId = "";
		public double lastTradeAmount = 0;
		public int tradeCount = 0;
		private SUMMARY_CONF(int intervalCount, boolean useSummary, int index, double fund){
			this.intervalCount = intervalCount;
			this.useSummary = useSummary;
			this.index = index;
			this.fund = fund;
		}
		public int getIntervalCount(){return intervalCount;}
		public void setIntervalCount(int interval) { this.intervalCount = interval; }
		public boolean isUseSummary(){return useSummary;}
		public int getIndex(){return index;}
		public double getFund(){return fund;}
		public void setFund(double fund){this.fund = fund;}
		public void setUseSummary(boolean use){this.useSummary = use;}
	}
	private final String url = "https://jp.investing.com/crypto/bitcoin/btc-jpy";
	private final String technical = "<td class=\"left first\">テクニカル指標</td>";
//	private final String sma = "<td class=\"left first\">移動平均</td>";
	private final String charset = "UTF-8";
	private long callCount = 0;

	@Override
	public String getLogicName() {
		return this.getClass().getName();
	}

	@Override
	public void exec() {

		List<SIGNAL> result = null;
		for (SUMMARY_CONF summary: SUMMARY_CONF.values()) {
			if (summary.isUseSummary() && checkPreviousTrade(summary.prevOrderId)) {
				if (callCount % summary.getIntervalCount() == 0) {
					if (result == null){
						result = read(url, charset, technical, 5);
						log.info("Summary is " + result);
					}
					log.info(summary + " is executed.");
					SIGNAL sig = result.get(summary.getIndex());
					switch (sig){
						case s_buy:
							if (!summary.prevTradeBuy){
								buy(summary);
							} else {
								log.info("Previous trade is BUY. Current tech summary is " + sig + ". Not trade.");
							}
							break;
						case s_sell:
							if (summary.prevTradeBuy){
								sell(summary);
							} else {
								log.info("Previous trade is SELL. Current tech summary is " + sig + ". Not trade.");
							}
							break;
						default:
							log.info("Current tech summary is " + sig + ". Not trade summary index.");
					}
				}
			}
		}
		callCount++;
	}

	private void sell(SUMMARY_CONF summary){

		double currentPrice = CoinManager.getInstance().getCurrentRate();
		int sellRate = (int) (currentPrice * 0.9);
		double sellAmount = summary.lastTradeAmount;
		String result = CoinCheckClient.postSellRequest(String.valueOf(sellRate), String.valueOf(sellAmount));
		if (postTrade(result)) {
			summary.prevTradeBuy = false;
			summary.setFund(Double.parseDouble(Util.roundForAmount(currentPrice * sellAmount)));
			summary.tradeCount++;
		}
	}

	private void buy(SUMMARY_CONF summary){

		double currentPrice = CoinManager.getInstance().getCurrentRate();
		int buyRate = (int) (currentPrice * 1.2);
		double currentFund = summary.getFund();
		String amount = Util.roundForAmount(currentFund/currentPrice);
		String result = CoinCheckClient.postBuyRequest(String.valueOf(buyRate), amount);
		if (postTrade(result)){
			summary.prevTradeBuy = true;
			summary.lastTradeAmount = Double.parseDouble(amount);
			summary.tradeCount++;
		}
	}

	private boolean checkPreviousTrade(String prevOrderId){
		if (prevOrderId.equals("0") || prevOrderId.isEmpty()){
			return true;
		}
		return TradeManager.getInstance().isCompletedOrder(prevOrderId);
	}

	private synchronized boolean postTrade(String result){
		if (result.isEmpty()){
			log.error("Fail to post trade request.");
			return false;
		}
		JSONObject resultJSON = new JSONObject(result);
		if (!resultJSON.getBoolean(PARAM_KEY.success.name())){
			log.warn(result);
			return false;
		}
		String id = String.valueOf(resultJSON.getLong(PARAM_KEY.id.name()));
		String rate = resultJSON.getString(PARAM_KEY.rate.name());
		String date = resultJSON.getString(PARAM_KEY.created_at.name());
		String amount = resultJSON.getString(PARAM_KEY.amount.name());
		String orderType = resultJSON.getString(PARAM_KEY.order_type.name());
		TradeManager.getInstance().addOrder(resultJSON, toString());
		log.info(" Exec Post Order ID : " + id + " Order Type : " + orderType + " Approximate RATE : " + rate + " Amount" + amount + " DATE : " + date);
		return true;
	}

	/**
	 * Get from the url. Take some time for getting HTML.
	 * @param url web site url
	 * @param charset
	 * @param baseTypeString target string for getting result
	 * @param lineCount how many lines you want to read from basedTypeString line
	 * @return
	 */
	private static List<SIGNAL> read(String url, String charset, String baseTypeString, int lineCount) {
		List<SIGNAL> result = new ArrayList<>();
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			URLConnection conn = new URL(url).openConnection();
			conn.setRequestProperty("User-agent","Mozilla/5.0");
			is = conn.getInputStream();
			isr = new InputStreamReader(is,charset);
			br = new BufferedReader(isr);
			String line = null;
			while((line = br.readLine()) != null) {
				if (line.contains(baseTypeString)) {
					for (int i = 0; i < lineCount ; i++) {
						String res = br.readLine();
						if (res.contains("強い売り")){
							result.add(SIGNAL.s_sell);
						} else if (res.contains("強い買い")) {
							result.add(SIGNAL.s_buy);
						} else if (res.contains("買い")) {
							result.add(SIGNAL.buy);
						} else if (res.contains("売り")) {
							result.add(SIGNAL.sell);
						} else {
							result.add(SIGNAL.neutral);
						}
					}
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			}catch(Exception e) {
			}
			try {
				isr.close();
			}catch(Exception e) {
			}
			try {
				is.close();
			}catch(Exception e) {
			}
		}
		return result;
	}

	@Override
	public boolean setParams(Map<String, String> params) {
		for (SUMMARY_CONF conf: SUMMARY_CONF.values()) {
			String name = conf.name() + ".";
			conf.setFund(Double.valueOf(params.get(name + "Fund")));
			conf.setUseSummary(Boolean.valueOf(params.get(name + "Use")));
			conf.prevTradeBuy = Boolean.valueOf(params.get(name + "PrevBuy"));
			conf.setIntervalCount(Integer.valueOf(params.get(name + "Interval")));
		}
		return true;
	}

	@Override
	public Map<String, String> getParams() {
		Map<String, String> paramMap = new LinkedHashMap<>();
		for (SUMMARY_CONF conf: SUMMARY_CONF.values()) {
			String name = conf.name() + ".";
			paramMap.put(name + "Fund", String.valueOf(conf.getFund()));
			paramMap.put(name + "Use", String.valueOf(conf.isUseSummary()));
			paramMap.put(name + "PrevBuy", String.valueOf(conf.prevTradeBuy));
			paramMap.put(name + "Interval", String.valueOf(conf.getIntervalCount()));
		}
		return paramMap;
	}

	@Override
	public boolean stopTask() {
		StringBuilder info = new StringBuilder();
		for (SUMMARY_CONF conf: SUMMARY_CONF.values()) {
			if (conf.isUseSummary()) {
				info.append(conf.name());
				info.append(" : Previous Trade is buy = ");
				info.append(conf.prevTradeBuy);
				info.append(", Trade count is " + conf.tradeCount);
				log.info(info.toString());
			}
		}
		return true;
	}

	@Override
	public boolean resetTrade(String orderId) {
		return true;
	}

	@Override
	public String toString(){
		return getLogicName() + "#" + taskID;
	}
}
