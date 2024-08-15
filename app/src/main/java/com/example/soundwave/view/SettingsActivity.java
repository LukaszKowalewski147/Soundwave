package com.example.soundwave.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.soundwave.R;
import com.example.soundwave.databinding.ActivitySettingsBinding;
import com.example.soundwave.utils.Options;
import com.example.soundwave.viewmodel.SettingsViewModel;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private SettingsViewModel viewModel;
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        setContentView(binding.getRoot());

        setupActionBar();
        setupView();
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
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

        float tonesDirSize = viewModel.getDirectorySizeInMB(tonesDir);
        float musicDirSize = viewModel.getDirectorySizeInMB(musicDir);

        String tonesDirSizeText = String.format(Locale.US, "%.2f", tonesDirSize) + getString(R.string.unit_megabyte);
        String musicDirSizeText = String.format(Locale.US, "%.2f", musicDirSize) + getString(R.string.unit_megabyte);

        binding.settingsStorageTonesFilepath.setText(tonesDir);
        binding.settingsStorageMusicFilepath.setText(musicDir);
        binding.settingsStorageTonesDownloadedSize.setText(tonesDirSizeText);
        binding.settingsStorageMusicDownloadedSize.setText(musicDirSizeText);
    }
}