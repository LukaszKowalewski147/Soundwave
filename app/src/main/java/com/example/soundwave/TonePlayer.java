package com.example.soundwave;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.widget.Toast;

public class TonePlayer {
    private final Tone tone;
    private AudioTrack audioTrack;

    public TonePlayer(Tone tone) {
        this.tone = tone;
        audioTrack = null;
    }

    public void play(Context context) {
        if (!isReady()) {
            Toast.makeText(context, "Błąd generatora dźwięku", Toast.LENGTH_SHORT).show();
            return;
        }
        audioTrack.flush();
        audioTrack.write(tone.getSinWaveData(), 0, tone.getSinWaveData().length);
        audioTrack.play();
        Toast.makeText(context, "Odtwarzanie...", Toast.LENGTH_SHORT).show();
    }

    public void stop(Context context) {
        if (isPlaying()) {
            fadeOutStop();
            audioTrack.stop();
            audioTrack.release();
            Toast.makeText(context, "Zatrzymano odtwarzanie", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isReady() {
        if (audioTrack == null)
            buildAudioTrack();
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
                        .setSampleRate(Constants.SAMPLE_RATE.value)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(tone.getSinWaveData().length)
                .build();
    }
}
