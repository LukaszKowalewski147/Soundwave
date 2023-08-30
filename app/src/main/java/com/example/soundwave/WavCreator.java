package com.example.soundwave;

import android.os.Environment;

import com.example.soundwave.utils.UnitsConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class WavCreator {

    private static final String FILE_FOLDER = "myTones";
    private final File filepathBase;
    private final Tone tone;
    private boolean success;

    public WavCreator(Tone tone, File filepathBase) {
        this.filepathBase = filepathBase;
        this.tone = tone;
        this.success = false;
    }

    public static String getFileFolder() {
        return FILE_FOLDER;
    }

    public boolean isSuccess() {
        return success;
    }

    public void saveSound() {
        FileOutputStream out = null;
        if (isExternalStorageAvailable()) {
            String fileName = getFilename();
            File wavFile = new File(filepathBase, fileName);
            try {
                out = new FileOutputStream(wavFile);
                writeWavHeader(out);
                out.write(tone.getSamples());
                updateWavHeader(wavFile);
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null)
                        out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getFilename() {
        String fileExtension = ".wav";
        String sampleRateTxt = UnitsConverter.convertSampleRateToStringFile(tone.getSampleRate()) + "-";
        return tone.getFundamentalFrequency() + "Hz-" + tone.getDurationInSeconds() + "s-" + sampleRateTxt + System.currentTimeMillis() + fileExtension;
    }

    private boolean isExternalStorageAvailable() {
        String externalStorageState = Environment.getExternalStorageState();
        return externalStorageState.equals(Environment.MEDIA_MOUNTED);
    }

    private void writeWavHeader(OutputStream out) throws IOException {
        final int sampleRate = tone.getSampleRate().sampleRate;
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
        } finally {
            if (accessWave != null) {
                try {
                    accessWave.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
