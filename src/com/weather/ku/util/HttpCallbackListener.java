package com.weather.ku.util;

public interface HttpCallbackListener {
	void onFinish(String response);//�˷�����ʾ���������ɹ���Ӧ����ʱ���ã�����������������ص�����
	
	void onError(Exception e);
}
