package com.cs360.inventory;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import android.util.Base64;
import android.util.Log;

public class InventoryDatabase extends SQLiteOpenHelper {
    private static final String INVENTORY_DATABASE = "inventoryApp.db";
    private static final int VERSION = 1;
    private static InventoryDatabase sInventoryDatabase;
    private List<ListData> mItems;

    private InventoryDatabase(Context context) {
        super(context, INVENTORY_DATABASE, null, VERSION);
    }

    // Singleton
    public static InventoryDatabase getInstance(Context context) {
        if (sInventoryDatabase == null) {
            sInventoryDatabase = new InventoryDatabase(context);
        }
        return sInventoryDatabase;
    }

    // Columns for Inventory Table
    private static final class InventoryTable {
            private static final String TABLE = "inventory";
            private static final String COL_ID = "_id";
            private static final String COL_NAME = "name";
            private static final String COL_QUANTITY = "qty";
    }

    // Columns for User Table
    private static final class UserTable {
        private static final String TABLE = "users";
        private static final String COL_ID = "_id";
        private static final String COL_USERNAME = "username";
        private static final String COL_PASSWORD_HASH = "password_hash";
        private static final String COL_SALT = "salt";
        private static final String COL_PHONE = "phone";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create User Table
        db.execSQL("CREATE table " + UserTable.TABLE + " (" +
                UserTable.COL_ID + " integer primary key autoincrement, " +
                UserTable.COL_USERNAME + " text, " +
                UserTable.COL_PASSWORD_HASH + " text, "+
                UserTable.COL_SALT + " text," +
                UserTable.COL_PHONE + " text)");

        // Create Inventory Table
        db.execSQL("CREATE table " + InventoryTable.TABLE + " (" +
                InventoryTable.COL_ID + " integer primary key autoincrement, " +
                InventoryTable.COL_NAME + " text, " +
                InventoryTable.COL_QUANTITY + " integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + UserTable.TABLE);
        db.execSQL("drop table if exists " + InventoryTable.TABLE);
        onCreate(db);
    }

//
//    INVENTORY database
//
    public boolean addItem(String name, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(InventoryTable.COL_NAME, name);
        values.put(InventoryTable.COL_QUANTITY, quantity);

        // Ensures item is correctly inserted, otherwise value is -1
        long itemId = db.insert(InventoryTable.TABLE, null, values);

        return itemId != -1;
    }

    public boolean updateItem(ListData item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(InventoryTable.COL_NAME, item.getName());
        values.put(InventoryTable.COL_QUANTITY, item.getQty());

        // Ensures item is correctly inserted, otherwise value is -1
        int itemUpdated = db.update(InventoryTable.TABLE, values, InventoryTable.COL_ID + " = ? ", new String[] { String.valueOf(item.getmId())});

        return itemUpdated > 0;
    }

    public boolean deleteItem(ListData item) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Ensures item is correctly inserted, otherwise value is -1
        int itemDeleted = db.delete(InventoryTable.TABLE, InventoryTable.COL_ID + " = ? ", new String[] { String.valueOf(item.getmId()) });

        return itemDeleted > 0;
    }

    // Retrieves all items from database
    public List<ListData> getItems() {
        List<ListData> items = new ArrayList<ListData>();
        SQLiteDatabase db = this.getReadableDatabase();

        String sql = "SELECT * FROM " + InventoryTable.TABLE;
        Cursor cursor = db.rawQuery(sql, new String[] {});

        if (cursor.moveToFirst()) {
            // Obtain and add item to List
            do {
                long id = cursor.getLong(0);
                String name = cursor.getString(1);
                int qty = cursor.getInt(2);
                items.add(new ListData(id, name, qty));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return items;
    }

//
//    USER Database
//
    // Helper function to ensure no duplicate usernames are added
    public boolean verifyUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + UserTable.TABLE + " WHERE username = ?";

        Cursor cursor = db.rawQuery(sql, new String[] { username });
        boolean userFound = cursor.getCount() > 0;
        cursor.close();

        return userFound;
    }

    public boolean addUser(String username, String password) {
        // Ensure no duplicate usernames added
        if (verifyUsername(username)) {
            return false;
        }

        // Generate new salt and hash values
        byte[] salt = PasswordUtil.generateSalt();
        byte[] hash = PasswordUtil.hashPassword(password.toCharArray(), salt);

        // Convert to string for storage
        String saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP);
        String hashBase64 = Base64.encodeToString(hash, Base64.NO_WRAP);

        SQLiteDatabase db = this.getWritableDatabase();

        // Create row
        ContentValues values = new ContentValues();
        values.put(UserTable.COL_USERNAME, username);
        values.put(UserTable.COL_PASSWORD_HASH, hashBase64);
        values.put(UserTable.COL_SALT, saltBase64);

        // Insert row, if unsuccessful value is -1
        long userId = db.insert(UserTable.TABLE, null, values);

        return userId != -1;
    }

    public boolean verifyLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + UserTable.TABLE + " WHERE " + UserTable.COL_USERNAME + " = ? ";
        Cursor cursor = db.rawQuery(sql, new String[] { username });

        if (cursor.moveToFirst()) {
            // Collect column indices from table
            int hashIndex = cursor.getColumnIndex(UserTable.COL_PASSWORD_HASH);
            int saltIndex = cursor.getColumnIndex(UserTable.COL_SALT);

            // If columns are not found
            if (hashIndex == -1 || saltIndex == -1) {
                Log.d("verifyLogin", "Hash/Salt Column not found in User Table");
                return false;
            }

            // Obtain value
            String storedHashBase64 = cursor.getString(hashIndex);
            String storedSaltBase64 = cursor.getString(saltIndex);
            cursor.close();

            // Convert value for verification
            byte[] storedHash = Base64.decode(storedHashBase64, Base64.NO_WRAP);
            byte[] storedSalt = Base64.decode(storedSaltBase64, Base64.NO_WRAP);

            return PasswordUtil.verifyPassword(password.toCharArray(), storedSalt, storedHash);
        } else {
            cursor.close();
            return false;
        }
    }

    // Update an individual user's phoneNumber
    public boolean updateUserPhoneNumber(String username, String phoneNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserTable.COL_PHONE,phoneNumber);

        // Insert value, if unsuccessful value is -1
        int rows = db.update(UserTable.TABLE, values, UserTable.COL_USERNAME + " = ? ", new String[]{ username });

        return rows > 0;
    }

    // Retrieve an individual user's phoneNumber
    public String getUserPhoneNumber(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT " + UserTable.COL_PHONE + " FROM " + UserTable.TABLE + " WHERE " + UserTable.COL_USERNAME + " = ? ";
        Cursor cursor = db.rawQuery(sql, new String[]{ username });

        // If user found
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(UserTable.COL_PHONE);
            String phoneNumber = cursor.getString(index);
            cursor.close();
            return phoneNumber;
        }

        Log.d("InventoryDB", "Error retrieving user phone number");
        cursor.close();
        return null;
    }
}
