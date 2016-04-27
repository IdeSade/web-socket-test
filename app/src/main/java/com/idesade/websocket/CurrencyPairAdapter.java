package com.idesade.websocket;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.util.SortedListAdapterCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.idesade.websocket.CurrencyPairAdapter.CurrencyPairViewHolder;
import com.idesade.websocket.ItemTouchHelperCallback.ItemTouchHelperAdapter;
import com.idesade.websocket.model.CurrencyPair;
import com.idesade.websocket.model.CurrencyPairManager;
import com.idesade.websocket.model.CurrencyPairManager.CurrencyPairListener;

public class CurrencyPairAdapter extends Adapter<CurrencyPairViewHolder> implements
        CurrencyPairListener, ItemTouchHelperAdapter {

    private final SortedList<CurrencyPair> mCurrencyPairList;
    private final Handler mHandler = new Handler();
    private final CurrencyPairManager mCurrencyPairManager;
    private int mCurrentSelectedPos = -1;

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
                        return item1 == item2;
                    }
                });
    }

    public void add(@NonNull CurrencyPair currencyPair) {
        if (mCurrentSelectedPos == -1) {
            mCurrencyPairList.add(currencyPair);
        }
    }

    public void remove(@NonNull CurrencyPair currencyPair) {
        mCurrencyPairList.remove(currencyPair);
    }

    private void addFromThread(@NonNull final CurrencyPair currencyPair) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                add(currencyPair);
            }
        });
    }

    private void removeFromThread(@NonNull final CurrencyPair currencyPair) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                remove(currencyPair);
            }
        });
    }

    @Override
    public CurrencyPairViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
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
        addFromThread(currencyPair);
    }

    @Override
    public void onUpdatePair(@NonNull CurrencyPair currencyPair) {
        addFromThread(currencyPair);
    }

    @Override
    public void onRemovePair(@NonNull CurrencyPair currencyPair) {
        removeFromThread(currencyPair);
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

        private CurrencyPair mItem;
        private TextView mTextView;

        public CurrencyPairViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView;
        }

        public void setItem(CurrencyPair item) {
            mItem = item;
            mTextView.setText(item.toString());
        }
    }
}
