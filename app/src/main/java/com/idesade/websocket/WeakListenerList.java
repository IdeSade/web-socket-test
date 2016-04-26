package com.idesade.websocket;

import android.support.annotation.NonNull;

import java.util.Map;
import java.util.WeakHashMap;

public class WeakListenerList<T> {

    public interface ListenerRunnable<T> {
        void run(@NonNull T listener);
    }

    private final Map<T, Void> mListeners = new WeakHashMap<>();

    public synchronized void register(@NonNull T listener) {
        mListeners.put(listener, null);
    }

    public synchronized void forEach(@NonNull ListenerRunnable<T> runnable) {
        for (T t : mListeners.keySet()) {
            runnable.run(t);
        }
    }
}
