package com.simpfun.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simpfun.app.R;
import com.simpfun.app.model.SelectItem;

import java.util.ArrayList;
import java.util.List;

/** 单选列表适配器：游戏 / 规格等步骤共用，选中项显示勾选并高亮 */
public class SelectAdapter extends RecyclerView.Adapter<SelectAdapter.VH> {

    public interface OnPick {
        void onPick(SelectItem item);
    }

    private final List<SelectItem> items = new ArrayList<>();
    private String selectedId = "";
    private final OnPick callback;

    public SelectAdapter(OnPick callback) {
        this.callback = callback;
    }

    /** 无回调构造：仅用于单选展示，选中项通过 getSelectedItem() 读取 */
    public SelectAdapter() {
        this(null);
    }

    public void setData(List<SelectItem> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    public void setSelectedId(String id) {
        this.selectedId = id == null ? "" : id;
        notifyDataSetChanged();
    }

    public String getSelectedId() {
        return selectedId;
    }

    /** 返回当前选中项（按 selectedId 匹配），未选中返回 null */
    public SelectItem getSelectedItem() {
        for (SelectItem it : items) {
            if (it.id.equals(selectedId)) return it;
        }
        return null;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_select, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SelectItem it = items.get(position);
        boolean selected = it.id.equals(selectedId);
        h.title.setText(it.title);
        if (it.sub == null || it.sub.isEmpty()) {
            h.sub.setVisibility(View.GONE);
        } else {
            h.sub.setVisibility(View.VISIBLE);
            h.sub.setText(it.sub);
        }
        h.check.setVisibility(selected ? View.VISIBLE : View.GONE);
        h.root.setBackgroundColor(selected
                ? 0xFFE3F2FD   // 选中浅蓝
                : 0xFFFFFFFF);
        h.root.setOnClickListener(v -> {
            selectedId = it.id;
            notifyDataSetChanged();
            if (callback != null) callback.onPick(it);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        View root;
        TextView title;
        TextView sub;
        ImageView check;

        VH(View v) {
            super(v);
            root = v;
            title = v.findViewById(R.id.tv_title);
            sub = v.findViewById(R.id.tv_sub);
            check = v.findViewById(R.id.iv_check);
        }
    }
}
