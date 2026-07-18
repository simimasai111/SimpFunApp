package com.simpfun.app;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.simpfun.app.adapter.MainPagerAdapter;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 vp;
    private BottomNavigationView nav;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        vp = findViewById(R.id.vp_main);
        nav = findViewById(R.id.bottom_nav);

        vp.setAdapter(new MainPagerAdapter(this));
        vp.setUserInputEnabled(false);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_servers) vp.setCurrentItem(0);
            else if (id == R.id.nav_shop) vp.setCurrentItem(1);
            else if (id == R.id.nav_me) vp.setCurrentItem(2);
            return true;
        });

        vp.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                int id = position == 0 ? R.id.nav_servers : position == 1 ? R.id.nav_shop : R.id.nav_me;
                nav.setSelectedItemId(id);
            }
        });
    }
}
