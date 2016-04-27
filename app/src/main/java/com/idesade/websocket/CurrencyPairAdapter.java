package com.idesade.websocket;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.idesade.websocket.CurrencyPairAdapter.CurrencyPairViewHolder;
import com.idesade.websocket.ItemTouchHelperCallback.ItemTouchHelperAdapter;
import com.idesade.websocket.model.CurrencyPair;
import com.idesade.websocket.model.CurrencyPairManager;
import com.idesade.websocket.model.CurrencyPairManager.CurrencyPairListener;
import com.idesade.websocket.model.CurrencyPairType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CurrencyPairAdapter extends Adapter<CurrencyPairViewHolder> implements
        CurrencyPairListener, ItemTouchHelperAdapter {

    private static final String LOG_TAG = CurrencyPairAdapter.class.getSimpleName();

    private final SortedList<CurrencyPair> mCurrencyPairList;

    private final CurrencyPairManager mCurrencyPairManager;
    private int mCurrentSelectedPos = -1;

    private final Map<CurrencyPairType, CurrencyPair> mAddMap = new ConcurrentHashMap<>();
    private final Map<CurrencyPairType, CurrencyPair> mUpdateMap = new ConcurrentHashMap<>();
    private final Map<CurrencyPairType, CurrencyPair> mRemoveMap = new ConcurrentHashMap<>();

    private final Handler mHandler = new Handler();
    private Runnable mUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateCurrencyPairList();
        }
    };

    public CurrencyPairAdapter(@NonNull CurrencyPairManager currencyPairManager) {
        mCurrencyPairManager = currencyPairManager;
        mCurrencyPairManager.register(this);

        mCurrencyPairList = new SortedList<>(CurrencyPair.class,
                new SortedListAdapterCallback<CurrencyPair>(this) {
                    @Override
                    public int compare(CurrencyPair o1, CurrencyPair o2) {
                        return Integer.valueOf(o1.getSortIndex()).compareTo(o2.getSortIndex());
                    }

                    @Override
                    public boolean areContentsTheSame(CurrencyPair oldItem, CurrencyPair newItem) {
                        return false;
                    }

                    @Override
                    public boolean areItemsTheSame(CurrencyPair item1, CurrencyPair item2) {
                        return item1.getType() == item2.getType();
                    }
                });
    }

    private void updateCurrencyPairList() {
        Log.d(LOG_TAG, "add: " + mAddMap.size() + " update: " + mUpdateMap.size() + " remove: " + mRemoveMap.size());
        for (CurrencyPair currencyPair : mAddMap.values()) {
            mCurrencyPairList.add(currencyPair);
        }
        mAddMap.clear();
        for (CurrencyPair currencyPair : mUpdateMap.values()) {
            if (mCurrentSelectedPos == -1 && mCurrencyPairList.indexOf(currencyPair) != -1) {
                mCurrencyPairList.add(currencyPair);
            }
        }
        mUpdateMap.clear();
        for (CurrencyPair currencyPair : mRemoveMap.values()) {
            mCurrencyPairList.remove(currencyPair);
        }
        if (mRemoveMap.size() > 0) {
            for (int i = 0; i < mCurrencyPairList.size(); i++) {
                mCurrencyPairList.get(i).setSortIndex(i);
            }
        }
        mRemoveMap.clear();
    }

    private void runUpdate() {
        mHandler.removeCallbacks(mUpdateRunnable);
        mHandler.post(mUpdateRunnable);
    }

    @Override
    public CurrencyPairViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        return new CurrencyPairViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CurrencyPairViewHolder holder, int position) {
        holder.setItem(mCurrencyPairList.get(position));
    }

    @Override
    public int getItemCount() {
        return mCurrencyPairList.size();
    }

    @Override
    public void onAddPair(@NonNull CurrencyPair currencyPair) {
        mAddMap.put(currencyPair.getType(), currencyPair);
        mRemoveMap.remove(currencyPair.getType());
        runUpdate();
    }

    @Override
    public void onUpdatePair(@NonNull CurrencyPair currencyPair) {
        mUpdateMap.put(currencyPair.getType(), currencyPair);
        mRemoveMap.remove(currencyPair.getType());
        runUpdate();
    }

    @Override
    public void onRemovePair(@NonNull CurrencyPair currencyPair) {
        mAddMap.remove(currencyPair.getType());
        mUpdateMap.remove(currencyPair.getType());
        mRemoveMap.put(currencyPair.getType(), currencyPair);
        runUpdate();
    }

    @Override
    public void onItemSelect(int pos, int actionState) {
        mCurrentSelectedPos = pos;
    }

    @Override
    public void onItemMove(int fromPos, int toPos) {
        CurrencyPair from = mCurrencyPairList.get(fromPos);
        CurrencyPair to = mCurrencyPairList.get(toPos);
        int fromIndex = from.getSortIndex();
        from.setSortIndex(to.getSortIndex());
        to.setSortIndex(fromIndex);
        mCurrencyPairList.updateItemAt(fromPos, from);
    }

    @Override
    public void onItemDismiss(int pos) {
        mCurrencyPairManager.removeCurrencyPair(mCurrencyPairList.get(pos));
    }

    public static class CurrencyPairViewHolder extends ViewHolder {

        private TextView mPairName;
        private TextView mBidAsk;
        private TextView mSpread;

        public CurrencyPairViewHolder(View itemView) {
            super(itemView);
            mPairName = (TextView) itemView.findViewById(R.id.pair_name);
            mBidAsk = (TextView) itemView.findViewById(R.id.bid_ask);
            mSpread = (TextView) itemView.findViewById(R.id.spread);
        }

        public void setItem(CurrencyPair item) {
            mPairName.setText(item.getType().getDisplayName());
            mBidAsk.setText(String.format("%s / %s", item.getTick().getA(), item.getTick().getB()));
            mSpread.setText(item.getTick().getSpr());
        }
    }
}
