package trade;

import org.json.JSONObject;
import trade.coin.CoinCheckClient;
import trade.coin.PARAM_KEY;
import trade.exec.CheckRate;
import trade.exec.CheckTrade;
import trade.exec.TaskWorker;
import trade.manager.AccountInfo;
import trade.manager.TradeManager;

/**
 * Coincheck official API<br>
 * https://coincheck.com/ja/documents/exchange/api
 */
public class StartTrade {

    public static void main(String[] args) {

        if (args.length != 2){
            System.out.println("Invalid argument. Size : " + args.length);
            return;
        }
        System.out.println("start trade");
        initialize(args[0], args[1]);

        new TaskWorker().startTask(60000, 1800000);

//        test();
    }

    public static void initialize(String access, String secret){
        AccountInfo.getInstance().setAccessKey(access);
        AccountInfo.getInstance().setSecretKey(secret);
    }

    private static void test(){
        new CheckRate().exec();
        new CheckTrade().exec();
//        String result = CoinCheckClient.postBuyRequest("946900", "0.005");
//        String result = CoinCheckClient.postSellRequest("947700", "0.005");
//        String result = "{\"success\":true,\"id\":2423096044,\"amount\":\"0.005\",\"rate\":\"947700.0\",\"order_type\":\"sell\",\"pair\":\"btc_jpy\",\"created_at\":\"2020-05-05T16:52:21.000Z\",\"market_buy_amount\":null,\"stop_loss_rate\":null}";
//        System.out.println(result);
//        JSONObject json = new JSONObject(result);
//        TradeManager.getInstance().addOrder(json);
//        System.out.println(TradeManager.getInstance().getOrder(String.valueOf(json.getLong(PARAM_KEY.id.name()))).getAmount());
//        result = CoinCheckClient.getTradeHistory();
//        System.out.println(result);
    }
}
