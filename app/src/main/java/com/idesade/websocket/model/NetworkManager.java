package com.idesade.websocket.model;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.idesade.websocket.WeakListenerList;
import com.idesade.websocket.WeakListenerList.ListenerRunnable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketCall;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;

public class NetworkManager {

    public interface NetworkSubscribeListener {
        void onSubscribed(@NonNull Set<CurrencyPairTick> ticks);
    }

    public interface NetworkReceiveListener {
        void onReceived(@NonNull List<CurrencyPairTick> ticks);
    }

    private static final String LOG_TAG = NetworkManager.class.getSimpleName();

    private static final String JSON_SUBSCRIBED_COUNT = "subscribed_count";
    private static final String JSON_SUBSCRIBED_LIST = "subscribed_list";
    private static final String JSON_TICKS = "ticks";

    private final OkHttpClient mClient;
    private final Request mRequest;

    private WebSocket mWebSocket;
    private Set<CurrencyPairType> mCurrencyPairs = new HashSet<>();

    private final WeakListenerList<NetworkSubscribeListener> mSubscribeListeners = new WeakListenerList<>();
    private final WeakListenerList<NetworkReceiveListener> mReceiveListeners = new WeakListenerList<>();

    public NetworkManager() {
        mClient = new OkHttpClient();
        mRequest = new Request.Builder()
                .url("wss://quotes.exness.com:18400/")
                .build();
    }

    public void subscribe(@NonNull Set<CurrencyPairType> currencyPairs) {
        if (mWebSocket == null) {
            createWebSocketCall(currencyPairs);
        } else {
            final Set<CurrencyPairType> subscribeSet = new HashSet<>(currencyPairs);
            for (CurrencyPairType currencyPair : currencyPairs) {
                if (mCurrencyPairs.contains(currencyPair)) {
                    subscribeSet.remove(currencyPair);
                }
            }
            mCurrencyPairs.addAll(subscribeSet);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    sendMessage("SUBSCRIBE: %s", subscribeSet);
                }
            });
        }
    }

    public void unsubscribe(@NonNull CurrencyPairType currencyPairType) {
        Set<CurrencyPairType> types = new HashSet<>();
        types.add(currencyPairType);
        unsubscribe(types);
    }

    public void unsubscribe(@NonNull Set<CurrencyPairType> currencyPairs) {
        if (mWebSocket != null) {
            final Set<CurrencyPairType> unsubscribeSet = new HashSet<>(currencyPairs.size());
            for (CurrencyPairType currencyPair : currencyPairs) {
                if (mCurrencyPairs.contains(currencyPair)) {
                    unsubscribeSet.add(currencyPair);
                }
            }
            mCurrencyPairs.removeAll(unsubscribeSet);
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    sendMessage("UNSUBSCRIBE: %s", unsubscribeSet);
                }
            });
        }
    }

    public void registerSubscribe(NetworkSubscribeListener listener) {
        mSubscribeListeners.register(listener);
    }

    public void registerReceive(NetworkReceiveListener listener) {
        mReceiveListeners.register(listener);
    }

    public void notifySubscribed(@NonNull final Set<CurrencyPairTick> ticks) {
        mSubscribeListeners.forEach(new ListenerRunnable<NetworkSubscribeListener>() {
            @Override
            public void run(@NonNull NetworkSubscribeListener listener) {
                listener.onSubscribed(ticks);
            }
        });
    }

    public void notifyReceived(@NonNull final List<CurrencyPairTick> ticks) {
        mReceiveListeners.forEach(new ListenerRunnable<NetworkReceiveListener>() {
            @Override
            public void run(@NonNull NetworkReceiveListener listener) {
                listener.onReceived(ticks);
            }
        });
    }

    private void createWebSocketCall(final Set<CurrencyPairType> currencyPairs) {
        WebSocketCall call = WebSocketCall.create(mClient, mRequest);
        call.enqueue(new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(LOG_TAG, "onOpen: " + response.toString());
                mWebSocket = webSocket;
                subscribe(currencyPairs);
            }

            @Override
            public void onFailure(IOException e, Response response) {
                Log.e(LOG_TAG, "onFailure: " + (response != null ? response.toString() : ""), e);
                mWebSocket = null;
                subscribe(mCurrencyPairs);
            }

            @Override
            public void onMessage(ResponseBody responseBody) throws IOException {
                String body = responseBody.string();
                responseBody.close();
                Log.d(LOG_TAG, "onMessage: " + body);
                try {
                    JSONObject jsonBody = new JSONObject(body);
                    int count = jsonBody.optInt(JSON_SUBSCRIBED_COUNT, -1);
                    if (count > 0) {
                        JSONObject list = jsonBody.getJSONObject(JSON_SUBSCRIBED_LIST);
                        List<CurrencyPairTick> ticks = ticksFromJson(list.getJSONArray(JSON_TICKS));
                        notifySubscribed(new HashSet<>(ticks));
                    } else if (count < 0) {
                        notifyReceived(ticksFromJson(jsonBody.getJSONArray(JSON_TICKS)));
                    } else {
                        if (mWebSocket != null && mCurrencyPairs.size() == 0) {
                            mWebSocket.close(1000, "Close");
                            mWebSocket = null;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPong(Buffer buffer) {
                Log.d(LOG_TAG, "onPong: " + buffer.readString(Charset.defaultCharset()));
            }

            @Override
            public void onClose(int i, String s) {
                Log.d(LOG_TAG, "onClose: i:" + i + " s: " + s);
                mWebSocket = null;
            }
        });
    }

    private List<CurrencyPairTick> ticksFromJson(@NonNull JSONArray jsonArray) throws JSONException {
        List<CurrencyPairTick> ticks = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            CurrencyPairTick tick = CurrencyPairTick.fromJSONObject(jsonArray.getJSONObject(i));
            if (mCurrencyPairs.contains(tick.getType())) {
                ticks.add(tick);
            }
        }
        return ticks;
    }

    private void sendMessage(@NonNull String template, @NonNull Set<CurrencyPairType> currencyPairs) {
        if (currencyPairs.size() > 0) {
            try {
                String message = String.format(template, setToString(currencyPairs));
                Log.d(LOG_TAG, "sendMessage: " + message);
                mWebSocket.sendMessage(RequestBody.create(WebSocket.TEXT, message));
            } catch (IOException e) {
                Log.e(LOG_TAG, "sendMessage", e);
                e.printStackTrace();
            }
        }
    }

    @NonNull
    private String setToString(@NonNull Set<CurrencyPairType> set) {
        StringBuilder sb = new StringBuilder(set.size() * 7);
        for (CurrencyPairType currencyPair : set) {
            sb.append(currencyPair).append(",");
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}
