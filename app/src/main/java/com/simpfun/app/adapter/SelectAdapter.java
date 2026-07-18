package com.simpfun.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simpfun.app.R;
import com.simpfun.app.model.SelectItem;

import java.util.List;

public class SelectAdapter extends RecyclerView.Adapter<SelectAdapter.VH> {
    public interface OnPick {
        void onPick(SelectItem i);
    }

    private final List<SelectItem> data;
    private final OnPick cb;

    public SelectAdapter(List<SelectItem> d, OnPick c) {
        data = d;
        cb = c;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_select, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        SelectItem i = data.get(pos);
        h.title.setText(i.title);
        h.sub.setText(i.subtitle);
        h.sub.setVisibility(i.subtitle.isEmpty() ? View.GONE : View.VISIBLE);
        h.itemView.setOnClickListener(v -> cb.onPick(i));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, sub;

        VH(View v) {
            super(v);
            title = v.findViewById(R.id.tv_sel_title);
            sub = v.findViewById(R.id.tv_sel_sub);
        }
    }
}
