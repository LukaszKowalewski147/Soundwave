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
import android.widget.Toast;

import com.example.soundwave.databinding.ActivityMainBinding;
import com.example.soundwave.model.entity.Tone;
import com.example.soundwave.view.HomepageFragment;
import com.example.soundwave.view.ToneCreatorFragment;
import com.example.soundwave.view.ToneMixerFragment;
import com.example.soundwave.view.ToneStreamingFragment;
import com.example.soundwave.viewmodel.MainActivityViewModel;
import com.google.android.material.navigation.NavigationBarView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainActivityViewModel mainActivityViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        mainActivityViewModel.getAllTones().observe(this, new Observer<List<Tone>>() {
            @Override
            public void onChanged(List<Tone> tones) {
                Toast.makeText(MainActivity.this, "Num of tones: " + tones.size(), Toast.LENGTH_SHORT).show();
            }
        });

        loadFragment(new HomepageFragment());
        binding.mainBottomNavView.setSelectedItemId(R.id.my_homepage);

        binding.mainBottomNavView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.tone_creator:
                        loadFragment(new ToneCreatorFragment());
                        break;
                    case R.id.tone_mixer:
                        loadFragment(new ToneMixerFragment());
                        break;
                    case R.id.my_homepage:
                        loadFragment(new HomepageFragment());
                        break;
                    case R.id.tone_streaming:
                        loadFragment(new ToneStreamingFragment());
                        break;
                }
                return true;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.main_fragment_container, fragment);
        fragmentTransaction.commit();
    }
}