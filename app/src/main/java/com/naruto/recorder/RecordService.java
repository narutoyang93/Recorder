package com.naruto.recorder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.text.format.DateFormat;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_DCIM;

public class RecordService extends Service {
    public static final int NOTIFICATION_ID = 1;
    private static final String SAVE_FOLDER = Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM).getAbsolutePath() + "/sound record/";
    private RecordBinder binder = new RecordBinder();
    private MediaRecorder mediaRecorder;
    private String fileName;
    private static final String TAG = "RecordService";

    public RecordService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final String CHANNEL_ID = getPackageName() + ".notification.channel";
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //创建通知渠道
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "渠道的名字", importance);
            channel.setDescription("渠道描述");
            channel.setSound(null, null);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(channel);
        }
        //创建通知
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaRecorder != null) binder.stop();
    }

    /**
     * @Purpose
     * @Author Naruto Yang
     * @CreateDate 2020/2/13 0013
     * @Note
     */
    class RecordBinder extends Binder {
        private ChangeUI changeUI;

        public void setChangeUI(ChangeUI changeUI) {
            this.changeUI = changeUI;
        }

        public void start() {
            if (mediaRecorder == null) mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置麦克风
            /*
             * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
             * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
             */
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
            /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setAudioChannels(2);
            mediaRecorder.setAudioEncodingBitRate(160000);
            mediaRecorder.setAudioSamplingRate(48000);
            fileName = DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA)) + ".m4a";
            /* ③准备 */
            mediaRecorder.setOutputFile(SAVE_FOLDER + fileName);
            File folder = new File(SAVE_FOLDER);
            if (!folder.exists()) folder.mkdirs();
            boolean b = true;
            try {
                mediaRecorder.prepare();
                /* ④开始 */
                mediaRecorder.start();
            } catch (IOException e) {
                b = false;
                e.printStackTrace();
            }
            if (b)
                changeUI.changeUI(MainActivity.STATE_RECORDING);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void pause() {
            mediaRecorder.pause();
            changeUI.changeUI(MainActivity.STATE_PAUSE);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void resume() {
            mediaRecorder.resume();
            changeUI.changeUI(MainActivity.STATE_RECORDING);
        }

        public void stop() {
            boolean b = true;

            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            } catch (IllegalStateException e) {
                e.printStackTrace();
                b = false;
                changeUI.showDialog("保存异常");
                mediaRecorder.reset();
                mediaRecorder.release();
                mediaRecorder = null;
            }

            if (b) {
                Toast.makeText(RecordService.this, "保存成功", Toast.LENGTH_SHORT).show();
                changeUI.changeUI(MainActivity.STATE_READY);
            }
        }
    }

    /**
     * @Purpose
     * @Author Naruto Yang
     * @CreateDate 2020/2/13 0013
     * @Note
     */
    interface ChangeUI {
        void changeUI(int state);

        void showDialog(String message);
    }
}
