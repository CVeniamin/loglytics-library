package io.loglytics;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by Veniamin on 21/07/2017.
 */

public class LoglyticsSender {
    private String TAG = "LoglyticsSender";
    private Socket socket;
    private String serverUrl;
    private String token;
    private String id;
    private String app;

    protected LoglyticsSender(){}

    private Socket getSocket(String url) {
        return socket;
    }

    private String getServerUrl() {
        return serverUrl;
    }

    /**
     * Method used to create a new socket and assignment of class attributes
     * */
    protected void startSocket(String url, String token, String id, String app) {
        this.serverUrl = url;
        this.token = token;
        this.id = id;
        this.app = app;
        try{
            this.socket = IO.socket(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Used to create a new connection
     * Upon established connection send device info to remote server
     * */
    protected void socketConnection(){
        this.socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                // Sending an object containing token and information about device
                JSONObject obj = new JSONObject();
                String kernel = System.getProperty("os.version"); // OS version
                String sdk = android.os.Build.VERSION.RELEASE;
                String device = android.os.Build.DEVICE;
                String product = android.os.Build.PRODUCT;
                String model = android.os.Build.MODEL;
                String brand = android.os.Build.BRAND;
                try {
                    obj.put("id", id);
                    obj.put("token", token);
                    obj.put("app", app);
                    obj.put("kernel", kernel);
                    obj.put("sdk", sdk);
                    obj.put("device", device);
                    obj.put("product", product);
                    obj.put("model", model);
                    obj.put("brand", brand);
                    socket.emit("startAndroid", obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                socket.connect();
            }
        });

        this.socket.connect();
    }

    /**
     * Stub method used to send JSONObject to a server defined by url
     * */
    private void socketEmit(String url, JSONObject obj){
        this.socket.emit(url, obj);
    }

    /**
     * Stub method used to disconnect a socket
     * */
    protected void socketDisconnect(){
        this.socket.disconnect();
    }

    /**
     * Method used to create a JSONObject from payload
     * */
    private JSONObject setMessage(String[] payload) throws JSONException {
        JSONObject message = new JSONObject();

        message.put("token", this.token);
        message.put("id", this.id);
        message.put("app", this.app);
        message.put("date", payload[0]);
        message.put("time", payload[1]);
        message.put("level", payload[2]);
        message.put("message", payload[3]);

        return message;
    }

    /**
     * Method used to send a JSONObject message to server
     * */
    protected void sendMessage(String[] payload){
        try{
            socketEmit("log", setMessage(payload));
        }catch (JSONException e){
            Log.d(TAG, e.getMessage());
        }
    }

}
