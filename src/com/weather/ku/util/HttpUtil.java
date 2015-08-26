package com.weather.ku.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class HttpUtil {
	public static void sendHttpRequest(final String adddress, final HttpCallbackListener listener)
	{
		new Thread(new Runnable() {
			
			public void run() {
			HttpsURLConnection connection=null;
			try {
				URL url=new URL(adddress);
				connection=(HttpsURLConnection)url.openConnection();
				connection.setRequestMethod("GET");
				connection.setConnectTimeout(8000);
				InputStream in=connection.getInputStream();
				BufferedReader reader=new BufferedReader(new InputStreamReader(in));
				StringBuilder response=new StringBuilder();
				String line;
				while ((line=reader.readLine())!=null)
				{
					response.append(line);
					
				}
				if(listener!=null)
				{
					listener.onFinish(response.toString());
				}
				
			} catch (Exception e) {
				if(listener!=null)
				{
					listener.onError(e);
				}
				
			}finally{
				if(connection!=null)
				{
					connection.disconnect();
				}
			}
			
				
			}
		}).start();
	}


}