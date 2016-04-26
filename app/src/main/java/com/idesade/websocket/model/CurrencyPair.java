package com.idesade.websocket.model;

import android.support.annotation.NonNull;
import android.util.Log;

public class CurrencyPair {

    private static final String LOG_TAG = CurrencyPair.class.getSimpleName();

    private int mSortIndex;
    private final CurrencyPairType mType;
    private CurrencyPairTick mTick;

    public CurrencyPair(@NonNull CurrencyPairType type, int sortIndex) {
        mType = type;
        mSortIndex = sortIndex;
    }

    public CurrencyPairType getType() {
        return mType;
    }

    public void setTick(@NonNull CurrencyPairTick tick) {
        if (mType == tick.getType()) {
            mTick = tick;
        } else {
            Log.e(LOG_TAG, "setTick: this.Type != tick.Type");
        }
    }

    public CurrencyPairTick getTick() {
        return mTick;
    }

    public int getSortIndex() {
        return mSortIndex;
    }

    public void setSortIndex(int sortIndex) {
        mSortIndex = sortIndex;
    }

    @Override
    public String toString() {
        return mType + ": (" + mTick + ")";
    }
}
