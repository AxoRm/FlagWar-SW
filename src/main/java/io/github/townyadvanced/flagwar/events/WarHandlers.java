package io.github.townyadvanced.flagwar.events;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.TownAddResidentEvent;
import com.palmergames.bukkit.towny.event.damage.TownyExplosionDamagesEntityEvent;
import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamagePlayerEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.flagwar.FlagWar;
import io.github.townyadvanced.flagwar.storage.NewWar;
import io.github.townyadvanced.flagwar.war.WarProcess;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.Map;

public class WarHandlers implements Listener {

    public WarHandlers() {
        Bukkit.getPluginManager().registerEvents(this, FlagWar.getInstance());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAttackEvent(TownyPlayerDamagePlayerEvent damageEvent) {
        Town attacker = damageEvent.getAttackerTown();
        Town defender = damageEvent.getVictimTown();
        if (attacker == null || defender == null) return;
        WarProcess process = FlagWar.warManager.getWarProcess(attacker, defender);
        if (process == null) return;
        process.onAttack(damageEvent);
    }
}
