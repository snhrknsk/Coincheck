package trade.coin;


import org.apache.http.HttpStatus;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import trade.manager.AccountInfo;
import trade.util.Util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 *
 */
public class CoinCheckClient {

    private enum REQUEST_TYPE {
        GET, GET_AUTH, POST, POST_AUTH, DELETE, DELETE_AUTH
    }

    private static HttpUriRequest createRequest(REQUEST_TYPE type, String uri, String params)  throws IOException{

        long nonce = System.currentTimeMillis();
        String message = nonce + uri + params;
        String signature = Util.createHmacSha256(message, AccountInfo.getInstance().getSecretKey());
        switch (type) {
            case GET:
                return new HttpGet(uri);
            case GET_AUTH:
                HttpGet getReq = new HttpGet(uri);
                getReq.addHeader("Content-Type", "application/json");
                getReq.addHeader("ACCESS-KEY", AccountInfo.getInstance().getAccessKey());
                getReq.addHeader("ACCESS-NONCE", String.valueOf(nonce));
                getReq.addHeader("ACCESS-SIGNATURE", signature);
                return getReq;
            case POST:
            case POST_AUTH:
                HttpPost postReq = new HttpPost(uri);
                //set post header
                postReq.addHeader("Content-Type", "application/json");
                postReq.addHeader("ACCESS-KEY", AccountInfo.getInstance().getAccessKey());
                postReq.addHeader("ACCESS-NONCE", String.valueOf(nonce));
                postReq.addHeader("ACCESS-SIGNATURE", signature);
                StringEntity entity = new StringEntity(params);
                postReq.setEntity(entity);
                return postReq;
            case DELETE:
                return new HttpDelete(uri);
            case DELETE_AUTH:
                HttpDelete deleteReq = new HttpDelete(uri);
                deleteReq.addHeader("Content-Type", "application/json");
                deleteReq.addHeader("ACCESS-KEY", AccountInfo.getInstance().getAccessKey());
                deleteReq.addHeader("ACCESS-NONCE", String.valueOf(nonce));
                deleteReq.addHeader("ACCESS-SIGNATURE", signature);
                return deleteReq;
            default:
                throw new UnsupportedOperationException();
        }
    }

    /**
     * invoke request.<br>
     * if response code is not 200, response is empty.
     * @param type non NULL
     * @param uri request target URI
     * @param  params request param. this param must be json format.
     * @return
     */
    private static synchronized String postRequest(REQUEST_TYPE type, String uri, String params) {
        Charset charset = StandardCharsets.UTF_8;
        CloseableHttpResponse response = null;
        String responseData = "";
        CloseableHttpClient httpclient = null;
        try {
            // invoke request
            httpclient = HttpClients.createDefault();
            response = httpclient.execute(createRequest(type, uri, params));
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                responseData = EntityUtils.toString(response.getEntity(), charset);
            } else {
                String error = EntityUtils.toString(response.getEntity(), charset);
                System.err.println(error);
            }
        } catch (IOException e) {
            e.printStackTrace();
            responseData = "";
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                if (httpclient != null) {
                    httpclient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseData;
    }

    /**
     * refer to recent price
     * @return
     */
    public static String getCurrentPrice() {
        String uri = TRADE_API.rate.getUrl() + COIN.BITCOIN.getCoinPair();
        String result = postRequest(REQUEST_TYPE.GET, uri, "");
        return result;
    }

    /**
     * refer to recent trade history.
     * @return
     */
    public static String getTradeHistory(){
        String uri = TRADE_API.history.getUrl();
        String result = postRequest(REQUEST_TYPE.GET_AUTH,uri, "");
        return result;
    }

    public static String postBuyRequest(String rate, String amount){
        String uri = TRADE_API.sell.getUrl();
        JSONObject orderObj = new JSONObject();
        orderObj.put("rate", rate);
        orderObj.put("amount", amount);
        orderObj.put("order_type", "buy");
        orderObj.put("pair", COIN.BITCOIN.getCoinPair());
        String result = postRequest(REQUEST_TYPE.POST_AUTH, uri, orderObj.toString());
        return result;
    }

    public  static String postSellRequest(String rate, String amount){
        String uri = TRADE_API.buy.getUrl();
        JSONObject orderObj = new JSONObject();
        orderObj.put("rate", rate);
        orderObj.put("amount", amount);
        orderObj.put("order_type", "sell");
        orderObj.put("pair", COIN.BITCOIN.getCoinPair());
        String result = postRequest(REQUEST_TYPE.POST_AUTH, uri, orderObj.toString());
        return result;
    }

    public static String postOrderCancel(String id){
        String uri = TRADE_API.cancel.getUrl() + id;
        String result = postRequest(REQUEST_TYPE.DELETE_AUTH, uri, "");
        return result;
    }

    public static String postOpenOrder(){
        String uri = TRADE_API.open.getUrl();
        String result = postRequest(REQUEST_TYPE.GET_AUTH, uri, "");
        return result;
    }

    public static String getAccountInfo(){
        String uri = TRADE_API.account.getUrl();
        String result = postRequest(REQUEST_TYPE.GET_AUTH, uri, "");
        return result;
    }
}
