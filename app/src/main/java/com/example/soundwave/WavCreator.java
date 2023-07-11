package com.example.soundwave;

import android.content.Context;
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

public class WavCreator {

    private static final String FILE_EXTENSION = ".wav";
    private static final String FILE_FOLDER = "mySounds";

    private Context context;
    private Sound sound;

    public WavCreator(Context context, Sound sound) {
        this.context = context;
        this.sound = sound;
    }

    public void saveSound() {
        FileOutputStream out = null;
        if (isExternalStorageAvailable()) {
            String fileName = getFilename();
            Toast.makeText(context, "Nazwa pliku: " + fileName, Toast.LENGTH_SHORT).show();
            File wavFile = new File(context.getExternalFilesDir(FILE_FOLDER), fileName);
            try {
                out = new FileOutputStream(wavFile);
                writeWavHeader(out);
                out.write(sound.getSinWaveData());
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
        SampleRate samplingRate = sound.getSampleRate();
        String sampleingRateTxt = "?kHz-";
        switch (samplingRate) {
            case RATE_44_1_KHZ:
                sampleingRateTxt = "44_1kHz-";
                break;
            case RATE_48_KHZ:
                sampleingRateTxt = "48kHz-";
                break;
            case RATE_96_KHZ:
                sampleingRateTxt = "96kHz-";
                break;
            case RATE_192_KHZ:
                sampleingRateTxt = "192kHz-";
                break;
        }
        String filename = sound.getNumberOfTones() + "tone-" + sound.getDuration() + "s-" + sampleingRateTxt + System.currentTimeMillis() + FILE_EXTENSION;
        String vol_debug_filename = sound.getNumberOfTones() + "tone-" + sound.getDuration() + "s-" + sampleingRateTxt + "vol" + sound.tmp_debug_volume + FILE_EXTENSION;
        return filename;
    }

    private boolean isExternalStorageAvailable() {
        String externalStorageState = Environment.getExternalStorageState();
        if (externalStorageState.equals(Environment.MEDIA_MOUNTED))
            return true;
        return false;
    }

    private void writeWavHeader(OutputStream out) throws IOException {
        final int samplingRate = sound.getSampleRate().sampleRate;
        short channels = 1;
        short bitDepth = 16;

        byte[] littleBytes = ByteBuffer
                .allocate(14)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putShort(channels)
                .putInt(samplingRate)
                .putInt(samplingRate * channels * (bitDepth / 8))
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
