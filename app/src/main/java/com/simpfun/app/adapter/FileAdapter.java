package com.simpfun.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simpfun.app.R;
import com.simpfun.app.model.FileItem;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.VH> {
    public interface OnItemClick {
        void onItem(FileItem f);
    }

    public interface OnItemLongClick {
        void onItem(FileItem f);
    }

    private final List<FileItem> data;
    private final OnItemClick cb;
    private final OnItemLongClick longCb;

    public FileAdapter(List<FileItem> d, OnItemClick c, OnItemLongClick lc) {
        data = d;
        cb = c;
        longCb = lc;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_file, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        FileItem f = data.get(pos);
        h.icon.setText(f.isDir() ? "📁" : "📄");
        h.name.setText(f.name);
        h.meta.setText(f.isDir() ? "目录" : f.sizeText() + (f.modifiedAt.isEmpty() ? "" : "  ·  " + f.modifiedAt));
        h.itemView.setOnClickListener(v -> cb.onItem(f));
        h.itemView.setOnLongClickListener(v -> {
            longCb.onItem(f);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView icon, name, meta;

        VH(View v) {
            super(v);
            icon = v.findViewById(R.id.tv_icon);
            name = v.findViewById(R.id.tv_name);
            meta = v.findViewById(R.id.tv_meta);
        }
    }
}
