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
    private volatile boolean loopEnabled;

    public PlaybackManager(Context context, Handler handler, ImageButton playPauseBtn, SeekBar playbackBar, TextView playbackElapsedTime) {
        this.context = context;
        this.handler = handler;
        this.playPauseBtn = playPauseBtn;
        this.playbackBar = playbackBar;
        this.playbackElapsedTime = playbackElapsedTime;
        tone = null;
        tonePlayer = null;
        manageBar = false;
        loopEnabled = false;
        playPauseBtn.setTag(PLAYBACK_STATE_NOT_PLAYING);
    }

    public void setTone(Tone tone) {
        if (manageBar)
            stopPlayback();
        manageBar = false;
        managePlayPauseButton(false);
        this.tone = tone;
        tonePlayer = new TonePlayer(tone);
        tonePlayer.load(context);

        resetPlaybackBarValues();
    }

    public void startPlayback() {
        tonePlayer.play();
        manageBar = true;
        managePlayPauseButton(true);
    }

    public void pausePlayback() {
        tonePlayer.pause();
        manageBar = false;
        managePlayPauseButton(false);
    }

    public void resetPlayback() {
        stopPlayback();
        manageBar = false;
        managePlayPauseButton(false);
        resetPlaybackBarValues();
        tonePlayer.reload();
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

    public void setLooperState(boolean loopEnabled) {
        this.loopEnabled = loopEnabled;
    }

    @Override
    public void run() {
        final int samplingRate = tone.getSamplingRate().samplingRate;
        final int barRefreshRate = 40; // 40 ms = 25FPS
        final int endingPoint = samplingRate * tone.getDuration();
        final int barDivider = tone.getDuration() * 100;

        while (manageBar) {
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
            if (loopEnabled) {
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

    private void stopPlayback() {
        tonePlayer.stop();
        if (!loopEnabled) {
            manageBar = false;
            managePlayPauseButton(false);
        }
    }

    private void reloadTone() {
        stopPlayback();
        tonePlayer.reload();
        tonePlayer.play();
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

    public void extra() {
        tonePlayer.extra(context);
    }
}
