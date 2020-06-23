package trade.util;

import org.apache.log4j.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class Util {

    private static Logger log = Logger.getLogger(Util.class);

    /**
     * Round double value. 5 digits after the decimal point(Half Up)
     * @param target
     * @return round value
     */
    public static String roundForAmount(double target){
        BigDecimal result = new BigDecimal(target).setScale(5, BigDecimal.ROUND_HALF_UP);
        return result.toString();
    }

    public static String createHmacSha256(String message, String secretKey) {
        String algo = "HmacSHA256";
        try {
            SecretKeySpec sk = new SecretKeySpec(secretKey.getBytes(), algo);
            Mac mac = Mac.getInstance(algo);
            mac.init(sk);

            byte[] mac_bytes = mac.doFinal(message.getBytes());

            StringBuilder sb = new StringBuilder(2 * mac_bytes.length);
            for (byte b : mac_bytes) {
                sb.append(String.format("%02x", b & 0xff));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error(e);
        }
        return null;
    }

    public static String httpBuildQuery(Map<String, String> params) throws UnsupportedEncodingException {
        String result = "";
        final String internalEncoding = "UTF-8";
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (e.getKey().isEmpty()) {
                continue;
            }
            if (!result.isEmpty()) {
                result += "&";
            }
            result += URLEncoder.encode(e.getKey(), internalEncoding) + "=" + URLEncoder.encode(e.getValue(), internalEncoding);
        }

        return result;
    }
}
