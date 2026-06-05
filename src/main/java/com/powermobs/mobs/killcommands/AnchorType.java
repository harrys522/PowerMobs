package com.powermobs.mobs.killcommands;

/**
 * Where a kill command's position is anchored. The command itself is always
 * dispatched from the console sender (full permissions); this enum just
 * controls the vanilla {@code execute} wrapper around it.
 */
public enum AnchorType {
    /** Position is the mob's death location. {@code ~ ~ ~} resolves to it. Always available. */
    MOB,
    /** Position is the killer's current location. {@code ~ ~ ~} resolves to it. Skipped if killer absent or offline. */
    KILLER;

    public static AnchorType fromString(String value, AnchorType fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return AnchorType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
