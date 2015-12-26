package com.li.coolweather.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.li.coolweather.R;
import com.li.coolweather.service.AutoUpdateService;
import com.li.coolweather.util.HttpCallbackListener;
import com.li.coolweather.util.HttpUtil;
import com.li.coolweather.util.Utility;

public class WeatherActivity extends Activity implements OnClickListener {
	private LinearLayout weatherLayout;
	private TextView cityNameText;
	private TextView weatherNowText;
	private TextView temp1Text;
	private TextView temp2Text;
	private TextView currentDateText;
	private ImageView switchCity;
	private ImageView refreshWeather;
	private TextView weatherDespText;
	private TextView windText;
	private TextView tipsText;
	private ImageView weatherIcon;

	private int[] icons = new int[] { R.drawable.qing, R.drawable.yintian,
			R.drawable.duoyun, R.drawable.zhenyu, R.drawable.xiaoyu,
			R.drawable.dayu, R.drawable.baoyu, R.drawable.xiaoxue,
			R.drawable.daxue, R.drawable.baoxue, R.drawable.yujiaxue,
			R.drawable.shandian };

	private String[] types = new String[] { "晴", "阴", "多云", "阵雨", "小雨", "大雨",
			"暴雨", "小雪", "大雪", "暴雪", "雨夹雪", "闪电" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		initView();
	}

	private void initView() {
		currentDateText = (TextView) findViewById(R.id.current_date);
		refreshWeather = (ImageView) findViewById(R.id.refresh_weather);
		weatherLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		weatherNowText = (TextView) findViewById(R.id.weather_now);
		weatherIcon = (ImageView) findViewById(R.id.weatherIcon);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		windText = (TextView) findViewById(R.id.wind);
		tipsText = (TextView) findViewById(R.id.tips);
		cityNameText = (TextView) findViewById(R.id.city_name);
		switchCity = (ImageView) findViewById(R.id.switch_city);
		weatherIcon.setVisibility(View.INVISIBLE);

		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);

		String countyCode = getIntent().getStringExtra("county_code");
		if (!TextUtils.isEmpty(countyCode)) {
			weatherLayout.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		} else {
			showWeather();
		}
	}

	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city"
				+ countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}

	private void queryWeatherInfo(String weatherCode) {
		String address = "http://wthrcdn.etouch.cn/weather_mini?citykey="
				+ weatherCode;
		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(this).edit();
		editor.putString("weather_code", weatherCode);
		editor.commit();
		queryFromServer(address, "weatherCode");
	}

	private void queryFromServer(String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				} else if ("weatherCode".equals(type)) {
					Utility.handleWeatherResponse(WeatherActivity.this,
							response);
					runOnUiThread(new Runnable() {
						public void run() {
							showWeather();
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getApplicationContext(), "同步失败",
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}

	private void showWeather() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		weatherNowText.setText(prefs.getString("weather_now", "") + "℃");
		cityNameText.setText(prefs.getString("city_name", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		setImage(prefs.getString("weather_desp", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		windText.setText(prefs.getString("fengxiang", "") + "/"
				+ prefs.getString("fengli", ""));
		tipsText.setText("Tips:" + prefs.getString("tips", ""));
		currentDateText.setText(prefs.getString("current_date", ""));

		weatherLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		Intent intent = new Intent(this, AutoUpdateService.class);
		startService(intent);
	}

	private void setImage(String type) {
		for (int i = 0; i < types.length; i++) {
			if (type.equals(types[i])) {
				weatherIcon.setImageResource(icons[i]);
				weatherIcon.setVisibility(View.VISIBLE);
			}
		}
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			Toast.makeText(getApplicationContext(), "同步中", Toast.LENGTH_SHORT)
					.show();
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			String weatherCode = prefs.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}
			break;
		default:
			break;
		}
	}
}
