package com.xiaoban.app.util;

import android.content.Context;
import android.text.TextUtils;

import com.xiaoban.app.base.Constants;
import com.xiaoban.app.model.BindingRelationItem;

import java.util.List;

public final class ChildElderSelection {

    private ChildElderSelection() {
    }

    public static BindingRelationItem resolve(Context context, List<BindingRelationItem> relations) {
        if (relations == null || relations.isEmpty()) {
            clear(context);
            return null;
        }

        long selectedId = getSelectedElderId(context);
        BindingRelationItem firstActive = null;
        for (BindingRelationItem relation : relations) {
            if (!isActive(relation)) {
                continue;
            }
            if (firstActive == null) {
                firstActive = relation;
            }
            if (selectedId > 0 && relation.getElderId() == selectedId) {
                save(context, relation);
                return relation;
            }
        }

        if (firstActive != null) {
            save(context, firstActive);
            return firstActive;
        }

        clear(context);
        return null;
    }

    public static void save(Context context, BindingRelationItem relation) {
        if (relation == null) {
            clear(context);
            return;
        }
        SharedPrefUtil.putLong(context, Constants.SP_SELECTED_ELDER_ID, relation.getElderId());
        SharedPrefUtil.putString(context, Constants.SP_SELECTED_ELDER_NAME,
                safeName(relation.getDisplayNameForChild()));
    }

    public static long getSelectedElderId(Context context) {
        return SharedPrefUtil.getLong(context, Constants.SP_SELECTED_ELDER_ID, 0L);
    }

    public static String getSelectedElderName(Context context) {
        return SharedPrefUtil.getString(context, Constants.SP_SELECTED_ELDER_NAME, "");
    }

    public static void clear(Context context) {
        SharedPrefUtil.putLong(context, Constants.SP_SELECTED_ELDER_ID, 0L);
        SharedPrefUtil.putString(context, Constants.SP_SELECTED_ELDER_NAME, "");
    }

    private static boolean isActive(BindingRelationItem relation) {
        return relation != null && "active".equals(relation.getStatus()) && relation.getElderId() > 0;
    }

    private static String safeName(String name) {
        return TextUtils.isEmpty(name) ? "老人" : name;
    }
}
