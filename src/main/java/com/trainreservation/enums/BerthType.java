package com.trainreservation.enums;

// BerthType defines the position of a seat in a train compartment
public enum BerthType {

    LOWER,        // Bottom berth — most preferred, easy to sit
    MIDDLE,       // Middle berth — less preferred
    UPPER,        // Top berth — cheapest preference
    SIDE_LOWER,   // Side berth bottom — near the door
    SIDE_UPPER,   // Side berth top
    NONE          // For General class — no berth concept
}