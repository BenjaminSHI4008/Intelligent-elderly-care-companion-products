package com.xiaoban.app.util;

import android.content.Context;

import com.xiaoban.app.base.Constants;
import com.xiaoban.app.model.BindingRelationItem;

import java.util.List;

/**
 * Single source of truth for the elder currently selected on the child side.
 *
 * <p>v1 defaults to the first active bound elder, but the id is persisted and
 * every data-layer call takes it explicitly, so multi-elder switching can be
 * added later without touching the data flow. Never hardcode {@code elderId = 1L}.
 */
public final class ElderSelectionStore {

    public static final long NO_ELDER = -1L;

    private ElderSelectionStore() {
    }

    public static long getSelected(Context context) {
        return SharedPrefUtil.getLong(context, Constants.SP_SELECTED_ELDER_ID, NO_ELDER);
    }

    public static void setSelected(Context context, long elderId) {
        SharedPrefUtil.putLong(context, Constants.SP_SELECTED_ELDER_ID, elderId);
    }

    /**
     * Resolves a valid selected elder against the latest binding relations.
     * Keeps the current selection when it is still an active binding, otherwise
     * falls back to the first active elder. Returns {@link #NO_ELDER} when there
     * is no active binding at all.
     */
    public static long resolve(Context context, List<BindingRelationItem> relations) {
        if (relations == null || relations.isEmpty()) {
            return NO_ELDER;
        }
        long current = getSelected(context);
        if (current != NO_ELDER && isActiveElder(relations, current)) {
            return current;
        }
        for (BindingRelationItem item : relations) {
            if (isActive(item) && item.getElderId() > 0) {
                setSelected(context, item.getElderId());
                return item.getElderId();
            }
        }
        return NO_ELDER;
    }

    private static boolean isActiveElder(List<BindingRelationItem> relations, long elderId) {
        for (BindingRelationItem item : relations) {
            if (isActive(item) && item.getElderId() == elderId) {
                return true;
            }
        }
        return false;
    }

    private static boolean isActive(BindingRelationItem item) {
        return item != null && "active".equals(item.getStatus());
    }
}
