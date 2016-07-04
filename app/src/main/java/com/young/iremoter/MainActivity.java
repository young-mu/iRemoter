package com.young.iremoter;

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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private static final String TAG = "iRemoter";
    private static final int buttonNum = 6;
    private static final String spInitButtonNames = "button_init_names";
    private static final String spNewButtonNames = "button_new_names";
    private static final String spButtonIrCodes = "button_ircodes";

    private String ruffIp = "http://192.168.1.101";
    private String ruffPort = "3000";
    private String ruffIrRecvPath = "ir-recv";
    private String ruffIrSendPath = "ir-send";
    private String ruffIrRecvUrl = getUrl(ruffIp, ruffPort, ruffIrRecvPath);
    private String ruffIrSendUrl = getUrl(ruffIp, ruffPort, ruffIrSendPath);

    private static final int NORMAL_MODE = 0;
    private static final int CONF_MODE = 1;
    private static final int RENAME_MODE = 2;
    private int mode = NORMAL_MODE;

    private static final int CONF_SUCCESS = 1;
    private static final int CONF_FAILURE = 2;
    private static final int SEND_SUCCESS = 3;
    private static final int SEND_FAILURE = 4;

    private ArrayList<Button> buttons = new ArrayList<Button>();
    private ArrayList<Integer> buttonIds = new ArrayList<Integer>();

    private String getUrl(String ip, String port, String path) {
        return ip + ':' + port + '/' + path;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.i(TAG, "enter MainActivity");

        buttonIds.add(R.id.button1);
        buttonIds.add(R.id.button2);
        buttonIds.add(R.id.button3);
        buttonIds.add(R.id.button4);
        buttonIds.add(R.id.button5);
        buttonIds.add(R.id.button6);
        assert(buttonIds.size() == buttonNum);

        for (int i = 0; i < buttonIds.size(); i++) {
            buttons.add((Button)findViewById(buttonIds.get(i)));
        }
        assert(buttons.size() == buttonNum);

        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setOnClickListener(this);
        }

        saveInitButtonName(buttons, buttonIds);

        updateButtonName(buttonIds);
        updateButtonColor(buttons, buttonIds, mode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.settings_start_conf:
                if (mode == NORMAL_MODE) {
                    Log.i(TAG, "enter configure mode");
                    mode = CONF_MODE;
                    Toast.makeText(this, "进入配置模式", Toast.LENGTH_SHORT).show();
                    updateButtonColor(buttons, buttonIds, mode);
                } else if (mode == CONF_MODE) {
                    Toast.makeText(this, "当前已是配置模式", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "请先退出命名模式", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.settings_end_conf:
                if (mode == CONF_MODE) {
                    Log.i(TAG, "exit configure mode");
                    mode = NORMAL_MODE;
                    updateButtonColor(buttons, buttonIds, mode);
                    Toast.makeText(this, "退出配置模式", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.settings_clean_conf:
                if (mode == NORMAL_MODE) {
                    if (mode == NORMAL_MODE) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("确定清除所有配置?")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(MainActivity.this, "已清除所有配置", Toast.LENGTH_SHORT).show();
                                        removeIrCode(buttonIds);
                                        updateButtonColor(buttons, buttonIds, mode);
                                    }
                                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, "cancel cleaning configurations");
                            }
                        }).show();
                    }
                }
                break;
            case R.id.settings_start_rename:
                if (mode == NORMAL_MODE) {
                    Log.i(TAG, "enter rename mode");
                    mode = RENAME_MODE;
                    Toast.makeText(this, "进入命名模式", Toast.LENGTH_SHORT).show();
                    updateButtonColor(buttons, buttonIds, mode);
                } else if (mode == RENAME_MODE) {
                    Toast.makeText(this, "当前已是命名模式", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "请先退出配置模式", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.settings_end_rename:
                if(mode == RENAME_MODE) {
                    Log.i(TAG, "exit rename mode");
                    mode = NORMAL_MODE;
                    Toast.makeText(this, "退出命名模式", Toast.LENGTH_SHORT).show();
                    updateButtonColor(buttons, buttonIds, mode);
                }
                break;
            case R.id.settings_clean_rename:
                if (mode == NORMAL_MODE) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("确定清除所有命名?")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(MainActivity.this, "已清除所有命名", Toast.LENGTH_SHORT).show();
                                    resetInitButtonName(buttons, buttonIds);
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.i(TAG, "cancel cleaning renames");
                        }
                    }).show();
                }
                break;
            default:
                Log.e(TAG, "invalid item id");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage (Message msg) { // 从Handler的消息队列中取出消息并执行
            switch (msg.arg1) {
                case 1:
                    Toast.makeText(MainActivity.this, "配置成功", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(MainActivity.this, "配置失败", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(MainActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    Toast.makeText(MainActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Log.e(TAG, "invalid msg arg1 " + msg.arg1);
                    break;
            }
        }
    };

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Log.v(TAG, "id " + id);

        if (buttonIds.contains(id)) {
            switch (mode) {
                case CONF_MODE:
                    Log.i(TAG, "request to " + ruffIrRecvUrl);
                    HttpPostThread httpConf = new HttpPostThread("recv", id);
                    httpConf.start();
                    break;
                case RENAME_MODE:
                    renameButton(id);
                    break;
                case NORMAL_MODE:
                    Log.i(TAG, "request to " + ruffIrSendUrl);
                    String irCode = getIrCode(id);
                    if (!irCode.equals("UNDEFINED")) {
                        HttpPostThread httpNormal = new HttpPostThread("send", R.id.button1, irCode);
                        httpNormal.start();
                    }
                    break;
                default:
                    Log.e(TAG, "invalid mode" + mode);
                    break;
            }
        }
    }

    private void renameButton(int buttonId) {
        LayoutInflater factory = LayoutInflater.from(MainActivity.this);
        View buttonRenameView = factory.inflate(R.layout.button_rename, null);
        final EditText etRename = (EditText)buttonRenameView.findViewById(R.id.rename_edit);

        final int _buttonId = buttonId;
        new AlertDialog.Builder(MainActivity.this)
            .setTitle("按键重命名")
            .setView(buttonRenameView)
            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String buttonNewName = etRename.getText().toString();
                    if (buttonNewName.equals("")) {
                        Log.i(TAG, "invalid null input");
                        return;
                    }

                    Button button = (Button) findViewById(_buttonId);
                    Log.v(TAG, "button " + _buttonId + " changes name as " + buttonNewName);

                    button.setText(buttonNewName);
                    SharedPreferences sp = getSharedPreferences(spNewButtonNames, MODE_PRIVATE);
                    Editor editor = sp.edit();
                    editor.putString(String.valueOf(_buttonId), buttonNewName);
                    editor.commit();
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(TAG, "cancel rename");
                }
            }).show();
    }

    private void updateButtonName(ArrayList<Integer> buttonIds) {
        for (int i = 0; i < buttonIds.size(); i++) {
            int buttonId = buttonIds.get(i);
            Button button = (Button) findViewById(buttonId);
            SharedPreferences sp = getSharedPreferences(spNewButtonNames, MODE_PRIVATE);
            String buttonNewName = sp.getString(String.valueOf(buttonId), "UNDEFINED");
            if (!buttonNewName.equals("UNDEFINED")) {
                button.setText(buttonNewName);
                Log.v(TAG, "button " + buttonId + " use new name " + buttonNewName);
            } else {
                Log.v(TAG, "button " + buttonId + " keep name undefined");
            }
        }
    }

    private void saveInitButtonName(ArrayList<Button> buttons, ArrayList<Integer> buttonIds) {
        Log.v(TAG, "save all button initial names");
        SharedPreferences sp = getSharedPreferences(spInitButtonNames, MODE_PRIVATE);
        Editor editor = sp.edit();
        for (int i = 0; i < buttonIds.size(); i++) {
            editor.putString(String.valueOf(buttonIds.get(i)), buttons.get(i).getText().toString());
        }
        editor.commit();
    }

    private void resetInitButtonName(ArrayList<Button> buttons, ArrayList<Integer> buttonIds) {
        Log.v(TAG, "reset all button names");
        SharedPreferences sp = getSharedPreferences(spNewButtonNames, MODE_PRIVATE);
        Editor editor = sp.edit();
        for (int i = 0; i < buttonIds.size(); i++) {
            editor.remove(String.valueOf(buttonIds.get(i)));
        }
        editor.commit();

        sp = getSharedPreferences(spInitButtonNames, MODE_PRIVATE);
        for (int i = 0; i < buttonIds.size(); i++) {
            String initName = sp.getString(String.valueOf(buttonIds.get(i)), "UNDEFINED");
            assert(!initName.equals("UNDEFINED"));
            buttons.get(i).setText(initName);
        }
    }

    private void updateButtonColor(ArrayList<Button> buttons, ArrayList<Integer> buttonIds, int mode) {
        if (mode == CONF_MODE) {
            for (int i = 0; i < buttons.size(); i++) {
                buttons.get(i).setBackgroundColor(getResources().getColor(R.color.orange));
            }
        } else if (mode == RENAME_MODE) {
            for (int i = 0; i < buttons.size(); i++) {
                buttons.get(i).setBackgroundColor(getResources().getColor(R.color.green));
            }
        } else {
            SharedPreferences sp = getSharedPreferences(spButtonIrCodes, MODE_PRIVATE);
            for (int i = 0; i < buttonIds.size(); i++) {
                String irCode = sp.getString(String.valueOf(buttonIds.get(i)), "UNDEFINED");
                if (!irCode.equals("UNDEFINED")) {
                    buttons.get(i).setBackgroundColor(getResources().getColor(R.color.blue));
                } else {
                    buttons.get(i).setBackgroundColor(getResources().getColor(R.color.gray));
                }
            }
        }
    }

    private void putIrCode(int buttonId, String irCode) {
        Log.v(TAG, "button " + buttonId + " put irCode" + irCode);
        SharedPreferences sp = getSharedPreferences(spButtonIrCodes, MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putString(String.valueOf(buttonId), irCode);
        editor.commit();
    }

    private String getIrCode(int buttonId) {
        SharedPreferences sp = getSharedPreferences(spButtonIrCodes, MODE_PRIVATE);
        String irCode = sp.getString(String.valueOf(buttonId), "UNDEFINED");
        Log.v(TAG, "button " + buttonId + " get irCode " + irCode);
        return irCode;
    }

    private void removeIrCode(ArrayList<Integer> buttonIds) {
        SharedPreferences sp = getSharedPreferences(spButtonIrCodes, MODE_PRIVATE);
        Editor editor = sp.edit();
        for (int i = 0; i < buttonIds.size(); i++) {
            editor.remove(String.valueOf(buttonIds.get(i)));
        }
        editor.commit();
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

            if (this.postType.equals("recv") || this.postType.equals("send")) {
                HttpPost httpPost = new HttpPost(this.postType.equals("recv") ? ruffIrRecvUrl : ruffIrSendUrl);

                try {
                    HttpEntity requestHttpEntity = new UrlEncodedFormEntity(pairList);
                    httpPost.setEntity(requestHttpEntity);
                    HttpResponse httpResponse = httpClient.execute(httpPost);
                    int statusCode = httpResponse.getStatusLine().getStatusCode();

                    if (statusCode == 200) {
                        Log.i(TAG, "request SUCCESS");
                        String recvData = getResponseEntity(httpResponse);
                        Message msg = handler.obtainMessage();
                        if (this.postType.equals("recv")) {
                            putIrCode(this.buttonId, recvData);
                            msg.arg1 = CONF_SUCCESS;
                            handler.sendMessage(msg);
                        } else {
                            msg.arg1 = SEND_SUCCESS;
                            handler.sendMessage(msg);
                        }
                    }
                } catch (Exception e) {
                    Log.i(TAG, "request FAILED");
                    Message msg = handler.obtainMessage();
                    msg.arg1 = this.postType.equals("recv") ? CONF_FAILURE : SEND_FAILURE;
                    handler.sendMessage(msg);
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