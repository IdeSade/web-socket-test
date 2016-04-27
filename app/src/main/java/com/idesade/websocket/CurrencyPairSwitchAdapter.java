package com.idesade.websocket;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.idesade.websocket.model.CurrencyPairManager;
import com.idesade.websocket.model.CurrencyPairType;

import java.util.Arrays;
import java.util.List;

public class CurrencyPairSwitchAdapter extends RecyclerView.Adapter<CurrencyPairSwitchAdapter.ViewHolder> {

    private final List<CurrencyPairType> mValues;
    private final CurrencyPairManager mCurrencyPairManager;

    public CurrencyPairSwitchAdapter(@NonNull CurrencyPairManager currencyPairManager) {
        mValues = Arrays.asList(CurrencyPairType.values());
        mCurrencyPairManager = currencyPairManager;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_settings_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final CurrencyPairType type = mValues.get(position);

        holder.mPairName.setText(type.getDisplayName());
        holder.mSwitch.setChecked(mCurrencyPairManager.getCurrencyPairTypeSet().contains(type));

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.mSwitch.isChecked()) {
                    mCurrencyPairManager.removeCurrencyPair(type);
                } else {
                    mCurrencyPairManager.addCurrencyPair(type);
                }
                holder.mSwitch.setChecked(!holder.mSwitch.isChecked());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final View mView;
        public final TextView mPairName;
        public final Switch mSwitch;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mPairName = (TextView) view.findViewById(R.id.pair_name);
            mSwitch = (Switch) view.findViewById(R.id.pair_use);
        }
    }
}
