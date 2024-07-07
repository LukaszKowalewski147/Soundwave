package com.example.soundwave;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.soundwave.databinding.ActivityMainBinding;
import com.example.soundwave.utils.Options;
import com.example.soundwave.utils.WavCreator;
import com.example.soundwave.view.HomepageFragment;
import com.example.soundwave.view.ToneCreatorFragment;
import com.example.soundwave.view.ToneMixerFragment;
import com.example.soundwave.view.ToneStreamingFragment;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Fragment currentFragment;
    private Bundle toneToEditBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setDisplayDensity();
        setFilepathToDownload();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentFragment = new HomepageFragment();
        loadFragment(currentFragment);

        binding.mainBottomNavView.setSelectedItemId(R.id.my_homepage);

        binding.mainBottomNavView.setOnItemSelectedListener(item -> {
            int fragmentId = item.getItemId();

            // Making sure of safety exit of ToneCreatorFragment by onFragmentExit() if changes are not saved
            if ((currentFragment instanceof ToneCreatorFragment) && (fragmentId != R.id.tone_creator)) {
                if (!((ToneCreatorFragment) currentFragment).onFragmentExit(fragmentId))
                    return false;
            }

            if (fragmentId == R.id.tone_creator) {
                if (!(currentFragment instanceof ToneCreatorFragment)) {
                    manageVisibilityOfTopMenu(false);
                    currentFragment = new ToneCreatorFragment();
                    loadFragment(currentFragment);
                }
            } else if (fragmentId == R.id.tone_mixer) {
                if (!(currentFragment instanceof ToneMixerFragment)) {
                    manageVisibilityOfTopMenu(false);
                    currentFragment = new ToneMixerFragment();
                    loadFragment(currentFragment);
                }
            } else if (fragmentId == R.id.my_homepage) {
                if (!(currentFragment instanceof HomepageFragment)) {
                    manageVisibilityOfTopMenu(true);
                    currentFragment = new HomepageFragment();
                    loadFragment(currentFragment);
                }
            } else if (fragmentId == R.id.tone_streaming) {
                if (!(currentFragment instanceof ToneStreamingFragment)) {
                    manageVisibilityOfTopMenu(false);
                    currentFragment = new ToneStreamingFragment();
                    loadFragment(currentFragment);
                }
            }
            return true;
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

    public void openToneCreatorInEditionMode(Tone tone) {
        toneToEditBundle = new Bundle();
        toneToEditBundle.putSerializable("tone", tone);

        binding.mainBottomNavView.setSelectedItemId(R.id.tone_creator);
    }

    public void changeFragmentFromToneCreator(int fragmentId) {
        currentFragment = null;
        binding.mainBottomNavView.setSelectedItemId(fragmentId);
    }

    private void setFilepathToDownload() {
        File externalFilesDir = getExternalFilesDir(WavCreator.getFileFolder());
        if (externalFilesDir != null) {
            Options.filepathToDownload = externalFilesDir.toString();
        } else {
            Toast.makeText(this, R.string.error_msg_filepath_to_download_fail, Toast.LENGTH_SHORT).show();
        }
    }

    private void setDisplayDensity() {
        Options.displayDensity = getResources().getDisplayMetrics().density;
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

        if (fragment instanceof ToneCreatorFragment && toneToEditBundle != null) {
            fragment.setArguments(toneToEditBundle);
            toneToEditBundle = null;
        }

        fragmentTransaction.replace(R.id.main_fragment_container, fragment);
        fragmentTransaction.commit();
    }
}