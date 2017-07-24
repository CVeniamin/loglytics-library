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

    public LoglyticsSender(){
    }

    public Socket getSocket(String url) {
        return socket;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void startSocket(String url) throws URISyntaxException {
        this.serverUrl = url;
        this.socket = IO.socket(url);
    }
    public void socketConnection(){
        this.socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

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

        message.put("day", payload[0]);
        message.put("time", payload[1]);
        message.put("level", payload[2]);
        message.put("message", payload[3]);

        return message;
    }

    public void sendMessage(String[] payload){
        try{
            socketEmit("logData", setMessage(payload));
        }catch (JSONException e){
            Log.d(TAG, e.getMessage());
        }
    }

}
