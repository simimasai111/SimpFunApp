package com.simpfun.app.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.simpfun.app.fragment.BackupFragment;
import com.simpfun.app.fragment.FileFragment;
import com.simpfun.app.fragment.OverviewFragment;
import com.simpfun.app.fragment.SettingsFragment;
import com.simpfun.app.fragment.TerminalFragment;

import android.os.Bundle;

public class InstancePagerAdapter extends FragmentStateAdapter {
    private final String insId;

    public InstancePagerAdapter(@NonNull FragmentActivity a, String insId) {
        super(a);
        this.insId = insId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment f;
        switch (position) {
            case 0: f = new OverviewFragment(); break;
            case 1: f = new TerminalFragment(); break;
            case 2: f = new FileFragment(); break;
            case 3: f = new BackupFragment(); break;
            default: f = new SettingsFragment(); break;
        }
        Bundle b = new Bundle();
        b.putString("ins_id", insId);
        f.setArguments(b);
        return f;
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
