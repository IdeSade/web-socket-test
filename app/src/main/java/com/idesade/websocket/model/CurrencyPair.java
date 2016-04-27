package com.idesade.websocket.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

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

    public JSONObject toJSONObject() {
        JSONObject json = new JSONObject();
        try {
            json.put("type", mType.name());
            json.put("sortIndex", mSortIndex);
            json.put("tick", mTick.toJSONObject());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    @Nullable
    public static CurrencyPair fromJSONObject(@NonNull JSONObject jsonObject) {
        try {
            CurrencyPairType type = CurrencyPairType.valueOf(jsonObject.getString("type"));
            int sortIndex = jsonObject.getInt("sortIndex");
            CurrencyPair currencyPair = new CurrencyPair(type, sortIndex);

            CurrencyPairTick tick = CurrencyPairTick.fromJSONObject(jsonObject.getJSONObject("tick"));
            currencyPair.setTick(tick);

            return currencyPair;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static CurrencyPair fromJSONString(@NonNull String jsonString) {
        try {
            return fromJSONObject(new JSONObject(jsonString));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return mType + ": (" + mTick + ")";
    }
}
