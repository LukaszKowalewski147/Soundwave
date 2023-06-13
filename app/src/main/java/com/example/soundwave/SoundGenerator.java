package com.example.soundwave;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SoundGenerator {

    private static final String FILE_EXTENSION = ".wav";
    private static final String FILE_FOLDER = "myTones";

    private Context context;
    private AudioTrack audioTrack;

    private final int numberOfSamples;
    private final int frequency;        // in Hz
    private final int duration;         // in s
    private final double sample[];
    private final byte outputSound[];

    public SoundGenerator(Context context, int frequency, int duration) {
        this.context = context;
        this.frequency = frequency;
        this.duration = duration;

        numberOfSamples = duration * Constants.SAMPLE_RATE.value;
        sample = new double[numberOfSamples];
        outputSound = new byte[2 * numberOfSamples];

        genTone();
    }

    public void play() {
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
                .setBufferSizeInBytes(outputSound.length)
                .build();
        audioTrack.write(outputSound, 0, outputSound.length);
        audioTrack.play();
        Toast.makeText(context, "Odtwarzanie...", Toast.LENGTH_SHORT).show();
    }

    public void stop() {
        audioTrack.stop();
        audioTrack.release();
        Toast.makeText(context, "Zatrzymano odtwarzanie", Toast.LENGTH_SHORT).show();
    }

    public void saveTone() {
        FileOutputStream out = null;
        if (isExternalStorageAvailable())
        {
            String fileName = getFilename();
            Toast.makeText(context, "Nazwa pliku: " + fileName, Toast.LENGTH_LONG).show();
            File wavFile = new File(context.getExternalFilesDir(FILE_FOLDER), fileName);
            try {
                out = new FileOutputStream(wavFile);
                writeWavHeader(out);
                out.write(outputSound);
                updateWavHeader(wavFile);
                Toast.makeText(context, "Zapisano do: " + context.getExternalFilesDir(FILE_FOLDER), Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(context, "Błąd zapisu", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(context, "Błąd zapisu", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } finally {
                try {
                    if (out != null)
                        out.close();
                } catch (IOException e) {
                    Toast.makeText(context, "Błąd zamknięcia pliku", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private void genTone() {
        for (int i = 0; i < numberOfSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i/(Constants.SAMPLE_RATE.value/(double)frequency));
        }

        // convert to 16 bit pcm sound array
        int index = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            outputSound[index++] = (byte) (val & 0x00ff);
            outputSound[index++] = (byte) ((val & 0xff00) >>> 8);
        }
    }

    private String getFilename() {
        String filename = frequency + "hz-" + duration + "s-" + System.currentTimeMillis() + FILE_EXTENSION;
        return filename;
    }

    private boolean isExternalStorageAvailable() {
        String externalStorageState = Environment.getExternalStorageState();
        if (externalStorageState.equals(Environment.MEDIA_MOUNTED))
            return true;
        return false;
    }

    private static void writeWavHeader(OutputStream out) throws IOException {
        short channels = 1;
        short bitDepth = 16;

        byte[] littleBytes = ByteBuffer
                .allocate(14)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort(channels)
                .putInt(Constants.SAMPLE_RATE.value)
                .putInt(Constants.SAMPLE_RATE.value * channels * (bitDepth / 8))
                .putShort((short) (channels * (bitDepth / 8)))
                .putShort(bitDepth)
                .array();

        out.write(new byte[]{
                // RIFF header
                'R', 'I', 'F', 'F', // ChunkID
                0, 0, 0, 0, // ChunkSize (must be updated later)
                'W', 'A', 'V', 'E', // Format
                // fmt subchunk
                'f', 'm', 't', ' ', // Subchunk1ID
                16, 0, 0, 0, // Subchunk1Size
                1, 0, // AudioFormat
                littleBytes[0], littleBytes[1], // NumChannels
                littleBytes[2], littleBytes[3], littleBytes[4], littleBytes[5], // SampleRate
                littleBytes[6], littleBytes[7], littleBytes[8], littleBytes[9], // ByteRate
                littleBytes[10], littleBytes[11], // BlockAlign
                littleBytes[12], littleBytes[13], // BitsPerSample
                // data subchunk
                'd', 'a', 't', 'a', // Subchunk2ID
                0, 0, 0, 0, // Subchunk2Size (must be updated later)
        });
    }

    private static void updateWavHeader(File wav) throws IOException {
        byte[] sizes = ByteBuffer
                .allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt((int) (wav.length() - 8)) // ChunkSize
                .putInt((int) (wav.length() - 44)) // Subchunk2Size
                .array();

        RandomAccessFile accessWave = null;

        try {
            accessWave = new RandomAccessFile(wav, "rw");
            // ChunkSize
            accessWave.seek(4);
            accessWave.write(sizes, 0, 4);

            // Subchunk2Size
            accessWave.seek(40);
            accessWave.write(sizes, 4, 4);
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (accessWave != null) {
                try {
                    accessWave.close();
                } catch (IOException ex) {

                }
            }
        }
    }
}
