package trade.coin;

public enum PARAM_KEY {
	//REQ,RESP COMMON
	id,//long
	rate,
	amount,
	order_type,
	pair,
	//REQUEST

	//RESPONSE
	success,//boolean
	created_at,
	data,//JSON Array
	order_id,//long
	orders,//JSON Array
	pending_amount,
	funds,//JSONObject
	btc,
	;
}
