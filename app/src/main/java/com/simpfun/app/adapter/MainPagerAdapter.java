package com.simpfun.app.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.simpfun.app.fragment.ProfileFragment;
import com.simpfun.app.fragment.ServerListFragment;
import com.simpfun.app.fragment.ShopFragment;

public class MainPagerAdapter extends FragmentStateAdapter {
    public MainPagerAdapter(@NonNull FragmentActivity a) {
        super(a);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) return new ServerListFragment();
        if (position == 1) return new ShopFragment();
        return new ProfileFragment();
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
