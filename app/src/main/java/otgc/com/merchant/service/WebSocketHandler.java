package otgc.com.merchant.service;

import android.os.AsyncTask;
//import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

public class WebSocketHandler {

    private WebSocket mConnection = null;

    private WebSocketListener webSocketListener = null;

    private HashSet<String> sentMessageSet = new HashSet<String>();

    private Timer pingTimer = null;
    private final long pingInterval = 20000L;//ping every 20 seconds
    private final long pongTimeout = 5000L;//pong timeout after 5 seconds
    private boolean pingPongSuccess = false;

    public WebSocketHandler() {
    }

    public void start() {
        try {
            stop();
            connect();
            startPingTimer();

        } catch (IOException | com.neovisionaries.ws.client.WebSocketException e) {
            e.printStackTrace();
        }
    }

    public void stop() {

        stopPingTimer();

        if(mConnection != null && mConnection.isOpen()) {
            mConnection.disconnect();
        }
    }

    private void startPingTimer(){

        pingTimer = new Timer();
        pingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (mConnection != null) {
                    pingPongSuccess = false;
                    if(mConnection.isOpen())mConnection.sendPing();
                    startPongTimer();
                }
            }
        }, pingInterval, pingInterval);
    }

    private void stopPingTimer(){
        if(pingTimer != null) pingTimer.cancel();
    }

    private void startPongTimer(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (!pingPongSuccess) {
                    //ping pong unsuccessful after x seconds - restart connection
                    start();
                }
            }
        }, pongTimeout);
    }

    private void connect() throws IOException, WebSocketException
    {
        new ConnectionTask().execute();
    }

    public boolean isConnected(){
        return  mConnection != null && mConnection.isOpen();
    }

    public void addListener(WebSocketListener webSocketListener) {
        this.webSocketListener = webSocketListener;
    }

    private void send(String message) {
        //Make sure each message is only sent once per socket lifetime
        if(!sentMessageSet.contains(message)) {
            try {
                if (mConnection != null && mConnection.isOpen()) {
                    mConnection.sendText(message);
                    sentMessageSet.add(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
//            Log.d("WebSocketHandler", "Message sent already: "+message);
        }
    }

    public synchronized void subscribeToAddress(String address) {
        send("{\"op\":\"addr_sub\", \"addr\":\"" + address + "\"}");
    }

    private class ConnectionTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... args) {

            try {
                sentMessageSet.clear();

                mConnection = new WebSocketFactory()
                        .createSocket("wss://ws.blockchain.info/inv")
                        .addHeader("Origin", "https://blockchain.info").recreate()
                        .addListener(new WebSocketAdapter() {

                            @Override
                            public void onPongFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                                super.onPongFrame(websocket, frame);
                                pingPongSuccess = true;
                            }

                            public void onTextMessage(WebSocket websocket, String message) {

                                try {
                                    JSONObject jsonObject = null;
                                    try {
                                        jsonObject = new JSONObject(message);
                                    } catch (JSONException je) {
                                        jsonObject = null;
                                    }

                                    if (jsonObject == null) {
                                        return;
                                    }

                                    String op = (String) jsonObject.get("op");
                                    if (op.equals("utx") && jsonObject.has("x")) {

                                        JSONObject objX = (JSONObject) jsonObject.get("x");

                                        long value = 0L;

                                        String foundAddr = null;
                                        long txValue = 0L;
                                        if (objX.has("out")) {
                                            JSONArray outArray = (JSONArray) objX.get("out");
                                            JSONObject outObj = null;
                                            for (int j = 0; j < outArray.length(); j++) {
                                                outObj = (JSONObject) outArray.get(j);
                                                if(outObj.has("value")) {
                                                    value = outObj.getLong("value");
                                                }
                                                if(outObj.has("addr")) {
                                                    if(ExpectedIncoming.getInstance().getBTC().containsKey((String) outObj.get("addr"))) {
                                                        foundAddr = (String) outObj.get("addr");
                                                        txValue = value;
                                                        break;
                                                    }
                                                }
                                            }
                                        }

                                        if (txValue > 0L) {
                                            webSocketListener.onIncomingPayment(foundAddr, txValue);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                mConnection.connect();
            }
            catch(Exception e)	{
                e.printStackTrace();
            }

            return null;
        }
    }
}
