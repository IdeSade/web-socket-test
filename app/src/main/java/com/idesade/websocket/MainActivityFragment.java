package com.idesade.websocket;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.SimpleCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idesade.websocket.CurrencyPairAdapter.CurrencyPairViewHolder;
import com.idesade.websocket.model.CurrencyPairType;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        CurrencyPairAdapter adapter = new CurrencyPairAdapter(MainApp.getCurrencyPairManager());
        recyclerView.setAdapter(adapter);

        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(adapter));
        touchHelper.attachToRecyclerView(recyclerView);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        Set<CurrencyPairType> set = new LinkedHashSet<>();
        set.add(CurrencyPairType.EURUSD);
        set.add(CurrencyPairType.EURGBP);
        set.add(CurrencyPairType.AUDUSD);
        set.add(CurrencyPairType.EURCHF);
        set.add(CurrencyPairType.EURJPY);

        MainApp.getNetworkManager().subscribe(set);
    }

    @Override
    public void onPause() {
        super.onPause();

        MainApp.getNetworkManager().unsubscribe(MainApp.getNetworkManager().getCurrencyPair());
    }
}
