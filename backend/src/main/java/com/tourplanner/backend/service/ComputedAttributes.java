package com.tourplanner.backend.service;

public record ComputedAttributes(long logCount, String popularity, String childFriendliness) {

    public static final ComputedAttributes NONE = new ComputedAttributes(0, "Keine", "Unbekannt");
}
