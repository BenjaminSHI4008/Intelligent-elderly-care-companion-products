package com.xiaoban.app.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
<<<<<<< HEAD
import android.graphics.Typeface;
=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.xiaoban.app.voice.VoiceManager;
import com.xiaoban.app.voice.VoiceRecognizer;

public class VoiceButton extends View {

    private Paint circlePaint;
    private Paint ripplePaint;
    private Paint textPaint;

    private boolean isRecording = false;
<<<<<<< HEAD
    private boolean drawContent = true;
    private float rippleRadius = 0f;
    private String circleIdleText = "按住说话";
    private String rectangleIdleText = "🎙️ 按住录音回复";
    private String recordingText = "正在听...";
    private String displayText = circleIdleText;
=======
    private float rippleRadius = 0f;
    private String displayText = "按住说话";
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9

    private static final int COLOR_NORMAL = 0xFF3AAFA9;
    private static final int COLOR_PRESSED = 0xFF2B7A78;
    private static final int COLOR_RIPPLE = 0x4D3AAFA9;
<<<<<<< HEAD
    private static final float LARGE_BUTTON_MIN_DP = 140f;
    private static final float LARGE_BUTTON_TEXT_SP = 28f;
    private static final float SMALL_BUTTON_TEXT_SP = 12f;
=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9

    private VoiceRecognizer.Callback voiceCallback;
    private AnimatorSet rippleAnimator;

    public VoiceButton(Context context) {
        super(context);
        init();
    }

    public VoiceButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VoiceButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(COLOR_NORMAL);
        circlePaint.setStyle(Paint.Style.FILL);

        ripplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ripplePaint.setColor(COLOR_RIPPLE);
        ripplePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFFFFFFFF);
<<<<<<< HEAD
        textPaint.setTextSize(spToPx(LARGE_BUTTON_TEXT_SP));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
=======
        textPaint.setTextSize(48f);
        textPaint.setTextAlign(Paint.Align.CENTER);
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
    }

    public void setVoiceCallback(VoiceRecognizer.Callback callback) {
        this.voiceCallback = callback;
    }

<<<<<<< HEAD
    public void setRectangleIdleText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        rectangleIdleText = text;
        invalidate();
    }

    public void setDrawContent(boolean drawContent) {
        this.drawContent = drawContent;
        if (!drawContent) {
            stopRippleAnimation();
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!drawContent) {
            return;
        }

=======
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
        float width = getWidth();
        float height = getHeight();
        float cx = width / 2f;
        float cy = height / 2f;

        boolean isRoundButton = Math.abs(width - height) < 20;

        if (isRoundButton) {
            drawCircleButton(canvas, cx, cy);
        } else {
            drawRectangleButton(canvas, width, height);
        }
    }

    private void drawCircleButton(Canvas canvas, float cx, float cy) {
        float radius = Math.min(cx, cy) * 0.85f;
<<<<<<< HEAD
        float diameterDp = Math.min(getWidth(), getHeight()) / getResources().getDisplayMetrics().density;
        textPaint.setTextSize(spToPx(diameterDp >= LARGE_BUTTON_MIN_DP
                ? LARGE_BUTTON_TEXT_SP
                : SMALL_BUTTON_TEXT_SP));
=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9

        if (isRecording && rippleRadius > 0) {
            ripplePaint.setAlpha((int) (77 * (1 - rippleRadius / (radius * 1.5f))));
            canvas.drawCircle(cx, cy, rippleRadius, ripplePaint);
        }

        circlePaint.setColor(isRecording ? COLOR_PRESSED : COLOR_NORMAL);
        canvas.drawCircle(cx, cy, radius, circlePaint);

        canvas.drawText(displayText, cx, cy + textPaint.getTextSize() / 3, textPaint);
    }

<<<<<<< HEAD
    private float spToPx(float sp) {
        return sp * getResources().getDisplayMetrics().scaledDensity;
    }

=======
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
    private void drawRectangleButton(Canvas canvas, float width, float height) {
        circlePaint.setColor(isRecording ? COLOR_PRESSED : COLOR_NORMAL);

        float cornerRadius = Math.min(height / 2f, 28f);
        RectF rect = new RectF(0, 0, width, height);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, circlePaint);

        textPaint.setTextSize(Math.min(height * 0.5f, 56f));
<<<<<<< HEAD
        String text = isRecording ? recordingText : rectangleIdleText;
=======
        String text = isRecording ? "🎙️ 正在录音..." : "🎙️ 按住录音回复";
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
        canvas.drawText(text, width / 2f, height / 2f + textPaint.getTextSize() / 3, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startRecording();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                stopRecording();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void startRecording() {
        isRecording = true;
<<<<<<< HEAD
        displayText = recordingText;
        animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();

        boolean isRoundButton = Math.abs(getWidth() - getHeight()) < 20;
        if (drawContent && isRoundButton) {
=======
        displayText = "正在听...";
        animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start();

        boolean isRoundButton = Math.abs(getWidth() - getHeight()) < 20;
        if (isRoundButton) {
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
            startRippleAnimation();
        }

        invalidate();
        VoiceManager.getInstance().getRecognizer().startListening(voiceCallback);
    }

    private void stopRecording() {
        isRecording = false;
<<<<<<< HEAD
        displayText = circleIdleText;
=======
        displayText = "按住说话";
>>>>>>> ae5245dd4789e2f7f705836e323616db126ae8e9
        animate().scaleX(1f).scaleY(1f).setDuration(100).start();
        stopRippleAnimation();
        invalidate();
        VoiceManager.getInstance().getRecognizer().stopListening();
    }

    private void startRippleAnimation() {
        float maxRadius = Math.min(getWidth(), getHeight()) / 2f * 1.3f;
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "rippleRadius", 0f, maxRadius);
        animator.setDuration(1000);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.addUpdateListener(a -> {
            rippleRadius = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
        rippleAnimator = new AnimatorSet();
        rippleAnimator.play(animator);
    }

    private void stopRippleAnimation() {
        if (rippleAnimator != null) {
            rippleAnimator.cancel();
        }
        rippleRadius = 0;
    }

    public void setRippleRadius(float radius) {
        this.rippleRadius = radius;
    }
}
