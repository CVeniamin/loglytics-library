package io.loglytics;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import java.util.Scanner;

/**
 * Created by Veniamin on 18/07/2017.
 */

public class LoglyticsService extends Service {

    private static final String TAG = "LoglyticsService";

    /**
     *Creates a new instance of sender
     * */
    private LoglyticsSender sender = new LoglyticsSender();

    /**
     * Used to check if the log is from application running the library
     * */
    private static final String processId = Integer.toString(android.os.Process.myPid());

    /**
     *Server URL where log data should be sent
     * */
    private String serverUrl;

    /**
     *Used to store most recent log time from last invocation of getLog()
     * */
    private String[] recentTime;

    /**
    * Thread sleep interval (3s)
    */
    private final int SLEEP_TIME = 3000;

    private static Intent intentService;

    public LoglyticsService() {
        super();
    }

    /**
     * Public methods used to start LoglyticsService
     * Must be called from onCreate() when a user wants to use the library
     * Use start(Context context) when you want to send logs to localhost
     * Use start(Context context, String url) when you want to send log to another server
     * */
    public static void start(Context context){
        LoglyticsService.start(context, null);
    }

    public static void start(Context context, String url){
        intentService = new Intent(context, LoglyticsService.class);
        String token = getToken(context);
        if (token != null) {
            intentService.putExtra("token", token);
        }
        if(url != null && !url.isEmpty()){
            intentService.putExtra("serverURL", url);
        }
        context.startService(intentService);
    }

    /**
     * This method runs when LoglyticsService was created
     * At this moment only calls parent constructor
     * */
    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Method runs when LoglyticsService started
     * Receives an Intent from application MainActivity with a token and a url
     * Creates a new Thread that will run in background until application gets killed
     * Inside Thread getLog method will continuously obtain application log
     * */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        setUrl(intent);

        if (!serverUrl.isEmpty() && intent.hasExtra("token")){
            String id = Settings.Secure.getString(getApplicationContext().getContentResolver(),Settings.Secure.ANDROID_ID);
            String appName = getApplicationContext().getPackageName();
            sender.startSocket(serverUrl, intent.getStringExtra("token"), id, appName);
        }

        sender.socketConnection();

        new Thread(new Runnable() {

            @Override
            public void run() {
                String startTime = "01-01 00:00:00.001";
                String nextTime = "01-01 00:00:00.002";

                while (true) {
                    if (nextTime.equals(startTime)){
                        pauseThread();
                        startTime = incrementTime(startTime);
                    }else {
                        nextTime = getLog(startTime);
                        startTime = nextTime;
                    }
                }
            }
        }).start();

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    /**
     * Method used to pause the Thread for SLEEP_TIME interval
     * */
    private void pauseThread() {
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method assigns serverUrl obtained from MainActivity Intent
     * If Intent hasn't a "serverURL" extra
     * Then default (emulator localhost) "http://10.0.2.2:8080" is used
     * */
    private void setUrl(Intent intent){
        if (intent.hasExtra("serverURL")){
            this.serverUrl = intent.getStringExtra("serverURL");
        }else {
            this.serverUrl = "http://10.0.2.2:8080";
        }
    }

    /**
     * Reads the token from  AndroidManifest.xml <meta-data> tag
     * This tag must be created inside host application AndroidManifest.xml
     * */
    private static String getToken(Context context){
        String token = null;
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            token = bundle.getString("io.loglytics.token");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return token;
    }

    /**
     * Method used to increment log time
     * Obtained from last execution of getLog()
     * */
    private String incrementTime(String startTime) {

        //get last three chars from startTime
        String lastCharTime = startTime.substring(startTime.length() - 3, startTime.length());

        //create a substring without last three chars
        startTime = startTime.substring(0, startTime.length() - 3);

        //get value from last three chars, e.g., "009" => 9
        int currentTime = Integer.valueOf(lastCharTime);

        //increment 9 to become 10
        currentTime++;

        if (currentTime < 100) {
            //reformat 10 to "010"
            lastCharTime = String.format("%03d", currentTime);
        }else{
            //case gets to 1000 restart to 000
            if (currentTime == 1000) {
                currentTime = 000;
            }
            lastCharTime = Integer.toString(currentTime);
        }

        //reconstructs startTime with incremented last three chars
        startTime = startTime.concat(lastCharTime);

        return startTime;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Method reads log using logcat command from time specified as argument
     * Obtained log is parsed into a payload and sent using LoglyticsSender to remote server
     * Scanner uses a specific delimiter
     * in order to verify if log is a pre-determined format
     * Right now verifies log date and time
     * */
    private String getLog(String startTime) {
        recentTime = startTime.split("\\s+"); //fallback assignment in case there isn't new log
        try {
            String[] command = new String[] { "logcat", "-t", startTime,  "-v", "long","-v", "year"};

            Process process = Runtime.getRuntime().exec(command);
            Scanner scanner = new Scanner(new InputStreamReader(process.getInputStream()));

            scanner.useDelimiter("(.*?)(\\[)((?=(\\s.*(\\d{4}\\-\\d{2}\\-\\d{2})(\\s)((\\d+\\:){2})(\\d{2}\\.\\d{3}))))");

            while (scanner.hasNext()) {
                String line = scanner.next();
                if (!line.isEmpty() && !line.contains("--------- beginning of") ){
                    String[] payload = parseLine(line);
                    recentTime[0] =  payload[0];
                    recentTime[1] =  payload[1];
                    sender.sendMessage(payload);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            String[] clearCommand = new String[] { "logcat", "-c" };
            try {
                Runtime.getRuntime().exec(clearCommand);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return recentTime[0] + " " + recentTime[1];
    }

    /**
     * This method parses a line coming from Scanner that reads logs
     * Splits line by "[" separator
     * Allowing to return date, time, level and message
     * A line example could be:
     *"--------- beginning of main
     *[ 08-10 10:22:22.806 25519:25526 I/art      ]
     *Starting a blocking GC Instrumentation"
     */
    private String[] parseLine(String line) {
        String[] payload = new String[4];

        String[] data = line.split("\\]");
        String[] levelSender = data[0].split("\\/");
        String level = levelSender[0].substring(levelSender[0].length() - 1, levelSender[0].length());
        String message = levelSender[1] + ": " + data[1].trim();

        String[] aux_payload = data[0].split("\\s+");
        payload[0] = aux_payload[1];
        payload[1] = aux_payload[2];
        payload[2] = level;
        payload[3] = message;

        return payload;
    }

    /**
     * Used to disconnect socket when service is destroyed
     * */
    @Override
    public void onDestroy() {
        super.onDestroy();
        sender.socketDisconnect();
    }

    /**
     * Method used to stop the service from user side
     * Must be called from inside Activity onDestroy() method
     * */
    public static void stop(Context context){
        context.stopService(intentService);
    }
}