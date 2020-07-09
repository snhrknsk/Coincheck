package trade.coin;

public enum TRADE_API {
    rate("/api/rate/"),
    history("/api/exchange/orders/transactions_pagination"),
    buy("/api/exchange/orders"),
    market_buy("/api/exchange/orders"),
    sell("/api/exchange/orders"),
    market_sell("/api/exchange/orders"),
    cancel("/api/exchange/orders/"),
    open("/api/exchange/orders/opens"),
    account("api/accounts"),
    ;

    private final String BASE_API = "https://coincheck.com";
    private String url;
    TRADE_API(String url){
        this.url = url;
    }
    public String getUrl() {
        return BASE_API + url;
    }
}
