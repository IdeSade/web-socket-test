package com.idesade.websocket.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.NonNull;

import com.idesade.websocket.MainApp;
import com.idesade.websocket.WeakListenerList;
import com.idesade.websocket.WeakListenerList.ListenerRunnable;
import com.idesade.websocket.model.NetworkManager.NetworkReceiveListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CurrencyPairManager implements NetworkReceiveListener {

    public static final String PREF_NAME = "CurrentState";
    public static final String PREF_SET = "CurrencyPairSet";

    public interface CurrencyPairListener {
        void onAddPair(@NonNull CurrencyPair currencyPair);

        void onUpdatePair(@NonNull CurrencyPair currencyPair);

        void onRemovePair(@NonNull CurrencyPair currencyPair);
    }

    private final NetworkManager mNetworkManager;
    private final Map<CurrencyPairType, CurrencyPair> mTypeCurrencyPairMap = new HashMap<>();

    private final WeakListenerList<CurrencyPairListener> mListeners = new WeakListenerList<>();

    public CurrencyPairManager(NetworkManager networkManager) {
        mNetworkManager = networkManager;
        mNetworkManager.registerReceive(this);
    }

    @Override
    public void onReceived(@NonNull List<CurrencyPairTick> ticks) {
        for (CurrencyPairTick tick : ticks) {
            updateCurrencyPair(tick);
        }
    }

    public void register(CurrencyPairListener listener) {
        mListeners.register(listener);
    }

    public void notifyAddPair(@NonNull final CurrencyPair currencyPair) {
        mListeners.forEach(new ListenerRunnable<CurrencyPairListener>() {
            @Override
            public void run(@NonNull CurrencyPairListener listener) {
                listener.onAddPair(currencyPair);
            }
        });
    }

    public void notifyUpdatePair(@NonNull final CurrencyPair currencyPair) {
        mListeners.forEach(new ListenerRunnable<CurrencyPairListener>() {
            @Override
            public void run(@NonNull CurrencyPairListener listener) {
                listener.onUpdatePair(currencyPair);
            }
        });

    }

    public void notifyRemovePair(@NonNull final CurrencyPair currencyPair) {
        mListeners.forEach(new ListenerRunnable<CurrencyPairListener>() {
            @Override
            public void run(@NonNull CurrencyPairListener listener) {
                listener.onRemovePair(currencyPair);
            }
        });
    }

    public void addCurrencyPair(@NonNull CurrencyPairType type) {
        CurrencyPair currencyPair = mTypeCurrencyPairMap.get(type);
        if (currencyPair == null) {
            currencyPair = new CurrencyPair(type, mTypeCurrencyPairMap.size());
            mTypeCurrencyPairMap.put(currencyPair.getType(), currencyPair);
            mNetworkManager.subscribe(CurrencyPairType.asSet(type));
            notifyAddPair(currencyPair);
        }
    }

    public void updateCurrencyPair(@NonNull CurrencyPairTick tick) {
        CurrencyPair currencyPair = mTypeCurrencyPairMap.get(tick.getType());
        if (currencyPair != null) {
            currencyPair.setTick(tick);
            notifyUpdatePair(currencyPair);
        }
    }

    public void removeCurrencyPair(@NonNull CurrencyPairType type) {
        CurrencyPair currencyPair = mTypeCurrencyPairMap.remove(type);
        mNetworkManager.unsubscribe(CurrencyPairType.asSet(type));
        notifyRemovePair(currencyPair);
    }

    private Set<String> getStringSet() {
        Set<String> stringSet = new HashSet<>();
        for (CurrencyPair currencyPair : mTypeCurrencyPairMap.values()) {
            stringSet.add(currencyPair.toJSONObject().toString());
        }
        return stringSet;
    }

    public Set<CurrencyPairType> getCurrencyPairTypeSet() {
        return Collections.unmodifiableSet(mTypeCurrencyPairMap.keySet());
    }

    public void saveState() {
        Editor editor = MainApp.getMainApp().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit();
        editor.putStringSet(PREF_SET, getStringSet());
        editor.apply();
    }

    public void loadState() {
        mTypeCurrencyPairMap.clear();

        SharedPreferences sp = MainApp.getMainApp().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> stringSet = sp.getStringSet(PREF_SET, new HashSet<String>());
        for (String s : stringSet) {
            CurrencyPair pair = CurrencyPair.fromJSONString(s);
            if (pair != null) {
                mTypeCurrencyPairMap.put(pair.getType(), pair);
                notifyAddPair(pair);
            }
        }

        mNetworkManager.subscribe(getCurrencyPairTypeSet());
    }

    public void clear() {
        Set<CurrencyPairType> clearSet = new HashSet<>(mTypeCurrencyPairMap.keySet());
        for (CurrencyPairType type : clearSet) {
            removeCurrencyPair(type);
        }
    }
}
