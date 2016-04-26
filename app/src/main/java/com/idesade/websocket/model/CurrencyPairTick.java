package com.idesade.websocket.model;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public class CurrencyPairTick {

    private final CurrencyPairType mType;

    private String mB;
    private int mBf;
    private String mA;
    private int mAf;
    private String mSpr;

    public CurrencyPairTick(CurrencyPairType type) {
        mType = type;
    }

    public CurrencyPairType getType() {
        return mType;
    }

    public String getB() {
        return mB;
    }

    public void setB(String b) {
        mB = b;
    }

    public int getBf() {
        return mBf;
    }

    public void setBf(int bf) {
        mBf = bf;
    }

    public String getA() {
        return mA;
    }

    public void setA(String a) {
        mA = a;
    }

    public int getAf() {
        return mAf;
    }

    public void setAf(int af) {
        mAf = af;
    }

    public String getSpr() {
        return mSpr;
    }

    public void setSpr(String spr) {
        mSpr = spr;
    }

    public static CurrencyPairTick fromJSON(@NonNull JSONObject jsonObject) throws JSONException {
        CurrencyPairTick tick = new CurrencyPairTick(CurrencyPairType.valueOf(jsonObject.getString("s")));
        tick.mB = jsonObject.getString("b");
        tick.mBf = jsonObject.getInt("bf");
        tick.mA = jsonObject.getString("a");
        tick.mAf = jsonObject.getInt("af");
        tick.mSpr = jsonObject.getString("spr");
        return tick;
    }

    @Override
    public String toString() {
        return "CurrencyPairTick{" +
                "mType=" + mType +
                ", mB='" + mB + '\'' +
                ", mBf=" + mBf +
                ", mA='" + mA + '\'' +
                ", mAf=" + mAf +
                ", mSpr='" + mSpr + '\'' +
                '}';
    }
}
