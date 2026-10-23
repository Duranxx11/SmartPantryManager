package com.durangoindsamy.smartpantrymanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.durangoindsamy.smartpantrymanager.data.DatabaseHelper;
import com.durangoindsamy.smartpantrymanager.model.PantryItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddEditPantryActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "pantry_item_id";

    private EditText etIngredientName;
    private EditText etQuantity;
    private EditText etUnit;
    private EditText etExpiryDate;

    private DatabaseHelper db;
    private long itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_pantry);

        db = DatabaseHelper.getInstance(this);

        etIngredientName = findViewById(R.id.etIngredientName);
        etQuantity = findViewById(R.id.etQuantity);
        etUnit = findViewById(R.id.etUnit);
        etExpiryDate = findViewById(R.id.etExpiryDate);

        Button btnSave = findViewById(R.id.btnSaveIngredient);
        Button btnCancel = findViewById(R.id.btnCancel);

        // Check whether an existing ingredient is being edited.
        itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);

        if (itemId != -1) {
            loadIngredientForEditing();
        }

        btnSave.setOnClickListener(v -> saveIngredient());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadIngredientForEditing() {
        // Load only the row being edited, using its database id.
        PantryItem item = db.getPantryItem(itemId);

        if (item == null) {
            Toast.makeText(
                    this,
                    "Ingredient not found",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        etIngredientName.setText(item.getName());
        etQuantity.setText(String.valueOf(item.getQuantity()));
        etUnit.setText(item.getUnit());

        if (item.getExpiryDate() != null) {
            SimpleDateFormat formatter =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            etExpiryDate.setText(
                    formatter.format(new Date(item.getExpiryDate()))
            );
        } else {
            etExpiryDate.setText("");
        }

        setTitle("Edit Ingredient");
    }

    private void saveIngredient() {
        String name = etIngredientName.getText().toString().trim();
        String quantityText = etQuantity.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();
        String expiryText = etExpiryDate.getText().toString().trim();

        if (name.isEmpty()) {
            etIngredientName.setError("Enter an ingredient name");
            etIngredientName.requestFocus();
            return;
        }

        if (quantityText.isEmpty()) {
            etQuantity.setError("Enter a quantity");
            etQuantity.requestFocus();
            return;
        }

        double quantity;

        try {
            quantity = Double.parseDouble(quantityText);

            if (!Double.isFinite(quantity) || quantity <= 0) {
                etQuantity.setError("Quantity must be greater than zero");
                etQuantity.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etQuantity.setError("Enter a valid quantity");
            etQuantity.requestFocus();
            return;
        }

        if (unit.isEmpty()) {
            etUnit.setError("Enter a unit, e.g. pcs, g or ml");
            etUnit.requestFocus();
            return;
        }

        Long expiryTimestamp = null;

        if (!expiryText.isEmpty()) {
            SimpleDateFormat formatter =
                    new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            formatter.setLenient(false);

            try {
                Date expiryDate = formatter.parse(expiryText);

                if (expiryDate == null) {
                    etExpiryDate.setError("Enter a valid expiry date");
                    etExpiryDate.requestFocus();
                    return;
                }

                expiryTimestamp = expiryDate.getTime();

            } catch (ParseException e) {
                etExpiryDate.setError("Use the date format yyyy-MM-dd");
                etExpiryDate.requestFocus();
                return;
            }
        }

        PantryItem item = new PantryItem(
                name,
                quantity,
                unit,
                expiryTimestamp
        );

        boolean success;

        if (itemId == -1) {
            // Add a new ingredient.
            success = db.insertPantryItem(item) != -1;
        } else {
            // Update the existing ingredient.
            item.setId(itemId);
            success = db.updatePantryItem(item) > 0;
        }

        if (success) {
            Toast.makeText(
                    this,
                    itemId == -1
                            ? "Ingredient saved successfully!"
                            : "Ingredient updated successfully!",
                    Toast.LENGTH_SHORT
            ).show();

            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(
                    this,
                    "Could not save changes. Please try again.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}