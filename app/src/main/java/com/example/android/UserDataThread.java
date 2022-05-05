package com.example.android;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class UserDataThread extends Thread {
    private ArrayList<LocationDTO> nearByUserList = new ArrayList<LocationDTO>();
    private UserDTO userDTO;
    public UserDataThread(UserDTO userDTO){
        this.userDTO = userDTO;
    }
    public void run(){
        try {

            StringBuffer buffer = new StringBuffer("http://39.121.10.168:8001/user/");
            buffer.append("?");
            buffer.append("latitude").append("=").append(userDTO.getLatitude()).append("&");
            buffer.append("longitude").append("=").append(userDTO.getLongitude()).append("&");
            buffer.append("type").append("=").append(userDTO.getType());

            URL url = new URL(buffer.toString());
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            // GET 설정
            httpConn.setRequestMethod("GET");
            httpConn.setUseCaches(true);
            httpConn.setDoInput(true);

            InputStreamReader response = new InputStreamReader(httpConn.getInputStream(), "UTF-8");
            BufferedReader reader = new BufferedReader(response);

            buffer = new StringBuffer();

            String line = "";

            while((line = reader.readLine()) != null){
                buffer.append(line + "\n");
            }

            JSONArray jsonArray = new JSONArray(buffer.toString());
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jObject = jsonArray.getJSONObject(i);
                double latitude = jObject.getDouble("latitude");
                double longitude = jObject.getDouble("longitude");
                nearByUserList.add(new LocationDTO(latitude, longitude));
            }

            Log.i("스레드 RESULT", jsonArray.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            Log.i("REST API","REQUEST GET");
        }
    }
    public ArrayList<LocationDTO> getUserData(){
        return this.nearByUserList;
    }
}
