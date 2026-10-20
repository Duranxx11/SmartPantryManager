package com.durangoindsamy.smartpantrymanager.model;

import java.util.ArrayList;
import java.util.List;

/** A recipe with its method and list of required ingredients. */
public class Recipe {

    private long id;
    private String name;
    private String steps;   // numbered steps separated by new lines
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    public Recipe() { }

    public Recipe(String name, String steps, List<RecipeIngredient> ingredients) {
        this.name = name;
        this.steps = steps;
        this.ingredients = ingredients;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSteps() { return steps; }
    public void setSteps(String steps) { this.steps = steps; }

    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) { this.ingredients = ingredients; }
}