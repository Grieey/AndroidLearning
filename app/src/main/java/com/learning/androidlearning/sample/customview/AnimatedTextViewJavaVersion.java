package com.learning.androidlearning.sample.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.learning.androidlearning.R;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class AnimatedTextViewJavaVersion extends FrameLayout {

    private TextView currentTextView;
    private TextView nextTextView;
    private Animation fadeOutAnimation;
    private Animation fadeInAnimation;
    private Thread animationJob;

    public AnimatedTextViewJavaVersion(@NonNull Context context) {
        this(context, null);
    }

    public AnimatedTextViewJavaVersion(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimatedTextViewJavaVersion(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_animated_text, this, true);
        TextView textView1 = findViewById(R.id.textView1);
        TextView textView2 = findViewById(R.id.textView2);
        currentTextView = textView1;
        nextTextView = textView2;

        fadeOutAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_out_up);
        fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in_up);
    }

    public void startAnimation(List<String> texts) {
        if (animationJob != null) {
            animationJob.interrupt();
        }

        animationJob = new Thread(() -> {
            AtomicInteger index = new AtomicInteger(0);
            while (!Thread.currentThread().isInterrupted()) {
                final String text = texts.get(index.get());
                postOnAnimation(() -> animateTextChange(text));
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                index.set((index.get() + 1) % texts.size());
            }
        });
        animationJob.start();
    }

    private void animateTextChange(String newText) {
        nextTextView.setText(newText);
        nextTextView.setVisibility(View.VISIBLE);
        currentTextView.startAnimation(fadeOutAnimation);
        nextTextView.startAnimation(fadeInAnimation);

        TextView temp = currentTextView;
        currentTextView = nextTextView;
        nextTextView = temp;
        nextTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animationJob != null) {
            animationJob.interrupt();
        }
    }
}
