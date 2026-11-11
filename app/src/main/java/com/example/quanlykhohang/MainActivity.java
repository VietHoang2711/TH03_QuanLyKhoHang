package com.example.quanlykhohang;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView tvTotalProducts;
    private TextView tvTotalValue;
    private TextView tvLowStockCount;
    private TextView tvEmptyState;
    private RecyclerView rvProducts;
    private ExtendedFloatingActionButton btnAddProduct;
    private ImageButton btnLogout;

    private DatabaseHelper databaseHelper;
    private ProductAdapter productAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvTotalValue = findViewById(R.id.tvTotalValue);
        tvLowStockCount = findViewById(R.id.tvLowStockCount);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        rvProducts = findViewById(R.id.rvProducts);
        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnLogout = findViewById(R.id.btnLogout);

        rvProducts.setLayoutManager(new LinearLayoutManager(this));

        btnAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddProductActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        // Load statistics
        int totalProducts = databaseHelper.getTotalProductsCount();
        long totalValue = databaseHelper.getTotalValue();
        int lowStockCount = databaseHelper.countLowStock();

        tvTotalProducts.setText(totalProducts + " sản phẩm");
        tvTotalValue.setText(CurrencyUtils.formatVnd(totalValue));
        tvLowStockCount.setText(lowStockCount + " sản phẩm sắp hết hàng (< 5)");

        // Load product list
        ArrayList<Product> productList = databaseHelper.getAllProducts();

        if (productList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
        }

        if (productAdapter == null) {
            productAdapter = new ProductAdapter(this, productList, product -> {
                Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
                intent.putExtra("PRODUCT_ID", product.getId());
                startActivity(intent);
            });
            rvProducts.setAdapter(productAdapter);
        } else {
            productAdapter.updateData(productList);
        }
    }
}
