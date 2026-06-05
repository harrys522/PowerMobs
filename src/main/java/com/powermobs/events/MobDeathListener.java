package com.powermobs.events;

import com.powermobs.PowerMobsPlugin;
import com.powermobs.config.PowerMobConfig;
import com.powermobs.mobs.PowerMob;
import com.powermobs.mobs.killcommands.KillCommandsConfig;
import com.powermobs.mobs.killcommands.KillCommandsDispatcher;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Random;

/**
 * Handles power mob death events
 */
@RequiredArgsConstructor
public class MobDeathListener implements Listener {

    private final PowerMobsPlugin plugin;

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        PowerMob powerMob = PowerMob.getFromEntity(this.plugin, entity);

        if (powerMob == null) {
            return;
        }

        // Clear default drops
        event.getDrops().clear();
        event.setDroppedExp(0);

        // Get the killer player (if any)
        Player killer = entity.getKiller();
        plugin.debug("Killer: " + killer, "mob_combat");

        // Process drops using the drop handler
        plugin.getDropHandler().processDrops(powerMob, killer, entity.getLocation());

        // Run configured kill commands (no-op when section absent or empty)
        KillCommandsConfig killCommands = resolveKillCommands(powerMob);
        if (killCommands != null) {
            KillCommandsDispatcher.dispatch(plugin, killCommands, powerMob.getId(), entity, killer);
        }

        // Clean up tracking data AFTER processing drops
        plugin.getDamageTracker().cleanupMob(powerMob.getEntityUuid());

        // Unregister the power mob
        powerMob.remove();
        this.plugin.getPowerMobManager().unregisterPowerMob(powerMob);
    }

    private KillCommandsConfig resolveKillCommands(PowerMob powerMob) {
        PowerMobConfig cfg = this.plugin.getConfigManager().getPowerMob(powerMob.getId());
        return cfg != null ? cfg.getKillCommands() : null;
    }

}