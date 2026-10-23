package com.durangoindsamy.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.durangoindsamy.smartpantrymanager.data.DatabaseHelper;
import com.durangoindsamy.smartpantrymanager.model.Recipe;
import com.durangoindsamy.smartpantrymanager.model.RecipeIngredient;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private ListView listRecipes;
    private TextView textEmpty;
    private View emptyRecipesCard;
    private List<Recipe> suggestedRecipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        db = DatabaseHelper.getInstance(this);

        listRecipes = findViewById(R.id.listRecipes);
        textEmpty = findViewById(R.id.textEmptyRecipes);
        emptyRecipesCard = findViewById(R.id.emptyRecipesCard);

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

        // Bottom bar: Pantry | Recipes (this screen).
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_recipes);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_pantry) {
                // The Pantry List is underneath this screen, so close
                // this one to go back to it.
                finish();
                overridePendingTransition(0, 0);
                return true;
            }
            return item.getItemId() == R.id.nav_recipes;
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

        if (suggestedRecipes == null) {
            suggestedRecipes = new ArrayList<>();
        }

        RecipeCardAdapter adapter =
                new RecipeCardAdapter(suggestedRecipes);

        listRecipes.setAdapter(adapter);

        if (suggestedRecipes.isEmpty()) {
            textEmpty.setText(
                    "No recipes match your pantry right now.\n\n"
                            + "Make sure you have every required "
                            + "ingredient in the correct quantity, "
                            + "or add more items to your pantry."
            );

            emptyRecipesCard.setVisibility(View.VISIBLE);
            listRecipes.setVisibility(View.GONE);

        } else {
            emptyRecipesCard.setVisibility(View.GONE);
            listRecipes.setVisibility(View.VISIBLE);
        }
    }

    private String formatQuantity(double quantity) {
        return BigDecimal.valueOf(quantity)
                .stripTrailingZeros()
                .toPlainString();
    }

    private class RecipeCardAdapter extends BaseAdapter {

        private final List<Recipe> recipes;

        RecipeCardAdapter(List<Recipe> recipes) {
            this.recipes = recipes;
        }

        @Override
        public int getCount() {
            return recipes.size();
        }

        @Override
        public Recipe getItem(int position) {
            return recipes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(
                int position,
                View convertView,
                ViewGroup parent
        ) {
            View row = convertView;

            if (row == null) {
                row = LayoutInflater.from(
                        SuggestedRecipesActivity.this
                ).inflate(
                        R.layout.item_suggested_recipe,
                        parent,
                        false
                );
            }

            TextView recipeName =
                    row.findViewById(R.id.recipeName);

            TextView ingredientCount =
                    row.findViewById(R.id.recipeIngredientCount);

            TextView ingredientPreview =
                    row.findViewById(R.id.recipeIngredientPreview);

            Recipe recipe = getItem(position);

            recipeName.setText(recipe.getName());

            List<RecipeIngredient> ingredients =
                    recipe.getIngredients();

            int count = ingredients == null ? 0 : ingredients.size();

            ingredientCount.setText(
                    count + (count == 1
                            ? " required ingredient"
                            : " required ingredients")
            );

            StringBuilder preview = new StringBuilder();

            if (ingredients != null) {
                int previewLimit = Math.min(3, ingredients.size());

                for (int i = 0; i < previewLimit; i++) {
                    RecipeIngredient ingredient = ingredients.get(i);

                    if (i > 0) {
                        preview.append("  •  ");
                    }

                    preview.append(ingredient.getName())
                            .append(" (")
                            .append(formatQuantity(
                                    ingredient.getQuantity()
                            ))
                            .append(" ")
                            .append(ingredient.getUnit())
                            .append(")");
                }

                if (ingredients.size() > previewLimit) {
                    preview.append("  •  +")
                            .append(ingredients.size() - previewLimit)
                            .append(" more");
                }
            }

            ingredientPreview.setText(preview.toString());

            return row;
        }
    }
}