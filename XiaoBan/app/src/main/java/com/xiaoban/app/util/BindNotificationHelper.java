package com.xiaoban.app.util;

import android.content.Context;
import android.text.TextUtils;

import com.xiaoban.app.model.BindingRelationItem;
import com.xiaoban.app.voice.VoiceManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Detects newly established bind relations and triggers elder-side TTS announcements.
 */
public final class BindNotificationHelper {

    private static final String SP_KNOWN_RELATION_IDS = "known_bind_relation_ids";

    private BindNotificationHelper() {}

    /**
     * @param announce when false, only records current relation ids as baseline (no TTS)
     */
    public static void processBindings(Context context, List<BindingRelationItem> relations, boolean announce) {
        Set<Long> knownIds = loadKnownIds(context);
        Set<Long> activeIds = new HashSet<>();
        List<BindingRelationItem> newlyBound = new ArrayList<>();

        if (relations != null) {
            for (BindingRelationItem item : relations) {
                if (!"active".equals(item.getStatus())) {
                    continue;
                }
                activeIds.add(item.getId());
                if (announce && !knownIds.contains(item.getId())) {
                    newlyBound.add(item);
                }
            }
        }

        if (!announce) {
            saveKnownIds(context, activeIds);
            return;
        }

        for (BindingRelationItem item : newlyBound) {
            speakBindSuccess(item.getDisplayNameForElder());
        }
        saveKnownIds(context, activeIds);
    }

    public static void speakBindSuccess(String childDisplayName) {
        String name = TextUtils.isEmpty(childDisplayName) ? "您的家人" : childDisplayName;
        VoiceManager.getInstance().getSynthesizer().speak(
                "好消息，" + name + "已经成功和您绑定了，以后可以关心您的健康啦", null);
    }

    public static String formatElderNamesForChild(List<BindingRelationItem> relations) {
        if (relations == null || relations.isEmpty()) {
            return null;
        }
        StringBuilder names = new StringBuilder();
        int count = 0;
        for (BindingRelationItem item : relations) {
            if (!"active".equals(item.getStatus())) {
                continue;
            }
            if (count > 0) {
                names.append("、");
            }
            names.append(item.getDisplayNameForChild());
            count++;
        }
        return count == 0 ? null : names.toString();
    }

    public static int countActiveRelations(List<BindingRelationItem> relations) {
        if (relations == null) {
            return 0;
        }
        int count = 0;
        for (BindingRelationItem item : relations) {
            if ("active".equals(item.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private static Set<Long> loadKnownIds(Context context) {
        Set<Long> ids = new HashSet<>();
        String raw = SharedPrefUtil.getString(context, SP_KNOWN_RELATION_IDS, "");
        if (raw.isEmpty()) {
            return ids;
        }
        for (String part : raw.split(",")) {
            if (!part.isEmpty()) {
                try {
                    ids.add(Long.parseLong(part.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return ids;
    }

    private static void saveKnownIds(Context context, Set<Long> ids) {
        StringBuilder sb = new StringBuilder();
        for (Long id : ids) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(id);
        }
        SharedPrefUtil.putString(context, SP_KNOWN_RELATION_IDS, sb.toString());
    }
}
