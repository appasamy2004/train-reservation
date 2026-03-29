package com.trainreservation.enums;

// SeatType defines all train classes with their display name and price
public enum SeatType {

    AC_FIRST("First AC", 1500.0),
    AC_2TIER("2 Tier AC", 900.0),
    AC_3TIER("3 Tier AC", 600.0),
    SLEEPER("Sleeper",    250.0),
    GENERAL("General",   100.0);

    // Each seat type has a display name and a base price
    private final String displayName;
    private final double basePrice;

    // Constructor — runs when each value is defined above
    SeatType(String displayName, double basePrice) {
        this.displayName = displayName;
        this.basePrice   = basePrice;
    }

    // Getters — so we can use seatType.getDisplayName() anywhere
    public String getDisplayName() { return displayName; }
    public double getBasePrice()   { return basePrice; }
}