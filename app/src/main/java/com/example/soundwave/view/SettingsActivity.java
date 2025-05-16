package com.example.soundwave.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

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

        setObservers();
        setListeners();
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
        setupViewLanguage();
        setupViewStorage();
    }

    private void setupViewLanguage() {
        //TODO: a line below is getting the first element of app_languages from strings.xml, get it from viewModel.getSelectedLanguage();
        String selectedLanguage = getResources().getStringArray(R.array.app_languages)[0];

        binding.settingsLanguageBtn.setText(selectedLanguage);
    }

    private void setupViewStorage() {
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

    private void setDbTonesView() {
        int dbTonesNumber = viewModel.getNumberOfDbTones();

        if (dbTonesNumber == 0) {
            binding.settingsStorageDeleteDatabaseTonesBtn.setEnabled(false);
            binding.settingsStorageDeleteDatabaseTonesBtn.setBackgroundResource(R.drawable.background_btn_inactive);
        }

        binding.settingsStorageTonesDatabaseNumber.setText(String.valueOf(dbTonesNumber));
    }

    private void setDbMusicView() {
        int dbMusicNumber = viewModel.getNumberOfDbMusic();

        if (dbMusicNumber == 0) {
            binding.settingsStorageDeleteDatabaseMusicBtn.setEnabled(false);
            binding.settingsStorageDeleteDatabaseMusicBtn.setBackgroundResource(R.drawable.background_btn_inactive);
        }

        binding.settingsStorageMusicDatabaseNumber.setText(String.valueOf(dbMusicNumber));
    }

    private void setObservers() {
        viewModel.getAllTones().observe(this, tones -> setDbTonesView());

        viewModel.getAllMusic().observe(this, music -> setDbMusicView());
    }

    private void setListeners() {
        binding.settingsAccountBtn.setOnClickListener(view -> startAccountActivity());

        binding.settingsStorageDeleteDatabaseTonesBtn.setOnClickListener(view -> deleteDatabaseTonesDialog());

        binding.settingsStorageDeleteDatabaseMusicBtn.setOnClickListener(view -> deleteDatabaseMusicDialog());
    }

    private void startAccountActivity() {
        Intent intent = new Intent(SettingsActivity.this, AccountActivity.class);
        startActivity(intent);
    }

    private void deleteDatabaseTonesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_dialog_settings_delete_database_tones_message);
        builder.setPositiveButton(R.string.alert_dialog_settings_delete_database_tones_positive, (dialog, id) -> viewModel.deleteAllDatabaseTones().observe(this, success -> {
            if (success)
                Toast.makeText(this, R.string.alert_dialog_settings_delete_database_tones_success, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, R.string.alert_dialog_settings_delete_database_tones_fail, Toast.LENGTH_SHORT).show();
        }));
        builder.setNegativeButton(R.string.alert_dialog_settings_delete_database_tones_negative, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteDatabaseMusicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.alert_dialog_settings_delete_database_music_message);
        builder.setPositiveButton(R.string.alert_dialog_settings_delete_database_music_positive, (dialog, id) -> viewModel.deleteAllDatabaseMusic().observe(this, success -> {
            if (success)
                Toast.makeText(this, R.string.alert_dialog_settings_delete_database_music_success, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, R.string.alert_dialog_settings_delete_database_music_fail, Toast.LENGTH_SHORT).show();
        }));
        builder.setNegativeButton(R.string.alert_dialog_settings_delete_database_music_negative, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}