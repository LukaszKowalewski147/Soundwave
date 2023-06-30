package com.example.soundwave;

import android.content.Context;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class PlaybackManager implements Runnable {

    private final Context context;
    private final Handler handler;
    private final ImageButton playPauseBtn;
    private final SeekBar playbackBar;
    private final TextView playbackElapsedTime;

    private Tone tone;
    private TonePlayer tonePlayer;

    public PlaybackManager(Context context, Handler handler, ImageButton playPauseBtn, SeekBar playbackBar, TextView playbackElapsedTime) {
        this.context = context;
        this.handler = handler;
        this.playPauseBtn = playPauseBtn;
        this.playbackBar = playbackBar;
        this.playbackElapsedTime = playbackElapsedTime;
        tone = null;
        tonePlayer = null;
    }

    public void setTone(Tone tone) {
        if (isPlaying())
            stopPlayback();
        this.tone = tone;
        tonePlayer = new TonePlayer(tone);
        tonePlayer.load(context);
        resetPlaybackBarValues();
    }

    public void managePlayPauseActivity() {
        if (isPlaying())
            pausePlayback();
        else
            startPlayback();
    }

    public void resetPlayback() {
        stopPlayback();
        resetPlaybackBarValues();
        tonePlayer.reload();
    }

    private void startPlayback() {
        tonePlayer.play();
        Options.playbackState = Options.PlaybackState.ON;
        managePlayPauseButton();
    }

    private void pausePlayback() {
        tonePlayer.pause();
        Options.playbackState = Options.PlaybackState.OFF;
        managePlayPauseButton();
    }

    private void stopPlayback() {
        tonePlayer.stop();
        Options.playbackState = Options.PlaybackState.OFF;
        managePlayPauseButton();
    }

    private void reloadTone() {
        tonePlayer.stop();
        tonePlayer.reload();
        tonePlayer.play();
    }

    @Override
    public void run() {
        final int samplingRate = tone.getSampleRate().sampleRate;
        final int barRefreshRate = Constants.PLAYBACK_REFRESH_RATE.value;
        final int endingPoint = samplingRate * tone.getDuration();
        final int barDivider = tone.getDuration() * 100;

        while (isPlaying()) {
            updatePlaybackBar(endingPoint, barDivider, samplingRate);
            try {
                Thread.sleep(barRefreshRate);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updatePlaybackBar(int endingPoint, int barDivider, int samplingRate) {
        int playbackPosition = tonePlayer.getPlaybackPosition();
        if (playbackPosition >= endingPoint) {
            if (Options.looperState == Options.LooperState.ON) {
                reloadTone();
                return;
            }
            playbackPosition = (int) Math.round(endingPoint / (double) barDivider);
            setPlaybackBarValues(playbackPosition, tone.getDuration());
            stopPlayback();
            return;
        }

        int finalPlaybackPosition = (int) Math.round(playbackPosition / (double) barDivider);
        int finalElapsedTime = (int) Math.floor(playbackPosition / (double) samplingRate);

        setPlaybackBarValues(finalPlaybackPosition, finalElapsedTime);
    }

    private void setPlaybackBarValues(int playbackPosition, int elapsedTime) {
        handler.post(new Runnable() {
            public void run() {
                playbackBar.setProgress(playbackPosition);
                playbackElapsedTime.setText(String.valueOf(elapsedTime));
            }
        });
    }

    private void resetPlaybackBarValues() {
        handler.post(new Runnable(){
            public void run() {
                playbackBar.setProgress(0);
                playbackElapsedTime.setText("0");
            }
        });
    }

    private void managePlayPauseButton() {
        if (isPlaying()) {
            handler.post(new Runnable() {
                public void run() {
                    playPauseBtn.setBackgroundResource(R.drawable.pause_btn);
                }
            });
        } else {
            handler.post(new Runnable() {
                public void run() {
                    playPauseBtn.setBackgroundResource(R.drawable.play_btn);
                }
            });
        }
    }

    private boolean isPlaying() {
        return Options.playbackState == Options.PlaybackState.ON;
    }

    public void extra() {
        tonePlayer.extra(context);
    }
}
