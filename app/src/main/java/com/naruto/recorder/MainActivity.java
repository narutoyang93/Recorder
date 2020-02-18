package com.naruto.recorder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static final int STATE_READY = 0;//就绪
    public static final int STATE_RECORDING = 1;//录音中
    public static final int STATE_PAUSE = 2;//暂停

    public static final int REQUEST_CODE_PERMISSION = 100;

    private Button startBtn;
    private LinearLayout linearLayout;

    private int state = 0;//状态
    private RecordService.RecordBinder binder;
    private ServiceConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null)
            state = savedInstanceState.getInt("state", 0);
        //开始按钮点击监听
        linearLayout = findViewById(R.id.ll_1);
        startBtn = findViewById(R.id.btn_start);
        //搞版本才有暂停功能
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            linearLayout.getChildAt(0).setVisibility(View.VISIBLE);
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (RecordService.RecordBinder) service;
                binder.setChangeUI(new RecordService.ChangeUI() {
                    @Override
                    public void changeUI(int state) {
                        changeState(state);
                    }

                    @Override
                    public void showDialog(String message) {
                        new AlertDialog.Builder(MainActivity.this).setMessage(message)
                                .setPositiveButton("确定", null)
                                .show();
                    }
                });
                binder.start();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
    }

    /**
     * 开始
     *
     * @param view
     */
    public void start(View view) {
        //检查权限
        if (!MyTool.checkPermissions(this, REQUEST_CODE_PERMISSION
                , new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}))
            return;
        Intent intent = new Intent(this, RecordService.class);
/*        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }*/
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    /**
     * 暂停
     *
     * @param view
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void pauseOrResume(View view) {
        if (state == STATE_RECORDING) {//暂停
            binder.pause();
        } else {//继续
            binder.resume();
        }
    }

    /**
     * 完成
     *
     * @param view
     */
    public void complete(View view) {
        binder.stop();
        unbindService(connection);
//        stopService(new Intent(this, RecordService.class));
        changeState(STATE_READY);
    }

    /**
     * 切换状态
     *
     * @param newState
     */
    private void changeState(int newState) {
        if (newState != state) {
            if (newState == STATE_READY) {
                startBtn.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.GONE);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Button button = (Button) linearLayout.getChildAt(0);
                    button.setText(newState == STATE_PAUSE ? "继续" : "暂停");
                }
                startBtn.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);
            }
            state = newState;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION:
                MyTool.permissionRequestCallBack(grantResults, new MyTool.OperationInterface() {
                    @Override
                    public void done(Object o) {
                        start(null);
                    }
                }, new MyTool.OperationInterface() {
                    @Override
                    public void done(Object o) {
                        Toast.makeText(MainActivity.this, "授权失败", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("state", state);
    }
}
