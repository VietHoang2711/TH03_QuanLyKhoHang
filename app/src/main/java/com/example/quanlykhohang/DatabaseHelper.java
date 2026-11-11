package com.example.quanlykhohang;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "warehouse.db";
    private static final int DATABASE_VERSION = 2;

    // Table Users
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_USERNAME = "username";
    public static final String COL_USER_PASSWORD = "password";

    // Table Products
    public static final String TABLE_PRODUCTS = "products";
    public static final String COL_PROD_ID = "id";
    public static final String COL_PROD_NAME = "name";
    public static final String COL_PROD_PRICE = "price";
    public static final String COL_PROD_QUANTITY = "quantity";
    public static final String COL_PROD_CATEGORY = "category";
    public static final String COL_PROD_ACTIVE = "active";
    public static final String COL_PROD_DESCRIPTION = "description";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    private void createTables(SQLiteDatabase db) {
        try {
            // Create users table
            String createUsersTable = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                    COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_USERNAME + " TEXT NOT NULL UNIQUE, " +
                    COL_USER_PASSWORD + " TEXT NOT NULL" +
                    ")";
            db.execSQL(createUsersTable);

            // Create products table
            String createProductsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_PRODUCTS + " (" +
                    COL_PROD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_PROD_NAME + " TEXT NOT NULL, " +
                    COL_PROD_PRICE + " INTEGER NOT NULL, " +
                    COL_PROD_QUANTITY + " INTEGER NOT NULL, " +
                    COL_PROD_CATEGORY + " TEXT NOT NULL, " +
                    COL_PROD_ACTIVE + " INTEGER NOT NULL, " +
                    COL_PROD_DESCRIPTION + " TEXT" +
                    ")";
            db.execSQL(createProductsTable);

            // Insert initial user if not exists
            Cursor cursorUser = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);
            if (cursorUser == null || cursorUser.getCount() == 0) {
                ContentValues userValues = new ContentValues();
                userValues.put(COL_USER_USERNAME, "admin");
                userValues.put(COL_USER_PASSWORD, "123456");
                db.insert(TABLE_USERS, null, userValues);
            }
            if (cursorUser != null) cursorUser.close();

            // Insert initial products if not exists
            Cursor cursorProd = db.rawQuery("SELECT * FROM " + TABLE_PRODUCTS, null);
            if (cursorProd == null || cursorProd.getCount() == 0) {
                insertInitialProduct(db, "Chuột không dây", 150000, 20, "Điện tử", 1, "Chuột không dây Ergonomic 2.4GHz");
                insertInitialProduct(db, "Bàn phím cơ", 450000, 3, "Điện tử", 1, "Bàn phím cơ Red Switch 87 phím");
                insertInitialProduct(db, "Sổ tay A5", 25000, 40, "Văn phòng phẩm", 1, "Sổ tay bìa da A5 200 trang");
            }
            if (cursorProd != null) cursorProd.close();

        } catch (Exception e) {
            Log.e(TAG, "Error creating tables: " + e.getMessage());
        }
    }

    private void insertInitialProduct(SQLiteDatabase db, String name, int price, int quantity, String category, int active, String description) {
        ContentValues values = new ContentValues();
        values.put(COL_PROD_NAME, name);
        values.put(COL_PROD_PRICE, price);
        values.put(COL_PROD_QUANTITY, quantity);
        values.put(COL_PROD_CATEGORY, category);
        values.put(COL_PROD_ACTIVE, active);
        values.put(COL_PROD_DESCRIPTION, description);
        db.insert(TABLE_PRODUCTS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }

    // 1. Check login credentials
    public boolean checkLogin(String username, String password) {
        boolean exists = false;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            // Ensure table exists just in case
            createTables(db);

            String query = "SELECT " + COL_USER_ID + " FROM " + TABLE_USERS +
                    " WHERE " + COL_USER_USERNAME + " = ? AND " + COL_USER_PASSWORD + " = ?";
            Cursor cursor = db.rawQuery(query, new String[]{username, password});
            if (cursor != null) {
                exists = (cursor.getCount() > 0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checkLogin: " + e.getMessage());
        }
        return exists;
    }

    // 2. Insert new user
    public long insertUser(String username, String password) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_USER_USERNAME, username);
            values.put(COL_USER_PASSWORD, password);
            return db.insert(TABLE_USERS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error insertUser: " + e.getMessage());
            return -1;
        }
    }

    // 3. Insert new product
    public long insertProduct(Product p) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_PROD_NAME, p.getName());
            values.put(COL_PROD_PRICE, p.getPrice());
            values.put(COL_PROD_QUANTITY, p.getQuantity());
            values.put(COL_PROD_CATEGORY, p.getCategory());
            values.put(COL_PROD_ACTIVE, p.isActive() ? 1 : 0);
            values.put(COL_PROD_DESCRIPTION, p.getDescription());
            return db.insert(TABLE_PRODUCTS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error insertProduct: " + e.getMessage());
            return -1;
        }
    }

    // 4. Get all products
    public ArrayList<Product> getAllProducts() {
        ArrayList<Product> list = new ArrayList<>();
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_PRODUCTS + " ORDER BY " + COL_PROD_ID + " DESC";
            Cursor cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROD_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_NAME));
                    int price = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROD_PRICE));
                    int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROD_QUANTITY));
                    String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_CATEGORY));
                    int activeInt = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROD_ACTIVE));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_DESCRIPTION));

                    Product product = new Product(id, name, price, quantity, category, activeInt == 1, description);
                    list.add(product);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getAllProducts: " + e.getMessage());
        }
        return list;
    }

    // 5. Get product by ID
    public Product getProductById(int id) {
        Product product = null;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_PRODUCTS + " WHERE " + COL_PROD_ID + " = ?";
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(id)});

            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_NAME));
                int price = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROD_PRICE));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROD_QUANTITY));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_CATEGORY));
                int activeInt = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PROD_ACTIVE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COL_PROD_DESCRIPTION));

                product = new Product(id, name, price, quantity, category, activeInt == 1, description);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getProductById: " + e.getMessage());
        }
        return product;
    }

    // 6. Update product quantity
    public boolean updateQuantity(int id, int quantity) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_PROD_QUANTITY, quantity);
            int rows = db.update(TABLE_PRODUCTS, values, COL_PROD_ID + " = ?", new String[]{String.valueOf(id)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updateQuantity: " + e.getMessage());
            return false;
        }
    }

    // 7. Delete product by ID
    public boolean deleteProduct(int id) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            int rows = db.delete(TABLE_PRODUCTS, COL_PROD_ID + " = ?", new String[]{String.valueOf(id)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error deleteProduct: " + e.getMessage());
            return false;
        }
    }

    // 8. Get total inventory value: SUM(price * quantity)
    public long getTotalValue() {
        long total = 0;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT SUM(" + COL_PROD_PRICE + " * " + COL_PROD_QUANTITY + ") FROM " + TABLE_PRODUCTS;
            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getLong(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getTotalValue: " + e.getMessage());
        }
        return total;
    }

    // 9. Count low stock products: COUNT where quantity < 5
    public int countLowStock() {
        int count = 0;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT COUNT(*) FROM " + TABLE_PRODUCTS + " WHERE " + COL_PROD_QUANTITY + " < 5";
            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error countLowStock: " + e.getMessage());
        }
        return count;
    }

    // 10. Count total products
    public int getTotalProductsCount() {
        int count = 0;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT COUNT(*) FROM " + TABLE_PRODUCTS;
            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getTotalProductsCount: " + e.getMessage());
        }
        return count;
    }
}
