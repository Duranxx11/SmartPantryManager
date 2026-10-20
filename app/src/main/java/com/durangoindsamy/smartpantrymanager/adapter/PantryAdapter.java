package com.durangoindsamy.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.durangoindsamy.smartpantrymanager.R;
import com.durangoindsamy.smartpantrymanager.model.PantryItem;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Connects the list of PantryItems to the RecyclerView.
 * The RecyclerView only creates enough row views to fill the screen and
 * re-uses them as you scroll - onBindViewHolder fills a re-used row with new data.
 */
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    /** Lets the Activity decide what happens when a row is tapped. */
    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public PantryAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    /** Replaces the displayed data, e.g. after the database changes. */
    public void setItems(List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate (build) one row view from item_pantry.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);
        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PantryViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Holds references to one row's views so findViewById runs only once per row. */
    static class PantryViewHolder extends RecyclerView.ViewHolder {

        private final TextView textName;
        private final TextView textQuantity;
        private final TextView textExpiry;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textExpiry = itemView.findViewById(R.id.textExpiry);
        }

        void bind(PantryItem item, OnItemClickListener listener) {
            textName.setText(item.getName());
            textQuantity.setText(itemView.getContext().getString(
                    R.string.quantity_format, formatQuantity(item.getQuantity()), item.getUnit()));

            if (item.getExpiryDate() != null) {
                String date = DateFormat.getDateInstance().format(new Date(item.getExpiryDate()));
                textExpiry.setText(itemView.getContext().getString(R.string.expires_on, date));
            } else {
                textExpiry.setText(R.string.no_expiry);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        /** Shows 3 instead of 3.0, but keeps 0.5 as 0.5. */
        private static String formatQuantity(double q) {
            return q == Math.floor(q) ? String.valueOf((long) q) : String.valueOf(q);
        }
    }
}