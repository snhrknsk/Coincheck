package trade;

import ui.HomeUI;
import trade.exec.CheckRate;
import trade.exec.CheckTrade;
import trade.manager.AccountInfo;

/**
 * Coincheck official API<br>
 * https://coincheck.com/ja/documents/exchange/api
 */
public class StartTrade {

    public static void main(String[] args) {

        if (args.length != 2){
            System.out.println("Invalid argument. Size : " + args.length + " Need access and security keys.");
            return;
        }
        System.out.println("start trade");
        initialize(args[0], args[1]);

        new HomeUI();
    }

    public static void initialize(String access, String secret){
        AccountInfo.getInstance().setAccessKey(access);
        AccountInfo.getInstance().setSecretKey(secret);
    }
}
