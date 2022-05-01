package com.example.android;

import android.content.ContentValues;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RequestHttpConnection {
    // test server
    private String test_server = "http://39.121.10.168:8001/user/";
    private HttpURLConnection httpConn;
    private URL url;

    // get
    public ArrayList<LocationData> getUserData(UserData userData){
        ArrayList<LocationData> nearByUserList = new ArrayList<LocationData>();
        new Thread(){
            public void run() {
                try {
                    String getUrl = test_server;
                    url = new URL(getUrl);
                    httpConn = (HttpURLConnection) url.openConnection();

                    // GET 설정
                    httpConn.setRequestMethod("POST");

                    StringBuffer buffer = new StringBuffer();
                    buffer.append("id").append("=").append(userData.getId()).append("&");
                    buffer.append("latitude").append("=").append(userData.getLatitude()).append("&");
                    buffer.append("longitude").append("=").append(userData.getLongitude()).append("&");
                    buffer.append("type").append("=").append(userData.getType());

                    // 서버 전송
                    OutputStream os = httpConn.getOutputStream();
                    os.write(buffer.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    if(httpConn.getResponseCode() != 200) {
                        throw new Exception( "Not Ok : " + httpConn.getResponseCode());
                    }

                    InputStreamReader input = new InputStreamReader(httpConn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(input);
                    StringBuilder builder = new StringBuilder();
                    String str;
                    while ((str = reader.readLine()) != null) {
                        builder.append(str + "\n");                     // View에 표시하기 위해 라인 구분자 추가
                    }
                    String myResult = builder.toString();
                    System.out.println(myResult);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if (httpConn != null)
                        httpConn.disconnect();
                    Log.i("REST API","REQUEST GET");
                }
            }
        }.start();

        return null;
    }

    // insert
    public void postUserData(UserData userData){
        new Thread(){
            public void run() {
                try {
                    String postUrl = test_server + userData.getId();
                    url = new URL(postUrl);
                    httpConn = (HttpURLConnection) url.openConnection();

                    // POST 로 설정
                    httpConn.setRequestMethod("POST");

                    StringBuffer buffer = new StringBuffer();
                    buffer.append("id").append("=").append(userData.getId()).append("&");
                    buffer.append("latitude").append("=").append(userData.getLatitude()).append("&");
                    buffer.append("longitude").append("=").append(userData.getLongitude()).append("&");
                    buffer.append("type").append("=").append(userData.getType());

                    // 서버 전송
                    OutputStream os = httpConn.getOutputStream();
                    os.write(buffer.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    if(httpConn.getResponseCode() != 200) {
                        return;
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if (httpConn != null)
                        httpConn.disconnect();
                    Log.i("REST API","REQUEST POST");
                }
            }
        }.start();
    }

    // update
    public void putUserData(UserData userData){
        new Thread(){
            public void run() {
                try {
                    String putUrl = test_server + userData.getId();
                    url = new URL(putUrl);
                    httpConn = (HttpURLConnection) url.openConnection();

                    // POST 로 설정
                    httpConn.setRequestMethod("PUT");

                    StringBuffer buffer = new StringBuffer();
                    buffer.append("id").append("=").append(userData.getId()).append("&");
                    buffer.append("latitude").append("=").append(userData.getLatitude()).append("&");
                    buffer.append("longitude").append("=").append(userData.getLongitude()).append("&");
                    buffer.append("type").append("=").append(userData.getType());

                    // 서버 전송
                    OutputStream os = httpConn.getOutputStream();
                    os.write(buffer.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    if(httpConn.getResponseCode() != 200) {
                        throw new Exception( "Not Ok : " + httpConn.getResponseCode());
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if (httpConn != null)
                        httpConn.disconnect();
                    Log.i("REST API","REQUEST UPDATE");
                }
            }
        }.start();
    }

    // delete
    public void deleteUserData(UserData userData){
        new Thread(){
            public void run() {
                try {
                    String deleteUrl = test_server + userData.getId();
                    url = new URL(deleteUrl);
                    httpConn = (HttpURLConnection) url.openConnection();

                    // DELETE 로 설정
                    httpConn.setRequestMethod("DELETE");

                    StringBuffer buffer = new StringBuffer();
                    buffer.append("id").append("=").append(userData.getId());

                    // 서버 전송
                    OutputStream os = httpConn.getOutputStream();
                    os.write(buffer.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();

                    if(httpConn.getResponseCode() != 200) {
                        throw new Exception( "Not Ok : " + httpConn.getResponseCode());
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if (httpConn != null)
                        httpConn.disconnect();
                    Log.i("REST API","REQUEST DELETE");
                }
            }
        }.start();
    }



}
