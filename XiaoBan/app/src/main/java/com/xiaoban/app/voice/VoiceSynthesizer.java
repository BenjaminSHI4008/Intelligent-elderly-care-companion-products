package com.xiaoban.app.voice;

import android.content.Context;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.SpeechError;

import android.os.Bundle;

public class VoiceSynthesizer {

    private static final String TAG = "VoiceSynthesizer";
    private SpeechSynthesizer synthesizer;
    private Callback callback;

    public interface Callback {
        void onSpeakBegin();
        void onSpeakComplete();
    }

    public void init(Context context) {
        synthesizer = SpeechSynthesizer.createSynthesizer(context, new InitListener() {
            @Override
            public void onInit(int code) {
                if (code != ErrorCode.SUCCESS) {
                    Log.e(TAG, "TTS初始化失败，错误码：" + code);
                }
            }
        });

        if (synthesizer != null) {
            synthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            synthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
            synthesizer.setParameter(SpeechConstant.SPEED, "40");
            synthesizer.setParameter(SpeechConstant.VOLUME, "80");
            synthesizer.setParameter(SpeechConstant.PITCH, "50");
        }
    }

    public void speak(String text, Callback cb) {
        this.callback = cb;
        if (synthesizer != null) {
            synthesizer.startSpeaking(text, synthesizerListener);
        }
    }

    public void stop() {
        if (synthesizer != null) {
            synthesizer.stopSpeaking();
        }
    }

    public void destroy() {
        if (synthesizer != null) {
            synthesizer.stopSpeaking();
            synthesizer.destroy();
            synthesizer = null;
        }
    }

    private final SynthesizerListener synthesizerListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            if (callback != null) callback.onSpeakBegin();
        }

        @Override
        public void onSpeakPaused() {}

        @Override
        public void onSpeakResumed() {}

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {}

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {}

        @Override
        public void onCompleted(SpeechError error) {
            if (callback != null) callback.onSpeakComplete();
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {}
    };
}
