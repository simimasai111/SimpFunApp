package com.simpfun.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.simpfun.app.adapter.InstancePagerAdapter;

public class InstanceDetailActivity extends AppCompatActivity {
    private String insId;
    private String insName;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_instance_detail);
        insId = getIntent().getStringExtra("ins_id");
        insName = getIntent().getStringExtra("ins_name");

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setTitle(insName == null ? "实例详情" : insName);
        setSupportActionBar(tb);
        tb.setNavigationOnClickListener(v -> finish());

        ViewPager2 vp = findViewById(R.id.vp_instance);
        TabLayout tabs = findViewById(R.id.tabs_instance);
        vp.setAdapter(new InstancePagerAdapter(this, insId == null ? "" : insId));

        String[] titles = {"概览", "终端", "文件", "备份", "设置"};
        new TabLayoutMediator(tabs, vp, (tab, position) -> tab.setText(titles[position])).attach();
    }
}
