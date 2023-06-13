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

    private Context context;
    private static final int FILE_BPS = 16; // bits per sample
    private static final String FILE_EXTENSION = ".wav";
    private static final String FILE_FOLDER = "myTones";
    private static final String FILE_NAME = "myTone3.wav";

    private AudioTrack audioTrack;
    private final int sampleRate = 44100;   // in Hz (CD quality)
    private final int numberOfSamples;
    private final int frequency;         // in Hz
    private final double sample[];
    private final byte outputSound[];

    public SoundGenerator(Context context, int frequency, int duration) {
        this.context = context;
        this.frequency = frequency;
        numberOfSamples = duration * sampleRate;
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
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(outputSound.length)
                .build();
        audioTrack.write(outputSound, 0, outputSound.length);
        audioTrack.play();
    }

    public void stop() {
        audioTrack.stop();
        audioTrack.release();
    }

    private void genTone() {
        for (int i = 0; i < numberOfSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i/(sampleRate/(double)frequency));
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

    private String getFilename() {
        return (System.currentTimeMillis() + FILE_EXTENSION);
    }

    private boolean isExternalStorageAvailable() {
        String externalStorageState = Environment.getExternalStorageState();
        if (externalStorageState.equals(Environment.MEDIA_MOUNTED))
            return true;
        return false;
    }

    private static void writeWavHeader(OutputStream out) throws IOException {
        // Convert the multi-byte integers to raw bytes in little endian format as required by the spec

        short channels = 1;
        short bitDepth = 16;
        int sampleRate = 44100;

        byte[] littleBytes = ByteBuffer
                .allocate(14)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort(channels)
                .putInt(sampleRate)
                .putInt(sampleRate * channels * (bitDepth / 8))
                .putShort((short) (channels * (bitDepth / 8)))
                .putShort(bitDepth)
                .array();

        // Not necessarily the best, but it's very easy to visualize this way
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
                // There are probably a bunch of different/better ways to calculate
                // these two given your circumstances. Cast should be safe since if the WAV is
                // > 4 GB we've already made a terrible mistake.
                .putInt((int) (wav.length() - 8)) // ChunkSize
                .putInt((int) (wav.length() - 44)) // Subchunk2Size
                .array();

        RandomAccessFile accessWave = null;
        //noinspection CaughtExceptionImmediatelyRethrown
        try {
            accessWave = new RandomAccessFile(wav, "rw");
            // ChunkSize
            accessWave.seek(4);
            accessWave.write(sizes, 0, 4);

            // Subchunk2Size
            accessWave.seek(40);
            accessWave.write(sizes, 4, 4);
        } catch (IOException ex) {
            // Rethrow but we still close accessWave in our finally
            throw ex;
        } finally {
            if (accessWave != null) {
                try {
                    accessWave.close();
                } catch (IOException ex) {
                    //
                }
            }
        }
    }
}
