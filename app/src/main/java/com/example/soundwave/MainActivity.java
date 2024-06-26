package com.example.soundwave;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.soundwave.databinding.ActivityMainBinding;
import com.example.soundwave.model.entity.Tone;
import com.example.soundwave.utils.Options;
import com.example.soundwave.utils.WavCreator;
import com.example.soundwave.view.HomepageFragment;
import com.example.soundwave.view.ToneCreatorFragment;
import com.example.soundwave.view.ToneMixerFragment;
import com.example.soundwave.view.ToneStreamingFragment;
import com.example.soundwave.viewmodel.MainActivityViewModel;
import com.google.android.material.navigation.NavigationBarView;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainActivityViewModel mainActivityViewModel;
    boolean userClick = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFilepathToDownload();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        loadFragment(new HomepageFragment());
        binding.mainBottomNavView.setSelectedItemId(R.id.my_homepage);

        binding.mainBottomNavView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (userClick) {
                    switch (item.getItemId()) {
                        case R.id.tone_creator:
                            manageVisibilityOfTopMenu(false);
                            loadFragment(new ToneCreatorFragment());
                            break;
                        case R.id.tone_mixer:
                            manageVisibilityOfTopMenu(false);
                            loadFragment(new ToneMixerFragment());
                            break;
                        case R.id.my_homepage:
                            manageVisibilityOfTopMenu(true);
                            loadFragment(new HomepageFragment());
                            break;
                        case R.id.tone_streaming:
                            manageVisibilityOfTopMenu(false);
                            loadFragment(new ToneStreamingFragment());
                            break;
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (binding.mainBottomNavView.getSelectedItemId() == R.id.my_homepage) {
            super.onBackPressed();
        } else {
            binding.mainBottomNavView.setSelectedItemId(R.id.my_homepage);
        }
    }

    public void selectToneCreatorOnBottomNav() {
        if (binding != null) {
            manageVisibilityOfTopMenu(false);
            userClick = false;
            binding.mainBottomNavView.setSelectedItemId(R.id.tone_creator);
            userClick = true;
        }
    }

    private void setFilepathToDownload() {
        File externalFilesDir = getExternalFilesDir(WavCreator.getFileFolder());
        if (externalFilesDir != null) {
            Options.filepathToDownload = externalFilesDir.toString();
        } else {
            Toast.makeText(this, R.string.error_msg_filepath_to_download_fail, Toast.LENGTH_SHORT).show();
        }
    }

    private void manageVisibilityOfTopMenu(boolean visible) {
        if (visible) {
            final float scale = getResources().getDisplayMetrics().density;
            int pixels = (int) (48 * scale + 0.5f); //48dp

            binding.mainGuidelineTop.setGuidelineBegin(pixels);
            binding.mainTopMenu.setVisibility(View.VISIBLE);
        } else {
            binding.mainGuidelineTop.setGuidelineBegin(0);
            binding.mainTopMenu.setVisibility(View.GONE);
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_fragment_container, fragment);
        fragmentTransaction.commit();
    }
}