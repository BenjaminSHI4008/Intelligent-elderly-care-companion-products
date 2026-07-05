package com.xiaoban.app.util;

import java.lang.ref.WeakReference;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lightweight in-app notifier when a family message push arrives.
 */
public final class FamilyMessageNotifier {

    public interface Listener {
        void onFamilyMessageReceived();
    }

    private static final CopyOnWriteArrayList<WeakReference<Listener>> LISTENERS =
            new CopyOnWriteArrayList<>();

    private FamilyMessageNotifier() {}

    public static void addListener(Listener listener) {
        removeListener(listener);
        LISTENERS.add(new WeakReference<>(listener));
    }

    public static void removeListener(Listener listener) {
        LISTENERS.removeIf(ref -> {
            Listener current = ref.get();
            return current == null || current == listener;
        });
    }

    public static void notifyReceived() {
        for (WeakReference<Listener> ref : LISTENERS) {
            Listener listener = ref.get();
            if (listener != null) {
                listener.onFamilyMessageReceived();
            }
        }
        LISTENERS.removeIf(ref -> ref.get() == null);
    }
}
