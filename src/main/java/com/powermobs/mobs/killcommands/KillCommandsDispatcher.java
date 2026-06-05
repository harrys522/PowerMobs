package com.powermobs.mobs.killcommands;

import com.powermobs.PowerMobsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Random;

/**
 * Runs the configured kill-commands for a slain power mob.
 *
 * Contract:
 *  - All entries are dispatched from the console sender (full permissions, no
 *    "you do not have permission" gotchas for the killer).
 *  - The {@code at:} field on each entry controls position anchoring via a
 *    vanilla {@code execute} wrapper:
 *      - {@code at: mob}    →  execute in &lt;dim&gt; positioned &lt;mob.xyz&gt; run &lt;cmd&gt;
 *      - {@code at: killer} →  execute at &lt;killer-name&gt; run &lt;cmd&gt;
 *    So {@code ~ ~ ~} inside &lt;cmd&gt; resolves to the mob's death point or the
 *    killer's position respectively.
 *  - Synchronous, ordered, best-effort. A thrown exception, an unknown command
 *    (e.g. target plugin not installed), or a refused execution is logged and
 *    the next entry still runs.
 */
public final class KillCommandsDispatcher {

    private static final Random RANDOM = new Random();

    private KillCommandsDispatcher() {
    }

    public static void dispatch(
            PowerMobsPlugin plugin,
            KillCommandsConfig config,
            String mobId,
            LivingEntity entity,
            Player killer
    ) {
        if (config == null || config.isEmpty()) {
            return;
        }
        if (config.isPlayerRequired() && killer == null) {
            plugin.debug("kill-commands for '" + mobId + "' skipped: no player killer", "mob_combat");
            return;
        }

        for (KillCommand cmd : config.getCommands()) {
            dispatchOne(plugin, cmd, mobId, entity, killer);
        }
    }

    private static void dispatchOne(
            PowerMobsPlugin plugin,
            KillCommand cmd,
            String mobId,
            LivingEntity entity,
            Player killer
    ) {
        if (cmd.getChance() <= 0.0) {
            return;
        }
        if (cmd.getChance() < 1.0 && RANDOM.nextDouble() >= cmd.getChance()) {
            return;
        }

        if (cmd.getAt() == AnchorType.KILLER) {
            if (killer == null) {
                plugin.debug("kill-commands entry '" + cmd.getCommand()
                        + "' skipped: at=killer but no killer", "mob_combat");
                return;
            }
            if (!killer.isOnline()) {
                plugin.debug("kill-commands entry '" + cmd.getCommand()
                        + "' skipped: killer " + killer.getName() + " is offline", "mob_combat");
                return;
            }
        }

        String resolved = resolvePlaceholders(cmd.getCommand(), mobId, entity, killer);
        resolved = normalizeForDispatch(resolved);
        if (resolved == null || resolved.isEmpty()) {
            plugin.debug("kill-commands: skipped empty command after placeholder substitution "
                    + "(template: '" + cmd.getCommand() + "')", "mob_combat");
            return;
        }

        String wrapped = wrapForAnchor(plugin, cmd, entity, killer, resolved);
        if (wrapped == null) {
            return;
        }

        try {
            boolean ok = Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), wrapped);
            if (!ok) {
                plugin.debug("kill-commands: '" + wrapped
                        + "' returned false (unknown command or refused execution)", "mob_combat");
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("kill-commands: '" + wrapped
                    + "' threw during dispatch: " + t.getMessage());
        }
    }

    private static String wrapForAnchor(
            PowerMobsPlugin plugin,
            KillCommand cmd,
            LivingEntity entity,
            Player killer,
            String inner
    ) {
        if (cmd.getAt() == AnchorType.KILLER) {
            // killer is guaranteed non-null by dispatchOne's earlier guard.
            return "execute at " + killer.getName() + " run " + inner;
        }

        // AnchorType.MOB
        Location loc = entity != null ? entity.getLocation() : null;
        World world = loc != null ? loc.getWorld() : null;
        if (loc == null || world == null) {
            plugin.debug("kill-commands entry '" + cmd.getCommand()
                    + "' skipped: mob has no resolvable location", "mob_combat");
            return null;
        }
        String dimension = world.getKey().asString();
        return "execute in " + dimension
                + " positioned " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()
                + " run " + inner;
    }

    /**
     * dispatchCommand requires no leading slash; tolerate either form and trim.
     */
    static String normalizeForDispatch(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        while (s.startsWith("/")) {
            s = s.substring(1).trim();
        }
        return s;
    }

    public static String resolvePlaceholders(
            String template,
            String mobId,
            LivingEntity entity,
            Player killer
    ) {
        if (template == null || template.isEmpty()) {
            return template;
        }

        String playerName = killer != null ? killer.getName() : "";
        String playerUuid = killer != null ? killer.getUniqueId().toString() : "";

        Location loc = entity != null ? entity.getLocation() : null;
        World w = loc != null ? loc.getWorld() : null;
        String world = w != null ? w.getName() : "";
        String dimension = w != null ? w.getKey().asString() : "";
        String x = loc != null ? Integer.toString(loc.getBlockX()) : "";
        String y = loc != null ? Integer.toString(loc.getBlockY()) : "";
        String z = loc != null ? Integer.toString(loc.getBlockZ()) : "";

        String mobName = "";
        if (entity != null && entity.getCustomName() != null) {
            mobName = ChatColor.stripColor(entity.getCustomName());
        }

        return template
                .replace("{player}", playerName)
                .replace("{player_uuid}", playerUuid)
                .replace("{world}", world)
                .replace("{dimension}", dimension)
                .replace("{x}", x)
                .replace("{y}", y)
                .replace("{z}", z)
                .replace("{mob_id}", mobId != null ? mobId : "")
                .replace("{mob_name}", mobName);
    }
}
