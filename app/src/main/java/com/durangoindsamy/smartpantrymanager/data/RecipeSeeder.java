package com.durangoindsamy.smartpantrymanager.data;

import android.database.sqlite.SQLiteDatabase;

import com.durangoindsamy.smartpantrymanager.model.Recipe;
import com.durangoindsamy.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Pre-loads the recipe collection the first time the database is created. */
final class RecipeSeeder {

    private RecipeSeeder() { }

    static void seed(SQLiteDatabase db) {
        for (Recipe recipe : buildRecipes()) {
            DatabaseHelper.insertRecipe(db, recipe);
        }
    }

    private static List<Recipe> buildRecipes() {
        List<Recipe> list = new ArrayList<>();

        list.add(recipe("Scrambled Eggs",
                "1. Whisk the eggs with the milk; season to taste.\n"
                        + "2. Melt the butter in a pan over low heat.\n"
                        + "3. Add the eggs and stir gently until just set.",
                ing("Eggs", 3, "pcs"), ing("Milk", 30, "ml"), ing("Butter", 10, "g")));

        list.add(recipe("Cheese Omelette",
                "1. Beat the eggs and season to taste.\n"
                        + "2. Melt the butter in a pan and pour in the eggs.\n"
                        + "3. When almost set, add grated cheese, fold and serve.",
                ing("Eggs", 3, "pcs"), ing("Cheese", 40, "g"), ing("Butter", 10, "g")));

        list.add(recipe("French Toast",
                "1. Whisk the eggs and milk in a shallow dish.\n"
                        + "2. Soak each bread slice on both sides.\n"
                        + "3. Fry in butter until golden on both sides.",
                ing("Bread", 4, "pcs"), ing("Eggs", 2, "pcs"),
                ing("Milk", 100, "ml"), ing("Butter", 15, "g")));

        list.add(recipe("Pancakes",
                "1. Mix the flour and sugar, then whisk in the eggs and milk.\n"
                        + "2. Melt a little butter in a pan.\n"
                        + "3. Pour in small rounds and cook until bubbles form, then flip.",
                ing("Flour", 150, "g"), ing("Eggs", 2, "pcs"), ing("Milk", 250, "ml"),
                ing("Butter", 20, "g"), ing("Sugar", 20, "g")));

        list.add(recipe("Grilled Cheese Sandwich",
                "1. Butter one side of each bread slice.\n"
                        + "2. Place cheese between the unbuttered sides.\n"
                        + "3. Fry in a pan until golden and the cheese melts.",
                ing("Bread", 2, "pcs"), ing("Cheese", 50, "g"), ing("Butter", 15, "g")));

        list.add(recipe("Tomato Pasta",
                "1. Cook the pasta in salted water and drain.\n"
                        + "2. Fry chopped onion and garlic in olive oil until soft.\n"
                        + "3. Add chopped tomatoes and simmer 10 minutes; toss with the pasta.",
                ing("Pasta", 200, "g"), ing("Tomatoes", 4, "pcs"), ing("Onion", 1, "pcs"),
                ing("Garlic", 2, "pcs"), ing("Olive oil", 30, "ml")));

        list.add(recipe("Garlic Butter Pasta",
                "1. Cook the pasta and keep a little cooking water.\n"
                        + "2. Gently fry sliced garlic in the butter.\n"
                        + "3. Toss the pasta in the garlic butter and finish with cheese.",
                ing("Pasta", 200, "g"), ing("Butter", 40, "g"),
                ing("Garlic", 3, "pcs"), ing("Cheese", 30, "g")));

        list.add(recipe("Spaghetti Bolognese",
                "1. Brown the mince with chopped onion and garlic.\n"
                        + "2. Add chopped tomatoes and simmer 20 minutes; season to taste.\n"
                        + "3. Serve over the cooked pasta.",
                ing("Beef mince", 500, "g"), ing("Pasta", 250, "g"), ing("Tomatoes", 4, "pcs"),
                ing("Onion", 1, "pcs"), ing("Garlic", 2, "pcs")));

        list.add(recipe("Tuna Pasta Salad",
                "1. Cook the pasta, drain and cool.\n"
                        + "2. Mix in the drained tuna, chopped onion and mayonnaise.\n"
                        + "3. Season to taste and chill before serving.",
                ing("Pasta", 200, "g"), ing("Tuna", 170, "g"),
                ing("Mayonnaise", 60, "g"), ing("Onion", 1, "pcs")));

        list.add(recipe("Egg Fried Rice",
                "1. Fry chopped onion and grated carrot until soft.\n"
                        + "2. Push aside, scramble the eggs, then mix together.\n"
                        + "3. Add the cooked rice and soy sauce and stir-fry until hot.",
                ing("Rice", 200, "g"), ing("Eggs", 2, "pcs"), ing("Onion", 1, "pcs"),
                ing("Carrot", 1, "pcs"), ing("Soy sauce", 30, "ml")));

        list.add(recipe("Vegetable Stir Fry",
                "1. Slice the carrots, onion and green pepper thinly.\n"
                        + "2. Stir-fry with garlic over high heat for 5 minutes.\n"
                        + "3. Add soy sauce and serve over cooked rice.",
                ing("Carrot", 2, "pcs"), ing("Onion", 1, "pcs"), ing("Green pepper", 1, "pcs"),
                ing("Garlic", 2, "pcs"), ing("Soy sauce", 30, "ml"), ing("Rice", 200, "g")));

        list.add(recipe("Chicken Curry",
                "1. Fry chopped onion and garlic, then stir in the curry powder.\n"
                        + "2. Add the chicken pieces and brown them.\n"
                        + "3. Add chopped tomatoes and simmer 25 minutes; serve with rice.",
                ing("Chicken", 500, "g"), ing("Onion", 1, "pcs"), ing("Tomatoes", 3, "pcs"),
                ing("Garlic", 3, "pcs"), ing("Curry powder", 15, "g"), ing("Rice", 250, "g")));

        list.add(recipe("Chicken and Rice",
                "1. Brown the chicken pieces with chopped onion and garlic.\n"
                        + "2. Add the rice and twice its volume of water.\n"
                        + "3. Cover and simmer until the rice is cooked; season to taste.",
                ing("Chicken", 400, "g"), ing("Rice", 250, "g"),
                ing("Onion", 1, "pcs"), ing("Garlic", 2, "pcs")));

        list.add(recipe("Tomato Soup",
                "1. Soften chopped onion and garlic in the butter.\n"
                        + "2. Add chopped tomatoes and a cup of water; simmer 20 minutes.\n"
                        + "3. Blend until smooth and season to taste.",
                ing("Tomatoes", 6, "pcs"), ing("Onion", 1, "pcs"),
                ing("Garlic", 2, "pcs"), ing("Butter", 20, "g")));

        list.add(recipe("Mashed Potatoes",
                "1. Peel and boil the potatoes until soft, then drain.\n"
                        + "2. Mash with the butter.\n"
                        + "3. Stir in warm milk until creamy; season to taste.",
                ing("Potatoes", 5, "pcs"), ing("Butter", 40, "g"), ing("Milk", 100, "ml")));

        list.add(recipe("Potato Wedges",
                "1. Cut the potatoes into wedges.\n"
                        + "2. Toss with olive oil and season to taste.\n"
                        + "3. Roast at 200°C for 35-40 minutes, turning once.",
                ing("Potatoes", 4, "pcs"), ing("Olive oil", 30, "ml")));

        list.add(recipe("Banana Smoothie",
                "1. Peel and slice the bananas.\n"
                        + "2. Blend with the milk and honey until smooth.\n"
                        + "3. Serve immediately.",
                ing("Bananas", 2, "pcs"), ing("Milk", 250, "ml"), ing("Honey", 15, "ml")));

        list.add(recipe("Oat Porridge",
                "1. Simmer the oats in the milk for 5 minutes, stirring.\n"
                        + "2. Slice the banana on top.\n"
                        + "3. Drizzle with honey and serve.",
                ing("Oats", 80, "g"), ing("Milk", 300, "ml"),
                ing("Honey", 15, "ml"), ing("Banana", 1, "pcs")));

        return list;
    }

    // ---- small helpers to keep the recipe list readable ----

    private static Recipe recipe(String name, String steps, RecipeIngredient... ingredients) {
        return new Recipe(name, steps, new ArrayList<>(Arrays.asList(ingredients)));
    }

    private static RecipeIngredient ing(String name, double quantity, String unit) {
        return new RecipeIngredient(name, quantity, unit);
    }
}