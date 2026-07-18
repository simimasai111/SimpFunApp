package com.simpfun.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simpfun.app.R;
import com.simpfun.app.model.BackupItem;

import java.util.List;

public class BackupAdapter extends RecyclerView.Adapter<BackupAdapter.VH> {
    public interface OnItemClick {
        void onItem(BackupItem b);
    }

    private final List<BackupItem> data;
    private final OnItemClick cb;

    public BackupAdapter(List<BackupItem> d, OnItemClick c) {
        data = d;
        cb = c;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_backup, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        BackupItem b = data.get(pos);
        h.tag.setText(b.tag.isEmpty() ? ("备份 #" + b.id) : b.tag);
        h.size.setText(b.sizeText());
        h.time.setText(b.validTime.isEmpty() ? "" : ("有效至 " + b.validTime));
        h.itemView.setOnClickListener(v -> cb.onItem(b));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tag, size, time;

        VH(View v) {
            super(v);
            tag = v.findViewById(R.id.tv_backup_tag);
            size = v.findViewById(R.id.tv_backup_size);
            time = v.findViewById(R.id.tv_backup_time);
        }
    }
}
