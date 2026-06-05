package com.powermobs.mobs.killcommands;

import com.powermobs.PowerMobsPlugin;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-mob configuration for the kill-commands hook.
 *
 * Disabled (empty list) when the YAML section is missing or invalid; callers
 * can short-circuit on {@link #isEmpty()}.
 */
@Getter
public class KillCommandsConfig {

    private final boolean playerRequired;
    private final List<KillCommand> commands;

    public KillCommandsConfig(boolean playerRequired, List<KillCommand> commands) {
        this.playerRequired = playerRequired;
        this.commands = commands;
    }

    public static KillCommandsConfig empty() {
        return new KillCommandsConfig(true, new ArrayList<>());
    }

    public boolean isEmpty() {
        return this.commands.isEmpty();
    }

    /**
     * Parses a "kill-commands:" section from a mob's config. Returns
     * {@link #empty()} (with the requested player-required default) if the
     * section is null or malformed.
     */
    public static KillCommandsConfig fromSection(String mobId, ConfigurationSection section) {
        if (section == null) {
            return empty();
        }

        boolean playerRequired = section.getBoolean("player-required", true);

        List<Map<?, ?>> rawList = section.getMapList("commands");
        if (rawList.isEmpty() && section.isList("commands")) {
            PowerMobsPlugin.getInstance().getLogger().warning(
                    "kill-commands.commands for mob '" + mobId + "' is not a list of maps; ignoring.");
            return new KillCommandsConfig(playerRequired, new ArrayList<>());
        }

        List<KillCommand> commands = new ArrayList<>();
        int index = 0;
        for (Map<?, ?> raw : rawList) {
            index++;
            Object cmdObj = raw.get("command");
            if (!(cmdObj instanceof String) || ((String) cmdObj).isBlank()) {
                PowerMobsPlugin.getInstance().getLogger().warning(
                        "kill-commands entry #" + index + " for mob '" + mobId
                                + "' is missing a non-empty 'command' string; skipping.");
                continue;
            }
            String command = (String) cmdObj;

            double chance = 1.0;
            Object chanceObj = raw.get("chance");
            if (chanceObj instanceof Number) {
                chance = ((Number) chanceObj).doubleValue();
            } else if (chanceObj != null) {
                PowerMobsPlugin.getInstance().getLogger().warning(
                        "kill-commands entry #" + index + " for mob '" + mobId
                                + "' has non-numeric 'chance'; defaulting to 1.0.");
            }
            if (chance < 0.0 || chance > 1.0) {
                PowerMobsPlugin.getInstance().getLogger().warning(
                        "kill-commands entry #" + index + " for mob '" + mobId
                                + "' has 'chance' outside [0.0, 1.0]; clamping.");
                chance = Math.max(0.0, Math.min(1.0, chance));
            }

            Object atObj = raw.get("at");
            AnchorType at = AnchorType.MOB;
            if (atObj instanceof String) {
                AnchorType parsed = AnchorType.fromString((String) atObj, null);
                if (parsed == null) {
                    PowerMobsPlugin.getInstance().getLogger().warning(
                            "kill-commands entry #" + index + " for mob '" + mobId
                                    + "' has invalid 'at' value '" + atObj + "'; defaulting to mob.");
                } else {
                    at = parsed;
                }
            } else if (atObj != null) {
                PowerMobsPlugin.getInstance().getLogger().warning(
                        "kill-commands entry #" + index + " for mob '" + mobId
                                + "' has non-string 'at' value; defaulting to mob.");
            }

            commands.add(new KillCommand(command, chance, at));
        }

        return new KillCommandsConfig(playerRequired, commands);
    }

    public Map<String, Object> toConfigMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("player-required", this.playerRequired);
        List<Map<String, Object>> list = new ArrayList<>();
        for (KillCommand cmd : this.commands) {
            list.add(cmd.toConfigMap());
        }
        map.put("commands", list);
        return map;
    }
}
