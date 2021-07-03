package com.tave7.dobdob;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

//KaKao Geocode API 통해 주소 검색 결과 받기
@SuppressLint("StaticFieldLeak")
public class GetGEOTask extends AsyncTask<String, Void, String> {
    Context context;
    String receiveMsg = "";
    String whereAct = "";
    String address;
    URL link = null;
    HttpsURLConnection hc = null;

    GetGEOTask(Context context, String whereAct, String address) {
        super();
        this.context = context;
        this.whereAct = whereAct;
        this.address = address;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            address = address.substring(address.indexOf(',')+2);
            link = new URL("https://dapi.kakao.com/v2/local/search/address.json?query=" + URLEncoder.encode(address, "UTF-8"));

            HttpsURLConnection.setDefaultHostnameVerifier((arg0, arg1) -> true);
            String kakaoKey = context.getString(R.string.KAKAO_REST_API_KEY);      //KAKAO REST API 키;
            String auth = "KakaoAK " + kakaoKey;

            hc = (HttpsURLConnection) link.openConnection();
            hc.setRequestMethod("GET");
            hc.setRequestProperty("User-Agent", "Java-Client");
            hc.setRequestProperty("X-Requested-With", "curl");
            hc.setRequestProperty("Authorization", auth);

            if (hc.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(hc.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder buffer = new StringBuilder();
                String str;
                while ((str = reader.readLine()) != null) {
                    buffer.append(str);
                }
                receiveMsg = buffer.toString();
                reader.close();
            } else {
                Log.i("Kakao Address Geo Result", hc.getResponseCode() + ": error");
            }
        } catch (IOException e) { e.printStackTrace(); }

        return receiveMsg;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        try {
            JSONArray jarray = new JSONObject(result).getJSONArray("documents");
            String fullAddress = jarray.getJSONObject(0).getString("address_name");

            JSONObject roadAddress = jarray.getJSONObject(0).getJSONObject("road_address");
            String si = roadAddress.getString("region_1depth_name");
            String gu = roadAddress.getString("region_2depth_name");
            String dong = roadAddress.getString("region_3depth_name");
            Double locationX = Double.valueOf(roadAddress.getString("x"));
            Double locationY = Double.valueOf(roadAddress.getString("y"));

            JsonObject loc = new JsonObject();
            loc.addProperty("detail", fullAddress);
            loc.addProperty("si", si);
            loc.addProperty("gu", gu);
            loc.addProperty("dong", dong);
            loc.addProperty("locationX", locationX);
            loc.addProperty("locationY", locationY);

            if (whereAct == "initial") {
                InitialSettingActivity activity = (InitialSettingActivity) context;
                activity.initialSettingTown(loc);
            }
            else if (whereAct == "main") {
                MainActivity activity = (MainActivity) context;
                activity.mainSettingTown(loc);
            }
            else if (whereAct == "posting") {
                PostingActivity activity = (PostingActivity) context;
                activity.postingSettingTown(loc);
            }
            else if (whereAct == "modifyProfile"){
                ModifyProfileActivity activity = (ModifyProfileActivity) context;
                activity.modifyPSettingTown(loc);
            }
        } catch (JSONException e) { e.printStackTrace(); }
    }
}
