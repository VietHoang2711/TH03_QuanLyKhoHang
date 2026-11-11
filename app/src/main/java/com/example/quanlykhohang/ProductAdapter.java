package com.example.quanlykhohang;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    private final Context context;
    private ArrayList<Product> productList;
    private final OnItemClickListener listener;

    public ProductAdapter(Context context, ArrayList<Product> productList, OnItemClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    public void updateData(ArrayList<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvProductName.setText(product.getName());
        holder.tvCategory.setText(product.getCategory());
        holder.tvProductPrice.setText(CurrencyUtils.formatVnd(product.getPrice()));

        String quantityStr = "Tồn kho: " + product.getQuantity();
        holder.tvProductQuantity.setText(quantityStr);

        if (product.isLowStock()) {
            holder.tvLowStockBadge.setVisibility(View.VISIBLE);
            holder.tvProductQuantity.setTextColor(ContextCompat.getColor(context, R.color.low_stock_red));
        } else {
            holder.tvLowStockBadge.setVisibility(View.GONE);
            holder.tvProductQuantity.setTextColor(ContextCompat.getColor(context, R.color.text_secondary));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory;
        TextView tvLowStockBadge;
        TextView tvProductName;
        TextView tvProductPrice;
        TextView tvProductQuantity;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvLowStockBadge = itemView.findViewById(R.id.tvLowStockBadge);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
        }
    }
}
