package com.simpfun.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.simpfun.app.R;
import com.simpfun.app.model.ShopItem;

import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.VH> {
    public interface OnItemClick {
        void onItem(ShopItem i);
    }

    private final List<ShopItem> data;
    private final OnItemClick cb;

    public ShopAdapter(List<ShopItem> d, OnItemClick c) {
        data = d;
        cb = c;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_shop, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ShopItem s = data.get(pos);
        h.title.setText(s.areaText());
        h.sub.setText(s.cpu + "核 / " + s.ram + "G / " + s.disk + "G"
                + (s.traffic > 0 ? (" / 流量 " + s.traffic + "G") : "")
                + (!s.spec.isEmpty() ? (" / " + s.spec) : ""));
        h.price.setText(s.priceText());
        h.itemView.setAlpha(s.creatable ? 1f : 0.5f);
        h.itemView.setOnClickListener(v -> cb.onItem(s));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, sub, price;

        VH(View v) {
            super(v);
            title = v.findViewById(R.id.tv_shop_title);
            sub = v.findViewById(R.id.tv_shop_sub);
            price = v.findViewById(R.id.tv_shop_price);
        }
    }
}
