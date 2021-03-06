package com.plorial.exoroplayer.controllers;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.widget.TextView;

import com.plorial.exoroplayer.ijk.IjkVideoView;
import com.plorial.exoroplayer.model.SubtitleProcessingTask;
import com.plorial.exoroplayer.model.SubtitleProcessor;
import com.sri.subtitlessupport.utils.TimedTextObject;

import java.util.concurrent.ExecutionException;

/**
 * Created by plorial on 4/13/16.
 */
public class SubtitlesController {

    private Context context;
    private TextView subtitlesText;
    private IjkVideoView videoView;
    private String srtSource;
    private Handler subtitleDisplayHandler;
    private TextView tvTranslatedText;

    public SubtitlesController(Context context, IjkVideoView videoView, TextView subtitlesText, String srtSource, TextView tvTranslatedText) {
        this.context = context;
        this.videoView = videoView;
        this.subtitlesText = subtitlesText;
        this.srtSource = srtSource;
        this.tvTranslatedText = tvTranslatedText;
    }

    public void startSubtitles(){
        subtitlesText.setOnClickListener(new SubtitlesClickListener(tvTranslatedText));

        SubtitleProcessingTask subsFetchTask = new SubtitleProcessingTask(context);
        subsFetchTask.execute(srtSource);
        TimedTextObject srt = null;
        try {
            srt = subsFetchTask.get();
            subtitleDisplayHandler = new Handler();
            SubtitleProcessor processor = new SubtitleProcessor(subtitleDisplayHandler, videoView, subtitlesText, srt);
            subtitleDisplayHandler.post(processor);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void stopSubtitles(){
        subtitleDisplayHandler.removeCallbacksAndMessages(null);
    }
}
