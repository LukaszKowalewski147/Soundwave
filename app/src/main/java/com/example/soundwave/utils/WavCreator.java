package com.example.soundwave.utils;

import android.os.Environment;
import android.util.Log;

import com.example.soundwave.components.Music;
import com.example.soundwave.components.Tone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public class WavCreator {
    private final String TAG = "WavCreator";

    private static final String FILE_FOLDER_TONES = "myTones";
    private static final String FILE_FOLDER_MUSIC = "myMusic";
    private final String FILE_EXTENSION = ".wav";

    private final Tone tone;
    private final Music music;
    private boolean success;

    public WavCreator(Tone tone) {
        this.tone = tone;
        this.success = false;
        this.music = null;
    }

    public WavCreator(Music music) {
        this.music = music;
        this.success = false;
        this.tone = null;
    }

    public static String getFileFolderTones() {
        return FILE_FOLDER_TONES;
    }

    public static String getFileFolderMusic() {
        return FILE_FOLDER_MUSIC;
    }

    public boolean isSuccess() {
        return success;
    }

    public void download() {
        FileOutputStream out = null;
        if (isExternalStorageAvailable()) {
            File filepathBase = getFilepathBase();
            if (filepathBase == null)
                return;

            String fileName = getFilename();
            File wavFile = new File(filepathBase, fileName);
            byte[] pcmSamples = getPcmSamples();
            try {
                out = new FileOutputStream(wavFile);
                writeWavHeader(out);
                out.write(pcmSamples);
                updateWavHeader(wavFile);
                success = true;
            } catch (IOException e) {
                Log.e(TAG, "Writing wave file failed", e);
            } finally {
                try {
                    if (out != null)
                        out.close();
                } catch (IOException e) {
                    Log.e(TAG, "Closing wave file after failing to write it failed", e);
                }
            }
        }
    }

    private File getFilepathBase() {
        String filepathToDownload = tone != null ? Options.filepathToDownloadTones : Options.filepathToDownloadMusic;

        if (!filepathToDownload.isEmpty())
            return new File(filepathToDownload);

        return null;
    }

    private String getFilename() {
        String name = tone != null ? Objects.requireNonNull(tone).getName() : Objects.requireNonNull(music).getName();

        return name + FILE_EXTENSION;
    }

    private int getSampleRate() {
        return tone != null ? Objects.requireNonNull(tone).getSampleRate().sampleRate : Objects.requireNonNull(music).getSampleRate().sampleRate;
    }

    private byte[] getPcmSamples() {
        return tone != null ? Objects.requireNonNull(tone).getPcmSound() : Objects.requireNonNull(music).getSamples16BitPCM();
    }

    private boolean isExternalStorageAvailable() {
        String externalStorageState = Environment.getExternalStorageState();
        return externalStorageState.equals(Environment.MEDIA_MOUNTED);
    }

    private void writeWavHeader(OutputStream out) throws IOException {
        final int sampleRate = getSampleRate();
        short channels = 1;
        short bitDepth = 16;

        byte[] littleBytes = ByteBuffer
                .allocate(14)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort(channels)
                .putInt(sampleRate)
                .putInt(sampleRate * channels * (bitDepth / 8))
                .putShort((short) (channels * (bitDepth / 8)))
                .putShort(bitDepth)
                .array();

        out.write(new byte[]{
                // RIFF header
                'R', 'I', 'F', 'F', // ChunkID
                0, 0, 0, 0, // ChunkSize (must be updated later)
                'W', 'A', 'V', 'E', // Format
                // fmt subChunk
                'f', 'm', 't', ' ', // SubChunk1ID
                16, 0, 0, 0, // SubChunk1Size
                1, 0, // AudioFormat
                littleBytes[0], littleBytes[1], // NumChannels
                littleBytes[2], littleBytes[3], littleBytes[4], littleBytes[5], // SampleRate
                littleBytes[6], littleBytes[7], littleBytes[8], littleBytes[9], // ByteRate
                littleBytes[10], littleBytes[11], // BlockAlign
                littleBytes[12], littleBytes[13], // BitsPerSample
                // data subChunk
                'd', 'a', 't', 'a', // SubChunk2ID
                0, 0, 0, 0, // SubChunk2Size (must be updated later)
        });
    }

    private void updateWavHeader(File wav) throws IOException {
        byte[] sizes = ByteBuffer
                .allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt((int) (wav.length() - 8)) // ChunkSize
                .putInt((int) (wav.length() - 44)) // SubChunk2Size
                .array();

        RandomAccessFile accessWave = null;

        try {
            accessWave = new RandomAccessFile(wav, "rw");
            // ChunkSize
            accessWave.seek(4);
            accessWave.write(sizes, 0, 4);

            // SubChunk2Size
            accessWave.seek(40);
            accessWave.write(sizes, 4, 4);
        } finally {
            if (accessWave != null) {
                try {
                    accessWave.close();
                } catch (IOException ex) {
                    Log.e(TAG, "Updating wave file header failed", ex);
                }
            }
        }
    }
}
