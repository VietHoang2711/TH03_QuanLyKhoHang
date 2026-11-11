package com.example.quanlykhohang;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddProductActivity extends AppCompatActivity {

    private TextInputEditText etName;
    private TextInputEditText etPrice;
    private TextInputEditText etQuantity;
    private Spinner spCategory;
    private MaterialSwitch switchActive;
    private TextInputEditText etDescription;

    private TextInputLayout tilName;
    private TextInputLayout tilPrice;
    private TextInputLayout tilQuantity;

    private DatabaseHelper databaseHelper;

    private static final String[] CATEGORIES = {
            "Điện tử",
            "Thực phẩm",
            "Văn phòng phẩm",
            "Gia dụng",
            "Khác"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        databaseHelper = new DatabaseHelper(this);

        ImageButton btnBack = findViewById(R.id.btnBack);
        etName = findViewById(R.id.etName);
        etPrice = findViewById(R.id.etPrice);
        etQuantity = findViewById(R.id.etQuantity);
        spCategory = findViewById(R.id.spCategory);
        switchActive = findViewById(R.id.switchActive);
        etDescription = findViewById(R.id.etDescription);

        tilName = findViewById(R.id.tilName);
        tilPrice = findViewById(R.id.tilPrice);
        tilQuantity = findViewById(R.id.tilQuantity);

        MaterialButton btnSave = findViewById(R.id.btnSave);

        // Setup Category Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, CATEGORIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveProduct());
    }

    private void saveProduct() {
        // Clear errors
        tilName.setError(null);
        tilPrice.setError(null);
        tilQuantity.setError(null);

        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
        String quantityStr = etQuantity.getText() != null ? etQuantity.getText().toString().trim() : "";
        String category = spCategory.getSelectedItem() != null ? spCategory.getSelectedItem().toString() : CATEGORIES[0];
        boolean active = switchActive.isChecked();
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";

        // Validations
        if (TextUtils.isEmpty(name)) {
            tilName.setError("Tên sản phẩm không được để trống");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(priceStr)) {
            tilPrice.setError("Giá bán không được để trống");
            etPrice.requestFocus();
            return;
        }

        int price;
        try {
            price = Integer.parseInt(priceStr);
            if (price <= 0) {
                tilPrice.setError("Giá bán phải lớn hơn 0");
                etPrice.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            tilPrice.setError("Giá bán không hợp lệ");
            etPrice.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(quantityStr)) {
            tilQuantity.setError("Số lượng không được để trống");
            etQuantity.requestFocus();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity < 0) {
                tilQuantity.setError("Số lượng không được là số âm");
                etQuantity.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            tilQuantity.setError("Số lượng không hợp lệ");
            etQuantity.requestFocus();
            return;
        }

        // Save to Database
        Product product = new Product(name, price, quantity, category, active, description);
        long id = databaseHelper.insertProduct(product);

        if (id > 0) {
            Toast.makeText(this, "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi thêm sản phẩm!", Toast.LENGTH_SHORT).show();
        }
    }
}
