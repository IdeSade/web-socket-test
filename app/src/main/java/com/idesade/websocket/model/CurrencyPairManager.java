package com.idesade.websocket.model;

import android.support.annotation.NonNull;

import com.idesade.websocket.WeakListenerList;
import com.idesade.websocket.WeakListenerList.ListenerRunnable;
import com.idesade.websocket.model.NetworkManager.NetworkReceiveListener;
import com.idesade.websocket.model.NetworkManager.NetworkSubscribeListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CurrencyPairManager implements NetworkSubscribeListener, NetworkReceiveListener {

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
        mNetworkManager.registerSubscribe(this);
        mNetworkManager.registerReceive(this);
    }

    @Override
    public void onSubscribed(@NonNull Set<CurrencyPairTick> ticks) {
        for (CurrencyPairTick tick : ticks) {
            addCurrencyPair(tick);
        }
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

    public void addCurrencyPair(@NonNull CurrencyPairTick tick) {
        CurrencyPair currencyPair = mTypeCurrencyPairMap.get(tick.getType());
        if (currencyPair == null) {
            currencyPair = new CurrencyPair(tick.getType(), mTypeCurrencyPairMap.size());
            currencyPair.setTick(tick);
            mTypeCurrencyPairMap.put(currencyPair.getType(), currencyPair);
            notifyAddPair(currencyPair);
        } else {
            currencyPair.setTick(tick);
            notifyUpdatePair(currencyPair);
        }
    }

    public void updateCurrencyPair(@NonNull CurrencyPairTick tick) {
        CurrencyPair currencyPair = mTypeCurrencyPairMap.get(tick.getType());
        if (currencyPair != null) {
            currencyPair.setTick(tick);
            notifyUpdatePair(currencyPair);
        }
    }

    public void removeCurrencyPair(@NonNull CurrencyPair currencyPair) {
        mTypeCurrencyPairMap.remove(currencyPair.getType());
        mNetworkManager.unsubscribe(currencyPair.getType());
        notifyRemovePair(currencyPair);
    }


}
