package com.plorial.exoroplayer.controllers;

import android.os.Bundle;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.plorial.exoroplayer.R;
import com.plorial.exoroplayer.model.events.VideoStatusEvent;
import com.plorial.exoroplayer.model.TranslateTask;

import org.greenrobot.eventbus.EventBus;

import java.text.BreakIterator;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

/**
 * Created by plorial on 4/13/16.
 */
public class SubtitlesClickListener implements View.OnClickListener {

    private TextView tvTranslatedText;

    public SubtitlesClickListener(TextView tvTranslatedText) {
        this.tvTranslatedText = tvTranslatedText;
    }

    @Override
    public void onClick(View v) {
        EventBus.getDefault().post(new VideoStatusEvent(VideoStatusEvent.PAUSE));
        init(v);
    }

    private void init(View v) {
        String definition = ((TextView)v).getText().toString().trim();
        TextView definitionView = (TextView) v;
        definitionView.setMovementMethod(LinkMovementMethod.getInstance());
        definitionView.setText(definition, TextView.BufferType.SPANNABLE);
        Spannable spans = (Spannable) definitionView.getText();
        BreakIterator iterator = BreakIterator.getWordInstance(Locale.US);
        iterator.setText(definition);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
                .next()) {
            String possibleWord = definition.substring(start, end);
            if (Character.isLetterOrDigit(possibleWord.charAt(0))) {
                ClickableSpan clickSpan = getClickableSpan(possibleWord);
                spans.setSpan(clickSpan, start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private ClickableSpan getClickableSpan(final String word) {
        return new ClickableSpan() {
            final String mWord;
            {
                mWord = word;
            }

            @Override
            public void onClick(View widget) {
                Log.d("tapped on:", mWord);
                FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(tvTranslatedText.getContext());
                Bundle analytics = new Bundle();
                analytics.putString(FirebaseAnalytics.Param.ITEM_NAME, mWord);
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_SEARCH_RESULTS, analytics);
                TranslateTask translateTask = new TranslateTask();
                translateTask.execute(mWord);
                String translatedWord = "";
                try {
                    translatedWord = translateTask.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    translatedWord = "not translated";
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    translatedWord = "not translated";
                }
                tvTranslatedText.setVisibility(View.VISIBLE);
                tvTranslatedText.setText(translatedWord);
            }

            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
            }
        };
    }
}
