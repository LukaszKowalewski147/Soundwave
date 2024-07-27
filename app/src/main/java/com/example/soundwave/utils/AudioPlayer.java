package com.example.soundwave.utils;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import com.example.soundwave.components.Music;
import com.example.soundwave.components.Tone;

public class AudioPlayer {
    private final String TAG = "AudioPlayer";

    private AudioTrack audioTrack;

    public AudioPlayer() {
        audioTrack = null;
    }

    public boolean loadTone(Tone tone) {
        if (!isReadyToWriteTone(tone)) {
            Log.e(TAG, "Load tone: audioTrack not ready to write tone");
            return false;
        }
        audioTrack.flush();
        audioTrack.write(tone.getPcmSound(), 0, tone.getPcmSound().length);

        if (!isReadyToPlay()) {
            Log.e(TAG, "Load tone: audioTrack not ready to play tone");
            return false;
        }
        return true;
    }

    public boolean loadMusic(Music music) {
        if (!isReadyToWriteMusic(music)) {
            Log.e(TAG, "Load music: audioTrack not ready to write music");
            return false;
        }
        audioTrack.flush();
        audioTrack.write(music.getSamples16BitPCM(), 0, music.getSamples16BitPCM().length);

        if (!isReadyToPlay()) {
            Log.e(TAG, "Load music: audioTrack not ready to play music");
            return false;
        }
        return true;
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

    public boolean isPlaying() {
        if (audioTrack != null)
            return audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;

        return false;
    }

    public int getPlaybackPosition() {
        return audioTrack.getPlaybackHeadPosition();
    }

    private boolean isReadyToWriteTone(Tone tone) {
        if (audioTrack == null)
            buildAudioTrackTone(tone);

        return audioTrack.getState() != AudioTrack.STATE_UNINITIALIZED;
    }

    private boolean isReadyToWriteMusic(Music music) {
        if (audioTrack == null)
            buildAudioTrackMusic(music);

        return audioTrack.getState() != AudioTrack.STATE_UNINITIALIZED;
    }

    private boolean isReadyToPlay() {
        return audioTrack.getState() == AudioTrack.STATE_INITIALIZED;
    }

    private void fadeOutStop() {
        int fadeOutHoldTime = 50;   // time in ms
        audioTrack.setVolume(0.0f);

        try {
            Thread.sleep(fadeOutHoldTime);
        } catch (InterruptedException e) {
            Log.w(TAG, "Fade out stop: could not wait " + fadeOutHoldTime + "ms to fade out stop");
        }
    }

    private void buildAudioTrackTone(Tone tone) {
        audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(tone.getSampleRate().sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(tone.getPcmSound().length)
                .build();
    }

    private void buildAudioTrackMusic(Music music) {
        audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(music.getSampleRate().sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(music.getSamples16BitPCM().length)
                .build();
    }
}
