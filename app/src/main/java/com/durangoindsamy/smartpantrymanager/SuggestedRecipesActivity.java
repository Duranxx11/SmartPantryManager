package com.durangoindsamy.smartpantrymanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.durangoindsamy.smartpantrymanager.data.DatabaseHelper;
import com.durangoindsamy.smartpantrymanager.model.Recipe;
import com.durangoindsamy.smartpantrymanager.model.RecipeIngredient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SuggestedRecipesActivity extends Activity {

    private static final String TAG = "PantryDebug";

    private DatabaseHelper db;
    private ListView listRecipes;
    private TextView textEmpty;
    private List<Recipe> suggestedRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        db = DatabaseHelper.getInstance(this);

        listRecipes = findViewById(R.id.listRecipes);
        textEmpty = findViewById(R.id.textEmptyRecipes);

        listRecipes.setOnItemClickListener((parent, view, position, id) -> {
            if (suggestedRecipes != null
                    && position >= 0
                    && position < suggestedRecipes.size()) {

                Recipe recipe = suggestedRecipes.get(position);

                Intent intent = new Intent(
                        SuggestedRecipesActivity.this,
                        RecipeDetailActivity.class
                );

                intent.putExtra("recipe_id", recipe.getId());
                startActivity(intent);
            }
        });

        loadSuggestedRecipes();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (db != null && listRecipes != null) {
            loadSuggestedRecipes();
        }
    }

    private void loadSuggestedRecipes() {
        suggestedRecipes = db.getSuggestedRecipes();

        List<Recipe> allRecipes = db.getAllRecipes();
        List<?> pantryItems = db.getAllPantryItems();

        Log.d(TAG, "Total recipes in database: " + allRecipes.size());
        Log.d(TAG, "Pantry ingredients: " + pantryItems.size());
        Log.d(TAG, "Matching suggested recipes: " + suggestedRecipes.size());

        List<String> recipeNames = new ArrayList<>();

        for (Recipe recipe : suggestedRecipes) {
            StringBuilder details = new StringBuilder();

            details.append(recipe.getName()).append("\n\n");

            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                details.append("• ")
                        .append(ingredient.getName())
                        .append(" — ")
                        .append(formatQuantity(ingredient.getQuantity()))
                        .append(" ")
                        .append(ingredient.getUnit())
                        .append("\n");
            }

            recipeNames.add(details.toString().trim());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                recipeNames
        );

        listRecipes.setAdapter(adapter);

        if (suggestedRecipes.isEmpty()) {
            textEmpty.setText(
                    "No matching recipes found.\n\n"
                            + "Check that your pantry contains every "
                            + "required ingredient in the required quantity."
            );

            textEmpty.setVisibility(View.VISIBLE);
            listRecipes.setVisibility(View.GONE);
        } else {
            textEmpty.setVisibility(View.GONE);
            listRecipes.setVisibility(View.VISIBLE);
        }
    }

    private String formatQuantity(double quantity) {
        return BigDecimal.valueOf(quantity)
                .stripTrailingZeros()
                .toPlainString();
    }
}