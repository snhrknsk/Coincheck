package trade;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import trade.manager.AccountInfo;
import ui.HomeUI;

import java.io.*;

/**
 * Coincheck official API<br>
 * https://coincheck.com/ja/documents/exchange/api
 */
public class StartTrade {

    private static String OUTPUT_PATH = "./configuration/Configuration.json";
    private static Logger log = Logger.getLogger(StartTrade.class);

    public static void main(String[] args) {

        log.info("Start Trade");
        initialize();
        new HomeUI();
    }

    private static void initialize(){
        log.info("Initialize configuration.");
        String configJSON = readFile();
        if (configJSON == null){
            log.error("Fail to initialize. End.");
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
            log.equals(e);
            return null;
        }catch(IOException e){
            log.equals(e);
            return null;
        }
    }
}
