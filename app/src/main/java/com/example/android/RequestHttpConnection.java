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

public class RequestHttpConnection{
    // test server
    private String test_server;
    private HttpURLConnection httpConn;
    private URL url;

    public RequestHttpConnection(){
        test_server = "http://39.121.10.168:8001/user/";
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
                    }else{
                        Log.i("REST API"," COMPLETE POST");
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

                    // PUT 로 설정
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
                    }else{
                        Log.i("REST API","COMPLETE PUT");
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
                    }else{
                        Log.i("REST API","COMPLETE DELETE");
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
