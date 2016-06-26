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

public class MainActivity extends Activity implements OnClickListener, OnLongClickListener {
    private static final String TAG = "iRemoter";
    private Button button1;
    private String ruffIp = "http://192.168.1.105";
    private String ruffPort = "3000";
    private String ruffIrPath = "ir-post";
    private String ruffIrUrl = getUrl(ruffIp, ruffPort, ruffIrPath);
    private String irDevice = "AC";
    private String irCode = "01000123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "enter MainActivity");
        button1 = (Button)findViewById(R.id.button1);
        button1.setOnClickListener(this);
        button1.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        case R.id.button1:
            Log.i(TAG, "request to " + ruffIrUrl);
            HttpPostThread httpPostThread= new HttpPostThread(irDevice, irCode);
            httpPostThread.start();
            break;
        default:
            break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
        case R.id.button1:
            Log.i(TAG, "long press");
            break;
        default:
            break;
        }
        return true;
    }

    private String getUrl(String ip, String port, String path) {
        return ip + ':' + port + '/' + path;
    }

    class HttpPostThread extends Thread {
        String irDevice;
        String irCode;

        public HttpPostThread(String irDevice, String irCode) {
            this.irDevice = irDevice;
            this.irCode = irCode;
        }

        @Override
        public void run() {

            NameValuePair id = new BasicNameValuePair("irDevice", this.irDevice);
            NameValuePair value = new BasicNameValuePair("irCode", this.irCode);
            List<NameValuePair> pairList = new ArrayList<NameValuePair>();
            pairList.add(id);
            pairList.add(value);

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(ruffIrUrl);

            try {
                HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
                httpPost.setEntity(requestHttpEntity);
                HttpResponse httpResponse = httpClient.execute(httpPost);
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    showResponseEntity(httpResponse);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showResponseEntity(HttpResponse httpResponse) {
        if (httpResponse == null) {
            return;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
