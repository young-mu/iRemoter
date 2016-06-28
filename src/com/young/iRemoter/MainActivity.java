package com.young.iRemoter;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class MainActivity extends Activity implements OnClickListener, OnLongClickListener {
    private static final String TAG = "iRemoter";

    private String ruffIp = "http://192.168.1.110";
    private String ruffPort = "3000";
    private String ruffIrRecvPath = "ir-recv";
    private String ruffIrSendPath = "ir-send";
    private String ruffIrRecvUrl = getUrl(ruffIp, ruffPort, ruffIrRecvPath);
    private String ruffIrSendUrl = getUrl(ruffIp, ruffPort, ruffIrSendPath);

    private Button button1;
    private Button button2;
    private ArrayList<Integer> buttonIds = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(TAG, "enter MainActivity");

        button1 = (Button)findViewById(R.id.button1);
        button2 = (Button)findViewById(R.id.button2);

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);

        button1.setOnLongClickListener(this);
        button2.setOnLongClickListener(this);

        buttonIds.add(R.id.button1);
        buttonIds.add(R.id.button2);

        refreshButtonName(buttonIds);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.button1:
            Log.i(TAG, "request to " + ruffIrRecvUrl);
            HttpPostThread httpPostThread1 = new HttpPostThread("recv", R.id.button1);
            httpPostThread1.start();
            break;
        case R.id.button2:
            Log.i(TAG, "request to " + ruffIrSendUrl);
            String irCode = getIrCode(R.id.button1);
            if (irCode != "UNDEFINED") {
                HttpPostThread httpPostThread2 = new HttpPostThread("send", R.id.button1, irCode);
                httpPostThread2.start();
            }
            break;
        default:
            break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
        case R.id.button1:
            renameButton(R.id.button1);
            break;
        case R.id.button2:
            renameButton(R.id.button2);
            break;
        default:
            break;
        }
        return true;
    }

    private void renameButton(int buttonId) {
        Button button = (Button)findViewById(buttonId);
        String buttonNewName = "haha";
        Log.v(TAG, "button " + buttonId + " changes name as " + buttonNewName);
        button.setText(buttonNewName);

        SharedPreferences sp = getSharedPreferences("button_names", MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putString(String.valueOf(buttonId), buttonNewName);
        editor.commit();
    }

    private void refreshButtonName(ArrayList<Integer> buttonIds) {
        for (int i = 0; i < buttonIds.size(); i++) {
            int buttonId = buttonIds.get(i);
            Button button = (Button)findViewById(buttonId);
            SharedPreferences sp = getSharedPreferences("button_names", MODE_PRIVATE);
            String buttonNewName = sp.getString(String.valueOf(buttonId), "INTACT");
            if (buttonNewName != "INTACT") {
                button.setText(buttonNewName);
                Log.v(TAG, "button " + buttonId + " use new name " + buttonNewName);
            } else {
                Log.v(TAG, "button " + buttonId + " keep name intact");
            }
        }
    }

    private void putIrCode(int buttonId, String irCode) {
        SharedPreferences sp = getSharedPreferences("button_ircodes", MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putString(String.valueOf(buttonId), irCode);
        editor.commit();
    }

    private String getIrCode(int buttonId) {
        SharedPreferences sp = getSharedPreferences("button_ircodes", MODE_PRIVATE);
        String irCode = sp.getString(String.valueOf(buttonId), "UNDEFINED");
        Log.v(TAG, "button " + buttonId + " irCode " + irCode);
        return irCode;
    }

    private String getUrl(String ip, String port, String path) {
        return ip + ':' + port + '/' + path;
    }

    class HttpPostThread extends Thread {
        String postType;
        int buttonId;
        String irCode;

        public HttpPostThread(String postType, int buttonId) {
            this.postType = postType;
            this.buttonId = buttonId;
            this.irCode = "none";
        }

        public HttpPostThread(String postType, int buttonId, String irCode) {
            this.postType = postType;
            this.buttonId = buttonId;
            this.irCode = irCode;
        }

        @Override
        public void run() {
            NameValuePair irCode = new BasicNameValuePair("irCode", this.irCode);
            List<NameValuePair> pairList = new ArrayList<NameValuePair>();
            pairList.add(irCode);

            HttpClient httpClient = new DefaultHttpClient();

            if (this.postType == "recv" || this.postType == "send") {
                HttpPost httpPost = new HttpPost(this.postType == "recv" ? ruffIrRecvUrl : ruffIrSendUrl);

                try {
                    HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
                    httpPost.setEntity(requestHttpEntity);
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    int statusCode = httpResponse.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        Log.i(TAG, "request SUCCESS");
                        String recvData = getResponseEntity(httpResponse);
                        if (this.postType == "recv") {
                            putIrCode(this.buttonId, recvData);
                        }
                    }
                } catch (Exception e) {
                    Log.i(TAG, "request FAILED");
                    e.printStackTrace();
                }

            } else {
                Log.e(TAG, "invalid postType " + this.postType);
            }
        }
    }

    private String getResponseEntity(HttpResponse httpResponse) {
        if (httpResponse == null) {
            return null;
        }

        HttpEntity httpEntity = httpResponse.getEntity();

        try {
            InputStream inputStream = httpEntity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String data = "";
            String line = "";
            while ((line = reader.readLine()) != null) {
                data += line;
            }
            Log.i(TAG, "from server > " + data);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
