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

    public LoglyticsSender(){
    }

    public Socket getSocket(String url) {
        return socket;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void startSocket(String url, String token) {
        this.serverUrl = url;
        this.token = token;
        try{
            this.socket = IO.socket(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    public void socketConnection(){
        this.socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                // Sending an object
                JSONObject obj = new JSONObject();
                try {
                    obj.put("token", token);
                    socket.emit("start", obj);
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

    public void socketEmit(String url, JSONObject obj){
        this.socket.emit(url, obj);
    }

    public void socketDisconnect(){
        this.socket.disconnect();
    }

    public JSONObject setMessage(String[] payload) throws JSONException {
        JSONObject message = new JSONObject();

        message.put("token", this.token);
        message.put("day", payload[0]);
        message.put("time", payload[1]);
        message.put("level", payload[2]);
        message.put("message", payload[3]);

        return message;
    }

    public void sendMessage(String[] payload){
        try{
            socketEmit("log", setMessage(payload));
        }catch (JSONException e){
            Log.d(TAG, e.getMessage());
        }
    }

}
