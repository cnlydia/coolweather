package com.li.coolweather.util;

public interface HttpCallbackListener {
	void onFinish(String response);

	void onError(Exception e);
}
