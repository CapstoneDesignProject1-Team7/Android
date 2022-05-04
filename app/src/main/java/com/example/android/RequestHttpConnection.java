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
import java.util.concurrent.Callable;

public class RequestHttpConnection implements Callable<ArrayList<LocationDTO>> {
    // test server
    private String test_server;
    private HttpURLConnection httpConn;
    private URL url;
    private UserDTO userDTO;
    int otherType;

    public RequestHttpConnection(){
        test_server = "http://39.121.10.168:8001/user/";
    }
    public void setUserDTO(UserDTO userDTO) {
        this.userDTO = userDTO;
    }
    // get
    @Override
    public ArrayList<LocationDTO> call() throws Exception {
        // 운전자라면 보행자의 정보를, 보행자라면 운전자의 정보를 넘김
        otherType = userDTO.getType()==0 ? 1 : 0;

        ArrayList<LocationDTO> nearByUserList = new ArrayList<>();
        StringBuffer buffer = new StringBuffer(test_server);
        buffer.append("?");
        buffer.append("latitude").append("=").append(userDTO.getLatitude()).append("&");
        buffer.append("longitude").append("=").append(userDTO.getLongitude()).append("&");
        buffer.append("type").append("=").append(otherType);

        url = new URL(buffer.toString());
        httpConn = (HttpURLConnection) url.openConnection();
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
                    httpConn.setUseCaches(true);
                    httpConn.setDoOutput(true);

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
                    httpConn.setUseCaches(true);
                    httpConn.setDoOutput(true);

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
                    httpConn.setUseCaches(true);
                    httpConn.setDoOutput(true);

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
