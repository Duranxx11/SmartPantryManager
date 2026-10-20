package com.durangoindsamy.smartpantrymanager;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.durangoindsamy.smartpantrymanager.data.DatabaseHelper;
import com.durangoindsamy.smartpantrymanager.model.Recipe;
import com.durangoindsamy.smartpantrymanager.model.RecipeIngredient;

import java.util.Locale;

public class RecipeDetailActivity extends Activity {

    private DatabaseHelper db;
    private TextView textRecipeName;
    private TextView textIngredients;
    private TextView textInstructions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        db = DatabaseHelper.getInstance(this);

        textRecipeName = findViewById(R.id.textRecipeName);
        textIngredients = findViewById(R.id.textRecipeIngredients);
        textInstructions = findViewById(R.id.textRecipeInstructions);

        long recipeId = getIntent().getLongExtra("recipe_id", -1);

        if (recipeId == -1) {
            Toast.makeText(
                    this,
                    "Recipe could not be opened.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        Recipe recipe = db.getRecipe(recipeId);

        if (recipe == null) {
            Toast.makeText(
                    this,
                    "Recipe not found.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        displayRecipe(recipe);
    }

    private void displayRecipe(Recipe recipe) {
        textRecipeName.setText(recipe.getName());

        StringBuilder ingredients = new StringBuilder();

        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            ingredients.append("• ")
                    .append(ingredient.getName())
                    .append(" — ")
                    .append(formatQuantity(ingredient.getQuantity()))
                    .append(" ")
                    .append(ingredient.getUnit())
                    .append("\n");
        }

        textIngredients.setText(
                ingredients.length() > 0
                        ? ingredients.toString().trim()
                        : "No ingredients listed."
        );

        String instructions = recipe.getSteps();

        if (instructions == null || instructions.trim().isEmpty()) {
            instructions =
                    "Preparation instructions are not available yet.";
        }

        textInstructions.setText(instructions);
    }

    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.format(Locale.US, "%.0f", quantity);
        }

        return String.format(Locale.US, "%.2f", quantity)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
    }
}