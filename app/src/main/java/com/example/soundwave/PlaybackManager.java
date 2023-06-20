package com.example.soundwave;

import android.content.Context;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.TextView;

public class PlaybackManager implements Runnable {
    private final Context context;
    private final Handler handler;
    private final SeekBar playbackBar;
    private final TextView playbackElapsedTime;

    private Tone tone;
    private TonePlayer tonePlayer;

    private volatile boolean manageBar;

    public PlaybackManager(Context context, Handler handler, SeekBar playbackBar, TextView playbackElapsedTime) {
        this.context = context;
        this.handler = handler;
        this.playbackBar = playbackBar;
        this.playbackElapsedTime = playbackElapsedTime;
        tone = null;
        tonePlayer = null;
        manageBar = false;
    }

    public void setTone(Tone tone) {
        if (manageBar) {
            manageBar = false;
        }
        this.tone = tone;

        tonePlayer = new TonePlayer(tone);
        tonePlayer.load(context);

        resetPlaybackBar();
    }

    public void startPlayback() {
        tonePlayer.play();
        manageBar = true;
        this.run();
    }

    public void stopPlayback() {
        tonePlayer.stop();
        manageBar = false;
        resetPlaybackBar();
    }

    public void pausePlayback() {
        tonePlayer.pause();
        manageBar = false;
    }

    @Override
    public void run() {
        final int barRefreshRate = 200; // ms
        final int endingPoint = Constants.SAMPLE_RATE.value * tone.getDuration();
        final int barDivider = tone.getDuration() * 100;
        int playbackPosition = tonePlayer.getPlaybackPosition();

        while (playbackPosition < endingPoint) {
            if (!manageBar)
                break;

            playbackPosition = tonePlayer.getPlaybackPosition();
            playbackPosition = (int)Math.round(playbackPosition / (double)barDivider);

            int finalPlaybackPosition = playbackPosition;
            int finalElapsedTime = (int) Math.floor(playbackPosition / (double) Constants.SAMPLE_RATE.value);

            handler.post(new Runnable(){
                public void run() {
                    playbackBar.setProgress(finalPlaybackPosition);
                    playbackElapsedTime.setText(String.valueOf(finalElapsedTime));
                }
            });

            try {
                Thread.sleep(barRefreshRate);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void resetPlaybackBar() {
        handler.post(new Runnable(){
            public void run() {
                playbackBar.setProgress(0);
                playbackElapsedTime.setText("0");
            }
        });
    }
}
