package com.xiaoban.app.voice;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechRecognizer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class VoiceRecognizer {

    private static final String TAG = "VoiceRecognizer";
    private SpeechRecognizer recognizer;
    private Callback callback;
    private StringBuilder resultBuilder = new StringBuilder();

    public interface Callback {
        void onResult(String text);
        void onPartialResult(String text);
        void onError(String errorMsg);
    }

    public void init(Context context) {
        recognizer = SpeechRecognizer.createRecognizer(context, new InitListener() {
            @Override
            public void onInit(int code) {
                if (code != ErrorCode.SUCCESS) {
                    Log.e(TAG, "语音识别初始化失败，错误码：" + code);
                }
            }
        });

        if (recognizer != null) {
            recognizer.setParameter(SpeechConstant.DOMAIN, "iat");
            recognizer.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            recognizer.setParameter(SpeechConstant.ACCENT, "mandarin");
            recognizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            recognizer.setParameter(SpeechConstant.RESULT_TYPE, "json");
            recognizer.setParameter(SpeechConstant.VAD_BOS, "5000");
            recognizer.setParameter(SpeechConstant.VAD_EOS, "1800");
        }
    }

    public void startListening(Callback cb) {
        this.callback = cb;
        resultBuilder.setLength(0);
        if (recognizer != null) {
            recognizer.startListening(recognizerListener);
        }
    }

    public void stopListening() {
        if (recognizer != null) {
            recognizer.stopListening();
        }
    }

    public void destroy() {
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.destroy();
            recognizer = null;
        }
    }

    private final RecognizerListener recognizerListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int volume, byte[] data) {}

        @Override
        public void onBeginOfSpeech() {}

        @Override
        public void onEndOfSpeech() {}

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String text = parseResult(results.getResultString());
            resultBuilder.append(text);
            if (callback != null) {
                if (isLast) {
                    callback.onResult(resultBuilder.toString());
                } else {
                    callback.onPartialResult(resultBuilder.toString());
                }
            }
        }

        @Override
        public void onError(com.iflytek.cloud.SpeechError error) {
            if (callback != null) {
                callback.onError(error.getPlainDescription(true));
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {}
    };

    private String parseResult(String json) {
        StringBuilder sb = new StringBuilder();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);
            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                sb.append(items.getJSONObject(0).getString("w"));
            }
        } catch (Exception e) {
            Log.e(TAG, "解析识别结果失败", e);
        }
        return sb.toString();
    }
}
