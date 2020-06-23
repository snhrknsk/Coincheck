package trade;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import trade.manager.AccountInfo;
import ui.HomeUI;

import javax.swing.*;
import java.io.*;

/**
 * Coincheck official API<br>
 * https://coincheck.com/ja/documents/exchange/api
 */
public class StartTrade {

    private static final String OUTPUT_PATH = "./configuration/Configuration.json";
    private static final Logger log = Logger.getLogger(StartTrade.class);

    public static void main(String[] args) {

        log.info("Start Trade");
        initialize();
        SwingUtilities.invokeLater(HomeUI::new);
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
            StringBuilder data = new StringBuilder();
            String str = br.readLine();
            while(str != null){
                data.append(str);
                str = br.readLine();
            }
            br.close();
            return data.toString();
        } catch(IOException e){
            log.error(e);
            return null;
        }
    }
}
