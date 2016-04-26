package com.idesade.websocket;

import android.app.Application;

import com.idesade.websocket.model.CurrencyPair;
import com.idesade.websocket.model.CurrencyPairManager;
import com.idesade.websocket.model.NetworkManager;

public class MainApp extends Application {

    private static MainApp mMainApp;
    private static NetworkManager mNetworkManager;
    private static CurrencyPairManager mCurrencyPairManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mMainApp = this;
        mNetworkManager = new NetworkManager();
        mCurrencyPairManager = new CurrencyPairManager(mNetworkManager);
    }

    public static MainApp getMainApp() {
        return mMainApp;
    }

    public static NetworkManager getNetworkManager() {
        return mNetworkManager;
    }

    public static CurrencyPairManager getCurrencyPairManager() {
        return mCurrencyPairManager;
    }
}
