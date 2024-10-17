package com.learning.androidlearning.sample.profile;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.learning.androidlearning.R;

import java.util.Arrays;
import java.util.List;

public class SampleProfileActivityJavaVersion extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_profile);

        RecyclerView photosRecyclerView = findViewById(R.id.photoGrid);
        List<Integer> photos = Arrays.asList(
                R.drawable.sample_photo_1,
                R.drawable.sample_photo_2,
                R.drawable.sample_photo_3
        );

        PhotoAdapterJavaVersion adapter = new PhotoAdapterJavaVersion(photos);
        photosRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        photosRecyclerView.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return item.getItemId() == R.id.nav_home
                        || item.getItemId() == R.id.nav_search
                        || item.getItemId() == R.id.nav_chat
                        || item.getItemId() == R.id.nav_add
                        || item.getItemId() == R.id.nav_profile;
            }
        });
    }
}
