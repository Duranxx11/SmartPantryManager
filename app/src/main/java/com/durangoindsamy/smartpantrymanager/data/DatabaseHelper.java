package com.durangoindsamy.smartpantrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.durangoindsamy.smartpantrymanager.model.PantryItem;
import com.durangoindsamy.smartpantrymanager.util.IngredientNormalizer;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates and manages the local SQLite database.
 * Android calls onCreate() the first time the database is opened on a device.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 1;

    // ---- Shared column names ----
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_NORMALIZED = "normalized_name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit";

    // ---- pantry_items ----
    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_EXPIRY = "expiry_date";

    // ---- recipes ----
    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_STEPS = "steps";

    // ---- recipe_ingredients (one row per ingredient per recipe) ----
    public static final String TABLE_RECIPE_INGREDIENTS = "recipe_ingredients";
    public static final String COL_RECIPE_ID = "recipe_id";

    // Single shared instance so every screen uses the same connection
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            // Application context avoids leaking an Activity
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // SQLite ignores foreign keys unless switched on
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_NORMALIZED + " TEXT NOT NULL, "
                + COL_QUANTITY + " REAL NOT NULL, "
                + COL_UNIT + " TEXT NOT NULL, "
                + COL_EXPIRY + " INTEGER)");          // nullable

        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_STEPS + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + TABLE_RECIPE_INGREDIENTS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RECIPE_ID + " INTEGER NOT NULL, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_NORMALIZED + " TEXT NOT NULL, "
                + COL_QUANTITY + " REAL NOT NULL, "
                + COL_UNIT + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + COL_RECIPE_ID + ") REFERENCES "
                + TABLE_RECIPES + "(" + COL_ID + ") ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple strategy for an assignment: rebuild from scratch.
        // Child table dropped first because it references recipes.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ===================== PANTRY CRUD =====================

    /** CREATE - returns the new row id, or -1 on failure. */
    public long insertPantryItem(PantryItem item) {
        long id = getWritableDatabase().insert(TABLE_PANTRY, null, toContentValues(item));
        item.setId(id);
        return id;
    }

    /** READ (all) - sorted alphabetically for the list screen. */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query(TABLE_PANTRY, null, null, null,
                null, null, COL_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                items.add(pantryItemFromCursor(c));
            }
        } // try-with-resources closes the cursor automatically
        return items;
    }

    /** READ (one) - used by the Edit screen. Returns null if not found. */
    public PantryItem getPantryItem(long id) {
        try (Cursor c = getReadableDatabase().query(TABLE_PANTRY, null,
                COL_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            return c.moveToFirst() ? pantryItemFromCursor(c) : null;
        }
    }

    /** UPDATE - returns number of rows changed (1 on success). */
    public int updatePantryItem(PantryItem item) {
        return getWritableDatabase().update(TABLE_PANTRY, toContentValues(item),
                COL_ID + " = ?", new String[]{String.valueOf(item.getId())});
    }

    /** DELETE - returns number of rows removed (1 on success). */
    public int deletePantryItem(long id) {
        return getWritableDatabase().delete(TABLE_PANTRY,
                COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // ---- helpers: object <-> row ----

    /** Converts a PantryItem into column/value pairs for insert/update. */
    private ContentValues toContentValues(PantryItem item) {
        ContentValues v = new ContentValues();
        v.put(COL_NAME, item.getName().trim());
        v.put(COL_NORMALIZED, IngredientNormalizer.normalize(item.getName()));
        v.put(COL_QUANTITY, item.getQuantity());
        v.put(COL_UNIT, item.getUnit());
        if (item.getExpiryDate() != null) {
            v.put(COL_EXPIRY, item.getExpiryDate());
        } else {
            v.putNull(COL_EXPIRY);
        }
        return v;
    }

    /** Reads the current cursor row back into a PantryItem object. */
    private PantryItem pantryItemFromCursor(Cursor c) {
        PantryItem item = new PantryItem();
        item.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
        item.setName(c.getString(c.getColumnIndexOrThrow(COL_NAME)));
        item.setQuantity(c.getDouble(c.getColumnIndexOrThrow(COL_QUANTITY)));
        item.setUnit(c.getString(c.getColumnIndexOrThrow(COL_UNIT)));
        int expiryIdx = c.getColumnIndexOrThrow(COL_EXPIRY);
        item.setExpiryDate(c.isNull(expiryIdx) ? null : c.getLong(expiryIdx));
        return item;
    }
}