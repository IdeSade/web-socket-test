package com.idesade.websocket.model;

public enum CurrencyPairType {

    EURUSD("EUR/USD"),
    EURGBP("EUR/GBP"),
    USDJPY("USD/JPY"),
    GBPUSD("GBP/USD"),
    USDCHF("USD/CHF"),
    USDCAD("USD/CAD"),
    AUDUSD("AUD/USD"),
    EURJPY("EUR/JPY"),
    EURCHF("EUR/CHF");

    private String mDisplayName;

    CurrencyPairType(String displayName) {
        mDisplayName = displayName;
    }

    public String getDisplayName() {
        return mDisplayName;
    }
}
