package com.durangoindsamy.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.durangoindsamy.smartpantrymanager.adapter.PantryAdapter;
import com.durangoindsamy.smartpantrymanager.data.DatabaseHelper;
import com.durangoindsamy.smartpantrymanager.model.PantryItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class PantryListActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private RecyclerView recyclerPantry;
    private TextView textEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantry_list);

        View mainView = findViewById(R.id.main);

        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(
                        WindowInsetsCompat.Type.systemBars()
                );

                v.setPadding(
                        systemBars.left,
                        systemBars.top,
                        systemBars.right,
                        systemBars.bottom
                );

                return insets;
            });
        }

        db = DatabaseHelper.getInstance(this);

        recyclerPantry = findViewById(R.id.recyclerPantry);
        textEmpty = findViewById(R.id.textEmpty);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        // Tapping an ingredient shows Edit and Delete options.
        adapter = new PantryAdapter(this::showIngredientOptions);
        recyclerPantry.setAdapter(adapter);

        // Open a blank form to add an ingredient.
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(
                    PantryListActivity.this,
                    AddEditPantryActivity.class
            );
            startActivity(intent);
        });
    }

    private void showIngredientOptions(PantryItem item) {
        String[] options = {"Edit Ingredient", "Delete Ingredient"};

        new AlertDialog.Builder(this)
                .setTitle(item.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        editIngredient(item);
                    } else if (which == 1) {
                        confirmDeleteIngredient(item);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void editIngredient(PantryItem item) {
        Intent intent = new Intent(
                PantryListActivity.this,
                AddEditPantryActivity.class
        );

        intent.putExtra(
                AddEditPantryActivity.EXTRA_ITEM_ID,
                item.getId()
        );

        startActivity(intent);
    }

    private void confirmDeleteIngredient(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Ingredient")
                .setMessage(
                        "Are you sure you want to delete "
                                + item.getName() + "?"
                )
                .setPositiveButton("Delete", (dialog, which) ->
                        deleteIngredient(item))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteIngredient(PantryItem item) {
        int rowsDeleted = db.deletePantryItem(item.getId());

        if (rowsDeleted > 0) {
            Toast.makeText(
                    PantryListActivity.this,
                    item.getName() + " deleted successfully!",
                    Toast.LENGTH_LONG
            ).show();

            loadPantry();

        } else {
            Toast.makeText(
                    PantryListActivity.this,
                    "Could not delete ingredient.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    private void loadPantry() {
        List<PantryItem> items = db.getAllPantryItems();

        adapter.setItems(items);

        boolean isEmpty = items.isEmpty();

        textEmpty.setVisibility(
                isEmpty ? View.VISIBLE : View.GONE
        );

        recyclerPantry.setVisibility(
                isEmpty ? View.GONE : View.VISIBLE
        );
    }
}