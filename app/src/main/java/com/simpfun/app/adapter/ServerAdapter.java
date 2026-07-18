package com.simpfun.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simpfun.app.R;
import com.simpfun.app.model.Instance;

import java.util.List;

public class ServerAdapter extends RecyclerView.Adapter<ServerAdapter.VH> {
    public interface OnItemClick {
        void onItem(Instance i);
    }

    private final List<Instance> data;
    private final OnItemClick cb;

    public ServerAdapter(List<Instance> d, OnItemClick c) {
        data = d;
        cb = c;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_server, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Instance i = data.get(pos);
        h.name.setText(i.displayName());
        h.config.setText(i.configSummary());
        h.id.setText("ID: " + i.id);
        h.state.setText(i.stateText());
        h.state.setTextColor(i.stateColor());
        h.itemView.setOnClickListener(v -> cb.onItem(i));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, config, id, state;

        VH(View v) {
            super(v);
            name = v.findViewById(R.id.tv_name);
            config = v.findViewById(R.id.tv_config);
            id = v.findViewById(R.id.tv_id);
            state = v.findViewById(R.id.tv_state);
        }
    }
}
