package io.github.townyadvanced.flagwar.war;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.actions.TownyExplodingBlocksEvent;
import com.palmergames.bukkit.towny.event.damage.TownyExplosionDamagesEntityEvent;
import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamageEntityEvent;
import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamagePlayerEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WarProcess implements Listener {
    ZonedDateTime startTime;
    int respawns = 0;
    Town aggressorTown;
    Town defenderTown;
    List<Resident> aggressors;
    List<Resident> defenders;
    List<Resident> participants;
    Set<Chunk> warChunks;
    Set<Chunk> sideWarChunks;
    Set<Chunk> allChunks;

    public WarProcess(Town aggressorTown, Town defenderTown) {
        this.defenderTown = defenderTown;
        this.aggressorTown = aggressorTown;
        this.aggressors = new ArrayList<>(aggressorTown.getResidents());
        this.defenders = new ArrayList<>(defenderTown.getResidents());
        this.participants = new ArrayList<>();
        this.participants.addAll(aggressors);
        this.participants.addAll(defenders);
        this.warChunks = new HashSet<>();
        this.sideWarChunks = new HashSet<>();
        this.allChunks = new HashSet<>();

        // Add all chunks of the defender town to warChunks
        Set<WorldCoord> townCoords = new HashSet<>();
        for (TownBlock townBlock : defenderTown.getTownBlocks()) {
            World world = Bukkit.getWorld(townBlock.getWorld().getName());
            if (world != null) {
                Chunk chunk = world.getChunkAt(townBlock.getX(), townBlock.getZ());
                warChunks.add(chunk);
                townCoords.add(townBlock.getWorldCoord());
            }
        }

        // Add chunks around the town that are within 2 chunks distance to sideWarChunks
        for (WorldCoord coord : townCoords) {
            World world = Bukkit.getWorld(coord.getWorldName());
            if (world != null) {
                for (int x = -2; x <= 2; x++) {
                    for (int z = -2; z <= 2; z++) {
                        WorldCoord adjacentCoord = new WorldCoord(coord.getWorldName(), coord.getX() + x, coord.getZ() + z);
                        if (!townCoords.contains(adjacentCoord)) {
                            Chunk adjacentChunk = world.getChunkAt(adjacentCoord.getX(), adjacentCoord.getZ());
                            sideWarChunks.add(adjacentChunk);
                        }
                    }
                }
            }
        }

        // Combine warChunks and sideWarChunks into allChunks
        allChunks.addAll(warChunks);
        allChunks.addAll(sideWarChunks);
    }
    //EVENTS

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAttackEvent(TownyPlayerDamagePlayerEvent damageEvent) {
        Town attackerTown = damageEvent.getAttackerTown();
        Town victimTown = damageEvent.getVictimTown();
        if (attackerTown == null || victimTown == null) return;
        Resident attackingResident = damageEvent.getAttackingResident();
        Resident victimResident = damageEvent.getVictimResident();
        if (attackingResident == null || victimResident == null) return;
        if (participants.contains(attackingResident) && participants.contains(victimResident)) {
            if (attackerTown.equals(victimTown)) {
                damageEvent.setCancelled(true);
                return;
            }
            Player attacker = attackingResident.getPlayer();
            if (attacker == null) return;
            if (allChunks.contains(attacker.getChunk())) {
                damageEvent.setCancelled(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAttackEvent(TownyPlayerDamageEntityEvent damageEvent) {
        Player attacker = damageEvent.getAttackingPlayer();
        if (attacker == null) return;
        if (participants.contains(TownyAPI.getInstance().getResident(attacker))) {
            if (allChunks.contains(damageEvent.getEntity().getChunk())) {
                damageEvent.setCancelled(false);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onExplosionDamagesEvent(TownyExplosionDamagesEntityEvent explosionDamagesEvent) {
        if (!explosionDamagesEvent.isCancelled()) return;
        Chunk chunk = explosionDamagesEvent.getLocation().getChunk();
        if (allChunks.contains(chunk)) explosionDamagesEvent.setCancelled(false);
    }

    @EventHandler
    public void onRespawnEvent(PlayerRespawnEvent respawnEvent) {
        Resident resident = TownyAPI.getInstance().getResident(respawnEvent.getPlayer());
        if (!participants.contains(resident)) return;

    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent respawnEvent) {
        Resident resident = TownyAPI.getInstance().getResident(respawnEvent.getPlayer());
        if (!participants.contains(resident)) return;

    }

    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent deathEvent {

    }

}
