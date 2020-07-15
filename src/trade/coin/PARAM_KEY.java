package trade.coin;

public enum PARAM_KEY {
	//REQ,RESP COMMON
	/**getLong */
	id,
	/**getString */
	rate,
	/**getString */
	amount,
	/**getString */
	order_type,
	/**getString */
	pair,
	//REQUEST
	/**getString */
	market_buy_amount,
	/**getString */
	market_buy,
	/**getString*/
	market_sell,
	//RESPONSE
	/**getBoolean */
	success,
	/**getString */
	created_at,
	/**JSON Array */
	data,
	/**getLong */
	order_id,
	/**JSON Array */
	orders,
	/**getString */
	pending_amount,
	/**JSON Object */
	funds,
	/**getString */
	btc,
	/**getString */
	email,
	;
}
