package com.idesade.websocket;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.idesade.websocket.model.CurrencyPairType;

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

        MainApp.getCurrencyPairManager().loadState();

        if (MainApp.getCurrencyPairManager().getCurrencyPairTypeSet().size() == 0) {
            MainApp.getCurrencyPairManager().addCurrencyPair(CurrencyPairType.EURUSD);
            MainApp.getCurrencyPairManager().addCurrencyPair(CurrencyPairType.EURGBP);
            MainApp.getCurrencyPairManager().addCurrencyPair(CurrencyPairType.AUDUSD);
            MainApp.getCurrencyPairManager().addCurrencyPair(CurrencyPairType.EURCHF);
            MainApp.getCurrencyPairManager().addCurrencyPair(CurrencyPairType.EURJPY);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        MainApp.getCurrencyPairManager().saveState();
        MainApp.getCurrencyPairManager().clear();
    }
}
