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
    private Thread playbackThread;

    private final int streamSampleRate;
    private int streamBufferSize;
    private double streamFrequency = 440.0;
    private boolean isPlaying = false;

    public AudioPlayer() {
        audioTrack = null;
        streamSampleRate = 0;
    }

    public AudioPlayer(int streamSampleRate) {
        this.streamSampleRate = streamSampleRate;
        streamBufferSize = AudioTrack.getMinBufferSize(streamSampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        buildAudioTrackStream();
    }

    public boolean loadTone(Tone tone) {
        if (!isReadyToWriteTone(tone)) {
            Log.e(TAG, "Load tone: audioTrack not ready to write tone");
            return false;
        }
        audioTrack.flush();
        audioTrack.write(tone.getPcmSound(), 0, tone.getPcmSound().length);

        if (isReadyToPlay())
            return true;

        Log.e(TAG, "Load tone: audioTrack not ready to play tone");
        return false;
    }

    public boolean loadMusic(Music music) {
        if (!isReadyToWriteMusic(music)) {
            Log.e(TAG, "Load music: audioTrack not ready to write music");
            return false;
        }
        audioTrack.flush();
        audioTrack.write(music.getSamples16BitPCM(), 0, music.getSamples16BitPCM().length);

        if (isReadyToPlay())
            return true;

        Log.e(TAG, "Load music: audioTrack not ready to play music");
        return false;
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

    private void buildAudioTrackStream() {
        audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(streamSampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(streamBufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();
    }

    public void startPlaying() {
        if (isPlaying) return;

        isPlaying = true;
        playbackThread = new Thread(this::generateAndPlaySound);
        playbackThread.start();
    }

    public void stopPlaying() {
        if (!isPlaying) return;

        isPlaying = false;
        try {
            playbackThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        audioTrack.stop();
        audioTrack.flush();
    }

    public synchronized void setStreamFrequency(double streamFrequency) {
        this.streamFrequency = streamFrequency;
    }

    private synchronized double getStreamFrequency() {
        return streamFrequency;
    }

    private void generateAndPlaySound() {
        audioTrack.play();

        short[] buffer = new short[streamBufferSize];
        double angle = 0;
        while (isPlaying) {
            double currentFrequency = getStreamFrequency();
            double increment = 2.0 * Math.PI * currentFrequency / streamSampleRate;

            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = (short) (Math.sin(angle) * Short.MAX_VALUE);
                angle += increment;
                if (angle > 2.0 * Math.PI) {
                    angle -= 2.0 * Math.PI;
                }
            }
            audioTrack.write(buffer, 0, buffer.length);
        }
    }
}
