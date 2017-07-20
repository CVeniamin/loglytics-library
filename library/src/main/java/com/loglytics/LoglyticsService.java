package com.loglytics;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by Veniamin on 18/07/2017.
 */

public class LoglyticsService extends Service {

    private static final String TAG = "LoglyticsService";
    private Socket socket;
    private static final String processId = Integer.toString(android.os.Process
            .myPid());

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

        try {
            socket = IO.socket("http://10.0.2.2:8080");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "onStartCommand");

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                // Sending an object
                JSONObject obj = new JSONObject();
                try {
                    obj.put("hello", "from java client");
                    socket.emit("start", obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }}).on("event", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
            }

        });

        socket.connect();
        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        new Thread(new Runnable() {

            @Override
            public void run() {
                String startTime = "01-01 00:00:00.001";
                String time = "01-01 00:00:00.002";

                while (true) {
                    // Log.d(TAG, startTime.concat(": before"));

                    if (time.equals(startTime)){
                        try {
                            Thread.sleep(3000);

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
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else {
                        /*Log.i(TAG, "Service running");
                        String lastCharTime = startTime.substring(startTime.length() - 3, startTime.length());
                        int currentTime = Integer.valueOf(lastCharTime);
                        currentTime++;

                        if (currentTime < 100) {
                            lastCharTime = String.format("%03d", currentTime);
                        }

                        if (currentTime == 1000) {
                            currentTime = 000;
                            lastCharTime = Integer.toString(currentTime);
                        }*/

                        //startTime = startTime.substring(0, startTime.length() - 3);
                        //startTime = startTime.concat(lastCharTime);
                        time = getLog(startTime);
                        startTime = time;

                        //Stop service once it finishes its task
                        //stopSelf();
                    }
                }
            }
        }).start();

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getLog(String startTime) {

        StringBuilder builder = new StringBuilder();
        String[] date = startTime.split("\\s+");
        //Log.d(TAG,startTime.concat(" :starting Time"));
        try {
            //String[] command = new String[] { "logcat", "-t", "20", "-v", "threadtime" };
            String[] command = new String[] { "logcat", "-t", startTime,  "-v", "threadtime" };
            Process process = Runtime.getRuntime().exec(command);

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(processId)) {
                    builder.append(line);
                    String getLine = line;
                    date  =  line.split("\\s+");

                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("payload", line);
                        socket.emit("foo", obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            //String[] clearCommand = new String[] { "logcat", "-c" };
            //Runtime.getRuntime().exec(clearCommand);
            //Log.i(TAG,"logcat clean");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return date[0].concat(" ").concat((date[1]));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }
}