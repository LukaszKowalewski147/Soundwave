package com.example.soundwave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.soundwave.databinding.ActivitySettingsBinding;
import com.example.soundwave.utils.Options;

import java.io.File;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupActionBar();
        setupView();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Get back to MainActivity
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupActionBar() {
        setSupportActionBar(binding.settingsToolbar);

        // Enable back arrow button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupView() {
        String tonesDir = Options.filepathToDownloadTones;
        String musicDir = Options.filepathToDownloadMusic;

        float tonesDirSize = getDirectorySizeInMB(tonesDir);
        float musicDirSize = getDirectorySizeInMB(musicDir);

        String tonesDirSizeText = String.format(Locale.US, "%.2f", tonesDirSize) + getString(R.string.unit_megabyte);
        String musicDirSizeText = String.format(Locale.US, "%.2f", musicDirSize) + getString(R.string.unit_megabyte);

        binding.settingsStorageTonesFilepath.setText(tonesDir);
        binding.settingsStorageMusicFilepath.setText(musicDir);
        binding.settingsStorageTonesDownloadedSize.setText(tonesDirSizeText);
        binding.settingsStorageMusicDownloadedSize.setText(musicDirSizeText);
    }

    private float getDirectorySizeInMB(String directoryPath) {
        File directory = new File(directoryPath);
        long sizeInBytes = getDirectorySize(directory);

        return sizeInBytes / (1024f * 1024f);   // conversion to MB
    }

    private long getDirectorySize(File directory) {
        long length = 0;

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        length += file.length();
                    } else {
                        length += getDirectorySize(file);
                    }
                }
            }
        }
        return length;
    }
}