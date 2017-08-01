package io.loglytics;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

/**
 * Created by Veniamin on 18/07/2017.
 */

public class LoglyticsService extends Service {

    private static final String TAG = "LoglyticsService";
    private LoglyticsSender sender = new LoglyticsSender();
    private static final String processId = Integer.toString(android.os.Process.myPid());

    private String serverUrl = "";
    private String[] date;

    public LoglyticsService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        setUrl(intent);

        if (!serverUrl.isEmpty() && intent.hasExtra("token")){
            String id = Settings.Secure.getString(getApplicationContext().getContentResolver(),Settings.Secure.ANDROID_ID);
            String appName = getApplicationContext().getPackageName();

            sender.startSocket(serverUrl, intent.getStringExtra("token"), id, appName);

        }

        sender.socketConnection();

        // Starts a new thread that run in background,
        // obtains application log and then sleep during sleepTime specified
        new Thread(new Runnable() {

            @Override
            public void run() {
                String startTime = "01-01 00:00:00.001";
                String time = "01-01 00:00:00.002";
                int sleepTime = 3000;
                while (true) {

                    if (time.equals(startTime)){

                        pauseThread(sleepTime);
                        startTime = incrementTime(startTime);

                    }else {
                        time = getLog(startTime);
                        startTime = time;
                    }
                }
            }
        }).start();

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    private void pauseThread(int sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void setUrl(Intent intent){
        if (intent.hasExtra("serverURL")){
            this.serverUrl = intent.getStringExtra("serverURL");
        }else {
            //default emulator localhost url
            this.serverUrl = "http://10.0.2.2:8080";
        }
    }

    private String incrementTime(String startTime) {
        String lastCharTime = startTime.substring(startTime.length() - 3, startTime.length());
        int currentTime = Integer.valueOf(lastCharTime);
        startTime = startTime.substring(0, startTime.length() - 3);
        currentTime++;

        if (currentTime < 100) {
            lastCharTime = String.format("%03d", currentTime);
            startTime = startTime.concat(lastCharTime);
        }
        if (currentTime == 1000) {
            currentTime = 000;
            lastCharTime = Integer.toString(currentTime);
            startTime = startTime.concat(lastCharTime);
        }else {
            lastCharTime = Integer.toString(currentTime);
            startTime = startTime.concat(lastCharTime);
        }
        return startTime;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getLog(String startTime) {

        date = startTime.split("\\s+");
        String[] payload = new String[4];
        try {
            String[] command = new String[] { "logcat", "-t", startTime,  "-v", "time" };
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(processId)) {
                    date = line.split("\\s+");

                    payload[0] = date[0];
                    payload[1] = date[1];
                    payload[2] = line.substring(19, 20);
                    payload[3] = line.substring(21, line.length());

                    sender.sendMessage(payload);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return date[0].concat(" ").concat((date[1]));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sender.socketDisconnect();
    }
}