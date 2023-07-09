package com.example.soundwave;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.widget.Toast;

public class AudioPlayer {
    private final Sound sound;
    private AudioTrack audioTrack;

    public AudioPlayer(Sound sound) {
        this.sound = sound;
        audioTrack = null;
    }

    public void load(Context context) {
        if (!isReadyToWrite()) {
            Toast.makeText(context, "Błąd generatora dźwięku: " + audioTrack.getState(), Toast.LENGTH_SHORT).show();
            return;
        }
        audioTrack.flush();
        audioTrack.write(sound.getSinWaveData(), 0, sound.getSinWaveData().length);
        if (!isReadyToPlay()) {
            Toast.makeText(context, "Błąd generatora dźwięku: " + audioTrack.getState(), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    public void reload() {
        audioTrack.reloadStaticData();
    }

    public void play() {
        audioTrack.play();
    }

    public void pause() {
        if (isPlaying()) {
            audioTrack.pause();
        }
    }

    public void stop() {
        if (isPlaying()) {
            fadeOutStop();
            audioTrack.stop();
            audioTrack.setVolume(1.0f);
        }
    }

    public int getPlaybackPosition() {
        return audioTrack.getPlaybackHeadPosition();
    }

    private boolean isReadyToWrite() {
        if (audioTrack == null)
            buildAudioTrack();
        return audioTrack.getState() != AudioTrack.STATE_UNINITIALIZED;
    }

    private boolean isReadyToPlay() {
        return audioTrack.getState() == AudioTrack.STATE_INITIALIZED;
    }

    private boolean isPlaying() {
        if (audioTrack != null)
            return audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
        return false;
    }

    private void fadeOutStop() {
        audioTrack.setVolume(0.0f);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void buildAudioTrack() {
        audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sound.getSampleRate().sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(sound.getSinWaveData().length)
                .build();
    }

    public void extra(Context context) {
        Toast.makeText(context, "Playback position: " + audioTrack.getPlaybackHeadPosition(), Toast.LENGTH_SHORT).show();
    }
}