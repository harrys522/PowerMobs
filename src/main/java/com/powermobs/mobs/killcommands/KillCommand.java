package com.powermobs.mobs.killcommands;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One configured kill-command entry.
 */
@Getter
public class KillCommand {

    private final String command;
    private final double chance;
    private final AnchorType at;

    public KillCommand(String command, double chance, AnchorType at) {
        this.command = command;
        this.chance = chance;
        this.at = at;
    }

    public Map<String, Object> toConfigMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("command", this.command);
        map.put("chance", this.chance);
        map.put("at", this.at.name().toLowerCase());
        return map;
    }
}
