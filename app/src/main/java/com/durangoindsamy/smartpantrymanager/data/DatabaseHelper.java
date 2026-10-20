package com.durangoindsamy.smartpantrymanager.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.durangoindsamy.smartpantrymanager.model.PantryItem;
import com.durangoindsamy.smartpantrymanager.model.Recipe;
import com.durangoindsamy.smartpantrymanager.model.RecipeIngredient;
import com.durangoindsamy.smartpantrymanager.util.IngredientNormalizer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 2;

    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_NORMALIZED = "normalized_name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit";

    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_EXPIRY = "expiry_date";

    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_STEPS = "steps";

    public static final String TABLE_RECIPE_INGREDIENTS =
            "recipe_ingredients";
    public static final String COL_RECIPE_ID = "recipe_id";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
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
                + COL_EXPIRY + " INTEGER)");

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

        RecipeSeeder.seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    // ===================== PANTRY CRUD =====================

    public long insertPantryItem(PantryItem item) {
        long id = getWritableDatabase().insert(
                TABLE_PANTRY, null, toContentValues(item));
        if (id != -1) {
            item.setId(id);
        }
        return id;
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();

        try (Cursor c = getReadableDatabase().query(
                TABLE_PANTRY, null, null, null,
                null, null, COL_NAME + " COLLATE NOCASE ASC")) {

            while (c.moveToNext()) {
                items.add(pantryItemFromCursor(c));
            }
        }

        return items;
    }

    public PantryItem getPantryItem(long id) {
        try (Cursor c = getReadableDatabase().query(
                TABLE_PANTRY, null,
                COL_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null)) {

            return c.moveToFirst() ? pantryItemFromCursor(c) : null;
        }
    }

    public int updatePantryItem(PantryItem item) {
        return getWritableDatabase().update(
                TABLE_PANTRY, toContentValues(item),
                COL_ID + " = ?", new String[]{String.valueOf(item.getId())});
    }

    public int deletePantryItem(long id) {
        return getWritableDatabase().delete(
                TABLE_PANTRY, COL_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    private ContentValues toContentValues(PantryItem item) {
        ContentValues values = new ContentValues();

        values.put(COL_NAME, item.getName().trim());
        values.put(COL_NORMALIZED,
                IngredientNormalizer.normalize(item.getName()));
        values.put(COL_QUANTITY, item.getQuantity());
        values.put(COL_UNIT, item.getUnit());

        if (item.getExpiryDate() != null) {
            values.put(COL_EXPIRY, item.getExpiryDate());
        } else {
            values.putNull(COL_EXPIRY);
        }

        return values;
    }

    private PantryItem pantryItemFromCursor(Cursor c) {
        PantryItem item = new PantryItem();

        item.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
        item.setName(c.getString(c.getColumnIndexOrThrow(COL_NAME)));
        item.setQuantity(c.getDouble(
                c.getColumnIndexOrThrow(COL_QUANTITY)));
        item.setUnit(c.getString(c.getColumnIndexOrThrow(COL_UNIT)));

        int expiryIndex = c.getColumnIndexOrThrow(COL_EXPIRY);
        item.setExpiryDate(c.isNull(expiryIndex)
                ? null : c.getLong(expiryIndex));

        return item;
    }

    // ===================== RECIPES =====================

    static void insertRecipe(SQLiteDatabase db, Recipe recipe) {
        ContentValues recipeValues = new ContentValues();
        recipeValues.put(COL_NAME, recipe.getName());
        recipeValues.put(COL_STEPS, recipe.getSteps());

        long recipeId = db.insert(TABLE_RECIPES, null, recipeValues);
        recipe.setId(recipeId);

        if (recipeId == -1 || recipe.getIngredients() == null) {
            return;
        }

        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            ContentValues values = new ContentValues();

            values.put(COL_RECIPE_ID, recipeId);
            values.put(COL_NAME, ingredient.getName());
            values.put(COL_NORMALIZED,
                    IngredientNormalizer.normalize(ingredient.getName()));
            values.put(COL_QUANTITY, ingredient.getQuantity());
            values.put(COL_UNIT, ingredient.getUnit());

            long ingredientId = db.insert(
                    TABLE_RECIPE_INGREDIENTS, null, values);

            ingredient.setId(ingredientId);
            ingredient.setRecipeId(recipeId);
        }
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor c = db.query(
                TABLE_RECIPES, null, null, null,
                null, null, COL_NAME + " COLLATE NOCASE ASC")) {

            while (c.moveToNext()) {
                recipes.add(recipeFromCursor(db, c));
            }
        }

        return recipes;
    }

    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor c = db.query(
                TABLE_RECIPES, null,
                COL_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null)) {

            return c.moveToFirst() ? recipeFromCursor(db, c) : null;
        }
    }

    private Recipe recipeFromCursor(SQLiteDatabase db, Cursor c) {
        Recipe recipe = new Recipe();

        recipe.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
        recipe.setName(c.getString(c.getColumnIndexOrThrow(COL_NAME)));
        recipe.setSteps(c.getString(c.getColumnIndexOrThrow(COL_STEPS)));
        recipe.setIngredients(
                getIngredientsForRecipe(db, recipe.getId()));

        return recipe;
    }

    private List<RecipeIngredient> getIngredientsForRecipe(
            SQLiteDatabase db, long recipeId) {

        List<RecipeIngredient> ingredients = new ArrayList<>();

        try (Cursor c = db.query(
                TABLE_RECIPE_INGREDIENTS, null,
                COL_RECIPE_ID + " = ?",
                new String[]{String.valueOf(recipeId)},
                null, null, null)) {

            while (c.moveToNext()) {
                RecipeIngredient ingredient = new RecipeIngredient();

                ingredient.setId(c.getLong(
                        c.getColumnIndexOrThrow(COL_ID)));
                ingredient.setRecipeId(recipeId);
                ingredient.setName(c.getString(
                        c.getColumnIndexOrThrow(COL_NAME)));
                ingredient.setQuantity(c.getDouble(
                        c.getColumnIndexOrThrow(COL_QUANTITY)));
                ingredient.setUnit(c.getString(
                        c.getColumnIndexOrThrow(COL_UNIT)));

                ingredients.add(ingredient);
            }
        }

        return ingredients;
    }

    // ===================== STRICT RECIPE MATCHING =====================

    public List<Recipe> getSuggestedRecipes() {
        List<Recipe> pantryItems = new ArrayList<>();
        List<Recipe> matchingRecipes = new ArrayList<>();

        List<PantryItem> actualPantryItems = getAllPantryItems();

        for (Recipe recipe : getAllRecipes()) {
            if (canMakeRecipe(recipe, actualPantryItems)) {
                matchingRecipes.add(recipe);
            }
        }

        return matchingRecipes;
    }

    private boolean canMakeRecipe(
            Recipe recipe, List<PantryItem> pantryItems) {

        if (recipe.getIngredients() == null
                || recipe.getIngredients().isEmpty()) {
            return false;
        }

        for (RecipeIngredient required : recipe.getIngredients()) {
            String requiredName =
                    IngredientNormalizer.normalize(required.getName());

            if (requiredName.isEmpty() || required.getQuantity() <= 0) {
                return false;
            }

            double availableQuantity = 0;

            for (PantryItem item : pantryItems) {
                String pantryName =
                        IngredientNormalizer.normalize(item.getName());

                if (!requiredName.equals(pantryName)) {
                    continue;
                }

                String requiredUnitType = unitType(required.getUnit());
                String pantryUnitType = unitType(item.getUnit());

                if (!requiredUnitType.equals(pantryUnitType)) {
                    continue;
                }

                availableQuantity += toBaseQuantity(
                        item.getQuantity(), item.getUnit());
            }

            double requiredQuantity = toBaseQuantity(
                    required.getQuantity(), required.getUnit());

            if (availableQuantity + 0.000001 < requiredQuantity) {
                return false;
            }
        }

        return true;
    }

    private String unitType(String unit) {
        if (unit == null) {
            return "";
        }

        String u = unit.trim().toLowerCase(Locale.ROOT);

        switch (u) {
            case "g":
            case "gram":
            case "grams":
            case "kg":
            case "kilogram":
            case "kilograms":
                return "mass";

            case "ml":
            case "millilitre":
            case "millilitres":
            case "milliliter":
            case "milliliters":
            case "l":
            case "litre":
            case "litres":
            case "liter":
            case "liters":
                return "volume";

            case "pc":
            case "pcs":
            case "piece":
            case "pieces":
            case "unit":
            case "units":
                return "count";

            default:
                return "other:" + u;
        }
    }

    private double toBaseQuantity(double quantity, String unit) {
        if (unit == null) {
            return quantity;
        }

        String u = unit.trim().toLowerCase(Locale.ROOT);

        switch (u) {
            case "kg":
            case "kilogram":
            case "kilograms":
                return quantity * 1000.0;

            case "l":
            case "litre":
            case "litres":
            case "liter":
            case "liters":
                return quantity * 1000.0;

            default:
                return quantity;
        }
    }
}