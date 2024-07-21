package com.example.soundwave.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.widget.Toast;

import com.example.soundwave.components.Music;
import com.example.soundwave.components.Tone;

public class AudioPlayer {
    private AudioTrack audioTrack;

    public AudioPlayer() {
        audioTrack = null;
    }

    public void loadTone(Tone tone) {
        if (!isReadyToWriteTone(tone)) {
            // TODO: getErrorMessage
            //Toast.makeText(context, "Błąd generatora dźwięku: " + audioTrack.getState(), Toast.LENGTH_SHORT).show();
            return;
        }
        audioTrack.flush();
        audioTrack.write(tone.getPcmSound(), 0, tone.getPcmSound().length);
        /*if (!isReadyToPlay()) {
            // TODO: getErrorMessage
            //Toast.makeText(context, "Błąd generatora dźwięku: " + audioTrack.getState(), Toast.LENGTH_SHORT).show();
            //return;
        }*/
    }

    public void loadMusic(Music music) {
        if (!isReadyToWriteMusic(music)) {
            // TODO: getErrorMessage
            //Toast.makeText(context, "Błąd generatora dźwięku: " + audioTrack.getState(), Toast.LENGTH_SHORT).show();
            return;
        }
        audioTrack.flush();
        audioTrack.write(music.getSamples16BitPCM(), 0, music.getSamples16BitPCM().length);
        /*if (!isReadyToPlay()) {
            // TODO: getErrorMessage
            //Toast.makeText(context, "Błąd generatora dźwięku: " + audioTrack.getState(), Toast.LENGTH_SHORT).show();
            //return;
        }*/
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
        audioTrack.setVolume(0.0f);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
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

    public void extra(Context context) {
        Toast.makeText(context, "Playback position: " + audioTrack.getPlaybackHeadPosition(), Toast.LENGTH_SHORT).show();
    }
}
