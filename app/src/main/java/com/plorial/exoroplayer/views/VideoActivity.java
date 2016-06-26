package com.plorial.exoroplayer.views;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.devbrackets.android.exomedia.EMVideoView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.plorial.exoroplayer.R;
import com.plorial.exoroplayer.controllers.ErrorListener;
import com.plorial.exoroplayer.controllers.SettingsClickListener;
import com.plorial.exoroplayer.controllers.SubtitlesController;
import com.plorial.exoroplayer.controllers.VideoControl;
import com.plorial.exoroplayer.model.events.VideoStatusEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;



/**
 * Created by plorial on 6/25/16.
 */
public class VideoActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener, SettingsDialog.OnUpdateSettingsListener{

    private static final String TAG = VideoActivity.class.getSimpleName();

    public static final String VIDEO_PATH = "VIDEO_PATH";
    public static final String SRT1_PATH = "SRT1";
    public static final String SRT2_PATH = "SRT2";

    public static int subsCorrector = 0;

    private EMVideoView emVideoView;
    private TextView firstSubtitleText;
    private TextView secondSubtitleText;
    private FrameLayout controlsHolder;
    private long currentPos;

    private boolean doubleSubsReady = false;
    private String videoSource;
    private String srt1Source;
    private String srt2Source;
    private TextView tvTranslatedText;
    private ProgressBar pbPreparing;
    public static InterstitialAd ad;
    private int uiFlags;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_activity);
        setUpViews();
        if(savedInstanceState != null){
            currentPos = savedInstanceState.getLong("CURRENT_POS");
        }
        setUpAd();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            uiFlags = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        } else {
            uiFlags = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        getWindow().getDecorView().setSystemUiVisibility(uiFlags);
    }

    private void setUpAd() {
        ad = new InterstitialAd(this);
        ad.setAdUnitId(getString(R.string.pause_banner_id));
        ad.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                showAd();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                getWindow().getDecorView().setSystemUiVisibility(uiFlags);
                emVideoView.pause();
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                getWindow().getDecorView().setSystemUiVisibility(uiFlags);
                emVideoView.start();
            }
        });
    }

    private void setUpViews() {
        firstSubtitleText = (TextView) findViewById(R.id.firstSubtitleText);
        secondSubtitleText = (TextView) findViewById(R.id.secondSubtitleText);
        firstSubtitleText.setVisibility(View.INVISIBLE);
        secondSubtitleText.setVisibility(View.INVISIBLE);
        emVideoView = (EMVideoView) findViewById(R.id.video_play_activity_video_view);
        controlsHolder = (FrameLayout) findViewById(R.id.controlsHolder);
        tvTranslatedText = (TextView) findViewById(R.id.tvTranslatedText);
        pbPreparing = (ProgressBar) findViewById(R.id.pbPreparing);
        tvTranslatedText.setText(R.string.preparing_video);
        videoSource = getIntent().getExtras().getString(VIDEO_PATH);
        srt1Source = getIntent().getExtras().getString(SRT1_PATH);
        srt2Source = getIntent().getExtras().getString(SRT2_PATH);
        Log.d(TAG, "video source " + videoSource);
        getPreferences();
        setupVideoView();
    }

    private void setupVideoView() {
        emVideoView.setOnErrorListener(new ErrorListener(this));
        emVideoView.setOnPreparedListener(this);
        emVideoView.setVideoPath(videoSource);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        prepareSubs();
        VideoControl videoControl = new VideoControl(emVideoView);
        final VideoControllerView controller = new VideoControllerView(this);
        controller.setMediaPlayer(videoControl);
        controller.setAnchorView(controlsHolder);
        emVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.show(10000);
            }
        });
        AppCompatImageButton settingsButton = (AppCompatImageButton) controller.getRoot().findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new SettingsClickListener(this));
        emVideoView.seekTo((int) currentPos);
        tvTranslatedText.setText(R.string.preparing_subs);
    }

    private void prepareSubs(){
        if (srt1Source == null && srt2Source == null){
            doubleSubsReady = true;
            EventBus.getDefault().post(new VideoStatusEvent(VideoStatusEvent.READY_TO_START));
            firstSubtitleText.setVisibility(View.GONE);
            secondSubtitleText.setVisibility(View.GONE);
        }else if (srt1Source == null || srt2Source == null){
            EventBus.getDefault().post(new VideoStatusEvent(VideoStatusEvent.READY_TO_START));
            if(srt1Source != null){
                SubtitlesController firstSubController = new SubtitlesController(this, emVideoView, firstSubtitleText, srt1Source, tvTranslatedText);
                firstSubController.startSubtitles();
            }else if(srt2Source != null){
                SubtitlesController firstSubController = new SubtitlesController(this, emVideoView, firstSubtitleText, srt2Source, tvTranslatedText);
                firstSubController.startSubtitles();
            }
            firstSubtitleText.setVisibility(View.VISIBLE);
            secondSubtitleText.setVisibility(View.GONE);
        }else {
            SubtitlesController firstSubController = new SubtitlesController(this, emVideoView, firstSubtitleText, srt1Source, tvTranslatedText);
            firstSubController.startSubtitles();

            SubtitlesController secondSubController = new SubtitlesController(this, emVideoView, secondSubtitleText, srt2Source, tvTranslatedText);
            secondSubController.startSubtitles();
            firstSubtitleText.setVisibility(View.VISIBLE);
            secondSubtitleText.setVisibility(View.VISIBLE);
        }
    }

    public void getPreferences() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        float textSize = preferences.getFloat("TEXT_SIZE", 20);
        int alpha = preferences.getInt("TEXT_ALPHA", 185);
        updateTextSize(textSize);
        updateTextAlpha(alpha);
    }

    private void updateTextSize(float textSize) {
        firstSubtitleText.setTextSize(TypedValue.COMPLEX_UNIT_SP,textSize);
        secondSubtitleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        tvTranslatedText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
    }

    private void updateTextAlpha(int alpha){
        firstSubtitleText.setBackgroundColor(Color.argb(alpha, 42, 42, 42));
        secondSubtitleText.setBackgroundColor(Color.argb(alpha, 42, 42, 42));
        tvTranslatedText.setBackgroundColor(Color.argb(alpha, 42, 42, 42));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("CURRENT_POS",emVideoView.getCurrentPosition());
    }

    @Subscribe
    public void onVideoStatusEvent(VideoStatusEvent event){
        if(event.getMassage() == VideoStatusEvent.PAUSE && emVideoView.isPlaying()) {
            emVideoView.pause();
        }
        if(event.getMassage() == VideoStatusEvent.READY_TO_START && doubleSubsReady) {
            emVideoView.start();
            tvTranslatedText.setVisibility(View.INVISIBLE);
            tvTranslatedText.setText("");
            pbPreparing.setVisibility(View.INVISIBLE);
        }
        if (event.getMassage() == VideoStatusEvent.READY_TO_START && !doubleSubsReady) {
            doubleSubsReady = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAd();
    }

    public void loadAd(){
        AdRequest adRequest = new AdRequest.Builder().build();
        ad.loadAd(adRequest);
    }

    private void showAd() {
        if(ad.isLoaded()){
            ad.show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        emVideoView.release();
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onUpdateSettings() {
        getPreferences();
        getWindow().getDecorView().setSystemUiVisibility(uiFlags);
    }
}
