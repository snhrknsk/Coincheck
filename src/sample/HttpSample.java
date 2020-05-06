package sample;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpSample {
    public static void main(String[] args) {
        Charset charset = StandardCharsets.UTF_8;

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet request = new HttpGet("https://coincheck.com/api/rate/btc_jpy");

        System.out.println
                ("requestの実行　「" + request.getRequestLine() + "」");
        //requestの実行　「GET http://httpbin.org/get HTTP/1.1」

        CloseableHttpResponse response = null;

        try {
            response = httpclient.execute(request);

            int status = response.getStatusLine().getStatusCode();
            System.out.println("HTTPステータス:" + status);
            //HTTPステータス:200

            if (status == HttpStatus.SC_OK){
                String responseData =
                        EntityUtils.toString(response.getEntity(),charset);
                System.out.println(responseData);
                //取得したデータが表示される
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
    }
}
