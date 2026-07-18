package com.simpfun.app;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.simpfun.app.fragment.TerminalFragment;
import com.simpfun.app.fragment.FileFragment;
import com.simpfun.app.fragment.BackupFragment;
import com.simpfun.app.fragment.SettingsFragment;

/**
 * 实例详情：底部 Tab 切 终端 / 文件 / 备份 / 设置
 */
public class InstanceDetailActivity extends AppCompatActivity {
    public static final String EXTRA_INS_ID = "ins_id";
    public static final String EXTRA_INS_NAME = "ins_name";

    private String insId = "";
    private String insName = "";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_instance_detail);

        insId = getIntent().getStringExtra(EXTRA_INS_ID);
        insName = getIntent().getStringExtra(EXTRA_INS_NAME);
        if (insId == null) insId = "";
        if (insName == null) insName = "";

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        TextView tv = findViewById(R.id.tv_detail_title);
        tv.setText(insName.isEmpty() ? "实例详情" : insName);

        ViewPager2 pager = findViewById(R.id.pager);
        TabLayout tabs = findViewById(R.id.tabs);
        pager.setAdapter(new DetailPagerAdapter(this));
        new TabLayoutMediator(tabs, pager, (tab, pos) -> {
            String[] titles = {"终端", "文件", "备份", "设置"};
            tab.setText(titles[pos]);
        }).attach();
    }

    public String getInsId() {
        return insId;
    }

    static class DetailPagerAdapter extends FragmentStateAdapter {
        DetailPagerAdapter(AppCompatActivity a) {
            super(a);
        }

        @Override
        public int getItemCount() {
            return 4;
        }

        @Override
        public Fragment createFragment(int pos) {
            switch (pos) {
                case 0:
                    return new TerminalFragment();
                case 1:
                    return new FileFragment();
                case 2:
                    return new BackupFragment();
                case 3:
                    return new SettingsFragment();
                default:
                    return new TerminalFragment();
            }
        }
    }
}
