package com.example.android;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class RequestHttpConnection {
    // test server
    private String test_server;
    private HttpURLConnection httpConn;
    private URL url;

    public RequestHttpConnection(){
        test_server = "http://192.168.35.49:8080/user/";
    }

    // get
    public ArrayList<LocationDTO> getUserData(UserDTO userDTO){
        ArrayList<LocationDTO> nearByUserList = new ArrayList<>();

        new Thread(){
            public void run() {
                try {

                    StringBuffer buffer = new StringBuffer(test_server);
                    buffer.append("?");
                    buffer.append("latitude").append("=").append(userDTO.getLatitude()).append("&");
                    buffer.append("longitude").append("=").append(userDTO.getLongitude()).append("&");
                    buffer.append("type").append("=").append(userDTO.getType());

                    url = new URL(buffer.toString());
                    httpConn = (HttpURLConnection) url.openConnection();
                    // GET 설정
                    httpConn.setRequestMethod("GET");
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

                    //Log.i("RESULT", jsonArray.toString());

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    Log.i("REST API","REQUEST GET");
                }
            }
        }.start();

        return nearByUserList;
    }

    // insert
    public void postUserData(UserDTO userDTO){
        new Thread(){
            public void run() {
                try {
                    String postUrl = test_server + userDTO.getId();
                    url = new URL(postUrl);
                    httpConn = (HttpURLConnection) url.openConnection();

                    // POST 로 설정
                    httpConn.setRequestMethod("POST");

                    StringBuffer buffer = new StringBuffer();
                    buffer.append("id").append("=").append(userDTO.getId()).append("&");
                    buffer.append("latitude").append("=").append(userDTO.getLatitude()).append("&");
                    buffer.append("longitude").append("=").append(userDTO.getLongitude()).append("&");
                    buffer.append("type").append("=").append(userDTO.getType());

                    // 서버 전송
                    OutputStream os = httpConn.getOutputStream();
                    os.write(buffer.toString().getBytes("UTF-8"));
                    os.flush();

                    if(httpConn.getResponseCode() != 200) {
                        Log.i("ERROR","Not Ok : " + httpConn.getResponseCode());
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    Log.i("REST API","REQUEST POST");
                }
            }
        }.start();
    }

    // update
    public void putUserData(UserDTO userDTO){
        new Thread(){
            public void run() {
                try {
                    String putUrl = test_server + userDTO.getId();
                    url = new URL(putUrl);
                    httpConn = (HttpURLConnection) url.openConnection();

                    // POST 로 설정
                    httpConn.setRequestMethod("PUT");

                    StringBuffer buffer = new StringBuffer();
                    buffer.append("id").append("=").append(userDTO.getId()).append("&");
                    buffer.append("latitude").append("=").append(userDTO.getLatitude()).append("&");
                    buffer.append("longitude").append("=").append(userDTO.getLongitude()).append("&");
                    buffer.append("type").append("=").append(userDTO.getType());

                    // 서버 전송
                    OutputStream os = httpConn.getOutputStream();
                    os.write(buffer.toString().getBytes("UTF-8"));
                    os.flush();

                    if(httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        throw new Exception( "Not Ok : " + httpConn.getResponseCode());
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    Log.i("REST API","REQUEST UPDATE");
                }
            }
        }.start();
    }

    // delete
    public void deleteUserData(UserDTO userDTO){
        new Thread(){
            public void run() {
                try {
                    String deleteUrl = test_server + userDTO.getId();
                    url = new URL(deleteUrl);
                    httpConn = (HttpURLConnection) url.openConnection();

                    // DELETE 로 설정
                    httpConn.setRequestMethod("DELETE");

                    StringBuffer buffer = new StringBuffer();
                    buffer.append("id").append("=").append(userDTO.getId());

                    // 서버 전송
                    OutputStream os = httpConn.getOutputStream();
                    os.write(buffer.toString().getBytes("UTF-8"));
                    os.flush();

                    if(httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        throw new Exception( "Not Ok : " + httpConn.getResponseCode());
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    httpConn.disconnect();
                    Log.i("REST API","REQUEST DELETE");
                }
            }
        }.start();
    }



}
