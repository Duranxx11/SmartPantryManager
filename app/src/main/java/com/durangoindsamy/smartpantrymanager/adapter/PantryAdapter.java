package com.durangoindsamy.smartpantrymanager.adapter;

import android.graphics.Color;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class PantryAdapter
        extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(PantryItem item);
    }

    private final List<PantryItem> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public PantryAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);

        return new PantryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PantryViewHolder holder, int position) {

        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PantryViewHolder extends RecyclerView.ViewHolder {

        private final TextView textName;
        private final TextView textQuantity;
        private final TextView textExpiry;
        private final int defaultExpiryColor;

        PantryViewHolder(@NonNull View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.textName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textExpiry = itemView.findViewById(R.id.textExpiry);

            defaultExpiryColor = textExpiry.getCurrentTextColor();
        }

        void bind(PantryItem item, OnItemClickListener listener) {

            textName.setText(item.getName());

            textQuantity.setText(
                    itemView.getContext().getString(
                            R.string.quantity_format,
                            formatQuantity(item.getQuantity()),
                            item.getUnit()
                    )
            );

            Long expiryDate = item.getExpiryDate();

            if (expiryDate == null) {
                textExpiry.setText(R.string.no_expiry);
                textExpiry.setTextColor(defaultExpiryColor);

            } else {
                Date expiry = new Date(expiryDate);

                String formattedDate = DateFormat
                        .getDateInstance()
                        .format(expiry);

                Calendar today = Calendar.getInstance();
                setToStartOfDay(today);

                Calendar expiryDay = Calendar.getInstance();
                expiryDay.setTime(expiry);
                setToStartOfDay(expiryDay);

                long difference = expiryDay.getTimeInMillis()
                        - today.getTimeInMillis();

                long daysLeft = Math.round(
                        difference / 86400000.0
                );

                if (daysLeft < 0) {
                    textExpiry.setText(
                            "EXPIRED - " + formattedDate
                    );
                    textExpiry.setTextColor(Color.RED);

                } else if (daysLeft == 0) {
                    textExpiry.setText(
                            "EXPIRES TODAY - " + formattedDate
                    );
                    textExpiry.setTextColor(
                            Color.rgb(220, 100, 0)
                    );

                } else if (daysLeft <= 3) {
                    String message = daysLeft == 1
                            ? "Expires in 1 day - "
                            : "Expires in " + daysLeft + " days - ";

                    textExpiry.setText(message + formattedDate);
                    textExpiry.setTextColor(
                            Color.rgb(183, 121, 31)
                    );

                } else {
                    textExpiry.setText(
                            itemView.getContext().getString(
                                    R.string.expires_on,
                                    formattedDate
                            )
                    );
                    textExpiry.setTextColor(defaultExpiryColor);
                }
            }

            itemView.setOnClickListener(
                    v -> listener.onItemClick(item)
            );
        }

        private static void setToStartOfDay(Calendar calendar) {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }

        private static String formatQuantity(double quantity) {
            return quantity == Math.floor(quantity)
                    ? String.valueOf((long) quantity)
                    : String.valueOf(quantity);
        }
    }
}