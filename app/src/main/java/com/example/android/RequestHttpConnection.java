package com.example.android;

import android.content.ContentValues;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RequestHttpConnection {

    public String request(String _url, ContentValues params){
        HttpURLConnection conn = null;

        try{
            URL url = new URL(_url);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST"); // POST
            conn.setRequestProperty("Accept-Charset", "UTF-8"); // UTF-8

            String str = "39.121.10.168:8080/user/1";
            OutputStream os = conn.getOutputStream();
            os.write(str.getBytes("UTF-8"));
            os.flush();
            os.close();
            // 연결 확인
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
                return null;

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            String line;
            String page = "";

            // 라인을 받아와 합친다.
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                //page += line;
            }

            return page;
        }catch(MalformedURLException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            if(conn != null){
                conn.disconnect();
            }
        }
        return null;
    }

}
