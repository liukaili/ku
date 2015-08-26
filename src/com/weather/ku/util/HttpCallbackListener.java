package com.weather.ku.util;

public interface HttpCallbackListener {
	void onFinish(String response);//此方法表示当服务器成功响应请求时调用，参数代表服务器返回的数据
	
	void onError(Exception e);
}
