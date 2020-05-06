package trade.coin;

import java.lang.reflect.Constructor;

public enum COIN {
    BITCOIN("btc_jpy"),
    ;
    private String coinPair;
    private COIN(String coinPair){
        this.coinPair = coinPair;
    }

    public String getCoinPair() {
        return coinPair;
    }
}
