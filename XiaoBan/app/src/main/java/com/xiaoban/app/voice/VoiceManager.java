package com.xiaoban.app.voice;

import android.content.Context;

public class VoiceManager {

    private static VoiceManager instance;
    private VoiceRecognizer recognizer;
    private VoiceSynthesizer synthesizer;
    private boolean initialized = false;

    private VoiceManager() {}

    public static VoiceManager getInstance() {
        if (instance == null) {
            synchronized (VoiceManager.class) {
                if (instance == null) {
                    instance = new VoiceManager();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        if (initialized) return;
        recognizer = new VoiceRecognizer();
        recognizer.init(context);
        synthesizer = new VoiceSynthesizer();
        synthesizer.init(context);
        initialized = true;
    }

    public VoiceRecognizer getRecognizer() {
        return recognizer;
    }

    public VoiceSynthesizer getSynthesizer() {
        return synthesizer;
    }

    public void destroy() {
        if (recognizer != null) {
            recognizer.destroy();
        }
        if (synthesizer != null) {
            synthesizer.destroy();
        }
        initialized = false;
    }
}
