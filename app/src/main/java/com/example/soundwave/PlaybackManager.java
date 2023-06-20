package com.example.soundwave;

import android.content.Context;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class PlaybackManager implements Runnable {

    private final String PLAYBACK_STATE_PLAYING = "playing";
    private final String PLAYBACK_STATE_NOT_PLAYING = "notPlaying";

    private final Context context;
    private final Handler handler;
    private final ImageButton playPauseBtn;
    private final SeekBar playbackBar;
    private final TextView playbackElapsedTime;

    private Tone tone;
    private TonePlayer tonePlayer;

    private volatile boolean manageBar;

    public PlaybackManager(Context context, Handler handler, ImageButton playPauseBtn, SeekBar playbackBar, TextView playbackElapsedTime) {
        this.context = context;
        this.handler = handler;
        this.playPauseBtn = playPauseBtn;
        this.playbackBar = playbackBar;
        this.playbackElapsedTime = playbackElapsedTime;
        tone = null;
        tonePlayer = null;
        manageBar = false;
        playPauseBtn.setTag(PLAYBACK_STATE_NOT_PLAYING);
    }

    public void setTone(Tone tone) {
        if (manageBar)
            stopPlayback();
        this.tone = tone;
        tonePlayer = new TonePlayer(tone);
        tonePlayer.load(context);

        resetPlaybackBar();
    }

    public void resetTone() {
        // TODO: looper functionality
    }

    public void startPlayback() {
        tonePlayer.play();
        manageBar = true;
        managePlayPauseButton(true);
    }

    public void stopPlayback() {
        tonePlayer.stop();
        manageBar = false;
        managePlayPauseButton(false);
        resetPlaybackBar();
    }

    public void pausePlayback() {
        tonePlayer.pause();
        manageBar = false;
        managePlayPauseButton(false);
    }

    public boolean managePlayPauseActivity() {
        Object buttonState = playPauseBtn.getTag();
        boolean playing = false;

        if (buttonState.equals(PLAYBACK_STATE_PLAYING))
            pausePlayback();
        else {
            startPlayback();
            playing = true;
        }
        return playing;
    }

    @Override
    public void run() {
        final int barRefreshRate = 100; // ms
        final int endingPoint = Constants.SAMPLE_RATE.value * tone.getDuration();
        final int barDivider = tone.getDuration() * 100;

        while (manageBar) {
            updatePlaybackBar(endingPoint, barDivider);
            try {
                Thread.sleep(barRefreshRate);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void updatePlaybackBar(int endingPoint, int barDivider) {
        int playbackPosition = tonePlayer.getPlaybackPosition();
        if (playbackPosition >= endingPoint) {
            manageBar = false;
            managePlayPauseButton(false);
            playbackPosition = (int) Math.round(endingPoint / (double) barDivider);
            setPlaybackBarValues(playbackPosition, tone.getDuration());
            return;
        }

        int finalPlaybackPosition = (int) Math.round(playbackPosition / (double) barDivider);
        int finalElapsedTime = (int) Math.floor(playbackPosition / (double) Constants.SAMPLE_RATE.value);

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

    private void resetPlaybackBar() {
        handler.post(new Runnable(){
            public void run() {
                playbackBar.setProgress(0);
                playbackElapsedTime.setText("0");
            }
        });
    }

    private void managePlayPauseButton(boolean isPlaying) {
        if (isPlaying) {
            handler.post(new Runnable() {
                public void run() {
                    playPauseBtn.setBackgroundResource(R.drawable.pause_btn);
                    playPauseBtn.setTag(PLAYBACK_STATE_PLAYING);
                }
            });
        } else {
            handler.post(new Runnable() {
                public void run() {
                    playPauseBtn.setBackgroundResource(R.drawable.play_btn);
                    playPauseBtn.setTag(PLAYBACK_STATE_NOT_PLAYING);
                }
            });
        }
    }
}
