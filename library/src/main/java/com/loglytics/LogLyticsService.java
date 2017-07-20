package com.loglytics;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by Veniamin on 17/07/2017.
 */

public class LogLyticsService extends Service {

    private static final String TAG = LogLyticsService.class.getCanonicalName();
    private static final String processId = Integer.toString(android.os.Process
            .myPid());

    private boolean isRunning = true;

    public LogLyticsService(){
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        new Thread(new Runnable() {

            @Override
            public void run() {
                String startTime = "01-01 00:00:00.001";
                String time = "01-01 00:00:00.002";

                while (true) {
                    // Log.d(TAG, startTime.concat(": before"));
                    if(isRunning){

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

    private static String getLog(String startTime) {

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

                    //Log.i(TAG,line);

                    //String message = URLEncoder.encode(line, "UTF-8");
                    //String message = line;

                    try {
                        // instantiate the URL object with the target URL of the resource to
                        // request
                        JSONObject message = new JSONObject();
                        URL url = new URL("http://10.0.2.2:8080/");
                        message.put("message", line);
                        // instantiate the HttpURLConnection with the URL object - A new
                        // connection is opened every time by calling the openConnection
                        // method of the protocol handler for this URL.
                        // 1. This is the point where the connection is opened.
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        //connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        // set connection output to true
                        connection.setDoOutput(true);

                        // we're doing "POST" method
                        connection.setRequestMethod("POST");

                        // instantiate OutputStreamWriter using the output stream, returned
                        // from getOutputStream, that writes to this connection.
                        // 2. This is the point where you'll know if the connection was
                        // successfully established. If an I/O error occurs while creating
                        // the output stream, you'll see an IOException.
                        OutputStreamWriter writer = new OutputStreamWriter(
                                connection.getOutputStream());

                        // write data to the connection. This is data that you are sending
                        // to the server
                        writer.write(message.toString());

                        // Closes this output stream and releases any system resources
                        // associated with this stream. At this point, we've sent all the
                        // data. Only the outputStream is closed at this point, not the
                        // actual connection
                        writer.close();

                        // if there is a response code AND that response code is 200 OK, do
                        // stuff in the first if block
                        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            // OK
                            //Log.i(TAG, "sucess");

                            // otherwise, if any other status code is returned, or no status
                            // code is returned, do stuff in the else block
                        } else {
                            // Server returned HTTP error code.
                            Log.i(TAG, Integer.toString(connection.getResponseCode()));
                        }
                    } catch (ProtocolException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            //String[] clearCommand = new String[] { "logcat", "-c" };
            //Runtime.getRuntime().exec(clearCommand);
            //Log.i(TAG,"logcat clean");
        } catch (IOException ex) {
            Log.e(TAG, "getLog failed", ex);
        }

        return date[0].concat(" ").concat((date[1]));
    }
}
