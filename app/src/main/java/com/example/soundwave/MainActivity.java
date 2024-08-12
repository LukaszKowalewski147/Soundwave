package com.example.soundwave;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.soundwave.components.Tone;
import com.example.soundwave.databinding.ActivityMainBinding;
import com.example.soundwave.utils.Options;
import com.example.soundwave.utils.WavCreator;
import com.example.soundwave.view.HomepageMusicFragment;
import com.example.soundwave.view.HomepageTonesFragment;
import com.example.soundwave.view.ToneCreatorFragment;
import com.example.soundwave.view.ToneMixerFragment;
import com.example.soundwave.view.ToneStreamingFragment;
import com.google.android.material.tabs.TabLayout;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Fragment currentFragment;
    private Bundle toneToEditBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setDisplayDensity();
        setTrackPaddingStart();
        setFilePathsToDownload();
        setFilepathToSavePcmSamples();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeView();
        setListeners();
        setBackButtonBehavior();
    }

    public void openToneCreator() {
        binding.mainBottomNavView.setSelectedItemId(R.id.tone_creator);
    }

    public void openToneMixer() {
        binding.mainBottomNavView.setSelectedItemId(R.id.tone_mixer);
    }

    public void openToneCreatorInEditionMode(Tone tone) {
        toneToEditBundle = new Bundle();
        toneToEditBundle.putSerializable("tone", tone);

        binding.mainBottomNavView.setSelectedItemId(R.id.tone_creator);
    }

    public void changeFragmentFromToneCreator(int fragmentId) {
        currentFragment = null;
        binding.mainBottomNavView.setSelectedItemId(fragmentId);
    }

    public void changeFragmentFromToneMixer(int fragmentId) {
        currentFragment = null;
        binding.mainBottomNavView.setSelectedItemId(fragmentId);
    }

    public void resetToneCreator() {
        loadFragment(new ToneCreatorFragment());
    }

    private void setDisplayDensity() {
        Options.displayDensity = getResources().getDisplayMetrics().density;
    }

    private void setTrackPaddingStart() {
        Options.trackPaddingStart = getResources().getDimensionPixelSize(R.dimen.tone_mixer_track_padding_start);
    }

    private void setFilePathsToDownload() {
        File externalFilesDirTones = getExternalFilesDir(WavCreator.getFileFolderTones());
        File externalFilesDirMusic = getExternalFilesDir(WavCreator.getFileFolderMusic());

        if (externalFilesDirTones != null)
            Options.filepathToDownloadTones = externalFilesDirTones.toString();
        else
            Toast.makeText(this, R.string.error_msg_filepath_to_download_tones_fail, Toast.LENGTH_SHORT).show();

        if (externalFilesDirMusic != null)
            Options.filepathToDownloadMusic = externalFilesDirMusic.toString();
        else
            Toast.makeText(this, R.string.error_msg_filepath_to_download_music_fail, Toast.LENGTH_SHORT).show();
    }

    private void setFilepathToSavePcmSamples() {
        File filesDir = getFilesDir();

        if (filesDir != null)
            Options.filepathToSavePcmSamples = filesDir.toString();
        else
            Toast.makeText(this, R.string.error_msg_filepath_to_save_samples_fail, Toast.LENGTH_SHORT).show();
    }

    private void initializeView() {
        currentFragment = new HomepageTonesFragment();
        loadFragment(currentFragment);

        binding.mainBottomNavView.setSelectedItemId(R.id.my_homepage);

        binding.mainTabLayout.addTab(binding.mainTabLayout.newTab().setText("Tones"));
        binding.mainTabLayout.addTab(binding.mainTabLayout.newTab().setText("Music"));
    }

    private void setListeners() {
        binding.mainTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentFragment = new HomepageTonesFragment();
                        break;
                    case 1:
                        currentFragment = new HomepageMusicFragment();
                        break;
                }
                loadFragment(currentFragment);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        binding.mainBottomNavView.setOnItemSelectedListener(item -> {
            int fragmentId = item.getItemId();

            // Making sure of safety exit of ToneCreatorFragment by onFragmentExit() if changes are not saved
            if ((currentFragment instanceof ToneCreatorFragment) && (fragmentId != R.id.tone_creator)) {
                if (!((ToneCreatorFragment) currentFragment).onFragmentExit(fragmentId))
                    return false;
            }

            // Making sure of safety exit of ToneMixerFragment by onFragmentExit() if changes are not saved
            if ((currentFragment instanceof ToneMixerFragment) && (fragmentId != R.id.tone_mixer)) {
                if (!((ToneMixerFragment) currentFragment).onFragmentExit(fragmentId))
                    return false;
            }

            if (fragmentId == R.id.tone_creator) {
                if (!(currentFragment instanceof ToneCreatorFragment)) {
                    manageVisibilityOfTopMenu(false, false);
                    currentFragment = new ToneCreatorFragment();
                    loadFragment(currentFragment);
                }
            } else if (fragmentId == R.id.tone_mixer) {
                if (!(currentFragment instanceof ToneMixerFragment)) {
                    manageVisibilityOfTopMenu(false, false);
                    currentFragment = new ToneMixerFragment();
                    loadFragment(currentFragment);
                }
            } else if (fragmentId == R.id.my_homepage) {
                if (!(currentFragment instanceof HomepageTonesFragment) && !(currentFragment instanceof HomepageMusicFragment)) {
                    manageVisibilityOfTopMenu(true, true);
                    currentFragment = new HomepageTonesFragment();
                    loadFragment(currentFragment);
                    binding.mainTabLayout.selectTab(binding.mainTabLayout.getTabAt(0));
                }
            } else if (fragmentId == R.id.tone_streaming) {
                if (!(currentFragment instanceof ToneStreamingFragment)) {
                    manageVisibilityOfTopMenu(true, false);
                    currentFragment = new ToneStreamingFragment();
                    loadFragment(currentFragment);
                }
            }
            return true;
        });
    }

    private void setBackButtonBehavior() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.mainBottomNavView.getSelectedItemId() == R.id.my_homepage)
                    finish();
                binding.mainBottomNavView.setSelectedItemId(R.id.my_homepage);
            }
        });
    }

    private void manageVisibilityOfTopMenu(boolean visible, boolean isHomepage) {
        ConstraintLayout topMenu = binding.mainTopMenu;
        int visibility = visible ? View.VISIBLE : View.GONE;

        if (topMenu.getVisibility() != visibility)
            topMenu.setVisibility(visibility);

        TabLayout tabLayout = binding.mainTabLayout;
        visibility = isHomepage ? View.VISIBLE : View.GONE;

        if (tabLayout.getVisibility() != visibility)
            tabLayout.setVisibility(visibility);
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (fragment instanceof ToneCreatorFragment && toneToEditBundle != null) {
            fragment.setArguments(toneToEditBundle);
            toneToEditBundle = null;
        }

        fragmentTransaction.replace(R.id.main_fragment_container, fragment);
        fragmentTransaction.commit();
    }
}