package com.durangoindsamy.smartpantrymanager.model;

/**
 * One ingredient the user has at home.
 * Plain Java object - the database layer converts it to/from table rows.
 */
public class PantryItem {

    private long id;
    private String name;       // as the user typed it, e.g. "Tomatoes"
    private double quantity;   // e.g. 3 or 0.5
    private String unit;       // e.g. "pcs", "g", "ml"
    private Long expiryDate;   // epoch millis; null = no expiry set (hence Long, not long)

    public PantryItem() { }

    public PantryItem(String name, double quantity, String unit, Long expiryDate) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Long getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Long expiryDate) { this.expiryDate = expiryDate; }
}