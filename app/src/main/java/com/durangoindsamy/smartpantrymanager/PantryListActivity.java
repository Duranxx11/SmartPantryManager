package com.durangoindsamy.smartpantrymanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.durangoindsamy.smartpantrymanager.adapter.PantryAdapter;
import com.durangoindsamy.smartpantrymanager.data.DatabaseHelper;
import com.durangoindsamy.smartpantrymanager.model.PantryItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PantryListActivity extends AppCompatActivity {

    public static final String EXTRA_EXPIRY_ITEM_ID = "pantry_item_id";
    public static final String EXTRA_EXPIRY_ITEM_NAME = "pantry_item_name";

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private RecyclerView recyclerPantry;
    private TextView textEmpty;
    private EditText etSearchPantry;

    private List<PantryItem> allPantryItems = new ArrayList<>();

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        String message = isGranted
                                ? "Expiry notifications enabled"
                                : "Notifications are disabled";

                        Toast.makeText(
                                PantryListActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
            );

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
        etSearchPantry = findViewById(R.id.etSearchPantry);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        MaterialButton btnSuggestedRecipes =
                findViewById(R.id.btnSuggestedRecipes);
        MaterialButton btnSettings = findViewById(R.id.btnSettings);

        // Set up the pantry list.
        adapter = new PantryAdapter(this::showIngredientOptions);
        recyclerPantry.setAdapter(adapter);

        // Open the Suggested Recipes screen.
        if (btnSuggestedRecipes != null) {
            btnSuggestedRecipes.setOnClickListener(v -> {
                Intent intent = new Intent(
                        PantryListActivity.this,
                        SuggestedRecipesActivity.class
                );
                startActivity(intent);
            });
        }

        // Open the Settings screen.
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                Intent intent = new Intent(
                        PantryListActivity.this,
                        SettingsActivity.class
                );
                startActivity(intent);
            });
        }

        // Open the Add Ingredient screen.
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                Intent intent = new Intent(
                        PantryListActivity.this,
                        AddEditPantryActivity.class
                );
                startActivity(intent);
            });
        }

        // Search pantry ingredients.
        etSearchPantry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                filterPantry(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        setupExpiryNotifications();
    }

    private void setupExpiryNotifications() {
        ExpiryNotificationScheduler.schedule(this);

        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {

            notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (db != null && etSearchPantry != null) {
            loadPantry();
            handleExpiryNotificationIntent();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        loadPantry();
        handleExpiryNotificationIntent();
    }

    // Open the exact ingredient when its expiry notification is tapped.
    private void handleExpiryNotificationIntent() {
        Intent intent = getIntent();

        if (intent == null
                || (!intent.hasExtra(EXTRA_EXPIRY_ITEM_ID)
                && !intent.hasExtra(EXTRA_EXPIRY_ITEM_NAME))) {
            return;
        }

        long targetId = intent.getLongExtra(EXTRA_EXPIRY_ITEM_ID, -1L);
        String targetName = intent.getStringExtra(EXTRA_EXPIRY_ITEM_NAME);

        PantryItem targetItem = null;

        // Prefer the unique database ID.
        if (targetId != -1L) {
            for (PantryItem item : allPantryItems) {
                if (item.getId() == targetId) {
                    targetItem = item;
                    break;
                }
            }
        }

        // Fall back to the ingredient name if needed.
        if (targetItem == null && targetName != null) {
            for (PantryItem item : allPantryItems) {
                if (targetName.equalsIgnoreCase(item.getName())) {
                    targetItem = item;
                    break;
                }
            }
        }

        // Remove the extras so the same alert isn't handled repeatedly.
        intent.removeExtra(EXTRA_EXPIRY_ITEM_ID);
        intent.removeExtra(EXTRA_EXPIRY_ITEM_NAME);

        if (targetItem == null) {
            Toast.makeText(
                    this,
                    "This ingredient is no longer in your pantry.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        final PantryItem selectedItem = targetItem;

        // Filter the list to the ingredient from the notification.
        etSearchPantry.setText(selectedItem.getName());

        // Scroll to the matching ingredient.
        recyclerPantry.post(() -> {
            List<PantryItem> filteredItems = new ArrayList<>();

            for (PantryItem item : allPantryItems) {
                if (item.getName() != null
                        && item.getName().equalsIgnoreCase(
                        selectedItem.getName())) {
                    filteredItems.add(item);
                }
            }

            int position = -1;

            for (int i = 0; i < filteredItems.size(); i++) {
                if (filteredItems.get(i).getId() == selectedItem.getId()) {
                    position = i;
                    break;
                }
            }

            if (position >= 0) {
                recyclerPantry.scrollToPosition(position);
            }

            Toast.makeText(
                    PantryListActivity.this,
                    "Showing " + selectedItem.getName(),
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void loadPantry() {
        allPantryItems = db.getAllPantryItems();
        filterPantry(etSearchPantry.getText().toString());
    }

    private void filterPantry(String searchText) {
        List<PantryItem> filteredItems = new ArrayList<>();
        String query = searchText.trim().toLowerCase(Locale.ROOT);

        for (PantryItem item : allPantryItems) {
            if (item.getName() != null
                    && item.getName().toLowerCase(Locale.ROOT)
                    .contains(query)) {
                filteredItems.add(item);
            }
        }

        adapter.setItems(filteredItems);

        if (allPantryItems.isEmpty()) {
            textEmpty.setText(
                    "Your pantry is empty. Add an ingredient to get started."
            );
            textEmpty.setVisibility(View.VISIBLE);
            recyclerPantry.setVisibility(View.GONE);

        } else if (filteredItems.isEmpty()) {
            textEmpty.setText("No matching ingredients found.");
            textEmpty.setVisibility(View.VISIBLE);
            recyclerPantry.setVisibility(View.GONE);

        } else {
            textEmpty.setVisibility(View.GONE);
            recyclerPantry.setVisibility(View.VISIBLE);
        }
    }

    private void showIngredientOptions(PantryItem item) {
        String[] options = {
                "Edit Ingredient",
                "Delete Ingredient"
        };

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
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> deleteIngredient(item)
                )
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteIngredient(PantryItem item) {
        int rowsDeleted = db.deletePantryItem(item.getId());

        if (rowsDeleted > 0) {
            Toast.makeText(
                    this,
                    item.getName() + " deleted successfully!",
                    Toast.LENGTH_LONG
            ).show();

            loadPantry();
        } else {
            Toast.makeText(
                    this,
                    "Could not delete ingredient.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}