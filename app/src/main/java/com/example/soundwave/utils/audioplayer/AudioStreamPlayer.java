package com.example.soundwave.utils.audioplayer;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.util.Log;

import com.example.soundwave.utils.SampleRate;

public class AudioStreamPlayer extends AudioPlayer {
    private final String TAG = "AudioStreamPlayer";

    private Thread playbackThread;
    private double streamFrequency;
    private boolean isPlaying;

    public AudioStreamPlayer() {
        sampleRate = SampleRate.RATE_44_1_KHZ.sampleRate;
        bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        streamFrequency = 440.0d;
        isPlaying = false;

        buildAudioTrack();
    }

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
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();
    }

    @Override
    public void play() {
        if (isPlaying) {
            Log.i(TAG, "Play: audioTrack is already playing");
            return;
        }
        isPlaying = true;
        playbackThread = new Thread(this::generateAndPlaySound);
        playbackThread.start();
    }

    @Override
    public void stop() {
        if (!isPlaying) {
            Log.i(TAG, "Stop: audioTrack is already stopped");
            return;
        }
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

        short[] buffer = new short[bufferSize];
        double angle = 0;
        while (isPlaying) {
            double currentFrequency = getStreamFrequency();
            double increment = 2.0 * Math.PI * currentFrequency / sampleRate;

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
