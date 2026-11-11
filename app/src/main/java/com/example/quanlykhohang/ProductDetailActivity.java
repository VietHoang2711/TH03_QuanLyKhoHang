package com.example.quanlykhohang;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageButton btnHeaderBack;
    private TextView tvCategory;
    private TextView tvActiveStatus;
    private TextView tvProductName;
    private LinearLayout layoutLowStockBanner;

    private TextView tvPrice;
    private TextView tvQuantity;
    private TextView tvItemTotalValue;
    private TextView tvDescription;

    private MaterialButton btnStockIn;
    private MaterialButton btnStockOut;
    private MaterialButton btnDelete;
    private MaterialButton btnBack;

    private DatabaseHelper databaseHelper;
    private int productId = -1;
    private Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        databaseHelper = new DatabaseHelper(this);

        productId = getIntent().getIntExtra("PRODUCT_ID", -1);
        if (productId == -1) {
            Toast.makeText(this, "Không tìm thấy thông tin sản phẩm!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadProductDetail();
    }

    private void initViews() {
        btnHeaderBack = findViewById(R.id.btnHeaderBack);
        tvCategory = findViewById(R.id.tvCategory);
        tvActiveStatus = findViewById(R.id.tvActiveStatus);
        tvProductName = findViewById(R.id.tvProductName);
        layoutLowStockBanner = findViewById(R.id.layoutLowStockBanner);

        tvPrice = findViewById(R.id.tvPrice);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvItemTotalValue = findViewById(R.id.tvItemTotalValue);
        tvDescription = findViewById(R.id.tvDescription);

        btnStockIn = findViewById(R.id.btnStockIn);
        btnStockOut = findViewById(R.id.btnStockOut);
        btnDelete = findViewById(R.id.btnDelete);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        btnHeaderBack.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());

        btnStockIn.setOnClickListener(v -> handleStockIn());
        btnStockOut.setOnClickListener(v -> handleStockOut());
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void loadProductDetail() {
        currentProduct = databaseHelper.getProductById(productId);
        if (currentProduct == null) {
            Toast.makeText(this, "Sản phẩm không tồn tại!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvCategory.setText(currentProduct.getCategory());
        tvProductName.setText(currentProduct.getName());
        tvPrice.setText(CurrencyUtils.formatVnd(currentProduct.getPrice()));

        String qtyStr = currentProduct.getQuantity() + " sản phẩm";
        tvQuantity.setText(qtyStr);

        long totalVal = currentProduct.getTotalValue();
        tvItemTotalValue.setText(CurrencyUtils.formatVnd(totalVal));

        if (currentProduct.isActive()) {
            tvActiveStatus.setText("Đang kinh doanh");
            tvActiveStatus.setBackgroundResource(R.drawable.bg_badge_active);
            tvActiveStatus.setTextColor(getColor(R.color.active_green));
        } else {
            tvActiveStatus.setText("Ngừng kinh doanh");
            tvActiveStatus.setBackgroundResource(R.drawable.bg_badge_inactive);
            tvActiveStatus.setTextColor(getColor(R.color.inactive_gray));
        }

        if (currentProduct.isLowStock()) {
            layoutLowStockBanner.setVisibility(View.VISIBLE);
        } else {
            layoutLowStockBanner.setVisibility(View.GONE);
        }

        if (TextUtils.isEmpty(currentProduct.getDescription())) {
            tvDescription.setText("Không có mô tả.");
        } else {
            tvDescription.setText(currentProduct.getDescription());
        }
    }

    private void handleStockIn() {
        if (currentProduct == null) return;
        int newQuantity = currentProduct.getQuantity() + 1;
        boolean success = databaseHelper.updateQuantity(productId, newQuantity);
        if (success) {
            Toast.makeText(this, "Đã nhập thêm 1 sản phẩm", Toast.LENGTH_SHORT).show();
            loadProductDetail();
        } else {
            Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleStockOut() {
        if (currentProduct == null) return;

        if (currentProduct.getQuantity() <= 0) {
            Toast.makeText(this, "Hết hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        int newQuantity = currentProduct.getQuantity() - 1;
        boolean success = databaseHelper.updateQuantity(productId, newQuantity);
        if (success) {
            Toast.makeText(this, "Đã bán 1 sản phẩm", Toast.LENGTH_SHORT).show();
            loadProductDetail();
        } else {
            Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm \"" + (currentProduct != null ? currentProduct.getName() : "") + "\" khỏi kho hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteProduct())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteProduct() {
        boolean success = databaseHelper.deleteProduct(productId);
        if (success) {
            Toast.makeText(this, "Đã xóa sản phẩm thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Xóa thất bại!", Toast.LENGTH_SHORT).show();
        }
    }
}
