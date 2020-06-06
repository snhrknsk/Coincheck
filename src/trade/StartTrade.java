package trade;

import org.json.JSONObject;
import ui.HomeUI;
import trade.exec.CheckRate;
import trade.exec.CheckTrade;
import trade.manager.AccountInfo;

import java.io.*;

/**
 * Coincheck official API<br>
 * https://coincheck.com/ja/documents/exchange/api
 */
public class StartTrade {

    private static String OUTPUT_PATH = "./Configuration.json";

    public static void main(String[] args) {

        System.out.println("Start trade");
        initialize();

        new HomeUI();
    }

    private static void initialize(){
        System.out.println("Initialize configuration.");
        String configJSON = readFile();
        if (configJSON == null){
            System.out.println("Fail to initialize. End.");
            System.exit(0);
        }
        JSONObject config = new JSONObject(configJSON);
        AccountInfo.getInstance().setAccessKey(config.getString("access-key"));
        AccountInfo.getInstance().setSecretKey(config.getString("security-key"));
        AccountInfo.getInstance().setOutputFilePath(config.getString("file-path"));
    }

    private static String readFile(){
        try{
            File file = new File(OUTPUT_PATH);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String data = "";
            String str = br.readLine();
            while(str != null){
                data += str;
                str = br.readLine();
            }
            br.close();
            return data;
        }catch(FileNotFoundException e){
            System.out.println(e);
            return null;
        }catch(IOException e){
            System.out.println(e);
            return null;
        }
    }
}
