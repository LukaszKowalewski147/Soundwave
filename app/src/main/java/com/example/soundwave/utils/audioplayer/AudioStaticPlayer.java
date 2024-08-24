package com.example.soundwave.utils.audioplayer;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import com.example.soundwave.components.sound.Listenable;

public class AudioStaticPlayer extends AudioPlayer {
    private final String TAG = "AudioStaticPlayer";

    @Override
    public void buildAudioTrack() {
        audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(bufferSize)
                .build();
    }

    @Override
    public void play() {
        audioTrack.play();
    }

    @Override
    public void stop() {
        if (isPlaying()) {
            fadeOutStop();
            audioTrack.stop();
            audioTrack.setVolume(1.0f);
        }
    }

    public boolean loadSound(Listenable sound) {
        if (!isReadyToWriteSound(sound)) {
            Log.e(TAG, "Load sound: audioTrack not ready to write sound");
            return false;
        }
        audioTrack.flush();
        audioTrack.write(sound.getPcmData(), 0, bufferSize);

        if (isReadyToPlay())
            return true;

        Log.e(TAG, "Load sound: audioTrack not ready to play sound");
        return false;
    }

    public void reload() {
        audioTrack.reloadStaticData();
    }

    private boolean isPlaying() {
        if (audioTrack != null)
            return audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;

        return false;
    }

    private boolean isReadyToWriteSound(Listenable sound) {
        if (audioTrack == null) {
            getPcmDataLength(sound);
            getSampleRate(sound);
            buildAudioTrack();
        }
        return audioTrack.getState() != AudioTrack.STATE_UNINITIALIZED;
    }

    private void getPcmDataLength(Listenable sound) {
        bufferSize = sound.getPcmData().length;
    }

    private void getSampleRate(Listenable sound) {
        sampleRate = sound.getSampleRate().sampleRate;
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
}
