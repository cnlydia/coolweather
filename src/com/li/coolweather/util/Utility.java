package com.li.coolweather.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.li.coolweather.db.CoolWeatherDB;
import com.li.coolweather.model.City;
import com.li.coolweather.model.County;
import com.li.coolweather.model.Province;

public class Utility {
	public synchronized static boolean handleProvinceResponse(
			CoolWeatherDB coolWeatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceName(array[1]);
					province.setProvinceCode(array[0]);
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		System.out.println(response + "..");
		return false;
	}

	public synchronized static boolean handleCityResponse(
			CoolWeatherDB coolWeatherDB, String response, int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCitys = response.split(",");
			if (allCitys != null && allCitys.length > 0) {
				for (String c : allCitys) {
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityName(array[1]);
					city.setCityCode(array[0]);
					city.setProvinceId(provinceId);
					coolWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}

	public synchronized static boolean handleCountyResponse(
			CoolWeatherDB coolWeatherDB, String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0) {
				for (String c : allCounties) {
					String[] array = c.split("\\|");
					County county = new County();
					county.setCountyName(array[1]);
					county.setCountyCode(array[0]);
					county.setCityId(cityId);
					coolWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}

	public static void handleWeatherResponse(Context context, String response) {

		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("data");
			String weatherNow = weatherInfo.getString("wendu");
			String tips = weatherInfo.getString("ganmao");
			String cityName = weatherInfo.getString("city");
			JSONArray forecast = weatherInfo.getJSONArray("forecast");
			JSONObject forecast1 = forecast.getJSONObject(0);
			String weatherDesp = forecast1.getString("type");
			String temp1 = forecast1.getString("high");
			String temp2 = forecast1.getString("low");
			String fengxiang = forecast1.getString("fengxiang");
			String fengli = forecast1.getString("fengli");
			String date = forecast1.getString("date");
			saveWeatherInfo(context, weatherNow,tips, cityName, weatherDesp, temp1,
					temp2, fengxiang, fengli, date);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private static void saveWeatherInfo(Context context, String weatherNow,String tips,
			String cityName, String weatherDesp, String temp1, String temp2,
			String fengxiang, String fengli, String date) {
		SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("weather_now", weatherNow);
		editor.putString("tips", tips);
		editor.putString("city_name", cityName);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("fengxiang", fengxiang);
		editor.putString("fengli", fengli);
		editor.putString("current_date", date);

		editor.commit();
	}
}
