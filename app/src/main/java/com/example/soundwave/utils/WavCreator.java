package com.example.soundwave.utils;

import android.os.Environment;
import android.util.Log;

import com.example.soundwave.components.sound.ListenableSound;
import com.example.soundwave.components.sound.Music;
import com.example.soundwave.components.sound.Tone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WavCreator {
    private final String TAG = "WavCreator";

    private static final String FILE_FOLDER_TONES = "myTones";
    private static final String FILE_FOLDER_MUSIC = "myMusic";
    private final String FILE_EXTENSION = ".wav";

    private final ListenableSound sound;
    private boolean success;

    public WavCreator(ListenableSound sound) {
        this.sound = sound;
        this.success = false;
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
            byte[] pcmData = getPcmData();
            try {
                out = new FileOutputStream(wavFile);
                writeWavHeader(out);
                out.write(pcmData);
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
        String filepathToDownload = "";

        if (sound instanceof Tone)
            filepathToDownload = Options.filepathToDownloadTones;
        else if (sound instanceof Music)
            filepathToDownload = Options.filepathToDownloadMusic;

        if (!filepathToDownload.isEmpty())
            return new File(filepathToDownload);

        return null;
    }

    private String getFilename() {
        return sound.getName() + FILE_EXTENSION;
    }

    private int getSampleRate() {
        return sound.getSampleRate().sampleRate;
    }

    private byte[] getPcmData() {
        return sound.getPcmData();
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
