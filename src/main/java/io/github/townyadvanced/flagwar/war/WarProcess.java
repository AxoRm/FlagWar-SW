package io.github.townyadvanced.flagwar.war;

import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamagePlayerEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

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
    List<Resident>
    Set<Chunk> warChunks;
    Set<Chunk> sideWarChunks;

    public WarProcess(Town aggressorTown, Town defenderTown) {
        this.defenderTown = defenderTown;
        this.aggressorTown = aggressorTown;
        this.aggressors = aggressorTown.getResidents();
        this.defenders = defenderTown.getResidents();
        this.warChunks = new HashSet<>();
        this.sideWarChunks = new HashSet<>();

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

        // Add chunks around the town that are within 2 chunks distance
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

    }
    //EVENTS

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAttackEvent(TownyPlayerDamagePlayerEvent damageEvent) {
        Resident attackingResident = damageEvent.getAttackingResident();
        Resident victimResident = damageEvent.getVictimResident();
        if
    }
}
